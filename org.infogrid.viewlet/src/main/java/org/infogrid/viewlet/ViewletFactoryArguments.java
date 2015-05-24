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

import org.infogrid.util.context.Context;

/**
 * Helper class to collect the arguments into the ViewletFactory method when creating Viewlets.
 */
public class ViewletFactoryArguments
{
    /**
     * Factory method.
     *
     * @param parent the parent Viewlet, if any
     * @param c the Context for the Viewlet
     * @return the created ViewletFactoryArguments
     */
    public static ViewletFactoryArguments create(
            Viewlet parent,
            Context c )
    {
        return new ViewletFactoryArguments( parent, c );
    }

    /**
     * Factory method.
     *
     * @param c the Context for the Viewlet
     * @return the created ViewletFactoryArguments
     */
    public static ViewletFactoryArguments create(
            Context c )
    {
        return new ViewletFactoryArguments( null, c );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param parent the parent Viewlet, if any
     * @param c the Context for the Viewlet
     */
    protected ViewletFactoryArguments(
            Viewlet parent,
            Context c )
    {
        theParent  = parent;
        theContext = c;
    }

    /**
     * Obtain the desired parent Viewlet.
     *
     * @return the parent Viewlet
     */
    public final Viewlet getParent()
    {
        return theParent;
    }

    /**
     * Obtain the desired Context.
     *
     * @return the Context
     */
    public final Context getContext()
    {
        return theContext;
    }

    /**
     * The parent Viewlet, if any.
     */
    protected Viewlet theParent;

    /**
     * The Context.
     */
    protected Context theContext;
}
