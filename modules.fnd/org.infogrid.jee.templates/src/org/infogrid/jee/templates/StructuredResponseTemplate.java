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

package org.infogrid.jee.templates;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * A template for a StructuredResponse.
 */
public interface StructuredResponseTemplate
{
    /**
     * Stream a StructuredResponse to an HttpResponse employing this template.
     * 
     * @param delegate the delegate to stream to
     * @param structured the StructuredResponse
     * @throws ServletException exception passed on from underlying servlet output
     * @throws IOException exception passed on from underlying servlet output
     */
    public void doOutput(
            HttpServletResponse delegate,
            StructuredResponse  structured )
        throws
            ServletException,
            IOException;

    /**
     * Name of the LID template parameter.
     */
    public static final String LID_TEMPLATE_PARAMETER_NAME = "lid-template";
    
    /**
     * Name of the cookie representing the LID template.
     */
    public static final String LID_TEMPLATE_COOKIE_NAME = "org-netmesh-lid-template";
}
