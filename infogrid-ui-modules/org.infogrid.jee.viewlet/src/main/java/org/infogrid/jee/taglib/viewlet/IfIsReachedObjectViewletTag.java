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

package org.infogrid.jee.taglib.viewlet;

import javax.servlet.jsp.JspException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.model.traversal.TraversalPath;

/**
 * Tag that evaluates its content when a Viewlet's "reached" <code>MeshObjectSet</code> contains
 * a given MeshObject.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class IfIsReachedObjectViewletTag
    extends
        AbstractIsReachedObjectViewletTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfIsReachedObjectViewletTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();
    }

    /**
     * Evaluate the condition. If it returns true, we output
     * the Nodes contained in this Node. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @param test the MeshObject to evaluate
     * @param set the MeshObjectSet to evaluate
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     */
    protected boolean evaluateTest(
            MeshObject    test,
            MeshObjectSet set )
        throws
            JspException
    {
        if( test == null ) {
            return false;
        }
        if( set == null ) {
            return false;
        }
        if( !set.contains( test )) {
            return false;
        }
        return true;
    }

    /**
     * Evaluate the condition. If it returns true, we output
     * the Nodes contained in this Node. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @param test the TraversalPath to evaluate
     * @param set the TraversalPathSet to evaluate
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     */
    protected boolean evaluateTest(
            TraversalPath    test,
            TraversalPathSet set )
        throws
            JspException
    {
        if( test == null ) {
            return false;
        }
        if( set == null ) {
            return false;
        }
        if( !set.contains( test )) {
            return false;
        }
        return true;
    }
}
