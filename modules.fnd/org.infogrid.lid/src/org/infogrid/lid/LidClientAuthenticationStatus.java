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

import java.util.Date;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;

/**
 * Collects in one place all information known about the current client and how the client authenticated.
 */
public interface LidClientAuthenticationStatus
{
    /**
     * <p>Returns true if the client of this request did not present any kind of identification.</p>
     * 
     * @return true if the client of this request did not present any kind of identification
     */
    public abstract boolean isAnonymous();

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
    public abstract boolean isClaimedOnly();
    
    /**
     * <p>Returns true if the client of this request merely presented an identifier and an expired session id (e.g.
     *    via a cookie) as  credential to back up the claim.</p>
     * <p>For this to return true, the session id must have been valid in the past. If the session id is not recognized,
     *    or the session id is still valid, this will return false.</p>
     * 
     * @return true if client merely provided an expired session id as a credential for this request
     */
    public abstract boolean isExpiredSessionOnly();
    
    /**
     * <p>Returns true if the client of this request was authenticated using a still-valid session identifier only
     *    (e.g. via cookie) and no stronger valid credential was offered as part of the request.</p>
     * <p>Also returns true if a stronger credential was offered in addition to the still-valid session cookie, but the
     *    stronger credential was invalid. To determine whether or not such an attempt was made, use
     *    {@link #isCarryingValidCredential}.</p>
     * 
     * @return true if client merely provided a valid session cookie as a credential for this request
     */
    public abstract boolean isValidSessionOnly();
    
    /**
     * <p>Determine whether the client of this request offered a valid credential stronger than a session id
     *    for this request. To determine which valid credential type or types were offered, see
     *    {@link #getValidCredentialTypes}.</p>
     * 
     * @return true if the client provided a valid credential for this request that is stronger than a session identifier
     */
    public abstract boolean isCarryingValidCredential();
    
    /**
     * <p>Determine whether the client of this request offered an invalid credential stronger than a session id
     *    for this request. To determine which invalid credential type or types were offered, see
     *    {@link #getInvalidCredentialTypes}.</p>
     * 
     * @return true if the client provided an invalid credential for this request that is stronger than a session identifier
     */
    public abstract boolean isCarryingInvalidCredential();
    
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
    public abstract LidCredentialType [] getCarriedValidCredentialTypes();

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
    public abstract LidCredentialType [] getCarriedInvalidCredentialTypes();

    /**
     * <p>Obtain the set of LidInvalidCredentialExceptions that correspond to the carried invalid credential types.
     *    By making those available, user-facing error reporting is more likely going to be more useful as
     *    different subclasses of LidInvalidCredentialException can report different error messages.</p>
     * 
     * @return the LidInvalidCredentialExceptions, in the same sequence as getCarriedInvalidCredentialTypes
     * @see #getCarriedInvalidCredentialTypes
     */
    public abstract LidInvalidCredentialException [] getInvalidCredentialExceptions();
    
    /**
     * Obtain the time this session with this client was last validated with a proof stronger
     * than a session id (e.g. a cookie).
     * 
     * @return the time, or null if never
     */
    public abstract Date getSessionLastValidated();

    /**
     * Obtain the time this session with this client was last validated with a particular credential type.
     * 
     * @param type the credential type
     * @return the time, or null if never
     */
    // FIXME? Not yet
    // public abstract Date getSessionLastValidatedWith(
    //         LidCredentialType type );

    /**
     * Obtain the time this session with this client last interacted with this application while being valid.
     * 
     * @return the time, or null if never
     */
    public abstract Date getSessionLastUsedAndValid();

    // FIXME: we probably also want to know the authentication history with a particular credential type. For
    // example, I might want to know what while the client authenticated with username and password, there were
    // 10,000 attempts from the same IP address before that failed.

    /**
     * Obtain the time this session expired. If there is no session, or the session is still valid, this returns null.
     * 
     * @return thed time, or null if never
     */
    public abstract Date getSessionExpired();

    /**
     * Obtain the identifier of the client. To determine whether to trust that the client indeed
     * owns this identifier, other methods need to be consulted. This method makes no statement 
     * about trustworthiness.
     * 
     * @return the claimed client identifier
     */
    public abstract String getClientIdentifier();
    
    /**
     * Obtain what we know about the client with this client identifier here locally.
     * 
     * @return the LidPersona
     */
    public abstract LidPersona getClientPersona();
    
    /**
     * Determine whether the client has indicated its desire to cancel the active session, if any.
     * 
     * @return true if the client wishes to cancel the active session.
     */
    public abstract boolean clientWishesCancelSession();
    
    /**
     * Determine whether the client has indicated its desire to become fully anonymous again.
     * 
     * @return true if the client wishes to become fully anonymous again
     */
    public abstract boolean clientWishesAnonymous();
}
