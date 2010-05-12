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

import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * A ViewletFactoryChoice whose quality has been overridden, but otherwise delegates
 * to another ViewletFactoryChoice.
 */
public class MatchQualityChangedViewletFactoryChoice
        extends
            AbstractViewletFactoryChoice
{
    /**
     * Constructor.
     *
     * @param toView the MeshObjectsToView for which this is a choice
     * @param matchQuality the overridden match quality
     * @param delegate the underlying ViewletFactoryChoice
     */
    public MatchQualityChangedViewletFactoryChoice(
            MeshObjectsToView    toView,
            double               matchQuality,
            ViewletFactoryChoice delegate )
    {
        super( toView );
        
        theMatchQuality = matchQuality;
        theDelegate     = delegate;
    }
    
    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewlet's name
     */
    @Override
    public String getName()
    {
        return theDelegate.getName();
    }

    /**
      * Obtain the names of the interfaces provided by this ViewletFactoryChoice.
      *
      * @return the names of the interfaces provided by this ViewletFactoryChoice.
      */
    public String [] getInterfaceNames()
    {
        return theDelegate.getInterfaceNames();
    }

    /**
     * Obtain the name of the Viewlet's implementation.
     *
     * @return the implementation name
     */
    public String getImplementationName()
    {
        return theDelegate.getImplementationName();
    }

    /**
     * Obtain a measure of the match quality. 0 means &quot;perfect match&quot;,
     * while larger numbers mean increasingly worse match quality.
     *
     * @param toView the MeshObjectsToView to match against
     * @return the match quality
     */
    @Override
    public double getMatchQualityFor(
            MeshObjectsToView toView )
    {
        return theMatchQuality;
    }

    /**
     * Instantiate a ViewletFactoryChoice into a Viewlet. The caller still must call
     * {org.infogrid.viewlet.Viewlet#view Viewlet.view} after having called
     * this method.
     * 
     * @return the instantiated Viewlet
     */
    public Viewlet instantiateViewlet()
        throws
            CannotViewException
    {
        return theDelegate.instantiateViewlet();
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
        String delegateString = theDelegate.toStringRepresentation( rep, pars );

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                StringRepresentation.DEFAULT_ENTRY,
                pars,
        /* 0 */ this,
        /* 1 */ delegateString,
        /* 2 */ theMatchQuality );

        return ret;
    }

    /**
     * The underlying ViewletFactoryChoice that we override and delegate to.
     */
    protected ViewletFactoryChoice theDelegate;

    /**
     * The match quality.
     *
     * @see ViewletFactoryChoice#PERFECT_MATCH_QUALITY
     * @see ViewletFactoryChoice#AVERAGE_MATCH_QUALITY
     * @see ViewletFactoryChoice#WORST_MATCH_QUALITY
     */
    protected double theMatchQuality;
}
