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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.yadis;

import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidProcessingPipelineStage;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.context.ObjectInContext;
import org.infogrid.util.http.SaneRequest;

/**
 * Knows how to process Yadis requests.
 */
public interface YadisPipelineProcessingStage
        extends
            LidProcessingPipelineStage,
            ObjectInContext
{
    /**
     * Process any relevant requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param resource the resource to which the request refers, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    public void processRequest(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            HasIdentifier      resource )
        throws
            LidAbortProcessingPipelineException;


    /**
     * Name of the Yadis HTTP header.
     */
    public static final String YADIS_HTTP_HEADER = "X-XRDS-Location";

    /**
     * Yadis MIME type.
     */
    public static final String XRDS_MIME_TYPE = "application/xrds+xml";
}
