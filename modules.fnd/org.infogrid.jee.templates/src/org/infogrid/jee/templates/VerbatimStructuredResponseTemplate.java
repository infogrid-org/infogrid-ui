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
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.StringRepresentationDirectory;

/**
 * A ResponseTemplate that returns the default sections in the StructuredResponse without
 * any changes, one after each other.
 */
public class VerbatimStructuredResponseTemplate
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
     * @param c the Context to use
     * @return the created JspStructuredResponseTemplate
     */
    public static VerbatimStructuredResponseTemplate create(
            SaneRequest        request,
            String             requestedTemplate,
            String             userRequestedTemplate,
            StructuredResponse structured,
            Context            c )
    {
        VerbatimStructuredResponseTemplate ret = new VerbatimStructuredResponseTemplate(
                request,
                requestedTemplate,
                userRequestedTemplate,
                structured,
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
     * @param c the Context to use
     */
    protected VerbatimStructuredResponseTemplate(
            SaneRequest        request,
            String             requestedTemplate,
            String             userRequestedTemplate,
            StructuredResponse structured,
            Context            c )
    {
        super( request, requestedTemplate, userRequestedTemplate, structured, c );
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
        outputYadisHeader( delegate, structured );
        
        // stream default section(s)
        
        JeeFormatter theFormatter = getContext().findContextObjectOrThrow( JeeFormatter.class );
        
        List<Throwable> reportedProblems = structured.problems();
        String errorContent = theFormatter.formatProblems( theRequest, reportedProblems, StringRepresentationDirectory.TEXT_PLAIN_NAME, false );
        if( errorContent != null ) {
            Writer w = delegate.getWriter();
            w.write( errorContent );
            w.flush();
        }

        String textContent = structured.getDefaultTextSection().getContent();
        if( textContent != null ) {
            Writer w = delegate.getWriter();
            w.write( textContent );
            w.flush();
        }

        byte [] binaryContent = structured.getDefaultBinarySection().getContent();
        if( binaryContent != null ) {
            OutputStream o = delegate.getOutputStream();
            o.write( binaryContent );
            o.flush();
        }
    }
    
    /**
     * Name of this template that emits plain text without change.
     */
    public static final String VERBATIM_TEXT_TEMPLATE_NAME = "verbatim";
}
