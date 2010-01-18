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

package org.infogrid.lid.openid.servlet;

import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.lid.LidProcessingPipeline;
import org.infogrid.lid.openid.DefaultOpenIdLidProcessingPipeline;
import org.infogrid.lid.openid.OpenIdAssociationException;
import org.infogrid.lid.openid.OpenIdSsoException;
import org.infogrid.lid.servlet.LidProcessingPipelineServlet;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * Invokes the LidProcessingPipeline.
 */
public class OpenIdLidProcessingPipelineServlet
        extends
            LidProcessingPipelineServlet
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public OpenIdLidProcessingPipelineServlet()
    {
        // nothing right now
    }

    /**
     * Overridable method to create the LidProcessingPipeline.
     * 
     * @param c Context for the pipeline
     * @return the created LidProcessingPipeline
     */
    @Override
    protected LidProcessingPipeline obtainLidProcessingPipeline(
            Context c )
    {
        if( thePipeline == null ) {
            thePipeline = DefaultOpenIdLidProcessingPipeline.create( c );
        }
        return thePipeline;
    }

    /**
     * Overridable method to handle Exceptions thrown by the pipeline processing.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param t the exception
     */
    @Override
    protected void handleException(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            Throwable          t )
    {
        if( t instanceof OpenIdAssociationException ) {
            OpenIdAssociationException realT = (OpenIdAssociationException) t;

            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            
            section.setHttpResponseCode( 500 );
            section.setContent( realT.getLocalizedMessage() );
            section.setMimeType( "text/plain" );
            
        } else if( t instanceof OpenIdSsoException ) {
            OpenIdSsoException realT = (OpenIdSsoException) t;
            
            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            
            section.setHttpResponseCode( 500 );
            section.setContent( realT.getLocalizedMessage() );
            section.setMimeType( "text/plain" );
            
        } else {
            super.handleException( lidRequest, lidResponse, t );
        }
    }

}
