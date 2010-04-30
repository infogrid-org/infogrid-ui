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

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.set.MeshObjectSet;

/**
 * Prints out the size of a <code>MeshObjectSet</code>.
 */
public class SetSizeTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public SetSizeTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectSetName = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObjectSetName property.
     *
     * @return value of the meshObjectSetName property
     * @see #setMeshObjectSetName
     */
    public final String getMeshObjectSetName()
    {
        return theMeshObjectSetName;
    }

    /**
     * Set value of the meshObjectSetName property.
     *
     * @param newValue new value of the meshObjectSetName property
     * @see #getMeshObjectSetName
     */
    public final void setMeshObjectSetName(
            String newValue )
    {
        theMeshObjectSetName = newValue;
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
        MeshObjectSet set     = (MeshObjectSet) lookupOrThrow( theMeshObjectSetName );
        String        toPrint = String.valueOf( set.size() );

        print( toPrint );

        return SKIP_BODY;
    }

    /**
     * Name of the bean that contains the MeshObjectSet to render.
     */
    protected String theMeshObjectSetName;
}
