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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.httpd;

import org.infogrid.httpd.util.NameValueList;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
  * This class represents an HTTP Response. This is an abstract class; more
  * concrete classes represent different types of Responses.
  *
  * FIXME: does not do secure Cookies right now.
  */
public abstract class HttpResponse
{
    private static final Log log = Log.getLogInstance( HttpResponse.class ); // our own, private logger

    /**
      * Private constructor, for subclasses only.
      *
      * @param req the HttpRequest to which this is the HttpResponse
      * @param returnCode the HTTP return code and status message returned as part of the HttpResponse
      */
    protected HttpResponse(
            HttpRequest req,
            String      returnCode )
    {
        theRequest    = req;
        theReturnCode = returnCode;
    }

    /**
     * Obtain the HttpRequest that led to this HttpResponse.
     *
     * @return the HttpRequest that led to this HttpResponse
     */
    public HttpRequest getRequest()
    {
        return theRequest;
    }

    /**
     * Obtain the return code of this HttpResponse.
     *
     * @return return code of this HttpResponse
     */
    public String getReturnCode()
    {
        return theReturnCode;
    }

    /**
     * Set the return code of this HttpResponse.
     *
     * @param newValue the new return code of this HttpResponse
     */
    public void setReturnCode(
            String newValue )
    {
        theReturnCode = newValue;
    }

   /**
     * Add a cookie to be sent back in the response.
     *
     * @param name the name of the cookie
     * @param value the value of the cookie
     * @param domain the domain of the cookie
     * @param path the path of the cookie
     * @param expires the time of expiration
     */
    public void addCookieToSend(
            String name,
            String value,
            String domain,
            String path,
            Date   expires )
    {
        HttpCookie newCookie = new HttpCookie(
                name,
                value,
                domain,
                path,
                expires );

        theCookiesToSend.add( newCookie );
    }

    /**
     * Obtain the Cookies currently known to be sent with the Response.
     *
     * @return the Cookies known to be sent
     */
    public Collection getCookiesToSend()
    {
        return theCookiesToSend;
    }

    /**
     * Add an HTTP header.
     *
     * @param name the name of the header
     * @param value the value of the header
     */
    public void addHeader(
            String name,
            String value )
    {
        theHeaders.add( name, value );
    }

    /**
      * Write this HttpResponse to an OutputStream.
      *
      * @param theOutStream the OutputStream to write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    public void writeTo(
            OutputStream theOutStream )
        throws
            IOException
    {
        Writer theWriter = new OutputStreamWriter( theOutStream ); // default charset (FIXME?)

        writeResponseHeader( theWriter );

        theWriter.write( "\n" );
        theWriter.flush();

        writeContent( theOutStream );
    }

    /**
      * Write the HttpResponse header aspects of this Response to a Writer.
      *
      * @param theWriter the Writer we write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    protected void writeResponseHeader(
            Writer theWriter )
        throws
            IOException
    {
        DateFormat myRfc1123Format       = (DateFormat) theRfc1123Format.clone(); // DateFormat is not thread safe
        DateFormat myCookieExpiresFormat = (DateFormat) theCookieExpiresFormat.clone(); // DateFormat is not thread safe

        // HTTP/1.1 200 OK
        theWriter.write( theHttpVersion );
        theWriter.write( " " );
        theWriter.write( theReturnCode );
        theWriter.write( HttpResponseHeaderFields.CR );

        // Connection
        theWriter.write( HttpResponseHeaderFields.CONNECTION_TAG );
        theWriter.write( HttpResponseHeaderFields.SEPARATOR );
        theWriter.write( HttpResponseHeaderFields.CONNECTION_TAG_CLOSE );
        theWriter.write( HttpResponseHeaderFields.CR );

        // Date: Thu, 04 Dec 2003 05:36:08 GMT
        theWriter.write( HttpResponseHeaderFields.DATE_TAG );
        theWriter.write( HttpResponseHeaderFields.SEPARATOR );
        theWriter.write( myRfc1123Format.format( new Date() ));
        theWriter.write( HttpResponseHeaderFields.CR );

        // Pragma: skipped
        // Trailer: skipped
        // Transfer-Encoding: skipped
        // Upgrade: skipped
        // Via: skipped
        // Warning: skipped

        // Accept-Ranges
        theWriter.write( HttpResponseHeaderFields.ACCEPT_RANGES_TAG );
        theWriter.write( HttpResponseHeaderFields.SEPARATOR );
        theWriter.write( HttpResponseHeaderFields.ACCEPT_RANGES_TAG_BYTES );
        theWriter.write( HttpResponseHeaderFields.CR );

        // Age: skipped

        // ETag: "3df12-5b0-3b561f55;3f73dba3"
        // ETag: skipped

        // Location: skipped -- needed for Redirection (FIXME)

        // Proxy-Authenticate: skipped
        // Retry-After: skipped
        // Server
        theWriter.write( HttpResponseHeaderFields.SERVER_TAG );
        theWriter.write( HttpResponseHeaderFields.SEPARATOR );
        theWriter.write( SERVER_NAME );
        theWriter.write( HttpResponseHeaderFields.CR );

        // Vary: negotiate,accept-language,accept-charset
        // Vary: skipped
        // WWW-Authenticate: skipped -- only in ErrorResponse.

        if( ! theCookiesToSend.isEmpty() ) {
            Iterator<HttpCookie> iter = theCookiesToSend.iterator();
            while( iter.hasNext() ) {
                HttpCookie current = iter.next();

                String name   = current.getName();
                String value  = current.getValue();
                String domain = current.getDomain();
                String path   = current.getPath();
                Date expires  = current.getExpires();

                theWriter.write( HttpResponseHeaderFields.SET_COOKIE_TAG );
                theWriter.write( HttpResponseHeaderFields.SEPARATOR );
                theWriter.write( HTTP.encodeToValidUrlArgument( name ) );
                theWriter.write( HttpResponseHeaderFields.EQUALS );
                theWriter.write( HTTP.encodeToValidUrlArgument( value ) );

                if( expires != null ) {
                    theWriter.write( HttpResponseHeaderFields.SEMI_SEPARATOR );
                    theWriter.write( HttpResponseHeaderFields.BLANK_SEPARATOR );

                    theWriter.write( HttpResponseHeaderFields.COOKIE_EXPIRES );
                    theWriter.write( HttpResponseHeaderFields.EQUALS );
                    theWriter.write( myCookieExpiresFormat.format( expires ));
                }
                if( domain != null ) {
                    theWriter.write( HttpResponseHeaderFields.SEMI_SEPARATOR );
                    theWriter.write( HttpResponseHeaderFields.BLANK_SEPARATOR );

                    theWriter.write( HttpResponseHeaderFields.COOKIE_DOMAIN );
                    theWriter.write( HttpResponseHeaderFields.EQUALS );
                    theWriter.write( domain );
                }
                if( path != null ) {
                    theWriter.write( HttpResponseHeaderFields.SEMI_SEPARATOR );
                    theWriter.write( HttpResponseHeaderFields.BLANK_SEPARATOR );

                    theWriter.write( HttpResponseHeaderFields.COOKIE_PATH );
                    theWriter.write( HttpResponseHeaderFields.EQUALS );
                    theWriter.write( path );
                }
                theWriter.write( HttpResponseHeaderFields.CR );
            }
        }

        Iterator<String> keyIter = theHeaders.keyIterator();
        while( keyIter.hasNext() ) {
            String key   = keyIter.next();
            String value = theHeaders.get( key );

            theWriter.write( key );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( value );
            theWriter.write( HttpResponseHeaderFields.CR );
        }
    }

    /**
      * Write the content of this Response to an OutputStream. This must be implemented
      * by subclasses.
      *
      * @param theOutStream the OutputStream to write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    protected abstract void writeContent(
            OutputStream theOutStream )
        throws
            IOException;

    /**
     * Zero out all cookies that we have collected so far.
     */
    public void zeroCookies()
    {
        for( Iterator iter = theCookiesToSend.iterator() ; iter.hasNext() ; ) {
            HttpCookie current = (HttpCookie) iter.next();
            // current.setRemoved();
            // FIXME? Why was this commented out?
        }
    }

    /**
     * The HttpRequest that produced this HttpResponse.
     */
    private HttpRequest theRequest;

    /**
      * The return / error code of this HttpResponse.
      */
    protected String theReturnCode;

    /**
     * The location to which we redirect, if any.
     */
    protected String theLocation;

    /**
     * The set of cookies that shall be returned with this Response.
     */
    protected ArrayList<HttpCookie> theCookiesToSend = new ArrayList<HttpCookie>();

    /**
     * The set of custom headers that shall be returned with this Response.
     */
    protected NameValueList theHeaders = new NameValueList();

    /**
      * Our HTTP version String.
      */
    public static final String theHttpVersion = "HTTP/1.1";

    /**
     * The name of this HTTP Server.
     */
    public static final String SERVER_NAME = ResourceHelper.getInstance( HttpResponse.class ).getResourceStringOrDefault(
            "ServerName",
            "NetMesh" );

    /**
     * The RFC 1123 Date format for responses.
     */
    protected static final DateFormat theRfc1123Format;
    static {
        // = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.LONG, Locale.UK );

        theRfc1123Format = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz" );
        theRfc1123Format.setTimeZone( TimeZone.getTimeZone( "GMT" ));
    }

    /**
     * The Cookie expires Date format for responses.
     */
    protected static final DateFormat theCookieExpiresFormat;
    static {

        theCookieExpiresFormat = new SimpleDateFormat( "EEE, dd-MMM-yyyy HH:mm:ss zzz" );
        theCookieExpiresFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ));
    }
}
