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
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseSection;

/**
 * <p>Abstract superclass for all tags evaluating a <code>StructuredResponseSection</code>.</p>
 */
public abstract class AbstractSectionTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractSectionTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theSectionName = null;

        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the name property.
     *
     * @return value of the name property
     * @see #setSectionName
     */
    public final String getSectionName()
    {
        return theSectionName;
    }

    /**
     * Set value of the name property.
     *
     * @param newValue new value of the name property
     * @see #getSectionName
     */
    public final void setSectionName(
            String newValue )
    {
        theSectionName = newValue;
    }

    /**
     * Determine the section to be used in the tag.
     *
     * @return the section
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected StructuredResponseSection evaluate()
        throws
            JspException,
            IgnoreException
    {
        StructuredResponse response = (StructuredResponse) lookup( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        if( response == null ) {
            return null;
        }
        if( theSectionName == null ) {
            return null;
        }
        StructuredResponseSection ret = response.obtainTextSectionByName( theSectionName );
        if( ret == null ) {
            ret = response.obtainBinarySectionByName( theSectionName );
        }

        return ret;
    }

    /**
     * The name of the section in the StructuredResponse that is being evaluated.
     */
    protected String theSectionName;
}
