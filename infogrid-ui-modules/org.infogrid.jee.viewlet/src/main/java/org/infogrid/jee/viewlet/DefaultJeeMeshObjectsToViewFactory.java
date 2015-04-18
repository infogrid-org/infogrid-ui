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

import java.text.ParseException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.jee.taglib.viewlet.IncludeViewletTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.DefaultMeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.model.traversal.TermMissingTraversalTranslatorException;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.util.FactoryCreatedObject;
import org.infogrid.util.FactoryException;
import org.infogrid.util.UnknownSymbolParseException;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneUrl;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;

/**
 * The default factory for MeshObjectsToView objects to be used in a JEE context.
 */
public class DefaultJeeMeshObjectsToViewFactory
        extends
            AbstractObjectInContext
        implements
            JeeMeshObjectsToViewFactory
{
    private static final Log log = Log.getLogInstance( DefaultJeeMeshObjectsToViewFactory.class ); // our own, private logger

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
    public static DefaultJeeMeshObjectsToViewFactory create(
            MeshBaseIdentifier                              defaultMeshBaseIdentifier,
            MeshBaseIdentifierFactory                       meshBaseIdentifierFactory,
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer,
            TraversalTranslator                             translator,
            String                                          relativeContextPath,
            String                                          absoluteContextPath,
            Context                                         c )
    {
        if( meshBaseIdentifierFactory == null ) {
            meshBaseIdentifierFactory = DefaultMeshBaseIdentifierFactory.create();
        }

        return new DefaultJeeMeshObjectsToViewFactory(
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
    protected DefaultJeeMeshObjectsToViewFactory(
            MeshBaseIdentifier                              defaultMeshBaseIdentifier,
            MeshBaseIdentifierFactory                       meshBaseIdentifierFactory,
            MeshBaseNameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer,
            TraversalTranslator                             translator,
            String                                          relativeContextPath,
            String                                          absoluteContextPath,
            Context                                         c )
    {
        super( c );

        theDefaultMeshBaseIdentifier = defaultMeshBaseIdentifier;
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;
        theMeshBaseNameServer        = meshBaseNameServer;
        theTraversalTranslator       = translator;
        theRelativeContextPath       = relativeContextPath;
        theAbsoluteContextPath       = absoluteContextPath;
    }

    /**
     * Factory method. This is equivalent to specifying a null argument.
     *
     * @param key the key information required for object creation, if any
     * @return the created object
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            MeshObject key )
    {
        return obtainFor( key, null, null );
    }

    /**
     * Factory method.
     *
     * @param subject the key information required for object creation, if any
     * @param viewletTypeName any argument-style information required for object creation, if any
     * @return the created object
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            MeshObject subject,
            String     viewletTypeName )
    {
        return obtainFor( subject, viewletTypeName, null );
    }

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @return the created MeshObjectsToView
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            TraversalPath reachedBy )
    {
        return obtainFor( reachedBy, null, null );
    }

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param viewletTypeName the name of the Viewlet type
     * @return the created MeshObjectsToView
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            TraversalPath     reachedBy,
            String            viewletTypeName )
    {
        return obtainFor( reachedBy, viewletTypeName, null );
    }

    /**
     * Create a MeshObjectsToView that corresponds to the request(s) encloded in this SaneRequest.
     *
     * @param request the SaneUrl
     * @return the created MeshObjectsToView
     * @throws FactoryException thrown if the MeshObjectsToView could not be created
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            SaneUrl request )
        throws
            FactoryException
    {
        MeshObject                subject;
        MeshBaseIdentifier        mbIdentifier;
        MeshBase                  mb;
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
            String subjectIdentifierString;

            Matcher m = theUrlPattern.matcher( trailer );
            if( m.matches() ) {
                mbIdentifierString      = m.group( 2 );
                subjectIdentifierString = m.group( 3 );

            } else {
                mbIdentifierString   = null;
                subjectIdentifierString = trailer;
            }
            if( subjectIdentifierString == null ) {
                subjectIdentifierString = "";
            }
            if( mbIdentifierString != null ) {
                mbIdentifier = theMeshBaseIdentifierFactory.guessFromExternalForm( mbIdentifierString );
            } else {
                mbIdentifier = theDefaultMeshBaseIdentifier;
            }

            mb = theMeshBaseNameServer.get( mbIdentifier );
            if( mb == null ) {
                throw new UnknownSymbolParseException( request.getAbsoluteFullUri(), -1, mbIdentifierString );
            }

            MeshObjectIdentifier subjectIdentifier = mb.getMeshObjectIdentifierFactory().guessFromExternalForm( subjectIdentifierString );
            subject                                = mb.accessLocally( subjectIdentifier );

            if( subject == null ) {
                throw new FactoryException( this, new CannotViewException.NoSubject( subjectIdentifier ));
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

        DefaultJeeMeshObjectsToView ret = new DefaultJeeMeshObjectsToView(
                subject,
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
                getContext(),
                request );

        return ret;
    }


    /**
     * Create a MeshObjectsToView that only asks for a subject.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            MeshObject subject,
            SaneUrl    request )
    {
        return obtainFor( subject, null, request );
    }

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type.
     * Repeated here for clarity.
     *
     * @param subject the subject MeshObject
     * @param viewletTypeName the name of the Viewlet type
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            MeshObject subject,
            String     viewletTypeName,
            SaneUrl    request )
    {
        DefaultJeeMeshObjectsToView ret = new DefaultJeeMeshObjectsToView(
                subject,
                viewletTypeName,
                null,
                null,
                null,
                null,
                subject.getMeshBase(),
                null,
                null,
                null,
                theRelativeContextPath,
                getContext(),
                request );

        return ret;
    }

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            TraversalPath reachedBy,
            SaneUrl       request )
    {
        return obtainFor( reachedBy, null, request );
    }

    /**
     * Create a MeshObjectsToView that asks for a subject to be viewed with a named Viewlet type
     * and TraversalPath by which the subject was reached.
     *
     * @param reachedBy the TraversalPath by which the subject (the last MeshObject) was reached
     * @param viewletTypeName the name of the Viewlet type
     * @param request the SaneUrl of the current request
     * @return the created MeshObjectsToView
     */
    public DefaultJeeMeshObjectsToView obtainFor(
            TraversalPath reachedBy,
            String        viewletTypeName,
            SaneUrl       request )
    {
        MeshObject subject = reachedBy.getLastMeshObject();

        DefaultJeeMeshObjectsToView ret = new DefaultJeeMeshObjectsToView(
                subject,
                viewletTypeName,
                null,
                reachedBy,
                null,
                null,
                subject.getMeshBase(),
                null,
                null,
                null,
                theRelativeContextPath,
                getContext(),
                request );

        return ret;
    }

    /**
     * Factors out functionality to determine the value of a URL argument with prefix.
     *
     * @param request the incoming request
     * @param argName the name of the URL argument
     * @param prefix the prefix
     * @return the found value, or NO_ANSWER_STRING
     */
    protected static String determineArgumentForMulti(
            SaneUrl request,
            String  argName,
            String  prefix )
    {
        String    ret    = null;
        String [] values = request.getMultivaluedUrlArgument( argName );

        if( values != null ) {
            for( int i=0 ; i<values.length ; ++i ) {
                if( values[i].startsWith( prefix )) {
                    ret = values[i].substring( prefix.length() );
                    break;
                }
            }
        }
        return ret;
    }

    /**
     * Invoked only by objects that have been created by this Factory, this enables
     * the created objects to indicate to the Factory that they have been updated.
     * Depending on the implementation of the Factory, that may cause a
     * SmartFactory to write changes to disk, for example.
     *
     * @param object the FactoryCreatedObject
     */
    public void factoryCreatedObjectUpdated(
            FactoryCreatedObject<MeshObject,MeshObjectsToView,String> object )
    {
        // do nothing
    }

    /**
     * The TraversalTranslator to use.
     */
    protected TraversalTranslator theTraversalTranslator;

    /**
     * The identifier of the default MeshBase.
     */
    protected MeshBaseIdentifier theDefaultMeshBaseIdentifier;

    /**
     * The factory for MeshBaseIdentifiers.
     */
    protected MeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * The name server with which to look up MeshBases.
     */
    protected MeshBaseNameServer<MeshBaseIdentifier,MeshBase> theMeshBaseNameServer;

    /**
     * The web aplication's (relative) context path.
     */
    protected String theRelativeContextPath;

    /**
     * The web aplication's (absolute) context path.
     */
    protected String theAbsoluteContextPath;

    /**
     * The pattern for URL parsing after the context path.
     */
    protected static final Pattern theUrlPattern = Pattern.compile( "^(\\[meshbase=([^\\]]*)\\])?(.*)$" );
}
