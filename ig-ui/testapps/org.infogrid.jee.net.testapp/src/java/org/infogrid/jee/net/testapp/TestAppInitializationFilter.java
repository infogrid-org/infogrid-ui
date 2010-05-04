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

package org.infogrid.jee.net.testapp;

import java.text.ParseException;
import org.infogrid.jee.rest.net.local.defaultapp.m.AbstractMNetLocalRestfulAppInitializationFilter;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.net.NetMeshObjectAccessException;
import org.infogrid.meshbase.net.NetMeshObjectIdentifierFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.traversal.xpath.XpathTraversalTranslator;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality
 */
public class TestAppInitializationFilter
        extends
            AbstractMNetLocalRestfulAppInitializationFilter
{
    private static Log log; // because this a filter, need to delay initialization
    
    /**
     * Constructor for subclasses only, use factory method.
     */
    public TestAppInitializationFilter()
    {
        // nothing right now
    }

    /**
     * Overridable method to create the NetMeshBaseIdentifierFactory appropriate for this
     * application.
     *
     * @return the created NetMeshBaseIdentifierFactory
     */
    @Override
    protected NetMeshBaseIdentifierFactory createNetMeshBaseIdentifierFactory()
    {
        NetMeshBaseIdentifierFactory ret = DefaultNetMeshBaseIdentifierFactory.create(
                new String[] { "http", "https" },
                new String[] { "custom" } );
        return ret;
    }

    /**
     * Overridable method to create and populate a ProbeDirectory apporpriate for this
     * application.
     *
     * @param meshBaseIdentifierFactory the NetMeshBaseIdentifierFactory to us
     * @return the created and populated ProbeDirectory
     * @throws ParseException thrown if an identifier could not be parsed
     */
    @Override
    protected ProbeDirectory createAndPopulateProbeDirectory(
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
        throws
            ParseException
    {
        MProbeDirectory ret = MProbeDirectory.create();
        toAccess = new NetMeshBaseIdentifier[] {
            meshBaseIdentifierFactory.fromExternalForm( "custom://example.com/" ),
            meshBaseIdentifierFactory.fromExternalForm( "custom://example.org/?foo=bar&argl=brgl" ),
        };

        for( NetMeshBaseIdentifier current : toAccess ) {
            ret.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor( current.toExternalForm(), TestProbe1.class ));
        }
        return ret;
    }

    /**
     * Initialize the initial content of the NetMeshBase.
     *
     * @param incomingRequest the incoming request
     * @param mb the NetMeshBase to initialize
     */
    @Override
    protected void populateNetMeshBase(
            SaneRequest incomingRequest,
            NetMeshBase mb )
    {
        Transaction tx = null;
        try {
            NetMeshBaseLifecycleManager    life = mb.getMeshBaseLifecycleManager();
            NetMeshObjectIdentifierFactory fact = mb.getMeshObjectIdentifierFactory();

            NetMeshObject [] resolved = new NetMeshObject[ toAccess.length ];
            for( int i=0 ; i<resolved.length ; ++i ) {
                resolved[i] = mb.accessLocally( toAccess[i] );
                resolved[i].traverseToNeighborMeshObjects(); // get the related objects into the main MeshBase, too
            }
        
            tx = mb.createTransactionNow();
            
            NetMeshObject a = life.createMeshObject( fact.fromExternalForm( "#aaaa" )); // make sure there are long enough to pass the Regex filter
            NetMeshObject b = life.createMeshObject( fact.fromExternalForm( "#bbbb" ));
            NetMeshObject c = life.createMeshObject( fact.fromExternalForm( "#cccc" ));
            
            a.relate( b );
            c.relate( mb.getHomeObject() );
            
            for( int i=0 ; i<resolved.length ; ++i ) {
                if( resolved[i] != null ) {
                    c.relate( resolved[i] );
                }
            }
            
        } catch( TransactionException ex ) {
            getLog().error( ex );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            getLog().error( ex );
        } catch( NetMeshObjectAccessException ex ) {
            getLog().error( ex );
        } catch( RelatedAlreadyException ex ) {
            getLog().error( ex );
        } catch( NotPermittedException ex ) {
            getLog().error( ex );
        } catch( ParseException ex ) {
            getLog().error( ex );
        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }
    
    /**
     * Initialize the context objects. This may be overridden by subclasses.
     *
     * @param incomingRequest the incoming request
     * @param rootContext the root Context
     * @throws Exception initialization may fail
     */
    @Override
    protected void initializeContextObjects(
            SaneRequest incomingRequest,
            Context     rootContext )
        throws
            Exception
    {
        super.initializeContextObjects( incomingRequest, rootContext );
        
        MeshBase mb = rootContext.findContextObjectOrThrow( MeshBase.class );
        rootContext.addContextObject( XpathTraversalTranslator.create( mb ));

        ViewletFactory vlFact = new TestAppViewletFactory();
        rootContext.addContextObject( vlFact );
    }

    /**
     * Initialize and get the log.
     *
     * @return the log
     */
    private static Log getLog()
    {
        if( log == null ) {
            log = Log.getLogInstance( TestAppInitializationFilter.class ); // our own, private logger
        }
        return log;
    }

    /**
     * THe identifiers of the Probe home objects to be accessLocally'd.
     */
    protected NetMeshBaseIdentifier [] toAccess;
}
