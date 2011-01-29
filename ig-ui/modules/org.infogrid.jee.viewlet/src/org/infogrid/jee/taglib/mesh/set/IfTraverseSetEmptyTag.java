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

package org.infogrid.jee.taglib.mesh.set;

import javax.servlet.jsp.JspException;
import org.infogrid.mesh.set.MeshObjectSet;

/**
 * Tag that evaluates its content when a <code>MeshObjectSet</code> obtained by traversal is empty.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class IfTraverseSetEmptyTag
    extends
        AbstractTraverseSetTestTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfTraverseSetEmptyTag()
    {
        // noop
    }

    /**
     * Evaluate the condition. If it returns true, the content of this tag is processed.
     *
     * @param set the MeshObjectSet to evaluate
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     */
    protected boolean evaluateTest(
            MeshObjectSet set )
        throws
            JspException
    {
        boolean ret = set.isEmpty();
        return ret;
    }
}
