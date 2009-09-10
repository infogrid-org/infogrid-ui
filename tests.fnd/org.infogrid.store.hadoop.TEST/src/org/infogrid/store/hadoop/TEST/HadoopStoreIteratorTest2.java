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

package org.infogrid.store.hadoop.TEST;

import java.io.File;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.infogrid.store.TEST.AbstractStoreIteratorTest2;
import org.infogrid.store.hadoop.HadoopStore;
import org.infogrid.util.logging.Log;

/**
 * Tests the HadoopStoreIterator. See also SqlStoreTest4.
 */
public class HadoopStoreIteratorTest2
        extends
            AbstractStoreIteratorTest2
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        HadoopStoreIteratorTest2 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new HadoopStoreIteratorTest2( args );
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
    public HadoopStoreIteratorTest2(
            String [] args )
        throws
            Exception
    {
        super( localFileName( HadoopStoreIteratorTest2.class, "/ResourceHelper" ));

        File subdir1 = new File( AbstractHadoopStoreTest.TEST_SUBDIR_NAME );
        deleteFile( subdir1 );
        subdir1.mkdirs();

        Path subdir2 = new Path( AbstractHadoopStoreTest.TEST_SUBDIR_NAME );

        FileSystem fileSystem = new RawLocalFileSystem();
        fileSystem.setConf( new Configuration() );

        theTestStore = HadoopStore.create( fileSystem, subdir2 );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( HadoopStoreIteratorTest2.class);
}
