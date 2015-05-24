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
// Copyright 1998-2015 by Johannes Ernst
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
 * <p>Regular-expression-based dispatcher for the different request types.</p>
 * <p>If the path matches the <code>REGEX</code> regular expression as specified
 *      in the <code>web.xml</code> file for this Filter, it will run a named Serlvet; if
 *      not, regular Filter processing will continue down the Filter chain. This is useful, for example, to
 *      interpret the incoming request as a request for a REST-ful resource (<code>MeshObject</code>) and activate
 *      REST-ful processing.</li></p>
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
 *    <td><code>REGEX</code></td>
 *    <td>Java regular expression that categorizes inomcing requests into those that match and those that
 *        do not match.</td>
 *    <td>Optional. Defaults to &quot;at least four characters in the first segment of the path, or <code>/</code> itself.</td>
 *   </tr>
 *   <tr>
 *    <td><code>MATCHED_SERVLET_NAME</code></td>
 *    <td>Filter parameter that identifies the servlet name (in <code>web.xml</code>) of the
 *        servlet to be run if there is a match.</td>
 *    <td>Optional. Defaults to <code>ViewletDispatcherServlet</code>.</td>
 *   </tr>
 *  </tbody>
 * </table>
 */

public class RegexDispatcherFilter
        implements
            Filter
{
    /**
     * Constructor.
     */
    public RegexDispatcherFilter()
    {
    }
    
    /**
     * Main filter method.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
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
        HttpServletRequest realRequest = (HttpServletRequest) request;

        String relativePath = realRequest.getServletPath();

        Matcher m = thePattern.matcher( relativePath );
        if( m.matches() ) {
            // REST-ful
            doFilterMatched( request, response, chain );

        } else {
            doFilterUnmatched( request, response, chain );
        }
    }

    /**
     * The request matched, now process.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    protected void doFilterMatched(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        ServletContext    context    = theFilterConfig.getServletContext();
        RequestDispatcher dispatcher = context.getNamedDispatcher( theServletName );

        if( dispatcher == null ) {
            throw new ServletException( "Named dispatcher '" + theServletName + "' could not be found" );
        }
        dispatcher.include( request, response );
    }

    /**
     * The request did not match, now process.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    protected void doFilterUnmatched(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        chain.doFilter( request, response );
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
        
        String restRegex = filterConfig.getInitParameter( REGEX_PARAMETER );
        if( restRegex == null || restRegex.length() == 0 ) {
            restRegex = REGEX_DEFAULT;
        }
        thePattern = Pattern.compile( restRegex );

        theServletName = filterConfig.getInitParameter( SERVLET_NAME_PARAMETER );
        if( theServletName == null || theServletName.length() == 0 ) {
            theServletName = SERVLET_NAME_DEFAULT;
        }
    }

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;

    /**
     * Regular expression that identifies matched requests.
     */
    protected Pattern thePattern;

    /**
     * Name of the Servlet in the web.xml file, if any.
     */
    protected String theServletName;

    /**
     * Name of the Filter configuration parameter that contains the REST regular expression.
     */
    public static final String REGEX_PARAMETER = "REGEX";
    
    /**
     * Default for the REGEX parameter.
     */
    public static final String REGEX_DEFAULT = "(^/$)|(^/[^/]{4,}(.*)$)"; // anything that has more than 3 characters in the first segment, or / itself

    /**
     * Name of the Filter configuration parameter that identifies the servlet name of the ViewletDispatcherServlet.
     */
    public static final String SERVLET_NAME_PARAMETER = "SERVLET_NAME";
    
    /**
     * Default for the SERVLET_NAME parameter.
     */
    public static final String SERVLET_NAME_DEFAULT = "ViewletDispatcher";
}
