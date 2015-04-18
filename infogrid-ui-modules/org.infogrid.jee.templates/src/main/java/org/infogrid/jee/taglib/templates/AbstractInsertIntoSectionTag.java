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

package org.infogrid.jee.taglib.templates;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;

/**
 * <p>Abstract superclass for all tags that insert tag body content into the HTML header
 *    via a StructuredResponse object.</p>
 */
public abstract class AbstractInsertIntoSectionTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractInsertIntoSectionTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theResponse         = null;
        theSection         = null;
        theAllowDuplicates = true;

        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the allowDuplicates property.
     *
     * @return value of the allowDuplicates property
     * @see #setAllowDuplicates
     */
    public final boolean getAllowDuplicates()
    {
        return theAllowDuplicates;
    }

    /**
     * Set value of the allowDuplicates property.
     *
     * @param newValue new value of the allowDuplicates property
     * @see #getAllowDuplicates
     */
    public final void setAllowDuplicates(
            boolean newValue )
    {
        theAllowDuplicates = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        String sectionName = getSectionName();

        theResponse = (StructuredResponse) lookupOrThrow(
                StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        theSection = theResponse.obtainTextSectionByName( sectionName );

        String text = determineStartText();
        if( text != null ) {
            if( theAllowDuplicates ) {
                theSection.appendContent( text );
                
            } else if( !theSection.containsContent( text ) ) {
                theSection.appendContent( text );
            }
        }
        
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Determine the name of the section into which to insert.
     *
     * @return the name of the section
     */
    protected abstract String getSectionName();

    /**
     * Our implementation of doAfterBody().
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
        String text = determineBodyText();
        if( text != null ) {
            theSection.appendContent( text );
        }
        
        return SKIP_BODY;
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
        String text = determineEndText();
        if( text != null ) {
            theSection.appendContent( text );
        }
        return EVAL_PAGE; // reasonable default
    }

    /**
     * Determine the text to insert into the header when the tag is opened.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected String determineStartText()
        throws
            JspException,
            IgnoreException
    {
        return null; // default
    }

    /**
     * Determine the text to insert into the header when the tag's body has been processed.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected String determineBodyText()
        throws
            JspException,
            IgnoreException
    {
        BodyContent body = getBodyContent();
        if( body == null ) {
            return null;
        }
        String bodyString = body.getString();
        if( bodyString == null ) {
            return null;
        }
        if( bodyString.length() == 0 ) {
            return null;
        }
        throw new JspException( "Tag " + this + " must not contain body content" );
    }

    /**
     * Determine the text to insert into the header when the tag is closed.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected String determineEndText()
        throws
            JspException,
            IgnoreException
    {
        return null; // default
    }

    /**
     * The StructuredResponse to which we write.
     */
    protected StructuredResponse theResponse;
    
    /**
     * The TextStructuredResponse to which we write.
     */
    protected TextStructuredResponseSection theSection;
    
    /**
     * If true, duplicate insertions are allowed.
     */
    protected boolean theAllowDuplicates;
}
