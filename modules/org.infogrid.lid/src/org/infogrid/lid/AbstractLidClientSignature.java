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

import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Captures the information provided by the client that is relevant to authenticate a request.
 */
public abstract class AbstractLidClientSignature
        implements
            LidClientSignature
{
    private static final Log log = Log.getLogInstance( AbstractLidClientSignature.class ); // our own, private logger

    /**
     * Constructor for subclasses only.
     * 
     * @param request the incoming request
     * @param rpUrl the Relying Party's URL without identity parameters
     * @param identifier the provided identifier
     * @param credential the provided credential
     * @param lidCookieString the identifier held by the LID identifier cookie, if any
     * @param sessionId the content of the LID session cookie, if any
     * @param target the user's destination URL, if any
     * @param realm the realm of the trust request, if any
     * @param nonce the nonce in the request, if any
     * @param nonceManager the LidNonceManager to use to validate the nonce
     */
    protected AbstractLidClientSignature(
            SaneRequest     request,
            String          rpUrl,
            String          identifier,
            String          credential,
            String          lidCookieString,
            String          sessionId,
            String          target,
            String          realm,
            String          nonce,
            LidNonceManager nonceManager )
    {
        theRequest             = request;
        theRelyingPartyUrl     = rpUrl;
        theIdentifier          = identifier;
        theCredential          = credential;
        theSaneCookieString    = lidCookieString;
        theSessionId           = sessionId;
        theTarget              = target;
        theRealm               = realm;
        theNonce               = nonce;
        theNonceManager        = nonceManager;
    }
    
    /**
     * Obtain the SaneRequest from which this LidClientSignature was derived.
     * 
     * @return the LSaneRequest
     */
    public final SaneRequest getSaneRequest()
    {
        return theRequest;
    }

    /**
     * Obtain the URL of the Relying Party without any identity parameters/
     * 
     * @return the URL of the Relying Party
     */
    public String getRelyingPartyUrl()
    {
        return theRelyingPartyUrl;
    }

    /**
     * Obtain the identifier of the client.
     *
     * @return the identifier of the client
     */
    public String getIdentifier()
    {
        return theIdentifier;
    }

    /**
     * Obtain the value of the LID cookie, if any.
     *
     * @return the value of the LID cookie, if any
     */
    public String getSaneCookieString()
    {
        return theSaneCookieString;
    }

    /**
     * Determine whether this request causes a logout event.
     *
     * @return true if this causes a logout event, false otherwise
     */
    public boolean isLogoutEvent()
    {
        return theSaneCookieString != null && ( theIdentifier == null );
    }
    
    /**
     * Obtain the credential provided by the client, if any.
     *
     * @return the credential provided by the client, or null
     */
    public String getCredential()
    {
        return theCredential;
    }

    /**
     * Obtain the credential type provided by the client, if any.
     *
     * @return the credential type provided by the client, or null
     */
    public abstract String getCredentialType();

    /**
     * Is this Request signed. Does not imply that the signature was valid.
     *
     * @return true if the request is signed
     */
    public boolean isSignedRequest()
    {
        return theCredential != null;
    }

    /**
     * Is this Request signed, and if so, is the signature good.
     *
     * @return true if the Request is signed and the signature is good, false otherwise
     */
    public final boolean isSignedGoodRequest()
    {
        if( !isSignedRequest() ) {
            return false;
        }
        if( theNonce != null ) {
            if( !theNonceManager.validateNonce( theNonce )) {
                return false;
            }
        }
        
        boolean ret = determineSignedGoodRequest();
        return ret;
    }

    /**
     * The internal method that determines whether or not a request was signed, and if so,
     * whether the signature is any good.
     *
     * @return true if the Request is signed and the signature is good, false otherwise
     */
    protected abstract boolean determineSignedGoodRequest();

    /**
     * Determine the session id that was provided as part of this Request,
     * regardless of whether it is valid. This class does not know whether or not
     * the session is valid anyway.
     *
     * @return the session id
     */
    public String getSessionId()
    {
        return theSessionId;
    }

    /**
     * Obtain the target that is part of this request, if any.
     *
     * @return the target
     */
    public String getTarget()
    {
        return theTarget;
    }

    /**
     * Obtain the realm that is part of this request, if any.
     *
     * @return the realm
     */
    public String getRealm()
    {
        return theRealm;
    }

    /**
     * Obtain the nonce that is part of this request, if any.
     *
     * @return the nonce
     */
    public String getNonce()
    {
        return theNonce;
    }

    /**
     * The incoming SaneRequest.
     */
    protected SaneRequest theRequest;

    /**
     * The RelyingParty URL without identity parameters.
     */
    protected String theRelyingPartyUrl;
    
    /**
     * The provided identifier of the client.
     */
    protected String theIdentifier;

    /**
     * The value of the LID cookie.
     */
    protected String theSaneCookieString;

    /**
     * The provided session id.
     */
    protected String theSessionId;

    /**
     * The target.
     */
    protected String theTarget;

    /**
     * The realm.
     */
    protected String theRealm;

    /**
     * The provided credential.
     */
    protected String theCredential;

    /**
     * The provided nonce.
     */
    protected String theNonce;

    /**
     * The LidNonceManager to use.
     */
    protected LidNonceManager theNonceManager;
}
