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

package org.infogrid.store.sql.test;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.infogrid.store.test.AbstractStoreIteratorTest2;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.store.sql.postgresql.PostgresqlStore;
import org.infogrid.util.logging.Log;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Tests the SqlStoreIterator.
 */
public class SqlStoreIteratorTest2
        extends
            AbstractStoreIteratorTest2
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        SqlStoreIteratorTest2 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <database engine>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlStoreIteratorTest2( args );
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
    public SqlStoreIteratorTest2(
            String [] args )
        throws
            Exception
    {
        super( localFileName( SqlStoreIteratorTest2.class, "/ResourceHelper" ));

        String dataBaseEngine = args[0];

        if( "mysql".equalsIgnoreCase( dataBaseEngine ) ) {
            MysqlDataSource theDataSource = new MysqlDataSource();
            theDataSource.setDatabaseName( AbstractSqlStoreTest.test_DATABASE_NAME );

            theTestStore = MysqlStore.create( theDataSource, AbstractSqlStoreTest.test_TABLE_NAME );

        } else if( "postgresql".equalsIgnoreCase( dataBaseEngine )) {
            PGSimpleDataSource theDataSource = new PGSimpleDataSource();
            theDataSource.setDatabaseName( AbstractSqlStoreTest.test_DATABASE_NAME );
            theDataSource.setUser( "test" );
            theDataSource.setPassword( "" );

            theTestStore = PostgresqlStore.create( theDataSource, AbstractSqlStoreTest.test_TABLE_NAME );

        } else {
            throw new IllegalArgumentException( "Don't know about a database engine called " + dataBaseEngine );
        }
    }

    // Our Logger
    private static Log log = Log.getLogInstance( SqlStoreIteratorTest2.class );
}
