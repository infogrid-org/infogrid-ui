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
     * @param siteIdentifier identifies this site
     * @return the LidClientAuthenticationStatus
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     */
    public LidClientAuthenticationStatus determineAuthenticationStatus(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            Identifier         siteIdentifier )
        throws
            LidAbortProcessingPipelineException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "determineAuthenticationStatus", lidRequest, lidResponse, siteIdentifier );
        }

        String sessionCookieString = lidRequest.getCookieValue( LidCookies.LID_SESSION_COOKIE_NAME );
        String lidCookieString     = lidRequest.getCookieValue( LidCookies.LID_IDENTIFIER_COOKIE_NAME );

        // cleanup cookie values first
        if( sessionCookieString != null && sessionCookieString.startsWith( "\"" ) && sessionCookieString.endsWith( "\"" )) {
            sessionCookieString = sessionCookieString.substring( 1, sessionCookieString.length()-1 );
        }
        if( lidCookieString != null && lidCookieString.startsWith( "\"" ) && lidCookieString.endsWith( "\"" )) {
            lidCookieString = lidCookieString.substring( 1, lidCookieString.length()-1 );
        }

        // LID argument: first consider POST'd, then URL arguments
        String lidArgumentString = lidRequest.getPostedArgument( "lid" );
        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getPostedArgument( "openid_identifier" );
        }
        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getPostedArgument( "openid.identity" );
        }
        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getUrlArgument( "lid" );
        }
        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getUrlArgument( "openid_identifier" );
        }
        if( lidArgumentString == null ) {
            lidArgumentString = lidRequest.getUrlArgument( "openid.identity" );
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

        HasIdentifier client = null;
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

        boolean clientLoggedOn       = false;
        boolean wishesCancelSession  = false;
        boolean clientWishesToLogout = false;
        if(    lidRequest.matchUrlArgument( "lid-action", "cancel-session" )
            || lidRequest.matchPostedArgument( "lid-action", "cancel-session" ) )
        {
            wishesCancelSession = true;
        }
        if( lidArgumentString != null && lidArgumentString.length() == 0 && sessionClientIdentifier != null ) {
            clientWishesToLogout = true;
        }

        LidSession preexistingClientSession;
        if( sessionClientIdentifier != null && sessionCookieString != null ) {
            preexistingClientSession = theSessionManager.get( sessionCookieString );

            if( preexistingClientSession != null ) {
                if( clientIdentifier != null ) {
                    if( !preexistingClientSession.getClientIdentifier().equals( clientIdentifier )) {
                        preexistingClientSession = null; // this session does not belong to this client
                    } else if ( !preexistingClientSession.getSiteIdentifier().equals( siteIdentifier )) {
                        preexistingClientSession = null; // this session does not belong to this site
                    }
                } else if( sessionClientIdentifier != null ) {
                    // want to log out, but we still have a session
                    if( !preexistingClientSession.getClientIdentifier().equals( sessionClientIdentifier )) {
                        preexistingClientSession = null; // wrong session
                    } else if ( !preexistingClientSession.getSiteIdentifier().equals( siteIdentifier )) {
                        preexistingClientSession = null; // wrong session
                    }
                }
            }
        } else {
            preexistingClientSession = null;
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
                    if( !validCredentialTypes.isEmpty() && invalidCredentialTypes.isEmpty() ) {
                        if( sessionClientIdentifier == null ) {
                            clientLoggedOn = true;
                        }
                    }
                }
            } catch( FactoryException ex ) {
                log.error( ex );
            }
        }

        LidAuthenticationService [] authServices = determineAuthenticationServices( client );

        SimpleLidClientAuthenticationStatus ret = SimpleLidClientAuthenticationStatus.create(
                clientIdentifier,
                client,
                preexistingClientSession,
                validCredentialTypes,
                invalidCredentialTypes,
                invalidCredentialExceptions,
                clientLoggedOn,
                wishesCancelSession,
                clientWishesToLogout,
                authServices,
                siteIdentifier );

        if( log.isTraceEnabled() ) {
            log.traceMethodCallExit( this, "determineAuthenticationStatus", ret );
        }
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
     * Determine the available authentication services for this client.
     *
     * @param client the client
     * @return the authentication services, in sequence of preference, if any
     */
    protected LidAuthenticationService [] determineAuthenticationServices(
            HasIdentifier client )
    {
        return null;
    }

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
