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

package org.infogrid.util;

/**
 * <p>This Exception is thrown when a Thread is waiting for some sort of result
 * to a query which requires communication with a remote entity, and
 * the query times out. This Exception is abstract, but two concrete implementations
 * are provided as inner classes.</p>
 *
 * <p>It implements PartialResultException, as it is possible that parts
 * of the results are already available by the time the query times out.</p>
 *
 * <p>We'd really like to make this a parameterized class, but apparently the
 * Java spec does not like that ("A generic class may not extend java.lang.Throwable").</p>
 */
public abstract class RemoteQueryTimeoutException
        extends
            AbstractLocalizedException
        implements
            PartialResultException
{
    /**
     * Constructor.
     *
     * @param localProxy the local proxy object which is the local endpoint of the query
     * @param havePartialResult if true, we have a partial result
     * @param partialResult the partial result
     */
    protected RemoteQueryTimeoutException(
            Object  localProxy,
            boolean havePartialResult,
            Object  partialResult )
    {
        theLocalProxy        = localProxy;
        theHavePartialResult = havePartialResult;
        thePartialResult     = partialResult;
    }

    /**
     * Obtain the local proxy Object which is the local endpoint of the query.
     *
     * @return the local proxy Object
     */
    public Object getLocalProxy()
    {
        return theLocalProxy;
    }

    /**
     * Determine whether a partial result is available.
     *
     * @return if true, a partial result is available
     */
    public boolean isPartialResultAvailable()
    {
        return theHavePartialResult;
    }

    /**
     * Obtain the partial result, if any.
     *
     * @return the partial result, if any
     */
    public Object getBestEffortResult()
    {
        return thePartialResult;
    }

    /**
     * Determine whether the complete result is now available.
     *
     * @return true of the complete result is now available
     */
    public boolean isCompleteResultAvailable()
    {
        return false;
    }
    
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
     * Allow subclasses to override which ResourceHelper to use.
     *
     * @return the ResourceHelper to use
     */
    @Override
    protected ResourceHelper findResourceHelperForLocalizedMessage()
    {
        return ResourceHelper.getInstance( RemoteQueryTimeoutException.class );
    }

    /**
     * All subclasses have a common ResourceHelper for all inner classes.
     *
     * @return the key
     */
    @Override
    protected String findStringRepresentationParameter()
    {
        return findParameterViaEnclosingClass( STRING_REPRESENTATION_KEY );
    }

    /**
     * All subclasses have a common ResourceHelper for all inner classes.
     *
     * @return the key
     */
    @Override
    protected String findStringRepresentationLinkStartParameter()
    {
        return findParameterViaEnclosingClass( STRING_REPRESENTATION_LINK_START_KEY );
    }

    /**
     * All subclasses have a common ResourceHelper for all inner classes.
     *
     * @return the key
     */
    @Override
    protected String findStringRepresentationLinkEndParameter()
    {
        return findParameterViaEnclosingClass( STRING_REPRESENTATION_LINK_END_KEY );
    }

    /**
     * The local proxy object.
     */
    protected Object theLocalProxy;

    /**
     * Flag indicating whether we have a partial result.
     */
    protected boolean theHavePartialResult;

    /**
     * The partial result (if any).
     */
    protected Object thePartialResult;

    /**
     * This subclass of RemoteTimeoutQueryException is being instantiated with
     * a static partial result object, if any.
     */
    public static class QueryIsOver
            extends
                RemoteQueryTimeoutException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Create one.
         *
         * @param localProxy the local proxy object which is the local endpoint of the query
         * @param havePartialResult if true, we have a partial result
         * @param partialResult the partial result
         */
        public QueryIsOver(
                Object  localProxy,
                boolean havePartialResult,
                Object  partialResult )
        {
            super( localProxy, havePartialResult, partialResult );
        }
    }

    /**
     * This subclass of RemoteTimeoutQueryException may obtain a partial, or even
     * complete result, after it has been thrown.
     */
    public static class QueryIsOngoing
            extends
                RemoteQueryTimeoutException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Create one with the partial result of the query at the time of creation, if any.
         * This may change over time from havePartialResult=false to havePartialResult=true
         * and then to haveCompleteResult=true by invocations of the local methods after
         * the constructor has been invoked, as results come in.
         *
         * @param localProxy the local proxy object which is the local endpoint of the query
         * @param havePartialResult if true, we have a partial result
         * @param partialResult the partial result
         */
        public QueryIsOngoing(
                Object  localProxy,
                boolean havePartialResult,
                Object  partialResult )
        {
            super( localProxy, havePartialResult, partialResult );
        }

        /**
         * This can be invoked by the entity that previously threw this Exception
         * in order to indicate that a more complete partial result is now available.
         *
         * @param newResult a new result Object replacing the old partial result
         */
        public void haveMoreCompletePartialResult(
                Object newResult )
        {
            theHavePartialResult = true;
            thePartialResult     = newResult;
        }

        /**
         * This can be invoked by the entity that previous threw this Exception
         * in order to indicate that the complete result is now available.
         *
         * @param newResult the new result Object replacing the old partial result
         */
        public void haveCompleteResult(
                Object newResult )
        {
            theHavePartialResult  = true;
            theHaveCompleteResult = true;
            thePartialResult      = newResult;
        }

        /**
         * dDtermine whether the complete result is now available.
         *
         * @return true if the complete result is now available
         */
        @Override
        public boolean isCompleteResultAvailable()
        {
            return theHaveCompleteResult;
        }

        /**
         * Flag set to true when the complete result has arrived.
         */
        protected boolean theHaveCompleteResult = false;
    }
}
