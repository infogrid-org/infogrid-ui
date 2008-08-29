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

import org.infogrid.jee.taglib.IgnoreException;

import javax.servlet.jsp.JspException;

/**
 * <p>Abstract superclass for all tags that .</p>
 */
public abstract class AbstractHrefOrInlineTag
    extends
        AbstractInsertIntoHtmlHeaderTag
{
    /**
     * Constructor.
     */
    protected AbstractHrefOrInlineTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
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
     * Determine the text to insert into the header.
     *
     * @return text to insert
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected final String determineText()
        throws
            JspException,
            IgnoreException
    {
        String ret;
        
        if( theHref != null ) {
            ret = formatHref( theHref );

        } else {
            ret = formatInline( pageContext.getOut().toString() );
        }
        
        return ret;
    }

    /**
     * Enable subclass to format the Href properly.
     * 
     * @param href the Href
     * @return formatted String
     */
    protected abstract String formatHref(
            String href );
    
    /**
     * Enable subclass to format the inlined text properly.
     * 
     * @param text the inlined text
     * @return formatted String
     */
    protected abstract String formatInline(
            String text );
    
    /**
     * The optional href attribute.
     */
    protected String theHref;
}
