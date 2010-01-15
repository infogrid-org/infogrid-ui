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

package org.infogrid.store.sql.test;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.infogrid.store.encrypted.IterableEncryptedStore;
import org.infogrid.util.logging.Log;

/**
 * Same as {@link SqlStoreTest2 SqlStoreTest2}, but with an EncryptedStore in between.
 */
public class EncryptedSqlStoreTest2
        extends
            SqlStoreTest2
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        EncryptedSqlStoreTest2 test = null;
        try {
            if( args.length != 2 ) {
                System.err.println( "Synopsis: <database engine> <encryption transformation>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new EncryptedSqlStoreTest2( args );
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
     * @throws Exception all sorts of things may happen in a test
     */
    public EncryptedSqlStoreTest2(
            String [] args )
        throws
            Exception
    {
        super( args[0], EncryptedSqlStoreTest2.class );
        
        theTransformation = args[1];
        
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
    private static Log log = Log.getLogInstance( EncryptedSqlStoreTest2.class);
}
