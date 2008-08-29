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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.infogrid.store.StoreValue;
import org.infogrid.store.keystore.KeyStoreWrapper;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.logging.Log;

/**
 * Tests the Keystore.
 */
public class SqlKeyStoreTest1
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
        
        theSqlStore.deleteStore();
        theSqlStore.initialize();
        

        // 
        
        log.info( "Creating the KeyStoreWrapper" );
        
        KeyStoreWrapper wrapper = KeyStoreWrapper.create( theTestStore, KEY_INTO_STORE, theKeyStorePassword );
        
        InputStream inStream = SqlKeyStoreTest1.class.getResourceAsStream( theKeyStoreFile );
        wrapper.load( inStream );

        wrapper = null;
        collectGarbage();

        //
        
        log.info( "checking that the content is in the Store" );
        
        StoreValue v = theTestStore.get( KEY_INTO_STORE );
        checkObject( v, "No StoreValue found" );
        checkCondition( v.getData().length > 0, "empty data in store" );
        
        //
        
        log.info( "trying to recover" );
        
        wrapper = KeyStoreWrapper.create( theTestStore, KEY_INTO_STORE, theKeyStorePassword );

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        wrapper.store( outStream );
        
        byte [] dataFromStore = outStream.toByteArray();
        byte [] dataFromFile  = StreamUtils.slurp( SqlKeyStoreTest1.class.getResourceAsStream( theKeyStoreFile ));
        
        checkEqualByteArrays( dataFromFile, dataFromStore, "different content" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        SqlKeyStoreTest1 test = null;
        try {
            if( args.length != 2 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <key store file name> <key store password>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlKeyStoreTest1( args );
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
      */
    public SqlKeyStoreTest1(
            String [] args )
        throws
            Exception
    {
        super( SqlKeyStoreTest1.class );
        
        theKeyStoreFile     = args[0];
        theKeyStorePassword = args[1];

        theTestStore = theSqlStore;
    }

    // Our Logger
    private static Log log = Log.getLogInstance( SqlKeyStoreTest1.class );
    
    /**
     * Key into to the store for the key Store content.
     */
    public static final String KEY_INTO_STORE = "key-store-test-key";
    
    /**
     * THe test file.
     */
    protected String theKeyStoreFile;
    
    /**
     * The password on the key store.
     */
    protected String theKeyStorePassword;
}
