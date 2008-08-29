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
import java.io.OutputStream;
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;

/**
 * A ResponseTemplate that returns the default sections in the StructuredResponse without
 * any changes, one after each other.
 */
public class VerbatimResponseTemplate
        extends
            AbstractStructuredResponseTemplate
{
    /**
     * Factory method.
     *
     * @param request the incoming HTTP request
     * @param structured the StructuredResponse that contains the response
     * @param requestedTemplate the requested ResponseTemplate, if any
     * @return the created JspStructuredResponseTemplate
     */
    public static VerbatimResponseTemplate create(
            SaneServletRequest request,
            String             requestedTemplate,
            StructuredResponse structured )
    {
        VerbatimResponseTemplate ret = new VerbatimResponseTemplate( request, requestedTemplate, structured );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param request the incoming HTTP request
     * @param requestedTemplate the requested ResponseTemplate, if any
     * @param structured the StructuredResponse that contains the response
     */
    protected VerbatimResponseTemplate(
            SaneServletRequest request,
            String             requestedTemplate,
            StructuredResponse structured )
    {
        super( request, requestedTemplate, structured );
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
        defaultOutputStatusCode( delegate, structured );
        defaultOutputCookies(    delegate, structured );
        defaultOutputMimeType(   delegate, structured );
        
        // stream default section(s)
            
        String errorContent = structured.getErrorContentAsPlain();
        if( errorContent != null ) {
            Writer w = delegate.getWriter();
            w.write( errorContent );
            w.flush();
        }

        String textContent = structured.getSectionContent( TextStructuredResponseSection.DEFAULT_SECTION );
        if( textContent != null ) {
            Writer w = delegate.getWriter();
            w.write( textContent );
            w.flush();
        }

        byte [] binaryContent = structured.getSectionContent( BinaryStructuredResponseSection.DEFAULT_SECTION );
        if( binaryContent != null ) {
            OutputStream o = delegate.getOutputStream();
            o.write( binaryContent );
            o.flush();
        }
    }
}
