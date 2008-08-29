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

import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreValue;
import org.infogrid.store.sql.SqlStoreIOException;
import org.infogrid.util.logging.Log;

/**
 * Tests that we can recover from a closed database connection. It basically
 * performed SqlStoreTest1 (thus the implementation inheritance) but occasionally
 * closes the database connection.
 */
public class SqlStoreTest3
        extends
            SqlStoreTest1
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    @Override
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
            
            if( i % 2 == 1 ) {
                theSqlStore.closeConnection();
            }
        }

        checkEquals( listener.thePuts.size(),       firstSet.length, "Wrong number of puts" );
        checkEquals( listener.theUpdates.size(),    0,               "Wrong number of updates" );
        checkEquals( listener.theGets.size(),       0,               "Wrong number of gets" );
        checkEquals( listener.theFailedGets.size(), 0,               "Wrong number of failedGets" );
        checkEquals( listener.theDeletes.size(),    0,               "Wrong number of deletes" );
        listener.reset();

        for( int i=0 ; i<firstSet.length ; ++i ) {
            TestData   current = firstSet[i];
            StoreValue value   = theTestStore.get( current.theKey );

            checkEquals(          current.theEncodingId,      value.getEncodingId(),      "not the same encodingId" );
            checkEquals(          current.theTimeCreated,     value.getTimeCreated(),     "not the same timeCreated" );
            checkEquals(          current.theTimeUpdated,     value.getTimeUpdated(),     "not the same timeUpdated" );
            checkEquals(          current.theTimeRead,        value.getTimeRead(),        "not the same timeRead" );
            checkEquals(          current.theTimeExpires,     value.getTimeExpires(),     "not the same timeExpires" );
            checkEqualByteArrays( current.theData,            value.getData(),            "not the same content" );

            if( i % 3 == 2 ) {
                theSqlStore.closeConnection();
            }
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

            if( i % 4 == 3 ) {
                theSqlStore.closeConnection();
            }
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
            checkEquals(          current.theTimeExpires,     value.getTimeExpires(),     "not the same timeExpires" );
            checkEqualByteArrays( current.theData,            value.getData(),            "not the same content" );

            if( i % 2 == 1 ) {
                theSqlStore.closeConnection();
            }
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

            if( i % 2 == 1 ) {
                theSqlStore.closeConnection();
            }
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
                
                reportError( "delete was unsuccessful for " + current );

                if( i % 3 == 2 ) {
                    theSqlStore.closeConnection();
                }

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
        SqlStoreTest3 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlStoreTest3( args );
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
      */
    public SqlStoreTest3(
            String [] args )
        throws
            Exception
    {
        super( args );

        theTestStore = theSqlStore;
    }

    /**
      * Constructor for subclasses.
      *
      * @param c test class
      */
    protected SqlStoreTest3(
            Class c )
        throws
            Exception
    {
        super( c );
    }
    
    // Our Logger
    private static Log log = Log.getLogInstance( SqlStoreTest3.class);   
}
