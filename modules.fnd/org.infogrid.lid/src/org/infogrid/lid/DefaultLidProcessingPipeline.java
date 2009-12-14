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

package org.infogrid.lid;

import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.lid.yadis.YadisPipelineProcessingStage;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.Identifier;
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
        
        theResourceFinder         = c.findContextObject( LidHasIdentifierFinder.class );
        theYadisStage             = c.findContextObject( YadisPipelineProcessingStage.class );
        theAuthenticationStage    = c.findContextObject( LidClientAuthenticationPipelineStage.class );
        theSessionManagementStage = c.findContextObject( LidSessionManagementStage.class );
    }
    
    /**
     * Process the pipeline.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param siteIdentifier identifies this site
     * @param realm the realm of the authentication
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     */
    public void processPipeline(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            Identifier         siteIdentifier,
            String             realm )
        throws
            LidAbortProcessingPipelineException
    {
        HasIdentifier                    requestedResource = null;
        LidClientAuthenticationStatus    clientAuthStatus  = null;
        HasIdentifier                    clientPersona     = null;
        LidSessionManagementInstructions sessionMgmtInstructions = null;

        if( theResourceFinder != null ) {
            try {
                requestedResource = theResourceFinder.findFromRequest( lidRequest );

            } catch( Exception ex ) {
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
            clientAuthStatus = theAuthenticationStage.determineAuthenticationStatus( lidRequest, lidResponse, siteIdentifier, realm );
        }
        lidRequest.setAttribute( CLIENT_AUTHENTICATION_STATUS_ATTRIBUTE_NAME, clientAuthStatus );
        
        if( clientAuthStatus != null ) {
            clientPersona = clientAuthStatus.getClientPersona();
        }
        lidRequest.setAttribute( CLIENT_PERSONA_ATTRIBUTE_NAME, clientPersona );

        if( theSessionManagementStage != null ) {
            sessionMgmtInstructions = theSessionManagementStage.processSession( lidRequest, lidResponse, realm, clientAuthStatus );
        }
        lidRequest.setAttribute( SESSION_MANAGEMENT_INSTRUCTIONS_ATTRIBUTE_NAME, sessionMgmtInstructions );
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

    /**
     * The service that knows how to manage sessions.
     */
    protected LidSessionManagementStage theSessionManagementStage;
}
