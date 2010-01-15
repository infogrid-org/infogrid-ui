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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.openid;

import java.math.BigInteger;
import org.infogrid.crypto.diffiehellman.DiffieHellmanEndpoint;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidAbortProcessingPipelineWithContentException;
import org.infogrid.lid.LidProcessingPipelineStage;
import org.infogrid.util.Base64;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * Knows how to process incoming OpenID association requests. This does not implement
 * <code>DeclaresYadisFragment</code>; the SSO processor does.
 */
public class OpenIdIdpSideAssociationPipelineStage
        extends
            AbstractObjectInContext
        implements
            LidProcessingPipelineStage,
            OpenIdConstants
{
    /**
     * Factory method.
     *
     * @param associationManager the association manager to use
     * @param c the context in which this <code>ObjectInContext</code> runs.
     * @return the created OpenIdIdpSideAssociationPipelineStage
     */
    public static OpenIdIdpSideAssociationPipelineStage create(
            OpenIdIdpSideAssociationManager associationManager,
            Context                         c )
    {
        OpenIdIdpSideAssociationPipelineStage ret = new OpenIdIdpSideAssociationPipelineStage( associationManager, c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param associationManager the association manager to use
     * @param c the context in which this <code>ObjectInContext</code> runs.
     */
    protected OpenIdIdpSideAssociationPipelineStage(
            OpenIdIdpSideAssociationManager associationManager,
            Context                         c )
    {
        super( c );
        
        theAssociationManager = associationManager;
    }

    /**
     * Process any relevant requests.
     * 
     * @param lidRequest the incoming request
     * @throws OpenIdAssociationException thrown is an OpenID association problem occurred
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    public void processRequest(
            SaneRequest lidRequest )
        throws
            OpenIdAssociationException,
            LidAbortProcessingPipelineException
    {
        if( !"POST".equals( lidRequest.getMethod())) {
            return ;
        }
        String openIdMode = lidRequest.getPostedArgument( "openid.mode" );
        if( !"associate".equals( openIdMode )) {
            return; // does not apply to us
        }
        
        String assocType                 = lidRequest.getPostedArgument( "openid.assoc_type" );
        String sessionType               = lidRequest.getPostedArgument( "openid.session_type" );
        String dh_modulus_string         = lidRequest.getPostedArgument( "openid.dh_modulus" );
        String dh_gen_string             = lidRequest.getPostedArgument( "openid.dh_gen" );
        String dh_consumer_public_string = lidRequest.getPostedArgument( "openid.dh_consumer_public" );
        
        BigInteger dh_modulus         = null;
        BigInteger dh_gen             = null;
        BigInteger dh_consumer_public = null;
        
        if( dh_modulus_string != null ) {            
            dh_modulus = new BigInteger( Base64.base64decode( dh_modulus_string ));
        }
        if( dh_gen_string != null ) {            
            dh_gen = new BigInteger( Base64.base64decode( dh_gen_string ));
        }
        if(    DH_SHA1.equals( sessionType )
            && ( dh_consumer_public_string == null || dh_consumer_public_string.length() == 0 ))
        {
             throw new OpenIdAssociationException.InvalidPublicKey();
        }
        if( dh_consumer_public_string != null && dh_consumer_public_string.length() > 0 ) {
            dh_consumer_public = new BigInteger( Base64.base64decode( dh_consumer_public_string ));
        }
        
        // insert defaults
        assocType   = useDefaultIfNeeded( assocType,   HMAC_SHA1 );
        sessionType = useDefaultIfNeeded( sessionType, null ); // null means cleartext
        dh_modulus  = useDefaultIfNeeded( dh_modulus,  DEFAULT_P );
        dh_gen      = useDefaultIfNeeded( dh_gen,      DEFAULT_G );
        
        // sanity check
        
        if( !HMAC_SHA1.equals( assocType )) {
            throw new OpenIdAssociationException.UnknownAssociationType( assocType );
        }
        if( sessionType != null && !DH_SHA1.equals( sessionType )) {
            throw new OpenIdAssociationException.UnknownSessionType( sessionType );
        }
        if( DH_SHA1.equals( sessionType ) && dh_consumer_public == null ) {
            throw new OpenIdAssociationException.InvalidPublicKey();
        }
        
        OpenIdIdpSideAssociation assoc = theAssociationManager.create( sessionType );

        byte [] mac_key     = null;
        byte [] enc_mac_key = null;

        String dh_server_public_string = null;
        
        if( sessionType == null ) {
            // cleartext
            mac_key = assoc.getSharedSecret();
            
        } else {
            // Diffie-Hellman
            DiffieHellmanEndpoint dh             = DiffieHellmanEndpoint.create( dh_modulus, dh_gen );
            BigInteger            sharedDhSecret = dh.computeSharedSecret( dh_consumer_public );
            
            byte [] sharedSecret = assoc.getSharedSecret();
            enc_mac_key = CryptUtils.do160BitXor(
                    CryptUtils.calculateSha1( sharedDhSecret ),
                    sharedSecret );

            dh_server_public_string = Base64.base64encodeNoCr( dh.getPublicKey().toByteArray());
        }
        
        StringBuilder buf = new StringBuilder();
        long          now = System.currentTimeMillis();
        
        if( assocType != null ) {
            buf.append( "assoc_type:" );
            buf.append( assocType );
            buf.append( "\n" );
        }
        if( assoc.getAssociationHandle() != null ) {
            buf.append( "assoc_handle:" );
            buf.append( assoc.getAssociationHandle() );
            buf.append( "\n" );
        }
        if( assoc.getTimeExpires() > 0 ) {
            long delta = assoc.getTimeExpires() - now;
            buf.append( "expires_in:" );
            buf.append( delta / 1000L ); // need seconds
            buf.append( "\n" );
        }
        if( sessionType != null ) {
            buf.append( "session_type:" );
            buf.append( sessionType );
            buf.append( "\n" );
        }
        if( dh_server_public_string != null ) {
            buf.append( "dh_server_public:" );
            buf.append( dh_server_public_string );
            buf.append( "\n" );
        }
        if( enc_mac_key != null ) {
            buf.append( "enc_mac_key:" );
            buf.append( Base64.base64encodeNoCr( enc_mac_key ));
            buf.append( "\n" );
        }
        if( mac_key != null ) {
            buf.append( "mac_key:" );
            buf.append( Base64.base64encodeNoCr( mac_key ));
            buf.append( "\n" );
        }

        throw new LidAbortProcessingPipelineWithContentException( buf.toString(), "text/plain", this );
    }
    
    /**
     * Insert default if no value is given.
     * 
     * @param value the given value, if any
     * @param defaultValue the default value
     * @return the return value
     * @param <T> allows us to use the same method for different types
     */
    protected static <T> T useDefaultIfNeeded(
            T value,
            T defaultValue )
    {
        if( value == null ) {
            return defaultValue;

        } else if( value instanceof String && ((String)value).length() == 0 ) {
            return defaultValue;
        } else {
            return value;
        }
    }
    
    /**
     * Manages associations on the IdP end.
     */
    protected OpenIdIdpSideAssociationManager theAssociationManager;
}
