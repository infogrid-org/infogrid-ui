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

package org.infogrid.probe.shadow.proxy;

import org.infogrid.comm.ReceivingMessageEndpoint;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.proxy.AbstractProxy;
import org.infogrid.meshbase.net.proxy.ProxyPolicy;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.net.proxy.ProxyMessageEndpoint;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.util.logging.Log;

/**
 * A Proxy that is used only by ShadowMeshBases.
 */
public class DefaultShadowProxy
        extends
            AbstractProxy
{
    private static final Log log = Log.getLogInstance( DefaultShadowProxy.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param ep the communications endpoint
     * @param mb the MeshBase this Proxy belongs to
     * @return the created DefaultShadowProxy
     */
    public static DefaultShadowProxy create(
            ProxyMessageEndpoint ep,
            NetMeshBase        mb )
    {
        DefaultShadowProxyPolicy policy = DefaultShadowProxyPolicy.create(); // in the future, this should become configurable
        DefaultShadowProxy       ret    = new DefaultShadowProxy( ep, mb, policy );

        if( log.isDebugEnabled() ) {
            log.debug( "Created " + ret, new RuntimeException( "marker" ));
        }
        return ret;
    }

    /**
     * Factory method to restore from storage.
     *
     * @param ep the communications endpoint
     * @param mb the MeshBase this Proxy belongs to
     * @param isPlaceholder if true, this is a placeholder Proxy for the purposes of processing forward references
     * @param timeCreated the timeCreated to use
     * @param timeUpdated the timeUpdated to use
     * @param timeRead the timeRead to use
     * @param timeExpires the timeExpires to use
     * @return the created DefaultShadowProxy
     */
    public static DefaultShadowProxy restoreProxy(
            ProxyMessageEndpoint ep,
            NetMeshBase        mb,
            boolean            isPlaceholder,
            long               timeCreated,
            long               timeUpdated,
            long               timeRead,
            long               timeExpires )
    {
        DefaultShadowProxyPolicy policy = DefaultShadowProxyPolicy.create(); // in the future, this should become configurable
        DefaultShadowProxy       ret = new DefaultShadowProxy( ep, mb, policy, isPlaceholder, timeCreated, timeUpdated, timeRead, timeExpires );

        if( log.isDebugEnabled() ) {
            log.debug( "Created " + ret, new RuntimeException( "marker" ));
        }
        return ret;
    }
    
    /**
     * Constructor.
     *
     * @param ep the communications endpoint
     * @param mb the MeshBase this Proxy belongs to
     * @param policy the ProxyPolicy to use
     */
    protected DefaultShadowProxy(
            ProxyMessageEndpoint ep,
            NetMeshBase        mb,
            ProxyPolicy        policy )
    {
        super( ep, mb, policy );
        
        theIsPlaceholder = false;
    }

    /**
     * Constructor.
     *
     * @param ep the communications endpoint
     * @param mb the MeshBase this Proxy belongs to
     * @param policy the ProxyPolicy to use
     * @param isPlaceholder if true, this is a placeholder Proxy for the purposes of processing forward references
     * @param timeCreated the timeCreated to use
     * @param timeUpdated the timeUpdated to use
     * @param timeRead the timeRead to use
     * @param timeExpires the timeExpires to use
     */
    protected DefaultShadowProxy(
            ProxyMessageEndpoint ep,
            NetMeshBase        mb,
            ProxyPolicy        policy,
            boolean            isPlaceholder,
            long               timeCreated,
            long               timeUpdated,
            long               timeRead,
            long               timeExpires )
    {
        super( ep, mb, policy );
        
        theIsPlaceholder = isPlaceholder;

        theTimeCreated = timeCreated;
        theTimeUpdated = timeUpdated;
        theTimeRead    = timeRead;
        theTimeExpires = timeExpires;
    }
    
    /**
     * Internal implementation method for messageReceived. Overriding this makes
     * debugging easier.
     *
     * @param endpoint the MessageEndpoint through which the message arrived
     * @param incoming the incoming message
     */
    @Override
    protected void internalMessageReceived(
            ReceivingMessageEndpoint<XprisoMessage> endpoint,
            XprisoMessage                           incoming )
    {
        theMeshBaseIsDirty = false;

        super.internalMessageReceived( endpoint, incoming );
        
        if( theMeshBaseIsDirty ) {
            ((StagingMeshBase)theMeshBase).flushMeshBase();
        }
    }

    /**
     * Hook that enables subclasses to take note which MeshObjects in the MeshBase have
     * been modified in response to message processing.
     * 
     * @param modified the NetMeshObject that was modified
     */
    @Override
    protected void meshObjectModifiedDuringMessageProcessing(
            NetMeshObject modified )
    {
        theMeshBaseIsDirty = true;
    }
    
    /**
      * Indicates that a Transaction has been committed.
      *
      * @param theTransaction the Transaction that was committed
      */
    @Override
    public void transactionCommitted(
            Transaction theTransaction )
    {
        if( theIsPlaceholder ) {
            // don't do anything
        } else if( theEndpoint == null ) {
            log.warn( "This should be a placeholder Proxy: " + this );
            // don't do anything anyway
        } else {
            super.transactionCommitted( theTransaction );
        }
    }

    /**
     * Set the placeholder flag.
     *
     * @param newValue the new value 
     */
    public void setIsPlaceholder(
            boolean newValue )
    {
        theIsPlaceholder = newValue;
    }
    
    /**
     * Obtain the placeholder flag.
     * 
     * @return the placeholder flag
     */
    public boolean getIsPlaceholder()
    {
        return theIsPlaceholder;
    }

    /**
     * The placeholder flag.
     */
    protected boolean theIsPlaceholder;
    
    /**
     * Used during incoming message processing to determine whether changes were made
     * to the ShadowMeshBase that necessitate that the ShadowMeshBase is written back
     * to disk.
     */
    protected boolean theMeshBaseIsDirty;
}
