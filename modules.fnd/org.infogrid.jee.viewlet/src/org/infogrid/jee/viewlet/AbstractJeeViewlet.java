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

package org.infogrid.jee.viewlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.utils.JeeTemplateUtils;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.AbstractViewlet;

/**
 * Factors out commonly used functionality for JeeViewlets.
 */
public abstract class AbstractJeeViewlet
        extends
            AbstractViewlet
        implements
            JeeViewlet
{
    private static final Log log = Log.getLogInstance( AbstractJeeViewlet.class ); // our own, private loger

    /**
     * Constructor, for subclasses only.
     * 
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected AbstractJeeViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }
    
    /**
     * Obtain the Html class name for this Viewlet that will be used for the enclosing <tt>div</tt> tag.
     * By default, it is the Java class name, having replaced all periods with hyphens.
     * 
     * @return the HTML class name
     */
    public String getHtmlClass()
    {
        String ret = getClass().getName();

        ret = ret.replaceAll( "\\.", "-" );
        
        return ret;
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the GET method has been requested.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the GET execution of the servlet.</p>
     * <p>Subclasses will often override this.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeSafePost
     * @see #performBeforeUnsafePost
     * @see #performAfter
     */
    public void performBeforeGet(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        // no op on this level
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet, e.g. the evaluation of POST commands.</p>
     * <p>Subclasses will often override this.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeUnsafePost
     * @see #performAfter
     */
    public void performBeforeSafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        // no op on this level
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was <b>not</b> safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet.</p>
     * <p>It is strongly recommended that JeeViewlets do not regularly process the incoming
     *    POST data, as the request is likely unsafe (e.g. a Cross-Site Request Forgery).</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws UnsafePostException thrown if the unsafe POST operation was not acceptable
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performAfter
     */
    public void performBeforeUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            UnsafePostException,
            ServletException
    {
        throw new UnsafePostException( request.getSaneRequest() );
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and no FormTokenService has been used.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet.</p>
     * <p>It is strongly recommended that JeeViewlets do not regularly process the incoming
     *    POST data, as the request is likely unsafe (e.g. a Cross-Site Request Forgery).</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performAfter
     */
    public void performBeforeMaybeSafeOrUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        // no op on this level
    }
    
    /**
     * <p>Invoked after to the execution of the Servlet. It is the hook by which
     * the JeeViewlet can perform whatever operations needed after to the execution of the servlet, e.g.
     * logging. Subclasses will often override this.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @param thrown if this is non-null, it is the Throwable indicating a problem that occurred
     *        either during execution of performBefore or of the servlet.
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performBeforeUnsafePost
     */
    public void performAfter(
            RestfulRequest     request,
            StructuredResponse response,
            Throwable          thrown )
        throws
            ServletException
    {
        // noop on this level
    }

    /**
     * Obtain the path to the Servlet for this JeeViewlet. JeeViewlet may implement this in different ways,
     * such as be returning a path to a JSP. This returns the generic path, not a localized version.
     * 
     * @return the Servlet path
     */
    public String getServletPath()
    {
        String ret = constructDefaultDispatcherUrl( getClass() );
        return ret;
    }

    /**
     * Obtain the full URI of the incoming request.
     * 
     * @return the full URI, as String
     */
    public String getRequestURI()
    {
        String ret;
        if( theCurrentRequest != null ) {
            ret = theCurrentRequest.getSaneRequest().getAbsoluteFullUri();
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Process the incoming RestfulRequest. Default implementation that can be
     * overridden by subclasses.
     * 
     * @param restful the incoming RestfulRequest
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    public void processRequest(
            RestfulRequest     restful,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        synchronized( this ) {
            if( theCurrentRequest != null ) {
                throw new IllegalStateException( "Have current request already: " + theCurrentRequest );
            }
            theCurrentRequest = restful;
        }
        
        try {
            String servletPath = getServletPath();

            if( servletPath != null ) {
                InfoGridWebApp app = InfoGridWebApp.getSingleton();

                RequestDispatcher dispatcher = app.findLocalizedRequestDispatcher(
                        servletPath,
                        restful.getSaneRequest().acceptLanguageIterator(),
                        structured.getServletContext() );

                if( dispatcher != null ) {
                    JeeTemplateUtils.runRequestDispatcher( dispatcher, restful.getSaneRequest(), structured );
                } // FIXME? Should there be an else here, throwing an Exception?
            }

        } finally {
            synchronized( this ) {
                theCurrentRequest = null;
            }
        }        
    }

    /**
     * Obtain the URL to which forms should be HTTP POSTed. This
     * can be overridden by subclasses.
     *
     * @return the URL
     */
    public String getPostUrl()
    {
        String ret = getRequestURI();
        
        // append lid-xpath
        String xpath = theCurrentRequest.getSaneRequest().getArgument( RestfulRequest.XPATH_PREFIX );
        if( xpath != null ) {
            ret = HTTP.appendArgumentToUrl( ret, RestfulRequest.XPATH_PREFIX, xpath );
        }
        // append lid-format
        String format = theCurrentRequest.getSaneRequest().getArgument( RestfulRequest.LID_FORMAT_PARAMETER_NAME );
        if( format != null ) {
            ret = HTTP.appendArgumentToUrl( ret, RestfulRequest.LID_FORMAT_PARAMETER_NAME, format );
        }
        return ret;
    }


    /**
     * This method converts the name of a Class (subclass of this one) into the default request URL
     * for the RequestDispatcher.
     * 
     * @param viewletClassName the class name of the JeeViewlet
     * @return the JSP URL.
     */
    protected String constructDefaultDispatcherUrl(
            String viewletClassName )
    {
        StringBuilder almost = new StringBuilder();
        almost.append( "/v/" );
        almost.append( viewletClassName.replace( '.', '/' ));
        
        String mime = theCurrentRequest.getRequestedMimeType();
        if( mime != null && mime.length() > 0 ) {
            // FIXME: does not handle * parameters right now
            almost.append( "/" );
            almost.append( mime );
            almost.append( "/" );
            
            int lastDot = viewletClassName.lastIndexOf( '.' );
            if( lastDot > 0 ) {
                almost.append( viewletClassName.substring( lastDot+1 ));
            } else {
                almost.append( viewletClassName );
            }
        }
        almost.append( ".jsp" );
        return almost.toString();
    }

    /**
     * This method converts a Class (subclass of this one) into the default request URL
     * for the RequestDispatcher.
     * 
     * @param viewletClass the class of the JeeViewlet
     * @return the JSP URL.
     */
    protected String constructDefaultDispatcherUrl(
            Class viewletClass )
    {
        return constructDefaultDispatcherUrl( viewletClass.getName() );
    }

    /**
     * The request currently being processed.
     */
    protected RestfulRequest theCurrentRequest;
}
