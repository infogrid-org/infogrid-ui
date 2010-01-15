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

package org.infogrid.util.test;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.Factory;
import org.infogrid.util.MSmartFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests the event generation of the MSmartFactory.
 */
public class SmartFactoryTest2
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
        Factory<String,Integer,Integer> delegateFactory = new AbstractFactory<String,Integer,Integer>() {
            public Integer obtainFor(
                    String  key,
                    Integer argument )
            {
                int value = argument.intValue();
                return value*value;
            }
        };
        
        MSmartFactory<String,Integer,Integer> testFactory = MSmartFactory.createDirect( delegateFactory );
        
        SwappingHashMapTestListener listener = new SwappingHashMapTestListener();
        testFactory.getStorage().addDirectCachingMapListener( listener );
        
        checkCondition( testFactory.isEmpty(), "testFactory is not empty" );

        //
        
        log.info( "Creating a few objects" );
        
        int n1=10;
        
        for( int i=0 ; i<n1 ; ++i ) {
            int value = testFactory.obtainFor( "key-obtainFor-" + String.valueOf( i ), i );
        }

        checkEquals( testFactory.size(),               n1, "wrong size of testFactory" );
        checkEquals( listener.theAddedEvents.size(),   n1, "wrong number of added events" );
        checkEquals( listener.theRemovedEvents.size(),  0, "wrong number of removed events" );
        listener.clear();

        //
        
        log.info( "Inserting objects directly" );
        
        int n2 = 20;
        
        for( int i=0 ; i<n2 ; ++i ) {
            testFactory.put( "key-put-" + String.valueOf( i ), i );
        }
        
        checkEquals( testFactory.size(),               n1+n2, "wrong size of testFactory" );
        checkEquals( listener.theAddedEvents.size(),      n2, "wrong number of added events" );
        checkEquals( listener.theRemovedEvents.size(),     0, "wrong number of removed events" );
        listener.clear();

        checkCondition( !testFactory.isEmpty(), "testFactory is empty" );
 
        //
        
        log.info( "Removing some objects" );
        
        int n3 = 5;
        
        for( int i=0 ; i<n3 ; ++i ) {
            testFactory.remove( "key-obtainFor-" + String.valueOf( i ));
            testFactory.remove( "key-put-" + String.valueOf( i+5 ));
        }
        
        checkEquals( testFactory.size(),               n1 + n2 - 2*n3, "wrong size of testFactory" );
        checkEquals( listener.theAddedEvents.size(),                0, "wrong number of added events" );
        checkEquals( listener.theRemovedEvents.size(),           2*n3, "wrong number of removed events" );
        listener.clear();
        
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        SmartFactoryTest2 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SmartFactoryTest2( args );
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
    public SmartFactoryTest2(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( SmartFactoryTest2.class ); // our own, private logger
}
