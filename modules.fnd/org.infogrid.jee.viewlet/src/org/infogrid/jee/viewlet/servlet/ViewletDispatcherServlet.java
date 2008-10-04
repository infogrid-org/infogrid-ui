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

package org.infogrid.jee.viewlet.servlet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.ServletExceptionWithHttpStatusCode;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.DefaultRestfulRequest;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.SafeUnsafePostFilter;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TraversalDictionary;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactory;

/**
 * <p>Main JeeViewlet dispatcher to determine the REST subject, the best JeeViewlet, and
 *    the best available localization.</p>
 * </p> This may be subclassed by applications.</p>
 */
public class ViewletDispatcherServlet
        extends
            GenericServlet
{
    private static final Log  log              = Log.getLogInstance( ViewletDispatcherServlet.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Main servlet method.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    public final void service(
            ServletRequest  request,
            ServletResponse response )
        throws
            ServletException,
            IOException
    {
        HttpServletRequest  servletRequest = (HttpServletRequest) request;
        SaneServletRequest  saneRequest    = (SaneServletRequest) request.getAttribute( SaneServletRequest.SANE_SERVLET_REQUEST_ATTRIBUTE_NAME  );
        StructuredResponse  structured     = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        InfoGridWebApp      app     = InfoGridWebApp.getSingleton();
        Context             c       = app.getApplicationContext();
        TraversalDictionary dict    = c.findContextObject( TraversalDictionary.class ); // optional

        RestfulRequest restfulRequest = createRestfulRequest(
                saneRequest,
                servletRequest.getContextPath(),
                c.findContextObjectOrThrow( MeshBase.class ).getIdentifier().toExternalForm() );

        servletRequest.setAttribute( RestfulRequest.RESTFUL_REQUEST_ATTRIBUTE_NAME, restfulRequest );

        MeshObject          subject;
        MeshObjectsToView   toView;

        try {
            subject = restfulRequest.determineRequestedMeshObject();
            toView  = createMeshObjectsToView( restfulRequest, dict );

        } catch( MeshObjectAccessException ex ) {
            throw new ServletExceptionWithHttpStatusCode( ex, HttpServletResponse.SC_NOT_FOUND ); // 404

        } catch( NotPermittedException ex ) {
            throw new ServletExceptionWithHttpStatusCode( ex, HttpServletResponse.SC_FORBIDDEN ); // 402

        } catch( CannotViewException.NoSubject ex ) {
            throw new ServletExceptionWithHttpStatusCode( ex, HttpServletResponse.SC_NOT_FOUND ); // 404

        } catch( CannotViewException ex ) {
            throw new ServletExceptionWithHttpStatusCode( ex, HttpServletResponse.SC_BAD_REQUEST ); // 400

        } catch( URISyntaxException ex ) {
            throw new ServletExceptionWithHttpStatusCode( ex, HttpServletResponse.SC_BAD_REQUEST ); // 400
        }

        JeeViewlet viewlet = null;

        if( subject != null ) {
            servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME, subject );

            ViewletFactory viewletFact = c.findContextObjectOrThrow( ViewletFactory.class );
            try {
                viewlet = (JeeViewlet) viewletFact.obtainFor( toView, c );

            } catch( CannotViewException ex ) {
                throw new ServletException( ex ); // pass on

            } catch( FactoryException ex ) {
                log.info( ex );
            }
        }

        if( viewlet != null ) {
            // create a stack of Viewlets
            JeeViewlet oldViewlet = (JeeViewlet) servletRequest.getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
            servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, viewlet );

            synchronized( viewlet ) {
                Throwable thrown  = null;
                try {
                    viewlet.view( toView );
                    if( SafeUnsafePostFilter.isSafePost( servletRequest ) ) {
                        viewlet.performBeforeSafePost( restfulRequest, structured );

                    } else if( SafeUnsafePostFilter.isUnsafePost( servletRequest ) ) {
                        viewlet.performBeforeUnsafePost( restfulRequest, structured );
                        
                    } else if( SafeUnsafePostFilter.mayBeSafeOrUnsafePost( servletRequest ) ) {
                        viewlet.performBeforeMaybeSafeOrUnsafePost( restfulRequest, structured );
                        
                    } else {
                        viewlet.performBeforeGet( restfulRequest, structured );
                    }

                    viewlet.processRequest( restfulRequest, structured );

                } catch( RuntimeException t ) {
                    thrown = t;
                    throw (RuntimeException) thrown; // notice the finally block

                } catch( CannotViewException t ) {
                    thrown = t;
                    throw new ServletException( thrown ); // notice the finally block

                } catch( UnsafePostException t ) {
                    thrown = t;
                    throw new ServletException( thrown ); // notice the finally block

                } catch( ServletException t ) {
                    thrown = t;
                    throw (ServletException) thrown; // notice the finally block

                } catch( IOException t ) {
                    thrown = t;
                    throw (IOException) thrown; // notice the finally block

                } finally {
                    viewlet.performAfter( restfulRequest, structured, thrown );

                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, oldViewlet );
                }
            }

        } else if( viewlet != null ) {
            throw new ServletException( new CannotViewException.InvalidViewlet( viewlet, toView ));

        } else {
            throw new ServletException( new CannotViewException.NoViewletFound( toView ));
        }
    }

    /**
     * Construct a RestfulRequest object that is suitable to the URL-to-MeshObject mapping
     * applied by this application.
     *
     * @param lidRequest the incoming request
     * @param context the context path of the application
     * @param defaultMeshBaseIdentifier String form of the identifier of the default MeshBase
     * @return the created RestfulRequest
     */
    protected RestfulRequest createRestfulRequest(
            SaneServletRequest lidRequest,
            String             context,
            String             defaultMeshBaseIdentifier )
    {
        DefaultRestfulRequest ret = DefaultRestfulRequest.create(
                lidRequest,
                context,
                defaultMeshBaseIdentifier );
        return ret;
    }

    /**
     * Create a MeshObjectsToView object. This can be overridden by subclasses.
     * 
     * @param restful the incoming RESTful request
     * @param traversalDict the TraversalDictionary to use
     * @return the created MeshObjectsToView
     * @throws MeshObjectAccessException thrown if one or more MeshObjects could not be accessed
     * @throws CannotViewException thrown if a Viewlet could not view the requested MeshObjects
     * @throws URISyntaxException thrown if a URI parsing error occurred
     * @throws NotPermittedException thrown if an attempted operation was not permitted
     */
    protected MeshObjectsToView createMeshObjectsToView(
            RestfulRequest       restful,
            TraversalDictionary  traversalDict )
        throws
            MeshObjectAccessException,
            CannotViewException,
            URISyntaxException,
            NotPermittedException
    {
        MeshObject           subject           = restful.determineRequestedMeshObject();
        MeshObjectIdentifier subjectIdentifier = restful.determineRequestedMeshObjectIdentifier();
        String               viewletClassName  = restful.getRequestedViewletClassName();
        String               traversalString   = restful.getRequestedTraversal();

        if( subject == null ) {
            throw new CannotViewException.NoSubject( subjectIdentifier );
        }

        TraversalSpecification traversal   = ( traversalDict != null ) ? traversalDict.translate( subject, traversalString ) : null;
        Map<String,Object>     viewletPars = null;
        
        MeshObjectsToView ret = MeshObjectsToView.create(
                subject,
                null,
                viewletClassName,
                viewletPars,
                traversal );
        return ret;
    }
}
