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
import java.util.Date;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.FactoryException;
import org.infogrid.util.Identifier;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.OutgoingSaneCookie;
import org.infogrid.util.http.OutgoingSimpleSaneCookie;
import org.infogrid.util.logging.Log;

/**
 * Simple implementation of LidSessionManagementInstructions.
 */
public class SimpleLidSessionManagementInstructions
        implements
            LidSessionManagementInstructions
{
    private static final Log log = Log.getLogInstance( SimpleLidSessionManagementInstructions.class ); // our own, private logger

    /**
     * Factory method if a new session shall be created.
     *
     * @param clientAuthenticationStatus the current client's authentication status
     * @param sessionsToCancel all sessions to cancel
     * @param sessionToRenew all sessions to renew
     * @param clientIdentifierForNewSession  Identifier of the client for which a new session shall be created
     * @param siteIdentifierForNewSession Identifier of the site for which a new session shall be created
     * @param tokenForNewSession token for the new session
     * @param durationForNewSession duration for the new session, in milliseconds
     * @return the created SimpleLidSessionManagementInstructions
     */
    public static SimpleLidSessionManagementInstructions create(
            LidClientAuthenticationStatus clientAuthenticationStatus,
            LidSession []                 sessionsToCancel,
            LidSession []                 sessionToRenew,
            Identifier                    clientIdentifierForNewSession,
            Identifier                    siteIdentifierForNewSession,
            String                        tokenForNewSession,
            long                          durationForNewSession )
    {
        return new SimpleLidSessionManagementInstructions(
                clientAuthenticationStatus,
                sessionsToCancel,
                sessionToRenew,
                clientIdentifierForNewSession,
                siteIdentifierForNewSession,
                tokenForNewSession,
                durationForNewSession );
    }

    /**
     * Factory method if no new session shall be created.
     *
     * @param clientAuthenticationStatus the current client's authentication status
     * @param sessionsToCancel all sessions to cancel
     * @param sessionToRenew all sessions to renew
     * @return the created SimpleLidSessionManagementInstructions
     */
    public static SimpleLidSessionManagementInstructions create(
            LidClientAuthenticationStatus clientAuthenticationStatus,
            LidSession []                 sessionsToCancel,
            LidSession []                 sessionToRenew )
    {
        return new SimpleLidSessionManagementInstructions(
                clientAuthenticationStatus,
                sessionsToCancel,
                sessionToRenew,
                null,
                null,
                null,
                -1L );
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param clientAuthenticationStatus the current client's authentication status
     * @param sessionsToCancel all sessions to cancel
     * @param sessionToRenew all sessions to renew
     * @param clientIdentifierForNewSession  Identifier of the client for which a new session shall be created
     * @param siteIdentifierForNewSession Identifier of the site for which a new session shall be created
     * @param tokenForNewSession token for the new session
     * @param durationForNewSession duration for the new session, in milliseconds
     */
    protected SimpleLidSessionManagementInstructions(
            LidClientAuthenticationStatus clientAuthenticationStatus,
            LidSession []                 sessionsToCancel,
            LidSession []                 sessionToRenew,
            Identifier                    clientIdentifierForNewSession,
            Identifier                    siteIdentifierForNewSession,
            String                        tokenForNewSession,
            long                          durationForNewSession )
    {
        theClientAuthenticationStatus = clientAuthenticationStatus;

        theSessionsToCancel = sessionsToCancel;
        theSessionsToRenew  = sessionToRenew;

        theClientIdentifierForNewSession = clientIdentifierForNewSession;
        theSiteIdentifierForNewSession   = siteIdentifierForNewSession;

        theTokenForNewSession    = tokenForNewSession;
        theDurationForNewSession = durationForNewSession;
    }

    /**
     * Obtain the current client's authentication status.
     *
     * @return the current client's authentication status
     */
    public LidClientAuthenticationStatus getClientAuthenticationStatus()
    {
        return theClientAuthenticationStatus;
    }

    /**
     * Obtain the LidSessions to cancel, if any.
     *
     * @return the LidSessions to cancel, if any
     */
    public LidSession [] getSessionsToCancel()
    {
        return theSessionsToCancel;
    }

    /**
     * Obtain the LidSessions to renew, if any.
     *
     * @return the LidSessions to renew, if any
     */
    public LidSession [] getSessionsToRenew()
    {
        return theSessionsToRenew;
    }

    /**
     * Obtain the Identifier of the client for which a new session shall be created, if any.
     *
     * @return the client Identifier
     */
    public Identifier getClientIdentifierForNewSession()
    {
        return theClientIdentifierForNewSession;
    }

    /**
     * Obtain the Identifier of the site for which a new session shall be created, if any.
     *
     * @return the site Identifier
     */
    public Identifier getSiteIdentifierForNewSession()
    {
        return theSiteIdentifierForNewSession;
    }

    /**
     * Obtain the initial token for the to-be-created new session, if any.
     *
     * @return the initial token
     */
    public String getTokenForNewSession()
    {
        return theTokenForNewSession;
    }

    /**
     * Obtain the duration, in milliseconds, for the to-be-created new session, if any.
     *
     * @return the duration, or -1L
     */
    public long getDurationOfNewSession()
    {
        return theDurationForNewSession;
    }

    /**
     * Add a cookie to be removed.
     *
     * @param name name of the cookie
     * @param domain domain of the cookie, if any
     * @param path path of the cookie, if any
     */
    public void addCookieToRemove(
            String name,
            String domain,
            String path )
    {
        if( theCookiesToRemove == null ) {
            theCookiesToRemove = new ArrayList<OutgoingSimpleSaneCookie>();
        }
        theCookiesToRemove.add(
                OutgoingSimpleSaneCookie.create( name, "", domain, path, LONG_TIME_AGO ));
    }

    /**
     * Obtain the cookies that shall be removed.
     *
     * @return the cookies, if any
     */
    public OutgoingSaneCookie [] getCookiesToRemove()
    {
        if( theCookiesToRemove == null ) {
            return new OutgoingSaneCookie[0];
        } else {
            return ArrayHelper.copyIntoNewArray( theCookiesToRemove, OutgoingSaneCookie.class );
        }
    }

    /**
     * Add a cookie to set.
     *
     * @param name name of the cookie
     * @param value value of the cookie
     * @param domain domain of the cookie, if any
     * @param path path of the cookie, if any
     * @param expiresInSeconds number of seconds until the cookie expires. If negative, it means session cookie.
     */
    public void addCookieToSet(
            String name,
            String value,
            String domain,
            String path,
            int    expiresInSeconds )
    {
        if( theCookiesToSet == null ) {
            theCookiesToSet = new ArrayList<OutgoingSimpleSaneCookie>();
        }
        Date expires;
        if( expiresInSeconds < 0 ) {
            expires = null;
        } else {
            expires = new Date( System.currentTimeMillis() + 1000L * expiresInSeconds );
        }
        theCookiesToSet.add(
                OutgoingSimpleSaneCookie.create( name, value, domain, path, expires ));
    }

    /**
     * Obtain the cookies that shall be set.
     *
     * @return the cookies, if any
     */
    public OutgoingSaneCookie [] getCookiesToSet()
    {
        if( theCookiesToSet == null ) {
            return new OutgoingSaneCookie[0];
        } else {
            return ArrayHelper.copyIntoNewArray( theCookiesToSet, OutgoingSaneCookie.class );
        }
    }

    /**
     * Apply all instructions as recommended.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @param sessionManager the LidSessionManager to use
     */
    public void applyAsRecommended(
            SaneServletRequest  request,
            HttpServletResponse response,
            LidSessionManager   sessionManager )
    {
        if( theSessionsToCancel != null ) {
            for( LidSession current : theSessionsToCancel ) {
                current.cancel();
            }
        }
        if( theSessionsToRenew != null ) {
            for( LidSession current : theSessionsToRenew ) {
                current.renew( sessionManager.getDefaultSessionDuration() );
            }
        }

        LidSession newSession; // out here for debugging
        if( theClientIdentifierForNewSession != null ) {
            try {
                newSession = sessionManager.obtainFor(
                        theTokenForNewSession,
                        LidSessionManagerArguments.create(
                                sessionManager.getDefaultSessionDuration(),
                                theClientIdentifierForNewSession,
                                theSiteIdentifierForNewSession,
                                request.getClientIp() ));

            } catch( FactoryException ex ) {
                log.error( ex );
            }
        }

        if( theCookiesToRemove != null ) {
            for( OutgoingSimpleSaneCookie current : theCookiesToRemove ) {
                Cookie toAdd = new Cookie( HTTP.encodeCookieName( current.getName()), current.getValue() );
                if( current.getDomain() != null ) {
                    toAdd.setDomain( current.getDomain() );
                }
                if( current.getPath() != null ) {
                    toAdd.setPath( current.getPath() );
                }
                toAdd.setMaxAge( 0 ); // delete

                response.addCookie( toAdd );
            }
        }
        if( theCookiesToSet != null ) {
            for( OutgoingSimpleSaneCookie current : theCookiesToSet ) {
                Cookie toAdd = new Cookie( HTTP.encodeCookieName( current.getName()), current.getValue() );
                if( current.getDomain() != null ) {
                    toAdd.setDomain( current.getDomain() );
                }
                if( current.getPath() != null ) {
                    toAdd.setPath( current.getPath() );
                }
                if( current.getExpires() != null ) {
                    long ageInMillis = current.getExpires().getTime() - System.currentTimeMillis();
                    toAdd.setMaxAge( (int)( ageInMillis/1000L ) );
                }
                toAdd.setSecure( current.getSecure() );

                response.addCookie( toAdd );
            }
        }
    }

    /**
     * Convert to String format, for debugging.
     *
     * @return String format
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( getClass().getName() );
        buf.append( "{" );
        if( theClientAuthenticationStatus != null ) {
            buf.append( "\n    clientAuthenticationStatus: " );
            buf.append( "\n        clientLoggedOn: "       ).append( theClientAuthenticationStatus.clientLoggedOn() );
            buf.append( "\n        clientWishesToCancel: " ).append( theClientAuthenticationStatus.clientWishesToCancelSession() );
            buf.append( "\n        clientWishesToLogout: " ).append( theClientAuthenticationStatus.clientWishesToLogout() );
            // buf.append( "\n        clientLoggedOn: " ).append( theClientAuthenticationStatus.getAuthenticationServices() );
            if( theClientAuthenticationStatus.getCarriedInvalidCredentialTypes() != null ) {
                for( LidCredentialType current : theClientAuthenticationStatus.getCarriedInvalidCredentialTypes() ) {
                    buf.append( "\n        invalid credential: " ).append( current );
                }
            }
            if( theClientAuthenticationStatus.getCarriedValidCredentialTypes() != null ) {
                for( LidCredentialType current : theClientAuthenticationStatus.getCarriedValidCredentialTypes() ) {
                    buf.append( "\n        valid credential: " ).append( current );
                }
            }
            buf.append( "\n        clientIdentifier: " ).append( theClientAuthenticationStatus.getClientIdentifier() );
            buf.append( "\n        clientPersona: "    ).append( theClientAuthenticationStatus.getClientPersona() );
            // theClientAuthenticationStatus.getInvalidCredentialExceptions();
            buf.append( "\n        preexistingClientSession: " ).append( theClientAuthenticationStatus.getPreexistingClientSession() );
            if( theClientAuthenticationStatus.getSiteIdentifier() != null ) {
                buf.append( "\n        siteIdentifier: " ).append( theClientAuthenticationStatus.getSiteIdentifier().toExternalForm() );
            }
        }
        if( theSessionsToCancel != null ) {
            for( int i=0 ; i<theSessionsToCancel.length ; ++i ) {
                buf.append( "\n    session to cancel: " );
                buf.append( theSessionsToCancel[i] );
            }
        }
        if( theSessionsToRenew != null ) {
            for( int i=0 ; i<theSessionsToRenew.length ; ++i ) {
                buf.append( "\n    session to renew: " );
                buf.append( theSessionsToCancel[i] );
            }
        }
        if( theClientIdentifierForNewSession != null ) {
            buf.append( "\n    clientIdentifierForNewSession: " );
            buf.append( theClientIdentifierForNewSession.toExternalForm() );
        }
        if( theSiteIdentifierForNewSession != null ) {
            buf.append( "\n    siteIdentifierForNewSession: " );
            buf.append( theSiteIdentifierForNewSession.toExternalForm() );
        }
        if( theTokenForNewSession != null ) {
            buf.append( "\n    tokenForNewSession: " );
            buf.append( theTokenForNewSession );
        }
        if( theDurationForNewSession > 0 ) {
            buf.append( "\n    durationForNewSession: " );
            buf.append( theDurationForNewSession );
        }
        if( theCookiesToRemove != null ) {
            for( OutgoingSimpleSaneCookie current : theCookiesToRemove ) {
                buf.append( "\n    cookie to remove: " );
                buf.append( current.getAsHttpValue() );
            }
        }
        if( theCookiesToSet != null ) {
            for( OutgoingSimpleSaneCookie current : theCookiesToSet ) {
                buf.append( "\n    cookie to add: " );
                buf.append( current.getAsHttpValue() );
            }
        }
        buf.append( "\n}" );
        return buf.toString();
    }

    /**
     * The current client's authentication status.
     */
    protected LidClientAuthenticationStatus theClientAuthenticationStatus;

    /**
     * All sessions to cancel.
     */

    protected LidSession [] theSessionsToCancel;

    /**
     * All sessions to renew.
     */
    protected LidSession [] theSessionsToRenew;

    /**
     * Identifier of the client for which a new session shall be created.
     */
    protected Identifier theClientIdentifierForNewSession;

    /**
     * Identifier of the site for which a new session shall be created.
     */
    protected Identifier theSiteIdentifierForNewSession;

    /**
     * Token for the new session.
     */
    protected String theTokenForNewSession;

    /**
     * Duration of the new session.
     */
    protected long theDurationForNewSession;

    /**
     * Cookies to remove.
     */
    protected List<OutgoingSimpleSaneCookie> theCookiesToRemove;

    /**
     * Cookies to set.
     */
    protected List<OutgoingSimpleSaneCookie> theCookiesToSet;

    /**
     * Defines "long time ago".
     */
    protected static final Date LONG_TIME_AGO = new Date( 0L );
}
