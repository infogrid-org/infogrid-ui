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
import org.infogrid.lid.yadis.YadisPipelineProcessingStage;
import org.infogrid.util.CannotFindHasIdentifierException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Processes LID requests in the default manner.
 */
public class DefaultLidProcessingPipeline
        extends
            AbstractObjectInContext
        implements
             LidProcessingPipeline
{
    private static final Log log = Log.getLogInstance( LidProcessingPipeline.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param c the Context in which this object operates
     * @return the created LidProcessingPipeline
     */
    public static DefaultLidProcessingPipeline create(
            Context c )
    {
        DefaultLidProcessingPipeline ret = new DefaultLidProcessingPipeline( c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param c the Context in which this object operates
     */
    protected DefaultLidProcessingPipeline(
            Context c )
    {
        super( c );
        
        theResourceFinder      = c.findContextObject( LidHasIdentifierFinder.class );
        theYadisStage          = c.findContextObject( YadisPipelineProcessingStage.class );
        theAuthenticationStage = c.findContextObject( LidClientAuthenticationPipelineStage.class );
    }
    
    /**
     * Process the pipeline.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @return the authentication status of the client
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     * @throws URISyntaxException thrown if the specified client identifier could not be interpreted
     */
    public LidClientAuthenticationStatus processPipeline(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse )
        throws
            LidAbortProcessingPipelineException,
            URISyntaxException
    {
        HasIdentifier                 requestedResource = null;
        LidClientAuthenticationStatus clientAuthStatus  = null;
        HasIdentifier                 clientPersona     = null;

        if( theResourceFinder != null ) {
            try {
                requestedResource = theResourceFinder.findFromRequest( lidRequest );
            } catch( CannotFindHasIdentifierException ex ) {
                if( log.isInfoEnabled() ) {
                    log.info( ex );
                }
            }
        }
        lidRequest.setAttribute( REQUESTED_RESOURCE_ATTRIBUTE_NAME, requestedResource );
        
        if( theYadisStage != null ) {
            // this also needs to be invoked if requestedResource is null
            theYadisStage.processRequest( lidRequest, lidResponse, requestedResource );
        }
        
        if( theAuthenticationStage != null ) {
            clientAuthStatus = theAuthenticationStage.determineAuthenticationStatus( lidRequest, lidResponse );
        }
        lidRequest.setAttribute( CLIENT_AUTHENTICATION_STATUS_ATTRIBUTE_NAME, clientAuthStatus );
        
        if( clientAuthStatus != null ) {
            clientPersona = clientAuthStatus.getClientPersona();
        }
        lidRequest.setAttribute( CLIENT_PERSONA_ATTRIBUTE_NAME, clientPersona );

        return clientAuthStatus;
    }
    
    /**
     * The service that knows how to find LidResources for incoming requests.
     */
    protected LidHasIdentifierFinder theResourceFinder;

    /**
     * The service that knows how to respond to Yadis requests.
     */
    protected YadisPipelineProcessingStage theYadisStage;

    /**
     * The service that knows how to determine the authentication status of the client.
     */
    protected LidClientAuthenticationPipelineStage theAuthenticationStage;
}
