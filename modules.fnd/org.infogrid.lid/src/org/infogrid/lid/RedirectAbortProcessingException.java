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

package org.infogrid.lid;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;

/**
 * An AbortProcessingException that carries special content to be emitted instead
 * of the default response.
 */
public class RedirectAbortProcessingException
        extends
            AbortProcessingException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the LidService that threw this exception
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param responseCode the HTTP status code to use
     * @param location that destination URL
     */
    public RedirectAbortProcessingException(
            LidService         source,
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse,
            int                responseCode,
            String             location )
    {
        super( source, lidRequest, lidResponse, responseCode, null, null );
        
        theLocation = location;
    }
    
    /**
     * Write the HTTP header of this Response.
     *
     * @param response the HttpServletResponse to write to
     * @throws IOException thrown if an I/O error occurred
     * @throws ServletException another problem occurred
     */
    @Override
    protected void writeResponseHeader(
            HttpServletResponse response )
        throws
            IOException,
            ServletException
    {
        super.writeResponseHeader( response );

        response.setHeader( "Location", theLocation );
    }

    /**
     * The location header, if any.
     */
    protected String theLocation;    
}
