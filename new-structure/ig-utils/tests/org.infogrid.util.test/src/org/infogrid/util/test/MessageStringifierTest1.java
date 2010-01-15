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

import org.infogrid.util.logging.Log;

/**
 * CompoundStringifier tests. There are many "unchecked cast" exceptions, but somehow I can't figure it out better right now.
 */
public class MessageStringifierTest1
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
        MessageStringifierTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new MessageStringifierTest1( args );
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
    public MessageStringifierTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( MessageStringifierTest1.class  ); // our own, private logger
    

    static Dataset [] datasets = {
            new StringDataset(
                    "One",
                    "Abc {0,int}",
                    new Object[] { 12 },
                    2,
                    "Abc 12" ),
            new StringDataset(
                    "Two",
                    "Abc {0,int} def{1,string}",
                    new Object[] { 12, "XXX-X" },
                    4,
                    "Abc 12 defXXX-X" ),
            new StringDataset(
                    "Three",
                    "Abc {0,int} def",
                    new Object[] { 987 },
                    3,
                    "Abc 987 def" ),
            new StringDataset(
                    "Four",
                    "Abc {0,int} def{2,string}  ghi kl{0,int}mno {1,int}",
                    new Object[] { 0, 111, "222" },
                    8,
                    "Abc 0 def222  ghi kl0mno 111" ),
    };
}
