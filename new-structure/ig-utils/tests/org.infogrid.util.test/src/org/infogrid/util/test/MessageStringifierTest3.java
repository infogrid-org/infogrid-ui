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

import org.infogrid.util.logging.Log;

/**
 * CompoundStringifier tests for fixed-length hex's.
 */
public class MessageStringifierTest3
        extends
            AbstractMessageStringifierTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may happen in a test
     */
    @SuppressWarnings(value={"unchecked"})
    public void run()
        throws
            Exception
    {
        for( int i=0 ; i<datasets.length ; ++i ) {
            Dataset current = datasets[i];

            runOne( current, true );
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
        MessageStringifierTest3 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new MessageStringifierTest3( args );
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
    public MessageStringifierTest3(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( MessageStringifierTest3.class  ); // our own, private logger


    static Dataset [] datasets = {
            new StringDataset(
                    "One",
                    "#{0,hex2}",
                    new Object[] { 0xfe },
                    2,
                    "#fe" ),
            new StringDataset(
                    "Two",
                    "#{0,hex2}{1,hex2}",
                    new Object[] { 0xab, 0xcd },
                    3,
                    "#abcd" ),
            new StringDataset(
                    "Three",
                    "#{0,hex2}{1,hex2}{2,hex2}",
                    new Object[] { 0x12, 0x34, 0x56 },
                    4,
                    "#123456" ),
    };
}
