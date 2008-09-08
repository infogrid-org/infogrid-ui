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
 * Tests blessing of MeshObjects with WritableProbe.
 */
public class WritableProbeTest2
        extends
            AbstractWritableProbeTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things can go wrong during a test
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
                        log.debug( "Attempting to bless in main NetMeshBase, should fail" );

                        Transaction tx     = null;
                        Throwable   thrown = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.bless( NEW_VALUE );

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
                            log.debug( "Correctly failed blessing" );
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
            },
            new WritableProbeTestCase( WritableIgnoringTestProbe.class ) {
                    public void afterFirstRun(
                            NetMeshBase    mainBase,
                            ShadowMeshBase shadow,
                            NetMeshObject  shadowHomeInMain )
                        throws
                            Exception
                    {
                        log.debug( "Attempting to bless in main NetMeshBase, should work" );

                        Transaction tx = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.bless( NEW_VALUE );

                            checkCondition( shadowHomeInMain.isBlessedBy( NEW_VALUE ), "Could not bless" );

                        } catch( NotPermittedException ex ) {
                            reportError( "Was not permitted to bless", ex );

                        } finally {
                            if( tx != null ) {
                                tx.commitTransaction();
                            }
                        }
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

                        checkCondition( shadowHomeInMain.isBlessedBy( OLD_VALUE ), "did not revert to old value" );

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
                        log.debug( "Attempting to bless in main NetMeshBase, should work" );

                        Transaction tx = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.bless( NEW_VALUE );

                            checkCondition( shadowHomeInMain.isBlessedBy( NEW_VALUE ), "Could not bless" );

                        } catch( NotPermittedException ex ) {
                            reportError( "Was not permitted to bless", ex );

                        } finally {
                            if( tx != null ) {
                                tx.commitTransaction();
                            }
                        }
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

                        checkCondition( shadowHomeInMain.isBlessedBy( FINAL_VALUE ), "did not set to final value" );
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
                        log.debug( "Attempting to bless in main NetMeshBase, should fail" );

                        Transaction tx     = null;
                        Throwable   thrown = null;
                        try {
                            tx = mainBase.createTransactionAsap();
                            shadowHomeInMain.bless( NEW_VALUE );

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
    
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        WritableProbeTest2 test = null;
        try {
            if( args.length != 0 ) {
                System.err.println( "Synopsis: <no argument>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new WritableProbeTest2( args );
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
     * @throws Exception all sorts of things can go wrong during a test
     */
    public WritableProbeTest2(
            String [] args )
        throws
            Exception
    {
        super( WritableProbeTest2.class  );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( WritableProbeTest2.class  );

    protected static final EntityType OLD_VALUE   = TestSubjectArea.SIMPLE1;
    protected static final EntityType NEW_VALUE   = TestSubjectArea.SIMPLE2;
    protected static final EntityType FINAL_VALUE = TestSubjectArea.SIMPLE3;

    /**
     * Once the WritableProbe runs, it deposits the found ChangeSet here.
     */
    protected static ChangeSet theChangeSet;
    
    /**
     * Abstract Probe class for this set of tests.
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

            home.bless( theNextValue );
            
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
        protected EntityType theNextValue = OLD_VALUE;
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
            
            theNextValue = FINAL_VALUE;

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
