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

package org.infogrid.lid.yadis;

import java.text.MessageFormat;
import java.util.Iterator;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.VerbatimStructuredResponseTemplate;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * Default implementation of YadisPipelineProcessingStage.
 */
public class DefaultYadisPipelineProcessingStage
        extends
            AbstractYadisPipelineProcessingStage
{
    /**
     * Factory method.
     *
     * @param c the context containing the available services.
     * @return the created DefaultYadisPipelineProcessingStage
     */
    public static DefaultYadisPipelineProcessingStage create(
            Context c )
    {
        DefaultYadisPipelineProcessingStage ret = new DefaultYadisPipelineProcessingStage( c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param c the context containing the available services.
     */
    protected DefaultYadisPipelineProcessingStage(
            Context c )
    {
        super( c );
    }

    /**
     * Process any relevant requests.
     *
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param resource the resource to which the request refers, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    @Override
    public void processRequest(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            HasIdentifier      resource )
        throws
            LidAbortProcessingPipelineException
    {
        super.processRequest( lidRequest, lidResponse, resource );

        // add the HTTP header if no exception is thrown
        if( resource != null ) {
            lidResponse.addHeader( YADIS_HTTP_HEADER, resource.getIdentifier() + "?lid-meta=capabilities" );
        }
    }

    /**
     * It was discovered that this is a Yadis request. Process.
     *
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param resource the resource to which the request refers, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    protected void processYadisRequest(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            HasIdentifier      resource )
        throws
            LidAbortProcessingPipelineException
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
            section.setMimeType( XRDS_MIME_TYPE );
            lidResponse.setRequestedTemplateName( VerbatimStructuredResponseTemplate.VERBATIM_TEXT_TEMPLATE_NAME );

        } else {
            section.setHttpResponseCode( 404 );
        }

        throw new LidAbortProcessingPipelineException( this );
    }
}
