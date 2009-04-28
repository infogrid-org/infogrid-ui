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

package org.infogrid.crypto.hashedpassword;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.infogrid.util.Base64;
import org.infogrid.util.logging.Log;

/**
 * Utilities to store and validate passwords securely through hashing.
 */
public abstract class HashedPasswordUtils
{
    private static final Log log = Log.getLogInstance( HashedPasswordUtils.class ); // our own, private logger

    /**
     * Private constructor to keep this class abstract.
     */
    private HashedPasswordUtils()
    {
        // nothing
    }

    /**
     * Construct the hash of a password.
     *
     * @param password the password
     * @return the hash of the password
     */
    public static byte [] hash(
            String password )
    {
        try {
            MessageDigest md = MessageDigest.getInstance( DIGEST_ALGORITHM  );
            md.update( password.getBytes( "UTF-8" ));

            byte ret [] = md.digest();
            return ret;

        } catch ( NoSuchAlgorithmException ex ) {
            log.error( ex );
        } catch ( UnsupportedEncodingException ex ) {
            log.error( ex );
        }
        return null;
    }

    /**
     * Validate a password.
     *
     * @param password the password in clear text
     * @param hashed the password in hashed form
     * @return true if the password is valid
     */
    public static boolean isValid(
            String  password,
            byte [] hashed )
    {
        byte [] found = hash( password );

        if( found == null || hashed == null ) {
            return false;
        }
        if( found.length != hashed.length ) {
            return false;
        }
        for( int i=0 ; i<found.length ; ++i ) {
            if( found[i] != hashed[i] ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Convert a byte array into something that can be stored in most persistent stores.
     *
     * @param raw the raw byte array
     * @return a String
     */
    public static String raw2string(
            byte [] raw )
    {
        String ret = Base64.base64encodeNoCr( raw );
        return ret;
    }

    /**
     * Convert a String converted with raw2string back into a byte array.
     *
     * @param s the String
     * @return the byte array
     */
    public static byte [] string2raw(
            String s )
    {
        byte [] ret = Base64.base64decode( s );
        return ret;
    }

    /**
     * The Digest algorithm to use.
     */
    public static final String DIGEST_ALGORITHM = "SHA-512";
}
