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

package org.infogrid.jee.rest.net;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Collection of utility methods that are useful with InfoGrid JEE applications
 * and aware of InfoGrid REST conventions and the Net implementation.
 */
public class NetRestfulJeeFormatter
        extends
            RestfulJeeFormatter
{
    /**
     * Factory method.
     * 
     * @param stringRepDir the StringRepresentationDirectory to use
     * @return the created NetRestfulJeeFormatter
     */
    public static NetRestfulJeeFormatter create(
            StringRepresentationDirectory stringRepDir )
    {
        return new NetRestfulJeeFormatter( stringRepDir );
    }
    
    /**
     * Private constructor for subclasses only, use factory method.
     * 
     * @param stringRepDir the StringRepresentationDirectory to use
     */
    protected NetRestfulJeeFormatter(
            StringRepresentationDirectory stringRepDir )
    {
        super( stringRepDir );
    }

    /**
     * Format the start of a Proxy's identifier.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatProxyIdentifierStart(
            PageContext pageContext,
            Proxy       p,
            String      stringRepresentation,
            int         maxLength )
        throws
            StringifierException
    {
        StringRepresentation rep = determineStringRepresentation( stringRepresentation );
        
        String ret = p.toStringRepresentation( rep, null );
        return ret;
    }

    /**
     * Format the end of a Proxy's identifier.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatProxyIdentifierEnd(
            PageContext pageContext,
            Proxy       p,
            String      stringRepresentation )
    {
        return ""; // nothing
    }

    /**
     * Format the start of a Proxy's link.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param title title of the HTML link, if any
     * @param addArguments additional arguments to the URL, if any
     * @param target the HTML target, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatProxyLinkStart(
            PageContext pageContext,
            Proxy       p,
            String      addArguments,
            String      target,
            String      title,
            String      stringRepresentation )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY,      saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY,      saneRequest.getContextPath() );
        pars.put( StringRepresentationParameters.LINK_TARGET_KEY,               target );
        pars.put( StringRepresentationParameters.LINK_TITLE_KEY,                title );
        pars.put( StringRepresentationParameters.HTML_URL_ADDITIONAL_ARGUMENTS, addArguments );

        String ret = p.toStringRepresentationLinkStart( rep, pars );
        return ret;
    }

    /**
     * Format the end of a Proxy's link.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatProxyLinkEnd(
            PageContext pageContext,
            Proxy       p,
            String      stringRepresentation )
        throws
            StringifierException
    {
        SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

        StringRepresentation                 rep  = determineStringRepresentation( stringRepresentation );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY,      saneRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY,      saneRequest.getContextPath() );

        String ret = p.toStringRepresentationLinkEnd( rep, pars );
        return ret;
    }
}
