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

package org.infogrid.jee.viewlet.module;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import javax.servlet.ServletException;
import org.infogrid.util.context.Context;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.SimpleJeeViewlet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleMeta;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.DateTimeUtil;
import org.infogrid.util.StringHelper;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * A Viewlet to display the content of the ModuleDirectory.
 */
public class ModuleDirectoryViewlet
        extends
            SimpleJeeViewlet
{
    private static final Log log = Log.getLogInstance( ModuleDirectoryViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the MeshObjects are taken
     * @param c the application context
     * @return the created Viewlet
     */
    public static ModuleDirectoryViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        ModuleDirectoryViewlet      ret    = new ModuleDirectoryViewlet( viewed, c );

        viewed.setViewlet( ret );
        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param toView the MeshObjectsToView for which this is a choice
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            JeeMeshObjectsToView toView,
            double               matchQuality )
    {
        return new DefaultJeeViewletFactoryChoice( toView, ModuleDirectoryViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet()
                    throws
                        CannotViewException
                {
                    return create( getMeshObjectsToView().getMeshBase(), getMeshObjectsToView().getContext() );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the JeeViewedMeshObjects to use
     * @param c the application context
     */
    protected ModuleDirectoryViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
    }

    /**
     * Process the incoming request. Default implementation that can be
     * overridden by subclasses.
     *
     * @param request the incoming request
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    @Override
    public void processRequest(
            SaneRequest        request,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        synchronized( this ) {
            if( theCurrentRequest != null ) {
                throw new IllegalStateException( "Have current request already: " + theCurrentRequest );
            }
            theCurrentRequest = request;
        }

        try {
            ModuleRegistry registry = getContext().findContextObjectOrThrow( ModuleRegistry.class );

            String cssClassName = ModuleDirectoryViewlet.class.getName().replace( '.', '-' );
            String cssPathName  = ModuleDirectoryViewlet.class.getName().replace( '.', '/' );

            StringBuilder content = new StringBuilder();
            content.append( "<div class=\"viewlet " ).append( cssClassName ).append( "\">\n" );

            content.append( theResourceHelper.getResourceString( "Title" ));
            
            content.append( "<table class=\"" ).append( cssClassName ).append( "\">\n" );

            content.append( " <thead>\n" );
            content.append( "  <tr>\n" );
            content.append( "   <th>" ).append( theResourceHelper.getResourceString( "ModuleNameLabel"  )).append( "</th>\n" );
            content.append( "   <th>" ).append( theResourceHelper.getResourceString( "ModuleVersionLabel" )).append( "</th>\n" );
            content.append( "   <th>" ).append( theResourceHelper.getResourceString( "ModuleBuildDateLabel" )).append( "</th>\n" );
            content.append( "   <th>" ).append( theResourceHelper.getResourceString( "ModuleResolvedLabel" )).append( "</th>\n" );
            content.append( "  </tr>\n" );
            content.append( " </thead>\n" );

            content.append( " <tbody>\n" );

            for( String moduleName : registry.nameSet() ) {
                ModuleMeta [] moduleMetas  = registry.getModuleMetasFor( moduleName );
                for( int i=0 ; i<moduleMetas.length ; ++i ) {
                    ModuleMeta currentMeta = moduleMetas[i];

                    content.append( "  <tr>\n" );
                    if( moduleMetas.length > 1 ) {
                        if( i==0 ) {
                            content.append( "  <td rowspan=\"" ).append( moduleMetas.length ).append( "\">\n" );
                            content.append( StringHelper.stringToHtml( moduleName ));
                            content.append( "  </td>\n" );
                        }
                    } else {
                        content.append( "  <td>\n" );
                        content.append( StringHelper.stringToHtml( moduleName ));
                        content.append( "  </td>\n" );
                    }
                    Module currentMod = registry.getLocalResolution( currentMeta );

                    content.append( "  <td>\n" );
                    if( currentMeta.getModuleVersion() != null ) {
                        content.append( StringHelper.stringToHtml( currentMeta.getModuleVersion() ));
                    } else {
                        content.append( theResourceHelper.getResourceString( "NoValue" ));
                    }
                    content.append( "  </td>\n" );
                    
                    content.append( "  <td>\n" );
                    if( currentMeta.getModuleBuildDate() > 0 ) {
                        content.append( DateTimeUtil.dateToRfc3339( new Date( currentMeta.getModuleBuildDate() )));
                    } else {
                        content.append( theResourceHelper.getResourceString( "NoValue" ));
                    }
                    content.append( "  </td>\n" );

                    content.append( "  <td>\n" );
                    if( currentMod != null ) {
                        content.append( theResourceHelper.getResourceString( "Resolved" ));
                    } else {
                        content.append( theResourceHelper.getResourceString( "NotResolved" ));
                    }
                    content.append( "  </td>\n" );

                    content.append( "  </tr>" );
                }
            }

            content.append( " </tbody>\n" );
            content.append( "</table>\n" );
            content.append( "</div>\n" );

            structured.getDefaultTextSection().setMimeType( "text/html" );
            structured.getDefaultTextSection().appendContent( content.toString() );


            StringBuilder css = new StringBuilder();
            css.append( "<link rel=\"stylesheet\" href=\"" );
            css.append( request.getContextPath() );
            css.append( "/v/" );
            css.append( cssPathName );
            css.append( ".css\" />" );

            structured.obtainTextSectionByName( StructuredResponse.HTML_HEAD_SECTION.getSectionName() ).appendContent( css.toString() );

        } finally {
            synchronized( this ) {
                theCurrentRequest = null;
            }
        }
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( ModuleDirectoryViewlet.class );
}
