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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.candy;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * Accessory tag for BracketTag.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class BracketIfContentTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public BracketIfContentTag()
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
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Invoked after the Body tag has been invoked.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an error occurred
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException,
            IOException
    {
        Tag parentTag = getParent();
        if( parentTag == null || !( parentTag instanceof BracketTag )) {
            throw new JspException( "BracketIfContentTag tag must be directly contained in an BracketTag tag" );
        }
        BracketTag realParentTag = (BracketTag) parentTag;
        realParentTag.contentFromIfContentTag( bodyContent.getString() );

        return SKIP_BODY;
    }

    /**
     * Our implementation of doEndTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        return EVAL_PAGE;
    }
}
