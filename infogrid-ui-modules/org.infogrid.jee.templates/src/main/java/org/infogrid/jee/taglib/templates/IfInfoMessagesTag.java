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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.templates;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * Tests whether one or more informational messages have been reported.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class IfInfoMessagesTag
        extends
            AbstractInfoMessagesTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfInfoMessagesTag()
    {
        // noop
    }

    /**
     * Evaluate the condition. If it returns true, we include, in the output,
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
        boolean ret = haveInformationalMessagesBeenReportedAggregate();
        return ret;
    }
}
