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
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * A Tag that conditionally emits content if the content of another contained tag is not only white space.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class BracketTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public BracketTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theBuffer = new StringBuilder();
        theFlag   = null;
        
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
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Invoked after the Body tag has been invoked.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an error occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException
    {
        return SKIP_BODY;
    }

    /**
     * Our implementation of doEndTag().
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
        if( theFlag == null ) {
            throw new JspException( "BracketTag must contain exactly one BracketContentTag" );
        }
        if( theFlag && theBuffer != null ) {
            // emit
            theFormatter.printPrevious( pageContext, false, theBuffer.toString() );
        }
        return EVAL_PAGE;
    }

    /**
     * Allow the BracketContentTag to notify us what its content is.
     *
     * @throws JspException thrown if an evaluation error occurred
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected void contentFromContentTag(
            String content )
        throws
            JspException,
            IOException
    {
        if( theFlag != null ) {
            throw new JspException( "BracketTag must contain exactly one BracketContentTag" );
        }
        theBuffer.append( content );

        if( content.trim().length() == 0 ) {
            theFlag = false;
        } else {
            theFlag = true;
        }
    }

    /**
     * Allow the BracketIfContentTag to notify us of new content.
     *
     * @throws JspException thrown if an evaluation error occurred
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected void contentFromIfContentTag(
            String content )
        throws
            JspException,
            IOException
    {
        theBuffer.append( content );
    }

    /**
     * Buffer for conditional content before we know whether to emit it.
     */
    protected StringBuilder theBuffer;

    /**
     * If null, we don't know whether to emit yet.
     * If true, we emit.
     * If false, we don't emit.
     */
    protected Boolean theFlag;
}
