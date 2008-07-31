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

package org.infogrid.kernel.net.TEST.xpriso;

import org.infogrid.testharness.AbstractTestGroup;

/**
 *
 */
public class AllTests
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
                new TestSpec( XprisoMessageSerializationTest1.class ),

                new TestSpec( XprisoTest1.class ),
                new TestSpec( XprisoTest1a.class ),
                new TestSpec( XprisoTest1b.class ),
                new TestSpec( XprisoTest2a.class ),
                new TestSpec( XprisoTest2b.class ),
                new TestSpec( XprisoTest3.class ),
                new TestSpec( XprisoTest4.class ),
                new TestSpec( XprisoTest5.class ),
                new TestSpec( XprisoTest6.class ),
                new TestSpec( XprisoTest6_5a.class ),
                new TestSpec( XprisoTest6_5b.class ),
                new TestSpec( XprisoTest6_5c.class ),
                new TestSpec( XprisoTest7.class ),
                new TestSpec( XprisoTest8.class ),
                new TestSpec( XprisoTest9.class ),
                new TestSpec( XprisoTest10.class ),
                // new TestSpec( XprisoTest11.class ), // FAILS (minor). Requires memory debugger to make progress. See also ProbeTest6
                new TestSpec( XprisoTest12.class ),
                new TestSpec( XprisoTest13.class ),
                new TestSpec( XprisoTest14.class ),
                // new TestSpec( XprisoTest15.class ) // FIXME, to be written: accessLocally to two replicas in different NetMeshBases in parallel
        };

        runTests( tests );
    }
}

