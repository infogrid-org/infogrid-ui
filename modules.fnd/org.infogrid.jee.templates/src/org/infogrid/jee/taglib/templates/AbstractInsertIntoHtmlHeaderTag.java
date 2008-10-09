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

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextHtmlStructuredResponseSectionTemplate;
import org.infogrid.jee.templates.TextStructuredResponseSection;

/**
 * <p>Abstract superclass for all tags that insert tag body content into the HTML header
 *    via a StructuredResponse object.</p>
 */
public abstract class AbstractInsertIntoHtmlHeaderTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractInsertIntoHtmlHeaderTag()
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
        theResponse = (StructuredResponse) lookupOrThrow(
                StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        theHtmlHeadSection = theResponse.getTextSection( TextHtmlStructuredResponseSectionTemplate.HTML_HEAD_SECTION );

        String text = determineStartText();
        if( text != null ) {
            theHtmlHeadSection.appendContent( text );
        }
        
        return EVAL_BODY_BUFFERED;
    }

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
            theHtmlHeadSection.appendContent( text );
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
            theHtmlHeadSection.appendContent( text );
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
     * The HTML head section in the StructuredResponse to which we write.
     */
    protected TextStructuredResponseSection theHtmlHeadSection;
}
