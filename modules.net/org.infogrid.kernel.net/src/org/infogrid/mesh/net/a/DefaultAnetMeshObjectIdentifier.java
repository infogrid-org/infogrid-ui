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

package org.infogrid.mesh.net.a;

import org.infogrid.mesh.a.DefaultAMeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;

/**
 * Implements NetMeshObjectIdentifier for the Anet implementation.
 */
public class DefaultAnetMeshObjectIdentifier
        extends
            DefaultAMeshObjectIdentifier
        implements
            NetMeshObjectIdentifier
{
    /**
     * Factory method.
     *
     * @param factory the DefaultAnetMeshObjectIdentifierFactory that created this identifier
     * @param baseIdentifier identifier of the NetMeshBase relative to which a localId is specified
     * @param localId the localId of the to-be-created DefaultAnetMeshObjectIdentifier
     * @return the created DefaultAnetMeshObjectIdentifier
     * @throws IllegalArgumentException thrown if a non-null localId contains a period.
     */
    public static DefaultAnetMeshObjectIdentifier create(
            DefaultAnetMeshObjectIdentifierFactory factory,
            NetMeshBaseIdentifier                  baseIdentifier,
            String                                 localId )
    {
        if( baseIdentifier == null ) {
            throw new NullPointerException();
        }
        
        if( localId != null && factory.treatAsGlobalIdentifier( localId )) {
            throw new IllegalArgumentException( "DefaultAnetMeshObjectIdentifier's localId must not contain a period: " + localId );
        }
        
        if( localId != null && localId.length() == 0 ) {
            localId = null;
        }
        
        return new DefaultAnetMeshObjectIdentifier( factory, baseIdentifier, localId );
    }

    /**
     * Private constructor.
     * 
     * @param factory the DefaultAnetMeshObjectIdentifierFactory that created this identifier
     * @param baseIdentifier identifier of the NetMeshBase relative to which a localId is specified
     * @param localId the localId of the to-be-created MeshObjectIdentifier
     */
    protected DefaultAnetMeshObjectIdentifier(
            DefaultAnetMeshObjectIdentifierFactory factory,
            NetMeshBaseIdentifier                  baseIdentifier,
            String                                 localId )
    {
        super( factory, localId );

        theNetMeshBaseIdentifier = baseIdentifier;
    }

    /**
     * Obtain the factory that created this identifier.
     *
     * @return the factory
     */
    @Override
    public DefaultAnetMeshObjectIdentifierFactory getFactory()
    {
        return (DefaultAnetMeshObjectIdentifierFactory) super.getFactory();
    }

    /**
     * Obtain the identifier of the NetMeshBase in which this NetMeshObjectIdentifier was allocated.
     *
     * @return the dentifier of the NetMeshBase
     */
    public NetMeshBaseIdentifier getNetMeshBaseIdentifier()
    {
        return theNetMeshBaseIdentifier;
    }

    /**
     * Obtain an external form for this NetMeshObjectIdentifier, similar to
     * URL's getExternalForm(). This returns an empty String for local home objects.
     *
     * @return external form of this NetMeshObjectIdentifier
     */
    @Override
    public String toExternalForm()
    {
        if( theLocalId != null && theLocalId.length() > 0 ) {
            StringBuilder buf = new StringBuilder();
            buf.append( theNetMeshBaseIdentifier.toExternalForm() );
            buf.append( SEPARATOR );
            buf.append( theLocalId );
            return buf.toString();
        } else {
            return theNetMeshBaseIdentifier.toExternalForm();
        }
    }

    /**
     * Obtain the external form just of the local part of the NetMeshObjectIdentifier.
     * 
     * @return the local external form
     */
    public String toLocalExternalForm()
    {
        if( theLocalId == null || theLocalId.length() == 0 ) {
            return "";
        } else {
            return SEPARATOR + theLocalId;
        }
    }

    /**
     * The Identifier for the NetMeshBase in which this NetMeshObjectIdentifier was allocated.
     */
    protected NetMeshBaseIdentifier theNetMeshBaseIdentifier;

    /**
     * Separator between NetMeshBaseIdentifier and local id.
     */
    public static final char SEPARATOR = '#';
}
