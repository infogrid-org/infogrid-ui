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

package org.infogrid.jee.rest.net;

import java.util.HashMap;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.text.SimpleStringRepresentationContext;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectory;

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
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatProxyIdentifierStart(
            PageContext pageContext,
            Proxy       p,
            String      rootPath,
            String      stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        context = perhapsOverrideStringRepresentationContext( rootPath, context );
        
        String ret = p.toStringRepresentation( rep, context );
        return ret;
    }

    /**
     * Format the end of a Proxy's identifier.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatProxyIdentifierEnd(
            PageContext pageContext,
            Proxy       p,
            String      rootPath,
            String      stringRepresentation )
    {
        return ""; // nothing
    }

    /**
     * Format the start of a Proxy's link.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param addArguments additional arguments to the URL, if any
     * @param target the HTML target, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatProxyLinkStart(
            PageContext pageContext,
            Proxy       p,
            String      rootPath,
            String      addArguments,
            String      target,
            String      stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        context = perhapsOverrideStringRepresentationContext( rootPath, context );

        String ret = p.toStringRepresentationLinkStart( addArguments, target, rep, context );
        return ret;
    }

    /**
     * Format the end of a Proxy's link.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatProxyLinkEnd(
            PageContext pageContext,
            Proxy       p,
            String      rootPath,
            String      stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        context = perhapsOverrideStringRepresentationContext( rootPath, context );

        String ret = p.toStringRepresentationLinkEnd( rep, context );
        return ret;
    }

    /**
     * Helper method to perhaps override a StringRepresentationContext if a
     * non-default rootPath has been given.
     * 
     * @param rootPath alternate root path to use, if any
     * @param candidate the candidate StringRepresentationContext
     * @return the candidate StringRepresentationContext, or an overridden StringRepresentationContext
     */
    protected StringRepresentationContext perhapsOverrideStringRepresentationContext(
            String                      rootPath,
            StringRepresentationContext candidate )
    {
        StringRepresentationContext ret;
        
        if( rootPath == null ) {
            ret = candidate;
        } else {
            HashMap<String,Object> contextObjects = new HashMap<String,Object>();
            contextObjects.put( StringRepresentationContext.WEB_CONTEXT_KEY, rootPath );
            
            ret = SimpleStringRepresentationContext.create( contextObjects, candidate );
        }
        return ret;
    }
}
