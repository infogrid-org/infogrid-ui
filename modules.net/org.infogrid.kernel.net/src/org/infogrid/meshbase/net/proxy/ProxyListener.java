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

package org.infogrid.meshbase.net.proxy;

/**
 * Interface supported by objects wishing to listen to Proxy activity.
 */
public interface ProxyListener
{
    /**
     * The synchronous sending of a message failed.
     *
     * @param theEvent the event
     */
    public abstract void synchronousSendFailedEvent(
            SendFailedEvent theEvent );
    
    /**
     * The initiation of a resynchronization failed.
     * 
     * @param theEvent the event
     */
    public abstract void initiateResynchronizeFailedEvent(
            InitiateResynchronizeFailedEvent theEvent );
}
