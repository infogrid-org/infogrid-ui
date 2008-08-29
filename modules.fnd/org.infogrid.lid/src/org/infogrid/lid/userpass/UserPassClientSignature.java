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

package org.infogrid.lid.userpass;

import org.infogrid.lid.LidIdentityManager;
import org.infogrid.lid.AbstractLidClientSignature;
import org.infogrid.lid.LidInvalidCredentialException;
import org.infogrid.lid.LidLocalPersonaUnknownException;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.http.SaneRequest;

/**
 * A username/password ClientSignature.
 */
public class UserPassClientSignature
        extends
            AbstractLidClientSignature
{
    /**
     * Constructor.
     * 
     * @param request the incoming request
     * @param rpUrl the Relying Party's URL without identity parameters
     * @param identifier the given username, if any
     * @param credentialType the credential type to use
     * @param credential the given password, if any
     * @param lidCookieString the identifier held by the LID identifier cookie, if any
     * @param sessionId the content of the LID session cookie, if any
     * @param target the user's destination URL, if any
     * @param realm the realm of the trust request, if any
     * @param nonce the nonce in the request, if any
     * @param nonceManager the LidNonceManager to use to validate the nonce
     * @param identityManager the LidIdentityManager to use to validate the password
     */
    public UserPassClientSignature(
            SaneRequest        request,
            String             rpUrl,
            String             identifier,
            LidCredentialType  credentialType,
            String             credential,
            String             lidCookieString,
            String             sessionId,
            String             target,
            String             realm,
            String             nonce,
            LidNonceManager    nonceManager,
            LidIdentityManager identityManager )
    {
        super( request, rpUrl, identifier, credential, lidCookieString, sessionId, target, realm, nonce, nonceManager );
        
        theCredentialType  = credentialType;
        theIdentityManager = identityManager;
    }

    /**
     * Obtain the credential type provided by the client, if any.
     *
     * @return the credential type provided by the client, or null
     */
    public String getCredentialType()
    {
        return USER_PASS_CREDENTIAL_TYPE;
    }

    /**
     * The internal method that determines whether or not a request was signed, and if so,
     * whether the signature is any good.
     *
     * @return true if the Request is signed and the signature is good, false otherwise
     */
    protected boolean determineSignedGoodRequest()
    {
        try {
            theIdentityManager.checkCredential( theIdentifier, theCredentialType, theCredential );

            return true;
            
        } catch( LidLocalPersonaUnknownException ex ) {
            return false;

        } catch( LidInvalidCredentialException ex ) {
            return false;
        }
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

    /**
     * The PasswordManager to use.
     */
    protected LidIdentityManager theIdentityManager;

    /**
     * The LidCredentialType to use.
     */
    protected LidCredentialType theCredentialType;

    /**
     * The name of the simple password credential type.
     */
    public static final String USER_PASS_CREDENTIAL_TYPE = "simple-password";
}
