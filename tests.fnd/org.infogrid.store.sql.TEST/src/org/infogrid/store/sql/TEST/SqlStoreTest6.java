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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.store.sql.TEST;

import org.infogrid.store.StoreValue;
import org.infogrid.store.prefixing.IterablePrefixingStore;

import org.infogrid.util.logging.Log;

/**
 * Tests iteration over a PrefixingStore.
 */
public class SqlStoreTest6
        extends
            AbstractSqlStoreTest
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
        //
        
        log.info( "Deleting old database and creating new database" );
        
        theSqlStore.initializeHard();
        
        final String prefix1 = "one-";
        final String prefix2 = "two-";
        
        IterablePrefixingStore store1 = IterablePrefixingStore.create( prefix1, theSqlStore );
        IterablePrefixingStore store2 = IterablePrefixingStore.create( prefix2, theSqlStore );
        
        //
        
        log.info( "Inserting data" );

        for( int i=0 ; i<firstSet.length ; ++i ) {
            TestData current = firstSet[i];
            store1.put(
                    current.theKey,
                    current.theEncodingId,
                    current.theTimeCreated,
                    current.theTimeUpdated,
                    current.theTimeRead,
                    current.theTimeAutoDeletes,
                    current.theData );
        }
        for( int i=0 ; i<secondSet.length ; ++i ) {
            TestData current = secondSet[i];
            store2.put(
                    current.theKey,
                    current.theEncodingId,
                    current.theTimeCreated,
                    current.theTimeUpdated,
                    current.theTimeRead,
                    current.theTimeAutoDeletes,
                    current.theData );
        }

        //

        checkEquals( theSqlStore.size(), firstSet.length + secondSet.length, "Wrong size of SqlStore" );
        checkEquals( store1.size(), firstSet.length,  "Wrong size of store1" );
        checkEquals( store2.size(), secondSet.length, "Wrong size of store2" );
        
        //
        
        int count;
        
//        log.info( "Iterating over what's in the SQL Store" );
//        
//        count = 0;
//        for( StoreValue current : theSqlStore ) {
//            log.traceMethodCallEntry( "Found " + count + ": " + current.getKey() );
//            ++count;
//            TestData found = null;
//            for( TestData data : firstSet ) {
//                if( ( prefix1 + " " + data.theKey ).equals( current.getKey() )) {
//                    found = data;
//                    break;
//                }
//            }
//            for( TestData data : secondSet ) {
//                if( ( prefix2 + " " + data.theKey ).equals( current.getKey() )) {
//                    found = data;
//                    break;
//                }
//            }
//            if( found == null ) {
//                reportError( "Could not find record with key", current.getKey() );
//            }
//        }
//        checkEquals( count, firstSet.length + secondSet.length, "wrong length of set" );

        //
        
        log.info( "Iterating over what's in store1" );
        
        count = 0;
        for( StoreValue current : store1 ) {
            log.debug( "Found " + count + ": " + current.getKey() );
            ++count;
            TestData found = null;
            for( TestData data : firstSet ) {
                if( data.theKey.equals( current.getKey() )) {
                    found = data;
                    break;
                }
            }
            if( found == null ) {
                reportError( "Could not find record with key", current.getKey() );
            }
        }
        checkEquals( count, firstSet.length, "wrong length of set" );
        
        //
        
        log.info( "Iterating over what's in store2" );
        
        count = 0;
        for( StoreValue current : store2 ) {
            log.debug( "Found " + count + ": " + current.getKey() );
            ++count;
            TestData found = null;
            for( TestData data : secondSet ) {
                if( data.theKey.equals( current.getKey() )) {
                    found = data;
                    break;
                }
            }
            if( found == null ) {
                reportError( "Could not find record with key", current.getKey() );
            }
        }
        checkEquals( count, secondSet.length, "wrong length of set" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        SqlStoreTest6 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <database engine>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlStoreTest6( args );
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
     * @param args command-line arguments
     * @throws Exception all sorts of things may go wrong in tests
     */
    public SqlStoreTest6(
            String [] args )
        throws
            Exception
    {
        super( args[0], SqlStoreTest6.class );
        
        theTestStore = theSqlStore;
    }

    /**
     * Constructor for subclasses.
     *
     * @param dataBaseEngine the name of the database engine to use for testing
     * @param c test class
     * @throws Exception all sorts of things may go wrong in tests
     */
    protected SqlStoreTest6(
            String dataBaseEngine,
            Class  c )
        throws
            Exception
    {
        super( dataBaseEngine, c );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( SqlStoreTest6.class);
    
    /**
     * Test data.
     */
    protected static final long now = System.currentTimeMillis();
    protected static final TestData[] firstSet = new TestData[] {
        new TestData( "a", "enc1",           12345L, 67890L, 10111213L,     -1L, "some data".getBytes() ),
        new TestData( "b", "other encoding",    11L,    22L,       33L,     12L, "some longer data, but not very long".getBytes() ),
        new TestData( "c", "Shakespeare's collected cucumbers", now,    now+1,  now+10000L, 99999L, "other data".getBytes() ),
        new TestData( "d", "enc1", 0L, 0L, 0L, -1L, "aliergaierbg".getBytes() ),
    };
    protected static final TestData[] secondSet = new TestData[] {
        new TestData( "e", "enc1", 0L, 0L, 0L, -1L, "aqertghaqer".getBytes() ),
        new TestData( "f", "enc1", 0L, 0L, 0L, -1L, "qewrgqergqer".getBytes() ),
        new TestData( "g", "enc1", 0L, 0L, 0L, -1L, "zsdbgadgb".getBytes() ),
        new TestData( "h", "enc1", 0L, 0L, 0L, -1L, "afgae".getBytes() ),
        new TestData( "i", "enc1", 0L, 0L, 0L, -1L, "qerg".getBytes() ),
    };

    protected static class TestData
    {
        public TestData(
                String  key,
                String  encodingId,
                long    timeCreated,
                long    timeUpdated,
                long    timeRead,
                long    timeAutoDeletes,
                byte [] data )
        {
            theKey             = key;
            theEncodingId      = encodingId;
            theTimeCreated     = timeCreated;
            theTimeUpdated     = timeUpdated;
            theTimeRead        = timeRead;
            theTimeAutoDeletes = timeAutoDeletes;
            theData            = data;
        }
        
        @Override
        public String toString()
        {
            return "TestData with key '" + theKey + "'";
        }
        String  theKey;
        String  theEncodingId;
        long    theTimeCreated;
        long    theTimeUpdated;
        long    theTimeRead;
        long    theTimeAutoDeletes;
        byte [] theData;
    }
}
