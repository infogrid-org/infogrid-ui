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

package org.infogrid.jee.TESTAPP;

import org.infogrid.jee.rest.defaultapp.store.AbstractStoreRestfulAppInitializationFilter;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Blob.BlobSubjectArea;
import org.infogrid.store.m.MStore;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.ViewletFactory;

/**
 * Initializes application-level functionality.
 */
public class TESTAPPInitializationFilter
        extends
            AbstractStoreRestfulAppInitializationFilter
{
    private static final Log log = Log.getLogInstance( TESTAPPInitializationFilter.class ); // our own, private logger

    /**
     * Constructor.
     */
    public TESTAPPInitializationFilter()
    {
        // nothing
    }

    /**
     * Initialize the data sources.
     */
    @Override
    protected void initializeDataSources()
    {
        theMeshStore      = MStore.create();
        theFormTokenStore = MStore.create();
    }

    /**
     * Initialize the initial content of the MeshBase.
     * 
     * @param mb the MeshBase to initialize
     */
    @Override
    protected void initializeMeshBase(
            MeshBase mb )
    {
        MeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();
        
        Transaction tx = null;
        try {
            tx = mb.createTransactionNow();
            
            MeshObject image = life.createMeshObject( BlobSubjectArea.IMAGE );
            
            mb.getHomeObject().relate( image );
            
        } catch( IsAbstractException ex ) {
            log.error( ex );
        } catch( RelatedAlreadyException ex ) {
            log.error( ex );
        } catch( NotPermittedException ex ) {
            log.error( ex );
        } catch( TransactionException ex ) {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }

    
    /**
     * Initialize context objects.
     * 
     * @param context the Context
     */
    @Override
    protected void initializeContextObjects(
            Context context )
    {
        ViewletFactory vlFact = new TESTAPPViewletFactory();
        context.addContextObject( vlFact );
    }
}
