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

package org.infogrid.jee.taglib.logic;

import org.infogrid.jee.taglib.IgnoreException;

import javax.servlet.jsp.JspException;

/**
 * <p>This tag tests for a non-match of the String representation of a PropertyValue
 * against a regular expression.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class NotMatchTag
        extends
            AbstractPropertyMatchTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public NotMatchTag()
    {
        // noop
    }

    /**
     * Determine whether or not to process the content of this Tag. The content of this
     * tag shall be processed if <code>matches()</code> returns false.
     *
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluateTest()
        throws
            JspException,
            IgnoreException
    {
        return !matches();
    }
}
