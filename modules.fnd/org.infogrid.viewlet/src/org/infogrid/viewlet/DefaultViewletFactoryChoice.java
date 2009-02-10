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

import org.infogrid.util.ResourceHelper;

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
     * @param viewletClass the Viewlet's class
     * @param matchQuality the match quality
     */
    protected DefaultViewletFactoryChoice(
            Class<? extends Viewlet> viewletClass,
            double                   matchQuality )
    {
        super( matchQuality );

        theViewletClass = viewletClass;
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
     * The Viewlet's class.
     */
    protected Class<? extends Viewlet> theViewletClass;
}
