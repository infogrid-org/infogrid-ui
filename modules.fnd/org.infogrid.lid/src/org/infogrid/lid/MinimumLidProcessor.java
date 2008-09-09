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

package org.infogrid.lid;

import java.util.Iterator;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseTemplateFactory;
import org.infogrid.lid.gpg.LidGpgLocalPersonaAttributes;
import org.infogrid.util.context.Context;

/**
 * Knows how to process incoming MinimumLid requests.
 */
public class MinimumLidProcessor
        extends
            AbstractLidService
        implements
            LidService,
            LidGpgLocalPersonaAttributes
{
    /**
     * Factory method.
     *
     * @param context the context containing the available services.
     * @return the created MinimumLidProcessor
     */
    public static MinimumLidProcessor create(
            Context context )
    {
        MinimumLidProcessor ret = new MinimumLidProcessor( LID_DEFAULT_VERSION, context );
        return ret;
    }

    /**
     * Factory method with a non-default LID version.
     *
     * @param lidVersion the supported LID version
     * @param c the context containing the available services.
     * @return the created MinimumLidProcessor
     */
    public static MinimumLidProcessor create(
            String  lidVersion,
            Context c )
    {
        MinimumLidProcessor ret = new MinimumLidProcessor( lidVersion, c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param lidVersion the supported LID version
     * @param c the context containing the available services.
     */
    protected MinimumLidProcessor(
            String  lidVersion,
            Context c )
    {
        super( LID_PROFILE_NAME, LID_PROFILE_VERSION, YADIS_FRAGMENT, c );

        theLidVersion = lidVersion;
    }
    
    /**
     * Process any relevant requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param localPersona the localPersona to which the request refers
     * @throws AbortProcessingException thrown if processing is complete
     */
    public void processRequest(
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse,
            LidLocalPersona    localPersona  )
        throws
            AbortProcessingException
    {
        String  lidMeta     = lidRequest.getArgument( "lid-meta" );
        String  meta        = lidRequest.getArgument( "meta" );

        String responseString = null;
        
        if( "lid".equals( meta )) {
            StringBuilder versionString = new StringBuilder( 256 );
            versionString.append( "protocol: http://lid.netmesh.org/\n" );
            versionString.append( "version: " ).append( theLidVersion ).append( "\n" );

            Iterator<LidService> iter = getContext().contextObjectIterator( LidService.class );
            while( iter.hasNext() ) {
                LidService service = iter.next();
                                
                versionString.append( "\n" );
                versionString.append( "profile: " ).append( service.getLidProfileName()    ).append( "\n" );
                versionString.append( "version: " ).append( service.getLidProfileVersion() ).append( "\n" );
            }
            responseString = versionString.toString();

        } else if(    "gpg --export --armor".equals( lidMeta )
                   || "gpg -export --armor".equals( lidMeta )
                   || "gpg --export --armor".equals( meta )
                   || "gpg -export --armor".equals( meta ) )
        {
            String publicKey = localPersona.getAttribute( LID_GPG_PERSONA_PUBLIC_KEY_ATTRIBUTE );

            if( publicKey != null ) {
                publicKey = publicKey.trim();
            }
            if( publicKey != null && publicKey.length() > 0 ) {
                responseString = publicKey;

            } else {
                lidResponse.reportProblem(
                        new UnsupportedLidOperationException(
                                this,
                                "lid-meta=" + lidMeta ));
            }
        }
        
        if( responseString != null ) {
            lidResponse.setDefaultSectionContent( responseString );
            lidResponse.setHttpResponseCode( 200 );
            lidResponse.setMimeType( "text/plain");
            lidResponse.setRequestedTemplateName( StructuredResponseTemplateFactory.VERBATIM_TEXT_NAME );

            throw new AbortProcessingException(
                    this,
                    null,
                    null );
        }
    }
    
    /**
     * The default LID version.
     */
    public static final String LID_DEFAULT_VERSION = "2.0";

    /**
     * The LID version supported.
     */
    protected String theLidVersion;
    
    /**
     * Name of the LID profile.
     */
    public static final String LID_PROFILE_NAME = "http://lid.netmesh.org/minimum-lid/";
    
    /**
     * Name of the LID version.
     */
    public static final String LID_PROFILE_VERSION = DEFAULT_LID_SERVICE_VERSION;
    
    /**
     * Yadis service fragment.
     */
    public static final String YADIS_FRAGMENT
            = "<xrd:Type>" + LID_PROFILE_NAME + LID_PROFILE_VERSION + "</xrd:Type>\n"
            + "<xrd:URI>{0}</xrd:URI>\n";
}
