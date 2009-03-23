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

package org.infogrid.util;

/**
 * Collects the exceptions thrown by the ReturnSynchronizer.
 */
public abstract class ReturnSynchronizerException
    extends
        Exception
{
    /**
     * Private constructor for subclasses only.
     *
     * @param source the ReturnSynchronizer in which this exception occurred
     */
    protected ReturnSynchronizerException(
            ReturnSynchronizer<?,?> source )
    {
        super( null, null );

        theSource = source;
    }

    /**
     * Obtain the ReturnSynchronizer in which this exception occurred.
     *
     * @return the ReturnSynchronizer
     */
    public ReturnSynchronizer<?,?> getSource()
    {
        return theSource;
    }

    /**
     * The ReturnSynchronizer in which this exception occurred.
     */
    protected ReturnSynchronizer<?,?> theSource;

    /**
     * Common functionality for ReturnSynchronizerExceptions that indicate a state machine error.
     */
    public static abstract class IllegalState
            extends
                ReturnSynchronizerException
    {
       /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param t the Thread for which the state machine error occurred
         */
        public IllegalState(
                ReturnSynchronizer<?,?> source,
                Thread                  t )
        {
            super( source );

            theThread = t;
        }

        /**
         * Obtain the Thread for which the state machine error occurred.
         *
         * @return the Thread
         */
        public Thread getThread()
        {
            return theThread;
        }

        /**
         * The Thread for which the state machine error occurred.
         */
        protected Thread theThread;

    }

    /**
     * Thrown when a query has already been opened, and another attempt to open it is being made.
     */
    public static class TransactionOpenAlready
            extends
                IllegalState
    {
        private static final long serialVersionUID = 1L; // helps with serialization

       /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param t the Thread for which the query was open already
         */
        public TransactionOpenAlready(
                ReturnSynchronizer<?,?> source,
                Thread                  t )
        {
            super( source, t );
        }
    }

    /**
     * Thrown when a query should be open on this thread, but is not.
     */
    public static class NoTransactionOpen
            extends
                IllegalState
    {
        private static final long serialVersionUID = 1L; // helps with serialization

       /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param t the Thread for which no open query existed
         */
        public NoTransactionOpen(
                ReturnSynchronizer<?,?> source,
                Thread                  t )
        {
            super( source, t );
        }
    }

    /**
     * Thrown when an operation is executed that requires that a
     * query be complete, but which isn't.
     */
    public static class QueryIncomplete
            extends
                ReturnSynchronizerException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

       /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param key the key of the incomplete query
         */
        public QueryIncomplete(
                ReturnSynchronizer<?,?> source,
                Object                  key )
        {
            super( source );

            theKey = key;
        }

        /**
         * Obtain the key of the incomplete query.
         *
         * @return the key
         */
        public Object getKey()
        {
            return theKey;
        }

        /**
         * The key.
         */
        protected Object theKey;
    }

    /**
     * Common functionality of ReturnSynchronizerExceptions that deal with invalid keys.
     */
    public static abstract class IllegalKey
            extends
                ReturnSynchronizerException
    {
        /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param key the unknown key
         */
        public IllegalKey(
                ReturnSynchronizer<?,?> source,
                Object                  key )
        {
            super( source );

            theKey = key;
        }

        /**
         * Obtain the illegal key.
         *
         * @return the illegal key
         */
        public Object getIllegalKey()
        {
            return theKey;
        }

        /**
         * The illegal key.
         */
        protected Object theKey;
    }

    /**
     * Thrown when a key is used that is not known.
     */
    public static class UnknownKey
            extends
                IllegalKey
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param key the unknown key
         */
        public UnknownKey(
                ReturnSynchronizer<?,?> source,
                Object                  key )
        {
            super( source, key );
        }
    }

    /**
     * Thrown when a key is used that has already been used.
     */
    public static class DuplicateKey
            extends
                IllegalKey
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param key the duplicate key
         */
        public DuplicateKey(
                ReturnSynchronizer<?,?> source,
                Object                  key )
        {
            super( source, key );
        }
    }

    /**
     * Thrown if a result for the same key is deposited a second time.
     */
    public static class DuplicateResult
            extends
                IllegalKey
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the ReturnSynchronizer in which this exception occurred
         * @param key the key for the duplicate result
         * @param previousResult the previously deposited result
         * @param newResult the newly deposited result
         */
        public DuplicateResult(
                ReturnSynchronizer<?,?> source,
                Object                  key,
                Object                  previousResult,
                Object                  newResult )
        {
            super( source, key );

            thePreviousResult = previousResult;
            theNewResult      = newResult;
        }

        /**
         * The previously deposited result.
         */
        protected Object thePreviousResult;

        /**
         * The newly deposited result.
         */
        protected Object theNewResult;
    }
}
