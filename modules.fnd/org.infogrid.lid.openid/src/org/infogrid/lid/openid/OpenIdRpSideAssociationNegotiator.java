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

package org.infogrid.lid.openid;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import org.infogrid.crypto.diffiehellman.DiffieHellmanEndpoint;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.Base64;
import org.infogrid.util.FactoryException;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

/**
 * Negotiates a new OpenIdRpSideAssociation by acting as a Factory for OpenIdRpSideAssociations.
 */
public class OpenIdRpSideAssociationNegotiator
        extends
            AbstractFactory<String,OpenIdRpSideAssociation,OpenIdRpSideAssociationNegotiationParameters>
        implements
            OpenIdConstants
{
    private static final Log log = Log.getLogInstance( OpenIdRpSideAssociationNegotiator.class    ); // our own, private logger

    /**
     * Factory method.
     * 
     * @return the created OpenIdRpSideAssociationNegotiator
     */
    public static OpenIdRpSideAssociationNegotiator create()
    {
        return new OpenIdRpSideAssociationNegotiator(
                DEFAULT_P,
                DEFAULT_G );
    }

    /**
     * Factory method.
     * 
     * @param dhP the Diffie-Hellman P parameter
     * @param dhG the Diffie-Hellman G parameter
     * @return the created OpenIdRpSideAssociationNegotiator
     */
    public static OpenIdRpSideAssociationNegotiator create(
            BigInteger dhP,
            BigInteger dhG )
    {
        return new OpenIdRpSideAssociationNegotiator(
                dhP,
                dhG );
    }

    /**
     * Constructor.
     * 
     * @param dhP the Diffie-Hellman P parameter
     * @param dhG the Diffie-Hellman G parameter
     */
    protected OpenIdRpSideAssociationNegotiator(
            BigInteger dhP,
            BigInteger dhG )
    {
        theDhP = dhP;
        theDhG = dhG;
    }

    /**
     * Factory method.
     *
     * @param serverUrl the key information required for object creation, if any
     * @param parameters any information required for object creation, if any
     * @return the created object
     */
    public OpenIdRpSideAssociation obtainFor(
            String                                       serverUrl,
            OpenIdRpSideAssociationNegotiationParameters parameters )
        throws
            FactoryException
    {
        if( parameters == null ) {
            parameters = OpenIdRpSideAssociationNegotiationParameters.createWithDefaults();
        }
        StringBuffer sentContentBuf = new StringBuffer( 512 );

        DiffieHellmanEndpoint localDh = DiffieHellmanEndpoint.create( theDhP, theDhG );
        
        sentContentBuf.append(  "openid.mode="         ).append( "associate" );
        sentContentBuf.append( "&openid.assoc_type="   ).append( parameters.getWantedAssociationType() );

        if( DH_SHA1.equals( parameters.getWantedSessionType() )) {
            sentContentBuf.append( "&openid.session_type=" ).append( DH_SHA1 );

            if( !DEFAULT_P.equals( localDh.getP() )) {
                sentContentBuf.append( "&openid.modulus=" );
                sentContentBuf.append( HTTP.encodeToValidUrlArgument( Base64.base64encodeNoCr( localDh.getP().toByteArray() )));
            }
            if( !DEFAULT_G.equals( localDh.getG() )) {
                sentContentBuf.append( "&openid.dh_gen=" );
                sentContentBuf.append( HTTP.encodeToValidUrlArgument( Base64.base64encodeNoCr( localDh.getG().toByteArray() )));
            }
            sentContentBuf.append( "&openid.dh_consumer_public=" );
            sentContentBuf.append( HTTP.encodeToValidUrlArgument( Base64.base64encodeNoCr( localDh.getPublicKey().toByteArray() )));
        }

        HTTP.Response response    = null;
        String        sentContent = sentContentBuf.toString(); // this makes debugging easier
        IOException   thrownEx    = null;
        try {
            byte [] payload = sentContent.getBytes( "US-ASCII" );
            response = HTTP.http_post( serverUrl, "application/x-www-form-urlencoded", payload, false );

        } catch( IOException ex ) {
            thrownEx = ex;
        }
        if( thrownEx != null || response == null || !response.isSuccess() ) {
            throw new FactoryException( this, thrownEx );
        }

        String     theAssociationHandle     = null;
        String     theAssociationType       = null;
        String     theSessionType           = null;
        BigInteger theServerPublicKey       = null;
        byte []    theSharedSecret          = null;
        byte []    theEncryptedSharedSecret = null;
        long       issuedTime               = -1L;
        long       expiryTime               = -1L;

        long expires_in = Long.MIN_VALUE; // The number of seconds for which this is valid.
                                          // In OpenID V1.1, incoming times are relative, we convert to absolute below.

        // OpenID V1.0 parameters
        Date issued        = null;
        Date replace_after = null;
        Date expiry        = null;

        String receivedContent = response.getContentAsString();
        StringTokenizer token1 = new StringTokenizer( receivedContent, "\n" );
        while( token1.hasMoreElements() ) {
            try {
                String pair = token1.nextToken();
                int    colon = pair.indexOf( ":" );
                if( colon >=0  ) {
                    String key   = pair.substring( 0, colon );
                    String value = pair.substring( colon+1 );

                    if( "assoc_type".equals( key )) {
                        theAssociationType = value;
                    } else if( "assoc_handle".equals( key )) {
                        theAssociationHandle = value;
                    } else if( "issued".equals( key )) {
                        issued = theDateFormat.parse( value );
                    } else if( "replace_after".equals( key )) {
                        replace_after = theDateFormat.parse( value );
                    } else if( "expiry".equals( key )) {
                        expiry = theDateFormat.parse( value );
                    } else if( "expires_in".equals( key )) {
                        expires_in = Long.parseLong( value, 10 );
                    } else if( "session_type".equals( key )) {
                        theSessionType = value;
                    } else if( "dh_server_public".equals( key )) {
                        theServerPublicKey = new BigInteger( Base64.base64decode( value ));
                    } else if( "enc_mac_key".equals( key )) {
                        theEncryptedSharedSecret = Base64.base64decode( value );
                    } else if( "mac_key".equals( key )) {
                        theSharedSecret = Base64.base64decode( value );
                    } else {
                        log.warn( "When talking to " + serverUrl + ", received unknown key-value pair " + key + " -> " + value );
                    }
                }
            } catch( ParseException ex ) {
                throw new FactoryException( this, ex );
            }
        }
        if( theAssociationType != null && !OpenIdRpSideAssociationNegotiationParameters.HMAC_SHA1.equals( theAssociationType )) {
            throw new FactoryException(
                    this,
                    new OpenIdAssociationException.UnknownAssociationType( theAssociationType ));
        }
        if( theSessionType != null && !OpenIdRpSideAssociationNegotiationParameters.DH_SHA1.equals( theSessionType )) {
            throw new FactoryException(
                    this,
                    new OpenIdAssociationException.UnknownSessionType( theSessionType ));
        }
        if( expires_in == Long.MIN_VALUE ) {
            if( replace_after != null ) {
                expires_in = (replace_after.getTime() - issued.getTime())/1000L;
            } else {
                expires_in = (expiry.getTime() - issued.getTime())/1000L;
            }
        }
        if( expires_in < 0 ) {
            throw new FactoryException(
                    this,
                    new OpenIdAssociationException.InvalidExpiration());
        }

        if( issued != null ) {
            issuedTime = issued.getTime();
        }
        if( expires_in >=0 ) {
            long now = System.currentTimeMillis();
            issuedTime = now;
            expiryTime = now + 1000L*expires_in;
        }

        if( OpenIdRpSideAssociationNegotiationParameters.DH_SHA1.equals( theSessionType )) {
            BigInteger dhSharedSecret = localDh.computeSharedSecret( theServerPublicKey );

            byte [] dhSharedSecretSha1 = CryptUtils.calculateSha1( dhSharedSecret );

            theSharedSecret = CryptUtils.do160BitXor(
                    theEncryptedSharedSecret,
                    dhSharedSecretSha1 );
        }

        OpenIdRpSideAssociation ret = OpenIdRpSideAssociation.create( serverUrl, theAssociationHandle, theSharedSecret, issuedTime, expiryTime );
        ret.checkCompleteness();        
        return ret;
    }

    /**
     * The OpenID V1.0 timestamp format.
     */
    protected static final DateFormat theDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ss'Z'" );

    /**
     * The Diffie-Hellman P parameter to use.
     */
    protected BigInteger theDhP;
    
    /**
     * The Diffie-Hellman G parameter to use.
     */
    protected BigInteger theDhG;    
}
