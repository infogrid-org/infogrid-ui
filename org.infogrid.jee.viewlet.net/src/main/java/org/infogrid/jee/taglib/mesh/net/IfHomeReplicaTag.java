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

package org.infogrid.jee.taglib.mesh.net;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * This tag tests whether a NetMeshObject is the Home Replica.
 */
public class IfHomeReplicaTag
        extends
            AbstractIfHomeReplicaTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfHomeReplicaTag()
    {
        // no op
    }

    /**
     * Evaluate the condition. If it returns true, the content of this tag is processed.
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
        boolean value = evaluate();

        return value;
    }
}
