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
 * Negotiates a new RelyingPartySideAssociation by acting as a Factory for RelyingPartySideAssociations.
 */
public class RelyingPartySideAssociationNegotiator
        extends
            AbstractFactory<String,RelyingPartySideAssociation,AssociationNegotiationParameters>
{
    private static final Log log = Log.getLogInstance( RelyingPartySideAssociationNegotiator.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @return the created RelyingPartySideAssociationNegotiator
     */
    public static RelyingPartySideAssociationNegotiator create()
    {
        return new RelyingPartySideAssociationNegotiator();
    }

    /**
     * Factory method.
     *
     * @param serverUrl the key information required for object creation, if any
     * @param parameters any information required for object creation, if any
     * @return the created object
     */
    public RelyingPartySideAssociation obtainFor(
            String                           serverUrl,
            AssociationNegotiationParameters parameters )
        throws
            FactoryException
    {
        if( parameters == null ) {
            parameters = AssociationNegotiationParameters.createWithDefaults();
        }
        StringBuffer sentContentBuf = new StringBuffer( 512 );

        sentContentBuf.append(  "openid.mode="         ).append( "associate" );
        sentContentBuf.append( "&openid.assoc_type="   ).append( parameters.getWantedAssociationType() );

        DiffieHellmanEndpoint dh = null;

        if( AssociationNegotiationParameters.DH_SHA1.equals( parameters.getWantedSessionType() )) {
            sentContentBuf.append( "&openid.session_type=" ).append( AssociationNegotiationParameters.DH_SHA1 );

            dh = parameters.getWantedDiffieHellmanEndpoint();

            if( !AssociationNegotiationParameters.DEFAULT_P.equals( dh.getP() )) {
                sentContentBuf.append( "&openid.modulus=" ).append( HTTP.encodeToValidUrlArgument( Base64.base64encodeNoCr( dh.getP().toByteArray() )));
            }
            if( !AssociationNegotiationParameters.DEFAULT_G.equals( dh.getG() )) {
                sentContentBuf.append( "&openid.dh_gen=" ).append( HTTP.encodeToValidUrlArgument( Base64.base64encodeNoCr( dh.getG().toByteArray() )));
            }
            sentContentBuf.append( "&openid.dh_consumer_public=" ).append( HTTP.encodeToValidUrlArgument( Base64.base64encodeNoCr( dh.getPublicKey().toByteArray() )));
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
            throw new FactoryException( thrownEx );
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
                throw new FactoryException( ex );
            }
        }
        if( theAssociationType != null && !AssociationNegotiationParameters.HMAC_SHA1.equals( theAssociationType )) {
            throw new FactoryException( new AssociationException.UnknownAssociationType( theAssociationType ));
        }
        if( theSessionType != null && !AssociationNegotiationParameters.DH_SHA1.equals( theSessionType )) {
            throw new FactoryException( new AssociationException.UnknownSessionType( theSessionType ));
        }
        if( expires_in == Long.MIN_VALUE ) {
            if( replace_after != null ) {
                expires_in = (replace_after.getTime() - issued.getTime())/1000L;
            } else {
                expires_in = (expiry.getTime() - issued.getTime())/1000L;
            }
        }
        if( expires_in < 0 ) {
            throw new FactoryException( new AssociationException.InvalidExpiration());
        }

        if( issued != null ) {
            issuedTime = issued.getTime();
        }
        if( expires_in >=0 ) {
            long now = System.currentTimeMillis();
            issuedTime = now;
            expiryTime = now + 1000L*expires_in;
        }

        if( AssociationNegotiationParameters.DH_SHA1.equals( theSessionType )) {
            BigInteger dhSharedSecret = dh.computeSharedSecret( theServerPublicKey );

            byte [] dhSharedSecretSha1 = CryptUtils.calculateSha1( dhSharedSecret );

            theSharedSecret = CryptUtils.do160BitXor(
                    theEncryptedSharedSecret,
                    dhSharedSecretSha1 );
        }

        RelyingPartySideAssociation ret = RelyingPartySideAssociation.create( serverUrl, theAssociationHandle, theSharedSecret, issuedTime, expiryTime );
        ret.checkCompleteness();        
        return ret;
    }

    /**
     * The OpenID V1.0 timestamp format.
     */
    protected static final DateFormat theDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ss'Z'" );
}
