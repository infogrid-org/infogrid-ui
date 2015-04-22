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

package org.infogrid.jee.templates;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * A ResponseTemplate that emits nothing, regardless of what content has been set.
 */
public class NoContentStructuredResponseTemplate
        extends
            AbstractStructuredResponseTemplate
{
    /**
     * Factory method.
     *
     * @param request the incoming HTTP request
     * @param structured the StructuredResponse that contains the response
     * @param requestedTemplate the requested ResponseTemplate that will be used, if any
     * @param userRequestedTemplate the ResponseTemplate requested by the user, if any
     * @param defaultMime the default MIME type for the response
     * @param c the Context to use
     * @return the created JspStructuredResponseTemplate
     */
    public static NoContentStructuredResponseTemplate create(
            SaneRequest        request,
            String             requestedTemplate,
            String             userRequestedTemplate,
            StructuredResponse structured,
            String             defaultMime,
            Context            c )
    {
        NoContentStructuredResponseTemplate ret = new NoContentStructuredResponseTemplate(
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
     * @param request the incoming HTTP request
     * @param requestedTemplate the requested ResponseTemplate that will be used, if any
     * @param userRequestedTemplate the ResponseTemplate requested by the user, if any
     * @param structured the StructuredResponse that contains the response
     * @param defaultMime the default MIME type for the response
     * @param c the Context to use
     */
    protected NoContentStructuredResponseTemplate(
            SaneRequest        request,
            String             requestedTemplate,
            String             userRequestedTemplate,
            StructuredResponse structured,
            String             defaultMime,
            Context            c )
    {
        super( request, requestedTemplate, userRequestedTemplate, structured, defaultMime, c );
    }

    /**
     * Stream a StructuredResponse to an HttpResponse employing this template.
     * 
     * @param delegate the underlying HttpServletResponse
     * @param structured the StructuredResponse that contains the response
     * @throws IOException thrown if an I/O error occurred
     */
    public void doOutput(
            HttpServletResponse delegate,
            StructuredResponse  structured )
        throws
            IOException
    {
        outputStatusCode(  delegate, structured );
        outputLocale(      delegate, structured );
        outputCookies(     delegate, structured );
        outputMimeType(    delegate, structured );
        outputLocation(    delegate, structured );
        outputAdditionalHeaders( delegate, structured );
        
        // stream nothing
    }

    /**
     * Name of the template that represents a response that has no content.
     */
    public static final String NO_CONTENT_TEMPLATE_NAME = "no-content";
}
