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

package org.infogrid.meshbase.transaction;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown if a problem occurred during the execution of a TransactionAction.
 */
public abstract class TransactionActionException
    extends
        AbstractLocalizedException
{
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return null;
    }

    /**
     * Indicates that the Transaction needs to be rolled back, and that
     * a new attempt to execute the TransactionAction should not be
     * attempted.
     */
    public static class Rollback
            extends
                TransactionActionException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Default constructor.
         */
        public Rollback()
        {
            // nothing
        }
    }

    /**
     * Indicates that the Transaction needs to be rolled back, and that
     * a new attempt to execute the TransactionAction should be attempted.
     */
    public static class Retry
            extends
                TransactionActionException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Default constructor.
         */
        public Retry()
        {
        }
    }
}
