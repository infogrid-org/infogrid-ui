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

package org.infogrid.lid.gpg;

import java.io.IOException;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.Identifier;
import org.infogrid.util.logging.Log;

/**
 * Abstracts the GPG tool functionality.
 */
public abstract class LidGpg
        extends
            AbstractFactory<Identifier,LidKeyPair,Void>
        implements
            LidGpgKeyPairFactory
{
    protected static final Log log = Log.getLogInstance( LidGpg.class ); // our own, private logger

    /**
     * Constructor for subclasses only.
     * 
     * @param nonceManager the LidNonceManager to use
     */
    protected LidGpg(
            LidNonceManager nonceManager )
    {
        theNonceManager = nonceManager;
    }

    /**
     * Generate private/public key pair for this LID.
     *
     * @param key the LID URL for which a key pair needs to be generated
     * @param arg ignored
     * @return the generated LidKeyPair
     * @throws FactoryException thrown if the LidKeyPair could not be created
     */
    public abstract LidKeyPair obtainFor(
            Identifier key,
            Void       arg )
        throws
            FactoryException;

    /**
     * Reconstruct a signed message from a URL, any POST'd data and a credential.
     *
     * @param url the URL
     * @param postData the data posted to the URL using HTTP POST, if any
     * @param credential the credential
     * @return the reconstructed signed message
     */
    public String reconstructSignedMessage(
            String url,
            String postData,
            String credential )
    {
        // try argument list first
        int credentialIndex = url.indexOf( "&credential=" );
        if( credentialIndex <= 0 ) {
            credentialIndex = url.indexOf( "&lid-credential=" );
        }
        if( credentialIndex >= 0 ) {
            url = url.substring( 0, credentialIndex );
        } else if( postData != null ) {
            // try postData because argument list didn't work

            credentialIndex = postData.indexOf( "&credential=" );
            if( credentialIndex <= 0 ) {
               credentialIndex = postData.indexOf( "&lid-credential=" );
            }
            if( credentialIndex >= 0 ) {
                postData = postData.substring( 0, credentialIndex );
            }
        }

        int    crIndex             = credential.indexOf( '\n' );
        String credentialFirstLine = crIndex > 0 ? credential.substring( 0, crIndex ) : credential;

        StringBuffer signedText = new StringBuffer( 256 );
        signedText.append( "-----BEGIN PGP SIGNED MESSAGE-----\n" );
        signedText.append( "Hash: " ).append( credentialFirstLine ).append( "\n" );
        signedText.append( "\n" );
        signedText.append( url );
        if( postData != null && postData.length() > 0 ) {
            if( url.endsWith( "?" )) {
                signedText.append( '&' );
            } else {
                signedText.append( '?' );
            }
            signedText.append( postData );
        }
        signedText.append( "\n" );
        signedText.append( "-----BEGIN PGP SIGNATURE-----\n" );
        if( crIndex > 0 ) {
            signedText.append( credential.substring( crIndex+1 ));
        }

        signedText.append( "-----END PGP SIGNATURE-----\n" );

        return signedText.toString();
    }

    /**
     * Validate a piece of signed text against a LID.
     *
     * @param lid the LID URL
     * @param signedText the signed text
     * @param publicKey the public key to use
     * @return true if the validation was successful
     * @throws IOException an I/O error occurred
     */
    public abstract boolean validateSignedText(
            String lid,
            String signedText,
            String publicKey )
        throws
            IOException;
    
    /**
     * Sign a URL.
     *
     * @param lid the LID that will sign the URL
     * @param url the URL to be signed
     * @param privateKey the private key to use
     * @return the signed URL
     * @throws IOException an I/O error occurred
     */
    public String signUrl(
            String lid,
            String url,
            String privateKey )
        throws
            IOException
    {
        return signUrl( lid, url, privateKey, null );
    }

    /**
     * Sign a URL.
     *
     * @param lid the LID that will sign the URL
     * @param url the URL to be signed
     * @param privateKey the private key to use
     * @param lidVersion the LID protocol version to use
     * @return the signed URL
     * @throws IOException an I/O error occurred
     */
    public abstract String signUrl(
            String lid,
            String url,
            String privateKey,
            String lidVersion )
        throws
            IOException;

    /**
     * The LidNonceManager to use.
     */
    protected LidNonceManager theNonceManager;
}
