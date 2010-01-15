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

package org.infogrid.probe.store.test;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.testharness.AbstractTestGroup;

/**
 * Tests the Store implementation of MeshBase.
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

                new TestSpec( ShadowMeshBaseSerializationTest1.class ),

                new TestSpec( StoreShadowMeshBaseTest1.class ),

                new TestSpec( StoreShadowMeshBaseTest2.class ),

                new TestSpec(
                        StoreShadowMeshBaseTest3.class,
                        AbstractTest.tempInputFileName( StoreShadowMeshBaseTest3.class, "test3-active.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest3.class, "StoreProbeTest3_1.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest3.class, "StoreProbeTest3_2.xml" )),

                new TestSpec(
                        StoreShadowMeshBaseTest4.class,
                        AbstractTest.tempInputFileName( StoreShadowMeshBaseTest4.class, "test4-1.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest4.class, "StoreShadowMeshBaseTest4_1a.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest4.class, "StoreShadowMeshBaseTest4_1b.xml" )),

                new TestSpec(
                        StoreShadowMeshBaseTest5.class,
                        AbstractTest.tempInputFileName( StoreShadowMeshBaseTest5.class, "test5-1.xml" ),
                        AbstractTest.tempInputFileName( StoreShadowMeshBaseTest5.class, "test5-2.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest5.class, "StoreShadowMeshBaseTest5_1a.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest5.class, "StoreShadowMeshBaseTest5_2a.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest5.class, "StoreShadowMeshBaseTest5_1b.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest5.class, "StoreShadowMeshBaseTest5_2b.xml" )),

                new TestSpec(
                        StoreShadowMeshBaseTest6.class,
                        AbstractTest.tempInputFileName( StoreShadowMeshBaseTest6.class, "test6-1.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest6.class, "StoreShadowMeshBaseTest6_1a.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest6.class, "StoreShadowMeshBaseTest6_1b.xml" )),

                new TestSpec(
                        StoreShadowMeshBaseTest7.class ),

                new TestSpec(
                        StoreShadowMeshBaseTest8.class,
                        AbstractTest.tempInputFileName( StoreShadowMeshBaseTest8.class, "test8-1.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest8.class, "StoreShadowMeshBaseTest8_1a.xml" ),
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest8.class, "StoreShadowMeshBaseTest8_1b.xml" )),
                        
                new TestSpec(
                        StoreShadowMeshBaseTest9.class,
                        AbstractTest.fileSystemFileName( StoreShadowMeshBaseTest9.class, "StoreShadowMeshBaseTest9.xml" ))
                        
//                new TestSpec(
//                        WritableProbeStoreShadowMeshBaseTest1.class ) // FIXME to be written
        };

        runTests( tests );
    }
}