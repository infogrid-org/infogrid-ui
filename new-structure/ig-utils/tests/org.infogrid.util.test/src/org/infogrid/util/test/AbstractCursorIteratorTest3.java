
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

import java.util.NoSuchElementException;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Tests zero-element CursorIterators.
 * All CursorIterators are run through the same test sequence, which is factored out here.
 */
public abstract class AbstractCursorIteratorTest3
        extends
            AbstractTest
{
    /**
     * Run the test.
     *
     * @param iter the to-be-tested iterator
     * @param log the Logger to use
     * @param <T> the type of Iterator to test
     */
    protected <T> void runWith(
            CursorIterator<T> iter,
            Log               log )
    {
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
     * Constructor.
     *
     * @throws Exception all kinds of things may go wrong in a test
     */
    protected AbstractCursorIteratorTest3()
        throws
            Exception
    {
        super();
    }
}
