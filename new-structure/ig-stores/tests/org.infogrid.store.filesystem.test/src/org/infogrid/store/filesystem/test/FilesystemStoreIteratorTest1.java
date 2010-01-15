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

package org.infogrid.store.filesystem.test;

import java.io.File;
import org.infogrid.store.test.AbstractStoreIteratorTest1;
import org.infogrid.store.filesystem.FilesystemStore;
import org.infogrid.util.logging.Log;

/**
 * Tests the FilesystemStoreIterator.
 */
public class FilesystemStoreIteratorTest1
        extends
            AbstractStoreIteratorTest1
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        FilesystemStoreIteratorTest1 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new FilesystemStoreIteratorTest1( args );
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
     * @throws Exception all sorts of things may go wrong in a test
     */
    public FilesystemStoreIteratorTest1(
            String [] args )
        throws
            Exception
    {
        super( localFileName( FilesystemStoreIteratorTest1.class, "/ResourceHelper" ));
        
        File subdir = new File( AbstractFilesystemStoreTest.test_SUBDIR_NAME );
        deleteFile( subdir );
        subdir.mkdirs();

        theTestStore = FilesystemStore.create( subdir );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( FilesystemStoreIteratorTest1.class);
}
