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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.templates;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.Cookie;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * An actual section in a StructuredResponse.
 */
public abstract class StructuredResponseSection
        implements
            HasHeaderPreferences
{
    private static final Log log = Log.getLogInstance( StructuredResponseSection.class ); // our own, private logger

    /**
     * Constructor for subclasses only.
     * 
     * @param sectionTemplate the template that defines this section
     */
    protected StructuredResponseSection(
            StructuredResponseSectionTemplate sectionTemplate )
    {
        theSectionTemplate = sectionTemplate;
    }

    /**
     * Obtain the StructuredResponseSectionTemplate that defines this section.
     * 
     * @return the StructuredResponseSectionTemplate
     */
    public StructuredResponseSectionTemplate getSectionTemplate()
    {
        return theSectionTemplate;
    }

    /**
     * Determine whether this section is empty.
     * 
     * @return true if this section is empty
     */
    public abstract boolean isEmpty();

    /**
     * Stream this StructuredResponseSection to an OutputStream.
     * 
     * @param s the OutputStream to write to
     * @return true if something was output, false otherwise
     * @throws IOException thrown if an I/O error occurred
     */
    public abstract boolean doOutput(
            OutputStream s )
        throws
            IOException;

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
     * Report a problem that should be shown to the user.
     *
     * @param t the Throwable indicating the problem
     */
    public void reportProblem(
            Throwable t )
    {
        if( theCurrentProblems.size() <= theSectionTemplate.getMaxProblems() ) {
            // make sure we aren't growing this indefinitely
            theCurrentProblems.add( t );

        } else {
            log.error( "Too many problems. Ignored ", t );
        }
    }

    /**
     * Convenience method to report several problems that should be shown to the user.
     *
     * @param ts [] the Throwables indicating the problems
     */
    public void reportProblems(
            Throwable [] ts )
    {
        for( int i=0 ; i<ts.length ; ++i ) {
            if( theCurrentProblems.size() <= theSectionTemplate.getMaxProblems() ) {
                // make sure we aren't growing this indefinitely
                theCurrentProblems.add( ts[i] );
            } else {
                log.error( "Too many problems. Ignored " + (ts.length-i) + " problems starting with ", ts[i] );
                break;
            }
        }
    }

    /**
     * Obtain the problems reported so far.
     * 
     * @return problems reported so far, in sequence
     */
    public Iterator<Throwable> problems()
    {
        return theCurrentProblems.iterator();
    }

    /**
     * Report an informational message that should be shown to the user.
     *
     * @param t the THrowable indicating the message
     */
    public void reportInfoMessage(
            Throwable t )
    {
        if( log.isDebugEnabled() ) {
            log.debug( "Reporting info message: ", t );
        }
        if( theCurrentInfoMessages.size() <= theSectionTemplate.getMaxInfoMessages() ) {
            // make sure we aren't growing this indefinitely
            theCurrentInfoMessages.add( t );

        } else {
            log.error( "Too many info messages. Ignored ", t ); // late initialization
        }
    }

    /**
     * Convenience method to report several informational messages that should be shown to the user.
     *
     * @param ts [] the Throwables indicating the informational messages
     */
    public void reportInfoMessages(
            Throwable [] ts )
    {
        for( int i=0 ; i<ts.length ; ++i ) {
            if( theCurrentInfoMessages.size() <= theSectionTemplate.getMaxInfoMessages() ) {
                // make sure we aren't growing this indefinitely
                theCurrentInfoMessages.add( ts[i] );
            } else {
                log.error( "Too many informational messages. Ignored " + (ts.length-i) + " problems starting with ", ts[i] );
                break;
            }
        }
    }

    /**
     * Determine whether informational messages have been reported.
     *
     * @return true if at least one informational message has been reported
     */
    public boolean haveInfoMessagesBeenReported()
    {
        return !theCurrentInfoMessages.isEmpty();
    }

    /**
     * Obtain the informational messages reported so far.
     *
     * @return informational messages reported so far, in sequence
     */
    public Iterator<Throwable> infoMessages()
    {
        return theCurrentInfoMessages.iterator();
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
    public void addCookie(
            Cookie newCookie )
    {
        Cookie found = theOutgoingCookies.put( newCookie.getName(), newCookie );
        
        if( found != null ) {
            log.error( "Setting the same cookie again: " + newCookie + " vs. " + found );
        }
    }

    /**
     * Convenience method to delete a cookie with this response.
     * 
     * @param name name of the Cookie to delete
     */
    public void addDeleteCookie(
            String name )
    {
        Cookie newCookie = new Cookie( name, "**deleted**" );
        newCookie.setMaxAge( 0 );
        Cookie found = theOutgoingCookies.put( name, newCookie );

        if( found != null ) {
            log.error( "Setting the same cookie again: " + name + " vs. " + found );
        }
    }

    /**
     * Obtain the getCookies.
     * 
     * @return the getCookies
     */
    public Collection<Cookie> getCookies()
    {
        return theOutgoingCookies.values();
    }

    /**
     * Set the location header.
     * 
     * @param newValue the new value
     */
    public void setLocation(
            String newValue )
    {
        theLocation = newValue;
    }
    
    /**
     * Obtain the location header.
     * 
     * @return the currently set location header
     */
    public String getLocation()
    {
        return theLocation;
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
     * Set the locale.
     * 
     * @param newValue the new value
     */
    public void setLocale(
            Locale newValue )
    {
        theLocale = newValue;
    }
    
    /**
     * Obtain the locale.
     * 
     * @return the locale
     */
    public Locale getLocale()
    {
        return theLocale;
    }

    /**
     * Set the character encoding.
     * 
     * @param newValue the new value
     */
    public void setCharacterEncoding(
            String newValue )
    {
        theCharacterEncoding = newValue;
    }
    
    /**
     * Obtain the character encoding.
     * 
     * @return the character encoding
     */
    public String getCharacterEncoding()
    {
        return theCharacterEncoding;
    }

    /**
     * Add an additional header.
     *
     * @param name name of the header to add
     * @param value value of the header to add
     */
    public void addHeader(
            String name,
            String value )
    {
        addHeader( name, new String[] { value } );
    }

    /**
     * Add an additional header.
     *
     * @param name name of the header to add
     * @param value value of the header to add
     */
    public void addHeader(
            String    name,
            String [] value )
    {
        String [] already = theOutgoingHeaders.put( name, value );
        if( already != null && already.length > 0 ) {
            theOutgoingHeaders.put( name, ArrayHelper.append( already, value, String.class ));
        }
    }

    /**
     * Obtain the additionla headers.
     *
     * @return the headers, as Map
     */
    public Map<String,String[]> getHeaders()
    {
        return theOutgoingHeaders;
    }

    /**
     * The StructuredResponseSectionTemplate that defines this section.
     */
    protected StructuredResponseSectionTemplate theSectionTemplate;

    /**
     * The mime type of this section. null is default.
     */
    protected String theMimeType = null;

    /**
     * The location header, if any.
     */
    protected String theLocation = null;

    /**
     * The getCookies to be sent. This is represented as a HashMap in order to easily be
     * able to detect that the same cookie has been set again.
     */
    protected HashMap<String,Cookie> theOutgoingCookies = new HashMap<String,Cookie>();

    /**
     * The outgoing HTTP response code. -1 stands for "not set".
     */
    protected int theHttpResponseCode = -1;

    /**
     * The outgoing Locale.
     */
    protected Locale theLocale = null;

    /**
     * The outgoing character encoding.
     */
    protected String theCharacterEncoding = null;

    /**
     * The outgoing headers.
     */
    protected HashMap<String,String[]> theOutgoingHeaders = new HashMap<String,String[]>();

    /**
     * The current problems, in sequence of occurrence.
     */
    protected ArrayList<Throwable> theCurrentProblems = new ArrayList<Throwable>();

    /**
     * The current informational messages, in sequence of occurrence.
     */
    protected ArrayList<Throwable> theCurrentInfoMessages = new ArrayList<Throwable>();
}
