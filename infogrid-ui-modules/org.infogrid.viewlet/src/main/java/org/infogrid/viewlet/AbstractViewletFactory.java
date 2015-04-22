//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.viewlet;

import java.util.Arrays;
import java.util.Comparator;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.Viewlet.ViewletSubjectArea;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.FactoryException;
import org.infogrid.util.logging.Log;

/**
 * Factors out functionality common to {@link ViewletFactory ViewletFactories}.
 */
public abstract class AbstractViewletFactory
        extends
            AbstractFactory<MeshObjectsToView,Viewlet,Void>
        implements
            ViewletFactory
{
    private static final Log log = Log.getLogInstance( AbstractViewletFactory.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param implementationMarkerInterfaceName name of an interface that indicates the right implementation
     *        technology for a given Viewlet, e.g. "org.viewlet.swing.SwingViewlet".
     * @throws IllegalArgumentException if an empty implementationMarkerInterfaceName was provided
     */
    protected AbstractViewletFactory(
            String implementationMarkerInterfaceName )
    {
        if( implementationMarkerInterfaceName == null || implementationMarkerInterfaceName.length() == 0 ) {
            throw new IllegalArgumentException( "Must specify an implementationMarkerInterfaceName." );
        }
        theImplementationMarkerInterfaceName = implementationMarkerInterfaceName;
    }
    
    /**
     * Factory method.
     *
     * @param theObjectsToView the MeshObjectsToView by the to-be-created Viewlet
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public Viewlet obtainFor(
            MeshObjectsToView  theObjectsToView,
            Void               argument )
        throws
            FactoryException
    {
        ViewletFactoryChoice [] candidates = determineFactoryChoicesOrderedByMatchQuality( theObjectsToView );

        if( candidates.length == 0 ) {
            throw new NoViewletFoundException( this, theObjectsToView );
        }

        try {
            return candidates[0].instantiateViewlet();

        } catch( CannotViewException ex ) {
            throw new FactoryException( this, ex );
        }
    }

    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, and return them in
     * order of match quality.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoicesOrderedByMatchQuality(
            MeshObjectsToView theObjectsToView )
    {
        ViewletFactoryChoice [] ret = determineFactoryChoices( theObjectsToView );
        
        Arrays.sort( ret, new ByQualityComparator( theObjectsToView ) );
        
        return ret;
    }
    
    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoices(
            MeshObjectsToView theObjectsToView )
    {
        // find substitute viewlet type name if none given
        MeshObject subject         = theObjectsToView.getSubject();
        String     viewletTypeName = theObjectsToView.getViewletTypeName();

        if( viewletTypeName == null ) {
            // see whether the MeshObject itself specifies what it wants

            if( subject.isBlessedBy( ViewletSubjectArea.VIEWLETPREFERENCE )) {
                try {
                    StringValue preferred = (StringValue) subject.getPropertyValue( ViewletSubjectArea.VIEWLETPREFERENCE_VIEWLETTYPE );
                    if( preferred != null ) {
                        viewletTypeName = preferred.value();
                    }
                } catch( IllegalPropertyTypeException ex ) {
                    log.error( ex );
                } catch( NotPermittedException ex ) {
                    log.info( ex );
                }
            }
        }

        ViewletFactoryChoice [] withoutType = determineFactoryChoicesIgnoringType( theObjectsToView );
        // now:
        // - weed out those that do not match the implementation technology
        // - bolster the ones that have the right type

        ViewletFactoryChoice [] ret = new ViewletFactoryChoice[ withoutType.length ];
        int count = 0;
        
        for( int i=0 ; i<ret.length ; ++i ) {
            boolean foundImplementation = false;
            boolean foundType           = false;
            for( String intfc : withoutType[i].getInterfaceNames() ) {
                if( viewletTypeName != null && viewletTypeName.equals( intfc )) {
                    foundType = true;
                }
                if( theImplementationMarkerInterfaceName.equals( intfc )) {
                    foundImplementation = true;
                    break;
                }
            }
            if( foundImplementation ) {
                if( foundType ) {
                    ret[count++] = new MatchQualityChangedViewletFactoryChoice(
                            theObjectsToView,
                            ViewletFactoryChoice.USER_SELECTED_MATCH_QUALITY,
                            withoutType[i] );
                } else {
                    ret[count++] = withoutType[i];
                }
            }
        }
        // shorten array if needed
        if( count < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, count, ViewletFactoryChoice.class );
        }
        return ret;
    }
    
    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, but ignore the specified
     * viewlet type. If none are found, return an emtpy array.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public abstract ViewletFactoryChoice [] determineFactoryChoicesIgnoringType(
            MeshObjectsToView theObjectsToView );

    /**
     * The name of the interface that indicates the implementation technology and that needs to
     * be supported by all Viewlets returned by this (implementation-specific) factory.
     */
    protected String theImplementationMarkerInterfaceName;
    
    /**
     * Defines the ordering of ViewletFactoryChoices. This is implemented as a separate
     * Comparator rather than using &quot;natural ordering&quot; because there is no natural order of
     * ViewletFactoryChoices without the MeshObjectsToView instance, which doesn't
     * fit into the JDK API.
     */
    protected static class ByQualityComparator
        implements
            Comparator<ViewletFactoryChoice>
    {
        /**
         * Constructor.
         *
         * @param toView the MeshObjectsToView to use to compare
         */
        public ByQualityComparator(
                MeshObjectsToView toView )
        {
            theToView = toView;
        }
        
        /**
         * Compares its two arguments for order.  Returns a negative integer,
         * zero, or a positive integer as the first argument is less than, equal
         * to, or greater than the second.<p>
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         * @return a negative integer, zero, or a positive integer as the
         * 	       first argument is less than, equal to, or greater than the
         *	       second. 
         */
        public int compare(
                ViewletFactoryChoice o1,
                ViewletFactoryChoice o2)
        {
            double q1 = o1.getMatchQualityFor( theToView );
            double q2 = o2.getMatchQualityFor( theToView );
            
            if( q1 == q2 ) {
                return 0;
            } else {
                return (int) ( q1 - q2 );
            }
        }

        /**
         * The MeshObjectsToView to use.
         */
        protected MeshObjectsToView theToView;
    }
}
