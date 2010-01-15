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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid;

import org.infogrid.util.http.SaneRequest;

/**
 * Knows how to manage the session.
 */
public interface LidSessionManagementStage
        extends
            LidProcessingPipelineStage
{
    /**
     * Determine what operations should be performed to manage the client's session.
     * This acts as a factory method for LidSessionManagementInstructions.
     *
     * @param lidRequest the incoming request
     * @param realm the realm of the session
     * @param clientAuthStatus authentication status of the client
     * @return LidSessionManagementInstructions the instructions, if any
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     */
    public LidSessionManagementInstructions processSession(
            SaneRequest                   lidRequest,
            String                        realm,
            LidClientAuthenticationStatus clientAuthStatus )
        throws
            LidAbortProcessingPipelineException;
}
