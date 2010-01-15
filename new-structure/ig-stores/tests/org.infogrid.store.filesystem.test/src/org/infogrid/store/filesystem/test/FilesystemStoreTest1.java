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

package org.infogrid.store.filesystem.test;

import org.infogrid.store.Store;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreListener;
import org.infogrid.store.StoreValue;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.logging.Log;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Tests the basic FilesystemStore functions. See also SqlStoreTest1.
 */
public class FilesystemStoreTest1
        extends
            AbstractFilesystemStoreTest
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
        
        log.info( "Deleting old Store and creating new Store" );
        
        try {
            theFilesystemStore.deleteAll();
        } catch( IOException ex ) {
            // ignore this one
        }
        
        MyListener listener = new MyListener();
        theTestStore.addDirectStoreListener( listener );

        //
        
        log.info( "Inserting data and checking it's there" );

        for( int i=0 ; i<firstSet.length ; ++i ) {
            TestData current = firstSet[i];
            theTestStore.put(
                    current.theKey,
                    current.theEncodingId,
                    current.theTimeCreated,
                    current.theTimeUpdated,
                    current.theTimeRead,
                    current.theTimeExpires,
                    current.theData );
        }

        checkEquals( listener.thePuts.size(),    firstSet.length, "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(), 0,               "Wrong number of updates" );
        checkEquals( listener.theGets.size(),    0,               "Wrong number of gets" );
        checkEquals( listener.theDeletes.size(), 0,               "Wrong number of deletes" );
        listener.reset();

        for( int i=0 ; i<firstSet.length ; ++i ) {
            TestData   current = firstSet[i];
            StoreValue value   = theTestStore.get( current.theKey );

            checkEquals(          current.theEncodingId,      value.getEncodingId(),      "not the same encodingId" );
            checkEquals(          current.theTimeCreated,     value.getTimeCreated(),     "not the same timeCreated" );
            checkEquals(          current.theTimeUpdated,     value.getTimeUpdated(),     "not the same timeUpdated" );
            checkEquals(          current.theTimeRead,        value.getTimeRead(),        "not the same timeRead" );
            checkEquals(          current.theTimeExpires,     value.getTimeExpires()    , "not the same timeExpires" );
            checkEqualByteArrays( current.theData,            value.getData(),            "not the same content" );
        }

        checkEquals( listener.thePuts.size(),       0,               "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    0,               "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       firstSet.length, "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), 0,               "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    0,               "Wrong number of deletes" );
        listener.reset();

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
                reportError( "Could not find record with key", current.getKey() );
            }
        }
        checkEquals( count, firstSet.length, "wrong length of set" );

        //
        
        log.info( "Updating data and checking it's there" );

        for( int i=0 ; i<secondSet.length ; ++i ) {
            TestData current = secondSet[i];
            theTestStore.update(
                    current.theKey,
                    current.theEncodingId,
                    current.theTimeCreated,
                    current.theTimeUpdated,
                    current.theTimeRead,
                    current.theTimeExpires,
                    current.theData );
        }

        checkEquals( listener.thePuts.size(),       0,                "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    secondSet.length, "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       0,                "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), 0,                "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    0,                "Wrong number of deletes" );
        listener.reset();

        for( int i=0 ; i<secondSet.length ; ++i ) {
            TestData   current = secondSet[i];
            StoreValue value   = theTestStore.get( current.theKey );

            checkEquals(          current.theEncodingId,      value.getEncodingId(),      "not the same encodingId" );
            checkEquals(          current.theTimeCreated,     value.getTimeCreated(),     "not the same timeCreated" );
            checkEquals(          current.theTimeUpdated,     value.getTimeUpdated(),     "not the same timeUpdated" );
            checkEquals(          current.theTimeRead,        value.getTimeRead(),        "not the same timeRead" );
            checkEquals(          current.theTimeExpires,     value.getTimeExpires(),     "not the same timeAAutoDeletes" );
            checkEqualByteArrays( current.theData,            value.getData(),            "not the same content" );
        }

        checkEquals( listener.thePuts.size(),       0,                "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    0,                "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       secondSet.length, "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), 0,                "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    0,                "Wrong number of deletes" );
        listener.reset();

        //
        
        log.info( "Deleting some data and checking it's gone" );

        for( int i=0 ; i<thirdSet.length ; ++i ) {
            TestData current = thirdSet[i];
            theTestStore.delete( current.theKey );
        }

        checkEquals( listener.thePuts.size(),       0,                "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    0,                "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       0,                "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), 0,                "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    thirdSet.length,  "Wrong number of deletes" );
        listener.reset();

        for( int i=0 ; i<thirdSet.length ; ++i ) {
            TestData current = thirdSet[i];
            try {
                theTestStore.get( current.theKey );
                
                reportError( "delete was unsuccessful", current );

            } catch( StoreKeyDoesNotExistException ex ) {
                // noop
            }
        }
        checkEquals( listener.thePuts.size(),       0,                "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    0,                "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       0,                "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), thirdSet.length,  "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    0,                "Wrong number of deletes" );
        listener.reset();
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        FilesystemStoreTest1 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new FilesystemStoreTest1( args );
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
    public FilesystemStoreTest1(
            String [] args )
        throws
            Exception
    {
        super( FilesystemStoreTest1.class );
        
        theTestStore = theFilesystemStore;
    }

    /**
     * Constructor for subclasses.
     *
     * @param c test class
     * @throws Exception all sorts of things may go wrong in a test
     */
    protected FilesystemStoreTest1(
            Class c )
        throws
            Exception
    {
        super( c );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( FilesystemStoreTest1.class);
    
    /**
     * Test data.
     */
    protected static final long now = System.currentTimeMillis();
    protected static final byte [] testImage;
    static {
        byte [] data = new byte[0];
        try {
            data = StreamUtils.slurp( FilesystemStoreTest1.class.getResourceAsStream( "TestImage.jpg" ));
        } catch( IOException ex ) {
            log.error( ex );
        }
        testImage = data;
    }
    protected static final TestData[] one = new TestData[] {
        new TestData( "/x/abc",       "enc1",           12345L, 67890L, 10111213L,     -1L, "some data".getBytes() ),
        new TestData( "/x/def",       "other encoding",    11L,    22L,       33L,     12L, "some longer data, but not very long".getBytes() ),
        new TestData( "/x",           "third encoding",      1,      2,         3, 123456L, testImage )
    };
    protected static final TestData[] two = new TestData[] {
        new TestData( "/x/abc", "testing", 5555L, 666L, 1111L,  -1L, "some changed data".getBytes() ),
    };
    protected static final TestData[] three = new TestData[] {
        new TestData( "/x/ghi", "Shakespeare's collected cucumbers", now,    now+1,  now+10000L, 99999L, "Some <b>HTML</b> data".getBytes() )
    };
    protected static final TestData[] firstSet  = ArrayHelper.append( one, three, TestData.class );
    protected static final TestData[] secondSet = two;
    protected static final TestData[] thirdSet  = three;

    protected static class TestData
    {
        public TestData(
                String  key,
                String  encodingId,
                long    timeCreated,
                long    timeUpdated,
                long    timeRead,
                long    timeExpires,
                byte [] data )
        {
            theKey          = key;
            theEncodingId   = encodingId;
            theTimeCreated  = timeCreated;
            theTimeUpdated  = timeUpdated;
            theTimeRead     = timeRead;
            theTimeExpires  = timeExpires;
            theData         = data;
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
        long    theTimeExpires;
        byte [] theData;
    }
    
    /**
     * Test listener.
     */
    static class MyListener
            implements
                StoreListener
    {
        /**
         * A put operation was performed. This indicates either a
         * <code>Store.put</code> or a <code>Store.putOrUpdate</code> operation
         * in which an actual <code>put</code> was performed.
         *
         * @param store the Store that emitted this event
         * @param value the StoreValue that was put
         */
        public void putPerformed(
                Store      store,
                StoreValue value )
        {
            thePuts.add( value.getKey() );
        }

        /**
         * An update operation was performed. This indicates either a
         * <code>Store.update</code> or a <code>Store.putOrUpdate</code> operation
         * in which an actual <code>update</code> was performed.
         *
         * @param store the Store that emitted this event
         * @param value the StoreValue that was updated
         */
        public void updatePerformed(
                Store      store,
                StoreValue value )
        {
            theUpdates.add( value.getKey() );
        }

        /**
         * A get operation was performed.
         *
         * @param store the Store that emitted this event
         * @param value the StoreValue that was gotten
         */
        public void getPerformed(
                Store      store,
                StoreValue value )
        {
            theGets.add( value.getKey() );
        }

        /**
         * A get operation was attempted but not value could be found.
         *
         * @param store the Store that emitted this event
         * @param key the key that was attempted
         */
        public void getFailed(
                Store  store,
                String key )
        {
            theFailedGets.add( key );
        }

        /**
         * A delete operation was performed.
         *
         * @param store the Store that emitted this event
         * @param key the key with which the data element was stored
         */
        public void deletePerformed(
                Store  store,
                String key )
        {
            theDeletes.add( key );
        }

        /**
         * A delete-all operation was performed.
         *
         * @param store the Store that emitted this event
         * @param prefix if given, indicates the prefix of all keys that were deleted. If null, indicates &quot;all&quot;.
         */
        public void deleteAllPerformed(
                Store  store,
                String prefix )
        {
            // no op
        }
        
        /**
         * Reset the listener.
         */
        public void reset()
        {
            thePuts.clear();
            theUpdates.clear();
            theGets.clear();
            theFailedGets.clear();
            theDeletes.clear();
        }

        protected ArrayList<String> thePuts       = new ArrayList<String>();
        protected ArrayList<String> theUpdates    = new ArrayList<String>();
        protected ArrayList<String> theGets       = new ArrayList<String>();
        protected ArrayList<String> theFailedGets = new ArrayList<String>();
        protected ArrayList<String> theDeletes    = new ArrayList<String>();
    }
}
