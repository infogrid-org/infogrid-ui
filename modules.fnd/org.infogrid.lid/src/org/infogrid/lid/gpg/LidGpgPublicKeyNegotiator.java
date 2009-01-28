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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.Identifier;
import org.infogrid.util.http.HTTP;

/**
 * Obtains a public key for a given LID identifier.
 */
public class LidGpgPublicKeyNegotiator
    extends
        AbstractFactory<Identifier,String,Void>
{
    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public String obtainFor(
            Identifier key,
            Void       argument )
        throws
            FactoryException
    {
        try {
            String url = HTTP.appendArgumentPairToUrl( key.toExternalForm(), "lid-meta=gpg%20--export%20--armor" );

            HTTP.Response response = HTTP.http_get( url );
            if( !response.isSuccess() ) {
                throw new FactoryException( this, "Could not obtain public key at " + url + ", http status: " + response.getResponseCode() );
            }
            String ret = response.getContentAsString();

            if( ret != null ) {
                Matcher m = publicKeyPattern.matcher( ret );
                if( !m.matches() ) {
                    throw new FactoryException( this, "Could not obtain public key at " + url + ", wrong format" );
                }
            }
            return ret;

        } catch( IOException ex ) {
            throw new FactoryException( this, ex );
        }
    }

    /**
     * The GPG public key pattern that we are looking for when asking for GPG public keys.
     */
    private static final Pattern publicKeyPattern = Pattern.compile(
            "-----BEGIN PGP PUBLIC KEY BLOCK[\\s\\S]*" );
}
