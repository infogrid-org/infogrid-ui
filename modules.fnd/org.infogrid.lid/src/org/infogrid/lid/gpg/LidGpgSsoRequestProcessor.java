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
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.lid.AbortProcessingException;
import org.infogrid.lid.AbstractLidService;
import org.infogrid.lid.AuthenticationNeededAbortProcessingException;
import org.infogrid.lid.LidLocalPersona;
import org.infogrid.lid.LidService;
import org.infogrid.lid.RedirectAbortProcessingException;
import org.infogrid.lid.UnsupportedLidOperationException;
import org.infogrid.util.context.Context;

/**
 * Processes LID SSO requests. This is the Identity Provider side.
 */
public class LidGpgSsoRequestProcessor
        extends
            AbstractLidService
        implements
            LidService,
            LidGpgLocalPersonaAttributes

{
    /**
     * Factory method.
     *
     * @param c the context containing the available services.
     * @return the created LidGpgSsoRequestProcessor
     */
    public static LidGpgSsoRequestProcessor create(
            Context c )
    {
        LidGpgSsoRequestProcessor ret = new LidGpgSsoRequestProcessor( c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param c the context containing the available services.
     */
    protected LidGpgSsoRequestProcessor(
            Context c )
    {
        super( LID_PROFILE_NAME, LID_PROFILE_VERSION, YADIS_FRAGMENT, c );
    }

    /**
     * Process any relevant requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param persona the LidLocalPersona that is the subject of this request, if any
     * @throws AbortProcessingException thrown if processing is complete
     * @throws IOException thrown if an input/output error occurred
     */
    public void processRequest(
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse,
            LidLocalPersona    persona )
        throws
            AbortProcessingException,
            IOException
    {
        String  action  = lidRequest.getArgument( "lid-action" );
        if( action == null || action.length() == 0 ) {
            action = lidRequest.getArgument( "action" );
        }

        if( !"sso-approve".equals( action )) {
            return;
        }
        if( persona == null ) {
            return;
        }

        String target = lidRequest.getArgument( "lid-target" );
        if( target == null || target.length() == 0 ) {
            target = lidRequest.getArgument( "target" );
        }

        if( target == null || target.length() == 0 ) {
            lidResponse.reportProblem(
                    new UnsupportedLidOperationException(
                            this,
                            "lid-target=<empty>" ));
            
            return;
        }

        String localLid = lidRequest.getAbsoluteBaseUri();

        boolean isAuthenticatedAsOwner = lidRequest.getAbsoluteBaseUri().startsWith( persona.getIdentifier() )
                                && (    lidRequest.getAbsoluteBaseUri().length() == persona.getIdentifier().length()
                                     || lidRequest.getAbsoluteBaseUri().substring( persona.getIdentifier().length(), 1 ).equals( "/" ));
        
        if( !isAuthenticatedAsOwner ) {
            throw new AuthenticationNeededAbortProcessingException(
                    this,
                    lidRequest,
                    lidResponse,
                    persona,
                    target );
        }
        Matcher m = lidCredTypePattern.matcher( target );
        if( m.find() ) {
            target = m.replaceAll( "" );
        }

        m = credTypePattern.matcher( target );
        if( m.find() ) {
            target = m.replaceAll( "" );
        }

        m = clientidPattern.matcher( target );
        if( m.find() ) {
            target = m.replaceAll( "" );
        }

        m = lidPattern.matcher( target );
        if( m.find() ) {
            target = m.replaceAll( "" );
        }

        m = lidNoncePattern.matcher( target );
        if( m.find() ) {
            target = m.replaceAll( "" );
        }

        m = noncePattern.matcher( target );
        if( m.find() ) {
            target = m.replaceAll( "" );
        }
            
        if( target.endsWith( "&" )) {
            target = target.substring( 0, target.length()-1 );
        }
        if( target.endsWith( "?" )) {
            target = target.substring( 0, target.length()-1 );
        }

        String lidVersion = "1"; // FIXME?

        LidGpg theGpg = getContext().findContextObjectOrThrow( LidGpg.class );

        String privateKey = persona.getAttribute( LID_GPG_PERSONA_PRIVATE_KEY_ATTRIBUTE );

        if( privateKey != null ) {
            privateKey = privateKey.trim();
        }
        if( privateKey == null || privateKey.length() == 0 ) {
            lidResponse.reportProblem(
                    new UnsupportedLidOperationException(
                            this,
                            "lid-action=sso-approve" ));
            return;
        }
        
        theGpg.importPrivateKey( privateKey );

        String append = theGpg.signUrl( localLid, target, lidVersion );
        target += append;

        throw new RedirectAbortProcessingException( this, lidRequest, lidResponse, HttpServletResponse.SC_TEMPORARY_REDIRECT, target );        
    }

    /**
     * Various pre-compiled patterns.
     */
    protected static Pattern lidCredTypePattern = Pattern.compile( "lid-credtype=[^&]*&?" );
    protected static Pattern credTypePattern    = Pattern.compile( "credtype=[^&]*&?" );
    protected static Pattern clientidPattern    = Pattern.compile( "clientid=[^&]*&?" );
    protected static Pattern lidPattern         = Pattern.compile( "lid=[^&]*&?" );
    protected static Pattern lidNoncePattern    = Pattern.compile( "lid-nonce=[^&]*&?" );
    protected static Pattern noncePattern       = Pattern.compile( "nonce=[^&]*&?" );

    
    /**
     * Name of the LID profile.
     */
    public static final String LID_PROFILE_NAME = "http://lid.netmesh.org/sso/";
    
    /**
     * Name of the LID version.
     */
    public static final String LID_PROFILE_VERSION = DEFAULT_LID_SERVICE_VERSION;
    
    /**
     * Yadis service fragment.
     */
    public static final String YADIS_FRAGMENT
            = "<xrd:Type>" + LID_PROFILE_NAME + LID_PROFILE_VERSION + "</xrd:Type>\n";
}
