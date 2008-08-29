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

package org.infogrid.store.sql.TEST;

import org.infogrid.testharness.AbstractTestGroup;

/**
 * Tests the SQL implementation of Store.
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
        String encryption = "DES";

        TestSpec [] tests = {
                new TestSpec( SqlStoreTest1.class ),
                new TestSpec( SqlStoreTest2.class ),
                new TestSpec( SqlStoreTest3.class ),
                new TestSpec( SqlStoreTest4.class ),
                new TestSpec( SqlStoreTest5.class ),
                new TestSpec( SqlStoreTest6.class ),

                new TestSpec( SqlKeyStoreTest1.class, "test-keystore.key", "asdfgh" ),
                new TestSpec( SqlStorePerformanceTest1.class ),

                new TestSpec( EncryptedSqlStoreTest1.class, encryption ),
                new TestSpec( EncryptedSqlStoreTest2.class, encryption ),
                new TestSpec( EncryptedSqlStoreTest3.class, encryption ),

                new TestSpec( EncryptedSqlStorePerformanceTest1.class, encryption )
        };

        runTests( tests );
    }
}
