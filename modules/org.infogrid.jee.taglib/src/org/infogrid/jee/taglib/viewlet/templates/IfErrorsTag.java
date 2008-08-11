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

package org.infogrid.jee.taglib.viewlet.templates;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.JspStructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponse;

/**
 * Tests whether one or more errors has been reported.
 */
public class IfErrorsTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfErrorsTag()
    {
        // noop
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
    protected boolean evaluateTest()
        throws
            JspException,
            IgnoreException
    {
        StructuredResponse response = (StructuredResponse) lookup( JspStructuredResponseTemplate.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        if( response == null ) {
            return false;
        }
        boolean ret = response.haveProblemsBeenReported();

        return ret;
    }
}
