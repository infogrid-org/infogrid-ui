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

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactory;
import org.infogrid.viewlet.ViewletFactoryChoice;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.http.SaneRequestUtils;

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
        thePane     = ViewletAlternativeLinkTag.PANE_HERE;
        theRootPath = null;

        super.initializeToDefaults();
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
        RestfulRequest     restful        = (RestfulRequest) lookupOrThrow( RestfulRequest.RESTFUL_REQUEST_ATTRIBUTE_NAME );
        Viewlet            currentViewlet = (Viewlet) lookupOrThrow( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
        
        MeshObject subject = currentViewlet.getSubject();
        Context    c       = currentViewlet.getContext();
        
        ViewletFactory factory = c.findContextObjectOrThrow( ViewletFactory.class );
        
        MeshObjectsToView       toView     = MeshObjectsToView.create( subject, restful );
        ViewletFactoryChoice [] candidates = factory.determineFactoryChoicesOrderedByMatchQuality( toView );

        if( candidates.length > 0 ) {

            Integer nextId = (Integer) lookup( INSTANCE_ID_PAR_NAME );
            if( nextId == null ) {
                nextId = 1;
            }

            String nameInCss = getClass().getName().replace( '.', '-' );
            println( "<div class=\"" + nameInCss + "\" id=\"" + nameInCss + nextId + "\">" );
            print( "<h3><a href=\"javascript:toggle_viewlet_alternatives( '" + nameInCss + nextId + "' )\">" );
            print( theResourceHelper.getResourceString( "Title" ));
            println( "</a></h3>" );

            pageContext.getRequest().setAttribute( INSTANCE_ID_PAR_NAME, nextId + 1 );

            println( "<ul>" );

            for( int i=0 ; i<candidates.length ; ++i ) {
                print( "<li" );
                if( candidates[i].getName().equals( currentViewlet.getName() )) {
                    print( " class=\"selected\"" );
                }
                print( ">" );
                
                String text = ViewletAlternativeLinkTag.constructText(
                        currentViewlet,
                        candidates[i],
                        (RestfulJeeFormatter) theFormatter,
                        pageContext,
                        thePane,
                        theRootPath,
                        null,
                        null,
                        null,
                        null );
                print( text );
                print( candidates[i].getUserVisibleName() );
                print( "</a>" );
                println( "</li>" );
            }
            println( "</ul>" );
            println( "</div>" );

            String contextPath = restful.getSaneRequest().getContextPath();
            String classSlash  = getClass().getName().replace( '.' , '/' );

            StringBuilder js = new StringBuilder();
            js.append( "<script src=\"" );
            js.append( contextPath );
            js.append( "/v/" );
            js.append( classSlash );
            js.append( ".js" );
            js.append( "\" type=\"text/javascript\"></script>\n" );

            StringBuilder css = new StringBuilder();
            css.append( "<link rel=\"stylesheet\" href=\"" );
            css.append( contextPath );
            css.append( "/v/" );
            css.append( getClass().getName().replace( '.' , '/' ));
            css.append( ".css" );
            css.append( "\" />\n" );

            TextStructuredResponseSection headSection = theResponse.obtainTextSection( StructuredResponse.HTML_HEAD_SECTION );
            headSection.appendContent( js.toString() );
            headSection.appendContent( css.toString() );
        }

        return SKIP_BODY;
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
     * The HTTP path prepended to the HREF, e.g. http://example.com/foo/bar/?obj=
     */
    protected String theRootPath;

}
