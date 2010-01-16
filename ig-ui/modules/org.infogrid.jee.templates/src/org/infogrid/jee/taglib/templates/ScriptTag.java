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
import org.infogrid.jee.templates.StructuredResponse;

/**
 * <p>Insert a href'd or inline'd script into the HTML header.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ScriptTag
    extends
        AbstractInsertIntoSectionTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ScriptTag()
    {
        // noop
    }

    /**
     * Initialize all default values.
     */
    @Override
    protected void initializeToDefaults()
    {
        theSrc = null;

        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the src property.
     *
     * @return value of the src property
     * @see #setSrc
     */
    public final String getSrc()
    {
        return theSrc;
    }

    /**
     * Set value of the src property.
     *
     * @param newValue new value of the src property
     * @see #getSrc
     */
    public final void setSrc(
            String newValue )
    {
        theSrc = newValue;
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
        StringBuilder ret = new StringBuilder();
        ret.append( "<script type=\"text/javascript\"" );

        if( theSrc != null && theSrc.length() > 0 ) {
            // must be in the attribute
            ret.append( " src=\"" );
            ret.append( theSrc );
            ret.append( "\"" );
        }
        ret.append( ">" );

        return ret.toString();
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
        
        if( bodyString != null && bodyString.length() == 0 ) {
            bodyString = null;
        }

        if( bodyString == null ) {
            if( theSrc != null && theSrc.length() > 0 ) {
                return null;
            } else {
                throw new JspException( "Tag " + this + " must have either src attribute or body" );
            }
        }
        return bodyString;
    }

    /**
     * Determine the text to insert into the header when the tag is closed.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected String determineEndText()
        throws
            JspException,
            IgnoreException
    {
        return "</script>";
    }
    
    /**
     * The optional src attribute.
     */
    protected String theSrc;
    
}
