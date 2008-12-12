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

package org.infogrid.util.TEST;

import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

import java.util.NoSuchElementException;

/**
 * All CursorIterators are run through the same test data, which is factored out here.
 */
public abstract class AbstractCursorIteratorTest
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
            T []              testData,
            CursorIterator<T> iter,
            Log               log )
    {
        //
        
        log.info( "Check at the beginning" );
        
        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( testData.length ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( testData.length + 1 ), "Has too many nexts" );
        checkEquals( iter.peekNext(), testData[0], "wrong current element" );

        //
        
        log.info( "Check forward iteration" );

        for( int i=0 ; i<testData.length ; ++i ) {
            checkCondition( iter.hasNext(), "Not found next: " + i );
            
            Object found = iter.next();
            checkEquals( testData[i], found, "Not found element: " + i );
        }

        //
        
        log.info( "Check at the end" );

        checkCondition( !iter.hasNext(), "has next at the end" );
        checkCondition( iter.hasPrevious( testData.length ), "Does not have enough previous" );
        checkCondition( !iter.hasPrevious( testData.length + 1 ), "Has too many previous" );

        try {
            Object found = iter.peekNext();
            reportError( "Found element after end: " + found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }
        
        //
        
        log.info( "Check backward iteration" );

        for( int i=testData.length-1 ; i>=0 ; --i ) {
            checkCondition( iter.hasPrevious(), "Not found previous: " + i );
            
            Object found = iter.previous();
            checkEquals( testData[i], found, "Not found element: " + i );
        }
        
        //
        
        log.info( "Check again at the beginning" );
        
        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( testData.length ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( testData.length + 1 ), "Has too many nexts" );
        
        try {
            Object found = iter.peekPrevious();
            reportError( "Found element before beginning: " + found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }
        
        //
        
        log.info( "Move to element" );
        
        iter.moveToBefore( testData[ 4 ] ); // "e"
        checkEquals( iter.peekNext(),     testData[4], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[3], "wrong element found" );

        //
        
        log.info( "Move by positive number" );
        
        iter.moveBy( 2 ); // "g"
        checkEquals( iter.peekNext(),     testData[6], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[5], "wrong element found" );

        //
        
        log.info( "Move by negative number" );
        
        iter.moveBy( -3 ); // "d"
        checkEquals( iter.peekNext(),     testData[3], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[2], "wrong element found" );
        checkEquals( iter.peekNext(),     testData[3], "wrong element found" ); // make sure we can move about a bit, so repeat
        checkEquals( iter.peekPrevious(), testData[2], "wrong element found" );

        //
        
        log.info( "Copy" );
        
        CursorIterator<?> copy = iter.createCopy();
        
        checkEquals( iter.peekNext(), copy.peekNext(), "copied iterator in a different place" );
        checkEquals( iter.peekNext(),     testData[3], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[2], "wrong element found" );
        checkEquals( copy.peekNext(),     testData[3], "wrong element found" );
        checkEquals( copy.peekPrevious(), testData[2], "wrong element found" );
        
        //
        
        log.info( "Look backward" );

        Object [] before = iter.previous( 100 );

        checkEquals( before.length, 3, "wrong number of elements before" );
        for( int i=0 ; i<3 ; ++i ) {
            checkEquals( testData[i], before[ before.length - 1 - i ], "wrong data at index " + i );
        }

        //
        
        log.info( "Look forward" );
        
        Object [] after  = copy.next( 100 );

        checkEquals( after.length, testData.length - 3, "wrong number of elements after" );
        for( int i=3 ; i<testData.length ; ++i ) {
            checkEquals( testData[i], after[ i-3 ], "wrong data at index " + i );
        }
    }

    /**
     * Constructor.
     *
     * @throws Exception all kinds of things may go wrong in a test
     */
    protected AbstractCursorIteratorTest()
        throws
            Exception
    {
        super();
    }
}
