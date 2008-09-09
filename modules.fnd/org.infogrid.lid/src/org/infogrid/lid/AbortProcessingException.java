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
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.util.FactoryException;

/**
 * Thrown by processing objects to interrupt the current control flow in LID processing.
 * Subclasses indicate the content of the response to be returned.
 */
public class AbortProcessingException
        extends
            Exception
{
    /**
     * Constructor.
     *
     * @param source the LidService that threw this exception
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param message the internally logged error message
     * @param cause the cause of the Exception, if any
     */
    public AbortProcessingException(
            LidService         source,
//            SaneServletRequest lidRequest,
//            StructuredResponse lidResponse,
            String             message,
            Throwable          cause )
    {
        super( message, cause );

        theSource       = source;
        theSourceName   = source.getLidProfileName();
    }

//    /**
//     * Write out the correct content, given this Exception.
//     * 
//     * @param response the HttpServletResponse to write to
//     * @throws IOException thrown if an I/O error occurred
//     * @throws ServletException another problem occurred
//     */
//    public void writeResponse(
//            HttpServletResponse response )
//        throws
//            IOException,
//            ServletException
//    {
//        writeResponseHeader( response );
//        writeResponseContent( response );
//    }
//
//    /**
//     * Write the HTTP header of this Response.
//     *
//     * @param response the HttpServletResponse to write to
//     * @throws IOException thrown if an I/O error occurred
//     * @throws ServletException another problem occurred
//     */
//    protected void writeResponseHeader(
//            HttpServletResponse response )
//        throws
//            IOException,
//            ServletException
//    {
//        response.setContentType( theResponse.getMimeType() );
//
//        for( Cookie current : theResponse.cookies() ) {
//            response.addCookie( current );
//        }
//        for( String key : theResponse.additionalHeaders().keySet() ) {
//            String value = theResponse.additionalHeaders().get( key );
//            response.addHeader( key, value );
//        }
//        int httpStatus = theResponse.getHttpResponseCode();
//        response.setStatus( httpStatus );
//    }
//
//    /**
//     * Write the HTTP payload of this Response.
//     *
//     * @param response the HttpServletResponse to write to
//     * @throws IOException thrown if an I/O error occurred
//     * @throws ServletException another problem occurred
//     */
//    protected void writeResponseContent(
//            HttpServletResponse response )
//        throws
//            IOException,
//            ServletException
//    {
//        try {
//            StructuredResponseTemplateFactory templateFactory = theSource.getContext().findContextObjectOrThrow( StructuredResponseTemplateFactory.class );
//            StructuredResponseTemplate        template        = templateFactory.obtainFor( theRequest, theResponse );
//
//            template.doOutput( response, theResponse );
//
//        } catch( FactoryException ex ) {
//            throw new ServletException( ex );
//        }
//    }
//
    /**
     * The LidService that threw this exception.
     */
    protected transient LidService theSource;
            
    /**
     * The LID profile name that threw this exception. This is held here as
     * it is serializable, unlike LidService itself.
     */
    protected String theSourceName;
//
//    /**
//     * The incoming request.
//     */
//    protected transient SaneServletRequest theRequest;
//
//    /**
//     * The outgoing response.
//     */
//    protected transient StructuredResponse theResponse;
}
