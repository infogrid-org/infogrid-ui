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

import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.http.SaneRequestUtils;

/**
 * Processes LID requests.
 */
public interface LidProcessingPipeline
{
    /**
     * Process the pipeline.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @return the authentication status of the client
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     */
    public LidClientAuthenticationStatus processPipeline(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse )
        throws
            LidAbortProcessingPipelineException;

    /**
     * Name of the LidClientAuthenticationStatus instance found in the request after the
     * pipeline has been processed.
     */
    public static final String CLIENT_AUTHENTICATION_STATUS_ATTRIBUTE_NAME
            = SaneRequestUtils.classToAttributeName( LidClientAuthenticationStatus.class );

    /**
     * Name of the LidPersona instance representing the client, and found in the request after the
     * pipeline has been processed.
     */
    public static final String CLIENT_PERSONA_ATTRIBUTE_NAME
            = SaneRequestUtils.classToAttributeName( HasIdentifier.class, "RequestingClient" );

    /**
     * Name of the HasIdentifier instance found in the request after the pipeline has
     * been processed.
     */
    public static final String REQUESTED_RESOURCE_ATTRIBUTE_NAME
            = SaneRequestUtils.classToAttributeName( HasIdentifier.class, "RequestedResource" );
}
