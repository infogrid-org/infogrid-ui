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

package org.infogrid.module.servlet;

import org.infogrid.module.ModelModuleAdvertisement;
import org.infogrid.module.ModuleAdvertisement;
import org.infogrid.module.StandardModuleAdvertisement;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.Iterator;

/**
 * Servlet that displays the content of a ModuleRegistry.
 */
public class ServletModuleRegistryServlet
        extends
            HttpServlet
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ServletModuleRegistryServlet()
    {
        //  no op
    }
    
    /**
     * Respond to a GET request.
     *
     * @param request the incoming request
     * @param response the outgoing response
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public void doGet(
            HttpServletRequest  request,
            HttpServletResponse response )
        throws
            IOException
    {
        ServletModuleRegistry registry = ServletBootLoader.getModuleRegistry();
        
        response.setContentType( "text/html" );
        PrintWriter w = response.getWriter();
        
        w.println( "<html>" );
        w.println( " <head>" );
        w.println( "  <title>This InfoGrid application's ModuleRegistry</title>" );
        w.println( " <style>" );
        w.println( "* { font-family: Tahoma Arial sans-serif; }" );
        w.println( "span.module-name:before { content: \"Name:&nbsp;\" ; }" ); 
        w.println( "span.module-version:before { content: \"(Version:&nbsp;\" ; }" ); 
        w.println( "span.module-version:after { content: \") ; }" ); 
        w.println( " </style>" );
        w.println( " </head>" );
        w.println( " <body>" );
        w.println( "  <h1>ModuleRegistry</h1>" );

        w.println( "  <h2>ModelModuleAdvertisement</h2>" );
        Iterator<ModelModuleAdvertisement> modelModuleIter = registry.modelAdvertisementIterator();
        if( modelModuleIter.hasNext() ) {
            w.println( "   <ul>" );
            while( modelModuleIter.hasNext() ) {
                printOne( modelModuleIter.next(), w );
            }
            w.println( "   </ul>" );        
        } else {
            w.println( "<p>No ModelModules.</p>" );
        }

        w.println( "  <h2>StandardModuleAdvertisement</h2>" );
        Iterator<StandardModuleAdvertisement> standardModuleIter = registry.standardAdvertisementIterator();
        if( standardModuleIter.hasNext() ) {
            w.println( "   <ul>" );

            while( standardModuleIter.hasNext() ) {
                printOne( standardModuleIter.next(), w );
            }
            w.println( "   </ul>" );
        } else {
            w.println( "<p>No StandardModules.</p>" );
        }

        w.println( " </body>" );
        w.println( "</html>" );
    }
    
    /**
     * Print a single ModuleAdvertisement.
     * 
     * @param adv the ModuleAdvertisement to print
     * @param w the PrintWriter to print to
     * @throws IOException thrown if an I/O error occurred
     */
    protected void printOne(
            ModuleAdvertisement adv,
            PrintWriter         w )
        throws
            IOException
    {
        w.println( "    <li>" );
        w.print( "     <span class=\"module-name\">" );
        w.print( adv.getModuleName() );
        w.print( "/span>" );

        if( adv.getModuleVersion() != null ) {
            w.print( "<span class=\"module-version\">" );
            w.print( adv.getModuleVersion() );
            w.print( "/span>" );
        }                
        if( adv.getModuleBuildDate() != null ) {
            w.print( "<span class=\"module-buildtime\">" );
            w.print( adv.getModuleBuildDate() );
            w.print( "/span>" );
        }                
        w.println( "    </li>" );
    }
}
