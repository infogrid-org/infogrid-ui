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
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.model.traversal.TraversalPath;

/**
 * Factors out common code for tags that iterate over a <code>TraversalPathSet</code>.
 */
public abstract class AbstractTraversalPathSetIterateTag
    extends
        AbstractSetIterateTag<TraversalPath>
{
    /**
     * Constructor.
     */
    protected AbstractTraversalPathSetIterateTag()
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
     * This method is defined by subclasses to provide the set over which we iterate.
     *
     * @param reverse if true, iterate in the reverse direction
     * @return the set to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected Iterable<TraversalPath> determineSet(
            boolean reverse )
        throws
            JspException,
            IgnoreException
    {
        TraversalPathSet theSet = determineTraversalPathSet();

        return theSet;
    }

    /**
     * This method is defined by subclasses to provide the TraversalPathSet over which we iterate.
     *
     * @return the set to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract TraversalPathSet determineTraversalPathSet()
        throws
            JspException,
            IgnoreException;
}
