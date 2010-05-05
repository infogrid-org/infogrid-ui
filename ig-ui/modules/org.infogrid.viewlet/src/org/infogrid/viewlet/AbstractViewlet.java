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

package org.infogrid.viewlet;

import java.text.ParseException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;

/**
 * Factors out commonly used functionality for Viewlets.
 */
public abstract class AbstractViewlet
        extends
            AbstractObjectInContext
        implements
            Viewlet
{
    /**
     * Constructor, for subclasses only.
     * 
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param parent the parent Viewlet, if any
     * @param c the application context
     */
    protected AbstractViewlet(
            AbstractViewedMeshObjects viewed,
            Viewlet                   parent,
            Context                   c )
    {
        super( c );

        theViewedMeshObjects = viewed;
        theParentViewlet     = parent;
    }
    
    /**
     * Obtain a String, to be shown to the user, that identifies this Viewlet to the user.
     * This default implementation can be overridden by subclasses.
     *
     * @return a String
     */
    public String getUserVisibleName()
    {
        return getClass().getName();
    }

    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewet's name
     */
    public String getName()
    {
        return getClass().getName();
    }

    /**
      * The Viewlet is being instructed to view certain objects, which are packaged as MeshObjectsToView.
      *
      * @param toView the MeshObjects to view
      * @throws CannotViewException thrown if this Viewlet cannot view these MeshObjects
      */
    public void view(
            MeshObjectsToView toView )
        throws
            CannotViewException
    {
        theViewedMeshObjects.updateFrom( toView );
    }
    
    /**
      * Set the REST-ful subject for this Viewlet. This is a simplified version of {@link #view( MeshObjectsToView )}.
      *
      * @param toView the MeshObject to view
      * @throws CannotViewException thrown if this Viewlet cannot view this MeshObject
      * @deprecated for consistency, only <code>view( MeshObjectsToView )</code> should be used
      */
    public void setSubject(
            MeshObject toView )
        throws
            CannotViewException
    {
        view( MeshObjectsToView.create( toView, null ));
    }
    
    /**
     * Obtain the REST-ful subject.
     *
     * @return the subject
     */
    public MeshObject getSubject()
    {
        return theViewedMeshObjects.getSubject();
    }

    /**
     * Obtain the TraversalSpecification that the Viewlet currently uses.
     * 
     * @return the TraversalSpecification that the Viewlet currently uses
     */
    public TraversalSpecification getTraversalSpecification()
    {
        return theViewedMeshObjects.getTraversalSpecification();
    }

    /**
     * Obtain the set of TraversalPaths that the Viewlet currently uses to the Objects, if any.
     *
     * @return the TraversalPathSet
     */
    public TraversalPathSet getTraversalPathSet()
    {
        return theViewedMeshObjects.getTraversalPathSet();
    }

    /**
     * Obtain the Objects.
     * 
     * @return the set of Objects, which may be empty
     */
    public MeshObjectSet getReachedObjects()
    {
        return theViewedMeshObjects.getReachedObjects();
    }

    /**
      * Obtain the MeshObjects that this Viewlet is currently viewing, plus
      * context information. This method will return the same instance of ViewedMeshObjects
      * during the lifetime of the Viewlet.
      *
      * @return the ViewedMeshObjects
      */
    public ViewedMeshObjects getViewedObjects()
    {
        return theViewedMeshObjects;
    }

    /**
     * Obtain the Viewlet in which this Viewlet is contained, if any.
     *
     * @return the parent Viewlet
     */
    public Viewlet getParentViewlet()
    {
        return theParentViewlet;
    }

    /**
     * Helper method to resolve a MeshObject from a MeshObjectIdentifier in String form.
     *
     * @param identifier the MeshObjectIdentifier in String form
     * @return the found MeshObject, or null if not found
     * @throws ParseException the String was malformed
     * @throws MeshObjectAccessException accessing the MeshObject failed
     * @throws NotPermittedException the caller does not have sufficient permissions to access the MeshObject
     */
    protected MeshObject resolveMeshObject(
            String identifier )
        throws
            ParseException,
            MeshObjectAccessException,
            NotPermittedException
    {
        MeshBase             mb  = theViewedMeshObjects.getMeshBase();
        MeshObjectIdentifier id  = mb.getMeshObjectIdentifierFactory().fromExternalForm( identifier );
        MeshObject           ret = mb.accessLocally( id );

        return ret;
    }

    /**
     * The objects being viewed.
     */
    protected AbstractViewedMeshObjects theViewedMeshObjects;

    /**
     * The parent Viewlet, if any.
     */
    protected Viewlet theParentViewlet;
}
