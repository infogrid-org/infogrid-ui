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

package org.infogrid.jee.taglib.logic;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * <p>This tag tests for whether a numeric property value is not zero.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class NotIfZeroTag
    extends
        AbstractNumericalPropertyConditionTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public NotIfZeroTag()
    {
        // noop
    }

    /**
     * Determine whether or not to process the content of this tag. The content of this
     * tag shall be processed if {@link AbstractNumericalPropertyConditionTag#evaluateToInt evaluateToInt()} returns +1 or -1.
     *
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected boolean evaluateTest()
        throws
            JspException,
            IgnoreException
    {
        return evaluateToInt() != 0;
    }
}
