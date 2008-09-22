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

import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;

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
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     */
    public void processPipeline(
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse )
        throws
            LidAbortProcessingPipelineException;

    /**
     * Name of the LidClientAuthenticationStatus instance found in the request after the
     * pipeline has been processed.
     */
    public static final String CLIENT_AUTHENTICATION_STATUS_ATTRIBUTE_NAME
            = SaneServletRequest.classToAttributeName( LidClientAuthenticationStatus.class );
    
    /**
     * Name of the LidResource instance found in the request after the pipeline has
     * been processed.
     */
    public static final String REQUESTED_RESOURCE_ATTRIBUTE_NAME
            = SaneServletRequest.classToAttributeName( LidResource.class );
}
