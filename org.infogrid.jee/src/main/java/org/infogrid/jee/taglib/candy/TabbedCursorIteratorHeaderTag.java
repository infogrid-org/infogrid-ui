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

package org.infogrid.jee.taglib.candy;

import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

/**
 * Tag that collects the content of a header row within
 * the <code>TabbedCursorIteratorTag</code> tag and thus distinguishes it from content
 * and other rows.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TabbedCursorIteratorHeaderTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TabbedCursorIteratorHeaderTag()
    {
        // noop
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     */
    protected int realDoStartTag()
        throws
            JspException
    {
        Tag parentTag = getParent();
        if( parentTag == null || !( parentTag instanceof TabbedCursorIteratorTag )) {
            throw new JspException( "TabbedCursorIteratorHeaderTag tag must be directly contained in an TabbedCursorIteratorTag tag" );
        }

        TabbedCursorIteratorTag realParentTag = (TabbedCursorIteratorTag) parentTag;
        if( realParentTag.displayHeader() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }
}

