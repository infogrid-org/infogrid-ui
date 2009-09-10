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

package org.infogrid.store.TEST;

import java.util.NoSuchElementException;
import org.infogrid.store.IterableStore;
import org.infogrid.store.StoreValue;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Tests any StoreIterator. Subclasses define the Store implementation.
 */
public abstract class AbstractStoreIteratorTest1
        extends
            AbstractTest
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    public void run()
        throws
            Exception
    {
        //

        log.info( "Deleting old database and creating the Store" );

        theTestStore.initializeHard();

        //

        log.info( "Inserting data" );

        for( int i=0 ; i<testData.length ; ++i ) {
            StoreValue current = testData[i];
            theTestStore.put(
                    current.getKey(),
                    current.getEncodingId(),
                    current.getTimeCreated(),
                    current.getTimeUpdated(),
                    current.getTimeRead(),
                    current.getTimeExpires(),
                    current.getData() );
        }

        CursorIterator<StoreValue> iter = theTestStore.iterator();

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

            StoreValue found = iter.next();
            checkEquals( testData[i], found, "Not found element: " + i );
        }

        //

        log.info( "Check at the end" );

        checkCondition( !iter.hasNext(), "has next at the end" );
        checkCondition( iter.hasPrevious( testData.length ), "Does not have enough previous" );
        checkCondition( !iter.hasPrevious( testData.length + 1 ), "Has too many previous" );
        checkEquals( iter.peekPrevious(), testData[testData.length-1], "wrong last element" );

        try {
            StoreValue found = iter.peekNext();
            reportError( "Found element after end", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Check backward iteration" );

        for( int i=testData.length-1 ; i>=0 ; --i ) {
            checkCondition( iter.hasPrevious(), "Not found previous: " + i );

            StoreValue found = iter.previous();
            checkEquals( testData[i], found, "Not found element: " + i );
        }

        //

        log.info( "Check again at the beginning" );

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( testData.length ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( testData.length + 1 ), "Has too many nexts" );

        try {
            StoreValue found = iter.peekPrevious();
            reportError( "Found element before beginning", found );
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

        CursorIterator<StoreValue> copy = iter.createCopy();

        checkEquals( iter.peekNext(), copy.peekNext(), "copied iterator in a different place" );
        checkEquals( iter.peekNext(),     testData[3], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[2], "wrong element found" );
        checkEquals( copy.peekNext(),     testData[3], "wrong element found" );
        checkEquals( copy.peekPrevious(), testData[2], "wrong element found" );

        //

        log.info( "Look backward" );

        StoreValue [] before = iter.previous( 100 );

        checkEquals( before.length, 3, "wrong number of elements before" );
        for( int i=0 ; i<3 ; ++i ) {
            checkEquals( testData[i], before[ before.length-1-i ], "wrong data at index " + i );
        }

        //

        log.info( "Look forward" );

        StoreValue [] after  = copy.next( 100 );

        checkEquals( after.length, testData.length - 3, "wrong number of elements after" );
        for( int i=3 ; i<testData.length ; ++i ) {
            checkEquals( testData[i], after[ i-3 ], "wrong data at index " + i );
        }

        //

        log.info( "Go to past last" );

        iter.moveToAfterLast();

        checkCondition( !iter.hasNext(), "has next at the end" );
        checkCondition( iter.hasPrevious( testData.length ), "Does not have enough previous" );
        checkCondition( !iter.hasPrevious( testData.length + 1 ), "Has too many previous" );
        checkEquals( iter.peekPrevious(), testData[testData.length-1], "wrong last element" );

        try {
            StoreValue found = iter.peekNext();
            reportError( "Found element after end", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Go before first" );

        iter.moveToBeforeFirst();

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( testData.length ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( testData.length + 1 ), "Has too many nexts" );
        checkEquals( iter.peekNext(), testData[0], "wrong current element" );

        try {
            StoreValue found = iter.peekPrevious();
            reportError( "Found element before beginning", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Go to the middle, and get all next elements" );

        iter.moveToBeforeFirst();
        iter.moveBy( 2 );
        checkCondition( iter.hasNext( testData.length - 2 ), "not enough elements left" );
        checkCondition( !iter.hasNext( testData.length - 1 ), "too many elements left" );
        checkEquals( iter.next( testData.length - 2 ).length, testData.length - 2, "elements not found" );

        //

        log.info( "Go to the middle, and get all previous elements" );

        iter.moveToBeforeFirst();
        iter.moveBy( 2 );
        checkCondition( iter.hasPrevious( 2 ), "not enough elements left" );
        checkCondition( !iter.hasPrevious( 3 ), "too many elements left" );
        checkEquals( iter.previous( 2 ).length, 2, "elements not found" );

        //

        log.info( "Go to the middle, and move to the end" );

        iter.moveToBeforeFirst();
        iter.moveBy( 2 );
        checkCondition( iter.hasNext( testData.length - 2 ), "not enough elements left" ); // tested earlier
        iter.moveBy( testData.length - 2 );
        checkCondition( !iter.hasNext(), "has next at the end" );
        checkCondition( iter.hasPrevious(), "Does not have previous" );

        //

        log.info( "Go to the middle, and move to the start" );

        iter.moveToBeforeFirst();
        iter.moveBy( 2 );
        checkCondition( iter.hasPrevious( 2 ), "not enough elements left" ); // tested earlier
        iter.moveBy( -2 );
        checkCondition( iter.hasNext(), "Does not have next at the beginning" );
        checkCondition( !iter.hasPrevious(), "Has previous at the beginning" );
    }

    /**
     * Constructor if we do not have a special resource file.
     */
    protected AbstractStoreIteratorTest1()
    {
        super();

        // subclass has to initialize theTestStore
    }

    /**
     * Constructor if we have a special resource file.
     *
     * @param nameOfResourceHelperFile the name of the resource file
     */
    protected AbstractStoreIteratorTest1(
            String nameOfResourceHelperFile )
    {
        super( nameOfResourceHelperFile );

        // subclass has to initialize theTestStore
    }

    // Our Logger
    private static Log log = Log.getLogInstance( AbstractStoreIteratorTest1.class );

    /**
     * The actual Store to be tested. This may or may not be pointed to theSqlStore
     * by subclasses.
     */
    protected IterableStore theTestStore;

    /**
     * Test data.
     */
    protected static final long now = System.currentTimeMillis();
    protected static final StoreValue[] testData = new StoreValue[] {
        new StoreValue( "a", "enc1",           12345L, 67890L, 10111213L,     -1L, "some data".getBytes() ),
        new StoreValue( "b", "other encoding",    11L,    22L,       33L,     12L, "some longer data, but not very long".getBytes() ),
        new StoreValue( "c", "Shakespeare's collected cucumbers", now,    now+1,  now+10000L, 99999L, "other data".getBytes() ),
        new StoreValue( "d", "enc1", 0L, 0L, 0L, -1L, "aliergaierbg".getBytes() ),
        new StoreValue( "e", "enc1", 0L, 0L, 0L, -1L, "aqertghaqer".getBytes() ),
        new StoreValue( "f", "enc1", 0L, 0L, 0L, -1L, "qewrgqergqer".getBytes() ),
        new StoreValue( "g", "enc1", 0L, 0L, 0L, -1L, "zsdbgadgb".getBytes() ),
        new StoreValue( "h", "enc1", 0L, 0L, 0L, -1L, "afgae".getBytes() ),
        new StoreValue( "i", "enc1", 0L, 0L, 0L, -1L, "qerg".getBytes() ),
        new StoreValue( "j", "enc1", 0L, 0L, 0L, -1L, "adfga".getBytes() ),
        new StoreValue( "k", "enc1", 0L, 0L, 0L, -1L, "qergafg".getBytes() ),
        new StoreValue( "l", "enc1", 0L, 0L, 0L, -1L, "qergqerg".getBytes() ),
        new StoreValue( "m", "enc1", 0L, 0L, 0L, -1L, "erthwrthaegrae".getBytes() ),
        new StoreValue( "n", "enc1", 0L, 0L, 0L, -1L, "bg".getBytes() ),
        new StoreValue( "o", "enc1", 0L, 0L, 0L, -1L, "egtaerg".getBytes() ),
        new StoreValue( "p", "enc1", 0L, 0L, 0L, -1L, "ryhwretgWRGwfrgae".getBytes() ),
        new StoreValue( "q", "enc1", 0L, 0L, 0L, -1L, "sfgaetghserthgas".getBytes() ),
    };
}
