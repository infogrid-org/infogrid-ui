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

package org.infogrid.jee.templates.utils;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.BufferedServletResponse;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.util.logging.Log;

/**
 * Utility methods for template processing.
 */
public abstract class JeeTemplateUtils
{
    private static final Log log = Log.getLogInstance( JeeTemplateUtils.class ); // our own, private logger

    /**
     * Inaccessible constructor to keep this an abstract class.
     */
    private JeeTemplateUtils()
    {
        // nothing
    }

    /**
     * Invoke the RequestDispatcher and put the results in the default section of the StructuredResponse.
     * 
     * @param dispatcher the RequestDispatcher to invoke
     * @param request the incoming request
     * @param structured the outgoing response
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    public static void runRequestDispatcher(
            RequestDispatcher  dispatcher,
            SaneServletRequest request,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        BufferedServletResponse bufferedResponse = new BufferedServletResponse( structured.getDelegate() );

        dispatcher.include( request.getDelegate(), bufferedResponse );

        byte [] bufferedBytes  = bufferedResponse.getBufferedServletOutputStreamOutput();
        String  bufferedString = bufferedResponse.getBufferedPrintWriterOutput();

        if( bufferedBytes != null ) {
            if( bufferedString != null ) {
                // don't know what to do here -- defaults to "string gets processed, bytes ignore"
                log.warn( "Have both String and byte content, don't know what to do: " + request );
                structured.setDefaultSectionContent( bufferedString ); // do something is better than nothing

            } else {
                structured.setDefaultSectionContent( bufferedBytes );
            }

        } else if( bufferedString != null ) {
            structured.setDefaultSectionContent( bufferedString );
        } else {
            // do nothing
        }
        structured.setMimeType( bufferedResponse.getContentType() );
    }
                
}
