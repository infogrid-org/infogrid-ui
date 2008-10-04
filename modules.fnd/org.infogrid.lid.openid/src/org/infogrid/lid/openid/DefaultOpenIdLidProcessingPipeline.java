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

package org.infogrid.lid.openid;

import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidClientAuthenticationPipelineStage;
import org.infogrid.lid.LidClientAuthenticationStatus;
import org.infogrid.lid.LidPersona;
import org.infogrid.lid.LidProcessingPipeline;
import org.infogrid.lid.LidResource;
import org.infogrid.lid.LidResourceFinder;
import org.infogrid.lid.LidResourceUnknownException;
import org.infogrid.lid.yadis.YadisPipelineProcessingStage;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * Processes LID and OpenID requests in the default manner.
 * Compare with <code>org.infogrid.lid.DefaultLidIdProcessingPipeline</code>.
 */
public class DefaultOpenIdLidProcessingPipeline
        extends
            AbstractObjectInContext
        implements
             LidProcessingPipeline
{
    private static final Log log = Log.getLogInstance( DefaultOpenIdLidProcessingPipeline.class  ); // our own, private logger

    /**
     * Factory method.
     *
     * @param c the Context in which this object operates
     * @return the created LidProcessingPipeline
     */
    public static DefaultOpenIdLidProcessingPipeline create(
            Context c )
    {
        DefaultOpenIdLidProcessingPipeline ret = new DefaultOpenIdLidProcessingPipeline( c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param c the Context in which this object operates
     */
    protected DefaultOpenIdLidProcessingPipeline(
            Context c )
    {
        super( c );
        
        theOpenIdIdpSideAssociationStage = c.findContextObject(        OpenIdIdpSideAssociationPipelineStage.class );
        theResourceFinder                = c.findContextObjectOrThrow( LidResourceFinder.class );
        theYadisStage                    = c.findContextObject(        YadisPipelineProcessingStage.class );
        theAuthenticationStage           = c.findContextObject(        LidClientAuthenticationPipelineStage.class );
        theOpenIdSsoStage                = c.findContextObject(        OpenIdSsoPipelineStage.class );
    }
    
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
            LidAbortProcessingPipelineException
    {
        HttpServletRequest            realRequest       = lidRequest.getDelegate();
        LidResource                   requestedResource = null;
        LidClientAuthenticationStatus clientAuthStatus  = null;
        LidPersona                    clientPersona     = null;
        
        // This needs to be at the beginning due to the LidAbortProcessingPipelineException
        String lid_target = lidRequest.getArgument( "lid-target" );
        if( lid_target == null ) {
            lid_target = lidRequest.getArgument( "openid.return_to" );
        }
        realRequest.setAttribute( LID_TARGET_ATTRIBUTE_NAME, lid_target );

        if( theOpenIdIdpSideAssociationStage != null ) {
            theOpenIdIdpSideAssociationStage.processRequest( lidRequest, lidResponse );
        }

        try {
            requestedResource = theResourceFinder.findLidResource( lidRequest );
        } catch( LidResourceUnknownException ex ) {
            if( log.isInfoEnabled() ) {
                log.info( ex );
            }
        }
        realRequest.setAttribute( REQUESTED_RESOURCE_ATTRIBUTE_NAME, requestedResource );

        if( theYadisStage != null ) {
            theYadisStage.processRequest( lidRequest, lidResponse, requestedResource );
        }

        if( theAuthenticationStage != null ) {
            clientAuthStatus = theAuthenticationStage.determineAuthenticationStatus( lidRequest, lidResponse );
        }
        realRequest.setAttribute( CLIENT_AUTHENTICATION_STATUS_ATTRIBUTE_NAME, clientAuthStatus );

        if( clientAuthStatus != null ) {
            clientPersona = clientAuthStatus.getClientPersona();
        }
        realRequest.setAttribute( CLIENT_PERSONA_ATTRIBUTE_NAME, clientPersona );

        if( theOpenIdSsoStage != null ) {
            theOpenIdSsoStage.processRequest( lidRequest, lidResponse, clientAuthStatus, requestedResource );
        }
    }
    
    /**
     * Processes IdP-side OpenID Association requests.
     */
    protected OpenIdIdpSideAssociationPipelineStage theOpenIdIdpSideAssociationStage;

    /**
     * The service that knows how to find LidResources for incoming requests.
     */
    protected LidResourceFinder theResourceFinder;

    /**
     * The service that knows how to respond to Yadis requests.
     */
    protected YadisPipelineProcessingStage theYadisStage;

    /**
     * The service that knows how to determine the authentication status of the client.
     */
    protected LidClientAuthenticationPipelineStage theAuthenticationStage;

    /**
     * Processes IdP-side OpenID SSO requests.
     */
    protected OpenIdSsoPipelineStage theOpenIdSsoStage;

    /**
     * Name of the request attribute, optionally containing String, that gives the lid-target value.
     */
    public static final String LID_TARGET_ATTRIBUTE_NAME = "lid_target";    
}
