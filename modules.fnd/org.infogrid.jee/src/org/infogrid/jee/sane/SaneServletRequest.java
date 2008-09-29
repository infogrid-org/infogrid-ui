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

package org.infogrid.jee.sane;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.util.ArrayCursorIterator;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CompositeIterator;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneCookie;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * A ServletRequest following the <code>SaneRequest</code> API.
 */
public class SaneServletRequest
        extends
            SaneRequest
{
    private static final Log log = Log.getLogInstance( SaneServletRequest.class ); // our own, private logger
    
    /**
      * Smart factory method.
      *
      * @param sRequest the HttpServletRequest from which to create a SaneRequest.
      * @return the created SaneServletRequest
      */
    public static SaneServletRequest create(
             HttpServletRequest sRequest )
    {
        SaneServletRequest ret = (SaneServletRequest) sRequest.getAttribute( SANE_SERVLET_REQUEST_ATTRIBUTE_NAME );
        if( ret == null ) {
            ret = SaneServletRequest.internalCreate( sRequest );
            sRequest.setAttribute( SANE_SERVLET_REQUEST_ATTRIBUTE_NAME, ret );
        }

        return ret;
    }

    /**
      * Internal factory method.
      * 
      * @param sRequest the HttpServletRequest from which to create a SaneRequest.
      * @return the created SaneServletRequest
      */
    protected static SaneServletRequest internalCreate(
             HttpServletRequest sRequest )
    {
        HashMap<String,String[]> arguments     = new HashMap<String,String[]>();
        HashMap<String,String[]> postArguments = new HashMap<String,String[]>();
        
        String postData = null;

        String serverProtocol = sRequest.getScheme();
        String queryString    = sRequest.getQueryString();
        String method         = sRequest.getMethod();
        String server         = sRequest.getServerName();
        int    port           = sRequest.getServerPort();

        String httpHostOnly = sRequest.getServerName();
        String httpHost     = sRequest.getServerName();
        String protocol     = serverProtocol.equalsIgnoreCase( "https" ) ? "https" : "http";
        String contextPath  = sRequest.getContextPath();
        
        String relativeBaseUri;
        String relativeFullUri;
        
        SaneCookie [] cookies;
        
        if( "http".equals( protocol )) {
            if( port != 80 ) {
                httpHost += ":" + port;
            }
        } else {
            if( port != 443 ) {
                httpHost += ":" + port;
            }
        }
        
        relativeBaseUri = sRequest.getRequestURI();
        relativeFullUri = relativeBaseUri;

        if( queryString != null && queryString.length() != 0 ) {
            relativeFullUri += "?" + queryString;
        }

        Cookie [] servletCookies = sRequest.getCookies();
        if( servletCookies != null ) {
            cookies = new SaneCookie[ servletCookies.length ];
            for( int i=0 ; i<servletCookies.length ; ++i ) {
                cookies[i] = new CookieAdapter( servletCookies[i] );
            }
        } else {
            cookies = new SaneCookie[0];
        }
        // URL parameters override POSTed fields: more intuitive for the user
        if( "POST".equalsIgnoreCase( method ) ) { // we do our own parsing
            try {
                int length = sRequest.getContentLength();
                byte [] buf = StreamUtils.slurp( sRequest.getInputStream(), length );

                postData = new String( buf, "utf-8" );
                addToArguments( postData, true, arguments, postArguments );

            } catch( IOException ex ) {
                log.error( ex );
            }
        }

        addToArguments( queryString, false, arguments, postArguments );
     
        String clientIp = sRequest.getRemoteAddr();

        SaneServletRequest ret = new SaneServletRequest(
                sRequest,
                method,
                server,
                port,
                httpHostOnly,
                httpHost,
                protocol,
                relativeBaseUri,
                relativeFullUri,
                contextPath,
                postData,
                arguments,
                postArguments,
                queryString,
                cookies,
                clientIp );
        
        return ret;
    }

    /**
     * Constructor.
     *
     * @param sRequest the HttpServletRequest from which the SaneServletRequest is created
     * @param method the HTTP method
     * @param server the HTTP server
     * @param port the HTTP server port
     * @param httpHostOnly the HTTP host, without port
     * @param httpHost the HTTP host with port
     * @param protocol http or https
     * @param relativeBaseUri the relative base URI
     * @param relativeFullUri the relative full URI
     * @param contextPath the JEE context path
     * @param postData the data HTTP POST'd, if any
     * @param arguments the arguments given, if any
     * @param postArguments the arguments given via HTTP POST, if any
     * @param queryString the string past the ? in the URL
     * @param cookies the sent cookies
     * @param clientIp the client's IP address
     */
    protected SaneServletRequest(
            HttpServletRequest   sRequest,
            String               method,
            String               server,
            int                  port,
            String               httpHostOnly,
            String               httpHost,
            String               protocol,
            String               relativeBaseUri,
            String               relativeFullUri,
            String               contextPath,
            String               postData,
            Map<String,String[]> arguments,
            Map<String,String[]> postArguments,
            String               queryString,
            SaneCookie []        cookies,
            String               clientIp )
    {
        theDelegate        = sRequest;
        theMethod          = method;
        theServer          = server;
        thePort            = port;
        theHttpHostOnly    = httpHostOnly;
        theHttpHost        = httpHost;
        theProtocol        = protocol;
        theRelativeBaseUri = relativeBaseUri;
        theRelativeFullUri = relativeFullUri;
        theContextPath     = contextPath;
        thePostData        = postData;
        theArguments       = arguments;
        thePostArguments   = postArguments;
        theQueryString     = queryString;
        theCookies         = cookies;
        theClientIp        = clientIp;
    }

    /**
     * Helper to parse URL and POST data, and put them in the right places.
     *
     * @param data the data to add
     * @param isPost true of this argument was provided in an HTTP POST
     * @param arguments the URL arguments in the process of being assembled
     * @param postArguments the URL POST arguments in the process of being assembled
     */
    protected static void addToArguments(
            String               data,
            boolean              isPost,
            Map<String,String[]> arguments,
            Map<String,String[]> postArguments )
    {
        if( data == null || data.length() == 0 ) {
            return;
        }
        if( data.charAt( 0 ) == '?' ) {
            data = data.substring( 1 );
        }

        if( isPost && postArguments == null ) {
            postArguments = new HashMap<String,String[]>();
        }

        char sep = '?';
        StringTokenizer pairTokenizer = new StringTokenizer( data, "&" );
        while( pairTokenizer.hasMoreTokens() ) {
            String    pair     = pairTokenizer.nextToken();
            String [] keyValue = pair.split( "=", 2 );

            String key   = HTTP.decodeUrlArgument( keyValue[0] );
            String value = HTTP.decodeUrlArgument( keyValue.length == 2 ? keyValue[1] : "" ); // reasonable default?

            if( !"lid-submit".equalsIgnoreCase( key )) {
                // We need to remove the submit button's contribution
                
                String [] haveAlready = arguments.get( key );
                String [] newValue;
                if( haveAlready == null ) {
                    newValue = new String[] { value };
                } else {
                    newValue = ArrayHelper.append( haveAlready, value, String.class );
                }
                arguments.put( key, newValue );

                if( isPost ) {
                    haveAlready = postArguments.get( key );

                    if( haveAlready == null ) {
                        newValue = new String[] { value };
                    } else {
                        newValue = ArrayHelper.append( haveAlready, value, String.class );
                    }
                    postArguments.put( key, newValue );
                }
            }
        }
    }
    
    /**
     * Determine the HTTP method (such as GET).
     *
     * @return the HTTP method
     */
    public String getMethod()
    {
        return theDelegate.getMethod();
    }

    /**
     * Determine the requested, relative base URI.
     * In a request for URL <code>http://example.com:123/foo/bar?abc=def</code>,
     * that would be <code>/foo/bar</code>.
     *
     * @return the requested base URI
     */
    public String getRelativeBaseUri()
    {
        return theRelativeBaseUri;
    }

    /**
     * Determine the requested, relative full URI.
     * In a request to URL <code>http://example.com:123/foo/bar?abc=def</code>
     * that would be <code>/foo/bar?abc=def</code>.
     *
     * @return the requested relative full URI
     */
    public String getRelativeFullUri()
    {
        return theRelativeFullUri;
    }

    /**
     * Get the name of the server.
     *
     * @return the name of the server
     */
    public String getServer()
    {
        return theServer;
    }

    /**
     * Obtain the host name.
     *
     * @return the host name
     */
    public String getHttpHost()
    {
        return theHttpHost;
    }

    /**
     * Get the value of the HTTP 1.1 host name field, but without the port.
     *
     * @return the HTTP 1.1 host name field, but without the port
     */
    public String getHttpHostOnly()
    {
        return theHttpHostOnly;
    }

    /**
     * Get the port at which this request arrived.
     *
     * @return the port at which this request arrived
     */
    public int getPort()
    {
        return thePort;
    }

    /**
     * Get the protocol, i.e. <code>http</code> or <code>https</code>.
     *
     * @return <code>http</code> or <code>https</code>
     */
    public String getProtocol()
    {
        return theProtocol;
    }

    /**
     * Obtain all values of a multi-valued argument.
     *
     * @param argName name of the argument
     * @return the values, or <code>null</code>
     */
    public String [] getMultivaluedArgument(
            String argName )
    {
        String [] ret = theArguments.get( argName );
        return ret;
    }

    /**
     * Obtain all arguments of this request.
     *
     * @return a Map of name to value mappings for all arguments
     */
    public Map<String,String[]> getArguments()
    {
        return theArguments;
    }

    /**
     * Obtain all values of a multi-valued POST'd argument.
     *
     * @param argName name of the argument
     * @return the values, or <code>null</code>
     */
    public String [] getMultivaluedPostArgument(
            String argName )
    {
        if( thePostArguments == null ) {
            return null;
        }
        String [] ret = thePostArguments.get( argName );
        return ret;
    }

    /**
     * Obtain all POST'd arguments of this request.
     *
     * @return a Map of name to value mappings for all POST'd arguments
     */
    public Map<String,String[]> getPostArguments()
    {
        return thePostArguments;
    }    
    
    /**
     * Obtain the JEE app's context path, but in absolute terms.
     * 
     * @return the absolute context path
     */
    public String getAbsoluteContextUri()
    {
        if( theAbsoluteContextUri == null ) {
            StringBuilder buf = new StringBuilder();
            buf.append( getRootUri() );
            buf.append( theContextPath );
            theAbsoluteContextUri = buf.toString();
        }
        return theAbsoluteContextUri;
    }

    /**
     * Obtain the cookies that were sent as part of this request.
     *
     * @return the cookies that were sent as part of this request.
     */
    public synchronized SaneCookie[] getCookies()
    {
        if( theCookies == null ) {
            Cookie [] delegateCookies = theDelegate.getCookies();

            theCookies = new SaneCookie[ delegateCookies.length ];
            for( int i=0 ; i<delegateCookies.length ; ++i ) {
                theCookies[i] = new CookieAdapter( delegateCookies[i] );
            }
        } 
        return theCookies;
    }

    /**
     * Obtain the content of the request, e.g. HTTP POST data.
     *
     * @return the content of the request, or <code>null</code>
     */
    public String getPostData()
    {
        return thePostData;
    }

    /**
     * Obtain the query string, if any.
     * 
     * @return the query string
     */
    public String getQueryString()
    {
        return theQueryString;
    }

    /**
     * Obtain the client IP address.
     * 
     * @return the client IP address
     */
    public String getClientIp()
    {
        return theClientIp;
    }

    /**
     * Obtain an Iterator over the user's Locale preferences, in order of preference.
     * This Iterator takes into account a Locale cookie that might be set by the application,
     * followed by the value of the Accept-Language header in the HTTP request and
     * the default locale of the VM
     *
     * @return Iterator
     */
    @Override
    @SuppressWarnings(value={"unchecked"})
    public Iterator<Locale> acceptLanguageIterator()
    {
        SaneCookie  c        = getCookie( ACCEPT_LANGUAGE_COOKIE_NAME );
        Enumeration fromHttp = theDelegate.getLocales();
        if( c != null ) {
            String s = c.getValue();
            String [] parts = s.split( "-" );
            
            Locale cookieLocale;
            switch( parts.length ) {
                case 1:
                    cookieLocale = new Locale( parts[0] );
                    break;
                case 2:
                    cookieLocale = new Locale( parts[0], parts[2] );
                    break;
                default:
                    cookieLocale = new Locale( parts[0], parts[1], parts[2] );
                    break;
            }
            
            return new CompositeIterator<Locale>( new Enumeration[] {
                OneElementIterator.<Locale>create( cookieLocale ),
                fromHttp,
                OneElementIterator.<Locale>create( Locale.getDefault() ) } );

        } else {
            return new CompositeIterator<Locale>( new Enumeration[] {
                fromHttp,
                OneElementIterator.<Locale>create( Locale.getDefault() ) } );
        }
    }

    /**
     * Obtain an Iterator over the requested MIME types, if any. Return the higher-priority
     * MIME types first.
     *
     * @return Iterator over the requested MIME types, if any.
     */
    public Iterator<String> requestedMimeTypesIterator()
    {
        if( theRequestedMimeTypes == null ) {
            // first split by comma, then by semicolon
            String header = theDelegate.getHeader( ACCEPT_HEADER );
            if( header != null ) {
                theRequestedMimeTypes = header.split( "," );
                
                Arrays.sort( theRequestedMimeTypes, new Comparator<String>() {
                    public int compare(
                            String o1,
                            String o2 )
                    {
                        final String qString = ";q=";

                        float priority1;
                        float priority2;
                        
                        int semi1 = o1.indexOf( qString );
                        if( semi1 >= 0 ) {
                            priority1 = Float.parseFloat( o1.substring( semi1 + qString.length() ));
                        } else {
                            priority1 = 1.f;
                        }
                        int semi2 = o2.indexOf( qString );
                        if( semi2 >= 0 ) {
                            priority2 = Float.parseFloat( o2.substring( semi2 + qString.length() ));
                        } else {
                            priority2 = 1.f;
                        }

                        int ret;
                        if( semi1 > semi2 ) {
                            ret = 1;
                        } else if( semi1 == semi2 ) {
                            ret = 0;
                        } else {
                            ret = -1;
                        }
                        return ret;
                    }
                });
                
            } else {
                theRequestedMimeTypes = new String[0];
            }
        }
        return ArrayCursorIterator.<String>create( theRequestedMimeTypes );
    }

    /**
     * Obtain the delegate request.
     *
     * @return the delegate
     */
    public HttpServletRequest getDelegate()
    {
        return theDelegate;
    }

    /**
     * Helper method to convert a class name into a suitable attribute name.
     * 
     * @param clazz the Class
     * @return the attribute name
     */
    public static String classToAttributeName(
            Class<?> clazz )
    {
        String ret = clazz.getName();
        ret = ret.replaceAll( "\\.", "_" );
        return ret;
    }

    /**
     * Helper method to convert a class name and a local fragment into a suitable attribute name.
     * 
     * @param clazz the Class
     * @param fragment the fragment, or local id
     * @return the attribute name
     */
    public static String classToAttributeName(
            Class<?> clazz,
            String   fragment )
    {
        String ret = clazz.getName();
        ret = ret.replaceAll( "\\.", "_" );
        ret = ret + "__" + fragment;
        return ret;
    }

    /**
     * The underlying HttpServletRequest.
     */
    protected HttpServletRequest theDelegate;
    
    /**
     * The http method, such as GET.
     */
    protected String theMethod;

    /**
     * The http server.
     */
    protected String theServer;

    /**
     * The http host, potentially with port.
     */
    protected String theHttpHost;

    /**
     * The http host, without the port.
     */
    protected String theHttpHostOnly;

    /**
     * The port.
     */
    protected int thePort;

    /**
     * The relative base URI of the Request.
     */
    protected String theRelativeBaseUri;

    /**
     * The relative full URI of the Request.
     */
    protected String theRelativeFullUri;

    /**
     * The cookies on this request. Allocated when needed.
     */
    protected SaneCookie[] theCookies;
    
    /**
     * The http or https protocol.
     */
    protected String theProtocol;

    /**
     * The query String, if any.
     */
    protected String theQueryString;

    /**
     * The full absolute context URI.
     */
    protected String theAbsoluteContextUri;

    /**
     * The relative Jee context path.
     */
    protected String theContextPath;
    
    /**
     * The data that was posted, if any.
     */
    protected String thePostData;

    /**
     * The arguments to the Request, mapping from argument name to argument value.
     */
    protected Map<String,String[]> theArguments;

    /**
     * The arguments to the Request that were POST'd, if any.
     */
    protected Map<String,String[]> thePostArguments;

    /**
     * Name of the request attribute that contains an instance of SaneServletRequest.
     */
    public static final String SANE_SERVLET_REQUEST_ATTRIBUTE_NAME = classToAttributeName( SaneServletRequest.class );

    /**
     * The requested MIME types, in sequence of prioritization. Allocated as needed.
     */
    protected String [] theRequestedMimeTypes;

    /**
     * The IP address of the client.
     */
    protected String theClientIp;

    /**
     * Bridges the SaneCookie interface into the servlet cookies.
     */
    static class CookieAdapter
        implements
            SaneCookie
    {
        /**
         * Constructor.
         *
         * @param delegate the Servlet Cookie we delegate to
         */
        public CookieAdapter(
                javax.servlet.http.Cookie delegate )
        {
            theDelegate = delegate;
        }

        /**
         * Get the Cookie name.
         *
         * @return the Cookie name
         */
        public String getName()
        {
            if( theName == null ) {
                theName = HTTP.decodeUrlArgument( theDelegate.getName() );
            }
            return theName;
        }

        /**
         * Get the Cookie value.
         *
         * @return the Cookie value
         */
        public String getValue()
        {
            if( theValue == null ) {
                theValue = HTTP.decodeUrlArgument( theDelegate.getValue() );
            }
            return theValue;
        }

        /**
         * Get the Cookie domain.
         *
         * @return the Cookie domain
         */
        public String getDomain()
        {
            return theDelegate.getDomain();
        }

        /**
         * Get the Cookie path.
         *
         * @return the Cookie path
         */
        public String getPath()
        {
            return theDelegate.getPath();
        }

        /**
         * Get the Cookie expiration date.
         *
         * @return the Cookie expiration date
         */
        public Date getExpires()
        {
            return new Date( System.currentTimeMillis() + 1000L*theDelegate.getMaxAge() );
        }

        /**
         * No op, here.
         */
        public void setRemoved()
        {
            // no op
        }
     
        /**
         * Determine whether this cookie is supposed to be removed.
         * 
         * @return true if this cookie is removed or expired
         */
        public boolean getIsRemovedOrExpired()
        {
            return false; // does not apply here
        }
       
        /**
         * Return character of value.
         * 
         * @param index of the character
         * @return the character
         */
        public char charAt(
                int index )
        {
            return theValue.charAt( index );
        }

        /**
         * Length of value.
         * 
         * @return length
         */
        public int length()
        {
            return theValue.length();
        }

        /**
         * Sub-sequence of value.
         * 
         * @param start start index
         * @param end end index
         * @return sub-sequence
         */
        public CharSequence subSequence(
                int start,
                int end )
        {
            return theValue.subSequence( start, end );
        }

        /**
         * String form. Here, the value.
         * 
         * @return the value
         */
        @Override
        public String toString()
        {
            return theValue;
        }

        /**
         * The Servlet Cookie we delegate to.
         */
        protected Cookie theDelegate;
        
        /**
         * The decoded name.
         */
        protected String theName;
        
        /**
         * The decoded value.
         */
        protected String theValue;
    }
}
