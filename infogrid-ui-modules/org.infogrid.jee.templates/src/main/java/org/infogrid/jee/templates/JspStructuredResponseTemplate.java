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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.templates;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Dumper;

/**
 * A ResponseTemplate that processes a JSP page with placeholders, in which the named
 * sections of the StructuredResponse are inserted.
 */
public class JspStructuredResponseTemplate
        extends
            AbstractStructuredResponseTemplate
{
    /**
     * Factory method.
     *
     * @param dispatcher identifies the JSP file
     * @param request the incoming HTTP request
     * @param requestedTemplate the requested ResponseTemplate that will be used, if any
     * @param userRequestedTemplate the ResponseTemplate requested by the user, if any
     * @param structured the StructuredResponse that contains the response
     * @param defaultMime the default MIME type for the response
     * @param c the Context to use
     * @return the created JspStructuredResponseTemplate
     */
    public static JspStructuredResponseTemplate create(
            RequestDispatcher  dispatcher,
            SaneRequest        request,
            String             requestedTemplate,
            String             userRequestedTemplate,
            StructuredResponse structured,
            String             defaultMime,
            Context            c )
    {
        JspStructuredResponseTemplate ret = new JspStructuredResponseTemplate(
                dispatcher,
                request,
                requestedTemplate,
                userRequestedTemplate,
                structured,
                defaultMime,
                c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param dispatcher identifies the JSP file
     * @param request the incoming HTTP request
     * @param requestedTemplate the requested ResponseTemplate that will be used, if any
     * @param userRequestedTemplate the ResponseTemplate requested by the user, if any
     * @param structured the StructuredResponse that contains the response
     * @param defaultMime the default MIME type for the response
     * @param c the Context to use
     */
    protected JspStructuredResponseTemplate(
            RequestDispatcher  dispatcher,
            SaneRequest        request,
            String             requestedTemplate,
            String             userRequestedTemplate,
            StructuredResponse structured,
            String             defaultMime,
            Context            c )
    {
        super( request, requestedTemplate, userRequestedTemplate, structured, defaultMime, c );

        theRequestDispatcher = dispatcher;
    }

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
            IOException
    {
        outputStatusCode(  delegate, structured );
        outputLocale(      delegate, structured );
        outputCookies(     delegate, structured );
        outputMimeType(    delegate, structured );
        outputLocation(    delegate, structured );
        outputAdditionalHeaders( delegate, structured );

        Object oldStructured = theRequest.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        try {
            theRequest.setAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME, structured );

            theRequestDispatcher.forward(
                    ((SaneServletRequest) theRequest).getDelegate(),
                    delegate );

        } finally {
            theRequest.setAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME, oldStructured );
        }
    }
    
    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    @Override
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theRequest",
                    "theStructured",
                    "theRequestedTemplate",
                    "theUserRequestedTemplate",
                    "theRequestDispatcher"
                },
                new Object[] {
                    theRequest,
                    theStructured,
                    theRequestedTemplate,
                    theUserRequestedTemplate,
                    theRequestDispatcher
                });
    }

    /**
     * The dispatcher.
     */
    protected RequestDispatcher theRequestDispatcher;
}
