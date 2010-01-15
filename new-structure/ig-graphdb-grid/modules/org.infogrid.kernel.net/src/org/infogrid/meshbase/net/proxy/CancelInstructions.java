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
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * <p>Captures a set of instructions to cancel leases of specified NetMeshObjects that were
 * obtained via another, local Proxy.</p>
 * <p>This collects all instructions related to the same Proxy. A different instance of this
 * object must be used for canceling leases of NetMeshObjects from another Proxy.</p>
 */
public class CancelInstructions
        implements
            CanBeDumped
{
    /**
     * Factory method.
     * 
     * @param proxy the Proxy through which the leases were obtained
     * @return the created CancelInstructions
     */
    public static CancelInstructions create(
            Proxy proxy )
    {
        CancelInstructions ret = new CancelInstructions( proxy );
        return ret;
    }

    /**
     * Private constructor for subclasses only, use factory method.
     * 
     * @param proxy the Proxy through which the leases were obtained
     */
    protected CancelInstructions(
            Proxy proxy )
    {
        theProxy = proxy;
    }

    /**
     * Add a NetMeshObject whose lease shall be canceled.
     * 
     * @param toAdd the NetMeshObject
     */
    public void addNetMeshObject(
            NetMeshObject toAdd )
    {
        theNetMeshObjects.add( toAdd );
    }
    
    /**
     * Obtain the NetMeshObjects whose lease shall be canceled.
     * 
     * @return return the NetMeshObjects
     */
    public NetMeshObject [] getNetMeshObjects()
    {
        return ArrayHelper.copyIntoNewArray( theNetMeshObjects, NetMeshObject.class );
    }
    
    /**
     * Obtain the Proxy via which the leases were obtained.
     * 
     * @return the Proxy
     */
    public Proxy getProxy()
    {
        return theProxy;
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
                    "theProxy",
                    "theNetMeshObjects"
                },
                new Object[] {
                    theProxy,
                    theNetMeshObjects
                } );
    }
    
    /**
     * The NetMeshObjects whose leases shall be canceled.
     */
    protected ArrayList<NetMeshObject> theNetMeshObjects = new ArrayList<NetMeshObject>();
    
    /**
     * The Proxy via which the leases were obtained.
     */
    protected Proxy theProxy;
}
