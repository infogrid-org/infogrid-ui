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

package org.infogrid.util.test;

import org.infogrid.testharness.AbstractTestGroup;
import org.infogrid.testharness.AbstractTestGroup.TestSpec;

/**
 * Runs all tests.
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
                new TestSpec( ArrayHelperTest1.class ),

                new TestSpec( DereferencingIteratorTest1.class ),

                new TestSpec( ArrayCursorIteratorTest1.class ),
                new TestSpec( ArrayCursorIteratorTest2.class ),
                new TestSpec( ArrayCursorIteratorTest3.class ),

                new TestSpec( ArrayListCursorIteratorTest1.class ),
                new TestSpec( ArrayListCursorIteratorTest2.class ),
                new TestSpec( ArrayListCursorIteratorTest3.class ),

                new TestSpec( FileTreeFacadeCursorIteratorTest1.class ),
                new TestSpec( FileTreeFacadeCursorIteratorTest2.class ),
                // no FileTreeFacadeCursorIteratorTest3.class: can't have an empty FileTreeFacade

                new TestSpec( FilteringCursorIteratorTest1.class ),
                new TestSpec( FilteringCursorIteratorTest2a.class ),
                new TestSpec( FilteringCursorIteratorTest2b.class ),
                new TestSpec( FilteringCursorIteratorTest3a.class ),
                new TestSpec( FilteringCursorIteratorTest3b.class ),

                new TestSpec( PagingCursorIteratorTest1.class ),
                new TestSpec( PagingCursorIteratorTest2a.class ),
                new TestSpec( PagingCursorIteratorTest2b.class ),
                new TestSpec( PagingCursorIteratorTest2c.class ),
                // 3a case does not exist
                new TestSpec( PagingCursorIteratorTest3b.class ),
                // 3c case does not exist

                new TestSpec( CompositeCursorIteratorTest1.class ),
                new TestSpec( CompositeCursorIteratorTest2.class ),
                new TestSpec( CompositeCursorIteratorTest3.class ),

                new TestSpec( ReturnSynchronizerTest1.class ),

                new TestSpec( SmartFactoryTest1.class ),
                new TestSpec( SmartFactoryTest2.class ),
                new TestSpec( SmartFactoryTest3.class ),
                new TestSpec( SmartFactoryTest4.class ),

                new TestSpec( StringStringifierTest1.class ),
                new TestSpec( IntegerStringifierTest1.class ),
                new TestSpec( IntegerStringifierTest2.class ),
                new TestSpec( IntegerStringifierTest3.class ),
                new TestSpec( LongStringifierTest1.class ),
                new TestSpec( DoubleStringifierTest1.class ),
                new TestSpec( MessageStringifierTest1.class ),
                new TestSpec( MessageStringifierTest2.class ),
                new TestSpec( MessageStringifierTest3.class ),
                new TestSpec( MessageStringifierTest4.class ),
                new TestSpec( ArrayStringifierTest1.class ),

                new TestSpec( UrlEncodingTest1.class ),
        };

        runTests( tests );
    }
}

