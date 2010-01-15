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

package org.infogrid.probe.shadow.a;

import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.a.AnetMeshObject;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseAccessSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.net.a.AnetMeshBase;
import org.infogrid.meshbase.net.a.AnetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.transaction.ForwardReferenceCreatedEvent;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.probe.shadow.proxy.DefaultShadowProxy;
import org.infogrid.util.FactoryException;
import org.infogrid.util.logging.Log;

/**
 * This MeshBaseLifecycleManager overrides the default times for object creation and updates.
 */
public class AStagingMeshBaseLifecycleManager
        extends
            AnetMeshBaseLifecycleManager
        implements
            StagingMeshBaseLifecycleManager
{
    private static final Log log = Log.getLogInstance( AStagingMeshBaseLifecycleManager.class ); // our own, private logger

    /**
     * Factory method. The application developer should not call this or a subclass constructor; use
     * MeshBase.getMeshObjectLifecycleManager() instead.
     * 
     * @return the created AStagingMeshBaseLifecycleManager
     */
    public static AStagingMeshBaseLifecycleManager create()
    {
        return new AStagingMeshBaseLifecycleManager();
    }
    
    /**
     * Constructor. The application developer should not call this or a subclass constructor; use
     * MeshBase.getMeshObjectLifecycleManager() instead.
     */
    protected AStagingMeshBaseLifecycleManager()
    {
        super();
    }
    
    /**
     * Determine the current time for the purpose of assigning the right creation time to a
     * to-be-created MeshObject. This may be overridden in subclasses.
     *
     * @return the creation time to use, in System.currentTimeMillis() format
     */
    @Override
    protected long determineCreationTime()
    {
        AStagingMeshBase realBase = (AStagingMeshBase) theMeshBase;
        
        long ret = realBase.getCurrentUpdateStartedTime();
        return ret;
    }


    /**
     * <p>Create several ForwardReference with zero or more types. Each type may or may not be abstract: as this
     *    creates a ForwardReference, it may resolve in a MeshObject blessed with a subtype.</p>
     *
     * @param pathToObjects identifies the data sources where the MeshObjects can be found
     * @param types the EntityTypes with which the MeshObjects will be blessed
     * @param timeCreateds the time the ForwardReferences was created
     * @param timeUpdateds the time the ForwardReferences was last updated
     * @param timeReads the time the ForwardReferences was last read
     * @param timeExpiress the time the ForwardReferences will expire
     * @param giveUpHomeReplicas if true, the ForwardReferences are willing to give up home replica status
     * @param giveUpLocks if true, the ForwardReferences are willing to give up update rights
     * @param timeoutInMillis the timeout parameter for this call, in milli-seconds. -1 means "use default".
     * @return the created ForwardReferences
     * @throws TransactionException thrown if this method is invoked outside of proper Transaction boundaries
     * @throws MeshObjectIdentifierNotUniqueException thrown if a ForwardReference to the same location has been created already
     */
    @Override
    public AnetMeshObject [] createForwardReferences(
            NetMeshObjectAccessSpecification [] pathToObjects,
            EntityType [][]                     types,
            long []                             timeCreateds,
            long []                             timeUpdateds,
            long []                             timeReads,
            long []                             timeExpiress,
            boolean []                          giveUpHomeReplicas,
            boolean []                          giveUpLocks,
            long                                timeoutInMillis )
        throws
            TransactionException,
            MeshObjectIdentifierNotUniqueException
    {
        AnetMeshBase realBase = (AnetMeshBase) theMeshBase;
        Transaction  tx       = realBase.checkTransaction();

        Proxy                 incomingProxy           = realBase.determineIncomingProxy();
        NetMeshBaseIdentifier incomingProxyIdentifier = incomingProxy != null ? incomingProxy.getPartnerMeshBaseIdentifier() : null;

        NetMeshBaseAccessSpecification [][] accessPaths = new NetMeshBaseAccessSpecification[ pathToObjects.length ][];
        NetMeshObjectIdentifier []          identifiers = new NetMeshObjectIdentifier[ pathToObjects.length ];
        AnetMeshObject []                   ret         = new AnetMeshObject[ pathToObjects.length ];

        for( int i=0 ; i<pathToObjects.length ; ++i ) {
            accessPaths[i] = pathToObjects[i].getAccessPath();
            identifiers[i] = pathToObjects[i].getNetMeshObjectIdentifier();

            if( accessPaths[i] == null || accessPaths[i].length == 0 ) {
                throw new IllegalArgumentException( "Cannot use empty access path in NetMeshBaseAccessSpecification to create ForwardReference" );
            }
            if( identifiers[i] == null ) {
                throw new IllegalArgumentException( "null Identifier" );
            }
            NetMeshObject existing = findInStore( identifiers[i] );
            if( existing != null ) {
                throw new MeshObjectIdentifierNotUniqueException( existing );
            }

            long now = determineCreationTime();
            if( timeCreateds != null && timeCreateds[i] < 0 ) {
                timeCreateds[i] = now;
            }
            if( timeUpdateds != null && timeUpdateds[i] < 0 ) {
                timeUpdateds[i] = now;
            }
            if( timeReads != null && timeReads[i] < 0 ) {
                timeReads[i] = now;
            }
            // not timeAutoDeletes


            DefaultShadowProxy proxy;
            try {
                proxy = (DefaultShadowProxy) ((NetMeshBase)theMeshBase).obtainProxyFor(
                        accessPaths[i][0].getNetMeshBaseIdentifier(),
                        null );
            } catch( FactoryException ex ) {
                log.error( ex );
                continue;
            }
            proxy.setIsPlaceholder( true );

            ret[i] = instantiateMeshObjectImplementation(
                    identifiers[i],
                    timeCreateds != null ? timeCreateds[i] : -1L,
                    timeUpdateds != null ? timeUpdateds[i] : -1L,
                    timeReads    != null ? timeReads[i] : -1L,
                    timeExpiress != null ? timeExpiress[i] : -1L,
                    giveUpHomeReplicas != null ? giveUpHomeReplicas[i] : realBase.getDefaultWillGiveUpHomeReplica(),
                    giveUpLocks        != null ? giveUpLocks[i]        : realBase.getDefaultWillGiveUpLock(),
                    new Proxy[] { proxy },
                    0,
                    AnetMeshObject.HERE_CONSTANT );

            if( types != null && types[i] != null && types[i].length > 0 ) {
                try {
                    ret[i].blessForwardReference( types[i] );

                } catch( EntityBlessedAlreadyException ex ) {
                    log.error( ex );
                } catch( NotPermittedException ex ) {
                    log.error( ex );
                }
            }

            putIntoMeshBase(
                    ret[i],
                    new ForwardReferenceCreatedEvent(
                            realBase,
                            realBase.getIdentifier(),
                            pathToObjects[i],
                            ret[i],
                            incomingProxyIdentifier ));
            // assignOwner( ret ); FIXME? Should this assign an owner?
        }
        return ret;
    }
}
