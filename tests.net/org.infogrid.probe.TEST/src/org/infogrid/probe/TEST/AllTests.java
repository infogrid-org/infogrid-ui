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

package org.infogrid.probe.TEST;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.testharness.AbstractTestGroup;
import org.infogrid.testharness.AbstractTestGroup.TestSpec;

/**
 * Runs all tests in this package.
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
        TestSpec [] probeTests = {

                new TestSpec(
                        ProbeTest1.class,
                        AbstractTest.fileSystemFileName( ProbeTest1.class, "ProbeTest1.xml" )),

                new TestSpec(
                        ProbeTest2.class,
                        AbstractTest.tempInputFileName( ProbeTest2.class, "test2-active.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest2.class, "ProbeTest2_1.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest2.class, "ProbeTest2_2.xml" )),

                new TestSpec(
                        ProbeTest3.class,
                        AbstractTest.tempInputFileName( ProbeTest3.class, "test3-active.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest3.class, "ProbeTest2_1.xml" ),
                        AbstractTest.fileSystemFileName( ProbeTest3.class, "ProbeTest2_2.xml" )),

                new TestSpec(
                        ProbeTest4.class,
                        AbstractTest.fileSystemFileName( ProbeTest4.class, "ProbeTest4.xml" )),

                new TestSpec(
                        ProbeTest5.class ),

//                new TestSpec( // FAILS: not implemented. See also XprisoTest11.
//                        ProbeTest6.class,
//                        AbstractTest.fileSystemFileName( ProbeTest6.class, "ProbeTest6.xml" )),
//
                new TestSpec(
                        ProbeTest7.class ),

// FIXME: ProbeTest8 seems to work, but spits out way too many warnings (but only when run in this test suite,
// not when run standalone). It may be related to failure to clean up Proxies to expired Shadows, and perhaps
// ProbeTest6. This needs more investigation but does not appear to be critical.\
//               new TestSpec(
//                        ProbeTest8.class ),

               new TestSpec(
                       ProbeTest9a.class ),
               new TestSpec(
                       ProbeTest9b.class )
        };


        TestSpec [] yadisTests = {

                // 2000 is less than half the default timeout time of org.infogrid.meshbase.net.proxy.AbstractProxyPolicy!DefaultRpcWaitDuration,
                // otherwise the HTTP request of discovery times out
                new TestSpec(
                        YadisTest1.class.getName() + " (delay 0)",
                        YadisTest1.class, "0" ),
                new TestSpec(
                        YadisTest1.class.getName() + " (delay 2000)",
                        YadisTest1.class, "2000" ),

                new TestSpec(
                        YadisTest2.class.getName() + " (delay 0)",
                        YadisTest2.class, "0" ),
                new TestSpec(
                        YadisTest2.class.getName() + " (delay 2000)",
                        YadisTest2.class, "2000" ),

                new TestSpec(
                        YadisTest3.class.getName() + " (delay 0)",
                        YadisTest3.class, "0" ),
                new TestSpec(
                        YadisTest3.class.getName() + " (delay 2000)",
                        YadisTest3.class, "2000" ),

                new TestSpec(
                        YadisTest4.class.getName() + " (delay 0)",
                        YadisTest4.class, "0" ),
                new TestSpec(
                        YadisTest4.class.getName() + " (delay 2000)",
                        YadisTest4.class, "2000" ),

                new TestSpec(
                        YadisTest5.class.getName() + " (delay 0)",
                        YadisTest5.class,
                        AbstractTest.fileSystemFileName( YadisTest5.class, "YadisTest5.xml" ),
                        "0" ),
                new TestSpec(
                        YadisTest5.class.getName() + " (delay 2000)",
                        YadisTest5.class,
                        AbstractTest.fileSystemFileName( YadisTest5.class, "YadisTest5.xml" ),
                        "2000"),

                new TestSpec(
                        YadisTest6.class.getName() + " (delay 0)",
                        YadisTest6.class,
                        AbstractTest.fileSystemFileName( YadisTest6.class, "YadisTest6.xml" ),
                        "0" ),

                new TestSpec(
                        YadisTest6.class.getName() + " (delay 2000)",
                        YadisTest6.class,
                        AbstractTest.fileSystemFileName( YadisTest6.class, "YadisTest6.xml" ),
                        "2000" ),

                new TestSpec(
                        YadisTest7.class.getName() + " (delay 0)",
                        YadisTest7.class,
                        AbstractTest.fileSystemFileName( YadisTest7.class, "YadisTest7.xml" ),
                        "0" ),

                new TestSpec(
                        YadisTest7.class.getName() + " (delay 2000)",
                        YadisTest7.class,
                        AbstractTest.fileSystemFileName( YadisTest7.class, "YadisTest7.xml" ),
                        "2000" ),
        };

        TestSpec [] writableProbeTests = {
                new TestSpec( // property updates
                            WritableProbeTest1.class ),

                new TestSpec( // blessing
                            WritableProbeTest2.class ),

                new TestSpec( // unblessing
                            WritableProbeTest3.class ),

                new TestSpec( // deletion
                            WritableProbeTest4.class ),


// FIXME: The remaining WritableProbeTests have not been written yet.
//                new TestSpec( // relationship delete
//                        WritableProbeTest5.class,
//                        noArgs ),
//                new TestSpec( // bless relationship
//                        WritableProbeTest6.class,
//                        noArgs ),
//                new TestSpec( // unbless relationship
//                        WritableProbeTest7.class,
//                        noArgs ),
//                new TestSpec( // create relationship between objects instantiated by probe but unrelated
//                        WritableProbeTest8.class,
//                        noArgs ),
//                new TestSpec( // create object -- BROKEN: need new API call to "createAndPush" to avoid triggering the non-local NetMeshObjectIdentifier exception
//                       .class,
//                        noArgs ),
//                new TestSpec( // create and relate object
//                        WritableProbeTest10.class,
//                        noArgs ),
        };

        TestSpec [] shadowTests = {
                new TestSpec(
                        ShadowTest1.class,
                        AbstractTest.fileSystemFileName( ShadowTest1.class, "ProbeTest1.xml" )),

                new TestSpec(
                        ShadowTest2.class ),

                new TestSpec(
                        ShadowTest3.class,
                        AbstractTest.fileSystemFileName( ShadowTest3.class, "ShadowTest3a.html" )), // ShadowTest3b.xml referenced from ShadowTest3a.html

                new TestSpec(
                        ShadowTest4.class ),

                new TestSpec(
                        ShadowTest5.class ),

                new TestSpec(
                        ShadowTest6.class ),

                new TestSpec(
                        ShadowTest7.class ),

                new TestSpec(
                        ShadowTest8.class ),

                new TestSpec(
                        ShadowTest9.class ),

                new TestSpec(
                        ProbeUpdateCalculatorTest1.class ),

                new TestSpec(
                        ProbeMatchTest1.class ),
        };

        TestSpec [] forwardReferenceTests = {

                new TestSpec(
                        ForwardReferenceTest1.class.getName() + " fast",
                        ForwardReferenceTest1.class,
                        AbstractTest.fileSystemFileName( ForwardReferenceTest1.class, "ForwardReferenceTest1_1.xml" ),
                                // ForwardReferenceRest1_2.xml included by reference from ForwardReferenceRest1.xml
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest1.class.getName() + " slow",
                        ForwardReferenceTest1.class,
                        AbstractTest.fileSystemFileName( ForwardReferenceTest1.class, "ForwardReferenceTest1_1.xml" ),
                                // ForwardReferenceRest1_2.xml included by reference from ForwardReferenceRest1.xml
                        "slow"),

                new TestSpec(
                        ForwardReferenceTest2.class.getName() + " fast",
                        ForwardReferenceTest2.class,
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest2.class.getName() + " slow",
                        ForwardReferenceTest2.class,
                        "slow"),

                new TestSpec(
                        ForwardReferenceTest3.class.getName() + " fast",
                        ForwardReferenceTest3.class,
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest3.class.getName() + " slow",
                        ForwardReferenceTest3.class,
                        "slow"),

                new TestSpec(
                        ForwardReferenceTest4.class.getName() + " fast",
                        ForwardReferenceTest4.class,
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest4.class.getName() + " slow",
                        ForwardReferenceTest4.class,
                        "slow"),

                new TestSpec(
                        ForwardReferenceTest5.class.getName() + " fast",
                        ForwardReferenceTest5.class,
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest5.class.getName() + " slow",
                        ForwardReferenceTest5.class,
                        "slow"),

                new TestSpec(
                        ForwardReferenceTest6.class.getName() + " fast",
                        ForwardReferenceTest6.class,
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest6.class.getName() + " slow",
                        ForwardReferenceTest6.class,
                        "slow"),

                new TestSpec(
                        ForwardReferenceTest7.class.getName() + " fast",
                        ForwardReferenceTest7.class,
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest7.class.getName() + " slow",
                        ForwardReferenceTest7.class,
                        "slow"),

                new TestSpec(
                        ForwardReferenceTest8.class.getName() + " fast",
                        ForwardReferenceTest8.class,
                        "fast"),
                new TestSpec(
                        ForwardReferenceTest8.class.getName() + " slow",
                        ForwardReferenceTest8.class,
                        "slow"),
       };

        runTests( probeTests );
        runTests( yadisTests );
        runTests( writableProbeTests );
        runTests( shadowTests );
        runTests( forwardReferenceTests );
    }
}

