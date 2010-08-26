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

package org.infogrid.jee.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * <p>Filter that makes sure InfoGrid initialization has been performed prior to processing
 *    requests.</p>
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
 *    <td><code>org.infogrid.jee.app.InfoGridWebApp</code></td>
 *    <td>Filter parameter specifying the name of the class to use as the
 *        {@link org.infogrid.jee.app.InfoGridWebApp InfoGridWebApp}.
 *        This class must have a static factory method with the signature
 *        <code>public InfoGridWebApp create( String )</code>, where the String
 *        parameter is the name of the <code>DataSource</code> in the JNDI directory.</td>
 *    <td>Required</td>
 *   </tr>
 *   <tr>
 *    <td><code>DefaultMeshBaseIdentifier</code></td>
 *    <td>Filter parameter specifying the MeshBaseIdentifier of the default MeshBase in the
 *        application.</td>
 *    <td>Required</td>
 *   </tr>
 *  </tbody>
 * </table>
 */
public class InitializationFilter
        implements
            Filter
{
    protected static Log log; // initialized later

    /**
     * Constructor.
     */
    public InitializationFilter()
    {
        // noop
    }

    /**
     * Perform the main filter method.
     * 
     * @param request The incoming servlet request
     * @param response The outgoing servlet response
     * @param chain The filter chain to which this Filter belongs
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
        HttpServletRequest  realRequest  = (HttpServletRequest)  request;
        HttpServletResponse realResponse = (HttpServletResponse) response;

        try {
            request.setAttribute( CONTEXT_PARAMETER, realRequest.getContextPath() );

            initializeInfoGridWebApp( realRequest );

            // SaneServletRequest adds itself as a request attribute
            SaneRequest lidRequest      = SaneServletRequest.create( realRequest );
            SaneRequest originalRequest = lidRequest.getOriginalSaneRequest();

            if( getLog().isDebugEnabled() ) {
                getLog().debug( "InitializationFilter: ", lidRequest );
            }
            request.setAttribute( FULLCONTEXT_PARAMETER, lidRequest.getAbsoluteContextUri() );

            request.setAttribute( ORIGINAL_CONTEXT_PARAMETER,     originalRequest.getContextPath() );
            request.setAttribute( ORIGINAL_FULLCONTEXT_PARAMETER, originalRequest.getAbsoluteContextUri() );

            chain.doFilter( request, response );

        } catch( Throwable t ) {
            Log l = getLog();

            if( l != null ) { // catastrophic errors sometimes even prevent logging from being initialized
                l.error( t );
            } else {
                t.printStackTrace();
            }

            try {
                processException( realRequest, realResponse, t ); // may throw again
            } catch( ServletException t2 ) {
                throw t2;
            } catch( Throwable t2 ) {
                throw new ServletException( t2 );
            }
        }
    }

    /**
     * An Exception was thrown. This method handles the Exception.
     * 
     * @param request The incoming servlet request
     * @param response The outgoing servlet response
     * @param t the thrown Exception
     * @throws Throwable may re-throw the exception or a transformed exception
     */
    protected void processException(
            ServletRequest  request,
            ServletResponse response,
            Throwable       t )
        throws
            Throwable
    {
        Throwable rootCause = t;
        while( rootCause.getCause() == t ) {
            rootCause = rootCause.getCause();
        }
        throw new ServletException( rootCause );
    }
    
    /**
     * Initialize the InfoGridWebApp if needed.
     *
     * @param incomingRequest the incoming request
     * @throws ServletException thrown if the InfoGridWebApp could not be initialized
     */
    protected void initializeInfoGridWebApp(
            HttpServletRequest incomingRequest )
        throws
            ServletException
    {
        InfoGridWebApp theApp = InfoGridWebApp.getSingleton();
        if( theApp == null ) {
            String className = theFilterConfig.getInitParameter( INFOGRID_WEB_APP_CLASS_NAME_PARAMETER );
            if( className == null || className.length() == 0 ) {
                throw new ServletException( "Cannot initialize InfoGridWebApp: no " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " parameter given in web.xml file" );
            }

            try {                
                Class<?> appClass      = Class.forName( className );
                Method   factoryMethod = appClass.getMethod( "create", String.class );

                theApp = (InfoGridWebApp) factoryMethod.invoke( null, theDefaultMeshBaseIdentifier );

            } catch( ClassNotFoundException ex ) {
                throw new ServletException( "Cannot find class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex  );

            } catch( NoSuchMethodException ex ) {
                throw new ServletException( "Cannot find method \"create( String )\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex );

            } catch( IllegalAccessException ex ) {
                throw new ServletException( "Cannot access method \"create( String )\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex );

            } catch( InvocationTargetException ex ) {
                throw new ServletException( "Cannot execute method \"create( String )\" in class " + className + " specified as parameter " + INFOGRID_WEB_APP_CLASS_NAME_PARAMETER + " in Filter configuration (web.xml)", ex.getTargetException() );
            }

            try {
                InfoGridWebApp.setSingleton( theApp );

            } catch( IllegalStateException ex ) {
                // have one already, that's fine (a parallel thread was faster)
            }
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
     * @param filterConfig the filter configuration
     * @throws ServletException an exception occurred
     */
    public void init(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        theFilterConfig = filterConfig;
        
        theDefaultMeshBaseIdentifier = theFilterConfig.getInitParameter( DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER );
    }

    /**
     * Initialize and get the log.
     *
     * @return the log
     */
    private static Log getLog()
    {
        if( log == null ) {
            log = Log.getLogInstance( InitializationFilter.class ); // our own, private logger
        }
        return log;
    }

    /**
     * Name of the Filter parameter that contains the name of the InfoGridWebApp (sub-)class to use.
     */
    public static final String INFOGRID_WEB_APP_CLASS_NAME_PARAMETER = InfoGridWebApp.class.getName();

    /**
     * Name of the String in the RequestContext that contains the identifier of the default
     * MeshBase.
     */
    public static final String DEFAULT_MESH_BASE_IDENTIFIER_PARAMETER = "DefaultMeshBaseIdentifier";

    /**
     * Name of the String in the RequestContext that is the context path of the application.
     * Having this makes the development of path-independent JSPs much simpler. This
     * is a fully-qualified path from the root of the current host, not including the host.
     * @see #FULLCONTEXT_PARAMETER
     */
    public static final String CONTEXT_PARAMETER = "CONTEXT";
    
    /**
     * Name of the String in the RequestContext that is the context path of the application.
     * Having this makes the development of path-independent JSPs much simpler. This
     * is a fully-qualified path including protocol, host and port.
     * @see #CONTEXT_PARAMETER
     */
    public static final String FULLCONTEXT_PARAMETER = "FULLCONTEXT";
    
    /**
     * Name of the String in the RequestContext that is the context path of the application
     * at the Proxy.
     * Having this makes the development of path-independent JSPs much simpler. This
     * is a fully-qualified path from the root of the current host, not including the host.
     * @see #ORIGINAL_FULLCONTEXT_PARAMETER
     */
    public static final String ORIGINAL_CONTEXT_PARAMETER = "ORIGINAL_CONTEXT";

    /**
     * Name of the String in the RequestContext that is the context path of the application
     * at the Proxy.
     * Having this makes the development of path-independent JSPs much simpler. This
     * is a fully-qualified path including protocol, host and port.
     * @see #ORIGINAL_CONTEXT_PARAMETER
     */
    public static final String ORIGINAL_FULLCONTEXT_PARAMETER = "ORIGINAL_FULLCONTEXT";

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;
    
    /**
     * The default MeshBaseIdentifier, in String form,
     */
    protected String theDefaultMeshBaseIdentifier;
}
