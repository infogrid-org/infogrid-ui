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

package org.infogrid.lid.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.AbstractServletInvokingServlet;
import org.infogrid.jee.templates.NoContentStructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.templates.VerbatimStructuredResponseTemplate;
import org.infogrid.jee.templates.utils.JeeTemplateUtils;
import org.infogrid.lid.DefaultLidProcessingPipeline;
import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidAbortProcessingPipelineWithContentException;
import org.infogrid.lid.LidAbortProcessingPipelineWithErrorException;
import org.infogrid.lid.LidAbortProcessingPipelineWithRedirectException;
import org.infogrid.lid.LidProcessingPipeline;
import org.infogrid.lid.yadis.YadisPipelineProcessingStage;
import org.infogrid.util.SimpleStringIdentifier;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Invokes the LidProcessingPipeline.
 */
public class LidProcessingPipelineServlet
        extends
            AbstractServletInvokingServlet
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( LidProcessingPipelineServlet.class ); // our own, private logger

    /**
     * Constructor.
     */
    public LidProcessingPipelineServlet()
    {
        // nothing right now
    }

    /**
     * Main servlet method.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public final void service(
            ServletRequest  request,
            ServletResponse response )
        throws
            ServletException,
            IOException
    {
        InfoGridWebApp     app             = InfoGridWebApp.getSingleton();
        Context            appContext      = app.getApplicationContext();
        HttpServletRequest realRequest     = (HttpServletRequest) request;
        SaneServletRequest lidRequest      = SaneServletRequest.create( realRequest );
        SaneRequest        originalRequest = lidRequest.getOriginalSaneRequest();
        StructuredResponse lidResponse = (StructuredResponse) request.getAttribute( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );

        LidProcessingPipeline pipe = obtainLidProcessingPipeline( appContext );

        String site  = originalRequest.getAbsoluteContextUri();
        String realm = site;

        try {
            pipe.processPipeline( originalRequest, SimpleStringIdentifier.create( site ), realm );

            invokeServlet( originalRequest, lidResponse );

        } catch( Throwable ex ) {
            handleException( originalRequest, lidResponse, ex );
        }
    }
    
    /**
     * Overridable method to create the LidProcessingPipeline.
     * 
     * @param c Context for the pipeline
     * @return the created LidProcessingPipeline
     */
    protected LidProcessingPipeline obtainLidProcessingPipeline(
            Context c )
    {
        if( thePipeline == null ) {
            thePipeline = DefaultLidProcessingPipeline.create( c );
        }
        return thePipeline;
    }
    
    /**
     * Invoke the configured servlet. Instead of doing what our superclass does, we use the template framework.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    protected void invokeServlet(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse )
        throws
            ServletException,
            IOException
    {
        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher( theServletName );

        if( dispatcher != null ) {
            JeeTemplateUtils.runRequestDispatcher(
                    dispatcher,
                    lidRequest,
                    lidResponse );

            String yadisHeader = (String) lidRequest.getAttribute( YadisPipelineProcessingStage.YADIS_HTTP_HEADER_REQUEST_ATTRIBUTE_NAME );
            if( yadisHeader != null ) {
                lidResponse.addHeader( YadisPipelineProcessingStage.YADIS_HTTP_HEADER, yadisHeader );
            }

        } else {
            log.error( "Could not find RequestDispatcher (servlet) with name " + theServletName );
        }
    }
    
    /**
     * Overridable method to handle Exceptions thrown by the pipeline processing.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param t the exception
     */
    protected void handleException(
            SaneRequest        lidRequest,
            StructuredResponse lidResponse,
            Throwable          t )
    {
        if( t instanceof LidAbortProcessingPipelineWithContentException ) {

            LidAbortProcessingPipelineWithContentException realT = (LidAbortProcessingPipelineWithContentException) t;

            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            section.setHttpResponseCode( 200 );
            section.setContent(  realT.getContent()     );
            section.setMimeType( realT.getContentType() );

            lidResponse.setRequestedTemplateName( VerbatimStructuredResponseTemplate.VERBATIM_TEXT_TEMPLATE_NAME );

        } else if( t instanceof LidAbortProcessingPipelineWithRedirectException ) {

            LidAbortProcessingPipelineWithRedirectException realT = (LidAbortProcessingPipelineWithRedirectException) t;

            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            section.setHttpResponseCode( realT.getStatus() );
            section.setLocation(         realT.getLocation() );

            lidResponse.setRequestedTemplateName( NoContentStructuredResponseTemplate.NO_CONTENT_TEMPLATE_NAME );

        } else if( t instanceof LidAbortProcessingPipelineWithErrorException ) {

            LidAbortProcessingPipelineWithErrorException realT = (LidAbortProcessingPipelineWithErrorException) t;

            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            section.setHttpResponseCode( realT.getStatus() );

            lidResponse.setRequestedTemplateName( NoContentStructuredResponseTemplate.NO_CONTENT_TEMPLATE_NAME );

        } else if( t instanceof LidAbortProcessingPipelineException ) {
            // do nothing, this is fine
        } else {

            log.error( t );

            TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
            section.setHttpResponseCode( 500 );

            lidResponse.setRequestedTemplateName( NoContentStructuredResponseTemplate.NO_CONTENT_TEMPLATE_NAME );
        }
    }

    /**
     * Cached LidProcessingPipeline.
     */
    protected LidProcessingPipeline thePipeline;
}
