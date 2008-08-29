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
 * A LID "claimed identifier" signature with no credential.
 */
public class LidClaimedClientSignature
        extends
            AbstractLidClientSignature
{
    /**
     * Constructor.
     * 
     * @param request the incoming request
     * @param rpUrl the Relying Party's URL without identity parameters
     * @param identifier the provided identifier
     * @param lidCookieString the identifier held by the LID identifier cookie, if any
     * @param sessionId the content of the LID session cookie, if any
     * @param target the user's destination URL, if any
     * @param realm the realm of the trust request, if any
     * @param nonce the nonce in the request, if any
     * @param nonceManager the LidNonceManager to use to validate the nonce
     */
    public LidClaimedClientSignature(
            SaneRequest     request,
            String          rpUrl,
            String          identifier,
            String          lidCookieString,
            String          sessionId,
            String          target,
            String          realm,
            String          nonce,
            LidNonceManager nonceManager )
    {
        super( request, rpUrl, identifier, null, lidCookieString, sessionId, target, realm, nonce, nonceManager );
    }

    /**
     * Obtain the credential type provided by the client, if any.
     *
     * @return the credential type provided by the client, or null
     */
    public String getCredentialType()
    {
        return null;
    }

    /**
     * The internal method that determines whether or not a request was signed, and if so,
     * whether the signature is any good.
     *
     * @return this class always returns false
     */
    protected boolean determineSignedGoodRequest()
    {
        return false;
    }

    /**
     * Obtain the URL of the identity provider's endpoint.
     * 
     * @return the identifier of the identity provider's endpoint, if any
     */
    public String getIdpEndpointIdentifier()
    {
        return null;
    }
}
