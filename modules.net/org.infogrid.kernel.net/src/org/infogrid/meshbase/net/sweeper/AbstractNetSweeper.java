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

package org.infogrid.meshbase.net.sweeper;

import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.mesh.security.MustNotDeleteHomeObjectException;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetSweeper;
import org.infogrid.meshbase.sweeper.AbstractSweeper;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.logging.Log;

/**
 * Adds sweeping methods specific to NetMeshObjects and NetMeshBases.
 */
public abstract class AbstractNetSweeper
        extends
            AbstractSweeper
        implements
            NetSweeper
{
    private static final Log log = Log.getLogInstance( AbstractNetSweeper.class ); // our own, private logger

    /**
     * Constructor.
     */
    protected AbstractNetSweeper()
    {
        // noop
    }

    /**
     * Invoked by a background sweep process, the Sweeper will purge the
     * MeshObject if this MeshObject should be purged.
     *
     * @param candidate the NetMeshObject that is a candidate for purged
     * @return true if the MeshObject was purged
     */
    public boolean potentiallyPurge(
            NetMeshObject candidate )
    {
        if( shouldBePurged( candidate )) {
            purgeMeshObject( candidate );
            return true;

        } else {
            return false;
        }
    }

    /**
     * Purge a MeshObject. This is a convenience method.
     *
     * @param toPurge the MeshObject to purge
     */
    protected void purgeMeshObject(
            NetMeshObject toPurge )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, ".purgeMeshObject", toPurge.getIdentifier() );
        }

        NetMeshBase base = toPurge.getMeshBase();

        Transaction tx = null;
        try {
            tx = base.createTransactionAsapIfNeeded();
            
            base.getMeshBaseLifecycleManager().purgeReplica( toPurge );

        } catch( TransactionException ex ) {
            log.error( ex );

        } catch( MustNotDeleteHomeObjectException ex ) {
            log.error( ex );

        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }
}
