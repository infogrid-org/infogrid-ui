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
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.model.traversal.TraversalSpecification;

/**
 * Iterate over a <code>MeshObjectSet</code> that is determined by traversing from a start
 * <code>MeshObject</code> via some <code>TraversalSpecification</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectSetTraverseIterateTag
    extends
        AbstractMeshObjectSetIterateTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectSetTraverseIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStartObject            = null;
        theStartObjectName        = null;
        theTraversalSpecification = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the startObject property.
     *
     * @return value of the startObject property
     * @see #setStartObject
     */
    public final String getStartObject()
    {
        return theStartObject;
    }

    /**
     * Set value of the startObject property.
     *
     * @param newValue new value of the startObject property
     * @see #getStartObject
     */
    public final void setStartObject(
            String newValue )
    {
        theStartObject = newValue;
    }

    /**
     * Obtain value of the startObjectName property.
     *
     * @return value of the startObjectName property
     * @see #setStartObjectName
     */
    public final String getStartObjectName()
    {
        return theStartObjectName;
    }

    /**
     * Set value of the startObjectName property.
     *
     * @param newValue new value of the startObjectName property
     * @see #getStartObjectName
     */
    public final void setStartObjectName(
            String newValue )
    {
        theStartObjectName = newValue;
    }

    /**
     * Obtain value of the traversalSpecification property.
     *
     * @return value of the traversalSpecification property
     * @see #setTraversalSpecification
     */
    public final String getTraversalSpecification()
    {
        return theTraversalSpecification;
    }

    /**
     * Set value of the traversalSpecification property.
     *
     * @param newValue new value of the traversalSpecification property
     * @see #getTraversalSpecification
     */
    public final void setTraversalSpecification(
            String newValue )
    {
        theTraversalSpecification = newValue;
    }

    /**
     * Provide the MeshObjectSet over which we iterate.
     *
     * @return the set to iterate over
     * @throws JspException if the bean was not found and the ignore attribute was not set
     * @throws IgnoreException thrown if the bean could not be found but the ignore attribute was set
     */
    protected MeshObjectSet determineMeshObjectSet()
        throws
            JspException,
            IgnoreException
    {
        MeshObject start;
        if( theStartObject != null ) {
            start = findMeshObjectOrThrow( theStartObject );
        } else {
            start = lookupMeshObjectOrThrow( theStartObjectName );
        }

        TraversalSpecification spec  = findTraversalSpecificationOrThrow( start, theTraversalSpecification );

        MeshObjectSet ret = start.traverse( spec );
        return ret;
    }

    /**
     * String form of the start MeshObject's identifier.
     */
    protected String theStartObject;

    /**
     * Name of the bean that contains the start MeshObject.
     */
    protected String theStartObjectName;
    
    /**
     * The TraversalSpecification to take.
     */
    protected String theTraversalSpecification;
}
