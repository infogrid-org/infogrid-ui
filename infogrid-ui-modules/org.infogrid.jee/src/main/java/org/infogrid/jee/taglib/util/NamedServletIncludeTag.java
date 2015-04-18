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

package org.infogrid.jee.taglib.util;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.BodyContent;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * <p>Allows the inclusion of named servlets. This should be a standard feature
 *    of the <code>&lt;jsp:include&lt;</code> tag, but it isn't.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class NamedServletIncludeTag
    extends
        AbstractInfoGridTag
    
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public NamedServletIncludeTag()
    {
        // noop
    }

    /**
     * Release all of our resources.
     */
    @Override
    protected void initializeToDefaults()
    {
        theServletName = null;
        theFlush       = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the servletName property.
     *
     * @return value of the servletName property
     * @see #setServletName
     */
    public String getServletName()
    {
        return theServletName;
    }

    /**
     * Set value of the servletName property.
     *
     * @param newValue new value of the servletName property
     * @see #getServletName
     */
    public void setServletName(
            String newValue )
    {
        theServletName = newValue;
    }

    /**
     * Obtain value of the flush property.
     *
     * @return value of the flush property
     * @see #setFlush
     */
    public String getFlush()
    {
        return theFlush;
    }

    /**
     * Set value of the flush property.
     *
     * @param newValue new value of the flush property
     * @see #getFlush
     */
    public void setFlush(
            String newValue )
    {
        theFlush = newValue;
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
        // This is created after org/apache/jasper/runtime/JspRuntimeLibrary.include
        
        JspWriter out = pageContext.getOut();
        if( getFormatter().isTrue( theFlush ) && !(out instanceof BodyContent)) {
            out.flush();
        }

        try {
            RequestDispatcher rd = pageContext.getServletContext().getNamedDispatcher( theServletName );
            rd.include( pageContext.getRequest(), new ServletResponseWrapperInclude( (HttpServletResponse) pageContext.getResponse(), out ));
            return EVAL_BODY_INCLUDE;
            
        } catch( ServletException ex ) {
            throw new JspException( ex ); // why in the world are these two differnt types of exceptions?
        }
    }

    /**
     * Name of the Servlet as configured in <code>web.xml</code>.
     */
    protected String theServletName;
    
    /**
     * Should we flush prior to including.
     */
    protected String theFlush;
}
