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

package org.infogrid.jee.taglib.mesh.set;

import javax.servlet.jsp.JspException;
import org.infogrid.mesh.set.MeshObjectSet;

/**
 * Tag that evaluates its content when a <code>MeshObjectSet</code> obtained by traversal
 * has a number of members between a lower and an upper limit.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class IfTraverseSetContainsTag
    extends
        AbstractTraverseSetTestTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfTraverseSetContainsTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMinFound = null;
        theMaxFound = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the minFound property.
     *
     * @return value of the minFound property
     * @see #setMinFound
     */
    public final String getMinFound()
    {
        return theMinFound;
    }

    /**
     * Set value of the minFound property.
     *
     * @param newValue new value of the minFound property
     * @see #getMinFound
     */
    public final void setMinFound(
            String newValue )
    {
        theMinFound = newValue;
    }

    /**
     * Obtain value of the maxFound property.
     *
     * @return value of the maxFound property
     * @see #setMaxFound
     */
    public final String getMaxFound()
    {
        return theMaxFound;
    }

    /**
     * Set value of the maxFound property.
     *
     * @param newValue new value of the minFound property
     * @see #getMaxFound
     */
    public final void setMaxFound(
            String newValue )
    {
        theMaxFound = newValue;
    }

    /**
     * EvaluateEvaluate the condition. If it returns true, the content of this tag is processed.
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
        int size = set.size();
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

    /**
     * Determine the integer value of the provided String.
     *
     * @param s the String
     * @return the corresponding integer value
     */
    protected int determineValue(
            String s )
    {
        if( s == null || s.length() == 0 ) {
            return -1;
        }
        int ret = Integer.parseInt( s );
        return ret;
    }

    /**
     * The minimum number of MeshObjects found.
     */
    protected String theMinFound;

    /**
     * The maximum number of MeshObjects found.
     */
    protected String theMaxFound;
}
