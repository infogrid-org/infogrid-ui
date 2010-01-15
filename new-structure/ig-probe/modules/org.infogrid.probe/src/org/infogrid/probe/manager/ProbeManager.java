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
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseNameServer;
import org.infogrid.meshbase.net.xpriso.logging.XprisoMessageLogger;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseFactory;
import org.infogrid.util.SmartFactory;

/**
 * Objects that support this interface know how to manage ShadowMeshBases.
 */
public interface ProbeManager
        extends
            SmartFactory<NetMeshBaseIdentifier,ShadowMeshBase,CoherenceSpecification>, // not just Factory, but SmartFactory 
            ShadowMeshBaseFactory
{
    /**
     * Set the main MeshBase for which this ProbeManager manages the Probes.
     *
     * @param main the main MeshBase.
     */
    public void setMainNetMeshBase(
            NetMeshBase main );

    /**
     * Obtain a NameServer that contains all ShadowMeshBases and the main NetMeshBase.
     * 
     * @return the NameServer
     */
    public NetMeshBaseNameServer<NetMeshBaseIdentifier,NetMeshBase> getNetMeshBaseNameServer();
    
    /**
     * Tell this ProbeManager that it is not needed any more.
     * 
     * @param isPermanent if true, this ProbeManager will go away permanently; if false, it may come alive again some time later
     */
    public void die(
            boolean isPermanent );

    /**
     * Set a XprisoMessageLogger for all incoming and outgoing XprisoMessages.
     *
     * @param newValue the new value
     */
    public void setXprisoMessageLogger(
                XprisoMessageLogger newValue );

    /**
     * Obtain the currently active XprisoMessageLogger, if any.
     *
     * @return the currently active XprisoMessageLogger, if any
     */
    public XprisoMessageLogger getXprisoMessageLogger();
}
