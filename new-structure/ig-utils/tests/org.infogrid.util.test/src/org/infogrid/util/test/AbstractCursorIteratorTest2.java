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

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

import java.util.NoSuchElementException;

/**
 * Tests one-element CursorIterators.
 * All CursorIterators are run through the same test sequence, which is factored out here.
 */
public abstract class AbstractCursorIteratorTest2
        extends
            AbstractTest
{
    /**
     * Run the test.
     *
     * @param testData the provided test data
     * @param iter the to-be-tested iterator
     * @param log the Logger to use
     * @param <T> the type of Iterator to test
     */
    protected <T> void runWith(
            T                 testData,
            CursorIterator<T> iter,
            Log               log )
    {
        //

        log.info( "Check at the beginning" );

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( 1 ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( 2 ), "Has too many nexts" );
        checkEquals( iter.peekNext(), testData, "wrong current element" );

        //

        log.info( "Check forward iteration" );

        checkCondition( iter.hasNext(), "Not found next" );

        Object found = iter.next();
        checkEquals( testData, found, "Not found element" );

        //

        log.info( "Check at the end" );

        checkCondition( !iter.hasNext(), "has next at the end" );
        checkCondition( iter.hasPrevious( 1 ), "Does not have enough previous" );
        checkCondition( !iter.hasPrevious( 2 ), "Has too many previous" );
        checkEquals( iter.peekPrevious(), testData, "wrong last element" );

        try {
            Object found2 = iter.peekNext();
            reportError( "Found element after end", found2 );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Check backward iteration" );

        checkCondition( iter.hasPrevious(), "Not found previous" );

        Object found3 = iter.previous();
        checkEquals( testData, found3, "Not found element" );

        //

        log.info( "Check again at the beginning" );

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( 1 ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( 2 ), "Has too many nexts" );

        try {
            Object found4 = iter.peekPrevious();
            reportError( "Found element before beginning", found4 );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Move to element" );

        iter.moveToBefore( testData ); // "e"
        checkEquals( iter.peekNext(),     testData, "wrong element found" );
        checkCondition( !iter.hasPrevious(), "previous found" );

        //

        log.info( "Copy" );

        CursorIterator<?> copy = iter.createCopy();

        checkEquals( iter.peekNext(), copy.peekNext(), "copied iterator in a different place" );

        //

        log.info( "Go to past last" );

        iter.moveToAfterLast();

        checkCondition( !iter.hasNext(), "has next at the end" );
        checkCondition( iter.hasPrevious( 1 ), "Does not have enough previous" );
        checkCondition( !iter.hasPrevious( 2 ), "Has too many previous" );
        checkEquals( iter.peekPrevious(), testData, "wrong last element" );

        try {
            Object found5 = iter.peekNext();
            reportError( "Found element after end", found5 );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Go before first" );

        iter.moveToBeforeFirst();

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( 1 ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( 2 ), "Has too many nexts" );
        checkEquals( iter.peekNext(), testData, "wrong current element" );

        try {
            Object found6 = iter.peekPrevious();
            reportError( "Found element before beginning", found6 );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }
    }

    /**
     * Constructor.
     *
     * @throws Exception all kinds of things may go wrong in a test
     */
    protected AbstractCursorIteratorTest2()
        throws
            Exception
    {
        super();
    }
}
