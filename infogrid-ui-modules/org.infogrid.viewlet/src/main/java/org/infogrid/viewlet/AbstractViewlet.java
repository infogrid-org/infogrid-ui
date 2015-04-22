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

package org.infogrid.viewlet;

import java.text.ParseException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.ResourceHelper;
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
     * @param viewed the ViewedMeshObjects to use
     * @param c the application context
     */
    protected AbstractViewlet(
            ViewedMeshObjects viewed,
            Context           c )
    {
        super( c );

        theViewedMeshObjects = viewed;
    }
    
    /**
     * Obtain a String, to be shown to the user, that identifies this Viewlet to the user.
     * This default implementation can be overridden by subclasses.
     *
     * @return a String
     */
    public String getUserVisibleName()
    {
        String userVisibleName = ResourceHelper.getInstance( getClass() ).getResourceStringOrDefault( "UserVisibleName", getClass().getName() );
        return userVisibleName;
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
     * Obtain the REST-ful subject.
     *
     * @return the subject
     */
    public MeshObject getSubject()
    {
        return theViewedMeshObjects.getSubject();
    }

    /**
      * Obtain the MeshObjects that this Viewlet is currently viewing, plus
      * context information. This method will return the same instance of ViewedMeshObjects
      * during the lifetime of the Viewlet.
      *
      * @return the ViewedMeshObjects
      */
    public ViewedMeshObjects getViewedMeshObjects()
    {
        return theViewedMeshObjects;
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
        MeshObjectIdentifier id  = mb.getMeshObjectIdentifierFactory().guessFromExternalForm( identifier );
        MeshObject           ret = mb.accessLocally( id );

        return ret;
    }

    /**
     * The objects being viewed.
     */
    protected ViewedMeshObjects theViewedMeshObjects;
}
