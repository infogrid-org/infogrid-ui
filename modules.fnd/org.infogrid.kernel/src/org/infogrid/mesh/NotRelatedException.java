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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.mesh;

import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.StringHelper;

/**
 * This Exception is thrown if two MeshObjects are to become unrelated, but are not
 * currently related.
 */
public class NotRelatedException
        extends
            IllegalOperationTypeException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param mb the MeshBase in which this Exception was created
     * @param originatingMeshBaseIdentifier the MeshBaseIdentifier of the MeshBase in which this Exception was created
     * @param meshObject the first MeshObject that was unrelated, if available
     * @param meshObjectIdentifier the MeshObjectIdentifier for the first MeshObject that was unrelated
     * @param other the MeshObject at the other end of the non-existing relationship, if available
     * @param otherIdentifier the MeshObjectIdentifier for the MeshObject at the other end of the non-existing relationship, if available
     */
    public NotRelatedException(
            MeshBase             mb,
            MeshBaseIdentifier   originatingMeshBaseIdentifier,
            MeshObject           meshObject,
            MeshObjectIdentifier meshObjectIdentifier,
            MeshObject           other,
            MeshObjectIdentifier otherIdentifier )
    {
        super( mb, originatingMeshBaseIdentifier, meshObject, meshObjectIdentifier );
        
        theOther                = other;
        theOtherIdentifier      = otherIdentifier;
    }

    /**
     * More convenient simple constructor for the most common case.
     *
     * @param meshObject the first MeshObject that was unrelated, if available
     * @param other the MeshObject at the other end of the non-existing relationship, if available
     */
    public NotRelatedException(
            MeshObject           meshObject,
            MeshObject           other )
    {
        this(   meshObject.getMeshBase(),
                meshObject.getMeshBase().getIdentifier(),
                meshObject,
                meshObject.getIdentifier(),
                other,
                other.getIdentifier() );
    }

    /**
     * Obtain the MeshObject at the other end of the relationship that did not exist.
     * 
     * @return the other MeshObject
     * @throws MeshObjectAccessException thrown if the MeshObject could not be found
     * @throws NotPermittedException thrown if the caller is not authorized to perform this operation
     * @throws IllegalStateException thrown if no resolving MeshBase is available
     */
    public synchronized MeshObject getOtherMeshObject()
        throws
            MeshObjectAccessException,
            NotPermittedException,
            IllegalStateException
    {
        if( theOther == null ) {
            theOther = resolve( theOtherIdentifier );
        }
        return theOther;
    }

    /**
     * Obtain the MeshObjectIdentifier of the MeshObject at the other end of the relationship that did not exist.
     *
     * @return the MeshObjectIdentifier
     */
    public MeshObjectIdentifier getOtherMeshObjectIdentifier()
    {
        return theOtherIdentifier;
    }

    /**
     * Return this object in string form, for debugging.
     *
     * @return string form of this object
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theMeshObject",
                    "theMeshObjectIdentifier",
                    "theOther",
                    "theOtherIdentifier",
                },
                new Object[] {
                    theMeshObject,
                    theMeshObjectIdentifier,
                    theOther,
                    theOtherIdentifier
                });
    }

    /**
     * Obtain parameters for the internationalization.
     *
     * @return the parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theMeshObjectIdentifier, theOtherIdentifier };
    }

    /**
     * The MeshObject at the other end of the relationship for which we discovered a violation.
     */
    protected transient MeshObject theOther;

    /**
     * The MeshObjectIdentifier of the MeshObject at the other end of the relationship for which we discovered a violation.
     */
    protected MeshObjectIdentifier theOtherIdentifier;
}

