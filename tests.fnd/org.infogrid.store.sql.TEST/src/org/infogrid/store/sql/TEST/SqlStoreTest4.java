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

package org.infogrid.store.sql.TEST;

import org.infogrid.store.StoreValue;
import org.infogrid.store.sql.SqlStoreIOException;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

import java.util.NoSuchElementException;

/**
 * Tests the SqlStoreIterator. See also FilesystemStoreTest2.
 */
public class SqlStoreTest4
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
        
        try {
            theSqlStore.deleteStore();
        } catch( SqlStoreIOException ex ) {
            // ignore this one
        }
        theSqlStore.initialize();
        
        //
        
        log.info( "Inserting data" );

        for( int i=0 ; i<firstSet.length ; ++i ) {
            TestData current = firstSet[i];
            theTestStore.put(
                    current.theKey,
                    current.theEncodingId,
                    current.theTimeCreated,
                    current.theTimeUpdated,
                    current.theTimeRead,
                    current.theTimeAutoDeletes,
                    current.theData );
        }

        //
        
        log.info( "Iterating over what's in the Store" );
        
        int count = 0;
        for( StoreValue current : theTestStore ) {
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
                reportError( "Could not find record with key " + current.getKey() );
            }
        }
        checkEquals( count, firstSet.length, "wrong length of set" );

        //
        
        log.info( "Trying out different hasNext-N's" );
        
        CursorIterator<StoreValue> iter = theTestStore.iterator();
        
        checkCondition( iter.hasNext(), "does not have next" );
        for( int i=0 ; i<firstSet.length ; ++i ) {
            checkCondition( iter.hasNext( i ), "does not have next (" + i + ")" );
        }
        
        //
        
        log.info( "Trying out nexts" );
        
        checkEquals( iter.next().getKey(), firstSet[0].theKey, "first element wrong" );
        StoreValue [] found = iter.next( firstSet.length - 2 );
        for( int i=0 ; i<found.length ; ++i ) {
            checkEquals( found[i].getKey(), firstSet[1+i].theKey, "second set of elements wrong, index " + i );
        }
        checkEquals( iter.next().getKey(), firstSet[firstSet.length-1].theKey, "last element wrong" );
        try {
            iter.next();
            reportError( "Should have thrown exception" );
        } catch( NoSuchElementException ex ) {
            // no op
        }
        
        //
        
        log.info( "Trying out different hasPrevious-N's" );
        
        checkCondition( iter.hasPrevious(), "does not have previous" );
        for( int i=0 ; i<firstSet.length ; ++i ) {
            checkCondition( iter.hasPrevious( i ), "does not have previous (" + i + ")" );
        }
        
        //
        
        log.info( "Trying out nexts" );
        
        checkEquals( iter.previous().getKey(), firstSet[firstSet.length-1].theKey, "first element wrong" );
        found = iter.previous( firstSet.length - 2 );
        for( int i=0 ; i<found.length ; ++i ) {
            checkEquals( found[i].getKey(), firstSet[firstSet.length-2-i].theKey, "second set of elements wrong, index " + i );
        }
        checkEquals( iter.previous().getKey(), firstSet[0].theKey, "last element wrong" );
        try {
            iter.previous();
            reportError( "Should have thrown exception" );
        } catch( NoSuchElementException ex ) {
            // no op
        }        
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        SqlStoreTest4 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <database engine>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlStoreTest4( args );
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
     * @throws Exception all sorts of things may go wrong in a test
     */
    public SqlStoreTest4(
            String [] args )
        throws
            Exception
    {
        super( args[0], SqlStoreTest4.class );
        
        theTestStore = theSqlStore;
    }

    /**
     * Constructor for subclasses.
     *
     * @param dataBaseEngine the name of the database engine to use for testing
     * @param c test class
     * @throws Exception all sorts of things may go wrong in a test
     */
    protected SqlStoreTest4(
            String dataBaseEngine,
            Class  c )
        throws
            Exception
    {
        super( dataBaseEngine, c );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( SqlStoreTest4.class );
    
    /**
     * Test data.
     */
    protected static final long now = System.currentTimeMillis();
    protected static final TestData[] firstSet = new TestData[] {
        new TestData( "a", "enc1",           12345L, 67890L, 10111213L,     -1L, "some data".getBytes() ),
        new TestData( "b", "other encoding",    11L,    22L,       33L,     12L, "some longer data, but not very long".getBytes() ),
        new TestData( "c", "Shakespeare's collected cucumbers", now,    now+1,  now+10000L, 99999L, "other data".getBytes() ),
        new TestData( "d", "enc1", 0L, 0L, 0L, -1L, "aliergaierbg".getBytes() ),
        new TestData( "e", "enc1", 0L, 0L, 0L, -1L, "aqertghaqer".getBytes() ),
        new TestData( "f", "enc1", 0L, 0L, 0L, -1L, "qewrgqergqer".getBytes() ),
        new TestData( "g", "enc1", 0L, 0L, 0L, -1L, "zsdbgadgb".getBytes() ),
        new TestData( "h", "enc1", 0L, 0L, 0L, -1L, "afgae".getBytes() ),
        new TestData( "i", "enc1", 0L, 0L, 0L, -1L, "qerg".getBytes() ),
        new TestData( "j", "enc1", 0L, 0L, 0L, -1L, "adfga".getBytes() ),
        new TestData( "k", "enc1", 0L, 0L, 0L, -1L, "qergafg".getBytes() ),
        new TestData( "l", "enc1", 0L, 0L, 0L, -1L, "qergqerg".getBytes() ),
        new TestData( "m", "enc1", 0L, 0L, 0L, -1L, "erthwrthaegrae".getBytes() ),
        new TestData( "n", "enc1", 0L, 0L, 0L, -1L, "bg".getBytes() ),
        new TestData( "o", "enc1", 0L, 0L, 0L, -1L, "egtaerg".getBytes() ),
        new TestData( "p", "enc1", 0L, 0L, 0L, -1L, "ryhwretgWRGwfrgae".getBytes() ),
        new TestData( "q", "enc1", 0L, 0L, 0L, -1L, "sfgaetghserthgas".getBytes() ),
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
