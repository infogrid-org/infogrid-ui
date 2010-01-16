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
import org.infogrid.jee.templates.StructuredResponse;

/**
 * <p>Insert a href'd or inline'd style sheet into the HTML header.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class StylesheetTag
    extends
        AbstractInsertIntoSectionTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public StylesheetTag()
    {
        // noop
    }

    /**
     * Initialize all default values.
     */
    @Override
    protected void initializeToDefaults()
    {
        theHref = null;

        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the href property.
     *
     * @return value of the href property
     * @see #setHref
     */
    public final String getHref()
    {
        return theHref;
    }

    /**
     * Set value of the href property.
     *
     * @param newValue new value of the href property
     * @see #getHref
     */
    public final void setHref(
            String newValue )
    {
        theHref = newValue;
    }
    
    /**
     * Determine the name of the section into which to insert.
     *
     * @return the name of the section
     */
    protected String getSectionName()
    {
        return StructuredResponse.HTML_HEAD_SECTION.getSectionName();
    }

    /**
     * Determine the text to insert into the header when the tag is opened.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected String determineStartText()
        throws
            JspException,
            IgnoreException
    {
        if( theHref == null || theHref.length() == 0 ) {
            throw new JspException( "Href attribute on tag " + this + " must not be empty" );
        }
        StringBuilder ret = new StringBuilder();
        ret.append( "<link rel=\"stylesheet\" href=\"" );
        ret.append( theHref );
        ret.append( "\" />" );
        
        return ret.toString();
    }

    /**
     * The optional href attribute.
     */
    protected String theHref;
}
