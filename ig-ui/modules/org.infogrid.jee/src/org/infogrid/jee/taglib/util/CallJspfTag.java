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
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * <p>Allows the inclusion of JSP fragments as subroutines with parameters.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class CallJspfTag
    extends
        AbstractInfoGridTag

{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public CallJspfTag()
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
        theOldCallRecord          = (CallJspfRecord) request.getAttribute( CallJspfRecord.CALL_JSPF_RECORD_ATTRIBUTE_NAME );
        CallJspfRecord callRecord = new CallJspfRecord( thePage );
        request.setAttribute( CallJspfRecord.CALL_JSPF_RECORD_ATTRIBUTE_NAME, callRecord );

        return EVAL_BODY_INCLUDE; // contains parameter declarations
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
        // This is created after org/apache/jasper/runtime/JspRuntimeLibrary.include

        ServletRequest request = pageContext.getRequest();
        JspWriter      out     = pageContext.getOut();

        try {
            RequestDispatcher rd = pageContext.getServletContext().getRequestDispatcher( thePage );
            rd.include( request, new ServletResponseWrapperInclude( (HttpServletResponse) pageContext.getResponse(), out ));

            return EVAL_PAGE;

        } catch( ServletException ex ) {
            throw new JspException( ex ); // why in the world are these two differnt types of exceptions?
        } finally {
            request.setAttribute( CallJspfRecord.CALL_JSPF_RECORD_ATTRIBUTE_NAME, theOldCallRecord );
        }
    }

    /**
     * Name of the page.
     */
    protected String thePage;

    /**
     * The CallJspfRecord to restore.
     */
    CallJspfRecord theOldCallRecord;
}
