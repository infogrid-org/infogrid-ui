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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Deque;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.ServletExceptionWithHttpStatusCode;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.SafeUnsafePostFilter;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.servlet.AbstractInfoGridServlet;
import org.infogrid.jee.taglib.viewlet.IncludeViewletTag;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.jee.viewlet.JeeMeshObjectsToViewFactory;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.lidmetaformats.LidMetaFormatsViewlet;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
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
            AbstractInfoGridServlet
{
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
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        SaneRequest        saneRequest    = SaneServletRequest.create( servletRequest );
        StructuredResponse structured     = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        InfoGridWebApp app = getInfoGridWebApp();
        Context        c   = (Context) saneRequest.getAttribute( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );
        if( c == null ) {
            c = app.getApplicationContext();
        }

        MeshBase mb = c.findContextObject( MeshBase.class );
        if( mb == null ) {
            throw new ContextMeshBaseNotFoundException();
        }

        JeeMeshObjectsToViewFactory toViewFactory = c.findContextObjectOrThrow( JeeMeshObjectsToViewFactory.class );

        Deque<MeshObjectsToView> toViewStack = null; // make compiler happy
        try {
            toViewStack = IncludeViewletTag.determineMeshObjectsToViewStack( saneRequest, toViewFactory );
            
        } catch( FactoryException ex ) {
            handleMeshObjectsToViewFactoryException( ex );

        } catch( MalformedURLException ex ) {
            handleMeshObjectsToViewFactoryException( ex );
        }

        request.setAttribute( IncludeViewletTag.TO_INCLUDE_STACK_ATTRIBUTE_NAME, toViewStack );
        
        MeshObjectsToView toView = toViewStack.pop();

        JeeViewlet viewlet = null;
        if( toView != null ) {
            servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME, toView.getSubject() );

            if( saneRequest.matchUrlArgument( "lid-meta", "formats" )) {
                viewlet = LidMetaFormatsViewlet.create( mb, c );

            } else {
                ViewletFactory viewletFact = c.findContextObjectOrThrow( ViewletFactory.class );
                try {
                    viewlet = (JeeViewlet) viewletFact.obtainFor( toView );

                } catch( FactoryException ex ) {
                    throw new ServletException( ex ); // pass on
                }
            }
            servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, viewlet );
        }

        if( viewlet != null ) {
            synchronized( viewlet ) {
                Throwable thrown  = null;
                boolean   done    = false;
                try {
                    viewlet.view( toView );
                    if( SafeUnsafePostFilter.isSafePost( servletRequest ) ) {
                        done = viewlet.performBeforeSafePost( saneRequest, structured );

                    } else if( SafeUnsafePostFilter.isUnsafePost( servletRequest ) ) {
                        done = viewlet.performBeforeUnsafePost( saneRequest, structured );

                    } else if( SafeUnsafePostFilter.mayBeSafeOrUnsafePost( servletRequest ) ) {
                        done = viewlet.performBeforeMaybeSafeOrUnsafePost( saneRequest, structured );

                    } else {
                        done = viewlet.performBeforeGet( saneRequest, structured );
                    }

                    if( !done ) {
                        viewlet.processRequest( saneRequest, structured );
                    }

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
                    viewlet.performAfter( saneRequest, structured, thrown );
                }
            }
        }
    }

    /**
     * Handle exceptions thrown when attempting to create a MeshObjectsToView. This method is
     * factored out so subclasses can easily override.
     * 
     * @param t the thrown exception
     */
    protected void handleMeshObjectsToViewFactoryException(
            Throwable t )
        throws
            ServletException
    {
        if( t instanceof MalformedURLException ) {
            throw new ServletException( t );
        }

        Throwable cause = t.getCause();

        if( cause instanceof CannotViewException.NoSubject ) {
            throw new ServletExceptionWithHttpStatusCode( cause, HttpServletResponse.SC_NOT_FOUND ); // 404

        } else if( cause instanceof MeshObjectAccessException ) {
            throw new ServletExceptionWithHttpStatusCode( cause, HttpServletResponse.SC_NOT_FOUND ); // 404

        } else if( cause instanceof NotPermittedException ) {
            throw new ServletExceptionWithHttpStatusCode( cause, HttpServletResponse.SC_FORBIDDEN ); // 402

        } else {
            throw new ServletExceptionWithHttpStatusCode( cause, HttpServletResponse.SC_BAD_REQUEST ); // 400
        }
    }
}
