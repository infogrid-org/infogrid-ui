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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.meshbase.net.proxy;

import java.util.ArrayList;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * <p>Captures a set of instructions to resynchronize leases of specified NetMeshObjects
 *    via another, local Proxy. The Proxy is represented via the NetMeshBaseIdentifier
 *    of the NetMeshBase with which it communicates.</p>
 * <p>This collects all instructions related to the same Proxy. A different instance of this
 *    object must be used for canceling leases of NetMeshObjects from another Proxy.</p>
 */
public class ResynchronizeInstructions
        implements
            CanBeDumped
{
    /**
     * Factory method.
     * 
     * @param proxyIdentifier the NetMeshBaseIdentifier of the Proxy's partner NetMeshBase
     * @return the created ResynchronizeInstructions
     */
    public static ResynchronizeInstructions create(
            NetMeshBaseIdentifier proxyIdentifier )
    {
        ResynchronizeInstructions ret = new ResynchronizeInstructions( proxyIdentifier );
        return ret;
    }

    /**
     * Private constructor for subclasses only, use factory method.
     * 
     * @param proxyIdentifier the NetMeshBaseIdentifier of the Proxy's partner NetMeshBase
     */
    protected ResynchronizeInstructions(
            NetMeshBaseIdentifier proxyIdentifier )
    {
        theProxyIdentifier = proxyIdentifier;
    }

    /**
     * Add the identifier of another NetMeshObject that shall be resynchronized via this Proxy.
     * 
     * @param toAdd identifier of the NetMeshObject
     */
    public void addNetMeshObjectIdentifier(
            NetMeshObjectIdentifier toAdd )
    {
        theNetMeshObjectIdentifiers.add( toAdd );
    }
    
    /**
     * Obtain the identifiers of the NetMeshObjects that shall be resynchronized via this Proxy.
     * 
     * @return the identifiers
     */
    public NetMeshObjectIdentifier [] getNetMeshObjectIdentifiers()
    {
        return ArrayHelper.copyIntoNewArray( theNetMeshObjectIdentifiers, NetMeshObjectIdentifier.class );
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the Proxy's partner NetMeshBase.
     * 
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getProxyIdentifier()
    {
        return theProxyIdentifier;
    }
    
    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theProxyIdentifier",
                    "theNetMeshObjectIdentifiers"
                },
                new Object[] {
                    theProxyIdentifier,
                    theNetMeshObjectIdentifiers
                } );
    }
    
    /**
     * The identifiers of the NetMeshObjects that shall be resynchronized via this Proxy.
     */
    protected ArrayList<NetMeshObjectIdentifier> theNetMeshObjectIdentifiers = new ArrayList<NetMeshObjectIdentifier>();
    
    /**
     * The identifier of the partner NetMeshBase of the Proxy.
     */
    protected NetMeshBaseIdentifier theProxyIdentifier;
}

