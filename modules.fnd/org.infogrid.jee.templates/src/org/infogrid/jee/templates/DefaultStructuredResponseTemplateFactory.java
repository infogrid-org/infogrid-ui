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

package org.infogrid.jee.templates;

import javax.servlet.RequestDispatcher;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.http.SaneRequest;

/**
 * A default implementation for a StructuredResponseTemplateFactory.
 */
public class DefaultStructuredResponseTemplateFactory
        extends
            AbstractFactory<SaneServletRequest,StructuredResponseTemplate,StructuredResponse>
        implements
            StructuredResponseTemplateFactory
{
    /**
     * Factory method.
     *
     * @return the created DefaultStructuredResponseTemplateFactory
     */
    public static DefaultStructuredResponseTemplateFactory create()
    {
        DefaultStructuredResponseTemplateFactory ret = new DefaultStructuredResponseTemplateFactory( DEFAULT_TEMPLATE_NAME );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param defaultTemplateName name of the default template
     * @return the created DefaultStructuredResponseTemplateFactory
     */
    public static DefaultStructuredResponseTemplateFactory create(
            String defaultTemplateName )
    {
        DefaultStructuredResponseTemplateFactory ret = new DefaultStructuredResponseTemplateFactory( defaultTemplateName );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param defaultTemplateName name of the default template
     */
    protected DefaultStructuredResponseTemplateFactory(
            String defaultTemplateName )
    {
        theDefaultTemplateName = defaultTemplateName;
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
            SaneServletRequest request,
            StructuredResponse structured )
        throws
            FactoryException
    {
        String mime         = structured.getMimeType();
        String templateName = getRequestedTemplate( request );

        RequestDispatcher dispatcher = findRequestDispatcher( request, structured, templateName, mime );
        
        if( dispatcher == null && ( templateName != null && templateName.length() > 0 )) {
            // try default template if named template did not work
            dispatcher = findRequestDispatcher( request, structured, "default", mime );
        }
        
        StructuredResponseTemplate ret;
        if( dispatcher != null ) {
            ret = JspStructuredResponseTemplate.create( dispatcher, request, templateName, structured );
        } else {
            // if none is there, we stream verbatim
            ret = VerbatimStructuredResponseTemplate.create( request, templateName, structured );
        }
        return ret;
    }
    
    /**
     * Obtain the name of the requested layout, if any.
     * 
     * @param request the incoming HTTP request for which the response is being created
     * @return class name of the requested layout, if any
     */
    public String getRequestedTemplate(
            SaneServletRequest request )
    {
        String ret = request.getArgument( StructuredResponseTemplate.LID_TEMPLATE_PARAMETER_NAME );

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
        if( templateName == null || templateName.length() == 0 ) {
            templateName = theDefaultTemplateName;
        }

        StringBuilder jspPath = new StringBuilder();
        jspPath.append( "/s/templates/" );
        jspPath.append( templateName ).append( "/" );
        if( mime != null && mime.length() > 0 ) {
            jspPath.append( mime ).append( "/" );
        }
        jspPath.append( DEFAULT_JSP_NAME );

        InfoGridWebApp    app        = InfoGridWebApp.getSingleton();
        RequestDispatcher dispatcher = app.findLocalizedRequestDispatcher(
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
     * Name of the default template, if no other has been specified in ther constructor or the request.
     */
    public static final String DEFAULT_TEMPLATE_NAME = "default";
    
    /**
     * Name of the default JSP for each template.
     */
    public static final String DEFAULT_JSP_NAME = "template.jsp";
}
