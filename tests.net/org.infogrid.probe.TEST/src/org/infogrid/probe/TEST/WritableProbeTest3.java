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

package org.infogrid.probe.TEST;

import java.io.IOException;
import java.net.URISyntaxException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.ChangeSet;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.WriteableProbe;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.logging.Log;

/**
 * Tests unblessing of MeshObjects with WritableProbe.
 */
public class WritableProbeTest3
        extends
            AbstractWritableProbeTest
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    public void run()
        throws
            Exception
    {
        WritableProbeTestCase [] cases = new WritableProbeTestCase[] {
            new WritableProbeTestCase( NonWritableTestProbe.class ) {
                    public void afterFirstRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        log.debug( "Attempting to unbless in main NetMeshBase, should fail" );

                        Transaction tx     = null;
                        Throwable   thrown = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.unbless( SIMPLE2 );

                        } catch( Throwable ex ) {
                            thrown = ex;

                        } finally {
                            if( tx != null ) {
                                tx.commitTransaction();
                            }
                        }
                        if( thrown == null ) {
                            reportError( "Incorrectly did not throw NotPermittedException" );
                        } else if( !( thrown instanceof NotPermittedException )) {
                            reportError( "Threw wrong exception, was: ", thrown );
                        } else {
                            log.debug( "Correctly failed unblessing" );
                        }
                        checkCondition(  shadowHomeInMain.isBlessedBy( SIMPLE1 ), "Unblessed the wrong thing" );
                        checkCondition(  shadowHomeInMain.isBlessedBy( SIMPLE2 ), "Unblessed the wrong thing" );
                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE3 ), "Should not be blessed" );
                    }

                    public void afterSecondRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        // noop
                    }
            },
            new WritableProbeTestCase( WritableIgnoringTestProbe.class ) {
                    public void afterFirstRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        log.debug( "Attempting to unbless in main NetMeshBase, should work" );

                        Transaction tx = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.unbless( SIMPLE2 );

                        } catch( NotPermittedException ex ) {
                            reportError( "Was not permitted to bless", ex );

                        } finally {
                            if( tx != null ) {
                                tx.commitTransaction();
                            }
                        }
                        checkCondition(  shadowHomeInMain.isBlessedBy( SIMPLE1 ), "Unblessed the wrong thing" );
                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE2 ), "Could not unbless" );
                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE3 ), "Should not be blessed" );
                    }
                    public void afterSecondRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        log.debug( "Should be a ChangeSet of size 1" );

                        checkObject( theChangeSet, "ChangeSet is still null" );
                        checkEquals( theChangeSet.size(), 1, "Wrong size ChangeSet" );

                        //

                        log.debug( "Waiting for replication to happen" );

                        Thread.sleep( 2500L );

                        checkCondition(  shadowHomeInMain.isBlessedBy( SIMPLE1 ), "Unblessed the wrong thing" );
                        checkCondition(  shadowHomeInMain.isBlessedBy( SIMPLE2 ), "Did not revert to old value" );
                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE3 ), "Should not be blessed" );

                    }
            },
            new WritableProbeTestCase( WritableProcessingTestProbe.class ) {
                    public void afterFirstRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        log.debug( "Attempting to unbless in main NetMeshBase, should work" );

                        Transaction tx = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.unbless( SIMPLE2 );

                        } catch( NotPermittedException ex ) {
                            reportError( "Was not permitted to bless", ex );

                        } finally {
                            if( tx != null ) {
                                tx.commitTransaction();
                            }
                        }
                        checkCondition(  shadowHomeInMain.isBlessedBy( SIMPLE1 ), "Unblessed the wrong thing" );
                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE2 ), "Could not unbless" );
                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE3 ), "Should not be blessed" );
                    }

                    public void afterSecondRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        log.debug( "Should be a ChangeSet of size 1" );

                        checkObject( theChangeSet, "ChangeSet is still null" );
                        checkEquals( theChangeSet.size(), 1, "Wrong size ChangeSet" );

                        //

                        log.debug( "Waiting for replication to happen" );

                        Thread.sleep( 2500L );

                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE1 ), "Could not unbless" );
                        checkCondition( !shadowHomeInMain.isBlessedBy( SIMPLE2 ), "Could not unbless" );
                        checkCondition(  shadowHomeInMain.isBlessedBy( SIMPLE3 ), "Should be blessed" );
                    }
            },
            new WritableProbeTestCase( NoWritableProcessingTestProbe.class ) {
                    public void afterFirstRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        log.debug( "Attempting to unbless in main NetMeshBase, should fail" );

                        Transaction tx     = null;
                        Throwable   thrown = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.unbless( SIMPLE2 );

                        } catch( Throwable ex ) {
                            thrown = ex;

                        } finally {
                            if( tx != null ) {
                                tx.commitTransaction();
                            }
                        }
                        if( thrown == null ) {
                            reportError( "Incorrectly did not throw NotPermittedException" );
                        } else if( !( thrown instanceof NotPermittedException )) {
                            reportError( "Threw wrong exception, was: ", thrown );
                        }
                    }

                    public void afterSecondRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        // noop
                    }
            }
        };
        
        super.run( cases );
    }
    
    /*
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        WritableProbeTest3 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no argument>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new WritableProbeTest3( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            System.exit(1);
        }
        if( test != null ) {
            test.cleanup();
        }
        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.info( "FAIL (" + errorCount + " errors)" );
        }
        System.exit( errorCount );
    }

    /**
     * Constructor.
     *
     * @param args the command-line arguments
     * @throws Exception many kinds of things can happen in a test
     */
    public WritableProbeTest3(
            String [] args )
        throws
            Exception
    {
        super( WritableProbeTest3.class  );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( WritableProbeTest3.class  );

    protected static final EntityType SIMPLE1 = TestSubjectArea.SIMPLE1;
    protected static final EntityType SIMPLE2 = TestSubjectArea.SIMPLE2;
    protected static final EntityType SIMPLE3 = TestSubjectArea.SIMPLE3;

    /**
     * Once the WritableProbe runs, it deposits the found ChangeSet here.
     */
    protected static ChangeSet theChangeSet;
    
    /**
     * Abstract Probe class for this set of tests
     */
    public abstract static class AbstractWritableTestProbe
        implements
            ApiProbe
    {
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        freshMeshBase )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EntityNotBlessedException,
                RelatedAlreadyException,
                NotRelatedException,
                MeshObjectIdentifierNotUniqueException,
                IllegalPropertyTypeException,
                IllegalPropertyValueException,
                TransactionException,
                NotPermittedException,
                ProbeException,
                IOException,
                ModuleException,
                URISyntaxException
        {
            if( log.isDebugEnabled() ) {
                log.debug( "Probe: readFromApi()" );
            }

            NetMeshObject home = freshMeshBase.getHomeObject();

            home.bless( theNextValues );
            
            boolean giveUpLock = getGiveUpLock();
            if( giveUpLock ) {
                home.setWillGiveUpLock( giveUpLock );
            }
        }

        /**
         * Enable subclasses to say whether to give up lock.
         * 
         * @return true if give up lock
         */
        protected boolean getGiveUpLock()
        {
            return true;
        }
        
        /**
         * Next value for A_X.
         */
        protected EntityType [] theNextValues = { SIMPLE1, SIMPLE2 };
    }
    /**
     * The non-Writable Probe class.
     */
    public static class NonWritableTestProbe
        extends
            AbstractWritableTestProbe
    {
        @Override
        protected boolean getGiveUpLock()
        {
            return false;
        }
    }

    /**
     * The Writable Probe class that ignores the changes.
     */
    public static class WritableIgnoringTestProbe
        extends
            AbstractWritableTestProbe
        implements
            WriteableProbe
    {
        public void write(
                NetMeshBaseIdentifier networkId,
                ChangeSet             updateSet,
                StagingMeshBase       previousMeshBaseWithUpdates )
            throws
                ProbeException,
                IOException
        {
            if( log.isDebugEnabled() ) {
                log.debug( "Probe: write( " + updateSet + " ), but ignoring" );
            }
            
            theChangeSet = updateSet;
        }
    }
    
    /**
     * The Writable Probe class that allows and processes the changes.
     */
    public static class WritableProcessingTestProbe
        extends
            AbstractWritableTestProbe
        implements
            WriteableProbe
    {
         public void write(
                NetMeshBaseIdentifier networkId,
                ChangeSet             updateSet,
                StagingMeshBase       previousMeshBaseWithUpdates )
            throws
                ProbeException,
                IOException
        {
            if( log.isDebugEnabled() ) {
                log.debug( "Probe: write( " + updateSet + " )" );
            }
            
            theNextValues = new EntityType[] { SIMPLE3 };

            theChangeSet = updateSet;
        }
    }
    
    /**
     * The Writable Probe class that does not allow but processes the changes.
     */
    public static class NoWritableProcessingTestProbe
        extends
            WritableProcessingTestProbe
    {
        /**
         * Enable subclasses to say whether to give up lock.
         * 
         * @return true if give up lock
         */
        @Override
        protected boolean getGiveUpLock()
        {
            return false;
        }
    }
}