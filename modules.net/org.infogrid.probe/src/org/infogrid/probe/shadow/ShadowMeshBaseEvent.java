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

package org.infogrid.probe.shadow;

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.util.NameServer;
import org.infogrid.util.event.AbstractExternalizableEvent;
import org.infogrid.util.event.SourceUnresolvedException;
import org.infogrid.util.event.ValueUnresolvedException;

/**
  * This is a ShadowMeshBase-specific event. The actual meaning
  * of the event is determined by which of the listener methods have been invoked.
  */
public class ShadowMeshBaseEvent
    extends
        AbstractExternalizableEvent<ShadowMeshBase,NetMeshBaseIdentifier,Throwable,Throwable>
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor. No problem occurred.
     *
     * @param meshBase the ShadowMeshBase that sent this event
     */
    public ShadowMeshBaseEvent(
            ShadowMeshBase meshBase )
    {
        this( meshBase, meshBase.getIdentifier(), null, null, System.currentTimeMillis() );
    }

    /**
     * Constructor. A problem occurred.
     *
     * @param meshBase the ShadowMeshBase that sent this event
     * @param problem the Throwable indicating the problem that occurred
     */
    public ShadowMeshBaseEvent(
            ShadowMeshBase meshBase,
            Throwable      problem )
    {
        this( meshBase, meshBase.getIdentifier(), problem, problem, System.currentTimeMillis() );
    }

    /**
     * Internal constructor.
     * 
     * @param source the object that is the source of the event
     * @param sourceIdentifier the identifier representing the source of the event
     * @param deltaValue the value that changed
     * @param deltaValueIdentifier the identifier of the value that changed
     * @param timeEventOccurred the time at which the event occurred, in <code>System.currentTimeMillis</code> format
     */
    protected ShadowMeshBaseEvent(
            ShadowMeshBase        source,
            NetMeshBaseIdentifier sourceIdentifier,
            Throwable             deltaValue,
            Throwable             deltaValueIdentifier,
            long                  timeEventOccurred )
    {
        super( source, sourceIdentifier, deltaValue, deltaValueIdentifier, timeEventOccurred );
    }
       
    /**
     * Set a suitable resolver.
     * 
     * @param resolver the new resolver
     */
    public void setResolver(
            NameServer<NetMeshBaseIdentifier,NetMeshBase> resolver )
    {
        if( theResolver != resolver ) {
            clearCachedObjects();
            theResolver = resolver;
        }
    }

    /**
     * Obtain the problem that occurred, if any.
     *
     * @return the Throwable indicating the problem, if any.
     */
    public final Throwable getProblem()
    {
        return getDeltaValueIdentifier();
    }

    /**
     * Enable subclass to resolve the source of the event.
     *
     * @return the source of the event
     * @throws SourceUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the source failed
     */
    protected ShadowMeshBase resolveSource()
            throws
                SourceUnresolvedException
    {
        NetMeshBase ret = theResolver.get( getSourceIdentifier() );
        return (ShadowMeshBase) ret;
    }
    
    /**
     * Enable subclass to resolve a value of the event.
     *
     * @param vid the identifier of the value of the event
     * @return a value of the event
     * @throws ValueUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    protected Throwable resolveValue(
            Throwable vid )
       throws
           ValueUnresolvedException
    {
        return vid;
    }
    
    /**
     * The resolver to be used.
     */
    protected transient NameServer<NetMeshBaseIdentifier,NetMeshBase> theResolver;
}
