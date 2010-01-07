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

import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.Identifier;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Factors out common functionality of LidClientAuthenticationStatus
 * implementations.
 */
public abstract class AbstractLidClientAuthenticationStatus
        implements
            LidClientAuthenticationStatus,
            CanBeDumped
{
    /**
     * Constructor for subclasses only.
     * 
     * @param clientIdentifier the normalized identifier provided by the client, if any
     * @param clientPersona the client LidPersona that was found, if any
     * @param preexistingClientSession the LidSession that existed prior to this request, if any
     * @param carriedValidCredentialTypes the credential types carried as part of this request that validated successfully, if any
     * @param carriedInvalidCredentialTypes the credential types carried as part of this request that did not validate successfully, if any
     * @param invalidCredentialExceptions the exceptions indicating the problems with the invalid credentials, in the same sequence, if any
     * @param clientLoggedOn the client just logged on
     * @param clientWishesToLogin the client wishes to log in
     * @param clientWishesToCancelSession the client wishes to cancel the session
     * @param clientWishesToLogout the client wishes to log out
     * @param authenticationServices the authentication services available to this client, if any
     * @param siteIdentifier identifies the site at which this status applies
     */
    protected AbstractLidClientAuthenticationStatus(
            Identifier                       clientIdentifier,
            HasIdentifier                    clientPersona,
            LidSession                       preexistingClientSession,
            LidCredentialType []             carriedValidCredentialTypes,
            LidCredentialType []             carriedInvalidCredentialTypes,
            LidInvalidCredentialException [] invalidCredentialExceptions,
            boolean                          clientLoggedOn,
            boolean                          clientWishesToLogin,
            boolean                          clientWishesToCancelSession,
            boolean                          clientWishesToLogout,
            LidAuthenticationService []      authenticationServices,
            Identifier                       siteIdentifier )
    {
        theClientIdentifier = clientIdentifier;
        theClientPersona    = clientPersona;

        thePreexistingClientSession = preexistingClientSession;
        
        theCarriedValidCredentialTypes   = carriedValidCredentialTypes;
        theCarriedInvalidCredentialTypes = carriedInvalidCredentialTypes;
        theInvalidCredentialExceptions   = invalidCredentialExceptions;

        theClientLoggedOn              = clientLoggedOn;
        theClientWishesToLogin         = clientWishesToLogin;
        theClientWishesToCancelSession = clientWishesToCancelSession;
        theClientWishesToLogout        = clientWishesToLogout;

        theSiteIdentifier = siteIdentifier;

        theAuthenticationServices = authenticationServices;
    }

    /**
     * Determine the Identifier of the site to which this LidClientAuthenticationStatus belongs.
     *
     * @return the Identifier of the site
     */
    public Identifier getSiteIdentifier()
    {
        return theSiteIdentifier;
    }

    /**
     * <p>Returns true if the client of this request did not present any kind of identification.</p>
     * 
     * @return true if the client of this request did not present any kind of identification
     */
    public boolean isAnonymous()
    {
        return theClientIdentifier == null;
    }

    /**
     * <p>Returns true of the client of this request claimed an identifier that could not be resolved into
     *    a valid LidPersona.</p>
     *
     * @return true if the client claimed an identifier as part of this request that could not be resolved into
     *         a valid LidPersona
     */
    public boolean isInvalidIdentity()
    {
        boolean ret;

        if( theClientIdentifier != null && theClientPersona == null ) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }

    /**
     * <p>Returns true if the client of this request merely claimed an identifier, but offered no valid credential
     *    (not even an expired cookie) to back up the claim.</p>
     * <p>Also returns true if a session id was offered (e.g. via a cookie) but the session id was unrecognized.
     *    It will return false if the session id was recognized, even if the session expired earlier.</p>
     * <p>Also returns true if an authentication attempt was made in this request, but the authentication
     *    attempted failed (e.g. wrong password). If the authentication attempt succeeded, this returns false as
     *    the identifier is not &quot;claimed only&quot; any more. To determine whether or not an authentication
     *    attempt was made, use {@link #isCarryingValidCredential}.</p>
     * 
     * @return true if client merely claimed an identifier as part of this request and no credential was offered
     */
    public boolean isClaimedOnly()
    {
        boolean ret;
        
        if( theClientIdentifier == null ) {
            ret = false;

        } else if(    theCarriedValidCredentialTypes != null
                   && theCarriedValidCredentialTypes.length > 0
                   && ( theCarriedInvalidCredentialTypes == null || theCarriedInvalidCredentialTypes.length == 0 ) )
        {
            // if we carry a valid credential and no invalid one
            ret = false;

        } else if( thePreexistingClientSession != null && thePreexistingClientSession.isStillValid() ) {
            ret = false;

        } else {
            ret = true;
        }
        return ret;
    }
    
    /**
     * <p>Returns true if the client of this request merely presented an identifier and an expired session id (e.g.
     *    via a cookie) as  credential to back up the claim.</p>
     * <p>For this to return true, the session id must have been valid in the past. If the session id is not recognized,
     *    or the session id is still valid, this will return false.</p>
     * 
     * @return true if client merely provided an expired session id as a credential for this request
     */
    public boolean isExpiredSessionOnly()
    {
        boolean ret;
        
        if( theClientIdentifier == null ) {
            ret = false;
        } else if( thePreexistingClientSession == null ) {
            ret = false;
        } else if( thePreexistingClientSession.isStillValid() ) {
            ret = false;
        } else {
            ret = true;
        }
        return ret;
    }
    
    /**
     * <p>Returns true if the client of this request was authenticated using a still-valid session identifier only
     *    (e.g. via cookie) and no stronger valid credential was offered as part of the request.</p>
     * <p>Also returns true if a stronger credential was offered in addition to the still-valid session cookie, but the
     *    stronger credential was invalid. To determine whether or not such an attempt was made, use
     *    {@link #isCarryingValidCredential}.</p>
     * 
     * @return true if client merely provided a valid session cookie as a credential for this request
     */
    public boolean isValidSessionOnly()
    {
        boolean ret;
        
        if( theClientIdentifier == null ) {
            ret = false;
        } else if( thePreexistingClientSession == null ) {
            ret = false;
        } else if( !thePreexistingClientSession.isStillValid() ) {
            ret = false;
        } else {
            ret = true;
        }
        return ret;
    }
    
    /**
     * <p>Determine whether the client of this request offered a valid credential stronger than a session id
     *    for this request. To determine which valid credential type or types were offered, see
     *    {@link #getCarriedValidCredentialTypes}.</p>
     * 
     * @return true if the client provided a valid credential for this request that is stronger than a session identifier
     */
    public boolean isCarryingValidCredential()
    {
        LidCredentialType [] found = getCarriedValidCredentialTypes();
        return found != null && found.length > 0;
    }
    
    /**
     * <p>Determine whether the client of this request offered an invalid credential stronger than a session id
     *    for this request. To determine which invalid credential type or types were offered, see
     *    {@link #getCarriedInvalidCredentialTypes}.</p>
     * 
     * @return true if the client provided an invalid credential for this request that is stronger than a session identifier
     */
    public boolean isCarryingInvalidCredential()
    {
        LidCredentialType [] found = getCarriedInvalidCredentialTypes();
        return found != null && found.length > 0;
    }
    
    /**
     * <p>Determine the set of credential types stronger than a session id that were offered by the
     *    client for this request and that were valid.</p>
     * <p>This returns null if none such credential type was offered, regardless of whether any were valid or not.
     *    It returns an empty array if at least one credential type was offered, but none were valid.</p>
     * <p>For example, if a request carried 5 different credential types, of which 3 validated and 2 did not, this method
     *    would return the 3 validated credential types.</p>
     * 
     * @return the types of validated credentials provided by the client for this request, or null if none
     * @see #getCarriedInvalidCredentialTypes
     */
    public LidCredentialType [] getCarriedValidCredentialTypes()
    {
        return theCarriedValidCredentialTypes;
    }

    /**
     * <p>Determine the set of credential types stronger than a session id that  were offered by the
     *    client for this request and that were not valid.</p>
     * <p>This returns null if non such credential type was offered, regardless of whether any were valid or not.
     *    It returns an empty array if at least one credential type was offered, and all were valid.</p>
     * <p>For example, if a request carried 5 different credential types, of which 3 validated and 2 did not, this method
     *    would return the 2 invalid credential types.</p>
     * 
     * @return the types of invalid credentials provided by the client for this request, or null if none
     * @see #getCarriedValidCredentialTypes
     */
    public LidCredentialType [] getCarriedInvalidCredentialTypes()
    {
        return theCarriedInvalidCredentialTypes;
    }

    /**
     * <p>Obtain the set of LidInvalidCredentialExceptions that correspond to the carried invalid credential types.
     *    By making those available, user-facing error reporting is more likely going to be more useful as
     *    different subclasses of LidInvalidCredentialException can report different error messages.</p>
     * 
     * @return the LidInvalidCredentialExceptions, in the same sequence as getCarriedInvalidCredentialTypes
     * @see #getCarriedInvalidCredentialTypes
     */
    public LidInvalidCredentialException [] getInvalidCredentialExceptions()
    {
        return theInvalidCredentialExceptions;
    }

    /**
     * Obtain the identifier of the client. To determine whether to trust that the client indeed
     * owns this identifier, other methods need to be consulted. This method makes no statement 
     * about trustworthiness.
     * 
     * @return the claimed client identifier
     */
    public Identifier getClientIdentifier()
    {
        return theClientIdentifier;
    }
    
    /**
     * Obtain what we know about the client with this client identifier here locally.
     * 
     * @return the LidPersona
     */
    public HasIdentifier getClientPersona()
    {
        return theClientPersona;
    }
    
    /**
     * Determine whether the client just logged on.
     *
     * @return true if the client just logged on
     */
    public boolean clientLoggedOn()
    {
        return theClientLoggedOn;
    }

    /**
     * Determine whether the client has indicated its desire to log in.
     *
     * @return true if the client wishes to log in
     */
    public boolean clientWishesToLogin()
    {
        return theClientWishesToLogin;
    }

    /**
     * Determine whether the client has indicated its desire to cancel the active session, if any.
     * This does not mean the client wishes to become anonymous (that would be expressed as getClientPersona()==null
     * with a non-null getSessionBelongsToPersona()) but that the client wishes to move from authenticated
     * status to claimed only.
     * 
     * @return true if the client wishes to cancel the active session.
     */
    public boolean clientWishesToCancelSession()
    {
        return theClientWishesToCancelSession;
    }

    /**
     * Determine whether the client has indicated its desire to log out if the active session, if any.
     * This means the client wishes to become anonymous.
     *
     * @return true of the client wishes to become anonymous again
     */
    public boolean clientWishesToLogout()
    {
        return theClientWishesToLogout;
    }

    /**
     * Get the client session, if any.
     *
     * @return the client session, if any
     */
    public LidSession getPreexistingClientSession()
    {
        return thePreexistingClientSession;
    }

    /**
     * Obtain the authentication services available for this client, if any.
     * This will return recommended authentication services first.
     *
     * @return the authentication services
     */
    public LidAuthenticationService [] getAuthenticationServices()
    {
        return theAuthenticationServices;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theClientIdentifier",
                    "theClientPersona",
                    "thePreexistingClientSession",
                    "theCarriedValidCredentialTypes",
                    "theCarriedInvalidCredentialTypes",
                    "theInvalidCredentialExceptions",
                    "theClientLoggedOn",
                    "theWishesCancelSession",
                    "theClientWishesToLogout",
                    "theAuthenticationServices"
                }, new Object[] {
                    theClientIdentifier,
                    theClientPersona,
                    thePreexistingClientSession,
                    theCarriedValidCredentialTypes,
                    theCarriedInvalidCredentialTypes,
                    theInvalidCredentialExceptions,
                    theClientLoggedOn,
                    theClientWishesToCancelSession,
                    theClientWishesToLogout,
                    theAuthenticationServices
                });
    }

    /**
     * The normalized identifier provided by the client.
     */
    protected Identifier theClientIdentifier;
    
    /**
     * The determined client HasIdentifier.
     */
    protected HasIdentifier theClientPersona;
    
    /**
     * The credential types that were provided by the client as part of this request and that
     * were successfully validated.
     */
    protected LidCredentialType [] theCarriedValidCredentialTypes;
    
    /**
     * The credential types that were NOT successfully validated as part of this request,
     * although they were provided by the client
     */
    protected LidCredentialType [] theCarriedInvalidCredentialTypes;
    
    /**
     * The exceptions reflecting the issues with validation of the invalid credential types. They
     * are given in the same sequence as theCarriedInvalidCredentialTypes.
     */
    protected LidInvalidCredentialException [] theInvalidCredentialExceptions;

    /**
     * Client has just logged on.
     */
    protected boolean theClientLoggedOn;

    /**
     * Client has indicated that a login should be performed.
     */
    protected boolean theClientWishesToLogin;

    /**
     * Client has indicated that the session should be canceled.
     */
    protected boolean theClientWishesToCancelSession;

    /**
     * Client has indicated that a logout should be performed.
     */
    protected boolean theClientWishesToLogout;

    /**
     * The pre-existing session, if any.
     */
    protected LidSession thePreexistingClientSession;

    /**
     * Identifies the site to which this status applies.
     */
    protected Identifier theSiteIdentifier;

    /**
     * The authentication services, if any.
     */
    protected LidAuthenticationService [] theAuthenticationServices;
}
