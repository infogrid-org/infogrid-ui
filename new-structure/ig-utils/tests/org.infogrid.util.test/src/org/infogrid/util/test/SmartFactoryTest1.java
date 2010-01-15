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
 * Tests the basic behavior of the MSimpleSmartFactory.
 */
public class SmartFactoryTest1
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
        Factory<String,Integer,Float> delegateFactory = new AbstractFactory<String,Integer,Float>() {
            public Integer obtainFor(
                    String key,
                    Float  argument )
            {
                int value = argument.intValue();
                return value;
            }
        };
        
        MSmartFactory<String,Integer,Float> testFactory = MSmartFactory.createDirect( delegateFactory );
        
        String [] testKeys1 = {
            "abc",
            "def",
            "ghiklm"
        };
        Float [] testArgs1 = {
            1.f,
            2.f,
            3.4f
        };
        
        //
        
        log.info( "Creating a few objects" );

        Integer [] values1 = new Integer[ testKeys1.length ];
        for( int i=0 ; i<testKeys1.length ; ++i ) {
            values1[i] = testFactory.obtainFor( testKeys1[i], testArgs1[i] );
            
            checkEquals( testArgs1[i].intValue(), values1[i].intValue(), "Not the same" );
        }
        checkEquals( testFactory.size(), testKeys1.length, "wrong number of objects in factory" );

        //
        
        log.info( "Adding a few objects manually" );
        
        String [] testKeys2 = {
            "zztop",
            "aabottom",
            testKeys1[2]
        };
        Integer [] testValues2 = {
            5,
            6,
            7
        };
        Integer [] testValues2Return = {
            null,
            null,
            testArgs1[2].intValue()
        };
        Integer [] testValues2Actual = new Integer[ testValues2Return.length ];

        for( int i=0 ; i<testKeys2.length ; ++i ) {
            testValues2Actual[i] = testFactory.put( testKeys2[i], testValues2[i] );
            
            checkEquals( testValues2Return[i], testValues2Actual[i], "Not the same" );
        }
        checkEquals( testFactory.size(), testKeys1.length + testKeys2.length - 1, "wrong number of objects in factory" );
        
        //
        
        log.info( "looking up objects" );
        
        for( int i=0 ; i<testKeys1.length-1 /* skip the last one */ ; ++i ) {
            Integer ret = testFactory.get( testKeys1[i] );
        
            checkEquals( ret, values1[i], "not the same" );
        }
        for( int i=0 ; i<testKeys2.length ; ++i ) {
            Integer ret = testFactory.get( testKeys2[i] );
        
            checkEquals( ret, testValues2[i], "not the same" );
        }
        
        //
        
        
        log.info( "removing a few" );
        
        Integer ret = testFactory.remove( testKeys1[1] );
        checkEquals( ret, values1[1], "not the same" );
        
        ret = testFactory.remove( testKeys2[1] );
        checkEquals( ret, testValues2[1], "not the same" );

        checkEquals( testFactory.size(), testKeys1.length + testKeys2.length - 1 -2, "wrong number of objects in factory" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        SmartFactoryTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SmartFactoryTest1( args );
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
    public SmartFactoryTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( SmartFactoryTest1.class ); // our own, private logger
}
