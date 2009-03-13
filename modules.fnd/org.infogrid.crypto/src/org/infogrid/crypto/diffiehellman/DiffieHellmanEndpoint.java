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

package org.infogrid.crypto.diffiehellman;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Represents a Diffie-Hellman endpoint.
 */
public class DiffieHellmanEndpoint
        implements
            CanBeDumped
{
    /**
     * Factory method. Automatically generate new keys.
     *
     * @param p the Diffie-Hellman p parameter
     * @param g the Diffie-Hellman g parameter
     * @return the created DiffieHellmanEndpoint
     */
    public static DiffieHellmanEndpoint create(
            int p,
            int g )
    {
        return new DiffieHellmanEndpoint(
                BigInteger.valueOf( p ),
                BigInteger.valueOf( g ) );
    }

    /**
     * Factory method. Automatically generate new keys.
     *
     * @param p the Diffie-Hellman p parameter
     * @param g the Diffie-Hellman g parameter
     * @return the created DiffieHellmanEndpoint
     */
    public static DiffieHellmanEndpoint create(
            long p,
            long g )
    {
        return new DiffieHellmanEndpoint(
                BigInteger.valueOf( p ),
                BigInteger.valueOf( g ) );
    }

    /**
     * Factory method. Automatically generate new keys.
     *
     * @param p the Diffie-Hellman p parameter
     * @param g the Diffie-Hellman g parameter
     * @return the created DiffieHellmanEndpoint
     */
    public static DiffieHellmanEndpoint create(
            BigInteger p,
            BigInteger g )
    {
        return new DiffieHellmanEndpoint(
                p,
                g );
    }

    /**
     * Factory method. Provide, do not generate keys.
     *
     * @param p the Diffie-Hellman p parameter
     * @param g the Diffie-Hellman g parameter
     * @param privateKey our own private key
     * @param publicKey our own public Key
     * @return the created DiffieHellmanEndpoint
     */
    public static DiffieHellmanEndpoint create(
            BigInteger p,
            BigInteger g,
            BigInteger privateKey,
            BigInteger publicKey )
    {
        return new DiffieHellmanEndpoint( p, g, privateKey, publicKey );
    }

    /**
     * Constructor.
     *
     * @param p the Diffie-Hellman p parameter
     * @param g the Diffie-Hellman g parameter
     */
    protected DiffieHellmanEndpoint(
            BigInteger p,
            BigInteger g )
    {
        this( p, g, null, null );
    }

    /**
     * Constructor.
     *
     * @param p the Diffie-Hellman p parameter
     * @param g the Diffie-Hellman g parameter
     * @param privateKey our own private key
     * @param publicKey our own public Key
     */
    protected DiffieHellmanEndpoint(
            BigInteger p,
            BigInteger g,
            BigInteger privateKey,
            BigInteger publicKey )
    {
        theP          = p;
        theG          = g;
        thePrivateKey = privateKey;
        thePublicKey  = publicKey;
    }

    /**
     * Obtain the Diffie-Hellman p parameter.
     *
     * @return the Diffie-Hellman p parameter
     */
    public BigInteger getP()
    {
        return theP;
    }

    /**
     * Obtain the Diffie-Hellman g parameter.
     *
     * @return the Diffie-Hellman g parameter
     */
    public BigInteger getG()
    {
        return theG;
    }

    /**
     * Obtain the public key.
     *
     * @return the public key associated with this end point
     */
    public BigInteger getPublicKey()
    {
        generateKeyPairIfNeeded();
        return thePublicKey;
    }

    /**
     * Obtain the private key.
     *
     * @return the private key associated with this end point
     */
    public BigInteger getPrivateKey()
    {
        generateKeyPairIfNeeded();
        return thePrivateKey;
    }

    /**
     * Compute the shared secret given our partner's public key.
     *
     * @param partnerPublicKey our partner's public key
     * @return the shared secret, given this end point and our partner's public key
     */
    public BigInteger computeSharedSecret(
            BigInteger partnerPublicKey )
    {
        generateKeyPairIfNeeded();
        BigInteger ret = partnerPublicKey.modPow( thePrivateKey, theP );
        return ret;
    }

    /**
     * Generate the key pair if needed.
     */
    public synchronized final void generateKeyPairIfNeeded()
    {
        if( thePrivateKey == null ) {
            do {
                thePrivateKey = new BigInteger( theP.bitCount(), theRandomGenerator );
            } while( thePrivateKey.equals( ZERO ));
            thePublicKey = null;
        }
        if( thePublicKey == null ) {
            thePublicKey  = theG.modPow( thePrivateKey, theP );
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                        "theP",
                        "theG",
                        "thePublicKey",
                        "thePrivateKey"
                },
                new Object[] {
                        theP,
                        theG,
                        thePublicKey,
                        thePrivateKey
                } );
    }

    /**
     * The p parameter.
     */
    protected BigInteger theP;

    /**
     * The g parameter.
     */
    protected BigInteger theG;

    /**
     * The public key, or null if it has not been generated yet.
     */
    protected BigInteger thePublicKey;

    /**
     * The private key, or null if it has not been generated yet.
     */
    protected BigInteger thePrivateKey;

    /**
     * Zero as a BigInteger.
     */
    private static final BigInteger ZERO = BigInteger.valueOf( 0 );

    /**
     * Our Random number generator.
     */
    private static final Random theRandomGenerator = new SecureRandom();
}

