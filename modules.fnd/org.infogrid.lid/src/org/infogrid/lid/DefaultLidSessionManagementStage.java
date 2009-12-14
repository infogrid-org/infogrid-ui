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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.Identifier;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Knows how to manage the session.
 */
public class DefaultLidSessionManagementStage
        implements
             LidSessionManagementStage
{
    private static final Log log = Log.getLogInstance( DefaultLidSessionManagementStage.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param sessionMgr the LidSessionManager to use
     * @return the created DefaultLidSessionManagementStage
     */
    public static DefaultLidSessionManagementStage create(
            LidSessionManager sessionMgr )
    {
        return new DefaultLidSessionManagementStage( sessionMgr );
    }

    /**
     * Constructor, for subclasses only, use factory method.
     *
     * @param sessionMgr the LidSessionManager to use
     */
    protected DefaultLidSessionManagementStage(
            LidSessionManager sessionMgr )
    {
        theSessionManager = sessionMgr;
    }

    /**
     * Determine what operations should be performed to manage the client's session.
     * This acts as a factory method for LidSessionManagementInstructions.
     *
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param realm the realm of the session
     * @param clientAuthStatus authentication status of the client
     * @return LidSessionManagementInstructions the instructions, if any
     * @throws LidAbortProcessingPipelineException thrown if the response has been found,
     *         and no further processing is necessary
     */
    public LidSessionManagementInstructions processSession(
            SaneRequest                   lidRequest,
            StructuredResponse            lidResponse,
            String                        realm,
            LidClientAuthenticationStatus clientAuthStatus )
        throws
            LidAbortProcessingPipelineException
    {
        Boolean deleteLidCookie; // using Boolean instead of boolean to detect if we don't catch a case
        Boolean deleteSessionCookie;
        Boolean createNewSession;

        Identifier lidCookieIdentifierToSet = null;
        LidSession preexistingSession       = clientAuthStatus.getPreexistingClientSession();

        ArrayList<LidSession> sessionsToCancel = new ArrayList<LidSession>();
        ArrayList<LidSession> sessionsToRenew  = new ArrayList<LidSession>();

        if( clientAuthStatus.isAnonymous() )  {
            // anonymous
            if( preexistingSession != null ) {
                // just logged out
                deleteLidCookie     = Boolean.TRUE;
                deleteSessionCookie = Boolean.TRUE;
                createNewSession    = Boolean.FALSE;
                sessionsToCancel.add( preexistingSession );

            } else {
                deleteLidCookie     = Boolean.FALSE;
                deleteSessionCookie = Boolean.FALSE;
                createNewSession    = Boolean.FALSE;
            }

        } else if( clientAuthStatus.isInvalidIdentity() ) {
            // kick him out
            deleteLidCookie     = Boolean.TRUE;
            deleteSessionCookie = Boolean.TRUE;
            createNewSession    = Boolean.FALSE;

            if( preexistingSession != null ) {
                sessionsToCancel.add( preexistingSession );
            }

        } else if( clientAuthStatus.isCarryingInvalidCredential() ) {
            // at least one invalid credential -- cut off session

            deleteLidCookie     = Boolean.FALSE; // we can keep this one
            deleteSessionCookie = Boolean.TRUE;
            createNewSession    = Boolean.FALSE;

            if( preexistingSession != null ) {
                sessionsToCancel.add( preexistingSession );
            }

        } else if( clientAuthStatus.isCarryingValidCredential() ) {
            // client just successfully authenticated

            deleteLidCookie     = Boolean.FALSE;
            deleteSessionCookie = Boolean.FALSE;

            if( preexistingSession != null ) {
                if( preexistingSession.getClientIdentifier().equals( clientAuthStatus.getClientIdentifier() )) {
                    // always renew, whether still valid or not
                    sessionsToRenew.add( preexistingSession );
                    createNewSession = Boolean.FALSE;

                } else {
                    sessionsToCancel.add( preexistingSession );

                    createNewSession = Boolean.TRUE;
                    // we don't remove the session cookie -- a different value will be set anyway due to new session
                }
            } else {
                createNewSession = Boolean.TRUE;
            }

            lidCookieIdentifierToSet = clientAuthStatus.getClientIdentifier();

        } else if( clientAuthStatus.clientWishesToCancelSession()) {

            deleteLidCookie     = Boolean.FALSE;
            deleteSessionCookie = Boolean.TRUE;
            createNewSession    = Boolean.FALSE;

            if( preexistingSession != null ) {
                sessionsToCancel.add( preexistingSession );
            }

        } else if( clientAuthStatus.clientWishesToLogout()) {

            deleteLidCookie     = Boolean.TRUE;
            deleteSessionCookie = Boolean.TRUE;
            createNewSession    = Boolean.FALSE;

            if( preexistingSession != null ) {
                sessionsToCancel.add( preexistingSession );
            }

        } else if( clientAuthStatus.isValidSessionOnly() ) {
            // still valid session
            deleteLidCookie     = Boolean.FALSE;
            deleteSessionCookie = Boolean.FALSE;
            createNewSession    = Boolean.FALSE;

        } else if(    clientAuthStatus.isClaimedOnly()
                   || clientAuthStatus.isExpiredSessionOnly() )
        {
            // valid user, but no valid session
            deleteLidCookie     = Boolean.FALSE;
            deleteSessionCookie = Boolean.TRUE;
            createNewSession    = Boolean.FALSE;

        } else {
            log.error( "Not sure how we got here: ", clientAuthStatus );

            deleteLidCookie     = Boolean.FALSE;
            deleteSessionCookie = Boolean.FALSE;
            createNewSession    = Boolean.FALSE;
        }

        SimpleLidSessionManagementInstructions ret;
        String sessionCookieStringToSet;

        if( createNewSession ) {
            sessionCookieStringToSet = theSessionManager.createNewSessionToken();

            ret = SimpleLidSessionManagementInstructions.create(
                    clientAuthStatus,
                    ArrayHelper.copyIntoNewArray( sessionsToCancel, LidSession.class ),
                    ArrayHelper.copyIntoNewArray( sessionsToRenew, LidSession.class ),
                    lidCookieIdentifierToSet,
                    clientAuthStatus.getSiteIdentifier(),
                    sessionCookieStringToSet,
                    theSessionManager.getDefaultSessionDuration() );
        } else {
            sessionCookieStringToSet = null;
            
            ret = SimpleLidSessionManagementInstructions.create(
                    clientAuthStatus,
                    ArrayHelper.copyIntoNewArray( sessionsToCancel, LidSession.class ),
                    ArrayHelper.copyIntoNewArray( sessionsToRenew, LidSession.class ));
        }

        String cookieDomain = "";
        String cookiePath   = "/";
        
        if( clientAuthStatus.getSiteIdentifier() != null ) {
            String siteString = clientAuthStatus.getSiteIdentifier().toExternalForm();
            
            Matcher m = theUrlPattern.matcher( siteString );
            if( m.find() ) {
                cookieDomain = m.group( 1 );
                cookiePath   = m.group( 2 );
            }
        }

        // delete cookies if needed, but only if they have been sent in the request
        String lidCookieName     = determineLidCookieName( realm );
        String sessionCookieName = determineSessionCookieName( realm );

        if( deleteLidCookie && lidRequest.getCookie( lidCookieName ) != null ) {
            ret.addCookieToRemove( lidCookieName, cookieDomain, cookiePath );
        }
        if( deleteSessionCookie && lidRequest.getCookie( sessionCookieName ) != null ) {
            ret.addCookieToRemove( sessionCookieName, cookieDomain, cookiePath );
        }

        if( lidCookieIdentifierToSet != null ) {
            ret.addCookieToSet(
                    lidCookieName,
                    lidCookieIdentifierToSet.toExternalForm(),
                    cookieDomain,
                    cookiePath,
                    LidCookies.LID_IDENTIFIER_COOKIE_DEFAULT_MAX_AGE );
        }
        if( createNewSession ) {
            ret.addCookieToSet(
                    sessionCookieName,
                    sessionCookieStringToSet,
                    cookieDomain,
                    cookiePath,
                    LidCookies.LID_SESSION_COOKIE_DEFAULT_MAX_AGE );
        }
        return ret;
    }

    /**
     * Given a realm, determine the LID cookie's name.
     *
     * @param realm the name of the realm
     * @return name of the LID cookie
     * @see #determineSessionCookieName()
     * @see AbstractLidClientAuthenticationPipelineStage#determineLidCookieName()
     */
    protected String determineLidCookieName(
            String realm )
    {
        if( realm == null ) {
            return LidCookies.LID_IDENTIFIER_COOKIE_NAME;
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append( LidCookies.LID_IDENTIFIER_COOKIE_NAME );
            buf.append( '-' );
            buf.append( realm );
            return buf.toString();
        }
    }

    /**
     * Given a realm, determine the LID session cookie's name.
     *
     * @param realm the name of the realm
     * @return name of the LID session cookie
     * @see #determineSessionCookieName()
     * @see AbstractLidClientAuthenticationPipelineStage#determineSessionCookieName()
     */
    protected String determineSessionCookieName(
            String realm )
    {
        if( realm == null ) {
            return LidCookies.LID_SESSION_COOKIE_NAME;
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append( LidCookies.LID_SESSION_COOKIE_NAME );
            buf.append( '-' );
            buf.append( realm );
            return buf.toString();
        }
    }

    /**
     * The LidSessionManager to use.
     */
    protected LidSessionManager theSessionManager;

    /**
     * Pattern to help extract cookie domain and path from a URL.
     */
    protected static final Pattern theUrlPattern = Pattern.compile( "^https?://([^/:]*)[^/]*(/[^\\?]*)" );
}
