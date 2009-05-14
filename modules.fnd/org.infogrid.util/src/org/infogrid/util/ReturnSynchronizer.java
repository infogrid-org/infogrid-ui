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

import java.util.HashMap;
import java.util.Map;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * <p>A generic synchronizer for collecting the results of N queries that execute in parallel.</p>
 *
 * <p>The situation generally is as follows:
 * N queries, each of which can have a result, have to be executed.
 * We want to issue those queries in parallel, and only proceed
 * after all of them have produced results. These results can come
 * in in any order. Queries are identified by keys.</p>
 * <p>Sequence of invocation is as follows:</p>
 * <ol>
 *  <li><code>ReturnSynchronizer r = ReturnSynchronizer.create();</code></li>
 *  <li><code>r.beginTransaction();</code></li>
 *  <li><code>r.addOpenQuery( key1 );</code> (repeated as many times as needed)</li>
 *  <li><code>r.join()</code></li>
 *  <li><code>Object result1 = r.getResultFor( key1 );</code> (repeated as many times as needed)</li>
 *  <li><code>r.endTransaction();</code></li>
 * </ol>
 * <p>The purpose of the <code>beginTransaction</code> and <code>endTransaction</code> calls is
 * largely to make debugging easier and fail faster in case of programming errors.</p>
 * <p>FIXME? There is a chance this can be implemented more efficiently.</p>
 *
 * @param <K> the type of key
 * @param <R> the type of return value
 */
public class ReturnSynchronizer<K,R>
        implements
            CanBeDumped
{
    private static Log log = Log.getLogInstance( ReturnSynchronizer.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @return the created ReturnSynchronizer
     * @param <K> the type of key
     * @param <R> the type of return value
     */
    public static <K,R> ReturnSynchronizer<K,R> create()
    {
        ReturnSynchronizer<K,R> ret = new ReturnSynchronizer<K,R>( null );

        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret );
        }
        return ret;
    }

    /**
     * Factory method.
     *
     * @param name a name for this object
     * @return the created ReturnSynchronizer
     * @param <K> the type of key
     * @param <R> the type of return value
     */
    public static <K,R> ReturnSynchronizer<K,R> create(
            Object name )
    {
        ReturnSynchronizer<K,R> ret = new ReturnSynchronizer<K,R>( name );

        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret, name );
        }
        return ret;
    }

    /**
     * Construct one with a name, which is to be used for debugging only.
     *
     * @param name a name for this object
     */
    public ReturnSynchronizer(
            Object name )
    {
        theName = name;
    }

    /**
     * Start a new transaction on this thread.
     *
     * @throws ReturnSynchronizerException.TransactionOpenAlready thrown if a transaction is already active on this Thread.
     */
    public void beginTransaction()
        throws
            ReturnSynchronizerException.TransactionOpenAlready
    {
        Thread t = Thread.currentThread();

        beginTransaction( t );
    }

    /**
     * Start a new transaction on a given thread.
     *
     * @param thread the Thread
     * @throws ReturnSynchronizerException.TransactionOpenAlready thrown if a transaction is already active on this Thread.
     */
    public void beginTransaction(
            Thread thread )
        throws
            ReturnSynchronizerException.TransactionOpenAlready
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "beginTransaction", thread );
        }

        synchronized( this ) {
            CS<K,R> found = threadToMonitorTable.get( thread );
            if( found != null ) {
                throw new ReturnSynchronizerException.TransactionOpenAlready( this, thread );
            }
            CS<K,R> created = new CS<K,R>();
            threadToMonitorTable.put( thread, created );
        }
    }

    /**
     * End a transaction on this thread.
     *
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if no transaction is active on this Thread
     */
    public void endTransaction()
        throws
            ReturnSynchronizerException.NoTransactionOpen
    {
        Thread t = Thread.currentThread();

        endTransaction( t );
    }

    /**
     * End a transaction on a given thread.
     *
     * @param thread the Thread
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if no transaction is active on this Thread
     */
    public void endTransaction(
            Thread thread )
        throws
            ReturnSynchronizerException.NoTransactionOpen
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "endTransaction", thread );
        }

        synchronized( this ) {
            CS<K,R> found = threadToMonitorTable.remove( thread );
            if( found == null ) {
                throw new ReturnSynchronizerException.NoTransactionOpen( this, thread );
            }
        }
    }

    /**
     * Add one more outstanding query for the open transaction on this thread.
     *
     * @param keyForQuery the key identifying the query
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if no transaction is active on this Thread
     * @throws ReturnSynchronizerException.DuplicateKey thrown if this key is already associated with a query
     */
    public void addOpenQuery(
            K keyForQuery )
        throws
            ReturnSynchronizerException.NoTransactionOpen,
            ReturnSynchronizerException.DuplicateKey
    {
        Thread t = Thread.currentThread();

        addOpenQuery( keyForQuery, t );
    }

    /**
     * Add one more outstanding query for the open transaction on a given thread.
     *
     * @param keyForQuery the key identifying the query
     * @param thread the Thread
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if no transaction is active on this Thread
     * @throws ReturnSynchronizerException.DuplicateKey thrown if this key is already associated with a query
     */
    public void addOpenQuery(
            K      keyForQuery,
            Thread thread )
        throws
            ReturnSynchronizerException.NoTransactionOpen,
            ReturnSynchronizerException.DuplicateKey
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "addOpenQuery", keyForQuery, thread );
        }

        synchronized( this ) {
            CS<K,R> semaphore = threadToMonitorTable.get( thread );
            if( semaphore == null ) {
                throw new ReturnSynchronizerException.NoTransactionOpen( this, thread );
            }
            // out of an abundance of caution we do the following check:
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                if( current.hasQuery( keyForQuery ) != null ) {
                    throw new ReturnSynchronizerException.DuplicateKey( this, keyForQuery );
                }
            }
            semaphore.addOpenQuery( keyForQuery );
        }
    }

    /**
     * <p>Add one more outstanding query to a transaction. The transaction is identified not
     * by thread, but indirectly by the thread associated with an existing query.</p>
     * 
     * <p>This is useful if an entity produces only part of a response and triggers the
     * production of another part of the response.</p>
     * 
     * @param existingKeyForQuery the key identifying the existing query
     * @param newKeyForQuery the key of the query to be added
     * @throws ReturnSynchronizerException.DuplicateKey thrown if this key is already associated with a query
     */
    public void addFurtherOpenQueryToOpenQuery(
            K existingKeyForQuery,
            K newKeyForQuery )
        throws
            ReturnSynchronizerException.DuplicateKey
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "addFurtherOpenQueryToOpenQuery", existingKeyForQuery, newKeyForQuery );
        }

        synchronized( this ) {
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                if( current.hasQuery( existingKeyForQuery ) != null ) {
                    current.addOpenQuery( newKeyForQuery ); // may throw
                }
            }
        }
    }

    /**
     * <p>A callback that indicates that a certain query has completed,
     * and has produced a certain result (which may be null).</p>.
     *
     * <p>If this is invoked for a non-existing query, nothing happens other than a different return value.
     *    This is for the case where the main thread has moved on before a slow result was ready.</p>
     *
     * @param keyForQuery the key identifying the query
     * @param result the result obtained for this query
     * @return true if the query is known and open, false otherwise
     * @throws ReturnSynchronizerException.DuplicateResult thrown if a result was already deposited for the query
     */
    public boolean depositQueryResult(
            K keyForQuery,
            R result )
        throws
            ReturnSynchronizerException.DuplicateResult
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "queryHasCompleted", keyForQuery, result );
        }

        synchronized( this ) {
            boolean ret = false;
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                if( current.hasQuery( keyForQuery ) != null ) {
                    current.depositQueryResult( keyForQuery, result );
                }
            }
            return ret;
        }
    }

    /**
     * Obtain the result of a certain query.
     *
     * @param keyForQuery the key identifying the query
     * @return the result of the query
     * @throws ReturnSynchronizerException.QueryIncomplete thrown if the query has not been completed yet
     * @throws ReturnSynchronizerException.UnknownKey thrown if the key was not known for this query
     */
    @SuppressWarnings("unchecked")
    public R getResultFor(
            K keyForQuery )
        throws
            ReturnSynchronizerException.QueryIncomplete,
            ReturnSynchronizerException.UnknownKey
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "getResultFor", keyForQuery );
        }

        synchronized( this ) {
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                Object found = current.hasQuery( keyForQuery );
                if( found == null ) {
                    continue; // perhaps another semaphore has it
                }
                if( found == current ) {
                    throw new ReturnSynchronizerException.QueryIncomplete( this, keyForQuery );
                } else {
                    return (R) found;
                }
            }
        }
        throw new ReturnSynchronizerException.UnknownKey( this, keyForQuery );
    }

    /**
     * Determine whether a certain query is complete yet.
     *
     * @param keyForQuery the key identifying the query
     * @return true if the query is complete
     * @throws ReturnSynchronizerException.UnknownKey thrown if the key was not known for this query
     */
    public synchronized boolean isQueryComplete(
            Object keyForQuery )
        throws
            ReturnSynchronizerException.UnknownKey
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "isQueryComplete", keyForQuery );
        }

        synchronized( this ) {
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                if( current.theResults == null ) {
                    continue;
                }
                Object found = current.theResults.get( keyForQuery );
                if( found == null ) {
                    continue;
                }
                if( found != current ) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        throw new ReturnSynchronizerException.UnknownKey( this, keyForQuery );
    }

    /**
     * Determine whether a certain query exists and is still open.
     *
     * @param keyForQuery the key identifying the query
     * @return true if the query exists and is still open
     */
    public boolean hasOpenQuery(
            Object keyForQuery )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "hasOpenQuery", keyForQuery );
        }

        synchronized( this ) {
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                if( current.theResults == null ) {
                    continue;
                }
                Object found = current.theResults.get( keyForQuery );
                if( found == null ) {
                    continue;
                }
                if( found == current ) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Determine whether all queries for this Thread are complete.
     *
     * @return true if all queries for the calling Thread are complete
     */
    public boolean areAllQueriesCompleteForThisThread()
    {
        Thread t = Thread.currentThread();

        return areAllQueriesCompleteForThread( t );
    }

    /**
     * Determine whether all queries for a given Thread are complete.
     *
     * @param t the Thread
     * @return true if all queries for a given Thread are complete
     */
    public boolean areAllQueriesCompleteForThread(
            Thread t )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "areAllQueriesCompleteForThread", t );
        }

        synchronized( this ) {
            CS<K,R> found = threadToMonitorTable.get( t );
            return found == null;
        }
    }

    /**
     * Determine whether all queries are complete, regardless of Thread.
     *
     * @return true if all queries are complete
     */
    public boolean areAllQueriesComplete()
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "areAllQueriesComplete" );
        }

        synchronized( this ) {
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                if( current != null ) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Suspend the calling Thread until all the queries have produced results.
     * Do not time out.
     *
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if no query is active on this Thread
     * @throws InterruptedException if this Thread was interrupted externally
     */
    public void join()
        throws
            ReturnSynchronizerException.NoTransactionOpen,
            InterruptedException
    {
        join( 0L );
    }

    /**
     * Suspend the calling Thread until the earlier of the two following conditions
     * is true: 1) all the queries have produced results, or 2) the specified
     * timeout has occurred.
     *
     * @param timeout the timeout in milliseconds. If 0L, wait forever. If negative, don't wait.
     * @return if true, the join succeeded in the time alloted. if false, we timed out
     * @throws ReturnSynchronizerException.NoTransactionOpen thrown if no query is active on this Thread
     * @throws InterruptedException if this Thread was interrupted externally
     */
    public boolean join(
            long timeout )
        throws
            ReturnSynchronizerException.NoTransactionOpen,
            InterruptedException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "join", timeout );
        }

        Thread thread = Thread.currentThread();
        try {
            CS semaphore;
            synchronized( this ) {
                semaphore = threadToMonitorTable.get( thread );
            }
            if( semaphore == null ) {
                throw new ReturnSynchronizerException.NoTransactionOpen( this, thread );
            }

            boolean ret;
            if( timeout >= 0L ) {
                ret = semaphore.join( timeout );
            } else {
                ret = false;
            }
            return ret;

        } finally {
            if( log.isTraceEnabled() ) {
                log.traceMethodCallExit( this, "join" );
            }
        }
    }

    /**
     * If this is invoked, a disabling fatal error has occurred. All waiting threads are released.
     *
     * @param ex the Throwable indicating the disabling error
     */
    public void disablingError(
            Throwable ex )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "disablingError", ex );
        }
        synchronized( this ) {
            for( CS<K,R> current : threadToMonitorTable.values() ) {
                current.abandon();
            }
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "name",
                    "threadToMonitorTable"
                },
                new Object[] {
                    theName,
                    threadToMonitorTable
                });
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return super.toString() + "{ theName: " + theName + " }";
    }

    /**
     * Track object deallocation.
     */
    @Override
    public void finalize()
    {
        if( log.isTraceEnabled() ) {
            log.traceFinalization( this );
        }
    }

    /**
     * The name of this instance (if any). For debugging only.
     */
    protected Object theName;

    /**
     * This table maps waiting Threads to their monitors.
     */
    protected HashMap<Thread,CS<K,R>> threadToMonitorTable = new HashMap<Thread,CS<K,R>>();

    /**
     * This is some version of a counting semaphore.
     *
     * @param <K> the type of key
     * @param <R> the type of return value
     */
    protected class CS<K,R>
            implements
                CanBeDumped
    {
        /**
         * Wait until our value has been decremented to zero, or we timed out.
         *
         * @param timeout the timeout
         * @return true if our value has been decremented and we didn't time out
         * @throws InterruptedException if this Thread was interrupted externally
         */
        public synchronized boolean join(
                long timeout )
            throws
                InterruptedException
        {
            if( theResults == null || theResults.isEmpty() ) {
                return true;
            }

            wait( timeout );

            if( counter <= 0 ) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Add one more outstanding query for a given thread.
         *
         * @param keyForQuery the key identifying the query
         */
        public void addOpenQuery(
                K keyForQuery )
        {
            if( theResults == null ) {
                theResults = new HashMap<K,Object>();
            }
            Object already = theResults.put( keyForQuery, this ); // maker for "open query"
            ++counter;
            
            if( already != null ) {
                log.error( "Have object already: " + already );
            }
        }

        /**
         * <p>A callback that indicates that a certain query has completed,
         * and has produced a certain result (which may be null).</p>.
         *
         * <p>If this is invoked for a non-existing query, nothing happens other than a different return value.</p>
         *
         * @param keyForQuery the key identifying the query
         * @param result the result obtained for this query
         * @return true if the query is known and open, false otherwise
         * @throws ReturnSynchronizerException.DuplicateResult thrown if a result was previously deposited for this query
         */
        @SuppressWarnings("unchecked")
        public synchronized boolean depositQueryResult(
                K keyForQuery,
                R result )
            throws
                ReturnSynchronizerException.DuplicateResult
        {
            if( theResults == null ) {
                return false;
            }
            Object found = theResults.get( keyForQuery );
            if( found == null ) {
                return false;
            }
            if( found == this ) {
                theResults.put( keyForQuery, result );

                --counter;
                if( counter <= 0 ) {
                    synchronized( this ) {
                        notifyAll();
                    }
                }
                return true;
            } else {
                throw new ReturnSynchronizerException.DuplicateResult( ReturnSynchronizer.this, keyForQuery, (R) found, result );
            }
        }

        /**
         * Determine whether this transaction has a query with the given key.
         * They query may still be open, and have been answered already.
         *
         * @param keyForQuery the key identifying the query
         * @return non-null if a query with this key exists.
         */
        public Object hasQuery(
                K keyForQuery )
        {
            if( theResults == null ) {
                return null;
            }
            Object found = theResults.get( keyForQuery );
            return found;
        }

        /**
         * Abandon the monitor.
         */
        public synchronized void abandon()
        {
            this.notifyAll();
        }

        /**
         * Dump this object.
         *
         * @param d the Dumper to dump to
         */
        public void dump(
                Dumper d )
        {
            d.dump( this,
                    new String[] {
                        "counter",
                        "results"
                    },
                    new Object[] {
                        counter,
                        theResults
                    });
        }

        /**
         * Our counter.
         */
        protected int counter;

        /**
         * The results table associated with this semaphore.
         */
        protected Map<K,Object> theResults;
    }
}
