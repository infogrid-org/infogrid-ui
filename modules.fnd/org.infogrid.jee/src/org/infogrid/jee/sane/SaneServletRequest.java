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

package org.infogrid.jee.sane;

import org.infogrid.util.http.MimePart;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
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
import org.infogrid.util.CursorIterator;
import org.infogrid.util.MapCursorIterator;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.ZeroElementCursorIterator;
import org.infogrid.util.http.AbstractSaneRequest;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneCookie;
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
        Map<String,String[]>   arguments     = new HashMap<String,String[]>();
        Map<String,String[]>   postArguments = new HashMap<String,String[]>();
        Map<String,MimePart[]> mimeParts     = new HashMap<String,MimePart[]>();

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
        
        if( "http".equals( protocol )) {
            if( port != 80 ) {
                httpHost += ":" + port;
            }
        } else {
            if( port != 443 ) {
                httpHost += ":" + port;
            }
        }
        
        String relativeBaseUri = sRequest.getRequestURI();
        String relativeFullUri = relativeBaseUri;

        if( queryString != null && queryString.length() != 0 ) {
            relativeFullUri += "?" + queryString;
        }

        Cookie []     servletCookies = sRequest.getCookies();
        SaneCookie [] cookies;

        if( servletCookies != null ) {
            cookies = new SaneCookie[ servletCookies.length ];
            for( int i=0 ; i<servletCookies.length ; ++i ) {
                cookies[i] = new CookieAdapter( servletCookies[i] );
            }
        } else {
            cookies = new SaneCookie[0];
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

                    addUrlEncodedArguments( postData, true, arguments, postArguments );

                } else {
                    addFormDataArguments( inStream, mimeType, arguments, postArguments, mimeParts );
                }

            } catch( IOException ex ) {
                log.error( ex );
            }
        } else {
            mimeType = null;
        }

        addUrlEncodedArguments( queryString, false, arguments, postArguments );
     
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
                mimeParts,
                queryString,
                cookies,
                mimeType,
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
     * @param mimeParts the MimeParts given via HTTP POST, if any
     * @param queryString the string past the ? in the URL
     * @param cookies the sent cookies
     * @param mimeType the sent MIME type, if any
     * @param clientIp the client's IP address
     */
    protected SaneServletRequest(
            HttpServletRequest     sRequest,
            String                 method,
            String                 server,
            int                    port,
            String                 httpHostOnly,
            String                 httpHost,
            String                 protocol,
            String                 relativeBaseUri,
            String                 relativeFullUri,
            String                 contextPath,
            String                 postData,
            Map<String,String[]>   arguments,
            Map<String,String[]>   postArguments,
            Map<String,MimePart[]> mimeParts,
            String                 queryString,
            SaneCookie []          cookies,
            String                 mimeType,
            String                 clientIp )
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
        theMimeParts       = mimeParts;
        theQueryString     = queryString;
        theCookies         = cookies;
        theMimeType        = mimeType;
        theClientIp        = clientIp;
    }

    /**
     * Helper to parse URL-encoded URL and POST data, and put them in the right places.
     *
     * @param data the data to add
     * @param isPost true of this argument was provided in an HTTP POST
     * @param arguments the URL arguments in the process of being assembled
     * @param postArguments the URL POST arguments in the process of being assembled
     */
    protected static void addUrlEncodedArguments(
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
     * Helper to parse formdata-encoded POST data, and put them in the right places.
     *
     * @param inStream the incoming data
     * @param mime the MIME type of the incoming content
     * @param arguments the URL arguments in the process of being assembled
     * @param postArguments the URL POST arguments in the process of being assembled
     * @param mimeParts the MIME parts in the process of being assembled
     * @throws IOException thrown if an I/O error occurred
     */
    protected static void addFormDataArguments(
            BufferedInputStream    inStream,
            String                 mime,
            Map<String,String[]>   arguments,
            Map<String,String[]>   postArguments,
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
                String [] already = postArguments.get( partName );
                String [] toPut;

                if( already != null ) {
                    toPut = ArrayHelper.append( already, value, String.class );
                } else {
                    toPut = new String[] { value };
                }
                postArguments.put( partName, toPut );
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
     * Obtain the relative context Uri of this application.
     *
     * @return the relative context URI
     */
    public String getContextPath()
    {
        return theContextPath;
    }

    /**
     * Obtain the absolute context Uri of this application.
     *
     * @return the absolute context URI
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
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theDelegate",
                    "theMethod",
                    "theServer",
                    "theHttpHost",
                    "theHttpHostOnly",
                    "thePort",
                    "theRelativeBaseUri",
                    "theRelativeFullUri",
                    "theCookies",
                    "theProtocol",
                    "theQueryString",
                    "theAbsoluteContextUri",
                    "theContextPath",
                    "theArguments",
                    "thePostArguments",
                    "theRequestedMimeTypes",
                    "theClientIp"
                },
                new Object[] {
                    theDelegate,
                    theMethod,
                    theServer,
                    theHttpHost,
                    theHttpHostOnly,
                    thePort,
                    theRelativeBaseUri,
                    theRelativeFullUri,
                    theCookies,
                    theProtocol,
                    theQueryString,
                    theAbsoluteContextUri,
                    theContextPath,
                    theArguments,
                    thePostArguments,
                    theRequestedMimeTypes,
                    theClientIp
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
                String delegateName = theDelegate.getName();
                theName = HTTP.decodeUrlArgument( delegateName );
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
                theValue = HTTP.decodeUrlArgument( delegateValue );
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
            String ret = theDelegate.getDomain();
            return ret;
        }

        /**
         * Get the Cookie path.
         *
         * @return the Cookie path
         */
        public String getPath()
        {
            String ret = theDelegate.getPath();
            return ret;
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
            return getValue().charAt( index );
        }

        /**
         * Length of value.
         * 
         * @return length
         */
        public int length()
        {
            return getValue().length();
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
            return getValue().subSequence( start, end );
        }

        /**
         * String form. Here, the value.
         * 
         * @return the value
         */
        @Override
        public String toString()
        {
            return getValue();
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
