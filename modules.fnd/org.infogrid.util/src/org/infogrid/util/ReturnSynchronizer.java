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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * <p>A generic synchronizer for collecting the results of N queries executive in parallel.</p>
 *
 * <p>The situation generally is as follows:
 * N queries, each of which can have a result, have to be executed.
 * We want to issue those queries in parallel, and only proceed
 * after all of them have produced results. These results can come
 * in in any order. Queries are identified by keys.</p>
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
     * Constructor.
     */
    public ReturnSynchronizer()
    {
       // no op
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
     * Obtain a synchronization Object for the calling Thread.
     *
     * @return the synchronization Object
     */
    public synchronized Object getSyncObject()
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".getSyncObject()" );
        }
        Thread threadToWait = Thread.currentThread();

        CS monitor = threadToMonitorTable.get( threadToWait );
        if( monitor == null ) {
            monitor = new CS();
            threadToMonitorTable.put( threadToWait, monitor );
        }
        return monitor;
    }

    /**
     * Add one more outstanding query, specifying which Thread wants to wait for that query.
     *
     * @param keyForQuery the key identifying the query
     * @return the Object which we are supposed to use to synchronize our join call against
     */
    public synchronized Object addOpenQuery(
            K keyForQuery )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".addOpenQuery( " + keyForQuery + " )" );
        }
        Thread threadToWait = Thread.currentThread();

        ArrayList<Thread> threadsForThisQuery = queryToThreadsTable.get( keyForQuery );
        if( threadsForThisQuery == null ) {
            threadsForThisQuery = new ArrayList<Thread>();
            queryToThreadsTable.put( keyForQuery, threadsForThisQuery );
        }
        threadsForThisQuery.add( threadToWait );

        CS monitor = threadToMonitorTable.get( threadToWait );
        if( monitor == null ) {
            monitor = new CS();
            threadToMonitorTable.put( threadToWait, monitor );
        }
        monitor.inc();

        theResultsTable.put( keyForQuery, this ); // placeholder so we know query is in progress

        return monitor;
    }

    /**
     * <p>A callback that indicates that a certain query has completed,
     * and has produced a certain result (which may be null).</p>.
     *
     * <p>If this is invoked for a non-existing query, nothing happens.</p>
     *
     * @param keyForQuery the key identifying the query
     * @param result the result obtained for this query
     */
    public void queryHasCompleted(
            K keyForQuery,
            R result )
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".queryHasCompleted( " + keyForQuery + ", " + result + " )" );
        }
        CS [] monitors;

        synchronized( this ) {
            ArrayList<Thread> threadsForThisQuery = queryToThreadsTable.get( keyForQuery );
            if( threadsForThisQuery == null ) {
                return;
            }

            // we have to first find the monitors to decrement, the release the
            // lock on this object, and then decrement

            if( result != null ) {
                theResultsTable.put( keyForQuery, result );
            }
            monitors = new CS[ threadsForThisQuery.size() ];

            Iterator<Thread> theIter = threadsForThisQuery.iterator();
            for( int i=0 ; theIter.hasNext() ; ++i ) {
                Thread t = theIter.next();

                monitors[i] = threadToMonitorTable.get( t );

                // this may be null if our thread has left already
            }
        }

        for( int i=0 ; i<monitors.length ; ++i ) {
            if( monitors[i] != null ) {
                monitors[i].dec();
            }
        }
    }

    /**
     * Obtain the result of a certain query and remove it from our internal
     * results storage for this Thread.
     *
     * @param keyForQuery the key identifying the query
     * @return the result of the query
     * @throws QueryIncompleteException thrown if the query has not been completed yet
     */
    @SuppressWarnings(value={"unchecked"})
    public synchronized R takeResultFor(
            K keyForQuery )
        throws
            QueryIncompleteException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".takeResultFor( " + keyForQuery + " )" );
        }
        Thread t = Thread.currentThread();

        ArrayList<Thread> threadsForThisQuery = queryToThreadsTable.get( keyForQuery );
        if( threadsForThisQuery == null || !threadsForThisQuery.remove( t )) {
            throw new IllegalStateException();
        }

        int nWaiting = threadsForThisQuery.size();

        Object ret = theResultsTable.get( keyForQuery );
        if( ret == this ) {
            throw new QueryIncompleteException();
        }

        if( nWaiting == 0 ) {
            theResultsTable.remove( keyForQuery );
            queryToThreadsTable.remove( keyForQuery );
        }

        return (R) ret; // @SuppressWarnings(value={"unchecked"}) needed here
    }

    /**
     * Determine whether a certain query is complete yet.
     *
     * @param keyForQuery the key identifying the query
     * @return true if the query is complete
     */
    public synchronized boolean isQueryComplete(
            Object keyForQuery )
    {
        return theResultsTable.get( keyForQuery ) != this;
    }

    /**
     * Determine whether all queries for this Thread are complete.
     *
     * @return true if all queries for the calling Thread are complete
     */
    public boolean areAllQueriesCompleteForThisThread()
    {
        Thread t = Thread.currentThread();

        return threadToMonitorTable.get( t ) == null;
    }

    /**
     * Suspend the calling Thread until all the queries have produced results.
     * Do not time out.
     *
     * @throws InterruptedException if this Thread was interrupted externally
     */
    public void join()
        throws
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
     * @throws InterruptedException if this Thread was interrupted externally
     */
    public boolean join(
            long timeout )
        throws
            InterruptedException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".join( " + timeout + " )" );
        }

        Thread t = Thread.currentThread();
        CS monitor;

        synchronized( this ) {
            monitor = threadToMonitorTable.get( t );

            if( monitor == null ) {
                return true; // no more open arguments for this tread (maybe there never were)
            }
        }

        boolean ret;
        if( timeout >= 0L ) {
            ret = monitor.join( timeout );
        } else {
            ret = false;
        }
        
        threadToMonitorTable.remove( t );

        return ret;
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
                    "threadToMonitorTable",
                    "queryToThreadsTable",
                    "theResultsTable"
                },
                new Object[] {
                    theName,
                    threadToMonitorTable,
                    queryToThreadsTable,
                    theResultsTable
                });
    }

    /**
     * The name of this instance (if any). For debugging only.
     */
    protected Object theName;

    /**
     * This table maps waiting Threads to their monitors.
     */
    protected HashMap<Thread,CS> threadToMonitorTable = new HashMap<Thread,CS>();

    /**
     * This table maps queries to the Threads that wait for them.
     */
    protected HashMap<K,ArrayList<Thread>> queryToThreadsTable = new HashMap<K,ArrayList<Thread>>();

    /**
     * The table of queries and results as they come in and certain
     * Threads have not picked up their results yet. We'd like to make the value
     * type R, but then we can't use a marker object (such as "this") to mark in-progress queries.
     */
    protected HashMap<K,Object> theResultsTable = new HashMap<K,Object>();

    /**
     * This is some version of a counting semaphore.
     */
    protected static class CS
    {
        /**
         * Wait until our value has been decremented to zero, or we timed out.
         *
         * @param timeout the timeout
         * @return true if our value has been decremented and we didn't time out
         * @throws InterruptedException if this Thread was interrupted externally
         */
        public boolean join(
                long timeout )
            throws
                InterruptedException
        {
            if( counter == 0 ) {
                return true;
            }

            try {
                wait( timeout );
            } catch( IllegalMonitorStateException ex ) {
                throw new IllegalMonitorStateException(
                        "Did you forget to only invoke this from within a synchronized block with the object returned by addOpenQuery()?" );
            }

            if( counter == 0 ) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Increment counter.
         */
        public synchronized void inc()
        {
            ++counter;
        }

        /**
         * Decrement counter.
         */
        public synchronized void dec()
        {
            --counter;
            if( counter == 0 ) {
                this.notifyAll();
            }
        }

        /**
         * Convert to String format, for debugging only.
         *
         * @return this instance in String format
         */
        @Override
        public String toString()
        {
            return "CS" + hashCode() + "(" + counter + ")";
        }

        /**
         * Our counter.
         */
        protected int counter;
    }

    /**
     * Simple Exception thrown when an operation is executed that requires that a
     * query be complete, but which isn't.
     */
    public static class QueryIncompleteException
            extends
                Exception
    {
        private static final long serialVersionUID = 1L; // helps with serialization
    }

    /**
     * Simple Exception thrown when an illegal key is used.
     */
    public static class IllegalKeyException
            extends
                RuntimeException
    {
        private static final long serialVersionUID = 1L; // helps with serialization
    }
}
