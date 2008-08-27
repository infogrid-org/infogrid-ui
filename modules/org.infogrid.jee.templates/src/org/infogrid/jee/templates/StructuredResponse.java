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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.util.LocalizedObject;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.XmlUtils;
import org.infogrid.util.logging.Log;

/**
 * Encapsulates the content of an HTTP response in structured form.
 */
public class StructuredResponse
{
    private static final Log log = Log.getLogInstance( StructuredResponse.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param delegate the underlying HttpServletResponse
     * @param htmlObjectFormatter knows how to format an object in HTML
     * @param plainObjectFormatter knows how to format an object in plain text
     * @param servletContext the ServletContext in which the StructuredResponse is created
     * @return the created StructuredResponse
     */
    public static StructuredResponse create(
            HttpServletResponse      delegate,
            LocalizedObjectFormatter htmlObjectFormatter,
            LocalizedObjectFormatter plainObjectFormatter,
            ServletContext           servletContext )
    {
        StructuredResponse ret = new StructuredResponse( delegate, htmlObjectFormatter, plainObjectFormatter, servletContext );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param delegate the underlying HttpServletResponse
     * @param htmlObjectFormatter knows how to format an object in HTML
     * @param plainObjectFormatter knows how to format an object in plain text
     * @param servletContext the ServletContext in which the StructuredResponse is created
     */
    protected StructuredResponse(
            HttpServletResponse      delegate,
            LocalizedObjectFormatter htmlObjectFormatter,
            LocalizedObjectFormatter plainObjectFormatter,
            ServletContext           servletContext )
    {
        theDelegate             = delegate;
        theHtmlObjectFormatter  = htmlObjectFormatter;
        thePlainObjectFormatter = plainObjectFormatter;
        theServletContext       = servletContext;
    }
    
    /**
     * Obtain the underlying HttpServletResponse.
     * 
     * @return the delegate
     */
    public HttpServletResponse getDelegate()
    {
        return theDelegate;
    }

    /**
     * Set the MIME type of the StructuredResponse.
     * 
     * @param newValue the new value
     */
    public void setMimeType(
            String newValue )
    {
        theMimeType = newValue;
    }
    
    /**
     * Obtain the MIME type of the StructuredResponse.
     * 
     * @return the MIME type
     */
    public String getMimeType()
    {
        return theMimeType;
    }

    /**
     * Add a cookie to the response.
     * 
     * @param newCookie the new cookie
     */
    public void addNewCookie(
            Cookie newCookie )
    {
        Cookie found = theOutgoingCookies.put( newCookie.getName(), newCookie );
        
        if( found != null ) {
            log.error( "Setting the same cookie again: " + newCookie + " vs. " + found );
        }
    }

    /**
     * Obtain the cookies.
     * 
     * @return the cookies
     */
    public Collection<Cookie> cookies()
    {
        return theOutgoingCookies.values();
    }

    /**
     * Add a header to the response.
     * 
     * @param key the header key
     * @param value the header value
     * @return the old value of this header, if any
     */
    public String addAdditionalHeader(
            String key,
            String value )
    {
        String ret = theAdditionalHeaders.put( key, value );
        return ret;
    }
    
    /**
     * Obtain the additional headers.
     * 
     * @return the headers
     */
    public Map<String,String> additionalHeaders()
    {
        return theAdditionalHeaders;
    }

    /**
     * Set the HTTP response code.
     * 
     * @param code the HTTP response code
     */
    public void setHttpResponseCode(
            int code )
    {
        theHttpResponseCode = code;
    }

    /**
     * Obtain the HTTP response code.
     * 
     * @return the HTTP response code
     */
    public int getHttpResponseCode()
    {
        return theHttpResponseCode;
    }
    
    /**
     * Set the content of the default section.
     * 
     * @param newValue the new content of the section
     */
    public void setDefaultSectionContent(
            String newValue )
    {
        setSectionContent( TextStructuredResponseSection.DEFAULT_SECTION, newValue );
    }

    /**
     * Set the content of the default section.
     * 
     * @param newValue the new content of the section
     */
    public void setDefaultSectionContent(
            byte [] newValue )
    {
        setSectionContent( BinaryStructuredResponseSection.DEFAULT_SECTION, newValue );
    }

    /**
     * Set the content of a section.
     * 
     * @param section the section
     * @param newValue the new content of the section
     */
    public void setSectionContent(
            TextStructuredResponseSection section,
            String                        newValue )
    {
        theTextSections.put( section, newValue );
    }

    /**
     * Append to the content of a section.
     * 
     * @param section the section
     * @param toAppend the new content to append to the section
     */
    public void appendToSectionContent(
            TextStructuredResponseSection section,
            String                        toAppend )
    {
        String current = theTextSections.get( section );
        String newValue;
        if( current != null ) {
            newValue = current + toAppend;
        } else {
            newValue = toAppend;
        }
        theTextSections.put( section, newValue );
    }

    /**
     * Obtain the content of a section.
     * 
     * @param section the section
     * @return the content of the section, or null
     */
    public String getSectionContent(
            TextStructuredResponseSection section )
    {
        String ret = theTextSections.get( section );
        return ret;
    }

    /**
     * Set the content of a section.
     * 
     * @param section the section
     * @param newValue the new content of the section
     */
    public void setSectionContent(
            BinaryStructuredResponseSection section,
            byte []                         newValue )
    {
        theBinarySections.put( section, newValue );
    }

    /**
     * Obtain the content of a section.
     * 
     * @param section the section
     * @return the content of the section, or null
     */
    public byte [] getSectionContent(
            BinaryStructuredResponseSection section )
    {
        byte [] ret = theBinarySections.get( section );
        return ret;
    }

    /**
     * Find a section by name.
     * 
     * @param name the name of the section
     * @return the section, if any
     */
    public StructuredResponseSection findSectionByName(
            String name )
    {
        for( StructuredResponseSection current : theTextSections.keySet() ) {
            if( name.equals( current.getSectionName() )) {
                return current;
            }
        }
        for( StructuredResponseSection current : theBinarySections.keySet() ) {
            if( name.equals( current.getSectionName() )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Report a problem that should be shown to the user.
     *
     * @param t the Throwable indicating the problem
     */
    public void reportProblem(
            Throwable t )
    {
        if( theCurrentProblems.size() < MAX_PROBLEMS ) {
            // make sure we aren't growing this indefinitely
            theCurrentProblems.add( t );

        } else {
            log.error( "Too many problems. Ignored ", t );
        }
    }

    /**
     * Obtain the problems reported so far.
     * 
     * @return problems reported so far, in sequence
     */
    public List<Throwable> problems()
    {
        return theCurrentProblems;
    }

    /**
     * Obtain the error content as plain text.
     * 
     * @return the error content
     */
    public String getErrorContentAsPlain()
    {
        StringBuilder            buf       = new StringBuilder();
        LocalizedObjectFormatter formatter = obtainPlainObjectFormatter( );

        for( Throwable t : theCurrentProblems ) {
            String msg;
            if( t instanceof LocalizedObject ) {
                msg = ((LocalizedObject) t).getLocalizedMessage( formatter );
            } else {
                msg = t.getMessage();
            }
            if( msg == null ) {
                msg = t.getClass().getName();
            }
            buf.append( msg );
            
            for( StackTraceElement trace : t.getStackTrace() ) {
                buf.append( "    " );
                buf.append( trace );
                buf.append( "\n" );
            }            

            // now causes.
            for( Throwable cause = findCause( t ) ; cause != null ; cause = findCause( cause )) {
                buf.append( "Caused by: " );
                String msg2;
                if( cause instanceof LocalizedObject ) {
                    msg2 = ((LocalizedObject) cause).getLocalizedMessage( formatter );
                } else {
                    msg2 = cause.getMessage();
                }
                if( msg2 == null ) {
                    msg2 = cause.getClass().getName();
                }
                buf.append( msg2 );

                for( StackTraceElement trace : cause.getStackTrace() ) {
                    buf.append( "    " );
                    buf.append( trace );
                    buf.append( "\n" );
                }
            }
        }
        return buf.toString();
    }

    /**
     * Obtain the error content as HTML text.
     * 
     * @return the error content
     */
    public String getErrorContentAsHtml()
    {
        StringBuilder            buf       = new StringBuilder();
        LocalizedObjectFormatter formatter = obtainHtmlObjectFormatter( );

        for( Throwable t : theCurrentProblems ) {
            buf.append( "<div class=\"error\">\n" );
            String msg;
            if( t instanceof LocalizedObject ) {
                msg = ((LocalizedObject) t).getLocalizedMessage( formatter );
            } else {
                msg = t.getMessage();
            }
            if( msg == null ) {
                msg = t.getClass().getName();
            }

            // make safe for HTML
            buf.append( "<h1>" );
            String sep = "";
            for( String line : msg.split( "\n" )) {
                buf.append( sep );
                String escapedLine = XmlUtils.escape( line );
                buf.append( escapedLine );
                sep = "<br />\n";
            }
            buf.append( "</h1>\n" );
            
            buf.append( "<div class=\"stacktrace\">\n" );
            
            for( StackTraceElement trace : t.getStackTrace() ) {
                buf.append( "<div class=\"stacktracelement\">" );
                buf.append( trace );
                buf.append( "</div>\n" );
            }            

            // now causes.
            for( Throwable cause = findCause( t ) ; cause != null ; cause = findCause( cause )) {
                buf.append( "<div class=\"cause\">\n" );

                String msg2;
                if( cause instanceof LocalizedObject ) {
                    msg2 = ((LocalizedObject) cause).getLocalizedMessage( formatter );
                } else {
                    msg2 = cause.getMessage();
                }
                if( msg2 == null ) {
                    msg2 = cause.getClass().getName();
                }
                buf.append( "<h2>" ).append( msg2 ).append( "</h2>\n" );

                for( StackTraceElement trace : cause.getStackTrace() ) {
                    buf.append( "<div class=\"stacktracelement\">" );
                    buf.append( trace );
                    buf.append( "</div>\n" );
                }            
                buf.append( "</div>\n" );
            }
            buf.append( "</div>\n" );
        }
        return buf.toString();
    }

    /**
     * Helper method to find the cause of an Exception.
     * Unfortunately JspExceptions do getRootCause() instead of getCause().
     *
     * @param t the incoming Throwable whose cause we need to determine
     * @return the found cause, or null
     */
    protected Throwable findCause(
            Throwable t )
    {
        Throwable ret;
        if( t instanceof ServletException ) {
            ret = ((ServletException)t).getRootCause();
        } else {
            ret = t.getCause();
        }
        return ret;
    }

    /**
     * Quote HTML angle brackets so we can insert them into a &lt;pre&gt; element.
     *
     * @param in the unquoted String
     * @return the quoted String
     */
    protected String quoteAngleBrackets(
            String in )
    {
        String ret = in.replaceAll( "<", "&lt;" );
        ret = ret.replaceAll( ">", "&gt;" );
        return ret;
    }

    /**
     * Obtain a LocalizedObjectFormatter for Html output. This can be overridden by subclasses.
     * 
     * @return the LocalizedObjectFormatter to use with this application
     */
    protected LocalizedObjectFormatter obtainHtmlObjectFormatter()
    {
        return theHtmlObjectFormatter;
    }

    /**
     * Obtain a LocalizedObjectFormatter for plain text output. This can be overridden by subclasses.
     * 
     * @return the LocalizedObjectFormatter to use with this application
     */
    protected LocalizedObjectFormatter obtainPlainObjectFormatter()
    {
        return thePlainObjectFormatter;
    }

    /**
     * Determine whether problems have been reported.
     * 
     * @return true if at least one problem has been reported
     */
    public boolean haveProblemsBeenReported()
    {
        return !theCurrentProblems.isEmpty();
    }

    /**
     * Obtain the ServletContext whithin this response is being assembled.
     * 
     * @return the ServletContext
     */
    public ServletContext getServletContext()
    {
        return theServletContext;
    }

    /**
     * Determine whether this StructuredResponse is empty.
     * 
     * @return true if it is empty
     */
    public boolean isEmpty()
    {
        if( !theOutgoingCookies.isEmpty() ) {
            return false;
        }
        if( !theAdditionalHeaders.isEmpty() ) {
            return false;
        }
        if( !theTextSections.isEmpty() ) {
            return false;
        }
        if( !theBinarySections.isEmpty() ) {
            return false;
        }
        if( !theCurrentProblems.isEmpty() ) {
            return false;
        }
        return true;
    }

    /**
     * The underlying servlet response.
     */
    protected HttpServletResponse theDelegate;

    /**
     * The mime type of the response. HTML is default
     */
    protected String theMimeType = "text/html";

    /**
     * The cookies to be sent. This is represented as a HashMap in order to easily be
     * able to detect that the same cookie has been set again.
     */
    protected HashMap<String,Cookie> theOutgoingCookies = new HashMap<String,Cookie>();

    /**
     * The additional headers to be sent, as name-value pairs.
     */
    protected HashMap<String,String> theAdditionalHeaders = new HashMap<String,String>();

    /**
     * The outgoing HTTP response code. Default is 200/OK.
     */
    protected int theHttpResponseCode = HttpServletResponse.SC_OK;

    /**
     * The sections of the response that are represented as text.
     */
    protected HashMap<TextStructuredResponseSection,String> theTextSections
            = new HashMap<TextStructuredResponseSection,String>();

    /**
     * The sections of the response that are represented as binary.
     */
    protected HashMap<BinaryStructuredResponseSection,byte []> theBinarySections
            = new HashMap<BinaryStructuredResponseSection,byte []>();

    /**
     * The current problems, in sequence of occurrence.
     */
    protected ArrayList<Throwable> theCurrentProblems = new ArrayList<Throwable>();

    /**
     * The ServletContext within which this response is assembled.
     */
    protected ServletContext theServletContext;

    /**
     * Knows how to format an object in HTML.
     */
    protected LocalizedObjectFormatter theHtmlObjectFormatter;

    /**
     * Knows how to format an object in plain text.
     */
    protected LocalizedObjectFormatter thePlainObjectFormatter;

    /**
     * Name of the request attribute that contains the StructuredResponse. Make sure
     * this constant does not contain any characters that might make some processor
     * interpret it as being an expression.
     */
    public static final String STRUCTURED_RESPONSE_ATTRIBUTE_NAME = StructuredResponse.class.getName().replaceAll( "\\.", "_");
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( StructuredResponse.class );

    /**
     * The maximum number of problems to store.
     */
    public static final int MAX_PROBLEMS = theResourceHelper.getResourceIntegerOrDefault( "MaxProblems", 20 );
}
