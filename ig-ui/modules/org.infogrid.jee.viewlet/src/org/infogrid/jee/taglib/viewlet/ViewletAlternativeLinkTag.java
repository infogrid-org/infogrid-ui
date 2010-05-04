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

import java.util.Deque;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringifierException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * <p>Tag that links / hyperlinks to a MeshObject.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ViewletAlternativeLinkTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( ViewletAlternativeLinkTag.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ViewletAlternativeLinkTag()
    {
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theViewletAlternativeName = null;
        thePane                   = PANE_HERE;
        theRootPath               = null;
        theAddArguments           = null;
        theTarget                 = null;
        theTitle                  = null;
        theStringRepresentation   = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the viewletAlternativeName property.
     *
     * @return value of the viewletAlternativeName property
     * @see #setViewletAlternativeName
     */
    public String getViewletAlternativeName()
    {
        return theViewletAlternativeName;
    }

    /**
     * Set value of the viewletAlternativeName property.
     *
     * @param newValue new value of the viewletAlternativeName property
     * @see #getViewletAlternativeName
     */
    public void setViewletAlternativeName(
            String newValue )
    {
        theViewletAlternativeName = newValue;
    }

    /**
     * Obtain value of the pane property.
     *
     * @return value of the pane property
     * @see #setPane
     */
    public String getPane()
    {
        return thePane;
    }

    /**
     * Set value of the pane property.
     *
     * @param newValue new value of the pane property
     * @see #getPane
     */
    public void setPane(
            String newValue )
    {
        thePane = newValue;
    }

    /**
     * Obtain value of the rootPath property.
     *
     * @return value of the rootPath property
     * @see #setRootPath
     */
    public String getRootPath()
    {
        return theRootPath;
    }

    /**
     * Set value of the rootPath property.
     *
     * @param newValue new value of the rootPath property
     * @see #getRootPath
     */
    public void setRootPath(
            String newValue )
    {
        theRootPath = newValue;
    }

    /**
     * Obtain value of the addArguments property.
     *
     * @return value of the addArguments property
     * @see #setAddArguments
     */
    public String getAddArguments()
    {
        return theAddArguments;
    }

    /**
     * Set value of the addArguments property.
     *
     * @param newValue new value of the addArguments property
     * @see #getAddArguments
     */
    public void setAddArguments(
            String newValue )
    {
        theAddArguments = newValue;
    }

    /**
     * Obtain value of the target property.
     *
     * @return value of the target property
     * @see #setTarget
     */
    public String getTarget()
    {
        return theTarget;
    }

    /**
     * Set value of the target property.
     *
     * @param newValue new value of the target property
     * @see #getTarget
     */
    public void setTarget(
            String newValue )
    {
        theTarget = newValue;
    }

    /**
     * Obtain value of the title property.
     *
     * @return value of the title property
     * @see #setTitle
     */
    public String getTitle()
    {
        return theTitle;
    }

    /**
     * Set value of the title property.
     *
     * @param newValue new value of the title property
     * @see #getTitle
     */
    public void setTitle(
            String newValue )
    {
        theTitle = newValue;
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        Viewlet              viewlet = (Viewlet) lookupOrThrow( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
        ViewletFactoryChoice choice  = (ViewletFactoryChoice) lookupOrThrow( theViewletAlternativeName );

        if( viewlet != null ) { // may happen if ignore="true"
            String text = constructText(
                        viewlet,
                        choice,
                        (RestfulJeeFormatter) theFormatter,
                        pageContext,
                        thePane,
                        theRootPath,
                        theTarget,
                        theTitle,
                        theAddArguments,
                        theStringRepresentation );

            print( text );

            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Construct the correct text. This is a static method to the ViewletAlternativesTag
     * can also invoke it.
     *
     * @param viewlet the enclosing Viewlet
     * @param choice the ViewletFactoryChoice
     * @param formatter the RestfulJeeFormatter to use
     * @param pageContext the current PageContext
     * @param pane the pane attribute
     * @param rootPath the root path, if any
     * @param target the target attribute for the link, if any
     * @param title the title attribute for the link, if any
     * @param addArguments any manually specified additional arguments
     * @param stringRepresentation the StringRepresentation to use
     * @return the text to be printed
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected static String constructText(
            Viewlet              viewlet,
            ViewletFactoryChoice choice,
            RestfulJeeFormatter  formatter,
            PageContext          pageContext,
            String               pane,
            String               rootPath,
            String               target,
            String               title,
            String               addArguments,
            String               stringRepresentation )
        throws
            JspException,
            IgnoreException
    {
        String ret;
        if( PANE_HERE.equals( pane )) {
            ret = constructTextForHerePane(
                    viewlet,
                    choice,
                    formatter,
                    pageContext,
                    rootPath,
                    target,
                    title,
                    addArguments,
                    stringRepresentation );

        } else if( PANE_TOP.equals( pane )) {
            ret = constructTextForTopPane(
                    viewlet,
                    choice,
                    formatter,
                    pageContext,
                    rootPath,
                    target,
                    title,
                    addArguments,
                    stringRepresentation );

        } else {
            throw new JspException( "Unknown value for pane attribute: " + pane );
        }
        return ret;
    }

    /**
     * Construct the text to be printed in case the pane attribute is set to PANE_HERE.
     *
     * @param viewlet the enclosing Viewlet
     * @param choice the ViewletFactoryChoice
     * @param formatter the RestfulJeeFormatter to use
     * @param pageContext the current PageContext
     * @param rootPath the root path, if any
     * @param target the target attribute for the link, if any
     * @param title the title attribute for the link, if any
     * @param addArguments any manually specified additional arguments
     * @param stringRepresentation the StringRepresentation to use
     * @return the text to be printed
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected static String constructTextForHerePane(
            Viewlet              viewlet,
            ViewletFactoryChoice choice,
            RestfulJeeFormatter  formatter,
            PageContext          pageContext,
            String               rootPath,
            String               target,
            String               title,
            String               addArguments,
            String               stringRepresentation )
        throws
            JspException,
            IgnoreException
    {
        @SuppressWarnings("unchecked")
        Deque<Viewlet> viewletStack = (Deque<Viewlet>) pageContext.getRequest().getAttribute( JeeViewlet.VIEWLET_STACK_ATTRIBUTE_NAME );

        if( viewletStack == null || viewletStack.isEmpty() ) {
            return constructTextForTopPane( viewlet, choice, formatter, pageContext, rootPath, target, title, addArguments, stringRepresentation );
        }

        String delegateString;
        try {
            delegateString = formatter.formatMeshObjectLinkStart(
                    pageContext,
                    viewlet.getSubject(),
                    rootPath,
                    RestfulRequest.LID_FORMAT_PARAMETER_NAME + "=" + RestfulRequest.VIEWLET_PREFIX + choice.getName(),
                    null,
                    null,
                    "Url" );
        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        Viewlet topViewlet = viewletStack.getFirst();

        TraversalPath myTraversalPath = (TraversalPath) pageContext.getRequest().getAttribute( IncludeViewletTag.TRAVERSAL_PATH_ATTRIBUTE_NAME );

        String topAddArguments = constructAddArguments(
                topViewlet.getSubject(),
                myTraversalPath,
                topViewlet.getTraversalSpecification(),
                topViewlet.getName(),
                IncludeViewletTag.INCLUDE_URL_ARGUMENT_NAME + "=" + HTTP.encodeToValidUrlArgument( delegateString ) );
                // IncludeViewletTag.INCLUDE_URL_ARGUMENT_NAME + "=" + HTTP.encodeToValidUrlArgument( buf.toString() ) );

        try {
            String text = formatter.formatMeshObjectLinkStart(
                    pageContext,
                    topViewlet.getSubject(),
                    rootPath,
                    topAddArguments,
                    target,
                    title,
                    stringRepresentation );
            return text;

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }
    }

    /**
     * Construct the text to be printed in case the pane attribute is set to PANE_TOP.
     *
     * @param viewlet the enclosing Viewlet
     * @param choice the ViewletFactoryChoice
     * @param formatter the RestfulJeeFormatter to use
     * @param pageContext the current PageContext
     * @param rootPath the root path, if any
     * @param target the target attribute for the link, if any
     * @param title the title attribute for the link, if any
     * @param addArguments any manually specified additional arguments
     * @param stringRepresentation the StringRepresentation to use
     * @return the text to be printed
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected static String constructTextForTopPane(
            Viewlet              viewlet,
            ViewletFactoryChoice choice,
            RestfulJeeFormatter  formatter,
            PageContext          pageContext,
            String               rootPath,
            String               target,
            String               title,
            String               addArguments,
            String               stringRepresentation )
        throws
            JspException,
            IgnoreException
    {
        String realAddArguments = constructAddArguments(
                viewlet.getSubject(),
                null,
                viewlet.getViewedObjects().getTraversalSpecification(),
                choice.getName(),
                addArguments );

        try {
            String text = formatter.formatMeshObjectLinkStart(
                    pageContext,
                    viewlet.getSubject(),
                    rootPath,
                    realAddArguments,
                    target,
                    title,
                    stringRepresentation );
            return text;

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }
    }

    /**
     * Helper method.
     *
     * @param obj the MeshObject for which to construct the URL arguments
     * @param path the TraversalPath, if any
     * @param spec the TraversalSpecification, if any
     * @param viewletType the desired type of Viewlet, if any
     * @param addArguments any manually specified additional arguments
     * @return the constructed additional arguments
     */
    protected static String constructAddArguments(
            MeshObject    obj,
            TraversalPath path,
            TraversalSpecification spec,
            String        viewletType,
            String        addArguments )
    {
        StringBuilder ret = new StringBuilder();
        String        sep = "";

        if( viewletType != null ) {
            ret.append( sep );
            ret.append( RestfulRequest.LID_FORMAT_PARAMETER_NAME );
            ret.append( '=' );
            ret.append( RestfulRequest.VIEWLET_PREFIX );
            ret.append( HTTP.encodeToValidUrlArgument( viewletType ));
            sep = "&";
        }

        String [] traversalArgs = null;
        try {
            TraversalTranslator translator
                    = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( 
                            TraversalTranslator.class );

            if( path != null ) {
                traversalArgs = translator.translateTraversalPath( obj, path );
            } else if( spec != null ) {
                traversalArgs = translator.translateTraversalSpecification( obj, spec );
            }

        } catch( TraversalTranslatorException ex ) {
            log.error( ex );
        }
        if( traversalArgs != null && traversalArgs.length > 0 ) {
            for( int i=0 ; i<traversalArgs.length ; ++i ) {
                ret.append( sep );
                ret.append( RestfulRequest.LID_TRAVERSAL_PARAMETER_NAME ).append( "=" ).append( HTTP.encodeToValidUrlArgument( traversalArgs[i] ));
                sep = "&";
            }
        }

        if( addArguments != null ) {
            ret.append( sep );
            ret.append( addArguments );
            sep = "&";
        }

        if( ret.length() > 0 ) {
            return ret.toString();
        } else {
            return null;
        }
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException
    {
        print( "</a>" );

        return EVAL_PAGE;
    }

    /**
     * Name of the bean that holds the ViewletFactoryChoice
     */
    protected String theViewletAlternativeName;

    /**
     * Name of the pane.
     */
    protected String thePane;

    /**
     * Values for the pane attribute:
     */
    public static final String PANE_HERE = "here"; // default
    public static final String PANE_TOP  = "top";

    /**
     * The HTTP path prepended to the HREF, e.g. http://example.com/foo/bar/?obj=
     */
    protected String theRootPath;

    /**
     * The arguments to append to the URL, separated by &.
     */
    protected String theAddArguments;

    /**
     * The HTML frame to target, if any.
     */
    protected String theTarget;

    /**
     * The title of the link, if any.
     */
    protected String theTitle;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
}
