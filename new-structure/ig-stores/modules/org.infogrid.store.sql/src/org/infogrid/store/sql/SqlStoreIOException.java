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

package org.infogrid.store.sql;

import java.sql.SQLException;
import org.infogrid.util.DelegatingIOException;
import org.infogrid.util.text.HasStringRepresentation;

/**
 * <p>An <code>IOException</code> that delegates to a <code>SQLException</code>.</p>
 */
public class SqlStoreIOException
        extends
            DelegatingIOException
        implements
            HasStringRepresentation
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public SqlStoreIOException(
            SQLException cause )
    {
        super( "SQL Exception", cause );
    }
    
    /**
     * Obtain the underlying cause which we know to be a SQLException.
     * 
     * @return the cause
     */
    @Override
    public SQLException getCause()
    {
        return (SQLException) super.getCause();
    }
}
