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
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.jsp.JspWriter;

/**
 * Separate response, per org/apache/jasper/runtime/JspRuntimeLibrary.include. FIXME: not entirely clear why.
 */
// @SuppressWarnings(value={"deprecation"})
public class ServletResponseWrapperInclude
        extends
            HttpServletResponseWrapper
{
    /**
     * Constructor.
     *
     * @param response the real response
     * @param jspWriter the JspWriter
     */
    public ServletResponseWrapperInclude(
            HttpServletResponse response,
            JspWriter           jspWriter )
    {
        super( response );
        this.printWriter = new PrintWriter(jspWriter);
        this.jspWriter = jspWriter;
    }

    /**
     * Obtain a PrintWriter.
     *
     * @return the PrintWriter
     */
    @Override
    public PrintWriter getWriter()
    {
        return printWriter;
    }

    /**
     * Cannot obtain an OutputStream.
     *
     * @return nothing
     * @throws IllegalStateException
     */
    @Override
    public ServletOutputStream getOutputStream()
    {
        throw new IllegalStateException();
    }

    /**
     * Reset the buffer.
     */
    @Override
    public void resetBuffer()
    {
        try {
            jspWriter.clearBuffer();
        } catch( IOException ex ) {
            // ignore
        }
    }

    private PrintWriter printWriter;
    private JspWriter   jspWriter;
}