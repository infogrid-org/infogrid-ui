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

import java.net.URISyntaxException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.util.http.SaneRequest;

/**
 * Knows how to determine the authentication status of the client from an incoming request.
 */
public interface LidClientAuthenticationPipelineStage
        extends
            LidProcessingPipelineStage
{
    /**
     * Determine the authentication status of the client. This acts as a factory method for LidClientAuthenticationStatus.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @return the LidClientAuthenticationStatus
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     * @throws URISyntaxException thrown if the specified client identifier could not be interpreted
     */
    public LidClientAuthenticationStatus determineAuthenticationStatus(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse )
        throws
            LidAbortProcessingPipelineException,
            URISyntaxException;
}
