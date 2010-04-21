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
import java.util.Iterator;
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
import org.infogrid.jee.templates.utils.JeeTemplateUtils;
import org.infogrid.lid.DefaultLidPipeline;
import org.infogrid.lid.LidClientAuthenticationStatus;
import org.infogrid.lid.LidPipeline;
import org.infogrid.lid.LidPipelineInstructions;
import org.infogrid.lid.LidPipelineStageInstructions;
import org.infogrid.lid.LidSsoPipelineStageInstructions;
import org.infogrid.util.SimpleStringIdentifier;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.IdentifierStringifier;

/**
 * Invokes the LidPipeline.
 */
public class LidPipelineServlet
        extends
            AbstractServletInvokingServlet
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( LidPipelineServlet.class ); // our own, private logger

    /**
     * Constructor.
     */
    public LidPipelineServlet()
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

        LidPipeline pipe = obtainLidPipeline( appContext );

        String  site  = originalRequest.getAbsoluteContextUri();
        String  realm = site;
        boolean done  = false;
        LidClientAuthenticationStatus authStatus;

        try {
            LidPipelineInstructions compoundInstructions
                    = pipe.processPipeline( originalRequest, SimpleStringIdentifier.create( site ), realm );

            authStatus = compoundInstructions.getClientAuthenticationStatus();

            if( authStatus.isAnonymous() ) {
                if( authStatus.getClientIdentifier() != null ) {
                    request.setAttribute( CLIENT_ATTRIBUTE_NAME,    authStatus.getClientAccount() );
                    request.setAttribute( CLIENT_ID_ATTRIBUTE_NAME, authStatus.getClientIdentifier() );
                    request.setAttribute( USER_ID_ATTRIBUTE_NAME,   authStatus.getClientIdentifier() );
                    request.setAttribute( LID_ATTRIBUTE_NAME,       authStatus.getClientIdentifier() );
                    request.setAttribute( USER_NICK_ATTRIBUTE_NAME, IdentifierStringifier.toColloquialIdentifier( authStatus.getClientIdentifier().toExternalForm() )); // FIXME
                }
                if( authStatus.getClientAccountIdentifier() != null ) {
                    request.setAttribute( ACCOUNT_ATTRIBUTE_NAME,    authStatus.getClientAccount() );
                    request.setAttribute( ACCOUNT_ID_ATTRIBUTE_NAME, authStatus.getClientAccountIdentifier() );
                    if( authStatus.getClientIdentifier() == null ) {
                        request.setAttribute( USER_ID_ATTRIBUTE_NAME,   authStatus.getClientAccountIdentifier() );
                        request.setAttribute( LID_ATTRIBUTE_NAME,       authStatus.getClientAccountIdentifier() );
                        request.setAttribute( USER_NICK_ATTRIBUTE_NAME, IdentifierStringifier.toColloquialIdentifier( authStatus.getClientAccountIdentifier().toExternalForm() )); // FIXME
                    }
                }
            }

            LidPipelineStageInstructions currentInstructions;
            
            if( !done ) {
                currentInstructions = compoundInstructions.getYadisInstructions();
                if( currentInstructions != null ) {
                    done = currentInstructions.applyAsRecommended( originalRequest, lidResponse.getDelegate() );
                }
            }

            if( !done ) {
                Iterator<LidSsoPipelineStageInstructions> iter = compoundInstructions.getSsoInstructionsIter();
                while( !done && iter.hasNext() ) {
                    currentInstructions = iter.next();
                    if( currentInstructions != null ) {
                        done = currentInstructions.applyAsRecommended( originalRequest, lidResponse.getDelegate() );
                    }
                }
            }

            if( !done ) {
                currentInstructions = compoundInstructions.getSessionManagementInstructions();
                if( currentInstructions != null ) {
                    done = currentInstructions.applyAsRecommended( originalRequest, lidResponse.getDelegate() );
                }
            }

            if( !done ) {
                invokeServlet( originalRequest, lidResponse, compoundInstructions );
            }

        } catch( Throwable ex ) {
            handleException( originalRequest, lidResponse, ex );
        }
    }
    
    /**
     * Overridable method to create the LidPipeline.
     * 
     * @param c Context for the pipeline
     * @return the created LidPipeline
     */
    protected LidPipeline obtainLidPipeline(
            Context c )
    {
        if( thePipeline == null ) {
            thePipeline = DefaultLidPipeline.create( c );
        }
        return thePipeline;
    }

    /**
     * Invoke the configured servlet. Instead of doing what our superclass does, we use the template framework.
     * 
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param compoundInstructions the recommended LidPipelineInstructions to follow
     * @throws ServletException thrown if an error occurred
     * @throws IOException thrown if an I/O error occurred
     */
    protected void invokeServlet(
            SaneRequest             lidRequest,
            StructuredResponse      lidResponse,
            LidPipelineInstructions compoundInstructions )
        throws
            ServletException,
            IOException
    {
        lidRequest.setAttribute( PROCESSING_PIPELINE_INSTRUCTIONS_ATTRIBUTE_NAME, compoundInstructions );
        lidRequest.setAttribute( REQUESTED_RESOURCE_ATTRIBUTE_NAME,               compoundInstructions.getRequestedResource() );

        RequestDispatcher dispatcher = getServletContext().getNamedDispatcher( theServletName );

        if( dispatcher != null ) {
            JeeTemplateUtils.runRequestDispatcher(
                    dispatcher,
                    lidRequest,
                    lidResponse );

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
        log.error( t );

        TextStructuredResponseSection section = lidResponse.getDefaultTextSection();
        section.setHttpResponseCode( 500 );

        lidResponse.setRequestedTemplateName( NoContentStructuredResponseTemplate.NO_CONTENT_TEMPLATE_NAME );
    }

    /**
     * Cached LidPipeline.
     */
    protected LidPipeline thePipeline;

    /**
     * Name of the HasIdentifier instance found in the request after the pipeline has
     * been processed.
     */
    public static final String REQUESTED_RESOURCE_ATTRIBUTE_NAME = "org_infogrid_lid_RequestedResource";

    /**
     * Name of the LidPipelineProcessingInstructions that are recommended.
     */
    public static final String PROCESSING_PIPELINE_INSTRUCTIONS_ATTRIBUTE_NAME = "org_infogrid_lid_ProcessingPipelineInstructions";

    /**
     * Name of the request attribute that contains the identifier of the currently authenticated user if that user has
     * been authenticated with a remote account, e.g. OpenID, regardless of whether or not the user is associated with a
     * local LidAccount.
     */
    public static final String CLIENT_ID_ATTRIBUTE_NAME = "CLIENT_ID";

    /**
     * Name of the request attribute that contains the HasIdentifier object (e.g. a MeshObject) representing the authenticated
     * user if that user has been authenticated with a remote account, e.g. OpenID, regardless of whether or not the
     * user is associated with a local LidAccount.
     */
    public static final String CLIENT_ATTRIBUTE_NAME = "CLIENT";

    /**
     * Name of the request attribute that contains the identifier of the user's local LidAccount, if any.
     */
    public static final String ACCOUNT_ID_ATTRIBUTE_NAME = "ACCOUNT_ID";

    /**
     * Name of the request attribute that contains the user's local LidAccount, if any.
     */
    public static final String ACCOUNT_ATTRIBUTE_NAME = "ACCOUNT";

    /**
     * Name of the request attribute that contains the identifier of the currently authenticated user. If the user has
     * authenticated with the local LidAccount, it holds the identifier of the local LidAccount. If the user has authenticated
     * with a remote account (e.g. OpenID), it holds the identifier of the remote account (regardless of whether or
     * not it is associated with a local LidAccount).
     */
    public static final String USER_ID_ATTRIBUTE_NAME = "USER_ID";

    /**
     * Name of the request attribute that contains the user's nickname to be shown on web pages.
     */
    public static final String USER_NICK_ATTRIBUTE_NAME = "USER_NICK";

    /**
     * Synonym of USER_ID.
     */
    public static final String LID_ATTRIBUTE_NAME = "lid";
}