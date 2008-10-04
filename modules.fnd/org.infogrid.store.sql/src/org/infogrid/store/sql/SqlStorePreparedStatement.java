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

package org.infogrid.store.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import java.text.MessageFormat;

/**
 * Smart factory class for PreparedStatements, taking the current Connection into account.
 */
public class SqlStorePreparedStatement
{
    /**
     * Constructor.
     *
     * @param store the SqlStore this belongs to
     * @param sql the parameterized SQL for the PreparedStatement
     * @param arguments the open parameters for the SQL
     */
    public SqlStorePreparedStatement(
            SqlStore  store,
            String    sql,
            Object... arguments )
    {
        theStore = store;
        theSql   = MessageFormat.format( sql, arguments );
    }

    /**
     * Obtain a PreparedStatement for this Connection.
     *
     * @param conn the Connection
     * @return the PreparedStatement
     * @throws SQLException thrown when a database problem occurs
     */
    public synchronized PreparedStatement obtain(
            Connection conn )
        throws
            SQLException
    {
        if( conn != theConnection || thePreparedStatement == null ) {
            theConnection        = conn;
            thePreparedStatement = theConnection.prepareStatement( theSql ); 
        }
        return thePreparedStatement;
    }
    
    /**
     * Obtain the SqlStore that we belong to.
     *
     * @return the SqlStore
     */
    public SqlStore getStore()
    {
        return theStore;
    }

    /**
     * Close this PreparedStatement.
     *
     * @throws SQLException thrown when a database problem occurs
     */
    public void close()
        throws
            SQLException
    {
        if( thePreparedStatement != null ) {
            thePreparedStatement.close();
            thePreparedStatement = null;
        }
    }

    /**
     * Obtain the SQL code for this statement. This is primarily for debugging and logging.
     * 
     * @return the SQL code
     */
    public String getSql()
    {
        return theSql;
    }

    /**
     * The SqlStore we belong to.
     */
    protected SqlStore theStore;

    /**
     * The SQL for this statement.
     */
    protected String theSql;
    
    /**
     * The most-recently used Connection.
     */
    protected Connection theConnection;
    
    /**
     * The PreparedStatement for theConnection.
     */
    protected PreparedStatement thePreparedStatement;
}
