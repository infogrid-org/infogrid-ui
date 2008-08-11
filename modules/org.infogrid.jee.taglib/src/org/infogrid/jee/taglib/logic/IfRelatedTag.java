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

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.set.MeshObjectSet;

/**
 * <p>This tag tests whether the number of MeshObjects found by traversal is within the specified
 *    minimum and maximum values.
 *    {@link org.infogrid.jee.taglib.logic package documentation}.</p>
 */
public class IfRelatedTag
    extends
        AbstractRelatedTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfRelatedTag()
    {
        // noop
    }

    /**
     * Evaluatate the condition. If it returns true, the content of this tag is processed.
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
        MeshObjectSet value = evaluate();
        
        int size = value.size();
        int min  = determineValue( theMinFound );
        int max  = determineValue( theMaxFound );
        
        if( max == -1 ) {
            max = Integer.MAX_VALUE;
        }
        
        if( size < min ) {
            return false;
        } else if( size > max ) {
            return false;
        } else {
            return true;
        }
    }

}
