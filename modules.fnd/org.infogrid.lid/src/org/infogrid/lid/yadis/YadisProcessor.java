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
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.lid.AbortProcessingException;
import org.infogrid.lid.AbstractLidService;
import org.infogrid.lid.LidService;
import org.infogrid.util.context.Context;

/**
 * Knows how to process Yadis requests.
 */
public class YadisProcessor
        extends
            AbstractLidService
        implements
            LidService
{
    /**
     * Factory method.
     *
     * @param c the context containing the available services.
     * @return the created YadisProcessor
     */
    public static YadisProcessor create(
            Context c )
    {
        YadisProcessor ret = new YadisProcessor( c );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param c the context containing the available services.
     */
    protected YadisProcessor(
            Context c )
    {
        super( LID_PROFILE_NAME, LID_PROFILE_VERSION, YADIS_FRAGMENT, c );
    }
    
    /**
     * Process any relevant requests.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @throws AbortProcessingException thrown if processing is complete
     */
    public void processRequest(
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse )
        throws
            AbortProcessingException
    {
        String meta = lidRequest.getArgument( "lid-meta" );
        if( meta == null ) {
            meta = lidRequest.getArgument( "meta" );
        }

        if( "capabilities".equals( meta )) {
            StringBuilder ret = new StringBuilder( 256 );
            ret.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" );
            ret.append( "<XRDS xmlns=\"xri://$xrds\" xmlns:xrd=\"xri://$xrd*($v*2.0)\">\n" );
            ret.append( " <xrd:XRD>\n" );

            String thisUrl = lidRequest.getAbsoluteBaseUri();
            
            Iterator<LidService> iter = getContext().contextObjectIterator( LidService.class );
            int i = 1;
            while( iter.hasNext() ) {
                LidService service = iter.next();

                String frag  = service.getParameterizedYadisFragment();
                String frag2 = MessageFormat.format( frag, thisUrl );

                ret.append( "  <xrd:Service priority=\"" ).append( i ).append( "\">\n" );
                ret.append( frag2 );
                ret.append( "  </xrd:Service>\n" );
            }
            ret.append( " </xrd:XRD>\n" );
            ret.append( "</XRDS>\n" );

            throw new EmitYadisAbortProcessingException(
                    this,
                    lidRequest,
                    lidResponse,
                    ret.toString() );
        }

        // add the HTTP header
        lidResponse.addAdditionalHeader( "X-XRDS-Location:", lidRequest.getAbsoluteBaseUri() + "?meta=capabilities" );
    }
    
    /**
     * Name of the LID profile.
     */
    public static final String LID_PROFILE_NAME = "http://lid.netmesh.org/yadis/";
    
    /**
     * Name of the LID version.
     */
    public static final String LID_PROFILE_VERSION = "1.0";
    
    /**
     * Yadis service fragment.
     */
    public static final String YADIS_FRAGMENT
            = "<xrd:Type>" + LID_PROFILE_NAME + LID_PROFILE_VERSION + "</xrd:Type>\n"
            + "<xrd:URI>{0}?lid-meta=capabilities</xrd:URI>\n";
}
