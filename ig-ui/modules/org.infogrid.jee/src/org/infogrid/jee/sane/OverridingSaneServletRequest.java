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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.util.http.IncomingSaneCookie;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * A <code>SaneRequest</code> that is constructed from a URL only. This means it cannot, by definition,
 * have values for some of the properties, such as POST content.
 */
public class OverridingSaneServletRequest
        extends
            SaneServletRequest
{
    private static final Log log = Log.getLogInstance( OverridingSaneServletRequest.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param url the URL from which to create a SaneRequest.
     * @param method name of the HTTP method, e.g. "GET"
     * @param overridden the overridden SaneServletRequest
     * @return the created OverridingSaneServletRequest
     */
    public static OverridingSaneServletRequest create(
             URL                url,
             String             method,
             SaneServletRequest overridden )
    {
        Map<String,String[]>   urlArguments    = new HashMap<String,String[]>();

        String queryString      = url.getQuery();
        String server           = url.getHost();
        String httpHostOnly     = server;
        int    port             = url.getPort();
        String protocol         = url.getProtocol().equalsIgnoreCase( "https" ) ? "https" : "http";
        String relativeBaseUri  = url.getPath();

        if( port == -1 ) {
            port = "http".equals( protocol ) ? 80 : 443;
        }

        String httpHost = constructHttpHost( httpHostOnly, protocol, port );

        String relativeFullUri = relativeBaseUri;

        if( queryString != null && queryString.length() != 0 ) {
            relativeFullUri += "?" + queryString;
        }

        addUrlEncodedArguments( queryString, urlArguments );

        OverridingSaneServletRequest ret = new OverridingSaneServletRequest(
                overridden.getDelegate(),
                method,
                server,
                port,
                httpHostOnly,
                httpHost,
                protocol,
                relativeBaseUri,
                relativeFullUri,
                overridden.getContextPath(),
                urlArguments,
                queryString,
                overridden.getClientIp(),
                overridden.getSaneRequestAtProxy() );

        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret, ret );
        }
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
     * @param urlArguments the arguments given in the URL, if any
     * @param queryString the string past the ? in the URL
     * @param clientIp the client's IP address
     * @param requestAtProxy the request as received by the reverse proxy, if any
     */
    protected OverridingSaneServletRequest(
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
            Map<String,String[]>   urlArguments,
            String                 queryString,
            String                 clientIp,
            SaneRequest            requestAtProxy )
    {
        super(  sRequest,
                method,
                server,
                port,
                httpHostOnly,
                httpHost,
                protocol,
                relativeBaseUri,
                relativeFullUri,
                contextPath,
                null,
                urlArguments,
                null,
                null,
                queryString,
                new IncomingSaneCookie[0],
                null,
                clientIp,
                requestAtProxy );
    }
}
