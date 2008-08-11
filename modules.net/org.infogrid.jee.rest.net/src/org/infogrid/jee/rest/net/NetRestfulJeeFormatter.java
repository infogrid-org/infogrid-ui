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

package org.infogrid.jee.rest.net;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.text.StringRepresentation;

/**
 * Collection of utility methods that are useful with InfoGrid JEE applications
 * and aware of InfoGrid REST conventions and the Net implementation.
 */
public class NetRestfulJeeFormatter
        extends
            RestfulJeeFormatter
{
    /**
     * Private useless constructor to keep JavaDoc straight.
     */
    public NetRestfulJeeFormatter()
    {
        // nothing
    }

    /**
     * Format the start of a Proxy's identifier.
     *
     * @param pageContext the PageContext object for this page
     * @param p the Proxy whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatProxyIdentifierStart(
            PageContext pageContext,
            Proxy       p,
            String      stringRepresentation )
    {
        StringRepresentation rep = determineStringRepresentation( stringRepresentation );

        boolean isDefaultMeshBase = isDefaultMeshBase( p.getNetMeshBase() );

        String ret = p.toStringRepresentation( rep, isDefaultMeshBase );
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
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatProxyLinkStart(
            PageContext pageContext,
            Proxy       p,
            String      rootPath,
            String      stringRepresentation )
    {
        StringRepresentation rep = determineStringRepresentation( stringRepresentation );

        String contextPath;
        if( rootPath != null ) {
            contextPath = rootPath;
        } else {
            contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath() + "/";
        }

        boolean isDefaultMeshBase = isDefaultMeshBase( p.getNetMeshBase() );

        String ret = p.toStringRepresentationLinkStart( rep, contextPath, isDefaultMeshBase );
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
        StringRepresentation rep = determineStringRepresentation( stringRepresentation );

        String contextPath;
        if( rootPath != null ) {
            contextPath = rootPath;
        } else {
            contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath() + "/";
        }

        boolean contextImpliesThisMeshBase = isDefaultMeshBase( p.getNetMeshBase() );

        String ret = p.toStringRepresentationLinkEnd( rep, contextPath, contextImpliesThisMeshBase );
        return ret;
    }
}
