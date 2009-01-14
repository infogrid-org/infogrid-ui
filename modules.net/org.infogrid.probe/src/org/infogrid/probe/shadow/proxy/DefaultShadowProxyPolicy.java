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

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.proxy.AbstractProxyPolicy;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.proxy.ProxyProcessingInstructions;
import org.infogrid.meshbase.net.proxy.RippleInstructions;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRelationshipEvent;
import org.infogrid.meshbase.net.xpriso.ParserFriendlyXprisoMessage;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.logging.Log;

/**
 * Default implementation of ProxyPolicy for DefaultShadowProxies.
 */
public class DefaultShadowProxyPolicy
        extends
            AbstractProxyPolicy
{
    private static final Log log = Log.getLogInstance( DefaultShadowProxyPolicy.class );

    /**
     * Factory method.
     * 
     * @return the created DefaultShadowProxyPolicy
     */
    public static DefaultShadowProxyPolicy create()
    {
        return new DefaultShadowProxyPolicy( null, false ); // needs to point to other MeshBases in order to support ForwardReference resolution.
    }
    
    /**
     * Constructor.
     * 
     * @param coherence the CoherenceSpecification used by this ProxyPolicy
     * @param pointsReplicasToItself if true, new Replicas will be created by a branch from the local Replica
     */
    protected DefaultShadowProxyPolicy(
            CoherenceSpecification coherence,
            boolean                pointsReplicasToItself )
    {
        super( coherence, pointsReplicasToItself );
    }

    /**
     * Determine the ProxyProcessingInstructions for obtaining one or more
     * replicas via this Proxy.
     * 
     * @param paths the NetMeshObjectAccessSpecification for finding the NetMeshObjects to be replicated
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    @Override
    public ProxyProcessingInstructions calculateForObtainReplicas(
            NetMeshObjectAccessSpecification [] paths,
            long                                duration,
            Proxy                               proxy )
    {
        throw new UnsupportedOperationException( "A Shadow should not invoke this" );
    }

    /**
     * Determine the ProxyProcessingInstructions for obtaining one or more
     * locks via this Proxy.
     * 
     * @param localReplicas the local replicas for which the lock should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    @Override
    public ProxyProcessingInstructions calculateForTryToObtainLocks(
            NetMeshObject [] localReplicas,
            long             duration,
            Proxy            proxy )
    {
        throw new UnsupportedOperationException( "A Shadow should not invoke this" );
    }

    /**
     * Determine the ProxyProcessingInstructions for pushing one or more
     * locks via this Proxy.
     * 
     * @param localReplicas the local replicas for which the lock should be obtained
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    @Override
    public ProxyProcessingInstructions calculateForTryToPushLocks(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration,
            Proxy            proxy )
    {
        throw new UnsupportedOperationException( "A Shadow should not invoke this" );
    }

    /**
     * Determine the ProxyProcessingInstructions for obtaining one or more
     * home replica statuses via this Proxy.
     * 
     * @param localReplicas the local replicas for which the home replica statuses should be obtained
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    @Override
    public ProxyProcessingInstructions calculateForTryToObtainHomeReplicas(
            NetMeshObject [] localReplicas,
            long             duration,
            Proxy            proxy )
    {
        throw new UnsupportedOperationException( "A Shadow should not invoke this" );
    }
    
    /**
     * Determine the ProxyProcessingInstructions for pushing one or more
     * home replica statuses via this Proxy.
     * 
     * @param localReplicas the local replicas for which the home replica statuses should be obtained
     * @param isNewProxy if true, the the NetMeshObject did not replicate via this Proxy prior to this call.
     *         The sequence in the array is the same sequence as in localReplicas.
     * @param duration the duration, in milliseconds, that the caller is willing to wait to perform the request. -1 means "use default".
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    @Override
    public ProxyProcessingInstructions calculateForTryToPushHomeReplicas(
            NetMeshObject [] localReplicas,
            boolean []       isNewProxy,
            long             duration,
            Proxy            proxy )
    {
        throw new UnsupportedOperationException( "A Shadow should not invoke this" );
    }

    /**
     * Process the incoming request: requested home replicas.
     * 
     * @param proxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param outgoing the outgoing message being assembled
     */
    @Override
    protected void processIncomingRequestedHomeReplicas(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing )
    {
        // do absolutely nothing
    }

    /**
     * Process the incoming request: conveyed objects. The shadow only accepts any objects
     * if the Probe is a WritableProbe and the home is being pushed here.
     * 
     * @param incomingProxy the incoming Proxy
     * @param ret the instructions being assembled assembled
     * @param perhapsOutgoing the outgoing message being assembled
     * @param isResponseToOngoingQuery if true, this message was sent in response to a query
     */
    @Override
    protected void processIncomingConveyedObjects(
            Proxy                                         incomingProxy,
            ProxyProcessingInstructions                   ret,
            CreateWhenNeeded<ParserFriendlyXprisoMessage> perhapsOutgoing,
            boolean                                       isResponseToOngoingQuery )
    {
        XprisoMessage               incoming    = ret.getIncomingXprisoMessage();
        ShadowMeshBase              theMeshBase = (ShadowMeshBase) incomingProxy.getNetMeshBase();
    
    // conveyed objects
        NetMeshObjectIdentifier [] push  = incoming.getPushHomeReplicas();
        NetMeshObjectIdentifier [] locks = incoming.getPushLockObjects();
        
        if( push == null ) {
            push = new NetMeshObjectIdentifier[0];
        }
        if( locks == null ) {
            locks = new NetMeshObjectIdentifier[0];
        }
        
        boolean isWritableProbe = theMeshBase.usesWritableProbe();

        if( arrayHasContent( incoming.getConveyedMeshObjects())) {
            for( ExternalizedNetMeshObject current : incoming.getConveyedMeshObjects() ) {
                
                NetMeshObjectIdentifier identifier = current.getIdentifier();
                NetMeshObject           already    = theMeshBase.findMeshObjectByIdentifier( identifier );
                
                boolean wantIt;
                boolean gotLock = ArrayHelper.isIn( identifier, locks, true );
                boolean gotHome = ArrayHelper.isIn( identifier, push,  true );
                
                // determine whether we want it or not
                if( already != null ) {
                    // don't need it
                    wantIt = false;

                } else if( !isWritableProbe ) {
                    // we are certain we don't want it
                    wantIt = false;
                    
                } else {
                    // perhaps we want it
                    wantIt = gotHome;
                }
                
                // now do the right thing
                if( wantIt ) {
                    RippleInstructions ripple = RippleInstructions.create( current );
                    ripple.setProxies( new Proxy[] { incomingProxy } );
                    ripple.setProxyTowardsHomeIndex( gotHome ? -1 : 0 );
                    ripple.setProxyTowardsLockIndex( gotLock ? -1 : 0 );
                    ret.addRippleCreate( ripple );
                    
                } else {
                    // don't want it
                    ParserFriendlyXprisoMessage outgoing = perhapsOutgoing.obtain();
                    if( gotLock ) {
                        outgoing.addPushLockObject( identifier );
                    }
                    if( gotHome ) {
                        outgoing.addPushHomeReplica( identifier );
                    }
                    outgoing.addRequestedCanceledObject( identifier );
                }
            }
        }
    }

    /**
     * Given a committed Transaction, determine the ProxyProcessingInstructions for notifying
     * our partner Proxy.
     * 
     * @param tx the Transaction
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @return the calculated ProxyProcessingInstructions, or null
     */
    @Override
    public ProxyProcessingInstructions calculateForTransactionCommitted(
            Transaction tx,
            Proxy       proxy )
    {
         // This is overridden simply to make setting of shadow-specific breakpoints easier
         ProxyProcessingInstructions ret = super.calculateForTransactionCommitted( tx, proxy );
         
         return ret;
    }

     /**
     * Helper method to determine whether to accept an incoming relationship-related event.
     * Can be overridden in subclasses.
     * 
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @param e the event
     * @return true if we accept the event
     */
    @Override
    protected boolean acceptRelationshipEvent(
            Proxy                          proxy,
            NetMeshObjectRelationshipEvent e )
    {
        e.setResolver( proxy.getNetMeshBase() );

        MeshObject source   = e.getAffectedMeshObject();
        MeshObject neighbor = e.getNeighborMeshObject();
        
        return source != null && neighbor != null;
    }

    /**
     * Helper method to determine whether to accept an incoming relationship-related event.
     * Can be overridden in subclasses.
     * 
     * @param proxy the Proxy on whose behalf the ProxyProcessingInstructions are constructed
     * @param e the event
     * @return true if we accept the event
     */
    @Override
    protected boolean acceptRelationshipEvent(
            Proxy                               proxy,
            NetMeshObjectEquivalentsChangeEvent e )
    {
        // FIXME is this right?
        
        e.setResolver( proxy.getNetMeshBase() );

        MeshObject    source      = e.getAffectedMeshObject();
        MeshObject [] equivalents = e.getDeltaValue();
        
        if( source == null ) {
            return false;
        }
        for( MeshObject current : equivalents ) {
            if( current == null ) {
                return false;
            }
        }
        return true;
    }
}