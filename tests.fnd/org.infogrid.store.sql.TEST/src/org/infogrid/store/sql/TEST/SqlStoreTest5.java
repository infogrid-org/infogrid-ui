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

package org.infogrid.store.sql.TEST;

import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreValue;
import org.infogrid.store.sql.SqlStoreIOException;
import org.infogrid.store.util.DynamicLoadFromStoreMap;
import org.infogrid.util.logging.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import org.infogrid.store.Store;

/**
 * Tests the DynamicLoadFromStoreMap.
 */
public class SqlStoreTest5
        extends
            AbstractSqlStoreTest
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
        
        log.info( "Deleting old database and creating new database" );
        
        theSqlStore.initializeHard();
        
        //
        
        MyTestMap testMap = new MyTestMap( theSqlStore, "testentry" );
        
        testMap.putAll( testData );
        
        checkObject( testMap.getDelegate(), "no delegate, but have put data in" );
        
        //
        
        log.info( "Removing local cache" );
        
        testMap.clearLocalCache();
        
        checkCondition( testMap.getDelegate() == null, "has delegate but should not" );
        
        //
        
        log.info( "Accessing data" );
        
        for( int key : testData.keySet() ) {
            String testValue = testData.get( key );
            String actualValue = testMap.get( key );
            
            checkEquals( testValue, actualValue, "values not the same for key " + key );
        }
        checkObject( testMap.getDelegate(), "no delegate, but have recovered data" );
        
        //
        
        log.info( "Modifying data and saving again" );
        
        int    additionalKey   = 111;
        String additionalValue = "hundredeleven";
        
        testMap.put( additionalKey, additionalValue );
        
        testMap.clearLocalCache();

        //
        
        log.info( "Accessing data" );
        
        for( int key : testData.keySet() ) {
            String testValue = testData.get( key );
            String actualValue = testMap.get( key );

            checkEquals( testValue, actualValue, "values not the same for key " + key );
        }
        String actualValue = testMap.get( additionalKey );
        checkEquals( additionalValue, actualValue, "values not the same for key " + additionalKey );
        
        checkObject( testMap.getDelegate(), "no delegate, but have recovered data" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        SqlStoreTest5 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <database engine> <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlStoreTest5( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            System.exit(1);
        }
        if( test != null ) {
            test.cleanup();
        }
        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.info( "FAIL (" + errorCount + " errors)" );
        }
        System.exit( errorCount );
    }

    /**
     * Constructor.
     *
     * @param args command-line arguments
     * @throws Exception all sorts of things may go wrong in a test
     */
    public SqlStoreTest5(
            String [] args )
        throws
            Exception
    {
        super( args[0], SqlStoreTest1.class );
        
        theTestStore = theSqlStore;
    }

    /**
     * Constructor for subclasses.
     *
     * @param dataBaseEngine the name of the database engine to use for testing
     * @param c test class
     * @throws Exception all sorts of things may go wrong in a test
     */
    protected SqlStoreTest5(
            String dataBaseEngine,
            Class  c )
        throws
            Exception
    {
        super( dataBaseEngine, c );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( SqlStoreTest5.class );
    

    /**
     * Test data.
     */
    static HashMap<Integer,String> testData = new HashMap<Integer,String>();
    static {
        testData.put( 1, "one" );
        testData.put( 2, "two" );
        testData.put( 4, "four" );
        testData.put( 6, "six" );
        testData.put( 7, "seven" );
        testData.put( 3, "three" );
        testData.put( 10, "ten" );
    }
    
    /**
     * Test map implementation.
     */
    static class MyTestMap
            extends
                DynamicLoadFromStoreMap<Integer,String>
    {
        /**
         * Constructor.
         * 
         * @param store the Store to load from
         * @param storeEntryKey the key used to write the entire content of the Map into the Store
         */
        public MyTestMap(
                Store  store,
                String storeEntryKey )
        {
            super( store, storeEntryKey );
        }
        
        /**
         * Override load method.
         * 
         * @return the new delegate HashMap
         * @throws StoreKeyDoesNotExistException thrown if no map content could be found
         * @throws IOException thrown if loading failed
         */
        @SuppressWarnings(value={"unchecked"})
        protected HashMap<Integer,String> load()
            throws
                StoreKeyDoesNotExistException,
                IOException
        {
            StoreValue storeValue = theStore.get( theStoreEntryKey );
            
            ObjectInputStream inStream = new ObjectInputStream( storeValue.getDataAsStream() );
            
            HashMap<Integer,String> ret = null;
            try {
                ret = (HashMap<Integer,String>) inStream.readObject();
            } catch( ClassNotFoundException ex ) {
                log.error( ex );
            }
            return ret;
        }
        
        /**
         * Override save method.
         * 
         * @throws IOException thrown if saving failed
         */
        protected void save()
            throws
                IOException
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            ObjectOutputStream outStream = new ObjectOutputStream( buffer );
            
            outStream.writeObject( theDelegate );
            
            outStream.close();
            
            long now = System.currentTimeMillis();
            
            theStore.putOrUpdate( theStoreEntryKey, "", now, now, now, -1L, buffer.toByteArray() );
        }
        
        /**
         * Obtain delegate, this is for test instrumentation.
         * 
         * @return the delegate
         */
        public HashMap<Integer,String> getDelegate()
        {
            return theDelegate;
        }

        /**
         * Invoked only by objects held in this DynamicLoadFromStoreMap, this enables
         * the held objects to indicate to the DynamicLoadFromStoreMap that they have been updated.
         * Depending on the implementation of the DynamicLoadFromStoreMap, that may cause the
         * DynamicLoadFromStoreMap to write changes to disk, for example.
         *
         * @param key the key
         * @param value the value
         */
        public void valueUpdated(
                Integer key,
                String  value )
        {
            log.debug( "ValueUpdated" );
        }
    }
}
