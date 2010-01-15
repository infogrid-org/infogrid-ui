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

package org.infogrid.mesh.net;

import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshObjectAccessException;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.util.event.AbstractExternalizablePropertyChangeEvent;
import org.infogrid.util.event.SourceUnresolvedException;

/**
 * This event indicates the transfer of home replica status from one replica in
 * one NetMeshBase to a replica of the same MeshObject in a second NetMeshBase.
 * Inner classes represent the specific event.
 */
public abstract class HomeReplicaChangedEvent
        extends
            AbstractExternalizablePropertyChangeEvent<NetMeshObject,NetMeshObjectIdentifier,String,String,BooleanValue,BooleanValue>
{
    /**
     * Constructor.
     *
     * @param affectedObject the MeshObject between whose replicas the home replica status moved
     * @param oldValue the old value for home replica status by this replica
     * @param newValue the new value for home replica status by this replica
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    protected HomeReplicaChangedEvent(
            NetMeshObject affectedObject,
            BooleanValue  oldValue,
            BooleanValue  newValue,
            long          timeEventOccurred )
    {
        super(  affectedObject,
                affectedObject.getIdentifier(),
                PROPERTY_NAME,
                PROPERTY_NAME,
                oldValue,
                oldValue,
                newValue,
                newValue,
                newValue,
                newValue,
                timeEventOccurred );
    }

    /**
     * Set a NetMeshBase as a resolver.
     * 
     * @param mb the resolver
     */
    public void setResolver(
            NetMeshBase mb )
    {
        if( theResolver != mb ) {
            theResolver = mb;
            clearCachedObjects();
        }
    }

    /**
     * Obtain the resolver.
     * 
     * @return the resolver
     */
    public NetMeshBase getResolver()
    {
        return theResolver;
    }

    /**
     * Resolve the source of the event.
     *
     * @return the source of the event
     * @throws SourceUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the source failed
     */
    protected NetMeshObject resolveSource()
            throws
                SourceUnresolvedException
    {
        try {
            return theResolver.accessLocally( getSourceIdentifier() );
            
        } catch( NetMeshObjectAccessException ex ) {
            throw new SourceUnresolvedException( this, ex );

        } catch( NotPermittedException ex ) {
            throw new SourceUnresolvedException( this, ex );
        }
    }
    
    /**
     * Resolve the property of the event.
     *
     * @return the property of the event
     */
    protected String resolveProperty()
    {
        return getPropertyIdentifier();
    }
    
    /**
     * Resolve a value of the event.
     *
     * @param vid the identifier for the value of the event
     * @return a value of the event
     */
    protected BooleanValue resolveValue(
            BooleanValue vid )
    {
        return vid;
    }

    /**
     * The resolver.
     */
    protected NetMeshBase theResolver;

    /**
     * The name of the property as seen by the PropertyChangeEvent supertype.
     */
    public static final String PROPERTY_NAME = "_Home";

    /**
     * This specific subclass indicates that this replica has obtained home replica status.
     */
    public static class GainedHomeReplica
            extends
                HomeReplicaChangedEvent
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param affectedObject the replica that obtained the home replica status
         * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
         */
        public GainedHomeReplica(
                NetMeshObject affectedObject,
                long          timeEventOccurred )
        {
            super( affectedObject, BooleanValue.FALSE, BooleanValue.TRUE, timeEventOccurred );
        }

        /**
         * Determine equality.
         * 
         * @param other the Object to compare to
         * @return true if the two Objects are equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( !( other instanceof GainedHomeReplica )) {
                return false;
            }
            
            GainedHomeReplica realOther = (GainedHomeReplica) other;
            
            if( !getSourceIdentifier().equals( realOther.getSourceIdentifier() )) {
                return false;
            }
            
            if( getTimeEventOccurred() != realOther.getTimeEventOccurred() ) {
                return false;
            }
            return true;
        }

        /**
         * Determine hash code.
         * 
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            return super.hashCode();
        }
    }

    /**
     * This specific subclass indicates that this replica has lost home replica status.
     */
    public static class LostHomeReplica
            extends
                HomeReplicaChangedEvent
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param affectedObject the replica that lost the home replica status
         * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
         */
        public LostHomeReplica(
                NetMeshObject affectedObject,
                long          timeEventOccurred )
        {
            super( affectedObject, BooleanValue.TRUE, BooleanValue.FALSE, timeEventOccurred );
        }

       /**
         * Determine equality.
         * 
         * @param other the Object to compare to
         * @return true if the two Objects are equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( !( other instanceof LostHomeReplica )) {
                return false;
            }
            
            LostHomeReplica realOther = (LostHomeReplica) other;
            
            if( !getSourceIdentifier().equals( realOther.getSourceIdentifier() )) {
                return false;
            }
            
            if( getTimeEventOccurred() != realOther.getTimeEventOccurred() ) {
                return false;
            }
            return true;
        }

        /**
         * Determine hash code.
         * 
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            return super.hashCode();
        }
    }
}
