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
 * Same as MessageStringifierTest3 for long instead of int.
 */
public class MessageStringifierTest4
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
        MessageStringifierTest4 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new MessageStringifierTest4( args );
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
    public MessageStringifierTest4(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( MessageStringifierTest4.class  ); // our own, private logger


    static Dataset [] datasets = {
            new StringDataset(
                    "One",
                    "#{0,longhex4}",
                    new Object[] { 0xfedcL },
                    2,
                    "#fedc" ),
            new StringDataset(
                    "Two",
                    "#{0,longhex4}{1,longhex4}",
                    new Object[] { 0xabcdL, 0xcdefL },
                    3,
                    "#abcdcdef" ),
            new StringDataset(
                    "Three",
                    "#{0,longhex4}{1,longhex4}{2,longhex4}",
                    new Object[] { 0x1234L, 0x5678L, 0x9abcL },
                    4,
                    "#123456789abc" ),
    };
}
