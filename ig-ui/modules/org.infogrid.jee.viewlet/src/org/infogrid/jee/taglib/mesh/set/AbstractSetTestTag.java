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

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.set.MeshObjectSet;

/**
 * Factors out common code for tags that examine a condition derived from 
 * the content of a <code>MeshObjectSet</code>.
 */
public abstract class AbstractSetTestTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    public AbstractSetTestTag()
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
        MeshObjectSet set = (MeshObjectSet) lookupOrThrow( theMeshObjectSetName );
        
        if( evaluateTest( set ) ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Evaluate the condition. If it returns true, we output
     * the Nodes contained in this Node. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @param set the MeshObjectSet to evaluate
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     */
    protected abstract boolean evaluateTest(
            MeshObjectSet set )
        throws
            JspException;

    /**
     * Name of the bean that contains the MeshObjectSet to render.
     */
    protected String theMeshObjectSetName;
}
