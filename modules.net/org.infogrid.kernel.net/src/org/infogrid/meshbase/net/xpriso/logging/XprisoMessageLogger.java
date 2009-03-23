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

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;

/**
 * Knows how to log Xpriso messages.
 */
public interface XprisoMessageLogger
{
    /**
     * An XprisoMessage has arrived.
     *
     * @param base the NetMeshBase at which the XprisoMessage has arrived
     * @param msg the XprisoMessage that arrived
     */
    public void messageArrived(
            NetMeshBase   base,
            XprisoMessage msg );

    /**
     * An XprisoMessage is about to be sent.
     *
     * @param base the NetMeshBase sending the message
     * @param msg the XprisoMessage to be sent
     */
    public void messageToBeSent(
            NetMeshBase   base,
            XprisoMessage msg );

    /**
     * An XprisoMessage has successfully been sent.
     *
     * @param base the NetMeshBase sendingthe message
     * @param msg the XprisoMessage to be sent
     */
    public void messageSentSuccessfully(
            NetMeshBase   base,
            XprisoMessage msg );
}
