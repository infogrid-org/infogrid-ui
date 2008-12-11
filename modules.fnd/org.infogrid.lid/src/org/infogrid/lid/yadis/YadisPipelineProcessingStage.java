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

package org.infogrid.lid.yadis;

import java.text.MessageFormat;
import java.util.Iterator;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.VerbatimStructuredResponseTemplate;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidResource;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * Knows how to process Yadis requests.
 */
public class YadisPipelineProcessingStage
        extends
            AbstractYadisService
        implements
            DeclaresYadisFragment
{
    /**
     * Factory method.
     *
     * @param c the context containing the available services.
     * @return the created YadisPipelineProcessingStage
     */
    public static YadisPipelineProcessingStage create(
            Context c )
    {
        YadisPipelineProcessingStage ret = new YadisPipelineProcessingStage( c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param c the context containing the available services.
     */
    protected YadisPipelineProcessingStage(
            Context c )
    {
        super( YADIS_FRAGMENT, c );
    }
    
    /**
     * Process any relevant requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param resource the resource to which the request refers, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    public void processRequest(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            LidResource        resource )
        throws
            LidAbortProcessingPipelineException
    {
        String meta = lidRequest.getArgument( "lid-meta" );
        if( meta == null ) {
            meta = lidRequest.getArgument( "meta" );
        }
        String acceptHeader = lidRequest.getAcceptHeader();

        if(    "capabilities".equals( meta )
            || ( acceptHeader != null && acceptHeader.indexOf( "application/xrds+xml" ) >= 0 ))
        {
            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            if( resource != null ) {
                StringBuilder content = new StringBuilder( 256 );
                content.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
                content.append( "<XRDS xmlns=\"xri://$xrds\" xmlns:xrd=\"xri://$xrd*($v*2.0)\">\n" );
                content.append( " <xrd:XRD>\n" );

                String thisUrl = lidRequest.getAbsoluteBaseUri();

                Iterator<DeclaresYadisFragment> iter = getContext().contextObjectIterator( DeclaresYadisFragment.class );
                int i = 1;
                while( iter.hasNext() ) {
                    DeclaresYadisFragment service = iter.next();

                    String frag  = service.getParameterizedYadisFragment();
                    if( frag != null ) {
                        String frag2 = MessageFormat.format( frag, thisUrl );

                        content.append( "  <xrd:Service priority=\"" ).append( i ).append( "\">\n" );
                        content.append( frag2 );
                        content.append( "  </xrd:Service>\n" );
                    }
                }
                content.append( " </xrd:XRD>\n" );
                content.append( "</XRDS>\n" );

                section.setContent( content.toString() );
                section.setHttpResponseCode( 200 );
                section.setMimeType( "application/xrds+xml");
                lidResponse.setRequestedTemplateName( VerbatimStructuredResponseTemplate.VERBATIM_TEXT_TEMPLATE_NAME );

            } else {
                section.setHttpResponseCode( 404 );
            }
            
            throw new LidAbortProcessingPipelineException( this );
        }

        // add the HTTP header
        if( resource != null ) {
            lidResponse.setYadisHeader( resource.getIdentifier() + "?lid-meta=capabilities" );
        }
    }
    
    /**
     * Yadis service fragment.
     */
    public static final String YADIS_FRAGMENT
            = "<xrd:Type>http://lid.netmesh.org/yadis/1.0</xrd:Type>\n"
            + "<xrd:URI>{0}?lid-meta=capabilities</xrd:URI>\n";
}
