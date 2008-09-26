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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.templates;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * <p>Insert an HTML title into the HTML header.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TitleTag
    extends
        AbstractInsertIntoHtmlHeaderTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TitleTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();
    }
    
    /**
     * Determine the text to insert into the header.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected String determineText()
        throws
            JspException,
            IgnoreException
    {
        BodyContent body     = getBodyContent();
        String      theTitle = body != null ? body.getString() : null;

        if( theTitle != null ) {
            StringBuilder buf = new StringBuilder();
            buf.append( "<title>" );
            buf.append( theTitle );
            buf.append( "</title>" );
            return buf.toString();

        } else {
            return "";
        }
    }
}
