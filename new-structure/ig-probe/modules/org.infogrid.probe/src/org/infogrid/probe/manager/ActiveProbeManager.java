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

import org.infogrid.probe.ProbeException;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.IsDeadException;

/**
 * A ProbeManager that is active, i.e. schedules and runs Probes automatically.
 */
public interface ActiveProbeManager
    extends
        ProbeManager
{
    /**
     * Invoke a ShadowMeshBase update now
     *
     * @param shadow the ShadowMeshBase to update
     * @throws ProbeException thrown if the update was unsuccessful
     * @throws IsDeadException thrown in this ShadowMeshBase is dead already
     */
    public void doUpdateNow(
            ShadowMeshBase shadow )
        throws
            ProbeException,
            IsDeadException;

    /**
     * Stop future automatic updates for this ShadowMeshBase.
     *
     * @param shadow the ShadowMeshBase
     */
    public void disableFutureUpdates(
            ShadowMeshBase shadow );
}
