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

import java.util.Iterator;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.Factory;
import org.infogrid.util.MSmartFactory;
import org.infogrid.util.logging.Log;

/**
 * Tests the behavior of the values iterator in a MSmartFactory.
 */
public class SmartFactoryTest4
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
        Factory<String,Foo,Integer> delegateFactory = new AbstractFactory<String,Foo,Integer>() {
            public Foo obtainFor(
                    String  key,
                    Integer argument )
            {
                int value = argument.intValue();
                return new Foo( value*value );
            }
        };
        
        MSmartFactory<String,Foo,Integer> testFactory = MSmartFactory.createWeakReference( delegateFactory );
        
        //
        
        log.info( "Creating a few objects and keeping references to them" );
        
        int n1=10;
        Foo [] results1 = new Foo[ n1 ];

        for( int i=0 ; i<n1 ; ++i ) {
            results1[i] = testFactory.obtainFor( "key-obtainFor-" + String.valueOf( i ), i );
        }
        
        collectGarbage();

        //
        
        log.info( "checking that results iterator produces all of them" );
        
        Iterator<Foo> testIter = testFactory.values().iterator();
        Foo [] results2 = new Foo[ n1 ];
        for( int i=0 ; i<n1 ; ++i ) {
            if( log.isDebugEnabled() ) {
                log.debug( "Now iteration " + i );
            }
            if( !testIter.hasNext() ) {
                reportError( "testIter does not have next on iteration " + i );
                break;
            }
            Foo found = testIter.next();
            results2[i] = found;
        }
        checkCondition( !testIter.hasNext(), "testIter still has stuff " );
        checkEqualsOutOfSequence( results1, results2, "Not the same results" );
        
        //
        
        log.info( "Removing a few references" );

        results1[1] = null;
        results1[2] = null;
        results1[6] = null;
        
        results2 = null;
        testIter = null;
        
        Foo [] results3 = ArrayHelper.collectNonNull( results1, Foo.class );
        
        collectGarbage();
        
        //
        
        log.info( "checking that results iterator produces the remaining ones" );
        
        testIter = testFactory.values().iterator();
        Foo [] results4 = new Foo[ results3.length ];
        for( int i=0 ; i<results3.length ; ++i ) {
            if( log.isDebugEnabled() ) {
                log.debug( "Now iteration " + i );
            }
            if( !testIter.hasNext() ) {
                reportError( "testIter does not have next on iteration " + i );
                break;
            }
            Foo found = testIter.next();
            results4[i] = found;
        }
        checkCondition( !testIter.hasNext(), "testIter still has stuff " );
        checkEqualsOutOfSequence( results3, results4, "Not the same results" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        SmartFactoryTest4 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SmartFactoryTest4( args );
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
    public SmartFactoryTest4(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( SmartFactoryTest4.class ); // our own, private logger
    
    /**
     * Test class. Like Integer.
     */
    public static class Foo
    {
        /**
         * Constructor.
         *
         * @param payload the payload
         */
        public Foo(
                int payload )
        {
            thePayload = payload;
        }
        
        /**
         * Obtain the payload
         *
         * @return the payload
         */
        public int intValue()
        {
            return thePayload;
        }
        
        /**
         * Override finalize for easier debugging.
         */
        @Override
        public void finalize()
        {
            if( log.isDebugEnabled() ) {
                log.debug( "Foo( " + thePayload + " ) was just finalized." );
            }
        }
        
        /**
         * For debugging.
         */
        @Override
        public String toString()
        {
            StringBuilder ret = new StringBuilder();
            // ret.append( getClass().getName() );
            // ret.append( "{ pay: " );
            ret.append( thePayload );
            // ret.append( " }" );
            return ret.toString();
        }

        /**
         * The payload.
         */
        protected int thePayload;
    }
}
