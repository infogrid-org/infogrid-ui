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

package org.infogrid.lid.openid;

/**
 * This Exception is thrown when an error occurred while attempting to set up or
 * renew an OpenID Association.
 */
public abstract class AssociationException
    extends
        Exception
{
    /**
     * Constructor.
     *
     * @param cause the Exception that caused this Exception
     */
    protected AssociationException(
            Exception cause )
    {
        super( cause );
    }

    /**
     * This Exception is thrown if we don't understand the provided association type.
     */
    public static class UnknownAssociationType
        extends
            AssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param unknownType the type of Association that was unknown
         */
        public UnknownAssociationType(
                String unknownType )
        {
            super( null );
            
            theUnknownType = unknownType;
        }

        /**
         * The type of association that was not known.
         */
        protected String theUnknownType;
    }

    /**
     * This Exception is thrown if we don't understand the provided session type.
     */
    public static class UnknownSessionType
        extends
            AssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param unknownType the type of session that was unknown
         */
        public UnknownSessionType(
                String unknownType )
        {
            super( null );
            
            theUnknownType = unknownType;
        }

        /**
         * The type of association that was not known.
         */
        protected String theUnknownType;
    }

    /**
     * This Exception is thrown if the association could not be created because of an invalid
     * expiration time.
     */
    public static class InvalidExpiration
        extends
            AssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         */
        public InvalidExpiration()
        {
            super( null );
        }
    }

    /**
     * This Exception is thrown if the shared secret was invalid (e.g. wrong length).
     */
    public static class InvalidSecret
        extends
            AssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         */
        public InvalidSecret()
        {
            super( null );
        }
    }

    /**
     * This Exception is thrown if there was a syntax error in an OpenID field.
     */
    public static class SyntaxError
        extends
            AssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param cause the Exception that caused this Exception
         */
        public SyntaxError(
                Exception cause )
        {
            super( cause );
        }
    }
}
