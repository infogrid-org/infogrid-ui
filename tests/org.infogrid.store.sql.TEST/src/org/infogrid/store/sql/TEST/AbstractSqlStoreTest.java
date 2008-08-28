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

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import java.sql.SQLException;
import org.infogrid.store.IterableStore;
import org.infogrid.store.sql.SqlStore;
import org.infogrid.testharness.AbstractTest;

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
     * @param testClass the actual test Class
     * @throws SQLException thrown if the SqlStore could not be accessed
     */
    public AbstractSqlStoreTest(
            Class testClass )
        throws
            SQLException
    {
        super( localFileName( testClass, "/ResourceHelper" ),
               localFileName( testClass, "/Log.properties" ));
        
        theDataSource = new MysqlDataSource();
        theDataSource.setDatabaseName( TEST_DATABASE_NAME );
        
        theSqlStore = SqlStore.create( theDataSource, TEST_TABLE_NAME );
    }

    /**
     * The Database connection.
     */
    protected MysqlDataSource theDataSource;

    /**
     * The SqlStore to be tested.
     */
    protected SqlStore theSqlStore;
    
    /**
     * The actual Store to be tested. This may or may not be pointed to theSqlStore
     * by subclasses.
     */
    protected IterableStore theTestStore;

    /**
     * The name of the database that we use to store test data.
     */
    public static final String TEST_DATABASE_NAME = "test";

    /**
     * The name of the table that we use to store test data.
     */
    public static final String TEST_TABLE_NAME = "SqlStoreTest";

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
