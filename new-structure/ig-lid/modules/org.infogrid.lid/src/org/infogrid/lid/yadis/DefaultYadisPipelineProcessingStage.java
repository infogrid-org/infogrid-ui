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

package org.infogrid.lid.yadis;

import java.text.MessageFormat;
import java.util.Iterator;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidAbortProcessingPipelineWithContentException;
import org.infogrid.lid.LidAbortProcessingPipelineWithErrorException;
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
     * It was discovered that this is a Yadis request. Process.
     *
     * @param lidRequest the incoming request
     * @param resource the resource to which the request refers, if any
     * @throws LidAbortProcessingPipelineException thrown if processing is complete
     */
    protected void processYadisRequest(
            SaneRequest        lidRequest,
            HasIdentifier      resource )
        throws
            LidAbortProcessingPipelineException
    {
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

            throw new LidAbortProcessingPipelineWithContentException( content.toString(), XRDS_MIME_TYPE, this );

        } else {
            throw new LidAbortProcessingPipelineWithErrorException( 404, this );
        }
    }
}
