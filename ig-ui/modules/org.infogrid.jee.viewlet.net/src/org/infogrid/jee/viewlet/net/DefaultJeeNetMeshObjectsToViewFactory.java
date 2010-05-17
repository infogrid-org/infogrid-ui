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

package org.infogrid.jee.viewlet.net;

import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.jee.taglib.viewlet.IncludeViewletTag;
import org.infogrid.jee.viewlet.DefaultJeeMeshObjectsToViewFactory;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateEnum;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateTransitionEnum;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewletState;
import org.infogrid.jee.viewlet.JeeViewletStateTransition;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.model.traversal.TermMissingTraversalTranslatorException;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.util.FactoryException;
import org.infogrid.util.UnknownSymbolParseException;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneUrl;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;

/**
 *
 * There isn't much of a point to subclass DefaultJeeMeshObjectsToViewFactory, so we won't.
 */
public class DefaultJeeNetMeshObjectsToViewFactory
        extends
            DefaultJeeMeshObjectsToViewFactory
{
    private static final Log log = Log.getLogInstance( DefaultJeeNetMeshObjectsToViewFactory.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @param meshBaseIdentifierFactory the factory to use to create MeshBaseIdentifiers
     * @param meshBaseNameServer the name server with which to look up MeshBases
     * @param translator the TraversalTranslator to use
     * @param relativeContextPath the web application's (relative) context path
     * @param absoluteContextPath the web application's (absolute) context path
     * @param c the Context
     * @return the created DefaultJeeMeshObjectsToViewFactory
     */
    public static DefaultJeeNetMeshObjectsToViewFactory create(
            NetMeshBaseIdentifier                           defaultMeshBaseIdentifier,
            NetMeshBaseIdentifierFactory                    meshBaseIdentifierFactory,
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer,
            TraversalTranslator                             translator,
            String                                          relativeContextPath,
            String                                          absoluteContextPath,
            Context                                         c )
    {
        if( meshBaseIdentifierFactory == null ) {
            meshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create();
        }

        return new DefaultJeeNetMeshObjectsToViewFactory(
                defaultMeshBaseIdentifier,
                meshBaseIdentifierFactory,
                meshBaseNameServer,
                translator,
                relativeContextPath,
                absoluteContextPath,
                c );
    }

    /**
     * Constructor.
     *
     * @param defaultMeshBaseIdentifier the identifier, in String form, of the default MeshBase
     * @param meshBaseIdentifierFactory the factory to use to create MeshBaseIdentifiers
     * @param meshBaseNameServer the name server with which to look up MeshBases
     * @param translator the TraversalTranslator to use
     * @param relativeContextPath the web application's (relative) context path
     * @param absoluteContextPath the web application's (absolute) context path
     * @param c the Context
     */
    protected DefaultJeeNetMeshObjectsToViewFactory(
            MeshBaseIdentifier                              defaultMeshBaseIdentifier,
            MeshBaseIdentifierFactory                       meshBaseIdentifierFactory,
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer,
            TraversalTranslator                             translator,
            String                                          relativeContextPath,
            String                                          absoluteContextPath,
            Context                                         c )
    {
        super(  defaultMeshBaseIdentifier,
                meshBaseIdentifierFactory,
                meshBaseNameServer,
                translator,
                relativeContextPath,
                absoluteContextPath,
                c );
    }

    /**
     * Factory method. This is equivalent to specifying a null argument.
     *
     * @param key the key information required for object creation, if any
     * @return the created object
     */
    @Override
    public DefaultJeeNetMeshObjectsToView obtainFor(
            MeshObject key )
    {
        return (DefaultJeeNetMeshObjectsToView) super.obtainFor( key );
    }

    /**
     * Factory method.
     *
     * @param subject the key information required for object creation, if any
     * @param viewletTypeName any argument-style information required for object creation, if any
     * @return the created object
     */
    @Override
    public DefaultJeeNetMeshObjectsToView obtainFor(
            MeshObject subject,
            String     viewletTypeName )
    {
        DefaultJeeNetMeshObjectsToView ret = new DefaultJeeNetMeshObjectsToView(
                (NetMeshObject) subject,
                null,
                viewletTypeName,
                null,
                null,
                null,
                null,
                (NetMeshBase) subject.getMeshBase(),
                null,
                null,
                null,
                theRelativeContextPath,
                getContext() );

        return ret;
    }

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @return the created MeshObjectsToView
     */
    @Override
    public DefaultJeeNetMeshObjectsToView obtainFor(
            TraversalPath reachedBy )
    {
        return (DefaultJeeNetMeshObjectsToView) super.obtainFor( reachedBy );
    }

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param viewletTypeName the name of the Viewlet type
     * @return the created MeshObjectsToView
     */
    @Override
    public DefaultJeeNetMeshObjectsToView obtainFor(
            TraversalPath     reachedBy,
            String            viewletTypeName )
    {
        MeshObject subject = reachedBy.getLastMeshObject();

        DefaultJeeNetMeshObjectsToView ret = new DefaultJeeNetMeshObjectsToView(
                (NetMeshObject) subject,
                null,
                viewletTypeName,
                null,
                reachedBy,
                null,
                null,
                (NetMeshBase) subject.getMeshBase(),
                null,
                null,
                null,
                theRelativeContextPath,
                getContext() );

        return ret;
    }

    /**
     * Create a MeshObjectsToView that corresponds to the request(s) encloded in this SaneRequest.
     *
     * @param request the SaneUrl
     * @return the created MeshObjectsToView
     * @throws FactoryException thrown if the MeshObjectsToView could not be created
     */
    @Override
    public MeshObjectsToView obtainFor(
            SaneUrl request )
        throws
            FactoryException
    {
        NetMeshObject             subject;
        NetMeshBaseIdentifier     mbIdentifier;
        NetMeshBase               mb;
        NetMeshBaseIdentifier     requestedProxyIdentifier;
        Proxy                     requestedProxy;
        TraversalPath             path;
        TraversalSpecification    spec;
        String                    viewletTypeName;
        String                    mimeType;
        TraversalPathSet          paths;
        JeeViewletState           viewletState;
        JeeViewletStateTransition viewletTransition;
        HashMap<String,Object[]>  viewletPars;

        try {
            // Subject and MeshBase
            String relativeBaseUrl = request.getRelativeBaseUri();
            if( !relativeBaseUrl.startsWith( request.getContextPath() )) {
                throw new IllegalArgumentException( "Cannot process incoming relative URI " + relativeBaseUrl + " that is outside of context path " + request.getContextPath() );
            }

            String trailer = relativeBaseUrl.substring( request.getContextPath().length() );
            if( trailer.startsWith( "/" )) {
                trailer = trailer.substring( 1 );
            }
            trailer = HTTP.decodeUrl( trailer );

            String mbIdentifierString;
            String proxyIdentifierString;
            String subjectIdentifierString;

            Matcher m = theUrlNetPattern.matcher( trailer );
            if( m.matches() ) {
                mbIdentifierString      = m.group( 2 );
                proxyIdentifierString   = m.group( 4 );
                subjectIdentifierString = m.group( 5 );

            } else {
                mbIdentifierString      = null;
                proxyIdentifierString   = null;
                subjectIdentifierString = trailer;
            }
            if( subjectIdentifierString == null ) {
                subjectIdentifierString = "";
            }
            if( mbIdentifierString != null ) {
                mbIdentifier = ((NetMeshBaseIdentifierFactory)theMeshBaseIdentifierFactory).guessFromExternalForm( mbIdentifierString );
            } else {
                mbIdentifier = (NetMeshBaseIdentifier) theDefaultMeshBaseIdentifier;
            }

            mb = (NetMeshBase) theMeshBaseNameServer.get( mbIdentifier );
            if( mb == null ) {
                throw new UnknownSymbolParseException( request.getAbsoluteFullUri(), -1, mbIdentifierString );
            }

            NetMeshObjectIdentifier subjectIdentifier = mb.getMeshObjectIdentifierFactory().fromExternalForm( subjectIdentifierString );
            subject                                   = mb.accessLocally( subjectIdentifier );

            if( subject == null ) {
                throw new FactoryException( this, new CannotViewException.NoSubject( subjectIdentifier ));
            }
            if( proxyIdentifierString != null ) {
                requestedProxyIdentifier = (NetMeshBaseIdentifier) theMeshBaseIdentifierFactory.guessFromExternalForm( proxyIdentifierString );
                requestedProxy           = mb.getProxyFor( requestedProxyIdentifier );
            } else {
                requestedProxyIdentifier = null;
                requestedProxy           = null;
            }

            // Viewlet type
            viewletTypeName = determineArgumentForMulti( request, JeeMeshObjectsToView.LID_FORMAT_ARGUMENT_NAME, JeeMeshObjectsToView.VIEWLET_PREFIX );

            // MIME type
            mimeType = determineArgumentForMulti( request, JeeMeshObjectsToView.LID_FORMAT_ARGUMENT_NAME, JeeMeshObjectsToView.MIME_PREFIX );

            // traversal
            String [] traversalTerms = request.getMultivaluedUrlArgument( JeeMeshObjectsToView.LID_TRAVERSAL_ARGUMENT_NAME );
            path = null; // default
            spec = null;
            if( theTraversalTranslator != null && traversalTerms != null && traversalTerms.length > 0 ) {
                try {
                    path = theTraversalTranslator.translateTraversalPath( subject, traversalTerms );

                } catch( TermMissingTraversalTranslatorException ex ) {
                    // only a TraversalSpec given, not a TraversalPath
                } catch( TraversalTranslatorException ex ) {
                    log.error( ex );
                }
                if( path != null ) {
                    spec = path.getTraversalSpecification();
                } else {
                    try {
                        spec = theTraversalTranslator.translateTraversalSpecification( subject, traversalTerms );

                    } catch( TraversalTranslatorException ex ) {
                        log.error( ex );
                    }
                }
            }

            if( path != null ) {
                paths = mb.getMeshObjectSetFactory().createSingleMemberImmutableTraversalPathSet( path );
            } else if( spec != null ) {
                paths = mb.getMeshObjectSetFactory().createImmutableTraversalPathSet( subject, spec );
            } else {
                paths = null;
            }

            // ViewletState
            viewletState = DefaultJeeViewletStateEnum.fromRequest( request );

            // ViewletStateTransition
            viewletTransition = DefaultJeeViewletStateTransitionEnum.fromRequest( request );

            // remaining arguments (but not lid-include) are viewletPars
            viewletPars = null;
            for( String key : request.getUrlArguments().keySet() ) {
                if( JeeMeshObjectsToView.LID_FORMAT_ARGUMENT_NAME.equals( key )) {
                    continue;
                }
                if( JeeMeshObjectsToView.LID_TRAVERSAL_ARGUMENT_NAME.equals( key )) {
                    continue;
                }
                if( JeeViewletState.VIEWLET_STATE_PAR_NAME.equals( key )) {
                    continue;
                }
                if( JeeViewletStateTransition.VIEWLET_STATE_TRANSITION_PAR_NAME.equals( key )) {
                    continue;
                }
                if( IncludeViewletTag.INCLUDE_URL_ARGUMENT_NAME.equals( key )) {
                    continue;
                }
                String [] values = request.getMultivaluedUrlArgument( key );
                if( viewletPars == null ) {
                    viewletPars = new HashMap<String,Object[]>();
                }
                viewletPars.put( key, values );
            }
        } catch( ParseException ex ) {
            throw new FactoryException( this, ex );
        } catch( MeshObjectAccessException ex ) {
            throw new FactoryException( this, ex );
        } catch( NotPermittedException ex ) {
            throw new FactoryException( this, ex );
        }

        DefaultJeeNetMeshObjectsToView ret = new DefaultJeeNetMeshObjectsToView(
                subject,
                requestedProxy,
                viewletTypeName,
                viewletPars,
                null, // we didn't arrive here by any particular path
                spec,
                paths,
                subject.getMeshBase(),
                viewletState,
                viewletTransition,
                mimeType,
                theRelativeContextPath,
                getContext() );

        return ret;
    }

    /**
     * The pattern for URL parsing after the context path.
     */
    protected static final Pattern theUrlNetPattern = Pattern.compile( "^(\\[meshbase=([^\\]]*)\\])?(\\[proxy=([^\\]]*)\\])?(.*)$" );
}
