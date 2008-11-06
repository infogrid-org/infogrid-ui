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

package org.infogrid.probe;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotBlessedException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.IterableNetMeshBase;
import org.infogrid.meshbase.net.IterableNetMeshBaseDifferencer;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Probe.ProbeSubjectArea;
import org.infogrid.model.Probe.ProbeUpdateSpecification;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleCapability;
import org.infogrid.module.ModuleException;
import org.infogrid.module.ModuleNotFoundException;
import org.infogrid.module.ModuleRegistry;
import org.infogrid.module.ModuleResolutionException;
import org.infogrid.module.StandardModuleAdvertisement;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.probe.shadow.ShadowMeshBaseEvent;
import org.infogrid.probe.shadow.ShadowMeshBaseListener;
import org.infogrid.probe.shadow.m.MStagingMeshBase;
import org.infogrid.probe.xml.DomMeshObjectSetProbe;
import org.infogrid.probe.xml.MeshObjectSetProbeTags;
import org.infogrid.probe.xml.XmlDOMProbe;
import org.infogrid.probe.xml.XmlErrorHandler;
import org.infogrid.probe.xml.XmlProbeException;
import org.infogrid.probe.yadis.YadisServiceFactory;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.FlexibleListenerSet;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;

/**
 * Performs most of the work of ShadowMeshBases all the way to invoking the right Probes.
 * This is a separate class in order to make it easier to reuse behavior for multiple
 * implementations of ShadowMeshBase.
 */
public class ProbeDispatcher
{
    private static final Log log = Log.getLogInstance( ProbeDispatcher.class ); // our own, private logger

    /**
     * Constructor.
     * 
     * @param meshBase the ShadowMeshBase on whose behalf the ProbeDispatcher works
     * @param directory the ProbeDirectory to use
     * @param timeCreated the time the home object is supposed to be created
     * @param timeNotNeededTillExpires the time until the Probe gets removed after it was detected that the Probe was not needed
     * @param registry the ModuleRegistry to use for Probe discovery
     */
    public ProbeDispatcher(
            ShadowMeshBase        meshBase,
            ProbeDirectory        directory,
            long                  timeCreated,
            long                  timeNotNeededTillExpires,
            ModuleRegistry        registry )
    {
        theShadowMeshBase           = meshBase;
        theProbeDirectory           = directory;
        theTimeCreated              = timeCreated;
        theTimeNotNeededTillExpires = timeNotNeededTillExpires;
        theModuleRegistry           = registry;
    }
    
    /**
     * Calling this will trigger the Probe to run.
     *
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @return the computed result is the number of milliseconds until the next desired invocation, or -1 if never
     * @throws ProbeException thrown if unable to compute a result
     */
    public synchronized long doUpdateNow(
            CoherenceSpecification coherence )
        throws
            ProbeException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".doUpdateNow( " + coherence + " )" );
        }

        Throwable             problem          = null; // this helps us to know what happened in the finally clause
        boolean               updated          = true; // default is "we have been updated
        StagingMeshBase       oldBase          = null;
        StagingMeshBase       newBase          = null;
        long                  ret              = -1L;
        boolean               isFirstRun       = theShadowMeshBase.size() == 0;
        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();
        ProbeResult           probeResult      = null;

        theCurrentUpdate = System.currentTimeMillis();
        
        if( !isFirstRun ) {
            fireUpdateStarted();
        }

        try {
            // Determine which kind of Probe to run
            
            boolean isDirectory = false;
            boolean isStream    = false;
            // there is no "isApi" because it is the default if none of the others is set

            if( isDirectory( sourceIdentifier )) {
                isDirectory = true;

            } else if( isStreamProtocol( sourceIdentifier )) {
                isStream = true;

            } // else it is an API probe, the default


            // Pick right MeshBase -- first run is different from all others

            if( isFirstRun ) {
                oldBase = null;
                newBase = theShadowMeshBase;
            } else {
                oldBase = theShadowMeshBase;
                newBase = MStagingMeshBase.create( theShadowMeshBase );
            }
            if( !isFirstRun && coherence == null ) {
                // use the old coherence
                coherence = determineCoherenceFromMeshObject( theShadowMeshBase.getHomeObject() );
            }

            Transaction tx = null;
            try {
                // Run the Probe
                tx = newBase.createTransactionAsap();

                newBase.initializeHomeObject( theCurrentUpdate );
                
                if( isDirectory ) {
                    probeResult = handleDirectory( oldBase, newBase, coherence );
                } else if( isStream ) {
                    probeResult = handleStream( oldBase, newBase, coherence );
                } else {
                    probeResult = handleApi( oldBase, newBase, coherence );
                }
                
                updated = probeResult.getUpdated();
                
                if( newBase != theShadowMeshBase ) {
                    copyProbeUpdateSpecification( theShadowMeshBase.getHomeObject(), newBase.getHomeObject() );
                    // otherwise we lose what was there previously
                }

            } catch( IOException ex ) {
                problem = ex;
                throw new ProbeException.IO( sourceIdentifier, ex );

            } catch( TransactionException ex ) {
                problem = ex;
                throw new ProbeException.Other( sourceIdentifier, ex ); // should never happen

            } catch( URISyntaxException ex ) {
                problem = ex;
                throw new ProbeException.SyntaxError( sourceIdentifier, ex );

            } catch( RuntimeException ex ) {
                problem = ex;
                throw new ProbeException.Other( sourceIdentifier, ex );

            } catch( ProbeException ex ) {
                problem = ex;
                throw ex;

            } finally {
                // this is a long finally block, so we enclose everything in a try/catch and put the commit into the final finally
                
                Transaction tx2 = null;
                try {
                    if( problem == null && !isFirstRun && updated ) {

                        // get all the locks back
                        theUpdateInProgress = true;
                        
                        forceLockRecovery( theShadowMeshBase );

                        IterableNetMeshBaseDifferencer diff = new IterableNetMeshBaseDifferencer( theShadowMeshBase );

                        if( newBase != theShadowMeshBase && tx2 == null ) {
                            tx2 = theShadowMeshBase.createTransactionAsap();
                        }

                        ChangeSet changeSet = diff.determineChangeSet( newBase );

                        updated = !changeSet.isEmpty();
                        if( updated ) {
                            diff.applyChangeSet( changeSet );
                        }
                    }
                    long tookTime = System.currentTimeMillis() - theCurrentUpdate;

                    // Deal with the update policy. We apply this to theShadow rather than base, because that way
                    // we can do it after the Differencer has run, and correctly invoke whether or not there
                    // were changes.
                    
                    MeshObject home = theShadowMeshBase.getHomeObject();
                    if( !home.isBlessedBy( ProbeSubjectArea.PROBEUPDATESPECIFICATION )) {
                        // Probe did not do it, so we do it
                        if( coherence == null ) {
                            // default
                            coherence = CoherenceSpecification.getDefault();
                        }
                        blessMeshObjectWithCoherence( coherence, home );
                    }
                    
                    if( probeResult != null ) {
                        if( newBase != theShadowMeshBase && tx2 == null ) {
                            tx2 = theShadowMeshBase.createTransactionAsap();
                        }
                        try {
                            home.setPropertyValue(
                                    ProbeSubjectArea.PROBEUPDATESPECIFICATION_LASTRUNUSEDWRITABLEPROBE,
                                    BooleanValue.create( probeResult.getUsedWritableProbe() ));
                        } catch( IllegalPropertyTypeException ex3 ) {
                            log.error( ex3 );
                        } catch( IllegalPropertyValueException ex3 ) {
                            log.error( ex3 );
                        } catch( NotPermittedException ex3 ) {
                            log.error( ex3 );
                        }
                    }
                        
                    ProbeUpdateSpecification spec = (ProbeUpdateSpecification) home.getTypedFacadeFor( ProbeSubjectArea.PROBEUPDATESPECIFICATION );
                    if( spec != null ) {
                        try {
                            if( newBase != theShadowMeshBase && tx2 == null ) {
                                tx2 = theShadowMeshBase.createTransactionAsap();
                            }
                            
                            if( problem != null ) {
                                spec.performedUnsuccessfulRun( tookTime, problem );
                            } else if( updated ) {
                                spec.performedSuccessfulRunWithChange( tookTime );
                            } else {
                                spec.performedSuccessfulRunNoChange( tookTime );
                            }

                        } catch( Throwable ex3 ) { // this should never go wrong
                            log.error( ex3 );
                        }
                        IntegerValue nextRun = null;
                        try {
                            nextRun = spec.getNextProbeRun();
                        } catch( NotPermittedException ex3 ) {
                            log.error( ex3 );
                        }
                        if( nextRun != null ) {
                            ret = nextRun.value() - System.currentTimeMillis();
                        } else {
                            ret = -1L; // never again
                        }
                    }

                } catch( NotBlessedException ex2 ) {
                    throw new ProbeException.Other( sourceIdentifier, ex2 ); // should never happen

                } catch( TransactionException ex2 ) {
                    throw new ProbeException.Other( sourceIdentifier, ex2 ); // should never happen

                } finally {
                    try {
                        if( tx != null ) {
                            tx.commitTransaction();
                        }
                    } catch( Throwable ex3 ) {
                        log.error( ex3 );
                    }
                    try {
                        if( tx2 != null ) {
                            tx2.commitTransaction();
                        }
                    } catch( Throwable ex3 ) {
                        log.error( ex3 );
                    }
                    theChangesToWriteBack = null; // otherwise we queue our own changes
                    theUpdateInProgress   = false;
                }
            }

        } finally {

            theLastUpdate = theCurrentUpdate;
            
            if( problem != null ) {
                // do not set theLastSuccessfulUpdate
                if( !isFirstRun ) {
                    fireUpdateFinishedUnsuccessfully( problem );
                }

            } else if( updated ) {
                theLastSuccessfulUpdate = theCurrentUpdate;
                if( !isFirstRun ) {
                    fireUpdateFinishedSuccessfully();
                }

            } else { // successful but update skipped
                theLastSuccessfulUpdate = theCurrentUpdate;
                if( !isFirstRun ) {
                    fireUpdateSkipped();
                }
            }
            if( theTimeDiscoveredNotNeeded < 0 ) {
                if( !isNeeded() ) {
                    theTimeDiscoveredNotNeeded = theCurrentUpdate;
                }
            } else {
                if( isNeeded() ) {
                    theTimeDiscoveredNotNeeded = -1L;
                }
            }
            theDelayUntilNextUpdate = ret;
        }
        return ret;
    }

    /**
     * The data source refers to a directory in a fie system, probe the directory.
     * 
     * @param oldBase the StagingMeshBase after the most recent successful run, if any
     * @param newBase the new StagingMeshBase into which to instantiate the data
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @return the ProbeResult
     * @throws ProbeException thrown if unable to compute a result
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws IOException thrown if an I/O error occurred
     */
    protected ProbeResult handleDirectory(
            StagingMeshBase        oldBase,
            StagingMeshBase        newBase,
            CoherenceSpecification coherence )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        throw new UnsupportedOperationException(); // FIXME

//        if( theShadowUpdateCalculator == null )
//            theShadowUpdateCalculator = new AdaptivePeriodicShadowUpdateCalculator(); // FIXME locally defined parameters
//
//        try {
//            File dir = new File( theURL.toURL().getFile() );
//
//            // this will createCopy directory object already
//            facade.getModelObjectRepository().initializeHomeObjectIfNeeded(
//                    theDirectoryType,
//                    facade.getDefaultCreatedTime(),
//                    facade.getDefaultUpdatedTime() );
//
//            File [] containedFiles = determineContainedFiles( dir );
//            for( int i=0 ; i<containedFiles.length ; ++i )
//            {
//                // This is somewhat arbitrary, but looks like it could make good sense?
//                if( ! containedFiles[i].canRead())
//                    continue;
//
//                String shortFileName = containedFiles[i].getName();
//
//                ModelObjectPath path = ModelObjectPath.fromFile( containedFiles[i] );
//
//                RootEntity newDefinitionObject = facade.createForwardReferenceFromMetaObject( theDefinitionObjectType, path );
//                RootEntity newComponentObject  = facade.createFromMetaObject( theFileType, shortFileName );
//
//                newComponentObject.setName( StringValue.createCopy( shortFileName )); // FIXME: more
//
//                facade.createFromMetaObject( theContainsType,   "contains-"   + shortFileName, facade.getHomeObject(), newComponentObject );
//                facade.createFromMetaObject( theReferencesType, "references-" + shortFileName, newComponentObject,     newDefinitionObject );
//            }
//            return true; // assumed to always have been updated
//
//        } catch( DoNotHaveLockException ex ) {
//            throw new ProbeException.ErrorInProbe( theURLString, ex, ProbeDispatcher.class );
//        } catch( MeshObjectIdentifierNotUniqueException ex ) {
//            throw new ProbeException.ErrorInProbe( theURLString, ex, ProbeDispatcher.class );
//        } catch( RuntimeException ex ) {
//            throw new ProbeException.ErrorInProbe( theURLString, ex, ProbeDispatcher.class );
//        }
    }

    /**
     * The data source refers to a stream, parse the stream.
     *
     * @param oldBase the StagingMeshBase after the most recent successful run, if any
     * @param newBase the new StagingMeshBase into which to instantiate the data
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @return the ProbeResult
     * @throws ProbeException thrown if unable to compute a result
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws URISyntaxException thrown if a URI could not be parsed
     * @throws IOException thrown if an I/O error occurred
     */
    protected ProbeResult handleStream(
            StagingMeshBase        oldBase,
            StagingMeshBase        newBase,
            CoherenceSpecification coherence )
        throws
            ProbeException,
            TransactionException,
            URISyntaxException,
            IOException
    {
        boolean               updated          = false;
        byte []               content          = null;
        String                contentAsString  = null;
        String                contentType      = null;
        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();
        String                protocol         = sourceIdentifier.getUrlProtocol();
        URL                   url              = sourceIdentifier.toUrl();
        Probe                 probe            = null;

        String yadisServicesXml  = null;
        String yadisServicesHtml = null;

        long streamDataCreated      = 0L;
        long streamDataLastModified = 0L;

        if ( "file".equals( protocol )) {
            File dataSourceFile = new File( url.getFile() );
            streamDataCreated      = dataSourceFile.lastModified(); // FIXME? No API for that ...
            streamDataLastModified = dataSourceFile.lastModified();

            content     = StreamUtils.slurp( new FileInputStream( dataSourceFile ));
            contentType = null;

        } else if( "http".equals( protocol ) || "https".equals( protocol ) ) {

            HTTP.Response httpResponse = HTTP.http_get(
                    url,
                    XRDS_MIME_TYPE + ", " + HTTP_GET_ACCEPT_HEADER );

            if( httpResponse.isSuccess() && XRDS_MIME_TYPE.equals( httpResponse.getContentType() )) {
                // found XRDS content via MIME type
                
                yadisServicesXml = httpResponse.getContentAsString();

                // now ask again, without the XRDS mime type
                httpResponse = HTTP.http_get( url, HTTP_GET_ACCEPT_HEADER );

            } else {
                String yadisUrl = httpResponse.getSingleHttpHeaderField( "X-XRDS-Location" );
                if( yadisUrl == null ) {
                    yadisUrl = httpResponse.getSingleHttpHeaderField( "X-YADIS-Location" );
                }
                if( yadisUrl != null ) {
                    HTTP.Response yadisResponse = HTTP.http_get( yadisUrl );
                    yadisServicesXml = yadisResponse.getContentAsString();
                }
            }
            
            streamDataCreated      = httpResponse.getLastModified(); // FIXME? No API for that ...
            streamDataLastModified = httpResponse.getLastModified();

            if( !httpResponse.isSuccess() && yadisServicesXml == null ) {
                throw new ProbeException.HttpErrorResponse( sourceIdentifier, httpResponse.getResponseCode() );
            }

            content     = httpResponse.getContent();
            contentType = httpResponse.getContentType();

        } else {
            // we always assume it has changed
            streamDataCreated      = System.currentTimeMillis(); // now
            streamDataLastModified = streamDataCreated; // same

            content     = StreamUtils.slurp( url.openStream() );
            contentType = null;
        }

        if( streamDataLastModified != theMostRecentModificationDate ) {
            updated = true;
        } else if( streamDataLastModified == 0 ) { // that seems to occur
            updated = true;
        }

        if( log.isDebugEnabled() ) {
            log.debug( this + " -- handleStream() date changed: " + updated + " ( " + theMostRecentModificationDate + ", " + streamDataLastModified + " )" );
        }

        theMostRecentModificationDate = streamDataLastModified;

        if( updated ) {
            if( content == null || content.length == 0 ) {
                throw new ProbeException.EmptyDataSource( sourceIdentifier );
            }

            if( log.isDebugEnabled() ) {
                log.debug( this + " in handleStream(): content type is " + contentType );
            }

            if( contentType == null || UNKNOWN_MIME_TYPE.equals( contentType )) {
                contentType = ProbeDispatcher.guessContentTypeFromUrl( url );

            } else if( "text/xml".equals( contentType )) {
                contentType = XML_MIME_TYPE; // makes it easier down the road

            }
            if(    yadisServicesXml == null
                && (    "text/html".equals( contentType )
                     || "text/xhtml".equals( contentType )
                     || "application/xhtml+xml".equals( contentType )) )
            {
                if( contentAsString == null ) {
                    contentAsString = new String( content );
                }
                yadisServicesHtml = contentAsString;
            }

            if( log.isDebugEnabled() ) {
                log.debug( this + " in handleStream(): content type is " + contentType );
            }

            InputStream inStream = new ByteArrayInputStream( content );
            try {
                if( XML_MIME_TYPE.equals( contentType )) {
                    probe = handleXml(
                            oldBase,
                            newBase,
                            coherence,
                            inStream );
                } else {
                    probe = handleNonXml(
                            oldBase,
                            newBase,
                            coherence,
                            contentType,
                            inStream );
                }
            } catch( ProbeException.DontHaveProbe ex ) {
                if( yadisServicesXml == null && yadisServicesHtml == null ) {
                    throw ex; // if we have Yadis services, don't throw this exception
                }
            }

            if( theServiceFactory == null && ( yadisServicesXml != null || yadisServicesHtml != null )) {
                theServiceFactory = new YadisServiceFactory( theShadowMeshBase.getMeshBaseIdentifierFactory(), getDocumentBuilder() );
            }
            if( yadisServicesXml != null ) {
                theServiceFactory.addYadisServicesFromXml( sourceIdentifier, yadisServicesXml, newBase );
            } else if( yadisServicesHtml != null ) {
                theServiceFactory.addYadisServicesFromHtml( sourceIdentifier, yadisServicesHtml, newBase );
            }
            
        }
        return new ProbeResult(
                updated, // we don't know, we always say we might have been updated because that's safer
                probe instanceof WritableProbe );
    }

    /**
     * The data source refers to an API, parse the API.
     *
     * @param oldBase the StagingMeshBase after the most recent successful run, if any
     * @param newBase the new StagingMeshBase into which to instantiate the data
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @return the ProbeResult
     * @throws ProbeException thrown if unable to compute a result
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws IOException thrown if an I/O error occurred
     */
    @SuppressWarnings( "unchecked" )
    protected ProbeResult handleApi(
            StagingMeshBase        oldBase,
            StagingMeshBase        newBase,
            CoherenceSpecification coherence )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        NetMeshBaseIdentifier          sourceIdentifier = theShadowMeshBase.getIdentifier();
        ProbeDirectory.MatchDescriptor desc1            = theProbeDirectory.getApiProbeDescriptorByMatchedUrl( sourceIdentifier.getUriString() );

        Class<? extends Probe> foundClass       = null;
        String                 foundClassName   = null;
        Map<String,Object>     foundParameters  = null; // FIXME: currently not used
        ClassLoader            foundClassLoader = null;

        if( desc1 != null ) {
            foundClass      = desc1.getProbeClass();
            foundClassName  = desc1.getProbeClassName();
            foundParameters = desc1.getParameters();

            if( log.isDebugEnabled() ) {
                log.debug( this + ": based on match, found name for Probe class: " + foundClassName );
            }
            
        } else {
            try {
                ProbeDirectory.ApiProbeDescriptor desc2 = theProbeDirectory.getApiProbeDescriptorByProtocol( sourceIdentifier.getUrlProtocol() );

                if( desc2 != null ) {
                    foundClass      = desc2.getProbeClass();
                    foundClassName  = desc2.getProbeClassName();
                    foundParameters = desc2.getParameters();

                    if( log.isDebugEnabled() ) {
                        log.debug( this + ": based on protocol, found name for Probe class: " + foundClassName );
                    }
                }
            } catch( MalformedURLException ex ) {
                // thrown for unknown protocols. This is fine.
            }
        }

        ApiProbe probe = null;
        if( foundClass == null && foundClassName != null ) {
            if( theModuleRegistry != null ) {
                // we take the first module that supports this interface/class
                StandardModuleAdvertisement [] candidates = theModuleRegistry.findAdvertisementsForInterface( foundClassName, Integer.MAX_VALUE );
                for( int i=0 ; i<candidates.length ; ++i ) {
                    ModuleCapability [] caps = candidates[i].findCapabilitiesByInterface( foundClassName );
                    if( caps != null && caps.length > 0 ) {
                        try {
                            Module foundModule = theModuleRegistry.resolve( candidates[i], true );
                            foundClassLoader = foundModule.getClassLoader();
                            break;
                        } catch( ModuleResolutionException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( MalformedURLException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( ModuleNotFoundException ex ) {
                            log.warn( "Module not found for adv: " + candidates[i], ex );
                        }
                    }
                }
            }
            if( foundClassLoader == null ) { // attempt default loader
                foundClassLoader = getClass().getClassLoader();
            }

            try {
                foundClass = (Class<? extends Probe>) Class.forName( foundClassName, true, foundClassLoader );
            } catch( ClassNotFoundException ex ) {
                throw new ProbeException.DontHaveApiProbe( sourceIdentifier, ex );
            }
        }

        if( foundClass != null ) {
            try {
                probe = (ApiProbe) foundClass.newInstance();

            } catch( InstantiationException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( IllegalAccessException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
        }

        if( probe != null ) {
            // DifferencerMeshBase ret = null;

            if( log.isDebugEnabled() ) {
                log.debug( this + ": invoking the probe" );
            }

            ChangeSet changesToWriteBack;
            synchronized( this ) {
                changesToWriteBack    = theChangesToWriteBack;
                theChangesToWriteBack = null;
            }
            
            try {
                if( probe instanceof WritableProbe && changesToWriteBack != null ) {
                    ((WritableProbe) probe).write( sourceIdentifier, changesToWriteBack, oldBase );
                }

                probe.readFromApi( sourceIdentifier, coherence, newBase );

            } catch( EntityNotBlessedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( EntityBlessedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( IsAbstractException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( NotPermittedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( MeshObjectIdentifierNotUniqueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RelatedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( NotRelatedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RoleTypeBlessedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( IllegalPropertyTypeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( IllegalPropertyValueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( URISyntaxException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( ModuleException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RuntimeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
            if( log.isDebugEnabled() ) {
                log.debug( this + ": probe came back without exception" );
            }
        } else {
            throw new ProbeException.DontHaveApiProbe( sourceIdentifier, null );
        }

        return new ProbeResult(
                true, // we don't know, we always say we might have been updated because that's safer
                probe instanceof WritableProbe );
    }
    
    /**
     * The data source refers to an XML file or stream, parse the XML.
     *
     * @param oldBase the StagingMeshBase after the most recent successful run, if any
     * @param newBase the new StagingMeshBase into which to instantiate the data
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @param inStream the incoming data stream
     * @return the used Probe instance
     * @throws ProbeException thrown if unable to compute a result
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws IOException thrown if an I/O error occurred
     */
    @SuppressWarnings( "unchecked" )
    protected Probe handleXml(
            StagingMeshBase        oldBase,
            StagingMeshBase        newBase,
            CoherenceSpecification coherence,
            InputStream            inStream )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".handleXml( " + oldBase + ", " + newBase + ", " + inStream + " )" );
        }

        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();
        XmlErrorHandler       errorListener    = new XmlErrorHandler( sourceIdentifier, log );

        DocumentBuilder theDocumentBuilder = getDocumentBuilder();
        
        theDocumentBuilder.setErrorHandler( errorListener );

        Document doc;

        try {
            doc = theDocumentBuilder.parse( inStream );
        } catch( SAXException ex ) {
            throw new ProbeException.SyntaxError( sourceIdentifier, ex );
        }

        if( errorListener.numberOfErrors() > 0 ) {
            throw new ProbeException.SyntaxError( sourceIdentifier, errorListener.getAsException() );
        }

        if( !doc.hasChildNodes() ) {
            throw new ProbeException.EmptyDataSource( sourceIdentifier );
        }

        DocumentType docType = doc.getDoctype();
        String       tagType = doc.getDocumentElement().getTagName();

        if( log.isDebugEnabled() ) {
            if( docType != null ) {
                log.debug( this + ": parsed XML input, found document type \"" + docType.getName() + "\"" );
            } else {
                log.debug( this + ": parsed XML input, found null document type" );
            }
        }

        XmlDOMProbe            probe            = null;
        String                 foundClassName   = null;
        Class<? extends Probe> foundClass       = null;
        ClassLoader            foundClassLoader = null;
        Map<String,Object>     foundParameters  = null; // FIXME not used

        if( docType != null ) {
            if( MeshObjectSetProbeTags.MESHOBJECT_SET_TAG.equalsIgnoreCase( docType.getName() )) {
                Probe ret = handleNativeFormat( newBase, coherence, doc );
                return ret;
            }

            ProbeDirectory.XmlDomProbeDescriptor desc = theProbeDirectory.getXmlDomProbeDescriptorByDocumentType( docType.getName() );
            if( desc != null ) {
                foundClass      = desc.getProbeClass();
                foundClassName  = desc.getProbeClassName();
                foundParameters = desc.getParameters();

                if( log.isDebugEnabled() ) {
                    log.debug( this + ": based on doctype, found name for probe class: " + foundClassName );
                }
            }

        } else {
            if ( tagType != null ) {
                ProbeDirectory.XmlDomProbeDescriptor desc = theProbeDirectory.getXmlDomProbeDescriptorByTagType( tagType );
                if( desc != null ) {
                    foundClass      = desc.getProbeClass();
                    foundClassName  = desc.getProbeClassName();
                    foundParameters = desc.getParameters();

                    if( log.isDebugEnabled() ) {
                        log.debug( this + ": based on tagtype, found name for probe class: " + foundClassName );
                    }
                }
                
            } else {
                if( log.isDebugEnabled() ) {
                  log.info( this + ".handleXml - tagType is null - no probe found" );
                }

                throw new ProbeException.CannotDetermineContentType(
                        sourceIdentifier,
                        new XmlProbeException.CannotDetermineDtd( sourceIdentifier, docType, null ));
            }
        }

        if( foundClass == null && foundClassName != null ) {
            if( theModuleRegistry != null ) {
                // we take the first module that supports this interface/class
                StandardModuleAdvertisement [] candidates = theModuleRegistry.findAdvertisementsForInterface( foundClassName, Integer.MAX_VALUE );
                for( int i=0 ; i<candidates.length ; ++i ) {
                    ModuleCapability [] caps = candidates[i].findCapabilitiesByInterface( foundClassName );
                    if( caps != null && caps.length > 0 ) {
                        try {
                            Module foundModule = theModuleRegistry.resolve( candidates[i], true );
                            foundClassLoader = foundModule.getClassLoader();
                            break;
                        } catch( ModuleResolutionException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( ModuleNotFoundException ex ) {
                            log.warn( "Module not found for adv: " + candidates[i], ex );
                        }
                    }
                }
            }
            if( foundClassLoader == null ) { // attempt default loader
                foundClassLoader = getClass().getClassLoader();
            }

            try {
                foundClass = (Class<? extends Probe>) Class.forName( foundClassName, true, foundClassLoader );

            } catch( ClassNotFoundException ex ) {
                throw new ProbeException.DontHaveXmlStreamProbe( sourceIdentifier, docType != null ? docType.getName() : null, tagType, ex );
            }
        }

        if( foundClass != null ) {
            try {
                probe = (XmlDOMProbe) foundClass.newInstance();

            } catch( IllegalAccessException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( InstantiationException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
        }

        if( probe != null ) {
            if( log.isDebugEnabled() ) {
                log.debug( this + ": invoking the probe" );
            }
        
            ChangeSet changesToWriteBack;
            synchronized( this ) {
                changesToWriteBack    = theChangesToWriteBack;
                theChangesToWriteBack = null;
            }

            try {
                if( probe instanceof WritableProbe ) {
                    ((WritableProbe) probe).write( sourceIdentifier, changesToWriteBack, oldBase );
                }

                probe.parseDocument( sourceIdentifier, coherence, doc, newBase );

            } catch( IsAbstractException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( EntityBlessedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( EntityNotBlessedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( NotPermittedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( MeshObjectIdentifierNotUniqueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RelatedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( NotRelatedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RoleTypeBlessedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( IllegalPropertyTypeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( IllegalPropertyValueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( URISyntaxException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( ModuleException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RuntimeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
            if( log.isDebugEnabled() ) {
                log.debug( this + ": probe came back without exception" );
            }

        } else {
            throw new ProbeException.DontHaveXmlStreamProbe( sourceIdentifier, docType != null ? docType.getName() : null, tagType, null );
        }
        
        return probe;
    }

    /**
     * The data source refers to a non-XML file or stream, parse it.
     *
     * @param oldBase the StagingMeshBase after the most recent successful run, if any
     * @param newBase the new StagingMeshBase into which to instantiate the data
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @param contentType the MIME type of the data source
     * @param inStream the incoming data stream
     * @return the used Probe instance
     * @throws ProbeException thrown if unable to compute a result
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws IOException thrown if an I/O error occurred
     */
    @SuppressWarnings( "unchecked" )
    protected NonXmlStreamProbe handleNonXml(
            StagingMeshBase        oldBase,
            StagingMeshBase        newBase,
            CoherenceSpecification coherence,
            String                 contentType,
            InputStream            inStream )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".handleNonXml( " + contentType + ", " + inStream + " )" );
        }

        NonXmlStreamProbe      probe            = null;
        Class<? extends Probe> foundClass       = null;
        String                 foundClassName   = null;
        ClassLoader            foundClassLoader = null;
        Map<String,Object>     foundParameters  = null; // FIXME what are they used for?
        NetMeshBaseIdentifier  sourceIdentifier = theShadowMeshBase.getIdentifier();

        ProbeDirectory.StreamProbeDescriptor desc = theProbeDirectory.getStreamProbeDescriptorByMimeType( contentType );

        if( desc == null ) {
            desc = theProbeDirectory.getDefaultStreamProbe();
        }

        if( desc != null ) {
            foundClass      = desc.getProbeClass();
            foundClassName  = desc.getProbeClassName();
            foundParameters = desc.getParameters();

            if( log.isDebugEnabled() ) {
                log.debug( this + ": based on mime type, found name for probe class: " + foundClassName );
            }
        }

        if( foundClass == null && foundClassName != null ) {
            if( theModuleRegistry != null ) {
                // we take the first module that supports this interface/class
                StandardModuleAdvertisement [] candidates = theModuleRegistry.findAdvertisementsForInterface( foundClassName, Integer.MAX_VALUE );
                for( int i=0 ; i<candidates.length ; ++i ) {
                    ModuleCapability [] caps = candidates[i].findCapabilitiesByInterface( foundClassName );
                    if( caps != null && caps.length > 0 ) {
                        try {
                            Module foundModule = theModuleRegistry.resolve( candidates[i], true );
                            foundClassLoader = foundModule.getClassLoader();
                            break;

                        } catch( ModuleResolutionException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( MalformedURLException ex ) {
                            log.warn( "Module could not be resolved for adv: " + candidates[i], ex );
                        } catch( ModuleNotFoundException ex ) {
                            log.warn( "Module not found for adv: " + candidates[i], ex );
                        }
                    }
                }
            }
            if( foundClassLoader == null ) { // attempt default loader
                foundClassLoader = getClass().getClassLoader();
            }

            try {
                foundClass = (Class<? extends Probe>) Class.forName( foundClassName, true, foundClassLoader );
            } catch( ClassNotFoundException ex ) {
                throw new ProbeException.DontHaveNonXmlStreamProbe( sourceIdentifier, contentType, ex );
            }
        }

        if( foundClass != null ) {
            try {
                probe = (NonXmlStreamProbe) foundClass.newInstance();
            } catch( InstantiationException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            } catch( IllegalAccessException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
        }
        if( probe != null ) {
            // DifferencerMeshBase ret = null;

            if( log.isDebugEnabled() ) {
                log.debug( this + ": invoking the probe" );
            }

            ChangeSet changesToWriteBack;
            synchronized( this ) {
                changesToWriteBack    = theChangesToWriteBack;
                theChangesToWriteBack = null;
            }

            try {
                if( probe instanceof WritableProbe ) {
                    ((WritableProbe) probe).write( sourceIdentifier, changesToWriteBack, oldBase );
                }

                probe.readFromStream( sourceIdentifier, coherence, inStream, contentType, newBase );

            } catch( IsAbstractException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( EntityBlessedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( EntityNotBlessedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( NotPermittedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( MeshObjectIdentifierNotUniqueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RelatedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( NotRelatedException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RoleTypeBlessedAlreadyException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( IllegalPropertyTypeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( IllegalPropertyValueException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( URISyntaxException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( ModuleException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );

            } catch( RuntimeException ex ) {
                throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, foundClass );
            }
            if( log.isDebugEnabled() ) {
                log.debug( this + ": probe came back without exception" );
            }
        } else {
            throw new ProbeException.DontHaveNonXmlStreamProbe( sourceIdentifier, contentType, null );
        }
        
        return probe;
    }

    /**
     * The data source refers to InfoGrid native format, parse the DOM.
     *
     * @param newBase the new StagingMeshBase into which to instantiate the data
     * @param coherence the CoherenceSpecification specified by the client, if any
     * @param doc the XML Document to parse
     * @return the used Probe instance
     * @throws ProbeException thrown if unable to compute a result
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws IOException thrown if an I/O error occurred
     */
    protected DomMeshObjectSetProbe handleNativeFormat(
            StagingMeshBase        newBase,
            CoherenceSpecification coherence,
            Document               doc )
        throws
            ProbeException,
            TransactionException,
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".handleNativeFormat( " + doc + " )" );
        }

        NetMeshBaseIdentifier sourceIdentifier = theShadowMeshBase.getIdentifier();
        DomMeshObjectSetProbe theProbe         = new DomMeshObjectSetProbe();

        try {
            theProbe.parseDocument( sourceIdentifier, coherence, doc, newBase );

        } catch( NotPermittedException ex ) {
            throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, DomMeshObjectSetProbe.class );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            throw new ProbeException.ErrorInProbe( sourceIdentifier, ex, DomMeshObjectSetProbe.class );
        }
        
        return theProbe;
    }
    
    /**
     * Helper method to determine a CoherenceSpecification from a MeshObject, if it is suitably blessed.
     *
     * @param obj the MeshObject
     * @return the found CoherenceSpecification, or null
     */
    protected CoherenceSpecification determineCoherenceFromMeshObject(
            MeshObject obj )
    {
        CoherenceSpecification ret;

        try {
            EntityType subtype = obj.determineSingleBlessedSubtype( ProbeSubjectArea.PROBEUPDATESPECIFICATION );

            if( subtype == null ) {
                ret = null;

            } else if( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION == subtype ) {

                long   fallbackDelay  = ((IntegerValue)obj.getPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_FALLBACKDELAY  )).value();
                long   maxDelay       = ((IntegerValue)obj.getPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_MAXDELAY       )).value();
                double adaptiveFactor = ((FloatValue)  obj.getPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_ADAPTIVEFACTOR )).value();

                ret = new CoherenceSpecification.AdaptivePeriodic(
                        fallbackDelay,
                        maxDelay,
                        adaptiveFactor );

            } else if( ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION == subtype ) {

                ret = CoherenceSpecification.ONE_TIME_ONLY;

            } else if( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION == subtype ) {

                long period = ((IntegerValue)obj.getPropertyValue( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION_DELAY )).value();

                ret = new CoherenceSpecification.Periodic( period );

            } else {

                log.error( "ProbeUpdateSpecification subtype not supported: " + subtype.getIdentifier().toExternalForm() );
                ret = null;

            }

        } catch( IllegalStateException ex ) { // from the single blessed subtype
            log.error( ex );
            ret = null;
        } catch( EntityNotBlessedException ex ) {
            log.error( ex );
            ret = null;
        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );
            ret = null;
        } catch( NotPermittedException ex ) {
            log.error( ex );
            ret = null;
        }
        return ret;
    }
    
    /**
     * Helper method to bless a MeshObject with a suitable MeshType, given the provided CoherenceSpecification.
     *
     * @param coherence the CoherenceSpecification
     * @param obj the MeshObject
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     */
    protected void blessMeshObjectWithCoherence(
            CoherenceSpecification coherence,
            MeshObject             obj )
        throws
            TransactionException
    {
        try {
            if( coherence == CoherenceSpecification.ONE_TIME_ONLY ) {

                obj.bless( ProbeSubjectArea.ONETIMEONLYPROBEUPDATESPECIFICATION );

            } else if( coherence instanceof CoherenceSpecification.Periodic ) {

                CoherenceSpecification.Periodic realCoherence = (CoherenceSpecification.Periodic) coherence;

                obj.bless( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION );
                obj.setPropertyValue( ProbeSubjectArea.PERIODICPROBEUPDATESPECIFICATION_DELAY, IntegerValue.create( realCoherence.getPeriod() ));

            } else if( coherence instanceof CoherenceSpecification.AdaptivePeriodic ) {

                CoherenceSpecification.AdaptivePeriodic realCoherence = (CoherenceSpecification.AdaptivePeriodic) coherence;

                obj.bless( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION );
                obj.setPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_FALLBACKDELAY,  IntegerValue.create( realCoherence.getFallbackDelay() ));
                obj.setPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_MAXDELAY,       IntegerValue.create( realCoherence.getMaxDelay() ));
                obj.setPropertyValue( ProbeSubjectArea.ADAPTIVEPERIODICPROBEUPDATESPECIFICATION_ADAPTIVEFACTOR, FloatValue.create(   realCoherence.getAdaptiveFactor() ));

            } else {

                log.error( "CoherenceSpecification subtype not supported: " + coherence );
            }

        } catch( IsAbstractException ex ) {
            log.error( ex );

        } catch( EntityBlessedAlreadyException ex ) {
            log.error( ex );

        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );

        } catch( IllegalPropertyValueException ex ) {
            log.error( ex );

        } catch( NotPermittedException ex ) {
            log.error( ex );
        }
    }

    /**
     * Copy the ProbeUpdateSpecification from one MeshObject to another.
     *
     * @param source the source MeshObject
     * @param destination the destination MeshObject
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     */
    protected void copyProbeUpdateSpecification(
            MeshObject source,
            MeshObject destination )
        throws
            TransactionException
    {
        EntityType sourceSubtype;
        try {
            sourceSubtype = source.determineSingleBlessedSubtype( ProbeSubjectArea.PROBEUPDATESPECIFICATION );
            if( sourceSubtype == null ) {
                return;
            }
        } catch( EntityNotBlessedException ex ) {
            log.error( ex ); // we can't copy if it ain't there.
            return;
        }
        
        try {
            EntityType destinationSubtype = destination.determineSingleBlessedSubtype( ProbeSubjectArea.PROBEUPDATESPECIFICATION );
            if( destinationSubtype != null ) {
                return;
            }
        } catch( EntityNotBlessedException ex ) {
            // this is fine, we are copying
        }

        try {
            // copy over
            destination.bless( sourceSubtype );
            for( PropertyType prop : sourceSubtype.getAllPropertyTypes() ) {
                if( prop.getIsReadOnly().value() ) {
                    continue;
                }
                PropertyValue value = source.getPropertyValue( prop );
                destination.setPropertyValue( prop, value );
            }

        } catch( IsAbstractException ex ) {
            log.error( ex );

        } catch( IllegalStateException ex ) {
            log.error( ex );

        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );

        } catch( EntityBlessedAlreadyException ex ) {
            log.error( ex );

        } catch( IllegalPropertyValueException ex ) {
            log.error( ex );

        } catch( NotPermittedException ex ) {
            log.error( ex );
        }
    }

    /**
     * Obtain the time at which this ShadowMeshBase was created.
     *
     * @return the time at which this ShadowMeshBase was created, in System.currentTimeMillis() format
     * @see #getCurrentUpdateStartedTime
     * @see #getLastSuccessfulUpdateStartedTime
     * @see #getLastUpdateStartedTime
     */
    public long getTimeCreated()
    {
        return theTimeCreated;
    }

    /**
     * Obtain the time at which the most recent successful update of this
     * ShadowMeshBase was started.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     * @see #getCurrentUpdateStartedTime
     * @see #getTimeCreated
     * @see #getLastUpdateStartedTime
     */
    public long getLastSuccessfulUpdateStartedTime()
    {
        return theLastSuccessfulUpdate;
    }

    /**
     * Obtain the time at which the most recent update of this ShadowMeshBase
     * was started, regardless of whether it was successful or not. This is only
     * updated once the update has finished.
     *
     * @return the time at which the update started, in System.currentTimeMillis() format
     * @see #getCurrentUpdateStartedTime
     * @see #getTimeCreated
     * @see #getLastSuccessfulUpdateStartedTime
     */
    public long getLastUpdateStartedTime()
    {
        return theLastUpdate;
    }

    /**
     * Obtain the time at which the current run was started. This is updated as soon
     * as the run starts.
     *
     * @return the start time of the current update
     * @see #getTimeCreated
     * @see #getLastSuccessfulUpdateStartedTime
     * @see #getLastUpdateStartedTime
     */
    public long getCurrentUpdateStartedTime()
    {
        return theCurrentUpdate;
    }

    /**
     * Obtain the number of milliseconds from now until the next update is supposed to happen.
     *
     * @return the number of milliseconds from now until the next update is supposed to happen, or 0 if as soon as possible, or -1 if none.
     */
    public long getDelayUntilNextUpdate()
    {
        if( theDelayUntilNextUpdate == MAGIC_UNINITIALIZED_DELAY_UNTIL_NEXT_UPDATE ) {
            // need to initialize first

            theDelayUntilNextUpdate = 0L; // now if anything goes wrong
            MeshObject home = theShadowMeshBase.getHomeObject();
            try {
                IntegerValue found = (IntegerValue) home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_NEXTPROBERUN );
                if( found == null ) {
                    return -1L;
                }
                theDelayUntilNextUpdate = found.value() - System.currentTimeMillis();
                if( theDelayUntilNextUpdate < 0L ) {
                    theDelayUntilNextUpdate = 0; // as soon as possible
                }

            } catch( NotPermittedException ex ) {
                log.error( ex );
            } catch( IllegalPropertyTypeException ex ) {
                log.error( ex );
            }
        }
        return theDelayUntilNextUpdate;
    }

    /**
     * Determine whether at the last run, this ProbeDispatcher used a WritableProbe.
     * 
     * @return true if at the last run, this ProbeDispatcher used a WritableProbe
     */
    public boolean usesWritableProbe()
    {
        if( theUsesWritableProbe == null ) {
            // need to initialize first

            theUsesWritableProbe = Boolean.FALSE; // now if anything goes wrong
            MeshObject home = theShadowMeshBase.getHomeObject();
            try {
                BooleanValue found = (BooleanValue) home.getPropertyValue( ProbeSubjectArea.PROBEUPDATESPECIFICATION_LASTRUNUSEDWRITABLEPROBE );
                if( found == null ) {
                    return false;
                }
                theUsesWritableProbe = found.value();

            } catch( NotPermittedException ex ) {
                log.error( ex );
            } catch( IllegalPropertyTypeException ex ) {
                log.error( ex );
            }
        }
        return theUsesWritableProbe.booleanValue();
    }

    /**
     * Determine whether this ShadowMeshBase is still needed. It is needed if at least
     * one its MeshObjects is replicated to/from another NetMeshBase.
     *
     * @return true if it is still needed
     */
    public synchronized boolean isNeeded()
    {
        for( MeshObject current : theShadowMeshBase ) {
            Proxy [] proxies = ((NetMeshObject)current).getAllProxies();
            if( proxies != null && proxies.length > 0 ) {
                return true; // don't need to do anything
            }
        }
        return false;
    }
    
    /**
     * Forcefully reacquire all locks.
     * 
     * @param base the NetMeshBase for whose content the locks shall be acquired
     */
    protected void forceLockRecovery(
            IterableNetMeshBase base )
    {
        HashMap<Proxy,ArrayList<NetMeshObject>> buckets = new HashMap<Proxy,ArrayList<NetMeshObject>>();
        
        for( MeshObject current : theShadowMeshBase ) {
            NetMeshObject realCurrent = (NetMeshObject) current;
            Proxy         towardsLock = realCurrent.getProxyTowardsLockReplica();
            if( towardsLock != null ) {
                ArrayList<NetMeshObject> already = buckets.get( towardsLock );
                if( already == null ) {
                    already = new ArrayList<NetMeshObject>();
                    buckets.put( towardsLock, already );
                }
                already.add( realCurrent );
                realCurrent.proxyOnlyPushLock( towardsLock );
            }
        }
        for( Proxy p : buckets.keySet() ) {
            NetMeshObject [] localReplicas = ArrayHelper.copyIntoNewArray( buckets.get( p ), NetMeshObject.class );
            p.forceObtainLocks( localReplicas );
        }
    }

    /**
     * Determine whether this ShadowMeshBase can be deleted, because it is not needed
     * any more, and the necessary timeNotNeededTillExpires has expired.
     *
     * @return true if it can be deleted
     */
    public boolean mayBeDeleted()
    {
        if( theTimeDiscoveredNotNeeded < 0 ) {
            return false;
        }
        if( theTimeNotNeededTillExpires < 0 ) {
            return false;
        }
        long delta = System.currentTimeMillis() - theTimeDiscoveredNotNeeded;
        if( delta < theTimeNotNeededTillExpires ) {
            return false;
        }
        return true;
    }

    /**
     * Determine whether an update is in progress.
     *
     * @return true if an update is currently in progress on another thread
     */
    public boolean getIsUpdateInProgress()
    {
        return theUpdateInProgress;
    }
    
    /**
     * Obtain the current problem with updating this ShadowMeshBase, if any. This
     * is a "bound" property.
     *
     * @return the current problem with updating this ShadowMeshBase
     */
    public Throwable getCurrentProblem()
    {
        return theCurrentProblem;
    }

    /**
     * Queue new changes for the Shadow. These changes will be written out by the Probe
     * prior to reading the data source again.
     *
     * @param newChangeSet the set of new Changes
     */
    public synchronized void queueNewChanges(
            ChangeSet newChangeSet )
    {
        if( theChangesToWriteBack == null ) {
            theChangesToWriteBack = ChangeSet.createCopy( newChangeSet );
        } else {
            theChangesToWriteBack.append( newChangeSet );
        }
    }
    
    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addWeakShadowListener
     * @see #addSoftShadowListener
     * @see #removeShadowListener
     */
    public void addDirectShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theShadowListeners.addDirect( newListener );
    }

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addDirectShadowListener
     * @see #addSoftShadowListener
     * @see #removeShadowListener
     */
    public void addWeakShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theShadowListeners.addWeak( newListener );
    }

    /**
     * Add a listener to listen to ShadowMeshBase-specific events.
     *
     * @param newListener the listener to be added
     * @see #addDirectShadowListener
     * @see #addWeakShadowListener
     * @see #removeShadowListener
     */
    public void addSoftShadowListener(
            ShadowMeshBaseListener newListener )
    {
        theShadowListeners.addSoft( newListener );
    }

    /**
     * Remove a listener to listen to ShadowMeshBase-specific events.
     *
     * @param oldListener the listener to be removed
     * @see #addDirectShadowListener
     * @see #addWeakShadowListener
     * @see #addSoftShadowListener
     */
    public void removeShadowListener(
            ShadowMeshBaseListener oldListener )
    {
        theShadowListeners.remove( oldListener );
    }

    /**
     * Obtain an XML DocumentBuilder.
     * 
     * @return the DocumentBuilder
     */
    protected synchronized DocumentBuilder getDocumentBuilder()
    {
        DocumentBuilder ret = null;
        
        if( theDocumentBuilderRef != null ) {
            ret = theDocumentBuilderRef.get();
        }
        if( ret == null ) {
            // We do this messing thing with the context ClassLoader because otherwise,
            // there is no chance that our XML parser can be found. The Sun XML classloader
            // finder is an awful implementation using a broken conceptual model how things
            // should work, but at least we found this workaround.

            ClassLoader ctxt = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader( getClass().getClassLoader() );

                // do XML initialization in the constructor
                DocumentBuilderFactory theDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
                theDocumentBuilderFactory.setNamespaceAware( true );

                // FIXME? theDocumentBuilderFactory.setValidating( true );
                theDocumentBuilderFactory.setIgnoringComments( true );
                theDocumentBuilderFactory.setIgnoringElementContentWhitespace( true );

                ret = theDocumentBuilderFactory.newDocumentBuilder();
            } catch( Exception ex ) {
               log.error( ex );
               log.error( "DocumentBuilderFactory has class loader " + DocumentBuilderFactory.class.getClassLoader() );
            } finally {
                Thread.currentThread().setContextClassLoader( ctxt );
            }
            theDocumentBuilderRef = new WeakReference<DocumentBuilder>( ret );
        }
        return ret;
    }

    /**
     * Helper method to guess a MIME content type from a URL. We leverage the JDK.
     * Basically we look at the extension.
     * FIXME: this should not be hardcoded.
     *
     * @param u the URL whose MIME type shall be guessed
     * @return the MIME type, or null
     */
    public static String guessContentTypeFromUrl(
            URL u )
    {
        String fileName = u.getFile();

        String type = URLConnection.getFileNameMap().getContentTypeFor( fileName );
        if( type != null ) {
            return type;
        }

        // If the system does not know it, we add our own table here
        if( !"file".equals( u.getProtocol() )) {
            return UNKNOWN_MIME_TYPE;
        }

        int period = fileName.lastIndexOf( '.' );
        if( period < 0 ) {
            return UNKNOWN_MIME_TYPE;
        }

        String extension = fileName.substring( period + 1 );
        if( "vcf".equals( extension )) {
            return "text/x-vcard";
        }

        return UNKNOWN_MIME_TYPE;
    }

    /**
     * Determine whether a given NetMeshBaseIdentifier refers to a directory or not.
     * 
     * @param id the NetMeshBaseIdentifier
     * @return true if this references a directory that exists
     */
    protected static boolean isDirectory(
            NetMeshBaseIdentifier id )
    {
        try {
            URL url = id.toUrl();

            if( !"file".equals( url.getProtocol() )) {
                return false;
            }
            File f = new File( url.getPath() );
            if( !f.exists() ) {
                return false;
            }
            if( !f.isDirectory() ) {
                return false;
            }
            return true;
            
        } catch( MalformedURLException ex ) {
            return false;
        } catch( IllegalArgumentException ex ) { // thrown if the NetMeshBaseIdentifier does not have a protocol, for example
            return false;
        }
    }

    /**
     * Determine whether a given protocol is a protocol that requires us to read a
     * stream, or it is a non-stream protocol.
     *
     * @param id the NetMeshBaseIdentifier
     * @return true if this protocol requires us to read a stream
     */
    protected static boolean isStreamProtocol(
            NetMeshBaseIdentifier id )
    {
        try {
            String proto = id.getUrlProtocol();

           return "http".equals( proto ) || "https".equals( proto ) ||"ftp".equals( proto ) || "file".equals( proto );
           
        } catch( MalformedURLException ex ) {
            return false;
        } catch( IllegalArgumentException ex ) {
            return false;
        }
    }

    /**
     * Fire a "we are about to start updating" event.
     */
    protected void fireUpdateStarted()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateStarted()" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 0 );
    }

    /**
     * Fire a "we decided against updating" event.
     */
    protected void fireUpdateSkipped()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateSkipped()" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 1 );
    }

    /**
     * Fire a "we are finished updating and we were successful" event.
     */
    protected void fireUpdateFinishedSuccessfully()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateFinishedSuccessfully()" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 2 );        
    }

    /**
     * Fire a "we are finished updating but we were unsuccessful" event.
     *
     * @param problem the Throwable indicating the problem that occurred
     */
    public void fireUpdateFinishedUnsuccessfully(
             Throwable problem )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".fireUpdateFinishedUnsuccessfully( " + problem + " )" );
        }

        theShadowListeners.fireEvent( new ShadowMeshBaseEvent( theShadowMeshBase ), 3 );        
    }

    /**
     * The ShadowMeshBase on whose behalf we work.
     */
    protected ShadowMeshBase theShadowMeshBase;

    /**
     * The time at which this shadow was created.
     */
    protected long theTimeCreated;
    
    /**
     * The time at which the last successful update of this shadow was started.
     * This is in System.currentTimeMillis() format.
     */
    protected long theLastSuccessfulUpdate;

    /**
     * The time at which the last update of this shadow was started, whether successful or not.
     * This is in System.currentTimeMillis() format.
     */
    protected long theLastUpdate;

    /**
     * The time at which the current update of this shadow was started.
     * This is in System.currentTimeMillis() format.
     */
    protected long theCurrentUpdate;
    
    /**
     * The time at which the next update is supposed to start, or -1 if none.
     * This is in System.currentTimeMillis() format.
     */
    protected long theDelayUntilNextUpdate = MAGIC_UNINITIALIZED_DELAY_UNTIL_NEXT_UPDATE;
    
    /**
     * The last modification date of the data source in question.
     */
    private long theMostRecentModificationDate = 0;

    /**
     * The current problem with updating this shadow, if any. This is a "bound" property whose
     * changes get communicated via the ShadowMeshBaseListener mechanism.
     */
    protected Throwable theCurrentProblem;

    /**
     * The set of Changes to write to a Writable Probe (if any).
     */
    protected ChangeSet theChangesToWriteBack = null;

    /**
      * The directory of all Probes that we know.
      */
    protected ProbeDirectory theProbeDirectory;

    /**
     * The registry of all known Modules, or null if not present.
     */
    protected ModuleRegistry theModuleRegistry;

    /**
     * The list of shadow listeners if any.
     */
    protected FlexibleListenerSet<ShadowMeshBaseListener,ShadowMeshBaseEvent,Integer> theShadowListeners
            = new FlexibleListenerSet<ShadowMeshBaseListener,ShadowMeshBaseEvent,Integer>(){
                    /**
                     * Fire the event to one contained object.
                     *
                     * @param listener the receiver of this event
                     * @param event the sent event
                     * @param parameter dispatch parameter
                     */
                    protected void fireEventToListener(
                            ShadowMeshBaseListener listener,
                            ShadowMeshBaseEvent    event,
                            Integer                parameter )
                    {
                        switch( parameter ) {
                            case 0:
                                listener.updateStarting( event );
                                break;
                            case 1:
                                listener.updateSkipped( event );
                                break;
                            case 2:
                                listener.updateFinishedSuccessfully( event );
                                break;
                            case 3:
                                listener.updateFinishedUnsuccessfully( event );
                                break;
                        }
                    }
            };

    /**
     * The DocumentBuilder for XML parsing.
     */
    protected WeakReference<DocumentBuilder> theDocumentBuilderRef;

    /**
     * Time, in System.currentTimeMillis() format, when the current (or most recent)
     * Probe run started. We need this to make sure our MeshBaseLifecycleManager uses
     * it as the default object creation time.
     */
    protected long theStartOfThisRun;
    
    /**
     * If true, an update from the ProbeDispatcher is currently in progress.
     */
    protected boolean theUpdateInProgress;
    
    /**
     * The time this ShadowMeshBase is not needed until it expires.
     */
    protected long theTimeNotNeededTillExpires;

    /**
     * The time at which it was discovered that this ShadowMeshBase was not needed. (and ever since.)
     */
    protected long theTimeDiscoveredNotNeeded = -1L;

    /**
     * The factory for YadisServices.
     */
    protected YadisServiceFactory theServiceFactory;

    /**
     * If the last Probe run used a Writeble Probe, this is true.
     */
    protected Boolean theUsesWritableProbe;
    
    /**
     * We expect this MIME type to indicate that a stream is XML.
     */
    public static final String XML_MIME_TYPE = "application/xml";

    /**
     * This MIME type indicates that a stream is unknown.
     */
    public static final String UNKNOWN_MIME_TYPE = "content/unknown";

    /**
     * This MIME type indicates a Yadis file.
     */
    public static final String XRDS_MIME_TYPE = "application/xrds+xml";
    
    /**
     * This is the default accept header for HTTP requests.
     */
    public static final String HTTP_GET_ACCEPT_HEADER = "text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5";

    /**
     * The default time, in milliseconds, until a timeout occurs on accessing the AccessSemaphore.
     */
    public static final long DEFAULT_ACCESS_SEMAPHORE_TIMEOUT = 10000L;
    
    /**
     * Magic number indicating a non-initialized theDelayUntilNextUpdate.
     */
    private static final int MAGIC_UNINITIALIZED_DELAY_UNTIL_NEXT_UPDATE = -19191919;
}
