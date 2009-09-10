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

package org.infogrid.store.filesystem.TEST;

import org.infogrid.testharness.AbstractTestGroup;

/**
 * Tests the filesystem implementation of Store.
 */
public abstract class AllTests
        extends
            AbstractTestGroup
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        TestSpec [] tests = {
                new TestSpec( FilesystemStoreTest1.class ),
                new TestSpec( FilesystemStoreIteratorTest1.class ),
                new TestSpec( FilesystemStoreIteratorTest2.class ),
                new TestSpec( FilesystemStoreIteratorTest3.class ),
        };

        runTests( tests );
    }
}
