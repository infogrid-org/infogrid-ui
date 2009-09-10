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
 * Tests zero-element CursorIterators.
 * Tests any StoreIterator. Subclasses define the Store implementation.
 */
public abstract class AbstractStoreIteratorTest3
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

        CursorIterator<StoreValue> iter = theTestStore.iterator();

        //

        log.info( "Check at the beginning" );

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( !iter.hasNext(), "has next at the end" );

        try {
            Object found = iter.peekNext();
            reportError( "Found element after end", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }
        try {
            Object found = iter.peekPrevious();
            reportError( "Found element before beginning", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Copy" );

        CursorIterator<?> copy = iter.createCopy();

        checkCondition( !copy.hasPrevious(), "has previous at the beginning" );
        checkCondition( !copy.hasNext(), "has next at the end" );

        try {
            Object found = copy.peekNext();
            reportError( "Found element after end", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }
        try {
            Object found = copy.peekPrevious();
            reportError( "Found element before beginning", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }

        //

        log.info( "Go to past last" );

        iter.moveToAfterLast();

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( !iter.hasNext(), "has next at the end" );

        //

        log.info( "Go before first" );

        iter.moveToBeforeFirst();

        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( !iter.hasNext(), "has next at the end" );
    }

    /**
     * Constructor if we do not have a special resource file.
     */
    protected AbstractStoreIteratorTest3()
    {
        super();

        // subclass has to initialize theTestStore
    }

    /**
     * Constructor if we have a special resource file.
     *
     * @param nameOfResourceHelperFile the name of the resource file
     */
    protected AbstractStoreIteratorTest3(
            String nameOfResourceHelperFile )
    {
        super( nameOfResourceHelperFile );

        // subclass has to initialize theTestStore
    }

    // Our Logger
    private static Log log = Log.getLogInstance( AbstractStoreIteratorTest3.class );

    /**
     * The actual Store to be tested. This may or may not be pointed to theSqlStore
     * by subclasses.
     */
    protected IterableStore theTestStore;
}
