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

import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidProcessingPipelineStage;

/**
 * This Exception is thrown when an error occurred while attempting to set up or
 * renew an OpenID Association.
 */
public abstract class OpenIdAssociationException
    extends
        LidAbortProcessingPipelineException
{
    /**
     * Constructor.
     *
     * @param source the LidProcessingPipelineStage that threw this exception
     * @param cause the Exception that caused this Exception
     */
    protected OpenIdAssociationException(
            LidProcessingPipelineStage source,
            Throwable                  cause )
    {
        super( source, cause );
    }

    /**
     * This Exception is thrown if we don't understand the provided association type.
     */
    public static class UnknownAssociationType
        extends
            OpenIdAssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the LidProcessingPipelineStage that threw this exception
         * @param unknownType the type of Association that was unknown
         */
        public UnknownAssociationType(
                LidProcessingPipelineStage source,
                String                     unknownType )
        {
            super( source, null );
            
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
            OpenIdAssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the LidProcessingPipelineStage that threw this exception
         * @param unknownType the type of session that was unknown
         */
        public UnknownSessionType(
                LidProcessingPipelineStage source,
                String                     unknownType )
        {
            super( source, null );
            
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
            OpenIdAssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         * 
         * @param source the LidProcessingPipelineStage that threw this exception
         */
        public InvalidExpiration(
                LidProcessingPipelineStage source )
        {
            super( source, null );
        }
    }

    /**
     * This Exception is thrown if the shared secret was invalid (e.g. wrong length).
     */
    public static class InvalidSecret
        extends
            OpenIdAssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         * 
         * @param source the LidProcessingPipelineStage that threw this exception
         */
        public InvalidSecret(
                LidProcessingPipelineStage source )
        {
            super( source, null );
        }
    }

    /**
     * This Exception is thrown if there was a syntax error in an OpenID field.
     */
    public static class SyntaxError
        extends
            OpenIdAssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the LidProcessingPipelineStage that threw this exception
         * @param cause the cause of this Exception
         */
        public SyntaxError(
                LidProcessingPipelineStage source,
                Throwable                  cause )
        {
            super( source, cause );
        }
    }
    
    /**
     * This Exception is thrown if there was an invalid public key.
     */
    public static class InvalidPublicKey
        extends
            OpenIdAssociationException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         * 
         * @param source the LidProcessingPipelineStage that threw this exception
         */
        public InvalidPublicKey(
                LidProcessingPipelineStage source )
        {
            super( source, null );
        }
    }
}
