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

import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Instructions to rippleCreate or rippleResynchronize a NetMeshObject.
 */
public class RippleInstructions
        implements
            CanBeDumped
{
    /**
     * Factory method.
     * 
     * @param externalized the externalized representation of the NetMeshObject
     * @return the created RippleInstructions
     */
    public static RippleInstructions create(
            ExternalizedNetMeshObject externalized )
    {
        RippleInstructions ret = new RippleInstructions( externalized );
        return ret;
    }
    
    /**
     * Private constructor for subclasses only, use factory method.
     * 
     * @param externalized the externalized representation of the NetMeshObject
     */
    protected RippleInstructions(
            ExternalizedNetMeshObject externalized )
    {
        theExternalizedNetMeshObject = externalized;
    }

    /**
     * Obtain the externalized representation of the NetMeshObject
     * 
     * @return externalized representation
     */
    public ExternalizedNetMeshObject getExternalizedNetMeshObject()
    {
        return theExternalizedNetMeshObject;
    }
    
    /**
     * Set the correct Proxies for the NetMeshObject.
     * 
     * @param newValue the correct Proxies
     */
    public void setProxies(
            Proxy [] newValue )
    {
        theProxies = newValue;
    }
    
    /**
     * Obtain the correct Proxies for the NetMeshObject.
     * 
     * @return the correct Proxies
     */
    public Proxy [] getProxies()
    {
        return theProxies;
    }

    /**
     * Set the index, into the array of Proxies, of the Proxy towards the
     * update rights.
     * 
     * @param newValue the index, or -1 if none
     */
    public void setProxyTowardsLockIndex(
            int newValue )
    {
        theProxyTowardsLockIndex = newValue;
    }
    
    /**
     * Obtain the index, into the array of Proxies, of the Proxy towards the
     * update rights.
     * 
     * @return the index, or -1 if none
     */
    public int getProxyTowardsLockIndex()
    {
        return theProxyTowardsLockIndex;
    }

    /**
     * Set the index, into the array of Proxies, of the Proxy towards the
     * home replica.
     * 
     * @param newValue the index, or -1 if none
     */
    public void setProxyTowardsHomeIndex(
            int newValue )
    {
        theProxyTowardsHomeIndex = newValue;
    }
    
    /**
     * Obtain the index, into the array of Proxies, of the Proxy towards the
     * home replica.
     * 
     * @return the index, or -1 if none
     */
    public int getProxyTowardsHomeIndex()
    {
        return theProxyTowardsHomeIndex;
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
                    "theExternalizedNetMeshObject",
                    "theProxies",
                    "theProxyTowardsLockIndex",
                    "theProxyTowardsHomeIndex"
                },
                new Object[] {
                    theExternalizedNetMeshObject,
                    theProxies,
                    theProxyTowardsLockIndex,
                    theProxyTowardsHomeIndex
                } );
    }
    
    /**
     * The externalized representation of the NetMeshObject.
     */
    protected ExternalizedNetMeshObject theExternalizedNetMeshObject;
    
    /**
     * The correct Proxies.
     */
    protected Proxy [] theProxies;
    
    /**
     * The index into theProxies, of the Proxy towards the update rights. -1 indicates
     * that this replica has update rights.
     */
    protected int theProxyTowardsLockIndex = -1;
    
    /**
     * The index into theProxies, of the Proxy towards the home replica. -1 indicates
     * that this replica has the home replica status.
     */
    protected int theProxyTowardsHomeIndex = -1;
}
