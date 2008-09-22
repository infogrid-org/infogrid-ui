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

package org.infogrid.lid;

import org.infogrid.util.SmartFactory;

/**
 * Defines the concept of a session manager. The arguments are as follows:
 * <ul>
 *  <li>String: the identifier of the user whose session we look for</li>
 *  <li>LidSession: the session</li>
 *  <li>String: IP address of the user at the time the session was created</li>
 * </ul>
 */
public interface LidSessionManager
        extends
            SmartFactory<String,LidSession,String>
{
    // nothing
}
