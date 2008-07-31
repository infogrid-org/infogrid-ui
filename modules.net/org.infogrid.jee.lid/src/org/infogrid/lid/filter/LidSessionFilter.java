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

package org.infogrid.lid.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.lid.LidCookies;
import org.infogrid.lid.LidSession;
import org.infogrid.lid.LidSessionManager;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.security.ThreadIdentityManager;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * A JEE Filter that manages the LID cookies.
 */
public class LidSessionFilter
        implements
            Filter
{
    private static final Log log = Log.getLogInstance( LidSessionFilter.class ); // our own, private logger

    /**
     * Main filter method.
     * 
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @param chain The filter chain we are processing
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        SaneRequest saneRequest = (SaneRequest) request.getAttribute( SaneRequest.class.getName() );

        String lid     = saneRequest.getCookieValue( LidCookies.LID_IDENTIFIER_COOKIE_NAME );
        String session = saneRequest.getCookieValue( LidCookies.LID_SESSION_COOKIE_NAME );
        
        boolean setCaller = false;

        try {
            LidSession userSession = theSessionManager.get( lid );
            
            if( userSession != null && userSession.isStillValid() ) {

                try {
                    NetMeshObjectIdentifier callerId = theNetMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( lid );

                    NetMeshObject caller = theNetMeshBase.accessLocally( callerId );
                    
                    ThreadIdentityManager.setCaller( caller );
                    
                    setCaller = true;

                } catch( Throwable t ) {
                    log.error( t );
                }

            }        
            chain.doFilter( request, response );
            
        } finally {
            if( setCaller ) {
                ThreadIdentityManager.unsetCaller();
            }
        }
    }

    /**
     * Destroy method for this filter.
     */
    public void destroy()
    {
        // nothing
    }

    /**
     * Initialization method for this filter.
     * 
     * @param filterConfig the Filter configuration
     */
    public void init(
            FilterConfig filterConfig )
    {
        theFilterConfig = filterConfig;
        
        InfoGridWebApp app = InfoGridWebApp.getSingleton();
        
        theSessionManager = app.getApplicationContext().findContextObjectOrThrow( LidSessionManager.class );
        theNetMeshBase    = app.getApplicationContext().findContextObjectOrThrow( NetMeshBase.class );
    }

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;
    
    /**
     * The session manager to use.
     */
    protected LidSessionManager theSessionManager;
    
    /**
     * The NetMeshBase to use.
     */
    protected NetMeshBase theNetMeshBase;
}
