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

package org.infogrid.jee.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.infogrid.jee.app.InfoGridWebApp;

/**
 * Abstract Filter that makes it easier for subclasses to find InfoGridWebApp information.
 */
public abstract class AbstractInfoGridWebAppFilter
        implements
            Filter
{
    /**
     * Constructor.
     */
    public AbstractInfoGridWebAppFilter()
    {
        // noop
    }

    /**
     * Initialization method for this filter.
     *
     * @param filterConfig the filter configuration
     * @throws ServletException an exception occurred
     */
    public final void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theFilterConfig = filterConfig;

        internalInit( filterConfig );
    }

    /**
     * Override this method instead of init.
     *
     * @param filterConfig the filter configuration
     * @throws ServletException an exception occurred
     */
    protected void internalInit(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        // nothing on this level
    }

    /**
     * Find the InfoGridWebApp object.
     *
     * @return the InfoGridWebApp object
     */
    protected InfoGridWebApp getInfoGridWebApp()
    {
        InfoGridWebApp ret = (InfoGridWebApp) theFilterConfig.getServletContext().getAttribute( AbstractInfoGridServlet.INFOGRID_WEB_APP_NAME );
        return ret;
    }

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;
}
