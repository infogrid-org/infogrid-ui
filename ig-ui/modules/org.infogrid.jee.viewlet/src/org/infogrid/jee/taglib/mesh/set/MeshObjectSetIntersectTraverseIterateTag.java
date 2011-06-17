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
 * Iterate over a <code>MeshObjectSet</code> that is the intersection of two MeshObjectSets
 * determined by traversing from two start
 * <code>MeshObject</code>s via two <code>TraversalSpecification</code>s.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectSetIntersectTraverseIterateTag
    extends
        AbstractMeshObjectSetIterateTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectSetIntersectTraverseIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStartObject1            = null;
        theStartObject1Name        = null;
        theTraversalSpecification1 = null;
        theStartObject2            = null;
        theStartObject2Name        = null;
        theTraversalSpecification2 = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the startObject1 property.
     *
     * @return value of the startObject1 property
     * @see #setStartObject1
     */
    public final Object getStartObject1()
    {
        return theStartObject1;
    }

    /**
     * Set value of the startObject1 property.
     *
     * @param newValue new value of the startObject1 property
     * @see #getStartObject1
     */
    public final void setStartObject1(
            Object newValue )
    {
        theStartObject1 = newValue;
    }

    /**
     * Obtain value of the startObject1Name property.
     *
     * @return value of the startObject1Name property
     * @see #setStartObject1Name
     */
    public final String getStartObject1Name()
    {
        return theStartObject1Name;
    }

    /**
     * Set value of the startObject1Name property.
     *
     * @param newValue new value of the startObject1Name property
     * @see #getStartObject1Name
     */
    public final void setStartObject1Name(
            String newValue )
    {
        theStartObject1Name = newValue;
    }

    /**
     * Obtain value of the traversalSpecification1 property.
     *
     * @return value of the traversalSpecification1 property
     * @see #setTraversalSpecification1
     */
    public final String getTraversalSpecification1()
    {
        return theTraversalSpecification1;
    }

    /**
     * Set value of the traversalSpecification property.
     *
     * @param newValue new value of the traversalSpecification property
     * @see #getTraversalSpecification
     */
    public final void setTraversalSpecification1(
            String newValue )
    {
        theTraversalSpecification1 = newValue;
    }

    /**
     * Obtain value of the startObject2 property.
     *
     * @return value of the startObject2 property
     * @see #setStartObject2
     */
    public final Object getStartObject2()
    {
        return theStartObject2;
    }

    /**
     * Set value of the startObject2 property.
     *
     * @param newValue new value of the startObject2 property
     * @see #getStartObject2
     */
    public final void setStartObject2(
            Object newValue )
    {
        theStartObject2 = newValue;
    }

    /**
     * Obtain value of the startObject2Name property.
     *
     * @return value of the startObject2Name property
     * @see #setStartObject2Name
     */
    public final String getStartObject2Name()
    {
        return theStartObject2Name;
    }

    /**
     * Set value of the startObject2Name property.
     *
     * @param newValue new value of the startObject2Name property
     * @see #getStartObject2Name
     */
    public final void setStartObject2Name(
            String newValue )
    {
        theStartObject2Name = newValue;
    }

    /**
     * Obtain value of the traversalSpecification2 property.
     *
     * @return value of the traversalSpecification2 property
     * @see #setTraversalSpecification2
     */
    public final String getTraversalSpecification2()
    {
        return theTraversalSpecification2;
    }

    /**
     * Set value of the traversalSpecification property.
     *
     * @param newValue new value of the traversalSpecification property
     * @see #getTraversalSpecification
     */
    public final void setTraversalSpecification2(
            String newValue )
    {
        theTraversalSpecification2 = newValue;
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
        MeshObject             start1 = lookupMeshObjectOrThrow( "startObject1", theStartObject1, "startObject1Name", theStartObject1Name );
        TraversalSpecification spec1  = findTraversalSpecificationOrThrow( start1, theTraversalSpecification1 );

        MeshObject             start2 = lookupMeshObjectOrThrow( "startObject2", theStartObject2, "startObject2Name", theStartObject2Name );
        TraversalSpecification spec2  = findTraversalSpecificationOrThrow( start2, theTraversalSpecification2 );

        MeshObjectSet set1 = start1.traverse( spec1 );
        MeshObjectSet set2 = start2.traverse( spec2 );

        MeshObjectSet ret = set1.getFactory().createImmutableMeshObjectSetIntersection( set1, set2 );

        return ret;
    }

    /**
     * The first start MeshObject.
     */
    protected Object theStartObject1;

    /**
     * Name of the bean that contains the first start MeshObject.
     */
    protected String theStartObject1Name;

    /**
     * The first TraversalSpecification to take.
     */
    protected String theTraversalSpecification1;

    /**
     * The second start MeshObject.
     */
    protected Object theStartObject2;

    /**
     * Name of the bean that contains the second start MeshObject.
     */
    protected String theStartObject2Name;

    /**
     * The second TraversalSpecification to take.
     */
    protected String theTraversalSpecification2;
}
