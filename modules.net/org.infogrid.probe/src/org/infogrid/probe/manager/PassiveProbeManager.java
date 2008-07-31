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

package org.infogrid.probe.manager;

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;

import org.infogrid.util.CachingMap;
import org.infogrid.util.FactoryException;

/**
 * This slightly degenerate ProbeManager implementation is entirely passive and does not attempt
 * any automatic updates, regardless of requested CoherenceSpecifications.
 */
public abstract class PassiveProbeManager
        extends
            AbstractProbeManager
{
    /**
     * Constructor.
     *
     * @param delegateFactory the delegate ShadowMeshBaseFactory that knows how to instantiate ShadowMeshBases
     * @param storage the storage to use
     */
    protected PassiveProbeManager(
            ShadowMeshBaseFactory                            delegateFactory,
            CachingMap<NetMeshBaseIdentifier,ShadowMeshBase> storage )
    {
        super( delegateFactory, storage );
    }

    /**
     * Create a new, or obtain an already existing value for a provided key.
     *
     * @param key the key for which we want to obtain a value
     * @param argument optional argument to pass through to the createFor method
     * @return the found or created value for this key
     * @throws FactoryException catch-all Exception, consider its cause
     */
    @Override
    public ShadowMeshBase obtainFor(
            NetMeshBaseIdentifier  key,
            CoherenceSpecification argument )
        throws
            FactoryException
    {
        ShadowMeshBase ret = super.obtainFor( key, argument );

        return ret;
    }

    /**
     * Tell this ProbeManager that it is not needed any more.
     * 
     * @param isPermanent if true, this ProbeManager will go away permanently; if false, it may come alive again some time later
     */
    public void die(
            boolean isPermanent )
    {
        // noop
    }
}
