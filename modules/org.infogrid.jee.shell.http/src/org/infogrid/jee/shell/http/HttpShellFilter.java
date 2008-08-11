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

package org.infogrid.jee.shell.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.SafeUnsafePostFilter;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.util.logging.Log;

/**
 * <p>Recognizes <code>MeshObject</code> change-related requests as part of the incoming HTTP
 *    request and processes them. The protocol to express those change-related requests has been
 *    constructed to make it easy to issue them from HTML forms using HTTP POST.</p>
 * 
 * <p>The protocol recognizes the following HTTP POST parameters:</p>
 * <table class="infogrid-border">
 *  <thead>
 *   <tr>
 *    <td>Parameter</td>
 *    <td>Description</td>
 *    <td>Required?</td>
 *   </tr>
 *  </thead>
 *  <tbody>
 *   <tr>
 *    <td><code>mesh.subject</code></td>
 *    <td>Identifier (external form of the <code>MeshObject</code>'s Identifier)
 *        for the primary subject of the operation</td>
 *    <td>exactly 1 required (if not given, the <code>HttpShellFilter</code> will do nothing)</td>
 *   </tr>
 *   <tr>
 *    <td><code>mesh.verb</code></td>
 *    <td>Type of operation to be performed, for enumeration see
 *        {@link org.infogrid.jee.shell.http.HttpShellVerb HttpShellVerb}</td>
 *    <td>exactly 1 required (if not given, the <code>HttpShellFilter</code> will do nothing)</td>
 *   </tr>
 *   <tr>
 *    <td><code>mesh.object</code></td>
 *    <td>Identifier (external form of the <code>MeshObject</code>'s Identifier)
 *        for another object involved in the operation</td>
 *    <td>0 or 1 (may be required for some values of <code>mesh.verb</code>)</td>
 *   </tr>
 *   <tr>
 *    <td><code>mesh.propertytype</code></td>
 *    <td>Identifier for a <code>PropertyType</code> (external form of the <code>PropertyType</code>'s
 *        Identifier) on the subject</td>
 *    <td>0..N</td>
 *   </tr>
 *   <tr>
 *    <td><code>mesh.propertyvalue</code></td>
 *    <td>New <code>PropertyValue</code> for the property on the subject (external form of
 *        the <code>PropertyValue</code>) identified by
 *        <code>mesh.subject</code> and <code>mesh.propertytype</code></td>
 *    <td>0..N. Must be given the same number of times as <code>mesh.propertytype</code>.</td>
 *   </tr>
 *   <tr>
 *    <td><code>mesh.subjecttype</code></td>
 *    <td>Identifier (external form of the <code>EntityType</code>'s Identifier)
 *        for a type of the subject</td>
 *    <td>0..N</td>
 *   </tr>
 *   <tr>
 *    <td><code>mesh.roletype</code></td>
 *    <td>Identifier for a type of role (external form of the <code>RoleType</code>'s Identifier)
 *        the subject plays with the object</td>
 *    <td>0..N. Only permitted if <code>mesh.object</code> is given</td>
 *   </tr>
 *  </tbody>
 * </table>
 */
public class HttpShellFilter
    implements
        Filter
{
    private static Log log; // initialized only after the InitializationFilter has run.

    /**
     * Constructor.
     */
    public HttpShellFilter()
    {
        if( log == null ) {
            log = Log.getLogInstance( HttpShellFilter.class ); // our own, private logger
        }
    }

    /**
     * Main filter operation.
     *
     * @param request The servlet request to process
     * @param response The servlet response to assemble
     * @param chain The filter chain this Filter is part of
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
        HttpServletResponse realResponse = (HttpServletResponse) response;

        SaneServletRequest lidRequest = (SaneServletRequest) realRequest.getAttribute( SaneServletRequest.class.getName() );
        InfoGridWebApp     app        = InfoGridWebApp.getSingleton();

        try {
            performFactoryOperations( lidRequest );
        
        } catch( Throwable ex ) {
            log.warn( ex );
            
            @SuppressWarnings( "unchecked" )
            List<Throwable> problems = (List<Throwable>) request.getAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME );
            if( problems == null ) {
                problems = new ArrayList<Throwable>();
                request.setAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME, problems );
            }
            synchronized( problems ) {
                problems.add( ex );
            }
            
        }
        chain.doFilter( realRequest, realResponse );
    }
    
    /**
     * Perform all factory methods contained in the request.
     * 
     * @param lidRequest the incoming request
     * @throws NotPermittedException thrown if the caller had insufficient privileges to perform this operation
     * @throws HttpShellException a factory Exception occurred
     */
    protected void performFactoryOperations(
            SaneServletRequest lidRequest )
        throws
            NotPermittedException,
            HttpShellException
    {
        if( !"POST".equals( lidRequest.getMethod() )) {
            return;
        }

        boolean isSafePost;
        Boolean safeUnsafe = (Boolean) lidRequest.getDelegate().getAttribute( SafeUnsafePostFilter.SAFE_UNSAFE_FLAG );
        if( safeUnsafe != null && !safeUnsafe.booleanValue() ) {
            isSafePost = false;
        } else {
            isSafePost = true;
        }

        if( isSafePost ) {
            HttpShellVerb v = HttpShellVerb.findApplicableVerb( lidRequest );
            if( v == null ) {
                return;
            }
            v.performVerb( lidRequest );

        } else {
            log.warn( "Ignoring unsafe POST " + lidRequest );
        }
    }

    /**
     * Initialization method for this filter.
     *
     * @param filterConfig the filter configuration object
     */
    public void init(
            FilterConfig filterConfig )
    {
        theFilterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter.
     */
    public void destroy()
    {
    }
    
    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;
    
    /**
     * The prefix for all keywords for the protocol.
     */
    public static final String PREFIX = "mesh.";

    /**
     * Keyword indicating the MeshBase in which the operation shall be performed.
     */
    public static final String MESH_BASE_TAG      = PREFIX + "meshbase";
    
    /**
     * Keyword indicating the subject of the operation.
     */
    public static final String SUBJECT_TAG        = PREFIX + "subject";
    
    /**
     * Keyword indicating the verb of the operation.
     */
    public static final String VERB_TAG           = PREFIX + "verb";
    
    /**
     * Keyword indicating the object of the operation.
     */
    public static final String OBJECT_TAG         = PREFIX + "object";
    
    /**
     * Keyword indicating the <code>PropertyType</code> for an operation.
     */
    public static final String PROPERTY_TYPE_TAG  = PREFIX + "propertytype";
    
    /**
     * Keyword indicating the <code>PropertyValue</code> for an operation.
     */
    public static final String PROPERTY_VALUE_TAG = PREFIX + "propertyvalue";
    
    /**
     * Keyword indicating the <code>EntityType</code> for the subject of the operation.
     */
    public static final String SUBJECT_TYPE_TAG   = PREFIX + "subjecttype";
    
    /**
     * Keyword indicating the <code>RoleType</code> played by the subject in an operation
     * with the object.
     */
    public static final String ROLE_TYPE_TAG      = PREFIX + "roletype";
}
