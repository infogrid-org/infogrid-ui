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
import org.infogrid.jee.taglib.IgnoreException;

/**
 * <p>Write a piece of text to a named TextStructuredResponseSection, overwriting what was there already (if any).</p>
 * <p>Implementation inheritance here leads to the shortest amount of code to write.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class DefineSectionTag
    extends
        AppendSectionTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public DefineSectionTag()
    {
        // noop
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
        theSection.setContent( null );

        return super.determineBodyText();
    }

}
