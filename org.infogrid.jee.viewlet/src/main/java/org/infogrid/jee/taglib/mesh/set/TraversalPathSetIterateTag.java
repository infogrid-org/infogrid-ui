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
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.set.TraversalPathSet;

/**
 * Iterate over a <code>TraversalPathSet</code> given as a bean.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TraversalPathSetIterateTag
    extends
        AbstractTraversalPathSetIterateTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TraversalPathSetIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theTraversalPathSetName = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the traversalPathSet property.
     *
     * @return value of the traversalPathSet property
     * @see #setTraversalPathSetName
     */
    public final String getTraversalPathSetName()
    {
        return theTraversalPathSetName;
    }

    /**
     * Set value of the traversalPathSet property.
     *
     * @param newValue new value of the traversalPathSet property
     * @see #getTraversalPathSetName
     */
    public final void setTraversalPathSetName(
            String newValue )
    {
        theTraversalPathSetName = newValue;
    }

    /**
     * Provide the TraversalPathSet over which we iterate.
     *
     * @return the set to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected TraversalPathSet determineTraversalPathSet()
        throws
            JspException,
            IgnoreException
    {
        TraversalPathSet ret = (TraversalPathSet) lookupOrThrow( theTraversalPathSetName );
        return ret;
    }

    /**
     * Name of the bean that contains the TraversalPathSet to render.
     */
    protected String theTraversalPathSetName;
}
