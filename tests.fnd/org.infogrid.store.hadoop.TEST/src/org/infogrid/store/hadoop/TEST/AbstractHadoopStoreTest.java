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

package org.infogrid.store.hadoop.TEST;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.infogrid.store.IterableStore;
import org.infogrid.store.hadoop.HadoopStore;
import org.infogrid.testharness.AbstractTest;

/**
 * Factors out common functionality of HadoopStoreTests.
 */
public abstract class AbstractHadoopStoreTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     * 
     * @param testClass the actual test Class
     */
    public AbstractHadoopStoreTest(
            Class testClass )
    {
        super( localFileName( testClass, "/ResourceHelper" ),
               localFileName( testClass, "/Log.properties" ));
        
        
        Path subdir = new Path( TEST_SUBDIR_NAME );
        
        FileSystem fileSystem = new RawLocalFileSystem();
        fileSystem.setConf( new Configuration() );
        
        theHadoopStore = HadoopStore.create( fileSystem, subdir );
    }

    /**
     * The HadoopStore to be tested.
     */
    protected HadoopStore theHadoopStore;
    
    /**
     * The actual Store to be tested. This may or may not be pointed to theHadoopStore
     * by subclasses.
     */
    protected IterableStore theTestStore;

    /**
     * The name of the subdirectory in Hadoop in which to store 
     */
    public static final String TEST_SUBDIR_NAME = "build/test-HadoopStore";

    /**
     * The EncodingId for the tests.
     */
    public static final String ENCODING_ID = "TestEncodingId";
}
