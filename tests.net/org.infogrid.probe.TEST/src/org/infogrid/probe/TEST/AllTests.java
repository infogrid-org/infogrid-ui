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

package org.infogrid.probe.TEST;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.testharness.AbstractTestGroup;
import org.infogrid.testharness.AbstractTestGroup.TestSpec;

/**
 * Tests the Probe framework.
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
        String [] noArgs = {};
        
        TestSpec [] tests = {

                new TestSpec(
                        ProbeTest1.class,
                        new String [] {
                                AbstractTest.fileSystemFile( ProbeTest1.class, "ProbeTest1.xml" )
                        } ),
                new TestSpec(
                        ProbeTest2.class,
                        new String[] {
                                AbstractTest.tempInputFile( ProbeTest2.class, "test2-active.xml" ),
                                AbstractTest.fileSystemFile( ProbeTest2.class, "ProbeTest2_1.xml" ),
                                AbstractTest.fileSystemFile( ProbeTest2.class, "ProbeTest2_2.xml" )
                        } ),
                new TestSpec(
                        ProbeTest3.class,
                        new String[] {
                                AbstractTest.tempInputFile( ProbeTest3.class, "test3-active.xml" ),
                                AbstractTest.fileSystemFile( ProbeTest3.class, "ProbeTest2_1.xml" ),
                                AbstractTest.fileSystemFile( ProbeTest3.class, "ProbeTest2_2.xml" )
                        } ),
                new TestSpec(
                        ProbeTest4.class,
                        new String [] {
                                AbstractTest.fileSystemFile( ProbeTest4.class, "ProbeTest4.xml" )
                        } ),
                new TestSpec(
                        ProbeTest5.class,
                        noArgs ),
//                new TestSpec( // FAILS (minor). Requires memory debugger to make progress. See also ProbeTest6
//                        ProbeTest6.class,
//                        new String [] {
//                                AbstractTest.fileSystemFile( ProbeTest6.class, "ProbeTest6.xml" )
//                        } ),
                new TestSpec(
                        ProbeTest7.class,
                        noArgs ),

// FIXME: ProbeTest8 seems to work, but spits out way too many warnings (but only when run in this test suite,
// not when run standalone). It may be related to failure to clean up Proxies to expired Shadows, and perhaps
// ProbeTest6. This needs more investigation but does not appear to be critical.
//                new TestSpec(
//                        ProbeTest8.class,
//                        noArgs ),

                new TestSpec(
                        YadisTest1.class,
                        noArgs ),
                new TestSpec(
                        YadisTest2.class,
                        noArgs ),
                new TestSpec(
                        YadisTest3.class,
                        noArgs ),
                new TestSpec(
                        YadisTest4.class,
                        noArgs ),
                new TestSpec(
                        YadisTest5.class,
                        new String [] {
                                AbstractTest.fileSystemFile( YadisTest5.class, "YadisTest5.xml" )
                        } ),

            new TestSpec( // property updates
                        WritableProbeTest1.class,
                        noArgs ),
            new TestSpec( // blessing
                        WritableProbeTest2.class,
                        noArgs ),
            new TestSpec( // unblessing
                        WritableProbeTest3.class,
                        noArgs ),
            new TestSpec( // deletion
                        WritableProbeTest4.class,
                        noArgs ),

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


                new TestSpec(
                        ShadowTest1.class,
                        new String [] {
                                AbstractTest.fileSystemFile( ShadowTest1.class, "ProbeTest1.xml" )
                        } ),
                new TestSpec(
                        ShadowTest2.class,
                        noArgs ),
                new TestSpec(
                        ShadowTest3.class,
                        new String [] {
                                AbstractTest.fileSystemFile( ShadowTest3.class, "ShadowTest3a.html" ) // ShadowTest3b.xml referenced from ShadowTest3a.html
                        } ),
                new TestSpec(
                        ShadowTest4.class,
                        noArgs ),
                new TestSpec(
                        ShadowTest5.class,
                        noArgs ),
                new TestSpec(
                        ShadowTest6.class,
                        noArgs ),
                new TestSpec(
                        ShadowTest7.class,
                        noArgs ),

                new TestSpec(
                        ProbeUpdateCalculatorTest1.class,
                        noArgs ),

                new TestSpec(
                        ForwardReferenceTest1.class,
                        new String [] {
                                AbstractTest.fileSystemFile( ForwardReferenceTest1.class, "ForwardReferenceTest1_1.xml" )
                                // ForwardReferenceRest1_2.xml included by reference from ForwardReferenceRest1.xml
                        } ),
                new TestSpec(
                        ForwardReferenceTest2.class,
                        noArgs ),
                new TestSpec(
                        ForwardReferenceTest3.class,
                        noArgs ),

                new TestSpec(
                        ProbeMatchTest1.class,
                        noArgs ),
        };

        runTests( tests );
    }
}

