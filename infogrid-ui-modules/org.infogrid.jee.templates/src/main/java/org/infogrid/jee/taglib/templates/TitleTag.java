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

package org.infogrid.jee.taglib.templates;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;

/**
 * <p>Insert an HTML title into the HTML header.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TitleTag
    extends
        AbstractInsertIntoSectionTag
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
     * Determine the name of the section into which to insert.
     *
     * @return the name of the section
     */
    protected String getSectionName()
    {
        return StructuredResponse.HTML_TITLE_SECTION.getSectionName();
    }

    /**
     * Determine the text to insert into the header.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected String determineBodyText()
        throws
            JspException,
            IgnoreException
    {
        theSection.setContent( null ); // reset what was there before

        BodyContent body     = getBodyContent();
        String      theTitle = body != null ? body.getString() : null;

        if( theTitle != null ) {
            StringBuilder buf = new StringBuilder();
            buf.append( "<title>" );
            buf.append( theTitle );
            buf.append( "</title>" );
            return buf.toString();

        } else {
            return null;
        }
    }
}
