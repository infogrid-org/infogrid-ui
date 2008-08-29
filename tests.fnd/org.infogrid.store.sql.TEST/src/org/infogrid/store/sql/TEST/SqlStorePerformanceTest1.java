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
import org.infogrid.util.logging.Log;

/**
 * Tests the SqlStore and measures performance.
 */
public class SqlStorePerformanceTest1
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
        
        theSqlStore.deleteStore();
        theSqlStore.initialize();
        
        // 
        
        int nPuts = 1000;
        
        long    now   = System.currentTimeMillis();
        byte [] data = "abc".getBytes();

        log.info( "Doing " + nPuts + " puts" );
        
        startClock();
        for( int i=0 ; i<nPuts ; ++i ) {
            
            theTestStore.put(
                    "key-" + i,
                    ENCODING_ID,
                    now,
                    now,
                    now,
                    -1,
                    data );
        }
        long putsTook = getRelativeTime();
        
        log.debug( "Puts took " + putsTook + " msecs (" + ( putsTook / 1000.0 ) + " secs) -- " + ( (double) putsTook / nPuts ) + " msec per put" );
        
        //
        
        int nGets = 1000;
        
        log.info( "Doing " + nGets + " gets" );
        
        startClock();
        for( int i=0 ; i<nGets ; ++i ) {
            
            StoreValue found = theTestStore.get( "key-" + i );
        }
        long getsTook = getRelativeTime();
        
        log.debug( "Gets took " + getsTook + " msecs (" + ( getsTook / 1000.0 ) + " secs) -- " + ( (double) getsTook / nGets ) + " msec per get" );
        
        //
        
        log.info( "Mixing puts and gets" );
        
        startClock();
        for( int i=0 ; i<(nGets + nPuts)/2; ++i ) {
            
            theTestStore.putOrUpdate(
                    "key-" + ( i + (( nGets + nPuts ) / 4 )) % (( nGets + nPuts ) /2 ), // try to be different from the get index
                    ENCODING_ID,
                    now,
                    now,
                    now,
                    -1,
                    data );
            
            StoreValue found = theTestStore.get( "key-" + i );
        }
        long mixTook = getRelativeTime();
        
        log.debug( "Mix took " + mixTook + " msecs (" + ( mixTook / 1000.0 ) + " secs) -- " + ( (double) mixTook / ( nGets + nPuts ) ) + " msec per get or put" );
        
        //
        
        log.info( "Puts:  " + ( (double) putsTook / nPuts ) + " msec per put" );
        log.info( "Gets:  " + ( (double) getsTook / nPuts ) + " msec per get" );
        log.info( "Mixed: " + ( (double) mixTook /  ( nGets + nPuts )) + " msec per mixed" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        SqlStorePerformanceTest1 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlStorePerformanceTest1( args );
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
    public SqlStorePerformanceTest1(
            String [] args )
        throws
            Exception
    {
        super( SqlStorePerformanceTest1.class );

        theTestStore = theSqlStore;
    }

    /**
      * Constructor for subclasses.
      *
      * @param c test class
      */
    protected SqlStorePerformanceTest1(
            Class c )
        throws
            Exception
    {
        super( c );
    }
    
    // Our Logger
    private static Log log = Log.getLogInstance( SqlStorePerformanceTest1.class);
}
