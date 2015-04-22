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

package org.infogrid.jee.taglib.util;

import javax.servlet.jsp.JspException;

/**
 * Evaluate content if the current URL has an argument with a particular value.
 */
public class IfUrlArgumentMatchesTag
        extends
            AbstractUrlArgumentMatchesTag
{
    /**
     * Constructor.
     */
    public IfUrlArgumentMatchesTag()
    {
        // noop
    }

    /**
     * {@inheritDoc}
     */
    protected boolean evaluateTest(
            boolean flag )
        throws
            JspException
    {
        return flag;
    }
}
