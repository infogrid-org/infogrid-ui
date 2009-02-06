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

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.VerbatimStructuredResponseTemplate;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidClientAuthenticationStatus;
import org.infogrid.lid.yadis.AbstractYadisService;
import org.infogrid.util.Base64;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Knows how to process incoming OpenID SSO requests. THis does NOT handle dumb mode.
 */
public class OpenIdSsoPipelineStage
        extends
            AbstractYadisService
{
    private static final Log log = Log.getLogInstance( OpenIdSsoPipelineStage.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param smartAssocMgr manages smart OpenID associations
     * @param dumbAssocMgr manages dumb OpenID associations
     * @param c the context in which this <code>ObjectInContext</code> runs.
     * @return the created OpenIdSsoPipelineStage
     */
    public static OpenIdSsoPipelineStage create(
            OpenIdIdpSideAssociationManager smartAssocMgr,
            OpenIdIdpSideAssociationManager dumbAssocMgr,
            Context                         c )
    {
        OpenIdSsoPipelineStage ret = new OpenIdSsoPipelineStage( smartAssocMgr, dumbAssocMgr, c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param smartAssocMgr manages smart OpenID associations
     * @param dumbAssocMgr manages dumb OpenID associations
     * @param c the context in which this <code>ObjectInContext</code> runs.
     */
    protected OpenIdSsoPipelineStage(
            OpenIdIdpSideAssociationManager smartAssocMgr,
            OpenIdIdpSideAssociationManager dumbAssocMgr,
            Context                         c )
    {
        super( YADIS_FRAGMENT, c );

        theSmartAssociationManager = smartAssocMgr;
        theDumbAssociationManager  = dumbAssocMgr;
    }

    /**
     * Process any relevant requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param clientAuthStatus knows the authentication status of the client
     * @param requestedResource the requested resource, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    public void processRequest(
            SaneRequest                   lidRequest,
            StructuredResponse            lidResponse,
            LidClientAuthenticationStatus clientAuthStatus,
            HasIdentifier                 requestedResource )
        throws
            LidAbortProcessingPipelineException
    {
        String mode = lidRequest.getArgument( "openid.mode" );
        if( mode == null ) {
            return;
        }

        if( "checkid_immediate".equals( mode )) {
            processCheckId( lidRequest, lidResponse, clientAuthStatus, requestedResource, true );

        } else if( "checkid_setup".equals( mode )) {
            processCheckId( lidRequest, lidResponse, clientAuthStatus, requestedResource, false );

        } else if( "check_authentication".equals( mode )) {
            processCheckAuthentication( lidRequest, lidResponse, clientAuthStatus, requestedResource );

        } else {
            throw new OpenIdSsoException( this, "Unsupported value given for openid.mode: " + mode );
        }
    }
    
    /**
     * Process incoming checkid_immediate and checkid_setup requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param clientAuthStatus knows the authentication status of the client
     * @param requestedResource the requested resource, if any
     * @param checkIdImmediate if true, performed checkid_immediate; if false, perform checkid_setup
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    protected void processCheckId(
            SaneRequest                   lidRequest,
            StructuredResponse            lidResponse,
            LidClientAuthenticationStatus clientAuthStatus,
            HasIdentifier                 requestedResource,
            boolean                       checkIdImmediate )
        throws
            LidAbortProcessingPipelineException
    {
        if( requestedResource != null && lidRequest.getAbsoluteBaseUri().equals( requestedResource.getIdentifier().toExternalForm() )) {
            TextStructuredResponseSection htmlHeadSection = lidResponse.obtainTextSection( StructuredResponse.HTML_HEAD_SECTION );
            htmlHeadSection.appendContent(
                    "  <link rel=\"openid.server\" href=\"" +  lidRequest.getAbsoluteBaseUri() + "\" />\n" );
        }
        
        if( !"GET".equals( lidRequest.getMethod() )) {
            return;
        }
        if( theSmartAssociationManager == null ) {
            return;
        }
        
        String identifier   = lidRequest.getArgument( "openid.identity" );
        String assoc_handle = lidRequest.getArgument( "openid.assoc_handle" );
        String return_to    = lidRequest.getArgument( "openid.return_to" );
        String trust_root   = lidRequest.getArgument( "openid.trust_root" );

        if( identifier == null || identifier.length() == 0 ) {
            throw new OpenIdSsoException( this, "openid.identifier must not be empty" );
        }
        if( assoc_handle == null || assoc_handle.length() == 0 ) {
            throw new OpenIdSsoException( this, "openid.assoc_handle must not be empty" );
        }
        if( return_to == null || return_to.length() == 0 ) {
            throw new OpenIdSsoException( this, "openid.return_to must not be empty" );
            // The spec says it's optional, but I can't see how that can work
        }
        if( trust_root == null || trust_root.length() == 0 ) {
            trust_root = return_to;
        }
        
        OpenIdIdpSideAssociation assoc = theSmartAssociationManager.get( assoc_handle );
        if( assoc == null ) {
            throw new OpenIdSsoException( this, "cannot find association with handle " + assoc_handle );
        }
        // checking whether it is valid happens later
        
        boolean shouldSso = false; // don't do it unless we are sure we want to
        
        if( clientAuthStatus.isValidSessionOnly() ) {
            // everything else we consider invalid, even if credentials are provided with this request: they
            // have no business as part of the request.
            if( clientAuthStatus.getClientIdentifier().toExternalForm().equals( identifier )) {
                shouldSso = true;
            }
        }

        // assemble response
        
        StringBuilder redirect       = new StringBuilder();
        StringBuilder token_contents = new StringBuilder();
        
        if( shouldSso ) {
            construct( redirect, token_contents, "mode", "id_res" );
        } else if( checkIdImmediate ) {
            construct( redirect, token_contents, "mode", "cancel" );
        }
        if( shouldSso ) {
            construct( redirect, token_contents, "identity",  identifier );
            construct( redirect, token_contents, "return_to", return_to );

            if( assoc != null ) {
                if( assoc.isCurrentlyValid() ) {
                    construct( redirect, token_contents, "assoc_handle", assoc_handle );
                } else {
                    theSmartAssociationManager.remove( assoc.getAssociationHandle() );
                    // the spec seems to allow not to automatically create a new association
                    construct( redirect, token_contents, "invalidate_handle", assoc_handle );
                }
            }
            
            try {
                byte [] sharedSecret          = assoc.getSharedSecret();
                String  token_contents_string = token_contents.toString();

                byte [] signed = CryptUtils.calculateHmacSha1( sharedSecret, token_contents_string.getBytes( "US-ASCII" ) );
                String  signedFields = Base64.base64encodeNoCr( signed );

                redirect.append( "&openid.signed=" );
                redirect.append( "mode,identity,return_to,assoc_handle" );
                redirect.append( "&openid.sig=" );
                redirect.append( HTTP.encodeToValidUrlArgument( signedFields ));
                // FIXME? We don't do openid.invalidate_handle right now

            } catch( UnsupportedEncodingException ex ) {
                log.error( ex );
            }
        } else if( checkIdImmediate ) {
            redirect.append( "&openid.user_setup_url=" );
            redirect.append(
                    HTTP.encodeToValidUrlArgument(
                            lidRequest.getAbsoluteBaseUri()
                            + "?lid=" + HTTP.encodeToValidUrlArgument( identifier )));
        }

        if( redirect.length() > 0 ) {
            String redirectUrl = HTTP.appendArgumentPairToUrl( return_to, redirect.toString().substring( 1 ));
        
            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            section.setLocation( redirectUrl );
            section.setHttpResponseCode( 302 );
            lidResponse.setRequestedTemplateName( VerbatimStructuredResponseTemplate.VERBATIM_TEXT_TEMPLATE_NAME );
        } else {
            // user needs to authenticate first
            
        }
    }

    /**
     * Process incoming check_authentication requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param clientAuthStatus knows the authentication status of the client
     * @param requestedResource the requested resource, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    protected void processCheckAuthentication(
            SaneRequest                   lidRequest,
            StructuredResponse            lidResponse,
            LidClientAuthenticationStatus clientAuthStatus,
            HasIdentifier                 requestedResource )
        throws
            LidAbortProcessingPipelineException
    {
        if( !"POST".equals( lidRequest.getMethod() )) {
            return;
        }
        if( theDumbAssociationManager == null ) {
            return;
        }

        Map<String,String[]> postPars = lidRequest.getPostArguments();
        if( postPars == null || postPars.isEmpty() ) {
            return;
        }

        String assoc_handle = lidRequest.getPostArgument( "openid.assoc_handle" );
        String sig          = lidRequest.getPostArgument( "openid.sig" );
        String signed       = lidRequest.getPostArgument( "openid.signed" );

        boolean valid = false;
        OpenIdIdpSideAssociation assoc = theDumbAssociationManager.get( assoc_handle );
        if( assoc != null ) {
            StringBuilder toSign    = new StringBuilder( 128 );
            String        fields [] = signed.split( "," );
            for( int i=0 ; i<fields.length ; ++i ) {
                String field = fields[i];
                String value;
                if( "mode".equals( field )) { // see http://lists.danga.com/pipermail/yadis/2005-June/000734.html
                    value = "id_res";
                } else {
                    value = lidRequest.getPostArgument( field );
                }
                if( value != null ) {
                    toSign.append( field ).append( ":" ).append( value );
                    toSign.append( "\n" );
                }
            }

            byte [] bytesToSign   = toSign.toString().getBytes();
            byte [] hmacSha1      = CryptUtils.calculateHmacSha1( assoc.getSharedSecret(), bytesToSign );
            String  locallySigned = Base64.base64encode( hmacSha1 );

            if( locallySigned.equals( sig )) {
                valid = true;
            } else {
                log.warn(
                        "Error comparing sig '"
                        + sig
                        + "' with locally computed '"
                        + locallySigned
                        + "', toSign was '"
                        + toSign
                        + "'"
                        + " for request "
                        + lidRequest );
            }
        }

        // convert to the right format
        String validString;
        if( valid ) {
            validString = "true";
        } else {
            validString = "false";
        }

        // construct response
        StringBuilder responseBuffer = new StringBuilder( 64 );
        // obsoleted: responseBuffer.append( "lifetime:" ).append( assoc.getExpiresInSeconds()).append( "\n" );
        responseBuffer.append( "openid.mode:" ).append( "id_res" ).append( "\n" );
        responseBuffer.append( "is_valid:" ).append( validString ).append( "\n" );

        TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
        section.setMimeType( "text/plain" );
        section.setHttpResponseCode( 200 );
        section.setContent( responseBuffer.toString() );
        lidResponse.setRequestedTemplateName( VerbatimStructuredResponseTemplate.VERBATIM_TEXT_TEMPLATE_NAME );

        throw new LidAbortProcessingPipelineException( this );
    }

    /**
     * Internal String construction helper.
     *
     * @param redirect the redirect content to assemble
     * @param token_contents the token_content to assemble
     * @param appendName the name component of the name-value pair to append
     * @param appendValue the value component of the name-value pair to append
     */
    private static void construct(
            StringBuilder redirect,
            StringBuilder token_contents,
            String        appendName,
            String        appendValue )
    {
        redirect.append( "&openid." );
        redirect.append( appendName );
        redirect.append( "=" );
        redirect.append( HTTP.encodeToValidUrlArgument( appendValue ));

        token_contents.append( appendName );
        token_contents.append( ":" );
        token_contents.append( appendValue );
        token_contents.append( "\n" );
    }

    /**
     * The association manager to use for smart associations.
     */
    protected OpenIdIdpSideAssociationManager theSmartAssociationManager;
    
    /**
     * The association manager to use for dumb associations.
     */
    protected OpenIdIdpSideAssociationManager theDumbAssociationManager;
    
    /**
     * Yadis service fragment.
     */
    public static final String YADIS_FRAGMENT
            = "<xrd:Type>http://openid.net/signon/1.0</xrd:Type>\n"
            + "<xrd:URI>{0}</xrd:URI>\n"
            + "<openid:Delegate xmlns:openid=\"http://openid.net/xmlns/1.0\">{0}</openid:Delegate>\n";
}
