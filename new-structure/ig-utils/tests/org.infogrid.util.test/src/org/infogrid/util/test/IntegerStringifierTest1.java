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

import java.util.Iterator;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.IntegerStringifier;
import org.infogrid.util.text.StringifierParsingChoice;

/**
 * Tests IntegerStringifier.
 */
public class IntegerStringifierTest1
        extends
            AbstractTest
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
        //

        log.info( "IntegerStringifier" );
        
        int                data1a = 15243;
        IntegerStringifier str1   = IntegerStringifier.create( );
        String             res1a  = str1.format( null, data1a, null );
        
        checkEquals( String.valueOf( data1a ), res1a, "not the same" );

        Integer temp = str1.unformat( res1a, null );
        checkEquals( temp.intValue(), data1a, "Wrong found value" );

        Iterator<StringifierParsingChoice<Integer>> iter1    = str1.parsingChoiceIterator( res1a, 0, res1a.length(), Integer.MAX_VALUE, true, null );
        StringifierParsingChoice<Integer> []        choices1 = (StringifierParsingChoice<Integer> []) ArrayHelper.copyIntoNewArray( iter1, StringifierParsingChoice.class );
        
        checkEquals( choices1.length, 1, "Wrong number of choices" );
        checkEquals( choices1[0].getStartIndex(), 0,              "Wrong start index" );
        checkEquals( choices1[0].getEndIndex(),   res1a.length(), "Wrong end index" );

        String res1b = "123"; // something entirely different
        temp = str1.unformat( res1b, null );
        checkEquals( temp.toString(), res1b, "Wrong found value" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        IntegerStringifierTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new IntegerStringifierTest1( args );
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
    public IntegerStringifierTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( IntegerStringifierTest1.class ); // our own, private logger
}
