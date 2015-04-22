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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.templates;

import javax.servlet.RequestDispatcher;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * A default implementation for a StructuredResponseTemplateFactory.
 */
public class DefaultStructuredResponseTemplateFactory
        extends
            AbstractFactory<SaneRequest,StructuredResponseTemplate,StructuredResponse>
        implements
            StructuredResponseTemplateFactory
{
    /**
     * Factory method.
     *
     * @return the created DefaultStructuredResponseTemplateFactory
     */
    public static DefaultStructuredResponseTemplateFactory create(
            InfoGridWebApp app )
    {
        DefaultStructuredResponseTemplateFactory ret
                = new DefaultStructuredResponseTemplateFactory( DEFAULT_TEMPLATE_NAME, DEFAULT_MIME_TYPE, app );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param defaultTemplateName name of the default template
     * @return the created DefaultStructuredResponseTemplateFactory
     */
    public static DefaultStructuredResponseTemplateFactory create(
            String         defaultTemplateName,
            InfoGridWebApp app )
    {
        DefaultStructuredResponseTemplateFactory ret
                = new DefaultStructuredResponseTemplateFactory( defaultTemplateName, DEFAULT_MIME_TYPE, app );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param defaultTemplateName name of the default template
     * @param defaultMimeType default mime type of no other is specified.
     * @return the created DefaultStructuredResponseTemplateFactory
     */
    public static DefaultStructuredResponseTemplateFactory create(
            String         defaultTemplateName,
            String         defaultMimeType,
            InfoGridWebApp app )
    {
        DefaultStructuredResponseTemplateFactory ret
                = new DefaultStructuredResponseTemplateFactory( defaultTemplateName, defaultMimeType, app );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param defaultTemplateName name of the default template
     * @param defaultMimeType default mime type of no other is specified.
     */
    protected DefaultStructuredResponseTemplateFactory(
            String         defaultTemplateName,
            String         defaultMimeType,
            InfoGridWebApp app )
    {
        theDefaultTemplateName = defaultTemplateName;
        theDefaultMimeType     = defaultMimeType;
        theApp                 = app;
    }

    /**
     * Factory method.
     *
     * @param request the incoming HTTP request for which the response is being created
     * @param structured the StructuredResponse that contains the content to be returned
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public StructuredResponseTemplate obtainFor(
            SaneRequest        request,
            StructuredResponse structured )
        throws
            FactoryException
    {
        TextStructuredResponseSection   defaultTextSection   = structured.getDefaultTextSection();
        BinaryStructuredResponseSection defaultBinarySection = structured.getDefaultBinarySection();

        String mime;
        if( !defaultTextSection.isEmpty() ) {
            mime = defaultTextSection.getMimeType();
        } else if( !defaultBinarySection.isEmpty() ) {
            mime = defaultBinarySection.getMimeType();
        } else {
            mime = theDefaultMimeType;
        }

        StructuredResponseTemplate ret;

        String requestedTemplateName     = structured.getRequestedTemplateName();
        String userRequestedTemplateName = getUserRequestedTemplate( request );
        
        if( requestedTemplateName == null ) {
            // internally requested template overrides user-requested template
            requestedTemplateName = userRequestedTemplateName;
        }

        Context c = (Context) request.getAttribute( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );
        if( c == null ) {
            c = theApp.getApplicationContext();
        }
        
        if( NoContentStructuredResponseTemplate.NO_CONTENT_TEMPLATE_NAME.equals( requestedTemplateName )) {
            ret = NoContentStructuredResponseTemplate.create(
                    request,
                    requestedTemplateName,
                    userRequestedTemplateName,
                    structured,
                    mime,
                    c );

        } else if( VerbatimStructuredResponseTemplate.VERBATIM_TEXT_TEMPLATE_NAME.equals( requestedTemplateName )) {
            ret = VerbatimStructuredResponseTemplate.create(
                    request,
                    requestedTemplateName,
                    userRequestedTemplateName,
                    structured,
                    mime,
                    c );

        } else {
            RequestDispatcher dispatcher = null;

            if( requestedTemplateName != null ) {
                dispatcher = findRequestDispatcher( request, structured, requestedTemplateName, mime );
            }

            if( dispatcher == null ) {
                // try default template if named template did not work
                dispatcher = findRequestDispatcher( request, structured, theDefaultTemplateName, mime );
            }
            if( dispatcher == null && mime == null ) {
                // if no mime type is specified, default to html
                if( requestedTemplateName != null ) {
                    dispatcher = findRequestDispatcher( request, structured, requestedTemplateName, "text/html" );
                }
                if( dispatcher == null && ( requestedTemplateName != null && requestedTemplateName.length() > 0 )) {
                    // try default template if named template did not work
                    dispatcher = findRequestDispatcher( request, structured, theDefaultTemplateName, "text/html" );
                }
            }

            if( dispatcher != null ) {
                ret = JspStructuredResponseTemplate.create(
                        dispatcher,
                        request,
                        requestedTemplateName,
                        userRequestedTemplateName,
                        structured,
                        mime,
                        c );

            } else if( mime != null && !mime.startsWith( "text/" )) {
                // binary content
                ret = BinaryPassThruStructuredResponseTemplate.create(
                        request,
                        structured,
                        mime,
                        c );
                
            } else {
                // all hope is lost, we have to stream verbatim whatever it is that is in structured
                ret = VerbatimStructuredResponseTemplate.create(
                        request,
                        requestedTemplateName,
                        userRequestedTemplateName,
                        structured,
                        mime,
                        c );
            }
        }
        return ret;
    }
    
    /**
     * Obtain the name of the requested layout template, if any.
     * 
     * @param request the incoming HTTP request for which the response is being created
     * @return class name of the requested layout template, if any
     */
    public String getUserRequestedTemplate(
            SaneRequest request )
    {
        String ret = request.getUrlArgument( StructuredResponseTemplate.LID_TEMPLATE_PARAMETER_NAME );

        if( ret == null ) {
            ret = request.getCookieValue( StructuredResponseTemplate.LID_TEMPLATE_COOKIE_NAME );                
        }
        
        return ret;
    }

    /**
     * Find a RequestDispatcher for a given template name and mime type.
     * 
     * @param request the incoming HTTP request for which the response is being created
     * @param structured the StructuredResponse that contains the content to be returned
     * @param templateName the name of the template
     * @param mime the mime type of the response
     * @return the found RequestDispatcher
     */
    protected RequestDispatcher findRequestDispatcher(
            SaneRequest        request,
            StructuredResponse structured,
            String             templateName,
            String             mime )
    {
        StringBuilder jspPath = new StringBuilder();
        jspPath.append( PATH_TO_TEMPLATES );
        jspPath.append( templateName ).append( "/" );
        if( mime != null && mime.length() > 0 ) {
            jspPath.append( mime ).append( "/" );
        }
        jspPath.append( TEMPLATE_JSP_NAME );

        RequestDispatcher dispatcher = theApp.findLocalizedRequestDispatcher(
                jspPath.toString(),
                request.acceptLanguageIterator(),
                structured.getServletContext() );
        
        return dispatcher;
    }

    /**
     * Name of the default template, if no other has been specified in the request.
     */
    protected String theDefaultTemplateName;

    /**
     * The default mime type, if no other has been specified in the request.
     */
    protected String theDefaultMimeType;

    /**
     * The app we belong to.
     */
    protected InfoGridWebApp theApp;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( DefaultStructuredResponseTemplateFactory.class );
    
    /**
     * Name of the default template, if no other has been specified in ther constructor or the request.
     */
    public static final String DEFAULT_TEMPLATE_NAME = theResourceHelper.getResourceStringOrDefault( "DefaultTemplateName", "default" );
    
    /**
     * Default mime type, if no other has been specified in ther constructor or the request.
     */
    public static final String DEFAULT_MIME_TYPE = theResourceHelper.getResourceStringOrDefault( "DefaultMimeType", "text/html" );

    /**
     * Name of the JSP that contains the template.
     */
    public static final String TEMPLATE_JSP_NAME = theResourceHelper.getResourceStringOrDefault( "TemplateJspName", "template.jsp" );
    
    /**
     * Path to the directory containing the default template jsp.
     */
    public static final String PATH_TO_TEMPLATES = theResourceHelper.getResourceStringOrDefault( "PathToTemplates", "/s/templates/" );
}
