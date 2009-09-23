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

import java.util.ArrayList;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.credential.LidCredentialTypesFactory;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.HasIdentifierFinder;
import org.infogrid.util.Identifier;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Collects functionality common to LidClientAuthenticationPipelineStage
 * implementations.
 */
public abstract class AbstractLidClientAuthenticationPipelineStage
        extends
            AbstractObjectInContext
        implements
            LidClientAuthenticationPipelineStage
{
    private static final Log log = Log.getLogInstance( AbstractLidClientAuthenticationPipelineStage.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param context the Context
     */
    protected AbstractLidClientAuthenticationPipelineStage(
            Context context )
    {
        super( context );
    }

    /**
     * Determine the authentication status of the client. This acts as a factory method for LidClientAuthenticationStatus.
     *
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @return the LidClientAuthenticationStatus
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     */
    public LidClientAuthenticationStatus determineAuthenticationStatus(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse )
        throws
            LidAbortProcessingPipelineException
    {
        boolean wishesCancelSession = false;

        if( lidRequest.matchArgument( "lid-action", "cancel-session" )) {
            wishesCancelSession = true;
        }

        String sessionId         = lidRequest.getCookieValue( LidCookies.LID_SESSION_COOKIE_NAME );
        String lidCookieString   = lidRequest.getCookieValue( LidCookies.LID_IDENTIFIER_COOKIE_NAME );
        String lidArgumentString = lidRequest.getArgument( "lid" );

        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getArgument( "lid" );
        }
        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getArgument( "openid_identifier" );
        }
        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getArgument( "openid.identity" );
        }

        if( sessionId != null ) {
            sessionId = HTTP.decodeUrlArgument( sessionId );
        }
        if( lidCookieString != null ) {
            lidCookieString = HTTP.decodeUrlArgument( lidCookieString );
        }

        Identifier lidCookieIdentifier   = null;
        Identifier lidArgumentIdentifier = null;

        if( lidCookieString != null && lidCookieString.length() > 0 ) {
            try {
                lidCookieIdentifier = correctIdentifier( lidRequest.getAbsoluteContextUri(), lidCookieString );

            } catch( Throwable ex ) {
                log.warn( ex );
                lidCookieIdentifier = null; // We ignore the cookie if it is misformed
            }
        }

        if( lidArgumentString != null && lidArgumentString.length() > 0 ) {
            try {
                lidArgumentIdentifier = correctIdentifier( lidRequest.getAbsoluteContextUri(), lidArgumentString );
            } catch( Throwable ex ) {
                // could not be corrected
                if( log.isDebugEnabled() ) {
                    log.debug( "Cannot correct identifier", lidArgumentString, ex );
                }
            }
        }

        // Note: it is very important to distinguish between Strings that are null here,
        // and String that are empty. Do not accidentially "fix" this code by messing this up.

        Identifier clientIdentifier;
        Identifier sessionClientIdentifier = lidCookieIdentifier;

        if( lidArgumentString != null ) { // compare with string, not identifier
            clientIdentifier = lidArgumentIdentifier != null ? lidArgumentIdentifier : null;

        } else if( lidCookieIdentifier != null ) { // use identifier here
            clientIdentifier = lidCookieIdentifier;

        } else {
            clientIdentifier = null;
        }

        HasIdentifier client        = null;
        HasIdentifier sessionClient = null;

        if( clientIdentifier != null ) {
            try {
                client = thePersonaManager.find( clientIdentifier );

            } catch( Exception ex ) {
                // ignore
                if( log.isInfoEnabled() ) {
                    log.info( ex );
                }
            }
        }
        if( sessionClientIdentifier != null ) {
            if( sessionClientIdentifier.equals( clientIdentifier )) {
                // save us a lookup
                sessionClient = client;
            } else {
                try {
                    sessionClient = thePersonaManager.find( sessionClientIdentifier );

                } catch( Exception ex ) {
                    // ignore
                    if( log.isInfoEnabled() ) {
                        log.info( ex );
                    }
                }
            }
        }

        LidSession session = null;
        if( sessionClient != null ) {
            try {
                session = theSessionManager.obtainFor( sessionClientIdentifier, lidRequest.getClientIp()  );
            } catch( FactoryException ex ) {
                // ignore
                if( log.isInfoEnabled() ) {
                    log.info( ex );
                }
            }
            if( wishesCancelSession && session != null && session.isStillValid() ) {
                session.cancel();
            }
        }

        ArrayList<LidCredentialType>             validCredentialTypes        = null;
        ArrayList<LidCredentialType>             invalidCredentialTypes      = null;
        ArrayList<LidInvalidCredentialException> invalidCredentialExceptions = null;

        if( client != null ) {

            try {
                LidCredentialType [] referencedCredentialTypes = theCredentialTypeFactory.obtainFor( lidRequest );

                if( referencedCredentialTypes != null && referencedCredentialTypes.length > 0 ) {
                    validCredentialTypes        = new ArrayList<LidCredentialType>();
                    invalidCredentialTypes      = new ArrayList<LidCredentialType>();
                    invalidCredentialExceptions = new ArrayList<LidInvalidCredentialException>();

                    for( int i=0 ; i<referencedCredentialTypes.length ; ++i ) {
                        LidCredentialType current = referencedCredentialTypes[i];
                        try {
                            current.checkCredential( lidRequest, client );

                            validCredentialTypes.add( current );

                        } catch( LidInvalidCredentialException ex ) {
                            invalidCredentialTypes.add( current );
                            invalidCredentialExceptions.add( ex );
                        }
                    }
                }
            } catch( FactoryException ex ) {
                log.error( ex );
            }
        }

        SimpleLidClientAuthenticationStatus ret = SimpleLidClientAuthenticationStatus.create(
                clientIdentifier,
                client,
                session,
                validCredentialTypes,
                invalidCredentialTypes,
                invalidCredentialExceptions,
                sessionClientIdentifier,
                sessionClient,
                wishesCancelSession );
        return ret;
    }

    /**
     * Correct the entered clientIdentifier.
     *
     * @param contextUri the absolute URI of the application's context
     * @param candidate what the user typed
     * @return the corrected clientIdentifier
     * @throws StringRepresentationParseException thrown if the identifier was malformed
     */
    protected abstract Identifier correctIdentifier(
            String contextUri,
            String candidate )
        throws
            StringRepresentationParseException;

    /**
     * The LidCredentialTypesFactory to use.
     */
    protected LidCredentialTypesFactory theCredentialTypeFactory;

    /**
     * The LidSessionManager to use.
     */
    protected LidSessionManager theSessionManager;

    /**
     * The PersonaManager to use.
     */
    protected HasIdentifierFinder thePersonaManager;

}
