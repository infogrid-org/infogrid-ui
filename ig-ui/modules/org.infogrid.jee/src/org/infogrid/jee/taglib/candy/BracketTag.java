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
        theIfContentBuffer    = new StringBuilder();
        theNotIfContentBuffer = new StringBuilder();
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
        if( theFlag == null ) {
            throw new JspException( "BracketTag must contain exactly one BracketContentTag" );
        }
        if( theFlag ) {
            if( theIfContentBuffer != null ) {
                theFormatter.print( pageContext, false, theIfContentBuffer.toString() );
            }
        } else {
            if( theNotIfContentBuffer != null ) {
                theFormatter.print( pageContext, false, theNotIfContentBuffer.toString() );
            }
        }
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
        theIfContentBuffer.append( content );

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
        theIfContentBuffer.append( content );
    }

    /**
     * Allow the BracketNotIfContentTag to notify us of new content.
     *
     * @throws JspException thrown if an evaluation error occurred
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected void contentFromNotIfContentTag(
            String content )
        throws
            JspException,
            IOException
    {
        theNotIfContentBuffer.append( content );
    }

    /**
     * Buffer for conditional content before we know whether to emit it.
     * This will be emitted if the BracketContentTag has content.
     */
    protected StringBuilder theIfContentBuffer;

    /**
     * Buffer for conditional content before we know whether to emit it.
     * This will be emitted if the BracketContentTag does not have content.
     */
    protected StringBuilder theNotIfContentBuffer;

    /**
     * If null, we don't know whether to emit yet.
     * If true, we emit.
     * If false, we don't emit.
     */
    protected Boolean theFlag;
}
