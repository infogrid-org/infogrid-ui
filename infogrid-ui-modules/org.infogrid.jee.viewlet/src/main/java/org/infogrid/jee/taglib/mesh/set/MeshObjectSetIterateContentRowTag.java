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

package org.infogrid.jee.taglib.mesh.set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;

/**
 * Tag that collects the content of a single content row within
 * the <code>MeshObjectSetIterate</code> tag and thus distinguishes it from header
 * and other rows.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectSetIterateContentRowTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectSetIterateContentRowTag()
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
        Tag parentTag      = getParent();
        Tag grandParentTag = parentTag != null ? parentTag.getParent() : null;

        if( parentTag == null ) {
            throw new JspException( "MeshObjectSetIterateContentRow tag must be directly contained in an AbstractSetIterateTag tag" );
        }
        if(    !( parentTag      instanceof AbstractSetIterateTag )
            && !( grandParentTag instanceof AbstractSetIterateTag ))
        {
            throw new JspException( "MeshObjectSetIterateContentRow tag must be directly contained in an AbstractSetIterateTag tag" );
        }

        theRealParentTag =
                (parentTag instanceof AbstractSetIterateTag)
                        ? (AbstractSetIterateTag) parentTag
                        : (AbstractSetIterateTag) grandParentTag;

        if( theRealParentTag.displayContentRow() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * The real parent tag.
     */
    protected AbstractSetIterateTag theRealParentTag;
}
