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

package org.infogrid.store.sql.postgresql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import javax.sql.DataSource;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;
import org.infogrid.store.sql.AbstractSqlStore;
import org.infogrid.store.sql.SqlExecutionAction;
import org.infogrid.store.sql.SqlStoreIOException;
import org.infogrid.store.sql.SqlStorePreparedStatement;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.logging.Log;

/**
 * SqlStore implementation using Postgresql.
 */
public class PostgresqlStore
        extends
            AbstractSqlStore
{
    private static final Log log = Log.getLogInstance( PostgresqlStore.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param ds the SQL DataSource
     * @param tableName the name of the table in the SQL DataSource in which the data will be stored
     * @return the created AbstractSqlStore
     */
    public static PostgresqlStore create(
            DataSource ds,
            String     tableName )
    {
        return new PostgresqlStore( ds, tableName );
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param ds the SQL DataSource 
     * @param tableName the name of the table in the SQL DataSource in which the data will be stored
     */
    protected PostgresqlStore(
            DataSource ds,
            String     tableName )
    {
        super( ds, tableName, Boolean.FALSE );
        
        theCreateTablesPreparedStatement          = new SqlStorePreparedStatement( this, CREATE_TABLES_SQL,           tableName );
        theDropTablesPreparedStatement            = new SqlStorePreparedStatement( this, DROP_TABLES_SQL,             tableName );
        theHasTablesPreparedStatement             = new SqlStorePreparedStatement( this, HAS_TABLES_SQL,              tableName );
        thePutPreparedStatement                   = new SqlStorePreparedStatement( this, PUT_SQL,                     tableName );
        theUpdatePreparedStatement                = new SqlStorePreparedStatement( this, UPDATE_SQL,                  tableName );
        theGetPreparedStatement                   = new SqlStorePreparedStatement( this, GET_SQL,                     tableName );
        theDeletePreparedStatement                = new SqlStorePreparedStatement( this, DELETE_SQL,                  tableName );
        theDeleteAllPreparedStatement             = new SqlStorePreparedStatement( this, DELETE_ALL_SQL,              tableName );
        theSizePreparedStatement                  = new SqlStorePreparedStatement( this, SIZE_SQL,                    tableName );

        theFindNextIncludingPreparedStatement     = new SqlStorePreparedStatement( this, FIND_NEXT_INCLUDING_SQL,     tableName );
        theFindPreviousExcludingPreparedStatement = new SqlStorePreparedStatement( this, FIND_PREVIOUS_EXCLUDING_SQL, tableName );
        theFindLastValuesPreparedStatement        = new SqlStorePreparedStatement( this, FIND_LAST_VALUES_SQL,        tableName );
        theHasNextIncludingPreparedStatement      = new SqlStorePreparedStatement( this, HAS_NEXT_INCLUDING_SQL,      tableName );
        theHasPreviousExcludingPreparedStatement  = new SqlStorePreparedStatement( this, HAS_PREVIOUS_EXCLUDING_SQL,  tableName );
        theFindKeyAtPositivePreparedStatement     = new SqlStorePreparedStatement( this, FIND_KEY_AT_POSITIVE_SQL,    tableName );
        theFindKeyAtNegativePreparedStatement     = new SqlStorePreparedStatement( this, FIND_KEY_AT_NEGATIVE_SQL,    tableName );
        theFindKeyAtEndPreparedStatement          = new SqlStorePreparedStatement( this, FIND_KEY_AT_END_SQL,         tableName );
        theDetermineDistancePreparedStatement     = new SqlStorePreparedStatement( this, DETERMINE_DISTANCE_SQL,      tableName );
        
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "constructor" );
        }
    }

    /**
     * Determine whether the SqlStore has the SQL tables it needs.
     * 
     * @return true if the Store yhas the SQL tables it needs
     */
    protected boolean hasTables()
    {
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "hasTables" );
        }

        try {
            new SqlExecutionAction<Object>( theHasTablesPreparedStatement ) {
                protected Object perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.execute();
                    return null;
                }
            }.execute();
            
            return true;
                    
        } catch( Throwable ex ) {
            // ignore
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
        }
        return false;
    }
    
    /**
     * Drop all tables that this SqlStore needs. Do nothing if there are none.
     */
    protected void dropTables()
    {
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "dropTables" );
        }

        try {
            new SqlExecutionAction<Object>( theDropTablesPreparedStatement ) {
                protected Object perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.execute();
                    return null;
                }
            }.execute();
            
        } catch( Throwable ex ) {
            // ignore
            if( log.isInfoEnabled() ) {
                log.info( ex );
            }
        }        
    }
    
    /**
     * Create all tables that this SqlStore needs.
     * 
     * @throws IOException thrown if creating the tables was not possible
     */
    protected void createTables()
            throws
                IOException
    {
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "createTables" );
        }

        try {
            new SqlExecutionAction<Object>( theCreateTablesPreparedStatement ) {
                protected Object perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.execute();
                    return null;
                }
            }.execute();
            
        } catch( SQLException ex ) {
            throw new SqlStoreIOException( ex );
        }
    }

    /**
     * Put a data element into the Store for the first time. Throw an Exception if a data
     * element has already been store using the same key.
     *
     * @param key the key with which the specified data element is to be associated
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws SqlStoreIOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    @Override
    public void put(
            final String  key,
            final String  encodingId,
            final long    timeCreated,
            final long    timeUpdated,
            final long    timeRead,
            final long    timeExpires,
            final byte [] data )
        throws
            StoreKeyExistsAlreadyException,
            SqlStoreIOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".put( " + key + " -> " + new String( data ) + " )" );
        }
        checkKey(      key );
        checkEncoding( encodingId );
        checkData(     data );
        
        try {
            boolean success = new SqlExecutionAction<Boolean>( thePutPreparedStatement ) {
                protected Boolean perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString(            1, key );
                    stm.setString(            2, encodingId );

                    if( timeCreated >= 0 ) {
                        int timeCreatedMillis = (int) ( timeCreated % 1000 );
                        stm.setTimestamp(     3, new Timestamp( timeCreated - timeCreatedMillis ) );
                        stm.setInt(           4, timeCreatedMillis );
                    } else {
                        stm.setNull(          3, Types.TIMESTAMP );
                        stm.setNull(          4, Types.INTEGER );
                    }

                    if( timeUpdated >= 0 ) {
                        int timeUpdatedMillis = (int) ( timeUpdated % 1000 );
                        stm.setTimestamp(     5, new Timestamp( timeUpdated - timeUpdatedMillis ));
                        stm.setInt(           6, timeUpdatedMillis );
                    } else {
                        stm.setNull(          5, Types.TIMESTAMP );
                        stm.setNull(          6, Types.INTEGER );
                    }

                    if( timeRead >= 0 ) {
                        int timeReadMillis = (int) ( timeRead % 1000 );
                        stm.setTimestamp(     7, new Timestamp( timeRead - timeReadMillis ));
                        stm.setInt(           8, timeReadMillis );
                    } else {
                        stm.setNull(          7, Types.TIMESTAMP );
                        stm.setNull(          8, Types.INTEGER );
                    }

                    if( timeExpires >= 0 ) {
                        int timeExpiresMillis = (int) ( timeExpires % 1000 );
                        stm.setTimestamp(     9, new Timestamp( timeExpires - timeExpiresMillis ));
                        stm.setInt(          10, timeExpiresMillis );
                    } else {
                        stm.setNull(          9, Types.TIMESTAMP );
                        stm.setNull(         10, Types.INTEGER );
                    }

                    stm.setBytes( 11, data );

                    stm.execute();

                    boolean success = stm.getUpdateCount() > 0;

                    return success;
                }
            }.execute();
            
            if( !success ) {
                throw new StoreKeyExistsAlreadyException( this, key );
            }
        } catch( SQLException ex ) {
            throw new SqlStoreIOException( ex );

        } finally {
            StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

            firePutPerformed( value );
        }
    }
    
    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw an
     * Exception if a data element with this key does not exist already.
     *
     * @param key the key with which the specified data element is already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @throws StoreKeyDoesNotExistException thrown if no data element exists in the Store using this key
     * @throws SqlStoreIOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    @Override
    public void update(
            final String  key,
            final String  encodingId,
            final long    timeCreated,
            final long    timeUpdated,
            final long    timeRead,
            final long    timeExpires,
            final byte [] data )
        throws
            StoreKeyDoesNotExistException,
            SqlStoreIOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".update( " + key + " -> " + new String( data ) + " )" );
        }
        checkKey(      key );
        checkEncoding( encodingId );
        checkData(     data );

        try {
            boolean success = new SqlExecutionAction<Boolean>( theUpdatePreparedStatement ) {
                protected Boolean perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString(            1, encodingId );

                    if( timeCreated >= 0 ) {
                        int timeCreatedMillis = (int) ( timeCreated % 1000 );
                        stm.setTimestamp(     2, new Timestamp( timeCreated - timeCreatedMillis ) );
                        stm.setInt(           3, timeCreatedMillis );
                    } else {
                        stm.setNull(          2, Types.TIMESTAMP );
                        stm.setNull(          3, Types.INTEGER );
                    }

                    if( timeUpdated >= 0 ) {
                        int timeUpdatedMillis = (int) ( timeUpdated % 1000 );
                        stm.setTimestamp(     4, new Timestamp( timeUpdated - timeUpdatedMillis ));
                        stm.setInt(           5, timeUpdatedMillis );
                    } else {
                        stm.setNull(          4, Types.TIMESTAMP );
                        stm.setNull(          5, Types.INTEGER );
                    }

                    if( timeRead >= 0 ) {
                        int timeReadMillis = (int) ( timeRead % 1000 );
                        stm.setTimestamp(     6, new Timestamp( timeRead - timeReadMillis ));
                        stm.setInt(           7, timeReadMillis );
                    } else {
                        stm.setNull(          6, Types.TIMESTAMP );
                        stm.setNull(          7, Types.INTEGER );
                    }

                    if( timeExpires >= 0 ) {
                        int timeExpiresMillis = (int) ( timeExpires % 1000 );
                        stm.setTimestamp(     8, new Timestamp( timeExpires - timeExpiresMillis ));
                        stm.setInt(           9, timeExpiresMillis );
                    } else {
                        stm.setNull(          8, Types.TIMESTAMP );
                        stm.setNull(          9, Types.INTEGER );
                    }

                    stm.setBytes(            10, data );
                    stm.setString(           11, key );
                    stm.execute();

                    boolean success = stm.getUpdateCount() > 0;

                    return success;
                }
            }.execute();
            
            if( !success ) {
                throw new StoreKeyDoesNotExistException( PostgresqlStore.this, key  );
            }
        } catch( SQLException ex ) {
            throw new SqlStoreIOException( ex );

        } finally {
            StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

            fireUpdatePerformed( value );
        }
    }

    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param key the key with which the specified data element is already, and will continue to be stored
     * @param encodingId the id of the encoding that was used to encode the data element.
     * @param timeCreated the time at which the data element was created
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires if positive, the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     * @return true if the value was updated, false if it was put
     * @throws SqlStoreIOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    @Override
    public boolean putOrUpdate(
            final String  key,
            final String  encodingId,
            final long    timeCreated,
            final long    timeUpdated,
            final long    timeRead,
            final long    timeExpires,
            final byte [] data )
        throws
            SqlStoreIOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".putOrUpdate( " + key + " -> " + new String( data ) + " )" );
        }

        checkKey(      key );
        checkEncoding( encodingId );
        checkData(     data );

        boolean updated = false;
        try {
            updated = new SqlExecutionAction<Boolean>( theUpdatePreparedStatement ) {
                protected Boolean perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString(            1, encodingId );

                    if( timeCreated >= 0 ) {
                        int timeCreatedMillis = (int) ( timeCreated % 1000 );
                        stm.setTimestamp(     2, new Timestamp( timeCreated - timeCreatedMillis ) );
                        stm.setInt(           3, timeCreatedMillis );
                    } else {
                        stm.setNull(          2, Types.TIMESTAMP );
                        stm.setNull(          3, Types.INTEGER );
                    }

                    if( timeUpdated >= 0 ) {
                        int timeUpdatedMillis = (int) ( timeUpdated % 1000 );
                        stm.setTimestamp(     4, new Timestamp( timeUpdated - timeUpdatedMillis ));
                        stm.setInt(           5, timeUpdatedMillis );
                    } else {
                        stm.setNull(          4, Types.TIMESTAMP );
                        stm.setNull(          5, Types.INTEGER );
                    }

                    if( timeRead >= 0 ) {
                        int timeReadMillis = (int) ( timeRead % 1000 );
                        stm.setTimestamp(     6, new Timestamp( timeRead - timeReadMillis ));
                        stm.setInt(           7, timeReadMillis );
                    } else {
                        stm.setNull(          6, Types.TIMESTAMP );
                        stm.setNull(          7, Types.INTEGER );
                    }

                    if( timeExpires >= 0 ) {
                        int timeExpiresMillis = (int) ( timeExpires % 1000 );
                        stm.setTimestamp(     8, new Timestamp( timeExpires - timeExpiresMillis ));
                        stm.setInt(           9, timeExpiresMillis );
                    } else {
                        stm.setNull(          8, Types.TIMESTAMP );
                        stm.setNull(          9, Types.INTEGER );
                    }

                    stm.setBytes(            10, data );
                    stm.setString(           11, key );
                    stm.execute();

                    boolean success = stm.getUpdateCount() > 0;

                    return success;
                }
            
            }.execute( (Boolean) null ); // stay in the same transaction
            
            if( !updated ) {
                boolean putResult = new SqlExecutionAction<Boolean>( thePutPreparedStatement ) {
                    protected Boolean perform(
                            PreparedStatement stm,
                            Connection        conn )
                        throws
                            SQLException
                    {
                        stm.setString(            1, key );
                        stm.setString(            2, encodingId );

                        if( timeCreated >= 0 ) {
                            int timeCreatedMillis = (int) ( timeCreated % 1000 );
                            stm.setTimestamp(     3, new Timestamp( timeCreated - timeCreatedMillis ) );
                            stm.setInt(           4, timeCreatedMillis );
                        } else {
                            stm.setNull(          3, Types.TIMESTAMP );
                            stm.setNull(          4, Types.INTEGER );
                        }

                        if( timeUpdated >= 0 ) {
                            int timeUpdatedMillis = (int) ( timeUpdated % 1000 );
                            stm.setTimestamp(     5, new Timestamp( timeUpdated - timeUpdatedMillis ));
                            stm.setInt(           6, timeUpdatedMillis );
                        } else {
                            stm.setNull(          5, Types.TIMESTAMP );
                            stm.setNull(          6, Types.INTEGER );
                        }

                        if( timeRead >= 0 ) {
                            int timeReadMillis = (int) ( timeRead % 1000 );
                            stm.setTimestamp(     7, new Timestamp( timeRead - timeReadMillis ));
                            stm.setInt(           8, timeReadMillis );
                        } else {
                            stm.setNull(          7, Types.TIMESTAMP );
                            stm.setNull(          8, Types.INTEGER );
                        }

                        if( timeExpires >= 0 ) {
                            int timeExpiresMillis = (int) ( timeExpires % 1000 );
                            stm.setTimestamp(     9, new Timestamp( timeExpires - timeExpiresMillis ));
                            stm.setInt(          10, timeExpiresMillis );
                        } else {
                            stm.setNull(          9, Types.TIMESTAMP );
                            stm.setNull(         10, Types.INTEGER );
                        }

                        stm.setBytes( 11, data );

                        stm.execute();

                        boolean success = stm.getUpdateCount() > 0;

                        return success;
                    }               
                }.execute( null );  // stay in the same transaction
            }

            obtainConnection().commit(); // close transaction
            
            return updated;

        } catch( SQLException ex ) {
            throw new SqlStoreIOException( ex );

        } finally {
            StoreValue value = new StoreValue( key, encodingId, timeCreated, timeUpdated, timeRead, timeExpires, data );

            if( updated ) {
                fireUpdatePerformed( value );
            } else {
                firePutPerformed( value );
            }
        }        
    }
    
    /**
     * Obtain a data element and associated meta-data from the Store, given a key.
     *
     * @param key the key to the data element in the Store
     * @return the StoreValue stored in the Store for this key; this encapsulates data element and meta-data
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws SqlStoreIOException thrown if the database could not be read
     */
    public StoreValue get(
            final String key )
        throws
            StoreKeyDoesNotExistException,
            SqlStoreIOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".get( " + key + " )" );
        }

        checkKey( key );

        StoreValue ret = null;
        try {
            ret = new SqlExecutionAction<StoreValue>( theGetPreparedStatement ) {
                protected StoreValue perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    StoreValue ret = null;

                    stm.setString( 1, key );
                    stm.execute();

                    ResultSet set = stm.getResultSet();

                    if( set.next() ) {
                        String    foundKey              = set.getString(    "id" );
                        String    encodingId            = set.getString(    "encodingId" );
                        Timestamp timeCreated           = set.getTimestamp( "timeCreated" );
                        int       timeCreatedMillis     = set.getInt(       "timeCreatedMillis" );
                        Timestamp timeUpdated           = set.getTimestamp( "timeUpdated" );
                        int       timeUpdatedMillis     = set.getInt(       "timeUpdatedMillis" );
                        Timestamp timeRead              = set.getTimestamp( "timeRead" );
                        int       timeReadMillis        = set.getInt(       "timeReadMillis" );
                        Timestamp timeExpires           = set.getTimestamp( "timeExpires" );
                        int       timeExpiresMillis     = set.getInt(       "timeExpiresMillis" );
                        byte []   data                  = set.getBytes(     "content" );

                        if( key != null && !key.equals( foundKey )) {
                            log.error( "Found different key: " + key + " vs. " + foundKey );
                        }
                        ret = new StoreValue(
                                foundKey,
                                encodingId,
                                timeCreated != null ? (( timeCreated.getTime() / 1000 ) * 1000 + timeCreatedMillis ) : -1,
                                timeUpdated != null ? (( timeUpdated.getTime() / 1000 ) * 1000 + timeUpdatedMillis ) : -1,
                                timeRead    != null ? (( timeRead.getTime()    / 1000 ) * 1000 + timeReadMillis )    : -1,
                                timeExpires != null ? (( timeExpires.getTime() / 1000 ) * 1000 + timeExpiresMillis ) : -1,
                                data );
                    }

                    set.close();
                    return ret;
                }
            }.execute();


            if( ret == null ) {
                throw new StoreKeyDoesNotExistException( PostgresqlStore.this, key );
            }
            return ret;

        } catch( SQLException ex ) {
            throw new SqlStoreIOException( ex );

        } finally {
            if( ret != null ) {
                fireGetPerformed( ret );
            } else {
                fireGetFailed( key );
            }
        }
    }
    
    /**
     * Delete the StoreValue that is stored using this key.
     *
     * @param key the key to the data element in the Store
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws SqlStoreIOException thrown if the database could not be written
     */
    public void delete(
            final String key )
        throws
            StoreKeyDoesNotExistException,
            SqlStoreIOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".delete( " + key + " )" );
        }

        checkKey( key );

        try {
            boolean success = new SqlExecutionAction<Boolean>( theDeletePreparedStatement ) {
                protected Boolean perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString( 1, key );
                    int updates = stm.executeUpdate();

                    boolean success = updates > 0;
                    return success;
                }
            }.execute();

            if( !success ) {
                throw new StoreKeyDoesNotExistException( this, key );
            }

        } catch( SQLException ex ) {
            throw new SqlStoreIOException( ex );

        } finally {
            fireDeletePerformed( key );
        }
    }

    /**
     * Remove all data in this Store whose key starts with this string.
     *
     * @param startsWith the String the key starts with
     * @throws SqlStoreIOException thrown if the database could not be written
     */
    public void deleteAll(
            final String startsWith )
        throws
            SqlStoreIOException
    {
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "deleteAll" );
        }
        checkKey( startsWith );

        try {
            new SqlExecutionAction<Object>( theDeleteAllPreparedStatement ) {
                protected Object perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString( 1, startsWith + '%' ); // trailing wild card
                    stm.execute();
                    return null;
                }
            }.execute();
            
        } catch( SQLException ex ) {
            throw new SqlStoreIOException( ex );
        }
    }
    
    /**
     * Find the next n StoreValues, including the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected StoreValue [] findNextIncluding(
            final String key,
            final int    n )
    {
        if( key == null ) {
            return new StoreValue[0];
        }
        return findValues( theFindNextIncludingPreparedStatement, key, n );
    }
    
    /**
     * Find the previous n StoreValues, excluding the StoreValue for key. This method
     * will return fewer values if only fewer values could be found.
     *
     * @param key key for the first StoreValue NOT to be returned
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected StoreValue [] findPreviousExcluding(
            final String key,
            final int    n )
    {
        if( key != null ) {
            return findValues( theFindPreviousExcludingPreparedStatement, key, n );
        }
        
        StoreValue [] ret = new StoreValue[ n ];
        try {
            ResultSet set = new SqlExecutionAction<ResultSet>( theFindLastValuesPreparedStatement ) {
                protected ResultSet perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setInt(    1, n );

                    stm.execute();
                    return stm.getResultSet();
                }
            }.execute();

            int count = 0;
            while( set.next() ) {
                String    foundKey          = set.getString(    "id" );
                String    encodingId        = set.getString(    "encodingId" );
                Timestamp timeCreated       = set.getTimestamp( "timeCreated" );
                int       timeCreatedMillis = set.getInt(       "timeCreatedMillis" );
                Timestamp timeUpdated       = set.getTimestamp( "timeUpdated" );
                int       timeUpdatedMillis = set.getInt(       "timeUpdatedMillis" );
                Timestamp timeRead          = set.getTimestamp( "timeRead" );
                int       timeReadMillis    = set.getInt(       "timeReadMillis" );
                Timestamp timeExpires       = set.getTimestamp( "timeExpires" );
                int       timeExpiresMillis = set.getInt(       "timeExpiresMillis" );
                byte []   data              = set.getBytes(     "content" );

                // this is different from the get() method -- we find the one after, not the same one
                ret[count++] = new StoreValue(
                        foundKey,
                        encodingId,
                        reconstructTime( timeCreated, timeCreatedMillis ),
                        reconstructTime( timeUpdated, timeUpdatedMillis ),
                        reconstructTime( timeRead,    timeReadMillis ),
                        reconstructTime( timeExpires, timeExpiresMillis ),
                        data );
            }
            
            set.close();

            if( count < ret.length ) {
                ret = ArrayHelper.copyIntoNewArray( ret, 0, count, StoreValue.class );
            }
            
        } catch( SQLException ex ) {
            log.error( ex );
        }
        return ret;           
    }
    
    /**
     * Find the next n StoreValues, using the provided PreparedStatement.
     *
     * @param stm the SQL to use
     * @param key key for the first StoreValue
     * @param n the number of StoreValues to find
     * @return the found StoreValues
     */
    protected StoreValue [] findValues(
            SqlStorePreparedStatement stm,
            final String              key,
            final int                 n )
    {
        try {
            StoreValue [] ret = new SqlExecutionAction<StoreValue[]>( stm ) {
                protected StoreValue [] perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString( 1, key );
                    stm.setInt(    2, n );

                    stm.execute();
                    ResultSet set = stm.getResultSet();

                    // This needs to be performed in here to be within the same synchronized statement

                    StoreValue [] temp = new StoreValue[ n ];
                    int count = 0;
                    while( set.next() ) {
                        String    foundKey          = set.getString(    "id" );
                        String    encodingId        = set.getString(    "encodingId" );
                        Timestamp timeCreated       = set.getTimestamp( "timeCreated" );
                        int       timeCreatedMillis = set.getInt(       "timeCreatedMillis" );
                        Timestamp timeUpdated       = set.getTimestamp( "timeUpdated" );
                        int       timeUpdatedMillis = set.getInt(       "timeUpdatedMillis" );
                        Timestamp timeRead          = set.getTimestamp( "timeRead" );
                        int       timeReadMillis    = set.getInt(       "timeReadMillis" );
                        Timestamp timeExpires       = set.getTimestamp( "timeExpires" );
                        int       timeExpiresMillis = set.getInt(       "timeExpiresMillis" );
                        byte []   data              = set.getBytes(     "content" );

                        // this is different from the get() method -- we find the one after, not the same one
                        temp[count++] = new StoreValue(
                                foundKey,
                                encodingId,
                                reconstructTime( timeCreated, timeCreatedMillis ),
                                reconstructTime( timeUpdated, timeUpdatedMillis ),
                                reconstructTime( timeRead,    timeReadMillis ),
                                reconstructTime( timeExpires, timeExpiresMillis ),
                                data );
                    }
                    set.close();
                    
                    if( count < temp.length ) {
                        temp = ArrayHelper.copyIntoNewArray( temp, 0, count, StoreValue.class );
                    }
                    return temp;
                }
            }.execute();

            return ret;        
            
        } catch( SQLException ex ) {
            log.error( ex );

            return null;
        }
    }

    /**
     * Count the number of rows following and including the one with the key.
     *
     * @param key the key
     * @return the number of rows
     */
    protected int hasNextIncluding(
            String key )
    {
        if( key == null ) {
            return 0;
        }
        return countRows( theHasNextIncludingPreparedStatement, key );
    }

    /**
     * Count the number of rows preceding and excluding the one with the key.
     *
     * @param key the key
     * @return the number of rows
     */
    protected int hasPreviousExcluding(
            String key )
    {
        if( key == null ) {
            try {
                int ret = size();
                return ret;
            } catch( IOException ex ) {
                log.error( ex );
                return 0;
            }
        }
        return countRows( theHasPreviousExcludingPreparedStatement, key );
    }
    
    /**
     * Count the number of rows that meet the condition in the SQL statement.
     *
     * @param stm the SQL statement
     * @param key key parameter
     * @return the number of rows
     */
    protected int countRows(
            SqlStorePreparedStatement stm,
            final String              key )
    {
        try {
            int ret = new SqlExecutionAction<Integer>( stm ) {
                protected Integer perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString( 1, key );
                    stm.execute();
                    
                    ResultSet set = stm.getResultSet();
                    int ret;
                    if( set.next() ) {
                        ret = set.getInt( 1 );
                    } else {
                        ret = 0;
                    }
                    set.close();
                    return ret;
                }
            }.execute();

            return ret;

        } catch( SQLException ex ) {
            log.error( ex );
        }
        return Integer.MAX_VALUE; // Is this a reasonable default?        
    }

    /**
     * Find the key N rows up or down from the current key.
     *
     * @param key the current key
     * @param delta the number of rows up (positive) or down (negative)
     * @return the found key, or null
     */
    protected String findKeyAt(
            final String key,
            final int    delta )
    {
        if( key != null ) {

            SqlStorePreparedStatement stm;
            final int distance;
            if( delta >= 0 ) {
                stm = theFindKeyAtPositivePreparedStatement;
                distance = delta;
            } else {
                stm = theFindKeyAtNegativePreparedStatement;
                distance = -delta;
            }
            try {
                String ret = new SqlExecutionAction<String>( stm ) {
                    protected String perform(
                            PreparedStatement stm,
                            Connection        conn )
                        throws
                            SQLException
                    {
                        stm.setString( 1, key );
                        stm.setInt(    2, distance );
                        stm.execute();

                        ResultSet set = stm.getResultSet();
                        String ret;
                        if( set.last() ) {
                            ret = set.getString( 1 );
                        } else {
                            ret = null;
                        }
                        set.close();
                        return ret;
                    }
                }.execute();

                return ret;

            } catch( SQLException ex ) {
                log.error( ex );
            }
            
        } else {
            if( delta >= 0 ) {
                return null;
            }
            final int distance = -delta;
            try {
                String ret = new SqlExecutionAction<String>( theFindKeyAtEndPreparedStatement ) {
                    protected String perform(
                            PreparedStatement stm,
                            Connection        conn )
                        throws
                            SQLException
                    {
                        stm.setInt(    1, distance );
                        stm.execute();

                        ResultSet set = stm.getResultSet();
                        String ret;
                        if( set.last() ) {
                            ret = set.getString( 1 );
                        } else {
                            ret = null;
                        }
                        set.close();
                        return ret;
                    }
                }.execute();

                return ret;

            } catch( SQLException ex ) {
                log.error( ex );
            }            
        }
        return null;
    }

    /**
     * Determine the number of rows between two keys.
     *
     * @param from the start key
     * @param to the end key
     * @return the distance
     */
    protected int determineDistance(
            final String from,
            final String to )
    {
        try {
            int ret = new SqlExecutionAction<Integer>( theDetermineDistancePreparedStatement ) {
                protected Integer perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString( 1, from );
                    stm.setString( 2, to );
                    stm.execute();
                    
                    ResultSet set = stm.getResultSet();
                    int ret;
                    if( set.next() ) {
                        ret = set.getInt( 1 );
                    } else {
                        ret = 0;
                    }
                    set.close();
                    return ret;
                }
            }.execute();

            return ret;

        } catch( SQLException ex ) {
            log.error( ex );
        }
        return Integer.MAX_VALUE; // Is this a reasonable default?
    }

    /**
     * Determine the number of StoreValues in this Store whose key starts with this String.
     *
     * @param startsWith the String the key starts with
     * @return the number of StoreValues in this Store whose key starts with this String
     * @throws IOException thrown if an I/O error occurred
     */
    public int size(
            final String startsWith )
        throws
            IOException
    {
        checkKey( startsWith );

        try {
            int ret = new SqlExecutionAction<Integer>( theSizePreparedStatement ) {
                protected Integer perform(
                        PreparedStatement stm,
                        Connection        conn )
                    throws
                        SQLException
                {
                    stm.setString( 1, startsWith + '%' ); // trailing wild card
                    stm.execute();
                    
                    ResultSet set = stm.getResultSet();
                    int ret;
                    if( set.next() ) {
                        ret = set.getInt( 1 );
                    } else {
                        ret = 0;
                    }
                    set.close();
                    return ret;
                }
            }.execute();

            return ret;

        } catch( SQLException ex ) {
            log.error( ex );
        }
        return Integer.MAX_VALUE; // Is this a reasonable default?
    }

    /**
     * The createTables PreparedStatement.
     */
    protected SqlStorePreparedStatement theCreateTablesPreparedStatement;

    /**
     * The dropTables PreparedStatement.
     */
    protected SqlStorePreparedStatement theDropTablesPreparedStatement;

    /**
     * The hasTables PreparedStatement.
     */
    protected SqlStorePreparedStatement theHasTablesPreparedStatement;
    
    /**
     * The put PreparedStatement.
     */
    protected SqlStorePreparedStatement thePutPreparedStatement;

    /**
     * The update PreparedStatement.
     */
    protected SqlStorePreparedStatement theUpdatePreparedStatement;

    /**
     * The get PreparedStatement.
     */
    protected SqlStorePreparedStatement theGetPreparedStatement;

    /**
     * The delete PreparedStatement.
     */
    protected SqlStorePreparedStatement theDeletePreparedStatement;

    /**
     * The delete-all PreparedStatement.
     */
    protected SqlStorePreparedStatement theDeleteAllPreparedStatement;

    /**
     * The size PreparedStatement.
     */
    protected SqlStorePreparedStatement theSizePreparedStatement;

    /**
     * The PreparedStatement to obtain the next N rows including the specified key's.
     */
    protected SqlStorePreparedStatement theFindNextIncludingPreparedStatement;

    /**
     * The PreparedStatement to obtain the previous N rows excluding the specified key's.
     */
    protected SqlStorePreparedStatement theFindPreviousExcludingPreparedStatement;
    
    /**
     * The PreparedStatement to obtain the last N rows in the table.
     */
    protected SqlStorePreparedStatement theFindLastValuesPreparedStatement;
    
    /**
     * The PreparedStatement to obtain the number of rows including and after this key.
     */
    protected SqlStorePreparedStatement theHasNextIncludingPreparedStatement;
    
    /**
     * The PreparedStatement to obtain the number of rows excluding and before this key.
     */
    protected SqlStorePreparedStatement theHasPreviousExcludingPreparedStatement;
    
    /**
     * The PreparedStatement to find the key of N rows ahead.
     */
    protected SqlStorePreparedStatement theFindKeyAtPositivePreparedStatement;
    
    /**
     * The PreparedStatement to find the key of N rows back.
     */
    protected SqlStorePreparedStatement theFindKeyAtNegativePreparedStatement;
    
    /**
     * The PreparedStatement to find the key of N rows from the end of the table;
     */
    protected SqlStorePreparedStatement theFindKeyAtEndPreparedStatement;
    
    /**
     * The PreparedStatement to find the number of rows between two keys.
     */
    protected SqlStorePreparedStatement theDetermineDistancePreparedStatement;
    
    /**
     * The SQL to create the tables in the database.
     */
    protected static final String CREATE_TABLES_SQL
            = "CREATE TABLE {0} (\n"
            + "    id                    VARCHAR(511) NOT NULL PRIMARY KEY,\n" // this automatically creates an index
            + "    encodingId            VARCHAR(128),\n"
            + "    timeCreated           TIMESTAMP,\n"
            + "    timeCreatedMillis     SMALLINT,\n"
            + "    timeUpdated           TIMESTAMP,\n"
            + "    timeUpdatedMillis     SMALLINT,\n"
            + "    timeRead              TIMESTAMP,\n"
            + "    timeReadMillis        SMALLINT,\n"
            + "    timeExpires           TIMESTAMP,\n"
            + "    timeExpiresMillis     SMALLINT,\n"
            + "    content               BYTEA"
            + ");";

    /**
     * The SQL to drop the tables in the database.
     */
    protected static final String DROP_TABLES_SQL
            = "DROP TABLE {0};";

    /**
     * The SQL to detect whether or not the tables exist in the database.
     */
    protected static final String HAS_TABLES_SQL
            = "SELECT COUNT(*) from TABLE {0} WHERE id = '';";

    /**
     * The SQL to put data into the Store.
     */
    protected static final String PUT_SQL
            = "INSERT INTO {0} (\n"
            + "    id,\n"
            + "    encodingId,\n"
            + "    timeCreated,\n"
            + "    timeCreatedMillis,\n"
            + "    timeUpdated,\n"
            + "    timeUpdatedMillis,\n"
            + "    timeRead,\n"
            + "    timeReadMillis,\n"
            + "    timeExpires,\n"
            + "    timeExpiresMillis,\n"
            + "    content )\n"
            + "VALUES(\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ?,\n"
            + "    ? );";

    /**
     * The SQL to update data in the Store.
     */
    protected static final String UPDATE_SQL
            = "UPDATE {0} SET\n"
            + "    encodingId            = ?,\n"
            + "    timeCreated           = ?,\n"
            + "    timeCreatedMillis     = ?,\n"
            + "    timeUpdated           = ?,\n"
            + "    timeUpdatedMillis     = ?,\n"
            + "    timeRead              = ?,\n"
            + "    timeReadMillis        = ?,\n"
            + "    timeExpires           = ?,\n"
            + "    timeExpiresMillis     = ?,\n"
            + "    content = ?"
            + "WHERE id = ? ;";

    /**
     * The SQL to get data from the Store.
     */
    protected static final String GET_SQL
            = "SELECT * from {0} WHERE id = ?";

    /**
     * The SQL to delete data in the Store.
     */
    protected static final String DELETE_SQL
            = "DELETE FROM {0} WHERE id = ?";

    /**
     * The SQL to obtain the next n StoreValues in the Store.
     */
    protected static final String FIND_NEXT_INCLUDING_SQL
            = "SELECT * from {0} WHERE id >= ? ORDER BY id LIMIT ?;";

    /**
     * The SQL to obtain the previous n StoreValues in the Store.
     */
    protected static final String FIND_PREVIOUS_EXCLUDING_SQL
            = "SELECT * from {0} WHERE id < ? ORDER BY id DESC LIMIT ?;";

    /**
     * The SQL to obtain the last n StoreValues in the Store.
     */
    protected static final String FIND_LAST_VALUES_SQL
            = "SELECT * from {0} ORDER BY id DESC LIMIT ?;";
    
    /**
     * The SQL to obtain the number of rows including and after this key.
     */
    protected static final String HAS_NEXT_INCLUDING_SQL
            = "SELECT COUNT(*) from {0} WHERE id >= ?;";
            
    /**
     * The SQL to obtain the number of rows excluding and before this key.
     */
    protected static final String HAS_PREVIOUS_EXCLUDING_SQL
            = "SELECT COUNT(*) from {0} WHERE id < ?;";
            
    /**
     * The SQL to find the key N rows ahead.
     */
    protected static final String FIND_KEY_AT_POSITIVE_SQL
            = "SELECT id from {0} WHERE id >= ? ORDER BY id LIMIT ?";

    /**
     * The SQL to find the key N rows back.
     */
    protected static final String FIND_KEY_AT_NEGATIVE_SQL
            = "SELECT id from {0} WHERE id < ? ORDER BY id DESC LIMIT ?";

    /**
     * The SQL to find the key N rows from the end of the table.
     */
    protected static final String FIND_KEY_AT_END_SQL
            = "SELECT id from {0} ORDER BY id DESC LIMIT ?";
    
    /**
     * The SQL to determine the distance, in rows, between a first and a second key.
     */
    protected static final String DETERMINE_DISTANCE_SQL
            = "SELECT COUNT(*) from {0} WHERE id > ? AND id <= ?";

    /**
     * The SQL to delete ALL data in the Store.
     */
    protected static final String DELETE_ALL_SQL
            = "DELETE FROM {0} WHERE id LIKE ?;";

    /**
     * The SQL to determine the number of entries in the entire Store.
     */
    protected static final String SIZE_SQL
            = "SELECT COUNT(*) FROM {0} WHERE id LIKE ?;";
}
