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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util.event;

import java.util.EventObject;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * A general-purpose {@link ExternalizableEvent} implementation. It inherits from
 * <code>java.util.EventObject</code> in order to be compatible with the Java APIs.
 * 
 * @param <S> the type of the event source
 * @param <SID> the type of the identifier of the event source
 * @param <V> the type of the value
 * @param <VID> the type of the identifier of the value
 */
public abstract class AbstractExternalizableEvent<S,SID,V,VID>
        extends
            EventObject
        implements
            ExternalizableEvent<S,SID,V,VID>,
            CanBeDumped
{
    /**
     * Constructor.
     * 
     * @param source the object that is the source of the event, if available
     * @param sourceIdentifier the identifier representing the source of the event (required)
     * @param deltaValue the value that changed, if available
     * @param deltaValueIdentifier the identifier of the value that changed (may be null)
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    protected AbstractExternalizableEvent(
            S    source,
            SID  sourceIdentifier,
            V    deltaValue,
            VID  deltaValueIdentifier,
            long timeEventOccurred )
    {
        super( DUMMY_SENDER );

        theSource               = source;
        theSourceIdentifier     = sourceIdentifier;
        theDeltaValue           = deltaValue;
        theDeltaValueIdentifier = deltaValueIdentifier;
        theTimeEventOccurred    = timeEventOccurred;
    }

    /**
     * Obtain the source of the event.
     *
     * @return the source of the event
     * @throws SourceUnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the source failed
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
    public final SID getSourceIdentifier()
    {
        return theSourceIdentifier;
    }
    
    /**
     * Obtain the new value of the data item whose change triggered the event.
     *
     * @return the new value of the data item
     * @throws UnresolvedException if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the delta value failed
     */
    public final synchronized V getDeltaValue()
    {
        if( theDeltaValue == null ) {
            theDeltaValue = resolveValue( getDeltaValueIdentifier() );
        }
        return theDeltaValue;
    }
    
    /**
     * Obtain the delta-value identifier of the event.
     *
     * @return the delta-value identifier
     */
    public final VID getDeltaValueIdentifier()
    {
        return theDeltaValueIdentifier;
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
     * Enable subclass to resolve a value of the event.
     *
     * @param vid the identifier of the value of the event
     * @return a value of the event
     * @throws ValueUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    protected abstract V resolveValue(
            VID vid )
       throws
           ValueUnresolvedException;
    
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
     * Clear cached objects to force a re-resolve.
     */
    protected void clearCachedObjects()
    {
        theSource     = null;
        theDeltaValue = null;
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
                    "theSource",
                    "theSourceIdentifier",
                    "theDeltaValue",
                    "theDeltaValueIdentifier",
                    "theTimeEventOccurred"
                },
                new Object[] {
                    theSource,
                    theSourceIdentifier,
                    theDeltaValue,
                    theDeltaValueIdentifier,
                    theTimeEventOccurred
                });
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
     * The delta value of the data item whose change triggered the event.
     */
    private transient V theDeltaValue;
    
    /**
     * The identifier for the delta value of the data item whose change triggered the event.
     */
    private VID theDeltaValueIdentifier;

    /**
     * The time at which the event occurred, in System.currentTimeMillis format.
     */
    private long theTimeEventOccurred;
    
    /**
     * Object we use as a source for java.util.EventObject instead of the real one,
     * because <code>java.util.EventObject</code> is rather broken.
     */
    private static final Object DUMMY_SENDER = new Object();
}
