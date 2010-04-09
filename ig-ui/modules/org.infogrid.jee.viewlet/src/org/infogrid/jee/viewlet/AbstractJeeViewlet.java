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

package org.infogrid.jee.viewlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.jee.templates.utils.JeeTemplateUtils;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.AbstractViewlet;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;

/**
 * Factors out commonly used functionality for JeeViewlets.
 */
public abstract class AbstractJeeViewlet
        extends
            AbstractViewlet
        implements
            JeeViewlet
{
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
      * The Viewlet is being instructed to view certain objects, which are packaged as MeshObjectsToView.
      *
      * @param toView the MeshObjects to view
      * @throws CannotViewException thrown if this Viewlet cannot view these MeshObjects
      */
    @Override
    public void view(
            MeshObjectsToView toView )
        throws
            CannotViewException
    {
        super.view( toView );

        theViewletState           = (JeeViewletState)           toView.getViewletParameter( VIEWLET_STATE_NAME );
        theViewletStateTransition = (JeeViewletStateTransition) toView.getViewletParameter( VIEWLET_STATE_TRANSITION_NAME );
    }

    /**
     * Obtain the current state of the Viewlet.
     *
     * @return the current state of the Viewlet, if any
     */
    public JeeViewletState getViewletState()
    {
        return theViewletState;
    }

    /**
     * Obtain all possible states of this Viewlet. This may depend on the current MeshObjectsToView
     * (e.g. whether the user may edit a MeshObject or not).
     *
     * @return the possible ViewletStates
     */
    public JeeViewletState [] getPossibleViewletStates()
    {
        // FIXME: should take MeshObject access rights into account
        return DefaultJeeViewletStateEnum.values();
    }

    /**
     * Obtain the desired next state of the Viewlet.
     *
     * @return the desired next state of the Viewlet, if any
     */
    public JeeViewletState getNextViewletState()
    {
        JeeViewletStateTransition trans = getViewletStateTransition();
        if( trans != null ) {
            return trans.getNextState();
        } else {
            return null;
        }
    }

    /**
     * Obtain the desired transition from the current state of the Viewlet.
     *
     * @return the desired transition from the current state of the Viewlet, if any
     */
    public JeeViewletStateTransition getViewletStateTransition()
    {
        return theViewletStateTransition;
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
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeSafePost
     * @see #performBeforeUnsafePost
     * @see #performAfter
     */
    public boolean performBeforeGet(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        // no op on this level
        return false;
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
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeUnsafePost
     * @see #performAfter
     */
    public boolean performBeforeSafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        response.setHttpResponseCode( 303 );
        response.setLocation( request.getSaneRequest().getAbsoluteFullUri() );
        return true;
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
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws UnsafePostException thrown if the unsafe POST operation was not acceptable
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performAfter
     */
    public boolean performBeforeUnsafePost(
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
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performAfter
     */
    public boolean performBeforeMaybeSafeOrUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        response.setHttpResponseCode( 303 );
        response.setLocation( request.getSaneRequest().getAbsoluteFullUri() );
        return true;
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
    public String getFullRequestURI()
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
     * Obtain the base URI of the incoming request.
     *
     * @return the base URI, as String
     */
    public String getBaseRequestURI()
    {
        String ret;
        if( theCurrentRequest != null ) {
            ret = theCurrentRequest.getSaneRequest().getAbsoluteBaseUri();
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
     * @param toView the MeshObjectsToView, mostly for error reporting
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    public void processRequest(
            RestfulRequest     restful,
            MeshObjectsToView  toView,
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

                } else {
                    throw new ServletException(
                            new CannotViewException.InternalError(
                                    this,
                                    toView,
                                    "Cannot find RequestDispatcher at " + servletPath,
                                    null ));
                }
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
        String      ret  = getBaseRequestURI();
        SaneRequest sane = theCurrentRequest.getSaneRequest();
        
        String [] traversal = sane.getMultivaluedUrlArgument( RestfulRequest.LID_TRAVERSAL_PARAMETER_NAME );
        if( traversal != null ) {
            for( int i=0 ; i<traversal.length ; ++i ) {
                ret = HTTP.appendArgumentToUrl( ret, RestfulRequest.LID_TRAVERSAL_PARAMETER_NAME, traversal[i] );
            }
        }
        String [] format = sane.getMultivaluedUrlArgument( RestfulRequest.LID_FORMAT_PARAMETER_NAME );
        if( format != null ) {
            for( int i=0 ; i<format.length ; ++i ) {
                ret = HTTP.appendArgumentToUrl( ret, RestfulRequest.LID_FORMAT_PARAMETER_NAME, format[i] );
            }
        }
        String [] appContext = sane.getMultivaluedUrlArgument( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );
        if( appContext != null ) {
            for( int i=0 ; i<appContext.length ; ++i ) {
                ret = HTTP.appendArgumentToUrl( ret, TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME, appContext[i] );
            }
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

    /**
     * The current JeeViewletState.
     */
    protected JeeViewletState theViewletState;

    /**
     * The desired transition from the current JeeViewletState.
     */
    protected JeeViewletStateTransition theViewletStateTransition;
}
