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

package org.infogrid.jee.viewlet;

import java.util.Deque;
import java.util.Map;
import org.infogrid.jee.taglib.viewlet.IncludeViewletTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.mesh.text.MeshStringRepresentationParameters;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;
import org.infogrid.viewlet.AbstractMeshObjectsToView;

/**
 * Augments AbstractMeshObjectsToView for JEE Viewlets.
 */
public abstract class AbstractJeeMeshObjectsToView
        extends
            AbstractMeshObjectsToView
        implements
            JeeMeshObjectsToView
{
    private static final Log log = Log.getLogInstance( AbstractJeeMeshObjectsToView.class ); // our own, private logger

    /**
     * Private constructor, use factory method.
     *
     * @param subject the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param arrivedAtPath the TraversalPath by which we arrived here, if any
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     * @param traversalPaths the TraversalPaths to the highlighted objects
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param state the ViewletState to to assume
     * @param transition the ViewletStateTransition to make
     * @param mimeType the requested MIME type, if any
     * @param contextPath the context path of the web application
     * @param c the Context
     */
    protected AbstractJeeMeshObjectsToView(
            MeshObject                subject,
            String                    viewletTypeName,
            Map<String,Object[]>      viewletParameters,
            TraversalPath             arrivedAtPath,
            TraversalSpecification    traversalSpecification,
            TraversalPathSet          traversalPaths,
            MeshBase                  mb,
            JeeViewletState           state,
            JeeViewletStateTransition transition,
            String                    mimeType,
            String                    contextPath,
            Context                   c )
    {
        super(  subject,
                viewletTypeName,
                viewletParameters,
                arrivedAtPath,
                traversalSpecification,
                traversalPaths,
                mb,
                c );

        theState         = state;
        theTransition    = transition;
        theMimeType      = mimeType;
        theContextPath   = contextPath;
    }

    /**
     * Obtain the desired JeeViewletState.
     *
     * @return the desired state
     */
    public JeeViewletState getViewletState()
    {
        return theState;
    }

    /**
     * Set the desired JeeViewletState.
     *
     * @param newValue the new value
     */
    public void setViewletState(
            JeeViewletState newValue )
    {
        theState = newValue;
    }

    /**
     * Obtain the desired JeeViewletStateTransition.
     *
     * @return the desired transition
     */
    public JeeViewletStateTransition getViewletStateTransition()
    {
        return theTransition;
    }

    /**
     * The desired JeeViewletStateTransition.
     *
     * @param newValue the new value
     */
    public void setViewletStateTransition(
            JeeViewletStateTransition newValue )
    {
        theTransition = newValue;
    }

    /**
     * Obtain the requested MIME type.
     *
     * @return the MIME type
     */
    public String getMimeType()
    {
        return theMimeType;
    }

    /**
     * Set the requested MIME type.
     *
     * @param newValue the new value
     */
    public void setMimeType(
            String newValue )
    {
        theMimeType = newValue;
    }

    /**
     * Obtain the context path of the web application.
     *
     * @return the incoming request
     */
    public String getContextPath()
    {
        return theContextPath;
    }

    /**
     * Obtain this MeshObjectsToView as a URL, i.e. obtain the URL that, when entered into a browser,
     * will recreate this MeshObjectsToView and render it.
     * If given, use the childArrivedAt argument instead of our current TraversalPath.
     *
     * @param childArrivedAt the TraversalPath, if any, to reach any child of this
     * @return the URL
     */
    public String getAsUrl(
            TraversalPath childArrivedAt )
    {
        Context c = getContext();

        StringRepresentationDirectory srepdir         = c.findContextObject( StringRepresentationDirectory.class );
        MeshBase                      defaultMeshBase = c.findContextObject( MeshBase.class );
        TraversalTranslator           translator      = c.findContextObject( TraversalTranslator.class );

        StringRepresentation                 rep  = srepdir.get( "Url" );
        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( MeshStringRepresentationParameters.DEFAULT_MESHBASE_KEY,      defaultMeshBase );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY,      theContextPath );

        // first the MeshObject
        StringBuilder buf = new StringBuilder();
        try {
            buf.append( theSubject.toStringRepresentationLinkStart( rep, pars ));

        } catch( StringifierException ex ) {
            log.error( ex );
        }

        // Viewlet type
        if( theViewletTypeName != null ) {
            HTTP.appendArgumentToUrl( buf, JeeMeshObjectsToView.LID_FORMAT_ARGUMENT_NAME, JeeMeshObjectsToView.VIEWLET_PREFIX + theViewletTypeName );
        }

        // the traversal
        try {
            String [] traversalArgs = null;
            if( childArrivedAt != null ) {
                traversalArgs = translator.translateTraversalPath( theSubject, childArrivedAt );
            } else if( theTraversalSpecification != null ) {
                traversalArgs = translator.translateTraversalSpecification( theSubject, theTraversalSpecification );
            }
            if( traversalArgs != null && traversalArgs.length > 0 ) {
                for( int i=0 ; i<traversalArgs.length ; ++i ) {
                    HTTP.appendArgumentToUrl( buf, JeeMeshObjectsToView.LID_TRAVERSAL_ARGUMENT_NAME, traversalArgs[i] );
                }
            }
        } catch( TraversalTranslatorException ex ) {
            log.error( ex );
        }

        // the ViewletState
        if( theState != null && !theState.isDefaultState()) {
            HTTP.appendArgumentToUrl( buf, JeeViewletState.VIEWLET_STATE_PAR_NAME, theState.getName() );
        }

        // the ViewletStateTransition
        if( theTransition != null ) {
            HTTP.appendArgumentToUrl( buf, JeeViewletStateTransition.VIEWLET_STATE_TRANSITION_PAR_NAME, theTransition.getName() );
        }

        // the Viewlet parameters
        if( theViewletParameters != null ) {
            for( String key : theViewletParameters.keySet() ) {
                Object [] values = theViewletParameters.get( key );
                for( int i=0 ; i<values.length ; ++i ) {
                    HTTP.appendArgumentToUrl( buf, key, values[i].toString() );
                }
            }
        }

        return buf.toString();
    }

    /**
     * Obtain this MeshObjectsToView as a URL, i.e. obtain the URL that, when entered into a browser,
     * will recreated this MeshObjectsToView and render it as a child of the JeeViewedMeshObjects
     * given.
     *
     * @param viewedMeshObjectsStack the Stack of ViewedMeshObjects of the parent Viewlets.
     * @return the URL
     */
    public String getAsUrl(
            Deque<JeeViewedMeshObjects> viewedMeshObjectsStack )
    {
        String toViewUrl = getAsUrl( (TraversalPath) null );

        if( viewedMeshObjectsStack != null ) {
            MeshObject currentSubject = getSubject();
            
            for( JeeViewedMeshObjects current : viewedMeshObjectsStack ) {
                // look for the TraversalPath to the currentSubject
                TraversalPath found = null;
                for( TraversalPath current2 : current.getTraversalPathSet() ) {
                    if( current2.getLastMeshObject() == currentSubject ) {
                        found = current2;
                        break;
                    }
                }
                String currentUrl = current.getAsUrl( found );

                toViewUrl = HTTP.appendArgumentToUrl( currentUrl, IncludeViewletTag.INCLUDE_URL_ARGUMENT_NAME, toViewUrl );

                currentSubject = current.getSubject();
            }
        }
        return toViewUrl;
    }

    /**
     * The request MIME type, if any.
     */
    protected String theMimeType;

    /**
     * The desired viewlet state, if any.
     */
    protected JeeViewletState theState;

    /**
     * The desired viewlet state transition, if any.
     */
    protected JeeViewletStateTransition theTransition;

    /**
     * The context path of the web application.
     */
    protected String theContextPath;
}
