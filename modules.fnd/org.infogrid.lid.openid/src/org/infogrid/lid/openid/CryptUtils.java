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

package org.infogrid.lid.openid;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import org.infogrid.util.logging.Log;
import org.infogrid.util.ArrayHelper;

/**
 * Cryptographic utilities needed for OpenID.
 */
public abstract class CryptUtils
{
    private static final Log log = Log.getLogInstance( CryptUtils.class ); // our own, private logger

    /**
     * Calculate the SHA-1 digest.
     *
     * @param data the data whose SHA-1 we calculate
     * @return the SHA-1 of the data
     */
    public static byte [] calculateSha1(
            BigInteger data )
    {
        try {
            byte [] realData = data.toByteArray();
            // insert leading zero if MSB set
            if( realData[0] > 127 ) {
                realData = ArrayHelper.append( new byte[] { (byte) 0 }, realData );
            }
            
            MessageDigest md = MessageDigest.getInstance( "SHA1" );
            md.update( realData );

            byte [] ret = md.digest();
            return ret;

        } catch( NoSuchAlgorithmException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Calculate the SHA-256 digest.
     *
     * @param data the data whose SHA-256 we calculate
     * @return the SHA-256 of the data
     */
    public static byte [] calculateSha256(
            BigInteger data )
    {
        try {
            byte [] realData = data.toByteArray();
            // insert leading zero if MSB set
            if( realData[0] > 127 ) {
                realData = ArrayHelper.append( new byte[] { (byte) 0 }, realData );
            }

            MessageDigest md = MessageDigest.getInstance( "SHA256" );
            md.update( realData );

            byte [] ret = md.digest();
            return ret;

        } catch( NoSuchAlgorithmException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Calculate the HMAC-SHA-1 message authentication code.
     *
     * @param key the secret key
     * @param data the data
     * @return the HMAC-SHA-1 message authentication code of the data, given key.
     */
    public static byte [] calculateHmacSha1(
            final byte [] key,
            final byte [] data )
    {
        try {
            Mac mac = Mac.getInstance( HMACSHA1_ALGORITHM );
            mac.init( new SecretKeySpec( key, HMACSHA1_ALGORITHM ));

            byte [] result = mac.doFinal( data );
            return result;

        } catch( Exception ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Calculate the HMAC-SHA-256 message authentication code.
     *
     * @param key the secret key
     * @param data the data
     * @return the HMAC-SHA-256 message authentication code of the data, given key.
     */
    public static byte [] calculateHmacSha256(
            final byte [] key,
            final byte [] data )
    {
        try {
            Mac mac = Mac.getInstance( HMACSHA256_ALGORITHM );
            mac.init( new SecretKeySpec( key, HMACSHA256_ALGORITHM ));

            byte [] result = mac.doFinal( data );
            return result;

        } catch( Exception ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Create a random set of bits.
     * 
     * @param bits the number of bits for the random number
     * @return array of bytes with the specified number of random bits, leading zero of needed
     */
    public static byte [] randomWithBits(
            int bits )
    {
        byte [] ret = new byte[ ((bits+7)/8) ];
        theRandomGenerator.nextBytes( ret );
        int remainder = bits % 8;
        int mask = 0xff;
        if( remainder > 0 ) {
            mask = mask >> (8-remainder);
        }
        ret[0] &= mask;
        return ret;
    }

    /**
     * Calculate A xor B for 160 bit values.
     *
     * @param one first argument
     * @param two second argument
     * @return the XOR of the two arguments
     */
    public static byte [] do160BitXor(
            byte [] one,
            byte [] two )
    {
        if( one.length != 20 ) {
            throw new IllegalArgumentException( "do160BitXor: First parameter is not 20 bytes long, is " + one.length );
        }
        if( two.length != 20 ) {
            throw new IllegalArgumentException( "do160BitXor: Second parameter is not 20 bytes long, is " + two.length );
        }
        byte [] ret = new byte[20];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = (byte) ( one[i] ^ two[i] );
        }
        return ret;
    }

    /**
     * Calculate A xor B for 256 bit values
     *
     * @param one first argument
     * @param two second argument
     * @return the XOR of the two arguments
     */
    public static byte [] do256BitXor(
            byte [] one,
            byte [] two )
    {
        if( one.length != 32 ) {
            throw new IllegalArgumentException( "do256BitXor: First parameter is not 32 bytes long, is " + one.length );
        }
        if( two.length != 32 ) {
            throw new IllegalArgumentException( "do256BitXor: Second parameter is not 32 bytes long, is " + two.length );
        }
        byte [] ret = new byte[32];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = (byte) ( one[i] ^ two[i] );
        }
        return ret;
    }

    /**
     * Convert a byte array into a hex-coded String.
     *
     * @param data byte[] the input data
     * @return String the constructed String
     */
    public static String hex(
            byte [] data )
    {
        StringBuffer ret = new StringBuffer( data.length*2 );
        for( int i=0 ; i<data.length ; ++i ) {
            char chigh;
            char clow;
            int c = data[i] & 0x0f;
            if( c < 10 ) {
                clow = (char) ( '0' + c );
            } else {
                clow = (char) ( 'a' + (c-10) );
            }
            c = data[i] & 0xf0;
            c >>= 4;
            if( c < 10 ) {
                chigh = (char) ( '0' + c );
            } else {
                chigh = (char) ( 'a' + (c-10) );
            }
            ret.append( chigh ).append( clow );
        }
        return ret.toString();
    }

    /**
     * Convert a hex-coded String into a byte array.
     *
     * @param data the input hex data
     * @return the constructed byte array
     */
    public static byte [] dehex(
            String data )
    {
        if( data.length() % 2 == 1 ) {
            data = "0" + data; // add leading zero front if odd length
        }
        byte [] ret = new byte[ data.length()/2 ];
        for( int i=data.length()-2 ; i>=0 ; i-=2 ) {
            char c1 = data.charAt( i );
            char c2 = data.charAt( i+1 );

            if( c1 >= 'a' && c1 <= 'z') {
                ret[i/2] = (byte) ( ( c1 - 'a' + 10 ) << 4 );
            } else if( c1 >= '0' && c1 <= '9' ) {
                ret[i/2] = (byte) ( ( c1 - '0' ) << 4 );
            } else {
                throw new IllegalArgumentException( "Character " + c1 + " is not a hexadecimal character" );
            }

            if( c2 >= 'a' && c2 <= 'z') {
                ret[i/2] |= (byte) ( c2 - 'a' + 10 );
            } else if( c2 >= '0' && c2 <= '9' ) {
                ret[i/2] |= (byte) ( c2 - '0' );
            } else {
                throw new IllegalArgumentException( "Character " + c1 + " is not a hexadecimal character" );
            }
        }
        return ret;
    }

    /**
     * Name of the algorithm.
     */
    static final String HMACSHA1_ALGORITHM = "HmacSha1";

    /**
     * Name of the algorithm.
     */
    static final String HMACSHA256_ALGORITHM = "HmacSha256";

    /**
     * Our Random number generator.
     */
    private static final Random theRandomGenerator = new SecureRandom();
}
