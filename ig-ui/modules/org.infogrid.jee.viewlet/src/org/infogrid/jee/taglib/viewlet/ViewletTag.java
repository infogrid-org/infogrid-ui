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
import javax.servlet.jsp.tagext.TagSupport;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateEnum;
import org.infogrid.jee.viewlet.JeeViewlet;

/**
 * Container for a JeeViewlet.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ViewletTag
        extends
            AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ViewletTag()
    {
        // nothing
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theFormId   = null;
        theCssClass = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the formId property.
     *
     * @return value of the formId property
     * @see #setFormId
     */
    public final String getFormId()
    {
        return theFormId;
    }

    /**
     * Set value of the formId property.
     *
     * @param newValue new value of the formId property
     * @see #getFormId
     */
    public final void setFormId(
            String newValue )
    {
        theFormId = newValue;
    }

    /**
     * Obtain value of the cssClass property.
     *
     * @return value of the cssClass property
     * @see #setCssClass
     */
    public final String getCssClass()
    {
        return theCssClass;
    }

    /**
     * Set value of the cssClass property.
     *
     * @param newValue new value of the cssClass property
     * @see #getCssClass
     */
    public final void setCssClass(
            String newValue )
    {
        theCssClass = newValue;
    }

    /**
     * Do the start tag operation.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int realDoStartTag()
        throws
            JspException
    {
        JeeViewlet vl = (JeeViewlet) pageContext.getRequest().getAttribute( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );

        StringBuilder content = new StringBuilder( 256 );

        if( !DefaultJeeViewletStateEnum.VIEW.getName().equals( vl.getViewletState().getName() )) { // compare Strings, enum's won't allow equals() override
            content.append( "<form method=\"POST\" action=\"" );
            content.append( vl.getPostUrl() );
            if( theFormId != null && theFormId.length() > 0 ) {
                content.append( "\" id=\"" );
                content.append( theFormId );
            }
            content.append( "\" enctype=\"multipart/form-data\" accept-charset=\"" ); // use the more general form
            content.append( SaneServletRequest.FORM_CHARSET ); // use the more general form
            content.append( "\">\n" ); // use the more general form

        }
        content.append( "<div class=\"viewlet" );

        String vlHtmlClass = vl.getHtmlClass();
        if( vlHtmlClass != null ) {
            content.append( " " ).append( vlHtmlClass.replace( '.', '-') );
        }
        if( theCssClass != null ) {
            content.append( " " ).append( theCssClass );
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
    public int realDoEndTag()
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
     * The value of the id element of the generated HTML form, if any, if the form is generated.
     */
    protected String theFormId;

    /**
     * Additional HTML/CSS class attribute, if any.
     */
    protected String theCssClass;
}
