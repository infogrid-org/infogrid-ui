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

package org.infogrid.probe.manager;

import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseNameServer;
import org.infogrid.meshbase.net.m.NetMMeshBaseNameServer;
import org.infogrid.meshbase.net.xpriso.logging.XprisoMessageLogger;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.util.CachingMap;
import org.infogrid.util.PatientSmartFactory;

/**
 * Factors out functionality common to many ProbeManager implementations.
 */
public abstract class AbstractProbeManager
        extends
            PatientSmartFactory<NetMeshBaseIdentifier,ShadowMeshBase,CoherenceSpecification>
        implements
            ProbeManager
{
    /**
     * Constructor.
     * 
     * @param delegateFactory the delegate ShadowMeshBaseFactory that knows how to instantiate ShadowMeshBases
     * @param storage the storage to use
     */
    protected AbstractProbeManager(
            ShadowMeshBaseFactory                            delegateFactory,
            CachingMap<NetMeshBaseIdentifier,ShadowMeshBase> storage )
    {
        super( delegateFactory, storage );
    }

    /**
     * Set the main MeshBase for which this ProbeManager manages the Probes.
     *
     * @param main the main MeshBase.
     */
    public void setMainNetMeshBase(
            NetMeshBase main )
    {
        if( theNameServer != null ) {
            if( theMainNetMeshBase != null ) {
                theNameServer.remove( theMainNetMeshBase.getIdentifier() );
            }        
            if( main != null ) {
                theNameServer.put( main.getIdentifier(), main );
            }
        }
        theMainNetMeshBase = main;
    }

    /**
     * Obtain a NameServer that contains all ShadowMeshBases and the main NetMeshBase.
     * 
     * @return the NameServer
     */
    public synchronized NetMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> getNetMeshBaseNameServer()
    {
        if( theNameServer == null ) {
            theNameServer = NetMMeshBaseNameServer.<NetMeshBaseIdentifier,NetMeshBase>create( this );
            if( theMainNetMeshBase != null ) {
                theNameServer.put( theMainNetMeshBase.getIdentifier(), theMainNetMeshBase );
            }
        }
        return theNameServer;
    }

    /**
     * This overridable method allows our subclasses to invoke particular functionality
     * every time this SmartFactory created a new value by invoking the delegate Factory.
     * It is not invoked for those returned values that are merely retrieved from
     * the storage in the smart factory.
     *
     * @param key the key of the newly created value
     * @param value the newly created value
     * @param argument the argument into the creation of the newly created value
     */
    @Override
    protected void createdHook(
            NetMeshBaseIdentifier  key,
            ShadowMeshBase         value,
            CoherenceSpecification argument )
    {
        if( theXprisoMessageLogger != null ) {
            value.setXprisoMessageLogger( theXprisoMessageLogger );
        }
    }

    /**
     * Set a XprisoMessageLogger for all incoming and outgoing XprisoMessages.
     *
     * @param newValue the new value
     */
    public void setXprisoMessageLogger(
            XprisoMessageLogger newValue )
    {
        theXprisoMessageLogger = newValue;
    }

    /**
     * Obtain the currently active XprisoMessageLogger, if any.
     *
     * @return the currently active XprisoMessageLogger, if any
     */
    public XprisoMessageLogger getXprisoMessageLogger()
    {
        return theXprisoMessageLogger;
    }

    /**
     * The main NetMeshBase.
     */
    protected NetMeshBase theMainNetMeshBase;
    
    /**
     * The NameServer, allocated as needed.
     */
    protected NetMMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> theNameServer;

    /**
     * The Xpriso Message Logger to use for all ShadowMeshBases, if any.
     */
    protected XprisoMessageLogger theXprisoMessageLogger;
}
