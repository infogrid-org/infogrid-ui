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

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.SQLException;
import org.infogrid.store.IterableStore;
import org.infogrid.store.sql.AbstractSqlStore;
import org.infogrid.store.sql.mysql.MysqlStore;
import org.infogrid.store.sql.postgresql.PostgresqlStore;
import org.infogrid.testharness.AbstractTest;
import org.postgresql.ds.PGSimpleDataSource;

/**
 * Factors out common functionality of SqlStoreTests.
 */
public abstract class AbstractSqlStoreTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     * 
     * @param dataBaseEngine the name of the database engine to use for testing
     * @param testClass the actual test Class
     * @throws SQLException thrown if the AbstractSqlStore could not be accessed
     */
    public AbstractSqlStoreTest(
            String dataBaseEngine,
            Class  testClass )
        throws
            SQLException
    {
        super( localFileName( testClass, "/ResourceHelper" ));
        
        if( "mysql".equalsIgnoreCase( dataBaseEngine ) ) {
            MysqlDataSource theDataSource = new MysqlDataSource();
            theDataSource.setDatabaseName( test_DATABASE_NAME );
            
            theSqlStore = MysqlStore.create( theDataSource, test_TABLE_NAME );
        
        } else if( "postgresql".equalsIgnoreCase( dataBaseEngine )) {
            PGSimpleDataSource theDataSource = new PGSimpleDataSource();
            theDataSource.setDatabaseName( test_DATABASE_NAME );
            theDataSource.setUser( "test" );
            theDataSource.setPassword( "" );
            
            theSqlStore = PostgresqlStore.create( theDataSource, test_TABLE_NAME );
            
        } else {
            throw new IllegalArgumentException( "Don't know about a database engine called " + dataBaseEngine );
        }
    }

    /**
     * The AbstractSqlStore to be tested.
     */
    protected AbstractSqlStore theSqlStore;
    
    /**
     * The actual Store to be tested. This may or may not be pointed to theSqlStore
     * by subclasses.
     */
    protected IterableStore theTestStore;

    /**
     * The name of the database that we use to store test data.
     */
    public static final String test_DATABASE_NAME = "test";

    /**
     * The name of the table that we use to store test data.
     */
    public static final String test_TABLE_NAME = "SqlStoreTest";

    /**
     * Holds the driver.
     */
    static Object theSqlDriver;

    static {
        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            theSqlDriver = Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
        }        
    }
    
    /**
     * The EncodingId for the tests.
     */
    public static final String ENCODING_ID = "TestEncodingId";
}
