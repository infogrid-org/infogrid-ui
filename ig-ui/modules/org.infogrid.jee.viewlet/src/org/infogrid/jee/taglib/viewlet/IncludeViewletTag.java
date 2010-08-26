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
import java.net.MalformedURLException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
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
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.TextStructuredResponseSectionTemplate;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.jee.viewlet.JeeMeshObjectsToViewFactory;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.lidmetaformats.LidMetaFormatsViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.ContextDirectory;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.http.SaneUrl;
import org.infogrid.util.http.SimpleSaneUrl;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactory;

/**
 * <p>Include another Viewlet.</p>
 *
 * <p>There are two stacks of objects in the request context:</p>
 * <ol>
 *  <li>The TO-INCLUDE-STACK contains the MeshObjectsToView in the user's incoming request URL; elements get taken off
 *      the stack each time we encounter an IncludeViewletTag</li>
 *  <li>The PARENT-STACK contains the ViewedMeshObjects of the Viewlet invoking this IncludeViewletTag (and its parents etc.);
 *      elements are put on the stack each time we encounter an IncludeViewletTag.</li>
 * </ol>
 * 
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
        MeshBase mb = c.findContextObjectOrThrow( MeshBase.class );

        saneRequest.setAttribute( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME, c );

        @SuppressWarnings("unchecked")
        Deque<MeshObjectsToView> toIncludeStack = (Deque<MeshObjectsToView>) saneRequest.getAttribute( TO_INCLUDE_STACK_ATTRIBUTE_NAME );
        MeshObjectsToView        toView;

        try {
            toView = toIncludeStack.pop();

        } catch( NoSuchElementException ex ) {
            // not given in the URL, use default
            toView = null;
        }

        if( toView == null ) {
            JeeMeshObjectsToViewFactory toViewFactory = c.findContextObjectOrThrow( JeeMeshObjectsToViewFactory.class );

            TraversalPath reachedBy = (TraversalPath) lookupOrThrow( theReachedByName );
            toView = toViewFactory.obtainFor( reachedBy, theViewletTypeName );
        }
        
        MeshObject subject = toView.getSubject();

        StructuredResponse outerStructured = (StructuredResponse) saneRequest.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        StructuredResponse innerStructured = StructuredResponse.create( (HttpServletResponse) pageContext.getResponse(), pageContext.getServletContext() );
        servletRequest.setAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME, innerStructured );

        JeeViewlet viewlet = null;
        if( toView != null ) {
            if( saneRequest.matchUrlArgument( "lid-meta", "formats" )) {
                viewlet = LidMetaFormatsViewlet.create( mb, c );

            } else {
                ViewletFactory viewletFact = c.findContextObjectOrThrow( ViewletFactory.class );
                try {
                    viewlet = (JeeViewlet) viewletFact.obtainFor( toView );

                } catch( FactoryException ex ) {
                    throw new JspException( ex ); // pass on
                }
            }
        }

        if( viewlet != null ) {
            synchronized( viewlet ) {
                // create a stack of Viewlets and other request attributes
                JeeViewlet    oldViewlet = (JeeViewlet) servletRequest.getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
                TraversalPath oldPath    = (TraversalPath) servletRequest.getAttribute( TRAVERSAL_PATH_ATTRIBUTE_NAME );

                @SuppressWarnings("unchecked")
                Deque<JeeViewedMeshObjects> parentStack = (Deque<JeeViewedMeshObjects>) saneRequest.getAttribute( PARENT_STACK_ATTRIBUTE_NAME );
                if( parentStack == null ) {
                    parentStack = new ArrayDeque<JeeViewedMeshObjects>();
                    saneRequest.setAttribute( PARENT_STACK_ATTRIBUTE_NAME, parentStack );
                }

                Throwable thrown  = null;
                try {
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, viewlet );
                    servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME, subject );
                    parentStack.push( oldViewlet.getViewedMeshObjects() );

                    viewlet.view( toView );
                    if( SafeUnsafePostFilter.isSafePost( servletRequest ) ) {
                        viewlet.performBeforeSafePost( saneRequest, innerStructured );

                    } else if( SafeUnsafePostFilter.isUnsafePost( servletRequest ) ) {
                        viewlet.performBeforeUnsafePost( saneRequest, innerStructured );

                    } else if( SafeUnsafePostFilter.mayBeSafeOrUnsafePost( servletRequest ) ) {
                        viewlet.performBeforeMaybeSafeOrUnsafePost( saneRequest, innerStructured );

                    } else {
                        viewlet.performBeforeGet( saneRequest, innerStructured );
                    }

                    viewlet.processRequest( saneRequest, innerStructured );

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
                    throw new JspException( thrown ); // notice the finally block

                } catch( IOException t ) {
                    thrown = t;
                    throw (IOException) thrown; // notice the finally block

                } finally {
                    try {
                        viewlet.performAfter( saneRequest, innerStructured, thrown );

                    } catch( Throwable ex2 ) {
                        log.error( ex2 );
                    }

                    // restore context
                    parentStack.pop();
                    servletRequest.setAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME, oldViewlet );
                    servletRequest.setAttribute( JeeViewlet.SUBJECT_ATTRIBUTE_NAME, oldViewlet.getSubject() );
                    servletRequest.setAttribute( TRAVERSAL_PATH_ATTRIBUTE_NAME,     oldPath );
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
     * Helper method to parse a SaneRequest into a Stack of MeshObjectsToView. This is invoked by the ViewletDispatcher.
     *
     * @param request the incoming request
     * @param toViewFactory the factory for MeshObjectsToView objects
     * @return the Stack of MeshObjectsToView, the outermost at the top
     * @throws FactoryException thrown if the request could not be parsed
     * @throws MalformedURLException thrown if a contained URL could not be parsed
     */
    public static Deque<MeshObjectsToView> determineMeshObjectsToViewStack(
            SaneRequest                 request,
            JeeMeshObjectsToViewFactory toViewFactory )
        throws
            FactoryException,
            MalformedURLException
    {
        ArrayDeque<MeshObjectsToView> ret = new ArrayDeque<MeshObjectsToView>();
        String originalAbsoluteContext = request.getOriginalSaneRequest().getAbsoluteContextUri();

        SaneUrl current = request;
        while( current != null ) {
            MeshObjectsToView found = toViewFactory.obtainFor( current );

            ret.addLast( found );

            String included = current.getUrlArgument( INCLUDE_URL_ARGUMENT_NAME );
            if( included != null ) {
                current = SimpleSaneUrl.create( included, originalAbsoluteContext );
            } else {
                current = null;
            }
        }
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

    /**
     * Name of the request attribute that contains the stack of MeshObjectsToView still to include.
     */
    public static final String TO_INCLUDE_STACK_ATTRIBUTE_NAME = SaneServletRequest.classToAttributeName( IncludeViewletTag.class, "ToIncludeStack" );

    /**
     * Name of the request attribute that contains the stack of MeshObjectsToView already included.
     */
    public static final String PARENT_STACK_ATTRIBUTE_NAME = SaneServletRequest.classToAttributeName( IncludeViewletTag.class, "ParentStack" );
}
