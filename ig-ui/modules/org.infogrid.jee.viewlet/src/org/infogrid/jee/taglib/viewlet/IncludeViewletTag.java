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

package org.infogrid.jee.taglib.viewlet;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.sane.OverridingSaneServletRequest;
import org.infogrid.jee.security.SafeUnsafePostFilter;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.TextStructuredResponseSectionTemplate;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateEnum;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateTransitionEnum;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.JeeViewletState;
import org.infogrid.jee.viewlet.JeeViewletStateTransition;
import org.infogrid.jee.viewlet.lidmetaformats.LidMetaFormatsViewlet;
import org.infogrid.jee.viewlet.servlet.ContextMeshBaseNotFoundException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.rest.ComposedRestfulRequest;
import org.infogrid.rest.DefaultRestfulRequest;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.ContextDirectory;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Include another Viewlet.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class IncludeViewletTag
        extends
            AbstractInfoGridTag
{
    private static final Log  log              = Log.getLogInstance( IncludeViewletTag.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IncludeViewletTag()
    {
        // nothing
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theSubject         = null;
        theReachedByName   = null;
        theMimeType        = null;
        theViewletTypeName = null;
        theRequestContext  = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the subject property.
     *
     * @return value of the subject property
     * @see #setSubject
     */
    public final MeshObject getSubject()
    {
        return theSubject;
    }

    /**
     * Set value of the subject property.
     *
     * @param newValue new value of the subject property
     * @see #getSubject
     */
    public final void setSubject(
            MeshObject newValue )
    {
        theSubject = newValue;
    }

    /**
     * Obtain value of the reachedByName property.
     *
     * @return value of the reachedByName property
     * @see #setSubject
     */
    public final String getReachedByName()
    {
        return theReachedByName;
    }

    /**
     * Set value of the reachedByName property.
     *
     * @param newValue new value of the reachedByName property
     * @see #getSubject
     */
    public final void setReachedByName(
            String newValue )
    {
        theReachedByName = newValue;
    }

    /**
     * Obtain value of the mimeType property.
     *
     * @return value of the mimeType property
     * @see #setMimeType
     */
    public final String getMimeType()
    {
        return theMimeType;
    }

    /**
     * Set value of the mimeType property.
     *
     * @param newValue new value of the mimeType property
     * @see #getMimeType
     */
    public final void setMimeType(
            String newValue )
    {
        theMimeType = newValue;
    }

    /**
     * Obtain value of the viewletTypeName property.
     *
     * @return value of the viewletTypeName property
     * @see #setViewletTypeName
     */
    public final String getViewletTypeName()
    {
        return theViewletTypeName;
    }

    /**
     * Set value of the viewletTypeName property.
     *
     * @param newValue new value of the viewletTypeName property
     * @see #getViewletTypeName
     */
    public final void setViewletTypeName(
            String newValue )
    {
        theViewletTypeName = newValue;
    }

    /**
     * Obtain value of the requestContext property.
     *
     * @return value of the requestContext property
     * @see #setRequestContext
     */
    public final String getRequestContext()
    {
        return theRequestContext;
    }

    /**
     * Set value of the requestContext property.
     *
     * @param newValue new value of the requestContext property
     * @see #getRequestContext
     */
    public final void setRequestContext(
            String newValue )
    {
        theRequestContext = newValue;
    }

    /**
     * Do the start tag operation.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        // This all seems to be a bit of a mess, but I can't think of making it simpler right now. (FIXME?)
        
        HttpServletRequest servletRequest = (HttpServletRequest) pageContext.getRequest();
        SaneServletRequest saneRequest    = SaneServletRequest.create( servletRequest );

        InfoGridWebApp app = InfoGridWebApp.getSingleton();
        Context c          = (Context) saneRequest.getAttribute( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );
        if( c == null && theRequestContext != null ) {
            ContextDirectory dir = app.getContextDirectory();
            c = dir.getContext( theRequestContext );
        }
        if( c == null ) {
            c = app.getApplicationContext();
        }

        saneRequest.setAttribute( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME, c );

        TraversalTranslator dict = c.findContextObject( TraversalTranslator.class ); // optional
        MeshBase            mb   = c.findContextObject( MeshBase.class );

        if( mb == null ) {
            throw new ContextMeshBaseNotFoundException();
        }

        MeshObject     subject;
        TraversalPath  path;
        RestfulRequest restfulRequest = null; // make compiler happy

        if( theSubject != null ) {
            if( theReachedByName != null ) {
                throw new JspException( "Specify either but not both attributes: subject and reachedByName" );
            }
            subject = theSubject;
            path    = null;

            restfulRequest = ComposedRestfulRequest.create(
                    saneRequest,
                    mb.getIdentifier(),
                    null,
                    subject,
                    subject.getIdentifier(),
                    theMimeType,
                    theViewletTypeName );

        } else {
            @SuppressWarnings("unchecked")
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> ns = c.findContextObject( MeshBaseNameServer.class );

            String [] includeUrls = saneRequest.getMultivaluedUrlArgument( INCLUDE_URL_ARGUMENT_NAME );

            // if there's no reachedBy, use includeUrls
            // if there's a reachedBy, try to find the right includeUrls

            if( theReachedByName != null ) {
                path    = (TraversalPath) lookupOrThrow( theReachedByName );
                subject = path.getLastMeshObject();

                if( includeUrls == null || includeUrls.length == 0 ) {
                    restfulRequest = ComposedRestfulRequest.create(
                            saneRequest,
                            mb.getIdentifier(),
                            null,
                            subject,
                            subject.getIdentifier(),
                            theMimeType,
                            theViewletTypeName );

                } else {
                    URL appUrl = new URL( saneRequest.getAbsoluteContextUri());
                    for( int i=0 ; i<includeUrls.length ; ++i ) {
                        URL u = new URL( appUrl, includeUrls[i] );

                        restfulRequest = DefaultRestfulRequest.create(
                                OverridingSaneServletRequest.create( u, "GET", saneRequest ),
                                mb.getIdentifier(),
                                ns );
                        try {
                            if( subject.getIdentifier().equals( restfulRequest.determineRequestedMeshObjectIdentifier() )) {
                                break;
                            }
                        } catch( ParseException ex ) {
                            log.error( ex );
                        }
                        restfulRequest = null;
                    }
                }
            } else {
                path    = null;
                subject = null;

                if( includeUrls != null && includeUrls.length == 1 ) {
                    restfulRequest = DefaultRestfulRequest.create(
                            OverridingSaneServletRequest.create( new URL( includeUrls[0] ), "GET", saneRequest ),
                            mb.getIdentifier(),
                            ns );
                }
            }

            if( restfulRequest != null ) {

                try {
                    subject = restfulRequest.determineRequestedMeshObject();
                    
                } catch( MeshObjectAccessException ex ) {
                    throw new JspException( ex );
                } catch( NotPermittedException ex ) {
                    throw new JspException( ex );
                } catch( ParseException ex ) {
                    throw new JspException( ex );
                }
                if( subject == null ) {
                    throw new JspException( "Cannot determine subject of included Viewlet" );
                }
            } else {
                log.error( "Inconsistent request: ", saneRequest );
                return SKIP_BODY;
            }
        }

        StructuredResponse outerStructured = (StructuredResponse) saneRequest.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        StructuredResponse innerStructured = StructuredResponse.create( (HttpServletResponse) pageContext.getResponse(), pageContext.getServletContext() );
        servletRequest.setAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME, innerStructured );

        servletRequest.setAttribute( RestfulRequest.RESTFUL_REQUEST_ATTRIBUTE_NAME, restfulRequest );

        MeshObjectsToView toView;
        try {
            toView  = createMeshObjectsToView( restfulRequest, dict );

        } catch( MeshObjectAccessException ex ) {
            throw new JspException( ex );

        } catch( NotPermittedException ex ) {
            throw new JspException( ex );

        } catch( CannotViewException.NoSubject ex ) {
            throw new JspException( ex );

        } catch( CannotViewException ex ) {
            throw new JspException( ex );

        } catch( ParseException ex ) {
            throw new JspException( ex );
        }

        JeeViewlet viewlet = null;
        if( subject != null ) {
            if( saneRequest.matchUrlArgument( "lid-meta", "formats" )) {
                viewlet = LidMetaFormatsViewlet.create( mb, c );

            } else {
                ViewletFactory viewletFact = c.findContextObjectOrThrow( ViewletFactory.class );
                try {
                    viewlet = (JeeViewlet) viewletFact.obtainFor( toView, c );

                } catch( FactoryException ex ) {
                    throw new JspException( ex ); // pass on
                }
            }
        }
        if( viewlet != null ) {
            Viewlet enclosingViewlet = (Viewlet) lookup( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
            if( enclosingViewlet != null && enclosingViewlet.getName().equals( viewlet.getName() )) {
                if( subject == enclosingViewlet.getSubject() ) {
                    throw new JspException( "Cannot include Viewlet " + viewlet.getName() + " in itself with same Subject: will lead to infinite recursion" );
                }
            }
        }

        if( viewlet != null ) {
            synchronized( viewlet ) {
                // create a stack of Viewlets and other request attributes
                JeeViewlet    oldViewlet = (JeeViewlet) servletRequest.getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
                TraversalPath oldPath    = (TraversalPath) servletRequest.getAttribute( TRAVERSAL_PATH_ATTRIBUTE_NAME );

                @SuppressWarnings("unchecked")
                ArrayDeque<Viewlet> viewletStack = (ArrayDeque<Viewlet>) servletRequest.getAttribute( JeeViewlet.VIEWLET_STACK_ATTRIBUTE_NAME );
                viewletStack.push( oldViewlet );

                Throwable thrown  = null;
                try {
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, viewlet );
                    servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME, subject );
                    servletRequest.setAttribute( TRAVERSAL_PATH_ATTRIBUTE_NAME,     path );

                    if( toView != null ) {
                        JeeViewletState state = (JeeViewletState) toView.getViewletParameter( JeeViewlet.VIEWLET_STATE_NAME );
                        servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_NAME, state != null ? state.getName() : null ); // even set if null

                        JeeViewletStateTransition transition = (JeeViewletStateTransition) toView.getViewletParameter( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME );
                        servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME, transition != null ? transition.getName() : null ); // even set if null
                    }

                    viewlet.view( toView );
                    if( SafeUnsafePostFilter.isSafePost( servletRequest ) ) {
                        viewlet.performBeforeSafePost( restfulRequest, innerStructured );

                    } else if( SafeUnsafePostFilter.isUnsafePost( servletRequest ) ) {
                        viewlet.performBeforeUnsafePost( restfulRequest, innerStructured );

                    } else if( SafeUnsafePostFilter.mayBeSafeOrUnsafePost( servletRequest ) ) {
                        viewlet.performBeforeMaybeSafeOrUnsafePost( restfulRequest, innerStructured );

                    } else {
                        viewlet.performBeforeGet( restfulRequest, innerStructured );
                    }

                    viewlet.processRequest( restfulRequest, toView, innerStructured );

                } catch( RuntimeException t ) {
                    thrown = t;
                    throw (RuntimeException) thrown; // notice the finally block

                } catch( CannotViewException t ) {
                    thrown = t;
                    throw new JspException( thrown ); // notice the finally block

                } catch( UnsafePostException t ) {
                    thrown = t;
                    throw new JspException( thrown ); // notice the finally block

                } catch( ServletException t ) {
                    thrown = t;
                    throw (JspException) thrown; // notice the finally block

                } catch( IOException t ) {
                    thrown = t;
                    throw (IOException) thrown; // notice the finally block

                } finally {
                    try {
                        viewlet.performAfter( restfulRequest, innerStructured, thrown );

                    } catch( Throwable ex2 ) {
                        log.error( ex2 );
                    }

                    // restore context
                    
                    viewletStack.pop();

                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME,        oldViewlet );
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_NAME,            oldViewlet.getViewletState() );
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME, oldViewlet.getViewedObjects().getViewletParameter( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME) );
                    servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME       , oldViewlet.getSubject() );
                    servletRequest.setAttribute( TRAVERSAL_PATH_ATTRIBUTE_NAME,            oldPath );
                }
            }
        }

        if( !innerStructured.isEmpty() ) {
            Iterator<TextStructuredResponseSectionTemplate> iter = innerStructured.textSectionTemplateIterator();
            while( iter.hasNext() ) {
                TextStructuredResponseSectionTemplate template = iter.next();
                TextStructuredResponseSection there = innerStructured.getTextSection( template );
                TextStructuredResponseSection here  = outerStructured.obtainTextSection( template );

                if( StructuredResponse.TEXT_DEFAULT_SECTION.equals( template )) {
                    // inline main section
                    JspWriter w = pageContext.getOut();
                    w.print( there.getContent() );

                } else {
                    // copy non-main sections
                    here.appendContent( there.getContent() );
                }

                // pass on errors
                List<Throwable> problems = there.problems();
                if( problems != null ) {
                    for( Throwable t : problems ) {
                        here.reportProblem( t );
                    }
                }
            }
        }

        return EVAL_PAGE;
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

        TraversalSpecification traversal = null;
        if( traversalDict != null && traversalStrings != null && traversalStrings.length > 0 ) {
            try {
                traversal = traversalDict.translateTraversalSpecification( subject, traversalStrings );

            } catch( TraversalTranslatorException ex ) {
                log.error( ex );
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
                traversal,
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

    /**
     * The subject of the included Viewlet.
     */
    protected MeshObject theSubject;

    /**
     * The name of the request attribute containing the TraversalPath that led to the Subject of
     * the included Viewlet.
     */
    protected String theReachedByName;

    /**
     * The requested MIME type.
     */
    protected String theMimeType;

    /**
     * The type of Viewlet to include, if any.
     */
    protected String theViewletTypeName;

    /**
     * The request context.
     */
    protected String theRequestContext;

    /**
     * Name of the request attribute that contains the TraversalPath.
     */
    public static final String TRAVERSAL_PATH_ATTRIBUTE_NAME = "traversal-path";

    /**
     * Name of the URL argument that contains the URL for the included viewlet.
     */
    public static final String INCLUDE_URL_ARGUMENT_NAME = "lid-include";
}
