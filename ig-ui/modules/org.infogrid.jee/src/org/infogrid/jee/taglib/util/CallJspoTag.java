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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.util;

import java.io.IOException;
import java.util.Map.Entry;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.candy.OverlayTag;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.ResourceHelper;

/**
 * <p>Allows the inclusion of JSP overlays as subroutines with parameters.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class CallJspoTag
    extends
        AbstractInfoGridBodyTag

{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public CallJspoTag()
    {
        // noop
    }

    /**
     * Release all of our resources.
     */
    @Override
    protected void initializeToDefaults()
    {
        thePage          = null;
        theLinkTitle     = null;
        theAction        = null;
        theSubmitLabel   = null;
        theOldCallRecord = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the page property.
     *
     * @return value of the page property
     * @see #setPage
     */
    public String getPage()
    {
        return thePage;
    }

    /**
     * Set value of the page property.
     *
     * @param newValue new value of the page property
     * @see #getPage
     */
    public void setPage(
            String newValue )
    {
        thePage = newValue;
    }

    /**
     * Obtain value of the title property.
     *
     * @return value of the title property
     * @see #setLinkTitle
     */
    public String getLinkTitle()
    {
        return theLinkTitle;
    }

    /**
     * Set value of the title property.
     *
     * @param newValue new value of the title property
     * @see #getLinkTitle
     */
    public void setLinkTitle(
            String newValue )
    {
        theLinkTitle = newValue;
    }

    /**
     * Obtain value of the action property.
     *
     * @return value of the action property
     * @see #setAction
     */
    public String getAction()
    {
        return theAction;
    }

    /**
     * Set value of the action property.
     *
     * @param newValue new value of the action property
     * @see #getAction
     */
    public void setAction(
            String newValue )
    {
        theAction = newValue;
    }

    /**
     * Obtain value of the submitLabel property.
     *
     * @return value of the submitLabel property
     * @see #setSubmitLabel
     */
    public String getSubmitLabel()
    {
        return theSubmitLabel;
    }

    /**
     * Set value of the submitLabel property.
     *
     * @param newValue new value of the submitLabel property
     * @see #getSubmitLabel
     */
    public void setSubmitLabel(
            String newValue )
    {
        theSubmitLabel = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        ServletRequest request    = pageContext.getRequest();
        theOldCallRecord          = (CallJspXRecord) request.getAttribute( CallJspXRecord.CALL_JSPX_RECORD_ATTRIBUTE_NAME );
        theCurrentCallRecord      = new CallJspXRecord( thePage );
        request.setAttribute( CallJspXRecord.CALL_JSPX_RECORD_ATTRIBUTE_NAME, theCurrentCallRecord );

        return EVAL_BODY_BUFFERED; // contains parameter declarations
    }

    /**
     * Our implementation of doAfterBody(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        ServletRequest request = pageContext.getRequest();
        BodyContent    body    = getBodyContent();
        JspWriter      out     = body.getEnclosingWriter();

        StringBuilder domId = new StringBuilder();
        domId.append( thePage );

        // This is not ordered, but that should not be a problem?
        for( Entry<String,Object> current : theCurrentCallRecord.getParameters() ) {
            Object value = current.getValue();
            if( value == null ) {
                continue;
            }

            String parId;
            if( value instanceof HasIdentifier ) {
                parId = ((HasIdentifier)value).getIdentifier().toExternalForm();
            } else {
                parId = value.toString();
            }

            domId.append( "-" );
            domId.append( parId );
        }

        out.print( "<a href=\"javascript:overlay_show( '" + domId + "', {} )\"" );
        if( theLinkTitle != null ) {
            out.print( " title=\"" + theLinkTitle + "\"" );
        }
        out.println( ">" );

        body.writeOut( out );

        out.println( "</a>" );
        out.print( "<div class=\"" );
        out.print( OverlayTag.class.getName().replace( '.', '-' ) );
        out.print( "\" id=\"" + domId + "\"" );
        out.println( ">" );

        if( theAction != null ) {
            out.print( "<form action=\"" + theAction + "\" method=\"post\" enctype=\"multipart/form-data\">" );

            String toInsert = SafeFormHiddenInputTag.hiddenInputTagString( pageContext );
            if( toInsert != null ) {
                out.print( toInsert );
            }
        }
        out.println( "<div class=\"scroll-if-too-long\">" );

        try {
            // This is created after org/apache/jasper/runtime/JspRuntimeLibrary.include
            RequestDispatcher rd = pageContext.getServletContext().getRequestDispatcher( thePage );
            rd.include( request, new ServletResponseWrapperInclude( (HttpServletResponse) pageContext.getResponse(), out ));

            return EVAL_PAGE;

        } catch( ServletException ex ) {
            throw new JspException( ex ); // why in the world are these two differnt types of exceptions?

        } finally {
            request.setAttribute( CallJspXRecord.CALL_JSPX_RECORD_ATTRIBUTE_NAME, theOldCallRecord );

            out.println( "</div>" );
            if( theAction != null ) {
                out.println( "<div class=\"dialog-buttons\">" );
                out.println( "<table class=\"dialog-buttons\">" );
                out.println( "<tr>" );
                out.print( "<td><input type=\"submit\" value=\"" );
                if( theSubmitLabel != null ) {
                    out.print( theSubmitLabel );
                } else {
                    out.print( DEFAULT_SUBMIT_LABEL );
                }
                out.println( "\" /></td>" );
                out.println( "<td><a href=\"javascript:overlay_hide( '" + domId + "' )\">Cancel</a></td>" );
                out.println( "</tr>" );
                out.println( "</table>" );
                out.println( "</div>" );
            }
            out.println( "</form>" );
            out.println( "</div>" );
        }
    }

    /**
     * Name of the page.
     */
    protected String thePage;

    /**
     * Title attribute on the generated link, if any.
     */
    protected String theLinkTitle;

    /**
     * Action to take in the form.
     */
    protected String theAction;

    /**
     * Label on the submit button.
     */
    protected String theSubmitLabel;

    /**
     * The CallJspXRecord to restore.
     */
    CallJspXRecord theOldCallRecord;

    /**
     * The CallJspXRecord for the current call.
     */
    CallJspXRecord theCurrentCallRecord;

    /**
     * The default label to be put on the submit button if none is given.
     */
    public static final String DEFAULT_SUBMIT_LABEL = ResourceHelper.getInstance( CallJspoTag.class ).getResourceStringOrDefault( "DefaultSubmitLabel", "Submit" );
}
