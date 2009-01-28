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

package org.infogrid.lid.gpg;

import org.infogrid.util.Identifier;
import org.infogrid.util.SmartFactory;

/**
 * Obtains and manages LID public keys for given identities that are not maintained locally.
 * The key into the  SmartFactory is the LID identifier, the created value is the public key.
 * There is no argument.
 */
public interface LidGpgPublicKeyManager
    extends
        SmartFactory<Identifier,String,Void>
{
}
