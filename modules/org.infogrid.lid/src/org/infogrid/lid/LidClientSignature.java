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

/**
 * Captures the information provided by the client to a Relying Party.
 */
public interface LidClientSignature
{
    /**
     * Obtain the SaneRequest from which this LidClientSignature was derived.
     * 
     * @return the LSaneRequest
     */
    public SaneRequest getSaneRequest();

    /**
     * Obtain the URL of the Relying Party without any identity parameters/
     * 
     * @return the URL of the Relying Party
     */
    public String getRelyingPartyUrl();

    /**
     * Obtain the identifier of the client.
     *
     * @return the identifier of the client
     */
    public String getIdentifier();

    /**
     * Obtain the value of the LID cookie, if any.
     *
     * @return the value of the LID cookie, if any
     */
    public String getSaneCookieString();

    /**
     * Determine whether this request causes a logout event.
     *
     * @return true if this causes a logout event, false otherwise
     */
    public boolean isLogoutEvent();
    
    /**
     * Obtain the credential provided by the client, if any.
     *
     * @return the credential provided by the client, or null
     */
    public String getCredential();

    /**
     * Obtain the credential type provided by the client, if any.
     *
     * @return the credential type provided by the client, or null
     */
    public String getCredentialType();

    /**
     * Is this Request signed. Does not imply that the signature was valid.
     *
     * @return true if the request is signed
     */
    public boolean isSignedRequest();

    /**
     * Is this Request signed, and if so, is the signature good.
     *
     * @return true if the Request is signed and the signature is good, false otherwise
     */
    public boolean isSignedGoodRequest();

    /**
     * Determine the session id that was provided as part of this Request,
     * regardless of whether it is valid. This class does not know whether or not
     * the session is valid anyway.
     *
     * @return the session id
     */
    public String getSessionId();

    /**
     * Obtain the target that is part of this request, if any.
     *
     * @return the target
     */
    public String getTarget();

    /**
     * Obtain the realm parameter that is part of this request, if any.
     * 
     * @return the realm
     */
    public String getRealm();

    /**
     * Obtain the nonce that is part of this request, if any.
     *
     * @return the nonce
     */
    public String getNonce();
    
    /**
     * Obtain the URL of the identity provider's endpoint.
     * 
     * @return the identifier of the identity provider's endpoint, if any
     */
    public String getIdpEndpointIdentifier();
}
