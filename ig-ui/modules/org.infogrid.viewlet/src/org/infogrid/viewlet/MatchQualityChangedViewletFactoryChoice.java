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

import org.infogrid.util.context.Context;

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
     * @param matchQuality the overridden match quality
     * @param delegate the underlying ViewletFactoryChoice
     */
    public MatchQualityChangedViewletFactoryChoice(
            double               matchQuality,
            ViewletFactoryChoice delegate )
    {
        super( matchQuality );

        theDelegate= delegate;
    }
    
    /**
     * Obtain a user-visible String to display to the user for this ViewletFactoryChoice.
     *
     * @return the user-visible String
     */
    public String getUserVisibleName()
    {
        return theDelegate.getUserVisibleName();
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
     * @param toView the MeshObjectsToView; only used for error reporting
     * @param parent the parent Viewlet, if any
     * @param c the Context to use
     * @return the instantiated Viewlet
     */
    public Viewlet instantiateViewlet(
            MeshObjectsToView        toView,
            Viewlet                  parent,
            Context                  c )
        throws
            CannotViewException
    {
        return theDelegate.instantiateViewlet( toView, parent, c );
    }
    
    /**
     * The underlying ViewletFactoryChoice that we override and delegate to.
     */
    protected ViewletFactoryChoice theDelegate;
}
