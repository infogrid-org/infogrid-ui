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

import org.infogrid.util.logging.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * <p>This class wraps SQL execution, and helps us with obtaining a new Connection
 * if an old one fails without having to expose this to the application programmer.
 * It is generally used by subclassing it with an anonymous class.</p>
 *
 * <p>This class is parameterized with the return type of the execution command.</p>
 * 
 * @param <R> the type of return value of the execution
 */
abstract class SqlExecutionAction<R>
{
    private static final Log log = Log.getLogInstance( SqlExecutionAction.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param stm the SqlStorePreparedStatement to execute
     */
    protected SqlExecutionAction(
            SqlStorePreparedStatement stm )
    {
        theStatement = stm;
    }

    /**
     * Execute the action.
     *
     * @return the return value, if any
     * @throws SQLException a SQL exception occurred
     */
    public R execute()
        throws
            SQLException
    {
        SqlStore   store = theStatement.getStore();        
        Connection conn  = store.obtainConnection();
        
        synchronized( conn ) {
            track();

            PreparedStatement stm = null;
            try {
                stm = theStatement.obtain( conn );
                R ret = perform( stm, conn );

                return ret;

            } catch( SQLException ex ) {
                theStatement.close();
            }

            conn = store.obtainNewConnection();

            // Get a new connection and try again. This time pass on any Exceptions.
            boolean error = true;

            try {
                stm = theStatement.obtain( conn );
                R ret = perform( stm, conn );

                error = false;

                return ret;

            } finally {
                // this funny construct is to not having to catch the Exception while
                // still closing the SqlStorePreparedStatement

                if( error ) {
                    theStatement.close();
                }
            }
        }
    }

    /**
     * Execute the actual SQL.
     * 
     * @param stm the PreparedStatement to execute
     * @param conn the Connection object to use
     * @return the return value
     * @throws SQLException thrown if the SQL could not be executed
     */
    protected abstract R perform(
            PreparedStatement stm,
            Connection        conn )
        throws
            SQLException;
    
    /**
     * Helper to track the execution of a SQL statement.
     */
    protected void track()
    {
        if( log.isInfoEnabled() ) {
            log.info( "Executing SQL: " + theStatement.getSql() );
        }
    }
    
    /**
     * The SqlStorePreparedStatement we execute.
     */
    protected SqlStorePreparedStatement theStatement;
}
