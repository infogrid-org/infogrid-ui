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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.util.logging.Log;

/**
 * Replaces text in a response. This filter only filters content types that start with "text/"
 * (such as "text/plain" or "text/xml"). It replaces all occurrences of the text Strings (interpreted
 * as regular expressions) that are given as parameters to the filter in the <code>web.xml</code> file, with the values
 * of these parameters. Subclasses may change this behavior by overriding the <code>replaceAll</code>
 * method.
 */
public class TextSubstitutionFilter
        implements
            Filter
{
    private static Log log = null; // initialize as late as possible

    /**
     * Constructor.
     */
    public TextSubstitutionFilter()
    {
        // noop
    }
    
    /**
     * Execute the filter.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        if( response instanceof HttpServletResponse ) {
            HttpServletResponse     realResponse      = (HttpServletResponse) response;
            BufferedServletResponse delegatedResponse = new BufferedServletResponse( realResponse );
            
            try {
                chain.doFilter( request, delegatedResponse );

            } catch( Throwable t ) {
                getLog().error( t );

            } finally {

                realResponse.setContentType( delegatedResponse.getContentType() );
                realResponse.setLocale(      delegatedResponse.getLocale() );
                realResponse.setStatus(      delegatedResponse.getStatus() );
                
                for( Cookie current : delegatedResponse.getCookies() ) {
                    realResponse.addCookie( current );
                }
                for( String key : delegatedResponse.getHttpHeaderKeySet() ) {
                    if( BufferedServletResponse.LOCATION_HEADER.equals( key )) {
                        continue;
                    }
                    String [] values = delegatedResponse.getHttpHeaderValues( key );
                    if( values != null ) {
                        for( String value : values ) {
                            realResponse.addHeader( key, value );
                        }
                    }
                }
                
                byte [] bufferedBytes  = delegatedResponse.getBufferedServletOutputStreamOutput();
                String  bufferedString = delegatedResponse.getBufferedPrintWriterOutput();
                
                if( bufferedBytes != null ) {
                    if( bufferedString != null ) {
                        // don't know what to do here -- defaults to "string gets processed, bytes ignore"
                        getLog().warn( "Have both String and byte content, don't know what to do: " + request );
                    }
                } else if( bufferedString == null ) {
                    // both null, i.e. there is no content (e.g. because of not-modified-since.). Do nothing.
                    return;
                }

                if( delegatedResponse.isText() ) {
                    if( bufferedString == null ) {
                        bufferedString = new String( bufferedBytes );
                    }
                    bufferedString = replaceAll( (HttpServletRequest) request, (HttpServletResponse) response, bufferedString );
                }

                if( bufferedString != null ) {
                    bufferedBytes = bufferedString.getBytes( "UTF-8" );
                }
                
                if( bufferedBytes != null ) {
                    response.setContentLength( bufferedBytes.length );
                    response.getOutputStream().write( bufferedBytes );
                    response.getOutputStream().flush();
                }
            }
        
        } else {
            chain.doFilter( request, response ); // do nothing in the non-HTTP case
        }
    }
    
    /**
     * Initialize the Filter.
     *
     * @param filterConfig the Filter configuration object
     * @throws ServletException thrown if misconfigured
     */
    public void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theFilterConfig = filterConfig;

        Enumeration e = theFilterConfig.getInitParameterNames();
        while( e.hasMoreElements() ) {
            String key   = (String) e.nextElement();
            String value = theFilterConfig.getInitParameter( key );
            
            theReplacementMap.put( key, value );
        }
    }
    
    /**
     * Destroy method for this Filter.
     */
    public void destroy()
    {
        // noop
    }
    
    /**
     * Replace all occurrences of the Filter parameters. Subclasses may override this.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @param unfiltered the unfiltered text
     * @return the filtered text
     */
    protected String replaceAll(
            HttpServletRequest  request,
            HttpServletResponse response,
            String              unfiltered )
    {
        String ret = unfiltered;

        Map<String,String> replacements = getReplacementMap( request, response );
        
        for( String key : replacements.keySet() ) {
            String value = replacements.get( key );
            
            // cannot use String.replaceAll because it interprets value 
            
            int index = 0;
            while( ( index = ret.indexOf( key, index )) > 0 ) {
                ret = ret.substring( 0, index ) + value + ret.substring( index + key.length() );
                index += value.length();
            }
        }
        return ret;
    }

    /**
     * Obtain the Map of name-value pairs to substitute.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @return the Map
     */
    protected Map<String,String> getReplacementMap(
            HttpServletRequest  request,
            HttpServletResponse response )
    {
        return theReplacementMap;
    }

    /**
     * Initialize and get the log.
     *
     * @return the log
     */
    private static Log getLog()
    {
        if( log == null ) {
            log = Log.getLogInstance( TextSubstitutionFilter.class ); // our own, private logger
        }
        return log;
    }

    /**
     * The filter configuration object this Filter is associated with.
     */
    protected FilterConfig theFilterConfig = null;

    /**
     * The Map containing the replacements.
     */
    protected HashMap<String,String> theReplacementMap = new HashMap<String,String>();    
}
