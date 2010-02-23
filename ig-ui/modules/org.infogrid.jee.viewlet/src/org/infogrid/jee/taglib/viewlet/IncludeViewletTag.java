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
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.SafeUnsafePostFilter;
import org.infogrid.jee.security.UnsafePostException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
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
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.rest.ComposedRestfulRequest;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.ContextDirectory;
import org.infogrid.util.http.SaneRequest;
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
     * @throws JspException thrown if a processing error occurred
     * @throws IOException thrown if an I/O Exception occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int realDoStartTag()
        throws
            JspException,
            IOException
    {
        // This all seems to be a bit of a mess, but I can't think of making it simpler right now. (FIXME?)
        
        HttpServletRequest servletRequest = (HttpServletRequest) pageContext.getRequest();
        SaneRequest        saneRequest    = SaneServletRequest.create( servletRequest );

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

        StructuredResponse outerStructured = (StructuredResponse) saneRequest.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        StructuredResponse innerStructured = StructuredResponse.create( (HttpServletResponse) pageContext.getResponse(), pageContext.getServletContext() );
        servletRequest.setAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME, innerStructured );

        @SuppressWarnings("unchecked")
        RestfulRequest restfulRequest = ComposedRestfulRequest.create(
                saneRequest,
                mb.getIdentifier(),
                null,
                theSubject,
                theSubject.getIdentifier(),
                theMimeType,
                theViewletTypeName );

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
        if( theSubject != null ) {
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
                throw new JspException( "Cannot include Viewlet of same name " + viewlet.getName() + " in itself: will lead to infinite recursion" );
            }
        }

        if( viewlet != null ) {
            synchronized( viewlet ) {
                    // create a stack of Viewlets and other request attributes
                JeeViewlet oldViewlet         = (JeeViewlet) servletRequest.getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
                MeshObject oldSubject         = (MeshObject) servletRequest.getAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME );
                String     oldState           = (String) servletRequest.getAttribute( JeeViewlet.VIEWLET_STATE_NAME );
                String     oldStateTransition = (String) servletRequest.getAttribute( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME );
                
                Throwable thrown  = null;
                try {
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, viewlet );
                    servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME, theSubject );

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

                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME,        oldViewlet );
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_NAME,            oldState );
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_STATE_TRANSITION_NAME, oldStateTransition );
                    servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME       , oldSubject );
                }
            }
        }

        if( !innerStructured.isEmpty() ) {
            Iterator<TextStructuredResponseSectionTemplate> iter = innerStructured.textSectionTemplateIterator();
            while( iter.hasNext() ) {
                TextStructuredResponseSectionTemplate template = iter.next();
                TextStructuredResponseSection there = innerStructured.getTextSection( template );

                if( StructuredResponse.TEXT_DEFAULT_SECTION.equals( template )) {
                    // inline main section
                    JspWriter w = pageContext.getOut();
                    w.print( there.getContent() );

                } else {
                    // copy non-main sections
                    TextStructuredResponseSection here  = outerStructured.obtainTextSection( template );
                    here.appendContent( there.getContent() );
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
}
