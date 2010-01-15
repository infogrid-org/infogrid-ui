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

import java.io.File;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.FileTreeFacade;

/**
 * Tests the TreeFacadeCursorIterator.
 */
public class FileTreeFacadeCursorIteratorTest2
        extends
            AbstractCursorIteratorTest2
{
    /**
     * Run the test.
     *
     * @throws Exception all kinds of things may go wrong in a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "create a test hierarchy" );

        File top = new File( "build/top" );
        deleteFile( top ); // cleanup

        top.mkdirs();

        FileTreeFacade facade = FileTreeFacade.create( top );

        //

        log.info( "testing iterator" );

        CursorIterator<File> iter1 = facade.iterator();

        super.runWith( top, iter1, log );
    }


    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        FileTreeFacadeCursorIteratorTest2 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new FileTreeFacadeCursorIteratorTest2( args );
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
     * @throws Exception all kinds of things may go wrong in a test
     */
    public FileTreeFacadeCursorIteratorTest2(
            String [] args )
        throws
            Exception
    {
        super();
    }

    private static final Log log = Log.getLogInstance( FileTreeFacadeCursorIteratorTest2.class  ); // our own, private logger
}
