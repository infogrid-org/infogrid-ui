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

package org.infogrid.jee.sane;

import org.infogrid.util.http.MimePart;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.util.ArrayCursorIterator;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CompositeIterator;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.MapCursorIterator;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.ZeroElementCursorIterator;
import org.infogrid.util.http.AbstractSaneCookie;
import org.infogrid.util.http.AbstractSaneRequest;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.IncomingSaneCookie;
import org.infogrid.util.http.SaneCookie;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * A ServletRequest following the <code>SaneRequest</code> API.
 */
public class SaneServletRequest
        extends
            AbstractSaneRequest
        implements
            CanBeDumped
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
        Map<String,String[]>   urlArguments    = new HashMap<String,String[]>();
        Map<String,String[]>   postedArguments = new HashMap<String,String[]>();
        Map<String,MimePart[]> mimeParts       = new HashMap<String,MimePart[]>();

        String postData = null;

        String queryString      = sRequest.getQueryString();
        String method           = sRequest.getMethod();
        String server           = sRequest.getServerName();
        int    port             = sRequest.getServerPort();
        String protocol         = sRequest.getScheme().equalsIgnoreCase( "https" ) ? "https" : "http";
        String relativeBaseUri  = sRequest.getRequestURI();
        String contextPath      = sRequest.getContextPath();
        String clientIp         = sRequest.getRemoteAddr();

        String serverWithNonDefaultPort = constructServerPlusNonDefaultPort( server, protocol, port );

        Cookie []             servletCookies = sRequest.getCookies();
        IncomingSaneCookie [] cookies;

        if( servletCookies != null ) {
            cookies = new IncomingCookieAdapter[ servletCookies.length ];
            for( int i=0 ; i<servletCookies.length ; ++i ) {
                cookies[i] = new IncomingCookieAdapter( servletCookies[i] );
            }
        } else {
            cookies = new IncomingCookieAdapter[0];
        }

        String mimeType;
        // URL parameters override POSTed fields: more intuitive for the user
        if( "POST".equalsIgnoreCase( method ) ) { // we do our own parsing

            mimeType = sRequest.getContentType();
            try {
                BufferedInputStream inStream = new BufferedInputStream( sRequest.getInputStream() );
                if( mimeType == null || !mimeType.startsWith( FORM_DATA_MIME )) {
                    int     length = sRequest.getContentLength();
                    byte [] buf    = StreamUtils.slurp( inStream, length );

                    postData = new String( buf, "utf-8" );

                    addUrlEncodedArguments( postData, postedArguments );

                } else {
                    addFormDataArguments( inStream, mimeType, postedArguments, mimeParts );
                }
                postedArguments.remove( LID_SUBMIT_PARAMETER_NAME ); // delete the contribution of the submit button

            } catch( IOException ex ) {
                log.error( ex );
            }
        } else {
            mimeType = null;
        }

        addUrlEncodedArguments( queryString, urlArguments );
        urlArguments.remove( LID_SUBMIT_PARAMETER_NAME ); // delete the contribution of the submit button

        SaneRequest requestAtProxy = null;
        if( theDetermineRequestFromProxyOriginalRequest ) {
            // We might be behind a reverse proxy. If the appropriate headers are set, use them for
            // an original request. If not, use the local ones.

            String clientIpAtProxy     = sRequest.getHeader( HTTP_PROXY_HEADER_FORWARDED_FOR );
            String serverAtProxy       = sRequest.getHeader( HTTP_PROXY_FORWARDED_SERVER );
            String protocolAtProxy     = sRequest.getHeader( HTTP_PROXY_FORWARDED_PROTOCOL );
            String contextPathAtProxy  = sRequest.getHeader( HTTP_PROXY_FORWARDED_CONTEXT );
            String tmp                 = sRequest.getHeader( HTTP_PROXY_FORWARDED_PORT );

            if(    clientIpAtProxy     != null
                || serverAtProxy       != null
                || serverAtProxy       != null
                || protocolAtProxy     != null
                || contextPathAtProxy  != null
                || tmp                 != null )
            {
                if( clientIpAtProxy == null ) {
                    clientIpAtProxy = clientIp;
                }
                if( serverAtProxy == null ) {
                    serverAtProxy = server;
                }
                if( contextPathAtProxy == null ) {
                    contextPathAtProxy = contextPath;
                }

                if( protocolAtProxy != null ) {
                    protocolAtProxy = protocolAtProxy.equalsIgnoreCase( "https" ) ? "https" : "http";
                } else {
                    protocolAtProxy = protocol;
                }

                int portAtProxy = -1;
                if( tmp != null ) {
                    portAtProxy = Integer.parseInt( tmp );
                } else {
                    portAtProxy = port;
                }

                String serverWithNonDefaultPortAtProxy = constructServerPlusNonDefaultPort( serverAtProxy, protocolAtProxy, portAtProxy );
                if( serverWithNonDefaultPortAtProxy == null ) {
                    serverWithNonDefaultPortAtProxy = serverWithNonDefaultPort;
                }

                String relativeBaseUriAtProxy;
                if( contextPathAtProxy != null ) {
                    if( contextPathAtProxy.endsWith( "/" )) {
                        // supposed to be given without slash, but then it not always is, particularly for "/" itself
                        contextPathAtProxy = contextPathAtProxy.substring( 0, contextPathAtProxy.length()-1 );
                    }
                    relativeBaseUriAtProxy = contextPathAtProxy + relativeBaseUri.substring( contextPath.length() );
                } else {
                    relativeBaseUriAtProxy = relativeBaseUri;
                }

                requestAtProxy = new SaneServletRequest(
                        sRequest,
                        method,
                        protocolAtProxy,
                        serverAtProxy,
                        portAtProxy,
                        serverWithNonDefaultPortAtProxy,
                        relativeBaseUriAtProxy,
                        queryString,
                        urlArguments,
                        postData,
                        postedArguments,
                        mimeParts,
                        cookies,
                        mimeType,
                        clientIpAtProxy,
                        contextPathAtProxy,
                        null );
            }
        }

        SaneServletRequest ret = new SaneServletRequest(
                sRequest,
                method,
                protocol,
                server,
                port,
                serverWithNonDefaultPort,
                relativeBaseUri,
                queryString,
                urlArguments,
                postData,
                postedArguments,
                mimeParts,
                cookies,
                mimeType,
                clientIp,
                contextPath,
                requestAtProxy );

        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret, ret );
        }
        return ret;
    }

    /**
     * Private constructor, use factory method
     *
     * @param sRequest the HttpServletRequest from which the SaneServletRequest is created
     * @param method the HTTP method
     * @param protocol http or https
     * @param server the server
     * @param port the server port
     * @param serverPlusNonDefaultPort the server, plus, if the port is non-default, a colon and the port number
     * @param relativeBaseUri the relative base URI
     * @param queryString the string past the ? in the URL
     * @param urlArguments the arguments given in the URL, if any
     * @param postData the data HTTP POST'd, if any
     * @param postedArguments the arguments given via HTTP POST, if any
     * @param mimeParts the MimeParts given via HTTP POST, if any
     * @param cookies the sent cookies
     * @param mimeType the sent MIME type, if any
     * @param clientIp the client's IP address
     * @param contextPath the JEE context path
     * @param requestAtProxy the SaneRequest received by the reverse proxy, if any
     */
    protected SaneServletRequest(
            HttpServletRequest     sRequest,
            String                 method,
            String                 protocol,
            String                 server,
            int                    port,
            String                 serverPlusNonDefaultPort,
            String                 relativeBaseUri,
            String                 queryString,
            Map<String,String[]>   urlArguments,
            String                 postData,
            Map<String,String[]>   postedArguments,
            Map<String,MimePart[]> mimeParts,
            IncomingSaneCookie []  cookies,
            String                 mimeType,
            String                 clientIp,
            String                 contextPath,
            SaneRequest            requestAtProxy )
    {
        super( protocol, server, port, serverPlusNonDefaultPort, relativeBaseUri, queryString, urlArguments, contextPath, requestAtProxy );

        theDelegate         = sRequest;
        theMethod           = method;
        thePostData         = postData;
        thePostedArguments  = postedArguments;
        theMimeParts        = mimeParts;
        theCookies          = cookies;
        theMimeType         = mimeType;
        theClientIp         = clientIp;
    }
    
    /**
     * Helper to parse formdata-encoded POST data, and put them in the right places.
     *
     * @param inStream the incoming data
     * @param mime the MIME type of the incoming content
     * @param arguments the URL arguments in the process of being assembled
     * @param mimeParts the MIME parts in the process of being assembled
     * @throws IOException thrown if an I/O error occurred
     */
    protected static void addFormDataArguments(
            BufferedInputStream    inStream,
            String                 mime,
            Map<String,String[]>   arguments,
            Map<String,MimePart[]> mimeParts )
        throws
            IOException
    {
        String charset = "ISO-8859-1"; // FIXME, needs to be more general
        String content;

        if( true ) {
            // makes debugging easier
            byte [] content2 = StreamUtils.slurp( inStream );
            inStream = new BufferedInputStream( new ByteArrayInputStream( content2 ));
            content = new String( content2, charset );
        }
        // determine boundary
        String  stringBoundary = FormDataUtils.determineBoundaryString( mime );
        byte [] byteBoundary   = FormDataUtils.constructByteBoundary( stringBoundary, charset );

        // forward to first boundary
        StreamUtils.slurpUntilBoundary( inStream, byteBoundary );
        boolean hasData = FormDataUtils.advanceToBeginningOfLine( inStream );

        // past first boundary now
        outer:
        while( hasData ) { // for all parts

            HashMap<String,String> partHeaders = new HashMap<String,String>();
            String currentLogicalLine = null;
            while( true ) { // for all headers in this part
                String line = FormDataUtils.readStringLine( inStream, charset );
                if( line == null ) {
                    hasData = false;
                    break outer; // end of stream -- we don't want heads and no content
                }
                if( line.startsWith( stringBoundary )) {
                    // not sure why it would do this here, but let's be safe
                    break;
                }
                if( line.length() == 0 ) {
                    break; // done with headers
                }
                if( Character.isWhitespace( line.charAt( 0 ) )) {
                    // continuation line
                    currentLogicalLine += line; // will throw if no currentLogicalLine, which is fine
                } else {
                    if( currentLogicalLine != null ) {
                        FormDataUtils.addNameValuePairTo( currentLogicalLine, partHeaders );
                    }
                    currentLogicalLine = line;
                }
            }
            if( currentLogicalLine != null ) {
                // don't forget the last line
                FormDataUtils.addNameValuePairTo( currentLogicalLine, partHeaders );
            }

            // have headers now, let's get the data
            byte [] partData = StreamUtils.slurpUntilBoundary( inStream, byteBoundary );
            if( partData == null || partData.length == 0 ) {
                hasData = false;
                break outer; // end of stream -- we don't want heads and no content
            }
            partData = FormDataUtils.stripTrailingBoundary( partData, byteBoundary );
            hasData  = FormDataUtils.advanceToBeginningOfLine( inStream );

            String partMime = partHeaders.get( "content-type" );
            if( partMime == null ) {
                partMime = "text/plain"; // apparently the default
            }

            String partCharset     = charset; // FIXME?
            String partName        = null;
            String partDisposition = null;
            String disposition     = partHeaders.get( "content-disposition" );
            if( disposition != null ) {
                String [] dispositionData = disposition.split( ";" );
                partDisposition = dispositionData[0];

                for( int i=1 ; i<dispositionData.length ; ++i ) { // ignore first, that's different
                    String current = dispositionData[i];
                    int    equals  = current.indexOf( '=' );
                    if( equals > 0 ) {
                        String key   = current.substring( 0, equals ).trim().toLowerCase();
                        String value = current.substring( equals+1 ).trim();

                        if( value.startsWith( "\"" ) && value.endsWith( "\"" )) {
                            value = value.substring( 1, value.length()-1 );
                        }

                        if( "name".equals( key )) {
                            partName = value;
                        }
                    }
                }
            }

            if( partName != null ) {
                MimePart part = MimePart.create( partName, partHeaders, partDisposition, partData, partMime, partCharset );

                MimePart [] already = mimeParts.get( partName );
                MimePart [] toPut;
                if( already != null ) {
                    toPut = ArrayHelper.append( already, part, MimePart.class );
                } else {
                    toPut = new MimePart[] { part };
                }
                mimeParts.put( partName, toPut );
            } else {
                log.warn( "Skipping unnamed part" );
            }

            // adding to post parameters too
            if( partName != null && "form-data".equals( partDisposition ) && partMime.startsWith( "text/" )) {
                String    value   = new String( partData, partCharset );
                String [] already = arguments.get( partName );
                String [] toPut;

                if( already != null ) {
                    toPut = ArrayHelper.append( already, value, String.class );
                } else {
                    toPut = new String[] { value };
                }
                arguments.put( partName, toPut );
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
        return theMethod;
    }

    /**
     * Obtain all values of a multi-valued POST'd argument.
     *
     * @param argName name of the argument
     * @return the values, or <code>null</code>
     */
    public String [] getMultivaluedPostedArgument(
            String argName )
    {
        if( thePostedArguments == null ) {
            return null;
        }
        String [] ret = thePostedArguments.get( argName );
        return ret;
    }

    /**
     * Obtain all POST'd arguments of this request.
     *
     * @return a Map of name to value mappings for all POST'd arguments
     */
    public Map<String,String[]> getPostedArguments()
    {
        return thePostedArguments;
    }    

    /**
     * Obtain the cookies that were sent as part of this request.
     *
     * @return the cookies that were sent as part of this request.
     */
    public synchronized IncomingSaneCookie[] getCookies()
    {
        if( theCookies == null ) {
            Cookie [] delegateCookies = theDelegate.getCookies();

            theCookies = new IncomingCookieAdapter[ delegateCookies.length ];
            for( int i=0 ; i<delegateCookies.length ; ++i ) {
                theCookies[i] = new IncomingCookieAdapter( delegateCookies[i] );
            }
        } 
        return theCookies;
    }

    /**
     * Obtain the request content type, if any. This only applies in
     * case of HTTP POST.
     *
     * @return the request content type
     */
    public String getContentType()
    {
        return theMimeType;
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
            
            return CompositeIterator.<Locale>createFromEnumerations(
                OneElementIterator.<Locale>create( cookieLocale ),
                (Enumeration<Locale>) fromHttp,
                OneElementIterator.<Locale>create( Locale.getDefault() ) );

        } else {
            return CompositeIterator.<Locale>createFromEnumerations(
                (Enumeration<Locale>) fromHttp,
                OneElementIterator.<Locale>create( Locale.getDefault() ) );
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
     * Obtain the value of the accept header, if any.
     *
     * @return the value of the accept header
     */
    public String getAcceptHeader()
    {
        String ret = theDelegate.getHeader( "Accept" );
        return ret;
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
     * Set a request-context attribute. The semantics are equivalent to setting
     * an attribute on an HttpServletRequest.
     *
     * @param name the name of the attribute
     * @param value the value of the attribute
     * @see #getAttribute
     * @see #removeAttribute
     * @see #getAttributeNames
     */
    public void setAttribute(
            String name,
            Object value )
    {
        theDelegate.setAttribute( name, value );
    }

    /**
     * Get a request-context attribute. The semantics are equivalent to getting
     * an attribute on an HttpServletRequest.
     *
     * @param name the name of the attribute
     * @return the value of the attribute
     * @see #setAttribute
     * @see #removeAttribute
     * @see #getAttributeNames
     */
    public Object getAttribute(
            String name )
    {
        Object ret = theDelegate.getAttribute( name );
        return ret;
    }

    /**
     * Remove a request-context attribute. The semantics are equivalent to removing
     * an attribute on an HttpServletRequest.
     *
     * @param name the name of the attribute
     * @see #setAttribute
     * @see #getAttribute
     * @see #getAttributeNames
     */
    public void removeAttribute(
            String name )
    {
        theDelegate.removeAttribute( name );
    }

    /**
     * Iterate over all request-context attributes currently set.
     *
     * @return an Iterator over the names of all the request-context attributes
     * @see #setAttribute
     * @see #getAttribute
     * @see #removeAttribute
     */
    @SuppressWarnings("unchecked")
    public Enumeration<String> getAttributeNames()
    {
        return theDelegate.getAttributeNames();
    }

    /**
     * Obtain the names of the MimeParts conveyed.
     *
     * @return the names of the MimeParts
     */
    public CursorIterator<String> getMimePartNames()
    {
        if( theMimeParts != null ) {
            return MapCursorIterator.createForKeys( theMimeParts, String.class, MimePart[].class );
        } else {
            return ZeroElementCursorIterator.create();
        }
    }

    /**
     * Obtain all MimeParts with a given name
     *
     * @param argName name of the MimePart
     * @return the values, or <code>null</code>
     */
    public MimePart [] getMultivaluedMimeParts(
            String argName )
    {
        if( theMimeParts == null ) {
            return null;
        }
        MimePart [] ret = theMimeParts.get( argName );
        return ret;
    }

    /**
     * Convert to String representation, for debugging reasons only.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return getAbsoluteFullUri();
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theDetermineRequestFromProxyOriginalRequest",
                    "theDelegate",
                    "theMethod",
                    "theServer",
                    "thePort",
                    "theRelativeBaseUri",
                    "theCookies",
                    "theProtocol",
                    "theQueryString",
                    "theAbsoluteContextUri",
                    "theContextPath",
                    "theUrlArguments",
                    "thePostedArguments",
                    "theRequestedMimeTypes",
                    "theClientIp",
                    "theRequestAtProxy"
                },
                new Object[] {
                    theDetermineRequestFromProxyOriginalRequest,
                    theDelegate,
                    theMethod,
                    theServer,
                    thePort,
                    theRelativeBaseUri,
                    theCookies,
                    theProtocol,
                    theQueryString,
                    theAbsoluteContextUri,
                    theContextPath,
                    theUrlArguments,
                    thePostedArguments,
                    theRequestedMimeTypes,
                    theClientIp,
                    theRequestAtProxy
                } );
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
     * The cookies on this request. Allocated when needed.
     */
    protected IncomingSaneCookie[] theCookies;
    
    /**
     * The data that was posted, if any.
     */
    protected String thePostData;

    /**
     * The arguments to the Request that were POST'd, if any.
     */
    protected Map<String,String[]> thePostedArguments;

    /**
     * The MIME type, if any.
     */
    protected String theMimeType;

    /**
     * Name of the request attribute that contains an instance of SaneServletRequest.
     */
    public static final String SANE_SERVLET_REQUEST_ATTRIBUTE_NAME = classToAttributeName( SaneServletRequest.class );

    /**
     * The requested MIME types, in sequence of prioritization. Allocated as needed.
     */
    protected String [] theRequestedMimeTypes;

    /**
     * The conveyed MimeParts, if any.
     */
    protected Map<String,MimePart[]> theMimeParts;

    /**
     * The IP address of the client.
     */
    protected String theClientIp;

    /**
     * Name of the MIME type that JEE does not know how to parse. :-(
     */
    public static final String FORM_DATA_MIME = "multipart/form-data";

    /**
     * If true, determine the SaneServletRequest from non-standard HTTP headers set by a reverse proxy.
     */
    public static final boolean theDetermineRequestFromProxyOriginalRequest
            = ResourceHelper.getInstance( SaneServletRequest.class ).getResourceBooleanOrDefault(
                    "DetermineRequestFromProxyOriginalRequest",
                    false );

    /**
     * HTTP header name for the client IP address in a reverse proxy configuration.
     */
    public static final String HTTP_PROXY_HEADER_FORWARDED_FOR = "X-Forwarded-For";

    /**
     * HTTP header name for the host in a reverse proxy configuration.
     */
    public static final String HTTP_PROXY_HEADER_FORWARDED_HOST = "X-Forwarded-Host";

    /**
     * HTTP header name for the server in a reverse proxy configuration.
     */
    public static final String HTTP_PROXY_FORWARDED_SERVER = "X-Forwarded-Server";

    /**
     * HTTP header name for the protocol in a reverse proxy configuration.
     */
    public static final String HTTP_PROXY_FORWARDED_PROTOCOL = "X-Forwarded-Proto";

    /**
     * HTTP header name for the port in a reverse proxy configuration.
     */
    public static final String HTTP_PROXY_FORWARDED_PORT = "X-Forwarded-Port";

    /**
     * HTTP header name for the context path in a reverse proxy configuration.
     */
    public static final String HTTP_PROXY_FORWARDED_CONTEXT = "X-Forwarded-Context";

    /**
     * Name of a POSTed parameter that represents the submit button.
     */
    public static final String LID_SUBMIT_PARAMETER_NAME = "lid-submit";

    /**
     * Bridges the SaneCookie interface into the servlet cookies.
     */
    static class IncomingCookieAdapter
        extends
            AbstractSaneCookie
        implements
            IncomingSaneCookie,
            CanBeDumped
    {
        /**
         * Constructor.
         *
         * @param delegate the Servlet Cookie we delegate to
         */
        public IncomingCookieAdapter(
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
                String delegateName = theDelegate.getName();
                theName = HTTP.decodeCookieName( delegateName );
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
                String delegateValue = theDelegate.getValue();
                theValue = HTTP.decodeFromQuotedString( delegateValue );
            }
            return theValue;
        }

        /**
         * Dump this object.
         *
         * @param d the Dumper to dump to
         */
        public void dump(
                Dumper d )
        {
            d.dump( this,
                    new String[] {
                            "getName()",
                            "getValue()"
                    },
                    new Object[] {
                            getName(),
                            getValue()
                    });
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
