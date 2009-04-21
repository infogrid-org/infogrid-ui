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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.viewlet;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateEnum;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;

/**
 * Container for a JeeViewlet.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ViewletTag
        extends
            TagSupport
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ViewletTag()
    {
        theFormatter = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( RestfulJeeFormatter.class );
    }

    /**
     * Do the start tag operation.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag()
        throws
            JspException
    {
        JeeViewlet vl = (JeeViewlet) pageContext.getRequest().getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );

        StringBuilder content = new StringBuilder( 256 );

        if( !DefaultJeeViewletStateEnum.VIEW.getName().equals( vl.getViewletState().getName() )) { // compare Strings, enum's won't allow equals() override
            content.append( "<form method=\"POST\" action=\"" );
            content.append( vl.getPostUrl() );
            content.append( "\">\n" );
//
//            MeshObject subject = vl.getSubject();
//            if( subject != null ) {
//                content.append( " <input type=\"hidden\" name=\"shell.subject\" value=\"" );
//                content.append( vl.getSubject().getIdentifier().toExternalForm() );
//                content.append( "\" />\n" );
//            }
        }
        content.append( "<div class=\"viewlet" );

        if( vl != null ) {
            String vlHtmlClass = vl.getHtmlClass();

            content.append( " " ).append( vlHtmlClass.replace( '.', '-') );
        }        
        content.append( "\">" );
        theFormatter.println( pageContext, false, content.toString() );

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Do the end tag operation.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public int doEndTag()
        throws
            JspException
    {
        JeeViewlet vl = (JeeViewlet) pageContext.getRequest().getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );

        theFormatter.println( pageContext, false, "</div>" );

        if( !DefaultJeeViewletStateEnum.VIEW.getName().equals( vl.getViewletState().getName() )) { // compare Strings, enum's won't allow equals() override
            theFormatter.println( pageContext, false, "</form>" );
        }

        return EVAL_PAGE;
    }
    
    /**
     * The formatter to use.
     */
    protected RestfulJeeFormatter theFormatter;
}
