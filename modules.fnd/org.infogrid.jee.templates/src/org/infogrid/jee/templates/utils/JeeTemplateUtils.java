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
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.BufferedServletResponse;
import org.infogrid.jee.templates.BinaryStructuredResponseSection;
import org.infogrid.jee.templates.BinaryStructuredResponseSectionTemplate;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseSection;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.TextStructuredResponseSectionTemplate;
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
        runRequestDispatcher(
                dispatcher,
                StructuredResponse.DEFAULT_TEXT_SECTION,
                StructuredResponse.DEFAULT_BINARY_SECTION,
                request,
                structured );
    }

    /**
     * Invoke the RequestDispatcher and put the results in a specified section.
     * 
     * @param dispatcher the RequestDispatcher to invoke
     * @param textSectionTemplate the section to put text results into
     * @param binarySectionTemplate the section to put binary results into
     * @param request the incoming request
     * @param structured the outgoing response
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    public static void runRequestDispatcher(
            RequestDispatcher                       dispatcher,
            TextStructuredResponseSectionTemplate   textSectionTemplate,
            BinaryStructuredResponseSectionTemplate binarySectionTemplate,
            SaneServletRequest                      request,
            StructuredResponse                      structured )
        throws
            ServletException,
            IOException
    {
        BufferedServletResponse bufferedResponse = new BufferedServletResponse( structured.getDelegate() );
        Throwable               lastException    = null;
        try {
            // This must be .forward instead of .include because the Java spec, in its wisdom,
            // requires containers to ignore the declared content (Mime) type of included content.
            // It does process is for forwarded requests. We do need the content type to drive the
            // selection of the right template.
            dispatcher.forward( request.getDelegate(), bufferedResponse );

        } catch( Throwable ex ) {
            lastException = ex;

        } finally {
            byte [] bufferedBytes  = bufferedResponse.getBufferedServletOutputStreamOutput();
            String  bufferedString = bufferedResponse.getBufferedPrintWriterOutput();

            StructuredResponseSection section;
            if( bufferedBytes != null ) {
                if( bufferedString != null ) {
                    // don't know what to do here -- defaults to "string gets processed, bytes ignore"
                    log.warn( "Have both String and byte content, don't know what to do: " + request );

                    TextStructuredResponseSection textSection = structured.obtainTextSection( textSectionTemplate );
                    textSection.setContent( bufferedString ); // do something is better than nothing
                    section = textSection;

                } else {
                    BinaryStructuredResponseSection binarySection = structured.obtainBinarySection( binarySectionTemplate );
                    binarySection.setContent( bufferedBytes );
                    section = binarySection;
                }

            } else if( bufferedString != null ) {
                TextStructuredResponseSection textSection = structured.obtainTextSection( textSectionTemplate );
                textSection.appendContent( bufferedString );
                section = textSection;

            } else {
                // empty section, but keep redirect status etc.
                TextStructuredResponseSection textSection = structured.obtainTextSection( textSectionTemplate );
                section = textSection;
            }

            if( section != null ) {
                @SuppressWarnings( "unchecked" )
                List<Throwable> problems = (List<Throwable>) request.getDelegate().getAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME );

                if( problems != null ) {
                    for( Throwable current : problems ) {
                        section.reportProblem( current );
                    }
                }
                if( lastException != null ) {
                    section.reportProblem( lastException );
                }
                request.getDelegate().removeAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME );

                if( bufferedResponse.getStatus() > 0 ) {
                    section.setHttpResponseCode( bufferedResponse.getStatus());
                }
                if( bufferedResponse.getContentType() != null ) {
                    section.setMimeType( bufferedResponse.getContentType() );
                }
                if( bufferedResponse.getLocale() != null ) {
                    section.setLocale( bufferedResponse.getLocale() );
                }
                if( bufferedResponse.getCharacterEncoding() != null ) {
                    section.setCharacterEncoding( bufferedResponse.getCharacterEncoding() );
                }
                if( bufferedResponse.getLocation() != null ) {
                    section.setLocation( bufferedResponse.getLocation() );
                }
                for( Cookie current : bufferedResponse.getCookies() ) {
                    section.addCookie( current );
                }
                // FIXME: we do not currently deal with additional headers that might have been written
                // into the bufferedResponse. Most importantly, we don't pass on ETag etc.

            } else {
                log.info( "No StructuredResponseSection was written" + request );
            }
        }
    }
}
