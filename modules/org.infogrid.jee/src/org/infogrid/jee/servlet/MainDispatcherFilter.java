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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Main dispatcher for the different request types.</p>
 * <p>The rules are as follows:</p>
 * <ul>
 *  <li>If the path matches the <code>REST_REGEX</code> regular expression as specified
 *      in the <code>web.xml</code> file for this Filter, it will interpret the incoming
 *      request as a request for a REST-ful resource (<code>MeshObject</code>) and activate
 *      REST-ful processing.</li>
 *  <li>Otherwise, the Filter simply continues executing further down the Filter chain.</li>
 * </ul>
 * <p>REST-ful processing here means that the incoming URL is interpreted as a request for
 *    a <code>MeshObject</code>, and control is passed to the
 *    {@link org.infogrid.jee.servlet.ViewletDispatcherServlet ViewletDispatcherServlet}.</p>
 * <p>The following Filter parameters are available in the <code>web.xml</code> file:</p>
 * <table class="infogrid-border">
 *  <thead>
 *   <tr>
 *    <td>Parameter Name</td>
 *    <td>Description</td>
 *    <td>Required?</td>
 *   </tr>
 *  </thead>
 *  <tbody>
 *   <tr>
 *    <td><code>REST_REGEX</code></td>
 *    <td>Java regular expression that categorizes requests into REST-ful requests (regex matches) and
 *        others (regex does not match)</td>
 *    <td>Optional. Defaults to &quot;at least four characters in the first segment of the path, or <code>/</code> itself.</td>
 *   </tr>
 *   <tr>
 *    <td><code>VIEWLET_DISPATCHER_SERVLET_NAME</code></td>
 *    <td>Filter parameter that identifies the servlet name (in <code>web.xml</code>) of the
 *        {@link org.infogrid.jee.servlet.ViewletDispatcherServlet ViewletDispatcherServlet}.</td>
 *    <td>Optional. Defaults to <code>org.infogrid.jee.servlet.ViewletDispatcherServlet</code> (its class name).</td>
 *   </tr>
 *  </tbody>
 * </table>
 */

public class MainDispatcherFilter
        implements
            Filter
{
    /**
     * Constructor.
     */
    public MainDispatcherFilter()
    {
    }
    
    /**
     * Main filter method.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        HttpServletRequest  realRequest  = (HttpServletRequest)  request;

        String              relativePath = realRequest.getServletPath();
        ServletContext      context      = theFilterConfig.getServletContext();

        Matcher restMatcher = theRestPattern.matcher( relativePath );
        if( restMatcher.matches() ) {
            // REST-ful
            
            RequestDispatcher theRequestDispatcher = context.getNamedDispatcher( theViewletDispatcherServletName );
            
            if( theRequestDispatcher == null ) {
                throw new ServletException( "Named dispatcher '" + theViewletDispatcherServletName + "' could not be found" );
            }
            theRequestDispatcher.include( request, response );

        } else {
            chain.doFilter( request, response );
        }
    }

    /**
     * Destroy method for this filter.
     */
    public void destroy()
    {
    }
    
    /**
     * Initialization method for this filter.
     * 
     * @param filterConfig the Filter configuration
     */
    public void init(
            FilterConfig filterConfig )
    {
        theFilterConfig = filterConfig;
        
        String restRegex = filterConfig.getInitParameter( REST_REGEX_PARAMETER );
        if( restRegex == null || restRegex.length() == 0 ) {
            restRegex = REST_REGEX_DEFAULT;
        }
        theRestPattern = Pattern.compile( restRegex );

        theViewletDispatcherServletName = filterConfig.getInitParameter( VIEWLET_DISPATCHER_SERVLET_NAME_PARAMETER );
        if( theViewletDispatcherServletName == null || theViewletDispatcherServletName.length() == 0 ) {
            theViewletDispatcherServletName = VIEWLET_DISPATCHER_SERVLET_NAME_DEFAULT;
        }
    }

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;

    /**
     * Regular expression that identifies REST-ful requests.
     */
    protected Pattern theRestPattern;

    /**
     * Name of the ViewletDispatcherServlet in the web.xml file, if any.
     */
    protected String theViewletDispatcherServletName;

    /**
     * Name of the Filter configuration parameter that contains the REST regular expression.
     */
    public static final String REST_REGEX_PARAMETER = "REST_REGEX";
    
    /**
     * Default for the REST_REGEX parameter.
     */
    public static final String REST_REGEX_DEFAULT = "^/([^/]{4,}(.*))?$"; // anything that has more than 3 characters in the first segment, or / itself

    /**
     * Name of the Filter configuration parameter that identifies the servlet name of the ViewletDispatcherServlet.
     */
    public static final String VIEWLET_DISPATCHER_SERVLET_NAME_PARAMETER = "VIEWLET_DISPATCHER_SERVLET_NAME";
    
    /**
     * Default for the VIEWLET_DISPATCHER_SERVLET_NAME parameter.
     */
    public static final String VIEWLET_DISPATCHER_SERVLET_NAME_DEFAULT = "ViewletDispatcher";
}
