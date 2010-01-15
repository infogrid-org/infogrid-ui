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

package org.infogrid.util.test;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ReturnSynchronizer;
import org.infogrid.util.ReturnSynchronizerException;
import org.infogrid.util.logging.Log;

/**
 * Tests the ReturnSynchronizer.
 */
public class ReturnSynchronizerTest1
        extends
            AbstractTest
{
    /**
     * Run the test.
     * 
     * @throws Exception all sorts of things may happen in a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "Starting" );

        ReturnSynchronizer<Object,Object> theSync = ReturnSynchronizer.create();

        for( int i=0 ; i<theTests.length ; ++i )
        {
            OneTest t = theTests[i];

            log.info( "Running test " + t.theName );

            theSync.beginTransaction();

            for( int j=0 ; j<t.theDelayTimes.length ; ++j ) {
                theSync.addOpenQuery( t.theKeys[j] );
                new ResultProducer( theSync, t.theDelayTimes[j], t.theKeys[j], t.theResults[j] );
            }

            log.info( "waiting for results: " + t.theDelayTimes.length );

            theSync.join();

            for( int j=0 ; j<t.theDelayTimes.length ; ++j )
            {
                Object r = theSync.getResultFor( t.theKeys[j] );
                if( r != t.theResults[j] ) {
                    reportError( "not the same result" );
                }
            }

            Exception caughtException = null;
            try {
                Object r = theSync.getResultFor( new Object() ); // sure not to have this one

            } catch( ReturnSynchronizerException.IllegalKey ex ) {
                caughtException = ex;
            }
            if( caughtException == null ) {
                reportError( "Exception not thrown" );
            }

            theSync.endTransaction();
            checkCondition( theSync.areAllQueriesComplete(), "Not all queries complete" );
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
        ReturnSynchronizerTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ReturnSynchronizerTest1( args );
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
            log.error( "FAIL (" + errorCount + " errors)" );
        }

        System.exit( errorCount );
    }

    /**
     * Constructor.
     * 
     * @param args command-line arguments
     * @throws Exception all sorts of things may happen in a test
     */
    public ReturnSynchronizerTest1(
            String [] args )
        throws
            Exception
    {
    }

    /**
     * return our log
     */
    @Override
    protected Log getLog()
    {
        return log;
    }

    /**
     * the tests that we do
     */
    public static final OneTest theStressTest;
    static {
        final int N = 100;

        long []   delays  = new long[ N ];
        Object [] keys    = new Object[ N ];
        Object [] results = new Object[ N ];

        for( int i=0 ; i<N ; ++i )
        {
            delays[i]  = 4000L + (i*10)/N; // that makes 10 at the same time
            keys[i]    = "stress-key-" + i;
            results[i] = "result-key-" + i;
        }
        theStressTest = new OneTest( "stress", delays, keys, results );
    }

    public static final OneTest [] theTests = new OneTest[] {
            new OneTest(
                    "one-query",
                    new long[]   { 5000L },
                    new Object[] { "one-query-key-1" },
                    new Object[] { "one-query-result-1" } ),
            new OneTest(
                    "two-query",
                    new long[]   { 1000L,                2000L },
                    new Object[] { "one-query-key-1",    "one-query-key-2" },
                    new Object[] { "one-query-result-1", "one-query-result-2" } ),
            theStressTest
            };


    // Our Logger
    private static Log log = Log.getLogInstance( ReturnSynchronizerTest1.class );

    /**
     * A helper class that contains everything necessary to perform and check the result of one test.
     */
    public static class OneTest
    {
        /**
         * create one
         *
         * @param name       name of the test
         * @param delayTimes vector of delay times when to produce the various results
         * @param keys       keys for the various results
         * @param results    the results
         */
        public OneTest(
                String    name,
                long []   delayTimes,
                Object [] keys,
                Object [] results )
        {
            theName       = name;
            theDelayTimes = delayTimes;
            theKeys       = keys;
            theResults    = results;
        }
        public String    theName;
        public long   [] theDelayTimes;
        public Object [] theKeys;
        public Object [] theResults;
    }

    /**
     * A helper thread class that produces a result after some time on a different thread.
     */
    public static class ResultProducer
            extends
                Thread
    {
        /**
         * Constructor.
         * 
         * @param sync the ReturnSynchronizer to use
         * @param theDelayTime the time to delay before producing a result
         * @param theKey the key to use for depositing the result
         * @param theResult the result
         */
        public ResultProducer(
                ReturnSynchronizer<Object,Object> sync,
                long                              theDelayTime,
                Object                            theKey,
                Object                            theResult )
        {
            theReturnSynchronizer = sync;
            delayTime   = theDelayTime;
            keyForQuery = theKey;
            result      = theResult;
            this.start();
        }

        /**
         * Execute thread.
         */
        @Override
        public void run()
        {
            try {
                Thread.sleep( delayTime );

                theReturnSynchronizer.depositQueryResult( keyForQuery, result );

                log.debug( "produced results" );

            } catch( Exception ex ) {
                log.error( ex );
            }
        }

        /**
         * the return synchronizer that we tell about our result
         */
        protected ReturnSynchronizer<Object,Object> theReturnSynchronizer;

        /**
         * the amount of time in millis we wait until we produce our result
         */
        protected long delayTime;

        /**
         * the key for the query whose results we produce
         */
        protected Object keyForQuery;

        /**
         * the result that we produce
         */
        protected Object result;
    }
}
