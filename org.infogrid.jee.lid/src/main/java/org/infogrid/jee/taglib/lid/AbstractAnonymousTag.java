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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.lid;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.lid.servlet.LidPipelineServlet;
import org.infogrid.util.Identifier;

/**
 * Collects functionality common to testing whether the current user is known or anonymous.
 */
public abstract class AbstractAnonymousTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractAnonymousTag()
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
     * Our implementation of doStartTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Determine whether or not to process the content of this Tag.
     *
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * Determine whether the current user is anonymous.
     *
     * @return true if the current user is anonymous
     */
    protected boolean isAnonymous()
    {
        Identifier me = (Identifier) pageContext.getRequest().getAttribute( LidPipelineServlet.USER_ID_ATTRIBUTE_NAME );

        return me == null;
    }
}
