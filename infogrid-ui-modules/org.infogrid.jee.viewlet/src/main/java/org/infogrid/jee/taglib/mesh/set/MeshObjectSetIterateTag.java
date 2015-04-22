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
import org.infogrid.mesh.set.MeshObjectSet;

/**
 * Iterate over a <code>MeshObjectSet</code> given as a bean. Ordering is accomplished
 * by passing in a <code>OrderedMeshObjectSet</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectSetIterateTag
    extends
        AbstractMeshObjectSetIterateTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectSetIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectSetName = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObjectSet property.
     *
     * @return value of the meshObjectSet property
     * @see #setMeshObjectSetName
     */
    public final String getMeshObjectSetName()
    {
        return theMeshObjectSetName;
    }

    /**
     * Set value of the meshObjectSet property.
     *
     * @param newValue new value of the meshObjectSet property
     * @see #getMeshObjectSetName
     */
    public final void setMeshObjectSetName(
            String newValue )
    {
        theMeshObjectSetName = newValue;
    }

    /**
     * Provide the MeshObjectSet over which we iterate.
     *
     * @return the set to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected MeshObjectSet determineMeshObjectSet()
        throws
            JspException,
            IgnoreException
    {        
        MeshObjectSet ret = (MeshObjectSet) lookupOrThrow( theMeshObjectSetName );
        return ret;
    }

    /**
     * Name of the bean that contains the MeshObjectSet to render.
     */
    protected String theMeshObjectSetName;
}
