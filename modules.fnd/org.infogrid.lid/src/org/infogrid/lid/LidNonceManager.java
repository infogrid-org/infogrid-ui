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

import org.infogrid.util.http.SaneRequest;

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
     * Validate a LID nonce contained in a request.
     * 
     * @param request the request
     * @throws LidInvalidNonceException thrown if the nonce was invalid
     */
    public void validateNonce(
            SaneRequest request )
        throws
            LidInvalidNonceException;

    /**
     * Validate a LID nonce contained in a request with the given URL parameter.
     *
     * @param request the request
     * @param name the name of the URL parameter
     * @throws LidInvalidNonceException thrown if the nonce was invalid
     */
    public void validateNonce(
            SaneRequest request,
            String      name )
        throws
            LidInvalidNonceException;

    /**
     * Name of the URL parameter that indicates the LID nonce.
     */
    public static final String LID_NONCE_PARAMETER_NAME = "lid-nonce";
}
