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
 * Tests one-element CursorIterators.
 * Tests any StoreIterator. Subclasses define the Store implementation.
 */
public abstract class AbstractStoreIteratorTest2
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

        theTestStore.put(
                testData.getKey(),
                testData.getEncodingId(),
                testData.getTimeCreated(),
                testData.getTimeUpdated(),
                testData.getTimeRead(),
                testData.getTimeExpires(),
                testData.getData() );

        CursorIterator<StoreValue> iter = theTestStore.iterator();

        //

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
     * Constructor if we do not have a special resource file.
     */
    protected AbstractStoreIteratorTest2()
    {
        super();

        // subclass has to initialize theTestStore
    }

    /**
     * Constructor if we have a special resource file.
     *
     * @param nameOfResourceHelperFile the name of the resource file
     */
    protected AbstractStoreIteratorTest2(
            String nameOfResourceHelperFile )
    {
        super( nameOfResourceHelperFile );

        // subclass has to initialize theTestStore
    }

    // Our Logger
    private static Log log = Log.getLogInstance( AbstractStoreIteratorTest2.class );

    /**
     * The actual Store to be tested. This may or may not be pointed to theSqlStore
     * by subclasses.
     */
    protected IterableStore theTestStore;

    /**
     * Test data.
     */
    protected static final long now = System.currentTimeMillis();
    protected static final StoreValue testData =
        new StoreValue( "a", "enc1",           12345L, 67890L, 10111213L,     -1L, "some data".getBytes() );
}
