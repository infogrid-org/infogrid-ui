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

package org.infogrid.meshbase.net;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.util.ResourceHelper;

/**
 * Captures the requirements for coherence between NetMeshBases. This is an abstract
 * class; concrete subclasses are defined as inner classes.
 */
public abstract class CoherenceSpecification
{
    /**
     * Parse a CoherenceSpecification's external form and instantiate the right subclass.
     *
     * @param ext the external form
     * @return the created CoherenceSpecification
     */
    public static CoherenceSpecification fromExternalForm(
            String ext )
    {
        CoherenceSpecification ret = null;
        
        if( ONE_TIME_ONLY_TAG.equals( ext )) {
            return ONE_TIME_ONLY;
        }
        if( ONE_TIME_ONLY_FAST_TAG.equals( ext )) {
            return ONE_TIME_ONLY_FAST;
        }

        ret = Periodic.fromExternalForm( ext );
        if( ret != null ) {
            return ret;
        }

        ret = AdaptivePeriodic.fromExternalForm( ext );
        if( ret != null ) {
            return ret;
        }

        throw new IllegalArgumentException( "Cannot parse CoherenceSpecification " + ext );
    }

    /**
     * Creates a default CoherenceSpecification if no other CoherenceSpecification is available
     * to an application.
     *
     * @return the created CoherenceSpecification
     */
    public static CoherenceSpecification getDefault()
    {
        return theDefaultCoherenceSpecification;
    }

    /**
     * Constructor, for subclasses only.
     *
     * @param waitForOngoingResynchronization if true, a response should wait until all resynchronization attempts have completed
     */
    protected CoherenceSpecification(
            boolean waitForOngoingResynchronization )
    {
        theWaitForOngoingResynchronization = waitForOngoingResynchronization;
    }

    /**
     * Determine whether a response should wait until all resynchronization attempts have completed.
     *
     * @return true if the response should wait
     */
    public boolean getWaitForOngoingResynchronization()
    {
        return theWaitForOngoingResynchronization;
    }

    /**
     * Obtain the external form of this CoherenceSpecification.
     *
     * @return the external form
     */
    public abstract String toExternalForm();

    /**
     * Obtain String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return toExternalForm();
    }

    /**
     * If true, a response should wait until all resynchronization attempts have completed.
     */
    protected boolean theWaitForOngoingResynchronization;

    /**
     * This (slightly degenerate) CoherenceSpecification indicates that coherence is only expected
     * once, at the beginning, and no further updates are necessary. However, it waits until all
     * resynchronization attempts have completed.
     */
    public static final CoherenceSpecification ONE_TIME_ONLY = new CoherenceSpecification( true ) {
            /**
             * Obtain the external form of this CoherenceSpecification.
             *
             * @return the external form
             */
            public String toExternalForm()
            {
                return ONE_TIME_ONLY_TAG;
            }
    };

    /**
     * This (slightly degenerate) CoherenceSpecification indicates that coherence is only expected
     * once, at the beginning, and no further updates are necessary. However, it does not wait until all
     * resynchronization attempts have completed.
     */
    public static final CoherenceSpecification ONE_TIME_ONLY_FAST = new CoherenceSpecification( false ) {
            /**
             * Obtain the external form of this CoherenceSpecification.
             *
             * @return the external form
             */
            public String toExternalForm()
            {
                return ONE_TIME_ONLY_FAST_TAG;
            }
    };

    /**
     * The name of the externalization tag for the ONE_TIME_ONLY instance.
     */
    public static final String ONE_TIME_ONLY_TAG = CoherenceSpecification.class.getName() + ".ONE_TIME_ONLY";

    /**
     * The name of the externalization tag for the ONE_TIME_ONLY_FAST instance.
     */
    public static final String ONE_TIME_ONLY_FAST_TAG = CoherenceSpecification.class.getName() + ".ONE_TIME_ONLY_FAST";

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( CoherenceSpecification.class );
    
    /**
     * The default CoherenceSpecification.
     */
    protected static final CoherenceSpecification theDefaultCoherenceSpecification = new AdaptivePeriodic(
            theResourceHelper.getResourceLongOrDefault(    "DefaultAdaptiveFallbackDelay",           60L * 60L * 1000L ), // 1 hour
            theResourceHelper.getResourceLongOrDefault(    "DefaultAdaptiveMaxDelay",                7L * 24L * 60L * 60L * 1000L ), // 1 week
            theResourceHelper.getResourceDoubleOrDefault(  "DefaultAdaptiveFactor",                  1.1 ),
            theResourceHelper.getResourceBooleanOrDefault( "DefaultWaitForOngoingResynchronization", true ));

    /**
     * A CoherenceSpecification that asks for periodic updates.
     */
    public static class Periodic
            extends
                CoherenceSpecification
    {
        /**
         * Constructor.
         *
         * @param period the requested maximum time period between updates, in milliseconds
         */
        public Periodic(
                long period )
        {
            this( period, true );
        }

        /**
         * Constructor.
         *
         * @param period the requested maximum time period between updates, in milliseconds
         * @param waitForOngoingResynchronization if true, a response should wait until all resynchronization attempts have completed
         */
        public Periodic(
                long    period,
                boolean waitForOngoingResynchronization )
        {
            super( waitForOngoingResynchronization );

            thePeriod = period;
        }
        
        /**
         * Parse a CoherenceSpecification's external form and instantiate the right subclass.
         *
         * @param ext the external form
         * @return the created CoherenceSpecification
         */
        public static CoherenceSpecification fromExternalForm(
                String ext )
        {
            CoherenceSpecification ret;

            Matcher m = thePattern.matcher( ext );
            if( m.matches() ) {
                String period  = m.group( 1 );
                String waitFor = m.group( 2 );
                ret = new Periodic( Long.parseLong( period ), Boolean.parseBoolean( waitFor ));
            } else {
                ret = null;
            }
            return ret;
        }

        /**
         * Obtain the external form of this CoherenceSpecification.
         *
         * @return the external form
         */
        public String toExternalForm()
        {
            StringBuilder ret = new StringBuilder();
            ret.append( getClass().getName() );
            ret.append( "{" );
            ret.append( thePeriod );
            ret.append( "," );
            ret.append( theWaitForOngoingResynchronization );
            ret.append( "}" );
            return ret.toString();
        }
        
        /**
         * Obtain the time period in milliseconds.
         *
         * @return the time period
         */
        public final long getPeriod()
        {
            return thePeriod;
        }

        /**
         * Determine equality.
         *
         * @param other the Object to compare with
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( !( other instanceof Periodic )) {
                return false;
            }
            Periodic realOther = (Periodic) other;
            return thePeriod == realOther.thePeriod;
        }

        /**
         * Determine hash code.
         * 
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            return (int) ( thePeriod >> 8 ); // more randomness that way
        }

        /**
         * The time period in milliseconds.
         */
        protected long thePeriod;
        
        /**
         * Our Regex pattern to parse an externalized CoherenceSpecification.YoungerThan.
         */
        protected static final Pattern thePattern = Pattern.compile(
                Periodic.class.getName().replace( ".", "\\." ).replace( "$", "\\$" )
                + "\\{(\\d+),([^\\}]*)\\}" );
    }

    /**
     * A CoherenceSpecification that asks for periodic updates according to an adaptive schedule.
     */
    public static class AdaptivePeriodic
            extends
                CoherenceSpecification
    {
        /**
         * Constructor.
         *
         * @param fallbackDelay the time period, in milliseconds, for the next run after a change has been detected
         * @param maxDelay      the maximum time period between runs
         * @param adaptiveFactor the multiplier by which the current delay is increased if no change has been detected
         */
        public AdaptivePeriodic(
                long    fallbackDelay,
                long    maxDelay,
                double  adaptiveFactor )
        {
            this( fallbackDelay, maxDelay, adaptiveFactor, true );
        }

        /**
         * Constructor.
         *
         * @param fallbackDelay the time period, in milliseconds, for the next run after a change has been detected
         * @param maxDelay      the maximum time period between runs
         * @param adaptiveFactor the multiplier by which the current delay is increased if no change has been detected
         * @param waitForOngoingResynchronization if true, a response should wait until all resynchronization attempts have completed
         */
        public AdaptivePeriodic(
                long    fallbackDelay,
                long    maxDelay,
                double  adaptiveFactor,
                boolean waitForOngoingResynchronization )
        {
            super( waitForOngoingResynchronization );

            theFallbackDelay  = fallbackDelay;
            theMaxDelay       = maxDelay;
            theAdaptiveFactor = adaptiveFactor;
        }
        
        /**
         * Parse a CoherenceSpecification's external form and instantiate the right subclass.
         *
         * @param ext the external form
         * @return the created CoherenceSpecification
         */
        public static CoherenceSpecification fromExternalForm(
                String ext )
        {
            CoherenceSpecification ret;

            Matcher m = thePattern.matcher( ext );
            if( m.matches() ) {
                String fallback = m.group( 1 );
                String max      = m.group( 2 );
                String factor   = m.group( 3 );
                String waitFor  = m.group( 4 );

                ret = new AdaptivePeriodic(
                        Long.parseLong( fallback ),
                        Long.parseLong( max ),
                        Double.parseDouble( factor ),
                        Boolean.parseBoolean( waitFor ));
            } else {
                ret = null;
            }
            return ret;
        }

        /**
         * Obtain the external form of this CoherenceSpecification.
         *
         * @return the external form
         */
        public String toExternalForm()
        {
            StringBuilder ret = new StringBuilder();
            ret.append( getClass().getName() );
            ret.append( "{" );
            ret.append( theFallbackDelay );
            ret.append( "," );
            ret.append( theMaxDelay );
            ret.append( "," );
            ret.append( theAdaptiveFactor );
            ret.append( "," );
            ret.append( theWaitForOngoingResynchronization );
            ret.append( "}" );
            return ret.toString();
        }
        
        /**
         * Obtain the fallback delay, in milliseconds.
         *
         * @return the fallback delay
         */
        public final long getFallbackDelay()
        {
            return theFallbackDelay;
        }

        /**
         * Obtain the maximum delay, in milliseconds.
         *
         * @return the maximum delay
         */
        public final long getMaxDelay()
        {
            return theMaxDelay;
        }

        /**
         * Obtain the adaptive factor.
         *
         * @return the adaptive factor
         */
        public final double getAdaptiveFactor()
        {
            return theAdaptiveFactor;
        }

        /**
         * Determine equality.
         *
         * @param other the Object to compare with
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( !( other instanceof AdaptivePeriodic )) {
                return false;
            }
            AdaptivePeriodic realOther = (AdaptivePeriodic) other;
            
            if( theFallbackDelay != realOther.theFallbackDelay ) {
                return false;
            }
            if( theMaxDelay != realOther.theMaxDelay ) {
                return false;
            }
            if( theAdaptiveFactor != realOther.theAdaptiveFactor ) {
                return false;
            }
            return true;
        }

        /**
         * Determine hash code.
         * 
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            return (int) ( theMaxDelay ^ theFallbackDelay ^ ((long) (theAdaptiveFactor*1024.f)) >> 8 ); // more randomness that way
        }

        /**
         * The fallback delay, in milliseconds.
         */
        protected long theFallbackDelay;

        /**
         * The maximum delay, in milliseconds.
         */
        protected long theMaxDelay;
        
        /**
         * The adaptive factor, as a multiplier.
         */
        protected double theAdaptiveFactor;
        
        /**
         * Our Regex pattern to parse an externalized CoherenceSpecification.AdaptivePeriodic.
         */
        protected static final Pattern thePattern = Pattern.compile(
                AdaptivePeriodic.class.getName().replace( ".", "\\." ).replace( "$", "\\$" )
                + "\\{(\\d+),(\\d+),(\\d*\\.\\d*),([^\\}]*)\\}" );
    }
}
