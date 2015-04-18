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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.sane;

import javax.servlet.http.HttpServletRequest;
import org.infogrid.util.http.SaneRequest;

/**
 * Utilities to determine the correct URLs for an application that is currently run staged
 * rather than in production.
 */
public abstract class StageUtils
{
    /**
     * Private constructor, this is a non-instantiable class.
     */
    private StageUtils()
    {
        // nothing
    }

    /**
     * Determine whether the application is run staged or in production.
     *
     * @param request the incoming request
     * @param productionLocation the URL of the production location of the application, or the hostname at which it runs
     * @return true if the application is staged
     */
    public static boolean isStaged(
            SaneRequest request,
            String      productionLocation )
    {
        if( productionLocation == null || productionLocation.length() == 0 ) {
            return true;
        }

        if( request.getAbsoluteFullUri().startsWith( productionLocation )) {
            return false;
        }
        if( request.getServer().equals( productionLocation )) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether the application is run staged or in production.
     *
     * @param request the incoming request
     * @param productionLocation the URL of the production location of the application, or the hostname at which it runs
     * @return true if the application is staged
     */
    public static boolean isStaged(
            HttpServletRequest request,
            String             productionLocation )
    {
        SaneRequest sane = SaneServletRequest.create( request );

        return isStaged( sane, productionLocation );
    }
}
