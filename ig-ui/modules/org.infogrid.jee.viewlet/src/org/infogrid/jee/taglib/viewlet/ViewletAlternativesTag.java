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

package org.infogrid.jee.taglib.viewlet;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.viewlet.JeeMeshObjectsToViewFactory;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.JeeViewletFactoryChoice;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.http.SaneRequestUtils;
import org.infogrid.util.http.SaneUrl;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewletFactory;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Allows the user to select an alternate JeeViewlet to display the current subject.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ViewletAlternativesTag
    extends
        AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( ViewletAlternativesTag.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ViewletAlternativesTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        thePane                 = null;
        theTarget               = null;
        theStringRepresentation = null;

        super.initializeToDefaults();
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
        StructuredResponse theResponse    = (StructuredResponse) lookupOrThrow( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        JeeViewlet         currentViewlet = (JeeViewlet) lookupOrThrow( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
        
        MeshObject    subject       = currentViewlet.getSubject();
        TraversalPath arrivedAtPath = currentViewlet.getViewedMeshObjects().getArrivedAtPath();
        Context       c             = currentViewlet.getContext();
        SaneUrl       request       = currentViewlet.getViewedMeshObjects().getMeshObjectsToView().getRequest();

        JeeMeshObjectsToViewFactory toViewFact = c.findContextObjectOrThrow( JeeMeshObjectsToViewFactory.class );
        ViewletFactory              vlFact     = c.findContextObjectOrThrow( ViewletFactory.class );
        
        MeshObjectsToView toView;
        if( arrivedAtPath != null ) {
            toView = toViewFact.obtainFor( arrivedAtPath, request );
        } else {
            toView = toViewFact.obtainFor( subject, request ); // don't have a parent
        }
        ViewletFactoryChoice [] candidates = vlFact.determineFactoryChoicesOrderedByMatchQuality( toView );

        if( candidates.length > 0 ) {

            Integer nextId = (Integer) lookup( INSTANCE_ID_PAR_NAME );
            if( nextId == null ) {
                nextId = 1;
            }

            String divId = INSTANCE_ID_PAR_NAME + nextId;
            print( "<div class=\"" );
            print( getClass().getName().replace( ".", "-" ));
            print( "\" id=\"" );
            print( divId );
            println( "\">" );

            print( "<h3><a href=\"javascript:toggle_css_class( '" + divId + "', 'expanded' )\">" );
            print( theResourceHelper.getResourceString( "Title" ));
            println( "</a></h3>" );

            pageContext.getRequest().setAttribute( INSTANCE_ID_PAR_NAME, nextId + 1 );

            SaneRequest          saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );
            StringRepresentation rep         = getFormatter().determineStringRepresentation( theStringRepresentation );

            SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
            pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
            pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );
            pars.put( StringRepresentationParameters.LINK_TARGET_KEY,          theTarget );
            pars.put( JeeViewlet.PANE_STRING_REPRESENTATION_PARAMETER_KEY,     thePane );
            pars.put( JeeViewletFactoryChoice.VIEWEDMESHOBJECTS_STACK_KEY,     saneRequest.getAttribute( IncludeViewletTag.PARENT_STACK_ATTRIBUTE_NAME ));

            println( "<ul>" );
            for( int i=0 ; i<candidates.length ; ++i ) {
                print( "<li" );
                if( candidates[i].getName().equals( currentViewlet.getName() )) {
                    print( " class=\"selected\"" );
                }
                print( ">" );

                try {
                    String text = candidates[i].toStringRepresentationLinkStart( rep, pars );
                    print( text );

                    text = candidates[i].toStringRepresentation( rep, pars );
                    print( text );

                    text = candidates[i].toStringRepresentationLinkEnd( rep, pars );
                    print( text );

                } catch( StringifierException ex ) {
                    throw new JspException( ex );
                }

                println( "</li>" );
            }

            String contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();

            StringBuilder js = new StringBuilder();
            js.append( "<script src=\"" );
            js.append( contextPath );
            js.append( "/v/org/infogrid/jee/taglib/candy/ToggleCssClass.js\" type=\"text/javascript\"></script>\n" );

            StringBuilder css = new StringBuilder();
            css.append( "<link rel=\"stylesheet\" href=\"" );
            css.append( contextPath );
            css.append( "/v/" );
            css.append( getClass().getName().replace( '.' , '/' ));
            css.append( ".css" );
            css.append( "\" />\n" );

            TextStructuredResponseSection headSection = theResponse.obtainTextSection( StructuredResponse.HTML_HEAD_SECTION );
            if( !headSection.containsContent( css.toString() )) {
                headSection.appendContent( css.toString() );
            }
            if( !headSection.containsContent( js.toString() )) {
                headSection.appendContent( js.toString() );
            }
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Our implementation of doEndTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        println( "</ul>" );
        println( "</div>" );
        
        return EVAL_PAGE;
    }
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( ViewletAlternativesTag.class );

    /**
     * Identifies the attribute in the request context that counts up instances of this tag per request.
     */
    protected static final String INSTANCE_ID_PAR_NAME = SaneRequestUtils.classToAttributeName( ViewletAlternativesTag.class, "instanceid" );

    /**
     * Name of the pane.
     */
    protected String thePane;

    /**
     * The HTML frame to target, if any.
     */
    protected String theTarget;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
}
