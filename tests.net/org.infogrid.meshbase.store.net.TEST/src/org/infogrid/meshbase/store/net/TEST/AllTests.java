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

package org.infogrid.meshbase.store.net.TEST;

import org.infogrid.testharness.AbstractTestGroup;
import org.infogrid.testharness.AbstractTestGroup.TestSpec;

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
        String [] subArgs1 = new String[] { "123" };
        String [] emptyArgs  = new String[] {};

        TestSpec [] tests = {
                new TestSpec( ProxySerializationTest1.class, emptyArgs ),
                new TestSpec( StoreNetMeshBaseTest1.class,   subArgs1 ),
                new TestSpec( StoreNetMeshBaseTest2.class,   emptyArgs ),
                new TestSpec( StoreNetMeshBaseTest3.class,   emptyArgs ),
                new TestSpec( StoreNetMeshBaseTest4.class,   emptyArgs ),
                new TestSpec( StoreNetMeshBaseTest5.class,   emptyArgs ),
                new TestSpec( StoreNetMeshBaseTest6.class,   emptyArgs )
        };

        runTests( tests );
    }
}
