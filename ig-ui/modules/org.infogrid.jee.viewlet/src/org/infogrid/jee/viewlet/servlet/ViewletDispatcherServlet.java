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

package org.infogrid.jee.viewlet.servlet;

import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.GenericServlet;
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
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateEnum;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateTransitionEnum;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.JeeViewletState;
import org.infogrid.jee.viewlet.JeeViewletStateTransition;
import org.infogrid.jee.viewlet.lidmetaformats.LidMetaFormatsViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TermMissingTraversalTranslatorException;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.rest.DefaultRestfulRequest;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
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
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        SaneRequest        saneRequest    = SaneServletRequest.create( servletRequest );
        StructuredResponse structured     = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        InfoGridWebApp app = InfoGridWebApp.getSingleton();
        Context        c   = (Context) saneRequest.getAttribute( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );
        if( c == null ) {
            c = app.getApplicationContext();
        }

        TraversalTranslator dict = c.findContextObject( TraversalTranslator.class ); // optional
        MeshBase            mb   = c.findContextObject( MeshBase.class );

        if( mb == null ) {
            throw new ContextMeshBaseNotFoundException();
        }

        @SuppressWarnings("unchecked")
        RestfulRequest restfulRequest = createRestfulRequest(
                saneRequest,
                mb.getIdentifier(),
                c );

        servletRequest.setAttribute( RestfulRequest.RESTFUL_REQUEST_ATTRIBUTE_NAME, restfulRequest );

        MeshObject        subject;
        MeshObjectsToView toView;
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

        } catch( ParseException ex ) {
            throw new ServletExceptionWithHttpStatusCode( ex, HttpServletResponse.SC_BAD_REQUEST ); // 400
        }

        JeeViewlet viewlet = null;
        if( subject != null ) {
            servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME, subject );

            if( saneRequest.matchUrlArgument( "lid-meta", "formats" )) {
                viewlet = LidMetaFormatsViewlet.create( mb, c );

            } else {
                ViewletFactory viewletFact = c.findContextObjectOrThrow( ViewletFactory.class );
                try {
                    viewlet = (JeeViewlet) viewletFact.obtainFor( toView, c );

                } catch( FactoryException ex ) {
                    throw new ServletException( ex ); // pass on
                }
            }
        }
        
        // create a stack of Viewlets and other request attributes
        JeeViewlet oldViewlet = (JeeViewlet) servletRequest.getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
        servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, viewlet );

        String oldState           = (String) request.getAttribute( JeeViewlet.VIEWLET_STATE_NAME );
        String oldStateTransition = (String) request.getAttribute( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME );

        if( toView != null ) {
            JeeViewletState state = (JeeViewletState) toView.getViewletParameter( JeeViewlet.VIEWLET_STATE_NAME );
            request.setAttribute( JeeViewlet.VIEWLET_STATE_NAME, state != null ? state.getName() : null ); // even set if null

            JeeViewletStateTransition transition = (JeeViewletStateTransition) toView.getViewletParameter( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME );
            request.setAttribute( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME, transition != null ? transition.getName() : null ); // even set if null
        }

        if( viewlet != null ) {
            synchronized( viewlet ) {
                Throwable thrown  = null;
                boolean   done    = false;
                try {
                    viewlet.view( toView );
                    if( SafeUnsafePostFilter.isSafePost( servletRequest ) ) {
                        done = viewlet.performBeforeSafePost( restfulRequest, structured );

                    } else if( SafeUnsafePostFilter.isUnsafePost( servletRequest ) ) {
                        done = viewlet.performBeforeUnsafePost( restfulRequest, structured );

                    } else if( SafeUnsafePostFilter.mayBeSafeOrUnsafePost( servletRequest ) ) {
                        done = viewlet.performBeforeMaybeSafeOrUnsafePost( restfulRequest, structured );

                    } else {
                        done = viewlet.performBeforeGet( restfulRequest, structured );
                    }

                    if( !done ) {
                        viewlet.processRequest( restfulRequest, toView, structured );
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
                    viewlet.performAfter( restfulRequest, structured, thrown );

                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME,        oldViewlet );
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_NAME,            oldState );
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME, oldStateTransition );
                }
            }
        }
    }

    /**
     * Construct a RestfulRequest object that is suitable to the URL-to-MeshObject mapping
     * applied by this application.
     *
     * @param lidRequest the incoming request
     * @param defaultMeshBaseIdentifier String form of the identifier of the default MeshBase
     * @param c the Context
     * @return the created RestfulRequest
     */
    protected RestfulRequest createRestfulRequest(
            SaneRequest        lidRequest,
            MeshBaseIdentifier defaultMeshBaseIdentifier,
            Context            c )
    {
        @SuppressWarnings("unchecked")
        DefaultRestfulRequest ret = DefaultRestfulRequest.create(
                lidRequest,
                defaultMeshBaseIdentifier,
                c.findContextObjectOrThrow( MeshBaseIdentifierFactory.class ),
                (MeshBaseNameServer<MeshBaseIdentifier,MeshBase>) c.findContextObjectOrThrow( MeshBaseNameServer.class ));
        return ret;
    }

    /**
     * Create a MeshObjectsToView object. This can be overridden by subclasses.
     * 
     * @param restful the incoming RESTful request
     * @param traversalDict the TraversalTranslator to use
     * @return the created MeshObjectsToView
     * @throws MeshObjectAccessException thrown if one or more MeshObjects could not be accessed
     * @throws CannotViewException thrown if a Viewlet could not view the requested MeshObjects
     * @throws ParseException thrown if a URI parsing error occurred
     * @throws NotPermittedException thrown if an attempted operation was not permitted
     */
    protected MeshObjectsToView createMeshObjectsToView(
            RestfulRequest       restful,
            TraversalTranslator  traversalDict )
        throws
            MeshObjectAccessException,
            CannotViewException,
            ParseException,
            NotPermittedException
    {
        MeshObject subject          = restful.determineRequestedMeshObject();
        String     viewletTypeName  = restful.getRequestedViewletTypeName();
        String []  traversalStrings = restful.getRequestedTraversalParameters();

        TraversalPath          path = null;
        TraversalSpecification spec = null;
        if( traversalDict != null && traversalStrings != null && traversalStrings.length > 0 ) {
            try {
                path = traversalDict.translateTraversalPath( subject, traversalStrings );

            } catch( TermMissingTraversalTranslatorException ex ) {
                // only a TraversalSpec given, not a TraversalPath
            } catch( TraversalTranslatorException ex ) {
                log.error( ex );
            }
            if( path != null ) {
                spec = path.getTraversalSpecification();
            } else {
                try {
                    spec = traversalDict.translateTraversalSpecification( subject, traversalStrings );

                } catch( TraversalTranslatorException ex ) {
                    log.error( ex );
                }
            }
        }

        if( subject == null ) {
            MeshObjectIdentifier subjectIdentifier = restful.determineRequestedMeshObjectIdentifier();
            throw new CannotViewException.NoSubject( subjectIdentifier );
        }

        Map<String,Object[]> viewletPars = determineViewletParameters( restful, traversalDict );

        MeshObjectsToView ret = MeshObjectsToView.create(
                subject,
                null,
                viewletTypeName,
                viewletPars,
                spec,
                path,
                restful );
        return ret;
    }

    /**
     * Determine the Viewlet parameters for the MeshObjectsToView.
     * Factored out to make overriding easier in subclasses.
     *
     * @param restful the incoming RESTful request
     * @param traversalDict the TraversalTranslator to use
     * @return the created Map, or null
     * @throws MeshObjectAccessException thrown if one or more MeshObjects could not be accessed
     * @throws ParseException thrown if a parsing problem occurred
     * @throws NotPermittedException thrown if an attempted operation was not permitted
     */
    protected Map<String,Object[]> determineViewletParameters(
            RestfulRequest       restful,
            TraversalTranslator  traversalDict )
        throws
            MeshObjectAccessException,
            ParseException,
            NotPermittedException
    {
        HashMap<String,Object[]> viewletPars = null;

        JeeViewletStateTransition transition = determineViewletStateTransition( restful );
        if( transition != null ) {
            if( viewletPars == null ) {
                viewletPars = new HashMap<String,Object[]>();
            }
            viewletPars.put( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME, new JeeViewletStateTransition[] { transition } );
        }

        // if there is a transition, it determines the state. If there is none, we try if we can tell the current state.
        JeeViewletState state = null;
        if( transition != null ) {
            state = transition.getNextState();
        }
        if( state == null ) {
            state = determineViewletState( restful );
        }
        if( state != null ) {
            if( viewletPars == null ) {
                viewletPars = new HashMap<String,Object[]>();
            }
            viewletPars.put( JeeViewlet.VIEWLET_STATE_NAME, new JeeViewletState[] { state } );
        }

        Map<String,String[]> otherPars = restful.getViewletParameters();
        if( otherPars != null ) {
            if( viewletPars == null ) {
                viewletPars = new HashMap<String,Object[]>();
            }
            viewletPars.putAll( otherPars );
        }

        return viewletPars;
    }

    /**
     * Overridable method to determine the current JeeViewletState from a request.
     *
     * @param restful the incoming RESTful request
     * @return the desired JeeViewletState
     */
    protected JeeViewletState determineViewletState(
            RestfulRequest restful )
    {
        JeeViewletState ret = DefaultJeeViewletStateEnum.fromRequest( restful );
        return ret;
    }

    /**
     * Overridable method to determine the desired JeeViewletStateTransition from a request.
     *
     * @param restful the incoming RESTful request
     * @return the desired JeeViewletState
     */
    protected JeeViewletStateTransition determineViewletStateTransition(
            RestfulRequest restful )
    {
        JeeViewletStateTransition ret = DefaultJeeViewletStateTransitionEnum.fromRequest( restful );
        return ret;
    }
}
