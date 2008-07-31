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

package org.infogrid.kernel.net.TEST.mesh.externalized;

import org.infogrid.testharness.AbstractTestGroup;

/**
 * Tests the externalization of NetMeshObjectIdentifiers and NetMeshBaseIdentifiers.
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
        String [] emptyArgs  = new String[] {};

        TestSpec [] tests = {
                new TestSpec( NetSerializerTest1.class, emptyArgs ),
                new TestSpec( NetSerializerTest2.class, emptyArgs ),
        };

        runTests( tests );
    }
}
