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

package org.infogrid.lid.yadis;

import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * Factors out functionality common to YadisPipelineProcessingStage implementations.
 */
public abstract class AbstractYadisPipelineProcessingStage
        extends
            AbstractObjectInContext
        implements
            YadisPipelineProcessingStage
{
    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param c the context containing the available services.
     */
    protected AbstractYadisPipelineProcessingStage(
            Context c )
    {
        super( c );
    }

    /**
     * Process any relevant requests.
     *
     * @param lidRequest the incoming request
     * @param resource the resource to which the request refers, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    public void processRequest(
            SaneRequest        lidRequest,
            HasIdentifier      resource )
        throws
            LidAbortProcessingPipelineException
    {
        String meta = lidRequest.getUrlArgument( "lid-meta" );
        if( meta == null ) {
            meta = lidRequest.getUrlArgument( "meta" );
        }
        String acceptHeader = lidRequest.getAcceptHeader();

        if(    "capabilities".equals( meta )
            || ( acceptHeader != null && acceptHeader.indexOf( XRDS_MIME_TYPE ) >= 0 ))
        {
            processYadisRequest( lidRequest, resource );
        }
    }

    /**
     * It was discovered that this is a Yadis request. Process.
     *
     * @param lidRequest the incoming request
     * @param resource the resource to which the request refers, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    protected abstract void processYadisRequest(
            SaneRequest        lidRequest,
            HasIdentifier      resource )
        throws
            LidAbortProcessingPipelineException;
}
