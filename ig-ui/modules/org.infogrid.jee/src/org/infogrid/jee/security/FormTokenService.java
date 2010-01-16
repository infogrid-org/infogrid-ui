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

package org.infogrid.jee.security;

/**
 * <p>A service that generates one-time HTML form tokens, and validates them when
 * presented later. This is to be used to make HTML forms resistent against
 * cross-site attacks.</p>
 * 
 * <p>This is an interface so multiple implementations are possible.</p>
 */
public interface FormTokenService
{
    /**
     * Generate a new token.
     * 
     * @return the newly generated token
     */
    public String generateNewToken();
    
    /**
     * Validate a presented token. It is up to the implementation to decide whether
     * or not to invalidate presented tokens.
     * 
     * @param token the presented token
     * @return true if the token is valid, false if it is not valid or unknown.
     */
    public boolean validateToken(
            String token );
}
