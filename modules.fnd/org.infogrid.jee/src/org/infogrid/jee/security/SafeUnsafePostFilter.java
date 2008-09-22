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

package org.infogrid.jee.security;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.util.http.SaneRequest;

/**
 * Categorizes incoming POST requests as safe or unsafe, depending on whether they contain
 * a valid form token or not.
 */
public class SafeUnsafePostFilter
        implements
            Filter
{
    /**
     * Constructor.
     */
    public SafeUnsafePostFilter()
    {
        // nothing right now
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
        boolean isSafe = true;
        FormTokenService theFormTokenService = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( 
                FormTokenService.class );

        if(    theFormTokenService != null // otherwise we always think it's safe
            && request instanceof HttpServletRequest )
        {
            HttpServletRequest realRequest = (HttpServletRequest) request;
            if( "POST".equalsIgnoreCase( realRequest.getMethod() )) {
                SaneRequest sane  = (SaneRequest) realRequest.getAttribute( SaneServletRequest.SANE_SERVLET_REQUEST_ATTRIBUTE_NAME  );
                String      token = sane.getPostArgument( INPUT_FIELD_NAME );
                
                isSafe = theFormTokenService.validateToken( token );
            }
        }
        request.setAttribute( SAFE_UNSAFE_FLAG, isSafe );
        chain.doFilter( request, response );
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
        theFilterConfig  = filterConfig;
    }
    
    /**
     * Destroy method for this Filter.
     */
    public void destroy()
    {
        // noop
    }
    
    /**
     * Determine whether this incoming request is a safe POST. This is a static method
     * here so it can be invoked from anywhere in the application.
     * 
     * @param request the incoming request
     * @return true if this is an HTTP POST, and the POST is safe
     */
    public static boolean isSafePost(
            HttpServletRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe != null && safeUnsafe.booleanValue() ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Determine whether this incoming request is an unsafe POST. This is a static method
     * here so it can be invoked from anywhere in the application.
     * 
     * @param request the incoming request
     * @return true if this is an HTTP POST, but the POST is not safe
     */
    public static boolean isUnsafePost(
            HttpServletRequest request )
    {
        boolean ret = false;
        if( "POST".equalsIgnoreCase( request.getMethod() )) {
            Boolean safeUnsafe = (Boolean) request.getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
            if( safeUnsafe == null || !safeUnsafe.booleanValue() ) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * The filter configuration object this Filter is associated with.
     */
    protected FilterConfig theFilterConfig = null;
    
    /**
     * Name of the attribute in the incoming request that indicates whether this is a safe request or not.
     */
    public static final String SAFE_UNSAFE_FLAG
            = SaneServletRequest.classToAttributeName( SafeUnsafePostFilter.class, "safeunsafe" );

    /**
     * Name of the hidden field in the form.
     */
    public static final String INPUT_FIELD_NAME = "org-infogrid-jee-store-StoreTokenFormTag-csrf-field";
}
