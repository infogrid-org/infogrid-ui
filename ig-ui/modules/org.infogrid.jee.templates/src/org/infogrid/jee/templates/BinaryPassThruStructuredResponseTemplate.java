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
import javax.servlet.http.HttpServletResponse;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * A ResponseTemplate that returns binary content verbatim.
 */
public class BinaryPassThruStructuredResponseTemplate
        extends
            AbstractStructuredResponseTemplate
{
    /**
     * Factory method.
     *
     * @param request the incoming HTTP request
     * @param structured the StructuredResponse that contains the response
     * @param defaultMime the default MIME type for the response
     * @param c the Context to use
     * @return the created JspStructuredResponseTemplate
     */
    public static BinaryPassThruStructuredResponseTemplate create(
            SaneRequest        request,
            StructuredResponse structured,
            String             defaultMime,
            Context            c )
    {
        BinaryPassThruStructuredResponseTemplate ret = new BinaryPassThruStructuredResponseTemplate(
                request,
                structured,
                defaultMime,
                c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param request the incoming HTTP request
     * @param structured the StructuredResponse that contains the response
     * @param defaultMime the default MIME type for the response
     * @param c the Context to use
     */
    protected BinaryPassThruStructuredResponseTemplate(
            SaneRequest        request,
            StructuredResponse structured,
            String             defaultMime,
            Context            c )
    {
        super( request, null, null, structured, defaultMime, c );
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
        outputStatusCode(        delegate, structured );
        outputLocale(            delegate, structured );
        outputCookies(           delegate, structured );
        outputMimeType(          delegate, structured );
        outputLocation(          delegate, structured );
        outputAdditionalHeaders( delegate, structured );
        
        byte [] binaryContent = structured.getDefaultBinarySection().getContent();
        if( binaryContent != null ) {
            OutputStream o = delegate.getOutputStream();
            o.write( binaryContent );
            o.flush();
        }
    }
}
