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

package org.infogrid.meshbase.net.xpriso.logging;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshObjectAccessSpecification;
import org.infogrid.meshbase.net.transaction.NetMeshObjectDeletedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectEquivalentsRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectNeighborRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectPropertyChangeEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectRoleRemovedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeAddedEvent;
import org.infogrid.meshbase.net.transaction.NetMeshObjectTypeRemovedEvent;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.util.logging.ToStringDumper;

/**
 * A BufferingDumper that dumps XprisoMessages in compact format.
 */
public class XprisoMessageDumper
    extends
        ToStringDumper
{
    /**
     * Factory method.
     *
     * @return the created XprisoMessageDumper
     */
    public static XprisoMessageDumper create()
    {
        return new XprisoMessageDumper( DEFAULT_MAXLEVEL );
    }

    /**
     * Factory method.
     *
     * @param maxLevel the number of object levels to dump
     * @return the created XprisoMessageDumper
     */
    public static XprisoMessageDumper create(
            int maxLevel )
    {
        return new XprisoMessageDumper( maxLevel );
    }

    /**
     * Constructor.
     *
     * @param maxLevel the number of object levels to dump
     */
    protected XprisoMessageDumper(
            int maxLevel )
    {
        super( maxLevel );
    }

    /**
     * Dump an object. This method is just the dispatcher and does not do any dumping itself.
     *
     * @param obj the object to dump
     */
    @Override
    public void dump(
            Object obj )
    {
        if( obj instanceof XprisoMessage ) {
            dumpXprisoMessage( (XprisoMessage) obj );
        } else {
            super.dump( obj );
        }
    }

    /**
     * Dump an XprisoMessage.
     *
     * @param obj the XprisoMessage
     */
    public void dumpXprisoMessage(
            XprisoMessage obj )
    {
        registerAsDumped( obj );
        emitObjectId( obj );

        emit( "{" );

        emit( "\n    route: " );
        emit( obj.getSenderIdentifier().toExternalForm() );
        emit( " -> " );
        emit( obj.getReceiverIdentifier().toExternalForm() );

        if( obj.getRequestId() > 0 ) {
            emit( ", requestId: " );
            emit( String.valueOf( obj.getRequestId() ));
        }
        if( obj.getResponseId() > 0 ) {
            emit( ", responseId: " );
            emit( String.valueOf( obj.getResponseId() ));
        }

        if( obj.getCeaseCommunications() ) {
            emit( "\n    ceaseCommunications: true" );
        }

        if( obj.getConveyedMeshObjects().length > 0 ) {
            emit( "\n    conveyedMeshObjects:" );
            for( ExternalizedNetMeshObject current : obj.getConveyedMeshObjects() ) {
                emit( "\n        id: " );
                emit( current.getIdentifier().toExternalForm() );
                if( current.getNeighbors().length > 0 ) {
                    emit( "\n            neighbors:" );
                    String sep = " ";
                    for( NetMeshObjectIdentifier current2 : current.getNeighbors() ) {
                        emit( sep );
                        emit( current2.toLocalExternalForm() );
                        sep = ", ";
                    }
                }
                if( current.getProxyIdentifiers().length > 0 ) {
                    emit( "\n            proxies:" );
                    String sep = " ";
                    for( NetMeshBaseIdentifier current2 : current.getProxyIdentifiers() ) {
                        emit( sep );
                        emit( current2.toExternalForm() );
                        if( current2.equals( current.getProxyTowardsHomeNetworkIdentifier() )) {
                            emit( "(home)" );
                        }
                        if( current2.equals( current.getProxyTowardsLockNetworkIdentifier() )) {
                            emit( "(lock)" );
                        }
                        sep = ", ";
                    }
                }
            }
        }

        if( obj.getRequestedFirstTimeObjects().length > 0 ) {
            emit( "\n    requestedFirstTime:" );
            for( NetMeshObjectAccessSpecification current : obj.getRequestedFirstTimeObjects() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getRequestedCanceledObjects().length > 0 ) {
            emit( "\n    requestedCanceled:" );
            for( NetMeshObjectIdentifier current : obj.getRequestedCanceledObjects() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getRequestedFreshenReplicas().length > 0 ) {
            emit( "\n    requestedFreshen:" );
            for( NetMeshObjectIdentifier current : obj.getRequestedFreshenReplicas() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getRequestedResynchronizeReplicas().length > 0 ) {
            emit( "\n    requestedResynchronized:" );
            for( NetMeshObjectIdentifier current : obj.getRequestedResynchronizeReplicas() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getRequestedLockObjects().length > 0 ) {
            emit( "\n    requestedLocks:" );
            for( NetMeshObjectIdentifier current : obj.getRequestedLockObjects() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getPushLockObjects().length > 0 ) {
            emit( "\n    pushLocks:" );
            for( NetMeshObjectIdentifier current : obj.getPushLockObjects() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getReclaimedLockObjects().length > 0 ) {
            emit( "\n    reclaimedLocks:" );
            for( NetMeshObjectIdentifier current : obj.getReclaimedLockObjects() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getRequestedHomeReplicas().length > 0 ) {
            emit( "\n    requestedHome:" );
            for( NetMeshObjectIdentifier current : obj.getRequestedHomeReplicas() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getPushHomeReplicas().length > 0 ) {
            emit( "\n    pushHome:" );
            for( NetMeshObjectIdentifier current : obj.getPushHomeReplicas() ) {
                emit( "\n        " );
                emit( current.toExternalForm() );
            }
        }

        if( obj.getTypeAdditions().length > 0 ) {
            emit( "\n    typeAdditions:" );
            for( NetMeshObjectTypeAddedEvent current : obj.getTypeAdditions() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                for( EntityType type : current.getDeltaValue() ) {
                    emit( " + " );
                    emit( type.toExternalForm() );
                }
            }
        }

        if( obj.getTypeRemovals().length > 0 ) {
            emit( "\n    typeRemovals:" );
            for( NetMeshObjectTypeRemovedEvent current : obj.getTypeRemovals() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                for( EntityType type : current.getDeltaValue() ) {
                    emit( " - " );
                    emit( type.toExternalForm() );
                }
            }
        }

        if( obj.getPropertyChanges().length > 0 ) {
            emit( "\n    propertyChanges:" );
            for( NetMeshObjectPropertyChangeEvent current : obj.getPropertyChanges() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                emit( ": " );
                emit( current.getPropertyTypeIdentifier().toExternalForm() );
                emit( " = " );
                emit( current.getDeltaValue() != null ? current.getDeltaValue().toString() : "null" );
            }
        }

        if( obj.getNeighborAdditions().length > 0 ) {
            emit( "\n    neighborAdditions:" );
            for( NetMeshObjectNeighborAddedEvent current : obj.getNeighborAdditions() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                for( MeshObjectIdentifier neigh : current.getDeltaValueIdentifier() ) {
                    emit( " + " );
                    emit( neigh.toExternalForm() );
                }
            }
        }

        if( obj.getNeighborRemovals().length > 0 ) {
            emit( "\n    neighborRemovals:" );
            for( NetMeshObjectNeighborRemovedEvent current : obj.getNeighborRemovals() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                for( MeshObjectIdentifier neigh : current.getDeltaValueIdentifier() ) {
                    emit( " - " );
                    emit( neigh.toExternalForm() );
                }
            }
        }

        if( obj.getEquivalentsAdditions().length > 0 ) {
            emit( "\n    equivalentAdditions:" );
            for( NetMeshObjectEquivalentsAddedEvent current : obj.getEquivalentsAdditions() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                for( MeshObjectIdentifier neigh : current.getDeltaValueIdentifier() ) {
                    emit( " + " );
                    emit( neigh.toExternalForm() );
                }
            }
        }

        if( obj.getEquivalentsRemovals().length > 0 ) {
            emit( "\n    equivalentRemovals:" );
            for( NetMeshObjectEquivalentsRemovedEvent current : obj.getEquivalentsRemovals() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                for( MeshObjectIdentifier neigh : current.getDeltaValueIdentifier() ) {
                    emit( " - " );
                    emit( neigh.toExternalForm() );
                }
            }
        }

        if( obj.getRoleAdditions().length > 0 ) {
            emit( "\n    roleAdditions:" );
            for( NetMeshObjectRoleAddedEvent current : obj.getRoleAdditions() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                emit( " -> " );
                emit( current.getNeighborMeshObjectIdentifier().toExternalForm() );
                for( MeshTypeIdentifier id : current.getAffectedRoleTypeIdentifiers() ) {
                    emit( " + " );
                    emit( id.toExternalForm() );
                }
            }
        }

        if( obj.getRoleRemovals().length > 0 ) {
            emit( "\n    roleRemovals:" );
            for( NetMeshObjectRoleRemovedEvent current : obj.getRoleRemovals() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
                emit( " -> " );
                emit( current.getNeighborMeshObjectIdentifier().toExternalForm() );
                for( MeshTypeIdentifier id : current.getAffectedRoleTypeIdentifiers() ) {
                    emit( " - " );
                    emit( id.toExternalForm() );
                }
            }
        }

        if( obj.getDeletions().length > 0 ) {
            emit( "\n    deletions:" );
            for( NetMeshObjectDeletedEvent current : obj.getDeletions() ) {
                emit( "\n        " );
                emit( current.getAffectedMeshObjectIdentifier().toExternalForm() );
            }
        }

        emit( "\n}" );
    }
}
