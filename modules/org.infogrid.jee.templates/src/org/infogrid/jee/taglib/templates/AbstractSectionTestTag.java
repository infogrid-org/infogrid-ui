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
import org.infogrid.jee.templates.JspStructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseSection;

/**
 * <p>Abstract superclass for all tags evaluating a <code>StructuredResponseSection</code>.</p>
 */
public abstract class AbstractSectionTestTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractSectionTestTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theName = null;

        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the name property.
     *
     * @return value of the name property
     * @see #setName
     */
    public final String getName()
    {
        return theName;
    }

    /**
     * Set value of the name property.
     *
     * @param newValue new value of the name property
     * @see #getName
     */
    public final void setName(
            String newValue )
    {
        theName = newValue;
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
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Evaluatate the condition. If it returns true, we include, in the output,
     * the content contained in this tag. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * Determine the PropertyValue to be used in the test.
     *
     * @return the PropertyValue
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected StructuredResponseSection evaluate()
        throws
            JspException,
            IgnoreException
    {
        StructuredResponse response = (StructuredResponse) lookup( JspStructuredResponseTemplate.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        if( response == null ) {
            return null;
        }
        StructuredResponseSection ret  = response.findSectionByName( theName );

        return ret;
    }

    /**
     * The name of the section in the StructuredResponse that is being evaluated.
     */
    protected String theName;
}
