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
import javax.servlet.jsp.tagext.Tag;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
        
/**
 * <p>Processes the body if the inclosing iteration has a next element.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class IfIterationHasNextTag
        extends
             AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfIterationHasNextTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();
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
        // find the closest enclosing iteration
        Tag iterationTag = this;
        while( ( iterationTag = iterationTag.getParent() ) != null ) {
            if( iterationTag instanceof InfoGridIterationTag ) {
                break;
            }
        }
        if( iterationTag == null ) {
            // not inside an iteration
            throw new JspException( "IfIterationHasNextTag: not contained inside an iteration tag" );
        }

        InfoGridIterationTag realIterationTag = (InfoGridIterationTag) iterationTag;

        if( realIterationTag.hasNext() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }        
    }
}
