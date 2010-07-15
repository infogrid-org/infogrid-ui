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
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.ByPropertyValueSorter;
import org.infogrid.mesh.set.DefaultMeshObjectSorter;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSorter;
import org.infogrid.model.primitives.PropertyType;

/**
 * Factors out common code for tags that iterate over the content of a <code>MeshObjectSet</code>.
 */
public abstract class AbstractMeshObjectSetIterateTag
    extends
        AbstractSetIterateTag<MeshObject>
{
    /**
     * Constructor.
     */
    protected AbstractMeshObjectSetIterateTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theOrderBy           = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the orderBy property.
     *
     * @return value of the orderBy property
     * @see #setOrderBy
     */
    public final String getOrderBy()
    {
        return theOrderBy;
    }

    /**
     * Set value of the orderBy property.
     *
     * @param newValue new value of the orderBy property
     * @see #getOrderBy
     */
    public final void setOrderBy(
            String newValue )
    {
        theOrderBy = newValue;
    }

    /**
     * This method is defined by subclasses to provide the set over which we iterate.
     *
     * @param reverse if true, iterate in the reverse direction
     * @return the set to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected Iterable<MeshObject> determineSet(
            boolean reverse )
        throws
            JspException,
            IgnoreException
    {
        MeshObjectSet theSet = determineMeshObjectSet();
        if( theSet == null ) {
            // can be, if ignore=true
            return null;
        }

        MeshObjectSorter sorter;
        if( theOrderBy != null ) {
            PropertyType orderBy = (PropertyType) findMeshTypeByIdentifierOrThrow( theOrderBy );

            sorter = ByPropertyValueSorter.create( orderBy, reverse );

        } else {
            if( !reverse ) {
                sorter = DefaultMeshObjectSorter.BY_USER_VISIBLE_STRING;
            } else {
                sorter = DefaultMeshObjectSorter.BY_REVERSE_USER_VISIBLE_STRING;
            }
        }

        theSet = theSet.getFactory().createOrderedImmutableMeshObjectSet( theSet, sorter );

        return theSet;
    }

    /**
     * This method is defined by subclasses to provide the MeshObjectSet over which we iterate.
     *
     * @return the set to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract MeshObjectSet determineMeshObjectSet()
        throws
            JspException,
            IgnoreException;

    /**
     * String containing the identifier of the PropertyType by which we sort the set, if any.
     */
    protected String theOrderBy;
}
