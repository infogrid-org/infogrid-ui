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

package org.infogrid.jee.viewlet;

import java.io.IOException;
import java.util.Deque;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.taglib.viewlet.IncludeViewletTag;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.utils.JeeTemplateUtils;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.viewlet.AbstractViewlet;
import org.infogrid.viewlet.CannotViewException;

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
     * @param viewed the JeeViewedMeshObjects to use
     * @param c the application context
     */
    protected AbstractJeeViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
    }

    /**
      * Obtain the MeshObjects that this Viewlet is currently viewing, plus
      * context information. This method will return the same instance of ViewedMeshObjects
      * during the lifetime of the Viewlet.
      *
      * @return the ViewedMeshObjects
      */
    @Override
    public JeeViewedMeshObjects getViewedMeshObjects()
    {
        return (JeeViewedMeshObjects) super.getViewedMeshObjects();
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
     * The current JeeViewletState.
     *
     * @return the current state
     */
    public JeeViewletState getViewletState()
    {
        return getViewedMeshObjects().getViewletState();
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
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        // no op on this level
        return false;
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the SafeUnsafePostFilter determined that the incoming POST was safe.
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
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        return defaultPerformPost( request, response );
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the SafeUnsafePostFilter determined that the incoming POST was <b>not</b> safe.
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
            SaneRequest        request,
            StructuredResponse response )
        throws
            UnsafePostException,
            ServletException
    {
        throw new UnsafePostException( request );
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and no SafeUnsafePostFilter has been used.
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
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        return defaultPerformPost( request, response );
    }
    
    /**
     * Default implementation of what happens upon POST.
     *
     * @param request the incoming request
     * @param response the response to be assembled
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     */
    protected boolean defaultPerformPost(
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        if( !response.haveProblemsBeenReported() ) {
            // only if no errors have been reported
            response.setHttpResponseCode( 303 );

            String target = request.getUrlArgument( "lid-target" );
            if( target == null ) {
                target = request.getAbsoluteFullUri();
            }
            response.setLocation( target );
            return true;
        } else {
            return false;
        }
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
            SaneRequest        request,
            StructuredResponse response,
            Throwable          thrown )
        throws
            ServletException
    {
        // if no HTML title was set but it's a non-binary html response, set one

        if( response.isEmpty() ) {
            return;
        }
        TextStructuredResponseSection titleSection = response.obtainTextSection( StructuredResponse.HTML_TITLE_SECTION );
        if( titleSection.isEmpty() ) {
            InfoGridWebApp app = getContext().findContextObjectOrThrow( InfoGridWebApp.class );

            String name                     = getName();
            String userVisibleName          = getUserVisibleName();
            String subjectIdentifierString  = getSubject().getIdentifier().toExternalForm();
            String subjectUserVisibleString = getSubject().getUserVisibleString();
            String appName                  = app.getName();
            String appUserVisibleName       = app.getUserVisibleName();

            if( name == null ) {
                name = "";
            }
            if( userVisibleName == null ) {
                userVisibleName = "";
            }
            if( subjectUserVisibleString == null ) {
                subjectUserVisibleString = "";
            }

            String prefix;
            if( thrown == null && !response.haveProblemsBeenReportedAggregate() ) {
                prefix = "Default";
            } else {
                prefix = "Error";
            }
            String content;
            if( appName != null ) {

                if( appUserVisibleName == null ) {
                    appUserVisibleName = appName;
                }

                content = theResourceHelper.getResourceStringWithArguments(
                        prefix + "TitleWithApp",
                /* 0 */ name,
                /* 1 */ userVisibleName,
                /* 2 */ subjectIdentifierString,
                /* 3 */ subjectUserVisibleString,
                /* 4 */ appName,
                /* 5 */ appUserVisibleName );
            } else {
                content = theResourceHelper.getResourceStringWithArguments(
                        prefix + "TitleWithoutApp",
                /* 0 */ name,
                /* 1 */ userVisibleName,
                /* 2 */ subjectIdentifierString,
                /* 3 */ subjectUserVisibleString );
            }

            titleSection.setContent( content );
        }
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
            ret = theCurrentRequest.getAbsoluteFullUri();
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
            ret = theCurrentRequest.getAbsoluteBaseUri();
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Process the incoming request. Default implementation that can be
     * overridden by subclasses.
     * 
     * @param request the incoming request
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    public void processRequest(
            SaneRequest        request,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        synchronized( this ) {
            if( theCurrentRequest != null ) {
                throw new IllegalStateException( "Have current request already: " + theCurrentRequest );
            }
            theCurrentRequest = request;
        }
        
        try {
            String servletPath = getServletPath();

            if( servletPath != null ) {
                InfoGridWebApp app = getContext().findContextObjectOrThrow( InfoGridWebApp.class );

                RequestDispatcher dispatcher = app.findLocalizedRequestDispatcher(
                        servletPath,
                        request.acceptLanguageIterator(),
                        structured.getServletContext() );

                if( dispatcher != null ) {
                    JeeTemplateUtils.runRequestDispatcher( dispatcher, request, structured );

                } else {
                    throw new ServletException(
                            new CannotViewException.InternalError(
                                    this,
                                    getViewedMeshObjects().getMeshObjectsToView(),
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
     * Obtain the default URL to which forms should be HTTP POSTed.
     *
     * @return the URL
     */
    public String getPostUrl()
    {
        @SuppressWarnings("unchecked")
        Deque<JeeViewedMeshObjects> parentViewedStack = (Deque<JeeViewedMeshObjects>) theCurrentRequest.getAttribute( IncludeViewletTag.PARENT_STACK_ATTRIBUTE_NAME );

        JeeMeshObjectsToView currentlyToView = getViewedMeshObjects().getMeshObjectsToView();
        JeeMeshObjectsToView newToView       = currentlyToView.createCopy();

        return getPostUrl( parentViewedStack, newToView );
    }

    /**
     * Obtain the URL to which forms should be HTTP POSTed.
     * By default, that is the URL of the current Viewlet, but in the "top" pane rather than the "here" pane,
     * concatenated with a lid-target argument with the value of the URL of the current Viewlet, in the "here" pane.
     * The Viewlet state is also set to VIEW.
     * This can be overridden by subclasses.
     *
     * @param viewedMeshObjectsStack the Stack of ViewedMeshObjects of the parent Viewlets, if any
     * @param toView the MeshObjectsToView upon post
     * @return the URL
     */
    public String getPostUrl(
            Deque<JeeViewedMeshObjects> viewedMeshObjectsStack,
            JeeMeshObjectsToView        toView )
    {
        toView.setViewletState( DefaultJeeViewletStateEnum.VIEW );

        StringBuilder buf = new StringBuilder();
        buf.append( toView.getAsUrl( (Deque<JeeViewedMeshObjects>) null ));
        if( viewedMeshObjectsStack != null && !viewedMeshObjectsStack.isEmpty() ) {
            HTTP.replaceOrAppendArgumentToUrl( buf, "lid-target", toView.getAsUrl( viewedMeshObjectsStack ));
        }
        if( toView.getViewletTypeName() != null ) {
            HTTP.replaceOrAppendArgumentToUrl( buf, JeeMeshObjectsToView.LID_FORMAT_ARGUMENT_NAME, JeeMeshObjectsToView.VIEWLET_PREFIX + toView.getViewletTypeName() );
        }
        if( toView.getMimeType() != null ) {
            HTTP.replaceOrAppendArgumentToUrl( buf, JeeMeshObjectsToView.LID_FORMAT_ARGUMENT_NAME, JeeMeshObjectsToView.MIME_PREFIX + toView.getMimeType() );
        }
        return buf.toString();
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
        
        String mime = getViewedMeshObjects().getMimeType();
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
    protected SaneRequest theCurrentRequest;

    /**
     * The ResourceHelper for this class.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( AbstractJeeViewlet.class );
}
