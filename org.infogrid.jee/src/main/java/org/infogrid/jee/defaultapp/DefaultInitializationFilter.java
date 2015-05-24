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

package org.infogrid.jee.defaultapp;

import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.AbstractInfoGridServlet;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.util.QuitManager;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Configures the default InfoGridWebApp with log4j logging.
 */
public class DefaultInitializationFilter
        extends
            InitializationFilter
{
    /**
     * Public constructor.
     */
    public DefaultInitializationFilter()
    {
        // nothing right now
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
     * Initialize the InfoGridWebApp if needed.
     *
     * @param incomingRequest the incoming request
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    @Override
    protected void initializeInfoGridWebApp(
            HttpServletRequest incomingRequest )
        throws
            ServletException
    {
        InfoGridWebApp theApp = getInfoGridWebApp();
        if( theApp == null ) {
            // try again, this time synchronized
            synchronized( DefaultInitializationFilter.class ) {
                theApp = getInfoGridWebApp();
                if( theApp == null ) {
                    SaneRequest lidRequest = SaneServletRequest.create( incomingRequest );

                    theApp = createInfoGridWebApp( lidRequest );

                    try {
                        theFilterConfig.getServletContext().setAttribute( AbstractInfoGridServlet.INFOGRID_WEB_APP_NAME, theApp );

                    } catch( IllegalStateException ex ) {
                        // have one already, that's fine (a parallel thread was faster)
                    }
                }
            }
        }
    }

    /**
     * Create the InfoGridWebApp instance.
     *
     * @param lidRequest the incoming request
     * @return the created InfoGridWebApp
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    protected InfoGridWebApp createInfoGridWebApp(
            SaneRequest lidRequest )
        throws
            ServletException
    {
        ResourceHelper.initializeLogging();

        log = Log.getLogInstance( getClass() );

        // Context
        SimpleContext rootContext = SimpleContext.createRoot( theFilterConfig.getServletContext().getServletContextName() + " root context" );
        rootContext.addContextObject( QuitManager.create() );

        // app
        DefaultInfoGridWebApp ret = new DefaultInfoGridWebApp( lidRequest, rootContext );
        rootContext.addContextObject( ret );

        return ret;        
    }

    /**
     * Name of the Filter parameter that contains the name of root module to activate.
     */
    public static final String ROOT_MODULE_NAME_PARAMETER = "ROOTMODULE";
}
