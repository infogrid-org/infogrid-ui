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
 * <p>Append a piece of text to a named TextStructuredResponseSection.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class AppendSectionTag
    extends
        AbstractInsertIntoSectionTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public AppendSectionTag()
    {
        // noop
    }

    /**
     * Initialize all default values.
     */
    @Override
    protected void initializeToDefaults()
    {
        theSectionName = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the sectionName property.
     *
     * @return value of the sectionName property
     * @see #setSectionName
     */
    public final String getSectionName()
    {
        return theSectionName;
    }

    /**
     * Set value of the sectionName property.
     *
     * @param newValue new value of the sectionName property
     * @see #getSectionName
     */
    public final void setSectionName(
            String newValue )
    {
        theSectionName = newValue;
    }

    /**
     * Determine the text to insert into the header when the tag's body has been processed.
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
        BodyContent body       = getBodyContent();
        String      bodyString = body != null ? body.getString() : null;

        return bodyString;
    }

    /**
     * The name of the section in the StructuredResponse that is being evaluated.
     */
    protected String theSectionName;
}
