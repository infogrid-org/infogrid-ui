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

package org.infogrid.jee.testapp;

import java.net.URISyntaxException;
import org.infogrid.jee.rest.defaultapp.m.AbstractMRestfulAppInitializationFilter;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Blob.BlobSubjectArea;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality.
 */
public class TestAppInitializationFilter
        extends
            AbstractMRestfulAppInitializationFilter
{
    private static Log log; // because this is a filter, delay initialization

    /**
     * Constructor.
     */
    public TestAppInitializationFilter()
    {
        // nothing
    }

    /**
     * Initialize the initial content of the MeshBase.
     * 
     * @param mb the MeshBase to initialize
     */
    @Override
    protected void populateMeshBase(
            MeshBase mb )
    {
        MeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();
        
        Transaction tx = null;
        try {
            tx = mb.createTransactionNow();
            
            MeshObjectIdentifier id = mb.getMeshObjectIdentifierFactory().fromExternalForm( "image" ); // testing is easier with well-known object
            MeshObject image = life.createMeshObject(
                    id,
                    BlobSubjectArea.IMAGE );
            
            mb.getHomeObject().relate( image );
            
        } catch( URISyntaxException ex ) {
            getLog().error( ex );
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            getLog().error( ex );
        } catch( IsAbstractException ex ) {
            getLog().error( ex );
        } catch( RelatedAlreadyException ex ) {
            getLog().error( ex );
        } catch( NotPermittedException ex ) {
            getLog().error( ex );
        } catch( TransactionException ex ) {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }

    /**
     * Initialize the context objects. This may be overridden by subclasses.
     *
     * @param rootContext the root Context
     * @throws Exception initialization may fail
     */
    @Override
    protected void initializeContextObjects(
            Context rootContext )
        throws
            Exception
    {
        super.initializeContextObjects( rootContext );

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
}
