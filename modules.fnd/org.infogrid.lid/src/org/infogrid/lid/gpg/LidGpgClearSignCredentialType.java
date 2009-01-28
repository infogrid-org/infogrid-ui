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
import org.infogrid.lid.LidInvalidNonceException;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.lid.credential.AbstractLidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Represents the LID GPG clearsign credential type.
 */
public class LidGpgClearSignCredentialType
        extends
            AbstractLidCredentialType
{
    private static final Log log = Log.getLogInstance( LidGpgClearSignCredentialType.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param gpg the LidGpg to use
     * @param gpgPublicKeyManager the LidGpgPublicKeyManager to use
     * @param nonceManager the LidNonceManager to use
     * @return the created MeshLidGpgClearSignCredentialType
     */
    public static LidGpgClearSignCredentialType create(
            LidGpg                 gpg,
            LidGpgPublicKeyManager gpgPublicKeyManager,
            LidNonceManager        nonceManager )
    {
        LidGpgClearSignCredentialType ret
                = new LidGpgClearSignCredentialType( gpg, gpgPublicKeyManager, nonceManager );

        return ret;
    }

    /**
     * Constructor, for subclasses only.
     *
     * @param gpg the LidGpg to use
     * @param gpgPublicKeyManager the LidGpgPublicKeyManager to use
     * @param nonceManager the LidNonceManager to use
     */
    protected LidGpgClearSignCredentialType(
            LidGpg                 gpg,
            LidGpgPublicKeyManager gpgPublicKeyManager,
            LidNonceManager        nonceManager )
    {
        theGpg                 = gpg;
        theGpgPublicKeyManager = gpgPublicKeyManager;
        theNonceManager        = nonceManager;
    }

    /**
     * Determine whether this LidCredentialType is contained in this request.
     *
     * @param request the request
     * @return true if this LidCredentialType is contained in this request
     */
    public boolean isContainedIn(
            SaneRequest request )
    {
        if( request.matchArgument( LID_CREDTYPE_PARAMETER_NAME, LID_GPG_CLEARSIGN_PARAMETER_VALUE )) {
            return true;
        }

        return false;
    }

    /**
     * Determine whether the request contains a valid LidCredentialType of this type
     * for the given subject.
     *
     * @param request the request
     * @param subject the subject
     * @throws LidInvalidCredentialException thrown if the contained LidCdedentialType is not valid for this subject
     */
    public void checkCredential(
            SaneRequest   request,
            HasIdentifier subject )
        throws
            LidInvalidCredentialException
    {
        String personaIdentifier = subject.getIdentifier().toExternalForm();

        String thePublicKey = null;
        try {
            thePublicKey = theGpgPublicKeyManager.obtainFor( subject.getIdentifier() );

        } catch( FactoryException ex ) {
            log.warn( ex );
        }
        if( thePublicKey == null ) {
            // can't do anything here
            throw new LidGpgNoPublicKeyException( subject.getIdentifier(), this );
        }

        try {
            theNonceManager.validateNonce( request );

        } catch( LidInvalidNonceException ex ) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this, ex );
        }

        String credential = request.getArgument( LID_CREDENTIAL_PARAMETER_NAME );
        if( credential == null || credential.length() == 0 ) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this );
        }

        try {
            theGpg.importPublicKey( thePublicKey );

            String  fullUri    = request.getAbsoluteFullUri();
            String  postString = request.getPostData();

            String signedText = theGpg.reconstructSignedMessage( fullUri, postString, credential );

            boolean ret = theGpg.validateSignedText( personaIdentifier, signedText );
            if( !ret ) {
                throw new LidInvalidCredentialException( subject.getIdentifier(), this );
            }
        } catch( IOException ex ) {
            log.error( ex );
            throw new LidInvalidCredentialException( subject.getIdentifier(), this, ex );
        }
    }

    /**
     * The LidGpg to use.
     */
    protected LidGpg theGpg;

    /**
     * The LidGpgPublicKeyManager to use,.
     */
    protected LidGpgPublicKeyManager theGpgPublicKeyManager;

    /**
     * The NonceManager to use.
     */
    protected LidNonceManager theNonceManager;

    /**
     * Name of the LidCredentialType.
     */
    public static final String LID_GPG_CLEARSIGN_PARAMETER_VALUE = "gpg --clearsign";
}