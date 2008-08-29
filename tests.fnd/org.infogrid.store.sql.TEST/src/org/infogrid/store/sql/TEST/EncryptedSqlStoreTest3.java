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

import org.infogrid.store.encrypted.IterableEncryptedStore;
import org.infogrid.util.logging.Log;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Same as {@link SqlStoreTest3 SqlStoreTest3}, but with an EncryptedStore in between.
 */
public class EncryptedSqlStoreTest3
        extends
            SqlStoreTest3
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        EncryptedSqlStoreTest3 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <encryption transformation>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new EncryptedSqlStoreTest3( args );
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
    public EncryptedSqlStoreTest3(
            String [] args )
        throws
            Exception
    {
        super( EncryptedSqlStoreTest3.class );
        
        theTransformation = args[0];
        
        theSecretKey      = KeyGenerator.getInstance( theTransformation ).generateKey();
        theEncryptedStore = IterableEncryptedStore.create( theTransformation, theSecretKey, theSqlStore );
        
        theTestStore = theEncryptedStore;
    }
    
    /**
     * The encrypted Store in the middle.
     */
    protected IterableEncryptedStore theEncryptedStore;

    /**
     * The algorithm.
     */
    protected String theTransformation;

    /**
     * The private key to use.
     */
    protected SecretKey theSecretKey;
    
    // Our Logger
    private static Log log = Log.getLogInstance( EncryptedSqlStoreTest3.class);
}
