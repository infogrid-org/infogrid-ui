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

package org.infogrid.store.filesystem.test;

import java.io.File;
import org.infogrid.store.IterableStore;
import org.infogrid.store.filesystem.FilesystemStore;
import org.infogrid.testharness.AbstractTest;

/**
 * Factors out common functionality of FilesystemStoreTests.
 */
public abstract class AbstractFilesystemStoreTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     * 
     * @param testClass the actual test Class
     */
    public AbstractFilesystemStoreTest(
            Class testClass )
    {
        super( localFileName( testClass, "/ResourceHelper" ));
        
        File subdir = new File( test_SUBDIR_NAME );
        
        theFilesystemStore = FilesystemStore.create( subdir );
    }

    /**
     * The FilesystemStore to be tested.
     */
    protected FilesystemStore theFilesystemStore;
    
    /**
     * The actual Store to be tested. This may or may not be pointed to theFilesystemStore
     * by subclasses.
     */
    protected IterableStore theTestStore;

    /**
     * The name of the subdirectory in which to store test data
     */
    public static final String test_SUBDIR_NAME = "build/test-FilesystemStore";

    /**
     * The EncodingId for the tests.
     */
    public static final String ENCODING_ID = "TestEncodingId";
}
