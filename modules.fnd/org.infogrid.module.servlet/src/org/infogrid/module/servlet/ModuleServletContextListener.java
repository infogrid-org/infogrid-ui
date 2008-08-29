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

package org.infogrid.module.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.InputStream;

/**
 * This class may be specified as a ServletContextListener in the web.xml file
 * of a Servlet application that uses the Module framework.
 */
public class ModuleServletContextListener
        implements
            ServletContextListener
{
    /**
     * The application is being deployed.
     *
     * @param sce the event
     */
    public void contextInitialized(
             ServletContextEvent sce )
    {
        String propertiesFile = sce.getServletContext().getInitParameter( ServletBootLoader.BOOTLOADER_PROPERTIES_FILE_NAME );

        ClassLoader loader     = ModuleServletContextListener.class.getClassLoader();
        InputStream propStream = loader.getResourceAsStream( propertiesFile );

        ServletBootLoader.initialize( propStream );
    }

    /**
     * The application is being undeployed.
     *
     * @param sce the event
     */
    public void contextDestroyed(
            ServletContextEvent sce )
    {
        // don't do nothing
    }
}
