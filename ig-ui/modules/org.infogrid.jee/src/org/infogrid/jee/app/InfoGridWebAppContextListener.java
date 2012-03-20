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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.app;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.infogrid.jee.servlet.AbstractInfoGridServlet;

/**
 * Listens to ServletContext events and destroys InfoGridWebApp appropriately.
 */
public class InfoGridWebAppContextListener
        implements
            ServletContextListener
{
    /**
     * The application has been initialized.
     *
     * @param event the event
     */
    public void contextInitialized(
            ServletContextEvent event )
    {
        // do nothing. We initialize when the first HTTP request comes in.
    }

    /**
     * The application has been destroyed. Clean up.
     *
     * @param event the event
     */
    public void contextDestroyed(
            ServletContextEvent event )
    {
        InfoGridWebApp app = null;
        
        try {
            app = (InfoGridWebApp) event.getServletContext().getAttribute( AbstractInfoGridServlet.INFOGRID_WEB_APP_NAME );
        } catch( Throwable t ) {
            // swallow everything. Tomcat 5.5 sometimes throws NoClassDefFoundErrors here.
        }

        if( app != null ) {
            // may be destroyed before it was accessed even once
            app.die();
        }
    }
}
