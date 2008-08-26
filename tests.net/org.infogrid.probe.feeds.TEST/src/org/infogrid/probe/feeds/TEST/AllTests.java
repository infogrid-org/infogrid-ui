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

package org.infogrid.probe.feeds.TEST;

import org.infogrid.probe.feeds.TEST.atom.AtomTest1;
import org.infogrid.probe.feeds.TEST.atom.AtomTest2;
import org.infogrid.probe.feeds.TEST.rss.RssTest1;
import org.infogrid.probe.feeds.TEST.rss.RssTest2;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.testharness.AbstractTestGroup;

/**
 * Tests the Feed Probes.
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

                new TestSpec(
                        AtomTest1.class,
                        AbstractTest.fileSystemFile( AtomTest1.class, "AtomTest1.xml" )),

                new TestSpec(
                        AtomTest2.class,
                        AbstractTest.fileSystemFile( AtomTest2.class, "AtomTest2.xml" )),

                new TestSpec(
                        RssTest1.class,
                        AbstractTest.fileSystemFile( RssTest1.class, "RssTest1.xml" )),

                new TestSpec(
                        RssTest2.class,
                        AbstractTest.fileSystemFile( RssTest2.class, "RssTest2.xml" ))
        };

        runTests( tests );
    }
}

