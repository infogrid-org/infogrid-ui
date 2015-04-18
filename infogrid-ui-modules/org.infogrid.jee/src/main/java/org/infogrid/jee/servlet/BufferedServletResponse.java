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

package org.infogrid.jee.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * <p>A ServletResponse that buffers all content. It does not write any content
 *    to the delegate stream; the caller must do that.</p>
 * <p>Both a byte[] and a String buffer may be created, in order to avoid converting
 *    to and from String and byte [] all the time.</p>
 * <p>This used to inherit from HttpServletResponseWrapper, but that turned out to be
 *    more trouble than it was worth.</p>
 */
public class BufferedServletResponse
        implements
            HttpServletResponse
{
    private static final Log log = Log.getLogInstance( BufferedServletResponse.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param delegate the underlying HttpServletResponse
     */
    public BufferedServletResponse(
            HttpServletResponse delegate )
    {
        theDelegate = delegate;
    }

    // use comment from supertype
    public String getCharacterEncoding()
    {
        return theCharacterEncoding;
    }

    // use comment from supertype
    public String getContentType()
    {
        return theContentType;
    }

    // use comment from supertype
    public ServletOutputStream getOutputStream()
            throws
                IOException
    {
        if( theServletOutputStream == null ) {
            if( theOutputStream == null ) {
                theOutputStream = new ByteArrayOutputStream( 2048 );
            }
            theServletOutputStream = new MyServletOutputStream( theOutputStream );
        }
        return theServletOutputStream;        
    }

    // use comment from supertype
    public PrintWriter getWriter()
            throws
                IOException
    {
        if( thePrintWriter == null ) {
            if( theWriter == null ) {
                theWriter = new StringWriter( 2048 );
            }
            thePrintWriter = new PrintWriter( theWriter );
        }
        return thePrintWriter;
    }
    
    // use comment from supertype
    public void setCharacterEncoding(
            String charset )
    {
        theCharacterEncoding = charset;
    }

    // use comment from supertype
    public void setContentLength(
            int len )
    {
        theContentLength = len;
    }
    
    // use comment from supertype
    public void setContentLengthLong(
            long len )
    {
        theContentLength = len;
    }
    
    // use comment from supertype
    public void setContentType(
            String type )
    {
        theContentType = type;
    }

    // use comment from supertype
    public void setBufferSize(
            int size )
    {
        // ignored (FIXME?)
    }

    // use comment from supertype
    public int getBufferSize()
    {
        return Integer.MAX_VALUE; // FIXME?
    }

    // use comment from supertype
    public void flushBuffer()
            throws
                IOException
    {
        if( thePrintWriter != null ) {
            thePrintWriter.flush();
        }
        if( theOutputStream != null ) {
            theOutputStream.flush();
        }
    }
    
    // use comment from supertype
    public void resetBuffer()
    {
        resetCache();
    }

    // use comment from supertype
    public boolean isCommitted()
    {
        return false; // this is always false as this is buffer
    }

    // use comment from supertype
    public void reset()
    {
        resetCache();
        theHeaders.clear();
        theStatusCode = -1;
    }

    // use comment from supertype
    public void setLocale(
            Locale loc )
    {
        theLocale = loc;
    }

    // use comment from supertype
    public Locale getLocale()
    {
        return theLocale;
    }

    // use comment from supertype
    public void addCookie(
            Cookie cookie )
    {
        theCookies.add( cookie );
    }

    // use comment from supertype
    public String getHeader(
            String name )
    {
        Object found = theHeaders.values();
        String ret;

        if( found == null ) {
            ret = null;

        } else if( found instanceof String ) {
            ret = (String) found;
        } else {
            ArrayList<String> realFound = (ArrayList<String>) found;
            if( realFound.isEmpty() ) {
                ret = null;
            } else {
                ret = realFound.get( 0 );
            }
        }
        return ret;
    }

    // use comment from supertype
    public Collection<String> getHeaders(
            String name )
    {
        Object             found = theHeaders.values();
        Collection<String> ret;

        if( found == null ) {
            ret = new ArrayList<>( 0 );

        } else if( found instanceof String ) {
            ret = new ArrayList<>();
            ret.add( (String) found );
        } else {
            ret = (ArrayList<String>) found;
        }
        return ret;
    }

    // use comment from supertype
    public Collection<String> getHeaderNames()
    {
        return theHeaders.keySet();
    }

    // use comment from supertype
    public boolean containsHeader(
            String name )
    {
        Object found = theHeaders.get( name );
        return found != null;
    }

    // use comment from supertype
    public String encodeURL(
            String url )
    {
        String ret = theDelegate.encodeURL( url );
        return ret;
    }

    // use comment from supertype
    public String encodeRedirectURL(
            String url )
    {
        String ret = theDelegate.encodeRedirectURL( url );
        return ret;
    }

    /**
     * @deprecated	As of version 2.1, use encodeURL(String url) instead
     *
     * @param	url	the url to be encoded.
     * @return		the encoded URL if encoding is needed; the unchanged URL otherwise.
     */
    public String encodeUrl(
            String url )
    {
        return encodeURL( url );
    }
    
    /**
     * @deprecated	As of version 2.1, use encodeRedirectURL(String url) instead
     *
     * @param	url	the url to be encoded.
     * @return		the encoded URL if encoding is needed; the unchanged URL otherwise.
     */
    public String encodeRedirectUrl(
            String url )
    {
        return encodeRedirectURL( url );
    }

    // use comment from supertype
    public void sendError(
            int    sc,
            String msg )
        throws
            IOException
    {
        theStatusCode    = sc;
        theStatusMessage = msg;
    }

    // use comment from supertype
    public void sendError(
            int sc )
        throws
            IOException
    {
        sendError( sc, null );
    }

    // use comment from supertype
    public void sendRedirect(
            String location )
        throws
            IOException
    {
        resetBuffer();

        theStatusCode = 302; // Found code
        theHeaders.put( LOCATION_HEADER, location );
    }
    
    // use comment from supertype
    public void setDateHeader(
            String name,
            long  date )
    {
        setHeader( name, theFormat.format( date ) );
    }
    
    // use comment from supertype
    public void addDateHeader(
            String name,
            long   date )
    {
        addHeader( name, theFormat.format( date ) );
    }
    
    // use comment from supertype
    public void setHeader(
            String name,
            String value )
    {
        Object already = theHeaders.put( name, value ); // return value for debugging only
    }

    // use comment from supertype
    @SuppressWarnings( "unchecked" )
    public void addHeader(
            String name,
            String value )
    {
        Object already = theHeaders.get( name );
        if( already == null ) {
            theHeaders.put( name, value );

        } else if( already instanceof String ) {
            ArrayList<String> newValue = new ArrayList<String>();
            newValue.add( (String) already );
            newValue.add( value );
            theHeaders.put( name, newValue );

        } else if( already instanceof ArrayList ) {
            ((ArrayList)already).add( value );
        } else {
            log.error( "Unexpected type: " + already );
        }
    }

    // use comment from supertype
    public void setIntHeader(
            String name,
            int    value )
    {
        setHeader( name, String.valueOf( value ));
    }

    // use comment from supertype
    public void addIntHeader(
            String name,
            int    value )
    {
        addHeader( name, String.valueOf( value ));
    }
    
    /**
     * Sets the status code for this response.
     *
     * @param	sc	the status code
     * @see #sendError
     */
    public void setStatus(
            int sc )
    {
        setStatus( sc, null );
    }
  
    /**
     * @deprecated As of version 2.1, due to ambiguous meaning of the 
     * message parameter. To set a status code 
     * use <code>setStatus(int)</code>, to send an error with a description
     * use <code>sendError(int, String)</code>.
     *
     * Sets the status code and message for this response.
     * 
     * @param	sc	the status code
     * @param	sm	the status message
     */
    public void setStatus(
            int    sc,
            String sm )
    {
        theStatusCode    = sc;
        theStatusMessage = sm;
    }

    /**
     * Obtain the status code for this response.
     * 
     * @return the status code
     */
    public int getStatus()
    {
        return theStatusCode;
    }

    /**
     * Obtain the entire buffered output that was written via the PrintWriter.
     *
     * @return the buffered output, or null
     * @throws IOException an I/O problem occurred
     */
    public String getBufferedPrintWriterOutput()
        throws
            IOException
    {
        if( thePrintWriter != null ) {
            thePrintWriter.flush();
        }
        if( theWriter != null ) {
            String ret = theWriter.getBuffer().toString();
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Obtain the entire buffered output that was written via the ServletOutputStream.
     *
     * @return the buffered output, or null
     * @throws IOException an I/O problem occurred
     */
    public byte [] getBufferedServletOutputStreamOutput()
        throws
            IOException
    {
        if( theServletOutputStream != null ) {
            theServletOutputStream.flush();
        }
        if( theOutputStream != null ) {
            byte [] ret = theOutputStream.toByteArray();
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Determine whether the buffer is empty or anything has been written into it.
     * 
     * @return true if this buffer is empty
     */
    public boolean isEmpty()
    {
        if( theOutputStream != null && theOutputStream.size() > 0 ) {
            return false;
        }
        if( theWriter != null && theWriter.getBuffer().length() > 0 ) {
            return false;
        }
        return true;
    }

    /**
     * Reset the locally held cache.
     */
    protected void resetCache()
    {
        if( thePrintWriter != null ) {
            thePrintWriter = null;
        }
        if( theWriter != null ) {
            theWriter = null;
        }
        if( theServletOutputStream != null ) {
            theServletOutputStream = null;
        }
        if( theOutputStream != null ) {
            theOutputStream = null;
        }
    }

    /**
     * Determine whether or not this context type is text.
     *
     * @return true if this content type is text
     */
    public boolean isText()
    {
        String type = theContentType.toLowerCase();
        if( type.startsWith( "text/" )) {
            return true;
        }
        if( type.startsWith( "application/xhtml" )) {
            return true;
        }
        if( type.startsWith( "application/xml" )) {
            return true;
        }
        return false;
    }
    
    /**
     * Obtain the location header, if there is any.
     * 
     * @return the location header
     */
    public String getLocation()
    {
        Object found = theHeaders.get( LOCATION_HEADER );
        if( found == null ) {
            return null;
        } else if( found instanceof String ) {
            return (String) found;
        } else {
            log.error( "More than one location header: " + found );
            return null;
        }
    }

    /**
     * Obtain the set of Cookies.
     * 
     * @return the set of Cookies
     */
    public Set<Cookie> getCookies()
    {
        return theCookies;
    }

    /**
     * Obtain the keys for the HTTP headers.
     * 
     * @return the keys for the headers
     */
    public Set<String> getHttpHeaderKeySet()
    {
        return theHeaders.keySet();
    }

    /**
     * Obtain the zero, one or more values for a given HTTP header key.
     * 
     * @param key the key
     * @return the values
     */
    public String [] getHttpHeaderValues(
            String key )
    {
        Object found = theHeaders.get( key );
        if( found == null ) {
            return null;
        } else if( found instanceof String ) {
            return new String[] { (String) found };
        } else {
            @SuppressWarnings( "unchecked" )
            String [] ret = ArrayHelper.copyIntoNewArray( (ArrayList)found, String.class );
            return ret;
        }
    }

    /**
     * Copy the buffer into this HttpServletResponse.
     * 
     * @param destination the HttpServletResponse to copy to
     * @throws IOException thown if an input/output error occurred
     */
    @SuppressWarnings( "unchecked" )
    public void copyTo(
            HttpServletResponse destination )
        throws
            IOException
    {
        if( theStatusCode > 0 ) {
            destination.setStatus( theStatusCode ); // FIXME? Status code
        }
        if( theContentType != null ) {
            destination.setContentType( theContentType );
        }
        if( theLocale != null ) {
            destination.setLocale( theLocale );
        }
        // if( theCharacterEncoding != null ) {
        //     destination.setCharacterEncoding( theCharacterEncoding );
        // The version of JEE I have does not seem to have this method
        // }
        if( theContentLength > 0 ) {
            destination.setContentLengthLong( theContentLength );
        }
        for( String key : theHeaders.keySet() ) {
            Object value = theHeaders.get( key );
            if( value == null ) {
                continue;
            } else if( value instanceof String ) {
                destination.addHeader( key, (String) value );
            } else if( value instanceof ArrayList ) {
                for( String v : (ArrayList<String>) value ) {
                    destination.addHeader( key, v );
                }
            }
        }
        for( Cookie c : theCookies ) {
            destination.addCookie( c );
        }
        
        String  stringContent = getBufferedPrintWriterOutput();
        byte [] byteContent   = getBufferedServletOutputStreamOutput();
        
        if( stringContent != null ) {
            destination.getOutputStream().print( stringContent );
        } else if( byteContent != null ) {
            destination.getOutputStream().write( byteContent );
        } else {
            // do nothing
        }
    }

    /**
     * The delegate HttpServletResponse to which this is ultimately written.
     */
    protected HttpServletResponse theDelegate;
    
    /**
     * Content type of this response.
     */
    protected String theContentType;
    
    /**
     * The locale.
     */
    protected Locale theLocale;

    /**
     * The character encoding.
     */
    protected String theCharacterEncoding;

    /**
     * The HTTP content length, if not -1.
     */
    protected long theContentLength;

    /**
     * The headers to send. The value is either of type String, or of type ArrayList&lt;String&gt;.
     */
    protected Map<String,Object> theHeaders = new HashMap<String,Object>();

    /**
     * The cookies to send.
     */
    protected Set<Cookie> theCookies = new HashSet<Cookie>();

    /**
     * The HTTP status code.
     */
    protected int theStatusCode;
    
    /**
     * The HTTP status message.
     */
    protected String theStatusMessage;

    /**
     * Byte-buffer to write into.
     */
    protected ByteArrayOutputStream theOutputStream;
    
    /**
     * ServletOutputStream on top of theOutputStream.
     */
    protected ServletOutputStream theServletOutputStream;
    
    /**
     * String-buffer to write into.
     */
    protected StringWriter theWriter;
    
    /**
     * PrintWriter on top of the theWriter.
     */
    protected PrintWriter thePrintWriter;
    
    /**
     * The key of the location header.
     */
    public static final String LOCATION_HEADER = "Location";

    /**
     * The date format for date headers.
     */
    protected static final SimpleDateFormat theFormat;
    static {
        theFormat = new SimpleDateFormat( "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US );
        theFormat.setTimeZone( TimeZone.getTimeZone("GMT") );
    }


    /**
     * Simple implementation of ServletOutputStream.
     */
    static class MyServletOutputStream
            extends
                ServletOutputStream
    {
        /**
         * Constructor.
         *
         * @param delegate the OutputStream to write to.
         */
        public MyServletOutputStream(
                OutputStream delegate )
        {
            theDelegate = delegate;
        }
        
        /**
         * Write method.
         *
         * @param i the integer to write
         * @throws IOException
         */
        public void write(
                int i )
            throws
                IOException
        {
            theDelegate.write( i );
        }

        /**
         * Can data be written without blocking.
         * 
         * @return true or false
         */
        public boolean isReady()
        {
            return true;
        }

        /**
         * Set a listener
         * 
         * @param writeListener 
         */
        public void setWriteListener(
                WriteListener writeListener )
        {
            throw new UnsupportedOperationException(); // FIXME?
        }

        /**
         * The underlying stream.
         */
        protected OutputStream theDelegate;
    }
}
