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

package org.infogrid.lid;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown if a LID nonce is invalid. Inner classes are concrete and capture the specifics.
 */
public abstract class LidInvalidNonceException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param invalidNonce the invalid nonce
     */
    public LidInvalidNonceException(
            String invalidNonce )
    {
        theInvalidNonce = invalidNonce;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theInvalidNonce };
    }

    /**
     * The invalid nonce.
     */
    protected String theInvalidNonce;
    
    /**
     * The nonce was not given or empty.
     */
    public static class Empty
            extends
                LidInvalidNonceException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         */
        public Empty()
        {
            super( null );
        }
    }

    /**
     * The time stamp on the nonce was out of range.
     */
    public static class InvalidTimeRange
            extends
                LidInvalidNonceException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param invalidNonce the invalid nonce
         */
        public InvalidTimeRange(
                String invalidNonce )
        {
            super( invalidNonce );
        }
    }

    /**
     * The nonce was not known.
     */
    public static class NotKnown
            extends
                LidInvalidNonceException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param invalidNonce the invalid nonce
         */
        public NotKnown(
                String invalidNonce )
        {
            super( invalidNonce );
        }
    }
}
