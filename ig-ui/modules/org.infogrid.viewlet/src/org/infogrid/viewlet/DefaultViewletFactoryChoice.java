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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.viewlet;

import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Default implementation for ViewletFactoryChoice.
 */
public abstract class DefaultViewletFactoryChoice
        extends
            AbstractViewletFactoryChoice
{
    /**
     * Private constructor, use factory method. Specify match quality.
     *
     * @param toView the MeshObjectsToView for which this is a choice
     * @param viewletClass the Viewlet's class
     * @param viewletTypeName name of the Viewlet type that this represents, if any
     * @param matchQuality the match quality
     */
    protected DefaultViewletFactoryChoice(
            MeshObjectsToView        toView,
            Class<? extends Viewlet> viewletClass,
            String                   viewletTypeName,
            double                   matchQuality )
    {
        super( possiblyCreateCopyWithViewletType( toView, viewletTypeName ) );

        theMatchQuality = matchQuality;
        theViewletClass = viewletClass;
    }

    /**
     * Helper method to create a copy of a MeshObjectsToView and set the correct Viewlet type.
     *
     * @param original the MeshObjectsToView to copy
     * @param viewletTypeName the Viewlet type
     * @return the adjusted MeshObjectsToView
     */
    static MeshObjectsToView possiblyCreateCopyWithViewletType(
            MeshObjectsToView original,
            String            viewletTypeName )
    {
        if( StringHelper.compareTo( original.getViewletTypeName(), viewletTypeName ) == 0 ) {
            return original;
        }
        MeshObjectsToView ret = original.createCopy();
        ret.setViewletTypeName( viewletTypeName );

        return ret;
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
      * Obtain the names of the interfaces provided by this ViewletFactoryChoice.
      *
      * @return the names of the interfaces provided by this ViewletFactoryChoice.
      */
    public String [] getInterfaceNames()
    {
        return getInterfaceNames( theViewletClass );
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
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        String userVisibleName = ResourceHelper.getInstance( theViewletClass ).getResourceStringOrDefault( "UserVisibleName", theViewletClass.getName() );
        
        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                StringRepresentation.DEFAULT_ENTRY,
                pars,
        /* 0 */ this,
        /* 1 */ theViewletClass.getName(),
        /* 2 */ userVisibleName,
        /* 2 */ theMatchQuality );

        return ret;
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
