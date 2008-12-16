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
import org.infogrid.util.Identifier;

/**
 * Factors out common functionality of LidClientAuthenticationStatus
 * implementations.
 */
public abstract class AbstractLidClientAuthenticationStatus
        implements
            LidClientAuthenticationStatus
{
    /**
     * Constructor for subclasses only.
     * 
     * @param clientIdentifier the normalized identifier provided by the client, if any
     * @param clientPersona the client LidPersona that was found, if any
     * @param session the client LidSession that was found, if any
     * @param carriedValidCredentialTypes the credential types carried as part of this request that validated successfully, if any
     * @param carriedInvalidCredentialTypes the credential types carried as part of this request that did not validate successfully, if any
     * @param invalidCredentialExceptions the exceptions indicating the problems with the invalid credentials, in the same sequence, if any
     * @param sessionClientIdentifier the normalized identifier of the client according to a currently valid session
     * @param sessionClientPersona the client LidPersona according to the currently valid session
     * @param wishesCancelSession the client wishes to cancel the session
     */
    protected AbstractLidClientAuthenticationStatus(
            Identifier                       clientIdentifier,
            LidPersona                       clientPersona,
            LidSession                       session,
            LidCredentialType []             carriedValidCredentialTypes,
            LidCredentialType []             carriedInvalidCredentialTypes,
            LidInvalidCredentialException [] invalidCredentialExceptions,
            Identifier                       sessionClientIdentifier,
            LidPersona                       sessionClientPersona,
            boolean                          wishesCancelSession )
    {
        theClientIdentifier = clientIdentifier;
        theClientPersona    = clientPersona;
        theClientSession    = session;
        
        theCarriedValidCredentialTypes   = carriedValidCredentialTypes;
        theCarriedInvalidCredentialTypes = carriedInvalidCredentialTypes;
        theInvalidCredentialExceptions   = invalidCredentialExceptions;
        
        theSessionClientIdentifier = sessionClientIdentifier;
        theSessionClientPersona    = sessionClientPersona;
        
        theWishesCancelSession = wishesCancelSession;
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
        } else if( theClientSession != null && theClientSession.isStillValid() ) {
            ret = false;
        } else {
            ret = true;
        }
        return ret;
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
        } else if( theClientSession == null ) {
            ret = false;
        } else if( theClientSession.isStillValid() ) {
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
        } else if( theClientSession == null ) {
            ret = false;
        } else if( !theClientSession.isStillValid() ) {
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
     * Obtain the time this session with this client was last validated with a proof stronger
     * than a session id (e.g. a cookie).
     * 
     * @return the time, or null if never
     */
    public Date getSessionLastValidated()
    {
        Date ret;
        
        if( theClientSession != null ) {
            ret = new Date( theClientSession.getTimeCreated() );
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Obtain the time this session with this client last interacted with this application while being valid.
     * 
     * @return the time, or null if never
     */
    public Date getSessionLastUsedAndValid()
    {
        Date ret;
        
        if( theClientSession != null ) {
            ret = new Date( theClientSession.getTimeRead() );
        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Obtain the time this session expired. If there is no session, or the session is still valid, this returns null.
     * 
     * @return thed time, or null if never
     */
    public Date getSessionExpired()
    {
        Date ret;
        
        if( theClientSession == null ) {
            ret = null;
        } else if( theClientSession.isStillValid() ) {
            ret = null;
        } else {
            ret = new Date( theClientSession.getTimeExpires());
        }
        return ret;
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
    public LidPersona getClientPersona()
    {
        return theClientPersona;
    }
    
    /**
     * Determine whether the client has indicated its desire to cancel the active session, if any.
     * This does not mean the client wishes to become anonymous (that would be expressed as getClientPersona()==null
     * with a non-null getSessionBelongsToPersona()) but that the client wishes to move from authenticated
     * status to claimed only.
     * 
     * @return true if the client wishes to cancel the active session.
     */
    public boolean clientWishesCancelSession()
    {
        return theWishesCancelSession;
    }
    
    /**
     * Determine the client of any authenticated session that was brought into this request. This may be
     * null in case the client just now authenticated. It may identify a different client if the client
     * logged off, or changed personas, with this request.
     * 
     * @return LidPersona representing the client identified by the session going into this request, if any
     * @see #getSessionBelongsToIdentifier() 
     */
    public LidPersona getSessionBelongsToPersona()
    {
        return theSessionClientPersona;
    }

    /**
     * Determine the identifier of the client of any authenticated session that was brought into this request.
     * This may be null in case the client just now authenticated. It may identify a different client if the
     * client logged off, or changed personas, with this request.
     * 
     * @return the identifier of the valid session going into this request, if any
     * @see #getSessionBelongsToPersona() 
     */
    public Identifier getSessionBelongsToIdentifier()
    {
        return theSessionClientIdentifier;
    }

    /**
     * The normalized identifier provided by the client.
     */
    protected Identifier theClientIdentifier;
    
    /**
     * The determined client LidPersona.
     */
    protected LidPersona theClientPersona;
    
    /**
     * The current session of the client.
     */
    protected LidSession theClientSession;
    
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
     * Client has indicated that the session should be canceled.
     */
    protected boolean theWishesCancelSession;

    /**
     * The normalized client identifier associated with the session, if any.
     */
    protected Identifier theSessionClientIdentifier;
    
    /**
     * The client LidPersona as determined from the session, if any.
     */
    protected LidPersona theSessionClientPersona;
}
