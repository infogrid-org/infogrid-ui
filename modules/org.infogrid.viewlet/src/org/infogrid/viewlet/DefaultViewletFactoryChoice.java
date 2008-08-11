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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.viewlet;

import java.util.ArrayList;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * Default implementation for ViewletFactoryChoice.
 */
public class DefaultViewletFactoryChoice
        extends
            ViewletFactoryChoice
{
    private static final Log log = Log.getLogInstance( DefaultViewletFactoryChoice.class ); // our own, private logger

    /**
     * Factory method. Assume default match quality.
     *
     * @param viewletClass the Viewlet's class
     * @return the created DefaultViewletFactoryChoice
     */
    public static DefaultViewletFactoryChoice create(
            Class<? extends Viewlet> viewletClass )
    {
        return new DefaultViewletFactoryChoice( viewletClass, AVERAGE_MATCH_QUALITY );
    }

    /**
     * Factory method. Assume default match quality.
     *
     * @param viewletClass the Viewlet's class
     * @param matchQuality the match quality
     * @return the created DefaultViewletFactoryChoice
     */
    public static DefaultViewletFactoryChoice create(
            Class<? extends Viewlet> viewletClass,
            double                   matchQuality )
    {
        return new DefaultViewletFactoryChoice( viewletClass, matchQuality );
    }
    
    /**
     * Private constructor, use factory method. Specify match quality.
     *
     * @param viewletClass the Viewlet's class
     * @param matchQuality the match quality
     */
    protected DefaultViewletFactoryChoice(
            Class<? extends Viewlet> viewletClass,
            double                   matchQuality )
    {
        theViewletClass = viewletClass;
        theMatchQuality = matchQuality;
    }
    
    /**
     * Obtain a user-visible String to display to the user for this ViewletFactoryChoice.
     *
     * @return the user-visible String
     */
    public String getUserVisibleName()
    {
        ResourceHelper rh  = ResourceHelper.getInstance( theViewletClass );
        String         ret = rh.getResourceStringOrDefault( "UserVisibleName", theViewletClass.getName() );

        return ret;
    }

    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewlet's name
     */
    public String getName()
    {
        return getImplementationName();
    }

    /**
      * Obtain the names of the interfaces provided by this ViewletFactoryChoice.
      *
      * @return the names of the interfaces provided by this ViewletFactoryChoice.
      */
    public String [] getInterfaceNames()
    {
        ArrayList<String> almost = new ArrayList<String>();
        
        determineClassNames( theViewletClass, almost );

        String [] ret = ArrayHelper.copyIntoNewArray( almost, String.class );
        return ret;
    }
    
    /**
     * Internal helper method that recursively looks up the names of all interface
     * and class names supported by a Class.
     *
     * @param clazz the Class
     * @param found the set of names found
     */
    protected void determineClassNames(
            Class             clazz,
            ArrayList<String> found )
    {
        found.add( clazz.getName() );
        
        Class toAdd = clazz.getSuperclass();
        if( toAdd != null ) {
            determineClassNames( toAdd, found );
        }
        for( Class intfc : clazz.getInterfaces()) {
            if( !found.contains( intfc.getName() )) {
                determineClassNames( intfc, found );
            }
        }
    }
    
    /**
     * Obtain the name of the Viewlet's implementation.
     *
     * @return the implementation name
     */
    public String getImplementationName()
    {
        return theViewletClass.getName();
    }

    /**
     * Obtain a measure of the match quality. 0 means &quot;perfect match&quot;,
     * while larger numbers mean increasingly worse match quality.
     * 
     * @param toView the MeshObjectsToView to match against
     * @return the match quality
     * @see ViewletFactoryChoice#PERFECT_MATCH_QUALITY
     * @see ViewletFactoryChoice#AVERAGE_MATCH_QUALITY
     * @see ViewletFactoryChoice#WORST_MATCH_QUALITY
     */
    public double getMatchQualityFor(
            MeshObjectsToView toView )
    {
        return theMatchQuality;
    }

    /**
     * Instantiate a ViewletFactoryChoice into a Viewlet.
     * 
     * @param toView the MeshObjectsToView; only used for error reporting
     * @param c the Context to use
     * @return the instantiated Viewlet
     * @throws CannotViewException if, against expectations, the Viewlet corresponding
     *         to this ViewletFactoryChoice could not view the MeshObjectsToView after
     *         all. This usually indicates a programming error.
     */
    public Viewlet instantiateViewlet(
            MeshObjectsToView toView,
            Context           c )
        throws
            CannotViewException
    {
        return instantiateViewlet( toView, theViewletClass, c );
    }
    
    /**
     * The Viewlet's class.
     */
    protected Class<? extends Viewlet> theViewletClass;
    
    /**
     * The match quality.
     * 
     * @see ViewletFactoryChoice#PERFECT_MATCH_QUALITY
     * @see ViewletFactoryChoice#AVERAGE_MATCH_QUALITY
     * @see ViewletFactoryChoice#WORST_MATCH_QUALITY
     */
    protected double theMatchQuality;
}
