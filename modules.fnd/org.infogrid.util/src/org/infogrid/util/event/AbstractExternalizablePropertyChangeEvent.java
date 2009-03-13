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

package org.infogrid.util.event;

import java.beans.PropertyChangeEvent;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * A general-purpose implementation of {@link ExternalizablePropertyChangeEvent}.
 * It inherits from <code>java.beans.PropertyChangeEvent</code> in order to be
 * compatible with the Java APIs.
 * 
 * @param <S> the type of the event source
 * @param <SID> the type of the identifier of the event source
 * @param <P> the type of the property
 * @param <PID> type of the identifier of the property
 * @param <V> the type of the value
 * @param <VID> the type of the identifier of the value
 */
public abstract class AbstractExternalizablePropertyChangeEvent<S,SID,P,PID,V,VID>
        extends
            PropertyChangeEvent
        implements
            ExternalizablePropertyChangeEvent<S,SID,P,PID,V,VID>,
            CanBeDumped
{
    /**
     * Constructor.
     * 
     * @param source the object that is the source of the event
     * @param sourceIdentifier the identifier representing the source of the event
     * @param property an object representing the property of the event
     * @param propertyIdentifier the identifier representing the property of the event
     * @param oldValue the old value of the property, prior to the event
     * @param oldValueIdentifier the identifier representing the old value of the property, prior to the event
     * @param deltaValue the value that changed
     * @param deltaValueIdentifier the identifier of the value that changed
     * @param newValue the new value of the property, after the event
     * @param newValueIdentifier the identifier representing the new value of the property, after the event
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    protected AbstractExternalizablePropertyChangeEvent(
            S    source,
            SID  sourceIdentifier,
            P    property,
            PID  propertyIdentifier,
            V    oldValue,
            VID  oldValueIdentifier,
            V    deltaValue,
            VID  deltaValueIdentifier,
            V    newValue,
            VID  newValueIdentifier,
            long timeEventOccurred )
    {
        super( DUMMY_SENDER, null, null, null );

        theSource               = source;
        theSourceIdentifier     = sourceIdentifier;
        theProperty             = property;
        thePropertyIdentifier   = propertyIdentifier;
        theOldValue             = oldValue;
        theOldValueIdentifier   = oldValueIdentifier;
        theDeltaValue           = deltaValue;
        theDeltaValueIdentifier = deltaValueIdentifier;
        theNewValue             = newValue;
        theNewValueIdentifier   = newValueIdentifier;
        theTimeEventOccurred    = timeEventOccurred;
    }

    /**
     * Obtain the source of the event.
     *
     * @return the source of the event
     * @throws SourceUnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    @Override
    public synchronized S getSource()
        throws
            SourceUnresolvedException
    {
        if( theSource == null ) {
            theSource = resolveSource();
        }
        return theSource;
    }
    
    /**
     * Obtain the source identifier of the event.
     *
     * @return the source identifier
     */
    public SID getSourceIdentifier()
    {
        return theSourceIdentifier;
    }
    
    /**
     * Obtain the new value of the data item whose change triggered the event.
     *
     * @return the new value of the data item
     * @throws UnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    public synchronized V getDeltaValue()
    {
        if( theDeltaValue == null ) {
            theDeltaValue = resolveValue( getDeltaValueIdentifier() );
        }
        return theDeltaValue;
    }
    
    /**
     * Obtain the new-value identifier of the event.
     *
     * @return the new-value identifier
     */
    public VID getDeltaValueIdentifier()
    {
        return theDeltaValueIdentifier;
    }

    /**
     * Obtain the property of the event.
     *
     * @return the property of the event
     * @throws UnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    public synchronized P getProperty()
        throws
            UnresolvedException
    {
        if( theProperty == null ) {
            theProperty = resolveProperty();
        }
        return theProperty;
    }
    
    /**
     * Obtain the property identifier of the event.
     *
     * @return the property identifier
     */
    public PID getPropertyIdentifier()
    {
        return thePropertyIdentifier;
    }

    /**
     * Obtain the old value of the property prior to the event.
     *
     * @return the old value of the property
     * @throws UnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    @Override
    public synchronized V getOldValue()
    {
        if( theOldValue == null ) {
            theOldValue = resolveValue( getOldValueIdentifier() );
        }
        return theOldValue;
    }
    
    /**
     * Obtain the old-value identifier of the event.
     *
     * @return the old-value identifier
     */
    public VID getOldValueIdentifier()
    {
        return theOldValueIdentifier;
    }

    /**
     * Obtain the new value of the property after the event.
     *
     * @return the new value of the property
     * @throws UnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    @Override
    public synchronized V getNewValue()
    {
        if( theNewValue == null ) {
            theNewValue = resolveValue( getNewValueIdentifier() );
        }
        return theNewValue;
    }

    /**
     * Obtain the new-value identifier of the event.
     *
     * @return the new-value identifier
     */
    public VID getNewValueIdentifier()
    {
        return theNewValueIdentifier;
    }

    /**
     * Obtain the time at which the event occurred.
     *
     * @return the time at which the event occurred, in System.currentTimeMillis() format
     */
    public final long getTimeEventOccurred()
    {
        return theTimeEventOccurred;
    }

    /**
     * Enable subclass to resolve the source of the event.
     *
     * @return the source of the event
     * @throws SourceUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the source failed
     */
    protected abstract S resolveSource()
            throws
                SourceUnresolvedException;
    
    /**
     * Enable subclass to resolve the property of the event.
     *
     * @return the property of the event
     * @throws PropertyUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the property failed
     */
    protected abstract P resolveProperty()
            throws
                PropertyUnresolvedException;
    
    /**
     * Enable subclass to resolve a value of the event.
     *
     * @param vid the identifier for the value of the event
     * @return a value of the event
     * @throws UnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving a value failed
     */
    protected abstract V resolveValue(
            VID vid )
       throws
           UnresolvedException;

    /**
     * Clear cached objects to force a re-resolve.
     */
    protected void clearCachedObjects()
    {
        theSource     = null;
        theOldValue   = null;
        theDeltaValue = null;
        theNewValue   = null;
        theProperty   = null;
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
                    "getSourceIdentifier()",
                    "theProperty",
                    "thePropertyIdentifier",
                    "getOldValueIdentifier()",
                    "getNewValueIdentifier()",
                    "getTimeEventOccurred()"
                },
                new Object[] {
                    getSourceIdentifier(),
                    theProperty,
                    thePropertyIdentifier,
                    getOldValueIdentifier(),
                    getNewValueIdentifier(),
                    getTimeEventOccurred()
                });
    }
    
    /**
     * Equals method.
     * 
     * @param other the Object to compare with
     * @return true if they are equal
     */
    @Override
    public abstract boolean equals(
            Object other );

    /**
     * Hash code.
     * 
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        int ret = theSourceIdentifier != null ? theSourceIdentifier.hashCode() : 0;
        if( theOldValueIdentifier != null ) {
            ret ^= theOldValueIdentifier.hashCode();
        }
        if( theDeltaValueIdentifier != null ) {
            ret ^= theDeltaValueIdentifier.hashCode();
        }
        if( theNewValueIdentifier != null ) {
            ret ^= theNewValueIdentifier.hashCode();
        }
        if( thePropertyIdentifier != null ) {
            ret ^= thePropertyIdentifier.hashCode();
        }
        ret ^= theTimeEventOccurred;
        return ret;
    }

    /**
     * The source of the event.
     */
    private transient S theSource;
    
    /**
     * The identifier for the source of the event.
     */
    private SID theSourceIdentifier;

    /**
     * The old value of the data item whose change triggered the event.
     */
    private transient V theOldValue;

    /**
     * The identifier for the old value of the data item whose change triggered the event.
     */
    private VID theOldValueIdentifier;

    /**
     * The delta value of the data item whose change triggered the event.
     */
    private transient V theDeltaValue;

    /**
     * The identifier for the delta value of the data item whose change triggered the event.
     */
    private VID theDeltaValueIdentifier;

    /**
     * The new value of the data item whose change triggered the event.
     */
    private transient V theNewValue;

    /**
     * The identifier for the new value of the data item whose change triggered the event.
     */
    private VID theNewValueIdentifier;

    /**
     * The time at which the event occurred, in System.currentTimeMillis format.
     */
    private long theTimeEventOccurred;
    
    /**
     * The property of the event.
     */
    private transient P theProperty;
    
    /**
     * The identifier for the property of the event.
     */
    private PID thePropertyIdentifier;
    
    /**
     * Object we use as a source for java.util.EventObject instead of the real one,
     * because java.util.EventObject is rather broken.
     */
    private static final Object DUMMY_SENDER = new Object();
}
