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

/**
 * Defines how to generate and validate LID nonces.
 */
public interface LidNonceManager
{
    /**
     * Generate a new nonce.
     * 
     * @return the newly generated nonce
     */
    public String generateNewNonce();
    
    /**
     * Validate a presented nonce.
     * 
     * @param nonce the presented nonce
     * @return true if the nonce is valid, false if it is not valid or unknown.
     */
    public boolean validateNonce(
            String nonce );
}
