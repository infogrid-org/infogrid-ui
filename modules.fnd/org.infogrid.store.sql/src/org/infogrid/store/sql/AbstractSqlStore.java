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

package org.infogrid.store.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import javax.sql.DataSource;
import org.infogrid.store.AbstractIterableStore;
import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * SQL implementation of the Store interface.
 */
public abstract class AbstractSqlStore
        extends
            AbstractIterableStore
        implements
            CanBeDumped
{
    /**
     * Constructor for subclasses only.
     *
     * @param ds the SQL DataSource 
     * @param tableName the name of the table in the SQL DataSource in which the data will be stored
     * @param autoCommit the value for new connection's autoCommit property. If null, will not be set
     */
    protected AbstractSqlStore(
            DataSource ds,
            String     tableName,
            Boolean    autoCommit )
    {
        theDataSource = ds;
        theTableName  = tableName;
        theAutoCommit = autoCommit;
    }

    /**
     * Initialize the Store. If the Store was initialized earlier, this will delete all
     * contained information. This operation is similar to unconditionally formatting a hard drive.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeHard()
            throws
                IOException
    {
        dropTables();
        createTables();
    }
    
    /**
     * Initialize the Store if needed. If the Store was initialized earlier, this will do
     * nothing. This operation is equivalent to {@link #initializeHard} if and only if
     * the Store had not been initialized earlier.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public synchronized void initializeIfNecessary()
            throws
                IOException
    {
        if( !hasTables() ) {
            createTables();
        }
    }

    /**
     * Determine whether the SqlStore has the SQL tables it needs.
     * 
     * @return true if the Store yhas the SQL tables it needs
     */
    protected abstract boolean hasTables();
    
    /**
     * Drop all tables that this SqlStore needs. Do nothing if there are none.
     */
    protected abstract void dropTables();
    
    /**
     * Create all tables that this SqlStore needs.
     * 
     * @throws IOException thrown if creating the tables was not possible
     */
    protected abstract void createTables()
            throws
                IOException;
    
    /**
     * Put a data element into the Store for the first time. Throw an Exception if a data
     * element has already been store using the same key.
     *
     * @param toStore the StoreValue to store
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void put(
            StoreValue toStore )
        throws
            StoreKeyExistsAlreadyException,
            IOException
    {
        put(    toStore.getKey(),
                toStore.getEncodingId(),
                toStore.getTimeCreated(),
                toStore.getTimeUpdated(),
                toStore.getTimeRead(),
                toStore.getTimeExpires(),
                toStore.getData() );
    }


    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw an
     * Exception if a data element with this key does not exist already.
     *
     * @param toUpdate the StoreValue to update
     * @throws StoreKeyDoesNotExistException thrown if no data element exists in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void update(
            StoreValue toUpdate )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        update( toUpdate.getKey(),
                toUpdate.getEncodingId(),
                toUpdate.getTimeCreated(),
                toUpdate.getTimeUpdated(),
                toUpdate.getTimeRead(),
                toUpdate.getTimeExpires(),
                toUpdate.getData() );
    }
    
    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param toStoreOrUpdate the StoreValue to store or update
     * @return true if the value was updated, false if it was put
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    public boolean putOrUpdate(
            StoreValue toStoreOrUpdate )
        throws
            IOException
    {
        return putOrUpdate(
                toStoreOrUpdate.getKey(),
                toStoreOrUpdate.getEncodingId(),
                toStoreOrUpdate.getTimeCreated(),
                toStoreOrUpdate.getTimeUpdated(),
                toStoreOrUpdate.getTimeRead(),
                toStoreOrUpdate.getTimeExpires(),
                toStoreOrUpdate.getData() );
    }
    
    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public IterableStoreCursor iterator()
    {
        return new SqlStoreIterator( this );
    }

    /**
     * Find the key N rows up or down from the current key.
     *
     * @param key the current key
     * @param delta the number of rows up (positive) or down (negative)
     * @return the found key, or null
     */
    protected abstract String findKeyAt(
            String key,
            int    delta );

    /**
     * Find the next n keys, including key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key the first key
     * @param n the number of keys to find
     * @return the found keys
     */
    protected String [] findNextKeyIncluding(
            String key,
            int    n )
    {
        // FIXME, this can be made more efficient
        StoreValue [] values = findNextIncluding( key, n );
        String     [] ret    = new String[0];
        for( int i=0 ; i<values.length ; ++i ) {
            ret[i] = values[i].getKey();
        }
        return ret;
    }

    /**
     * Find the next n StoreValues, including the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected abstract StoreValue [] findNextIncluding(
            String key,
            int    n );

    /**
     * Find the previous n keys, excluding the key for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key the first key NOT to be returned
     * @param n the number of keys to find
     * @return the found keys
     */
    protected String [] findPreviousKeyExcluding(
            String key,
            int    n )
    {
        // FIXME, this can be made more efficient
        StoreValue [] values = findPreviousExcluding( key, n );
        String     [] ret    = new String[0];
        for( int i=0 ; i<values.length ; ++i ) {
            ret[i] = values[i].getKey();
        }
        return ret;
    }

    /**
     * Find the previous n StoreValues, excluding the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue NOT to be returned
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected abstract StoreValue [] findPreviousExcluding(
            String key,
            int    n );

    /**
     * Count the number of rows following and including the one with the key.
     *
     * @param key the key
     * @return the number of rows
     */
    protected abstract int hasNextIncluding(
            String key );

    /**
     * Count the number of rows preceding and excluding the one with the key.
     *
     * @param key the key
     * @return the number of rows
     */
    protected abstract int hasPreviousExcluding(
            String key );
    
    /**
     * Determine the number of rows between two keys.
     *
     * @param from the start key
     * @param to the end key
     * @return the distance
     */
    protected abstract int determineDistance(
            final String from,
            final String to );

    /**
     * Helper method to convert the SQL values back into System.currentTimeMillis() format.
     *
     * @param stamp the SQL timestamp
     * @param millis milli-seconds to be added to the SQL timestamp
     * @return Java time
     */
    protected static long reconstructTime(
            Timestamp stamp,
            int       millis )
    {
        if( stamp == null ) {
            return -1L;
        }
        long ret = ( stamp.getTime() / 1000 ) * 1000 + millis;
        return ret;
    }

    /**
     * Obtain a connection to the database. This is a smart factory method,
     * returning an already-existing one if there is one instead of creating
     * a new one.
     *
     * @return the Connection
     * @throws SQLException thrown if the database could not be contacted
     */
    protected synchronized Connection obtainConnection()
        throws
            SQLException
    {
        if( theConnection == null ) {
            theConnection = theDataSource.getConnection();

            if( theConnection != null && theAutoCommit != null ) {
                theConnection.setAutoCommit( theAutoCommit.booleanValue() );
            }
        }
        return theConnection;
    }

    /**
     * Obtain a new connection to the database. This discards whatever old
     * Connection there may be already.
     *
     * @return the Connection
     * @throws SQLException thrown if the database could not be contacted
     */
    protected synchronized Connection obtainNewConnection()
        throws
            SQLException
    {
        if( theConnection != null ) {
            try {
                theConnection.close();
            } catch( Throwable t ) {
                // might be closed already -- ignore
            }
        }
        theConnection = theDataSource.getConnection();

        if( theConnection != null && theAutoCommit != null ) {
            theConnection.setAutoCommit( theAutoCommit.booleanValue() );
        }
        return theConnection;
    }

    /**
     * Close the connection to the database.
     */
    public void closeConnection()
    {
        if( theConnection != null ) {
            try {
                theConnection.close();
            } catch( SQLException ex2 ) {
                // noop
            }
            theConnection = null;
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
                    "dataSource",
                    "tableName"
                },
                new Object[] {
                    theDataSource,
                    theTableName
                });
    }

    /**
     * The JDBC data source.
     */
    protected DataSource theDataSource;
    
    /**
     * Name of the table in the JDBC data source. While we don't need this, it is
     * very useful in debugging.
     */
    protected String theTableName;

    /**
     * The most-recently used Connection, if any. This is private, so we are forced to
     * go through factory methods.
     */
    private Connection theConnection;
    
    /**
     * The value for the autoCommit property on Connections. If this is null, we do not set it.
     */
    protected Boolean theAutoCommit;
}
