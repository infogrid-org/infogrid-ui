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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.util.ResourceHelper;

/**
 * Factors out functionality common to LidNonceManagers.
 */
public abstract class AbstractLidNonceManager
        implements
            LidNonceManager
{
    /**
     * Constructor for subclasses only.
     */
    protected AbstractLidNonceManager()
    {
        // no op
    }

    /**
     * Generate a new nonce.
     * 
     * @return the newly generated nonce
     */
    public String generateNewNonce()
    {
        Calendar cal = new GregorianCalendar( TimeZone.getTimeZone( "GMT" ));
        cal.setTimeInMillis( System.currentTimeMillis() );

        StringBuilder nonce = new StringBuilder( 64 );
        nonce.append( formatTime( cal.get( Calendar.YEAR ), 4 ));
        nonce.append( '-' );
        nonce.append( formatTime( cal.get( Calendar.MONTH )+1, 2 ));
        nonce.append( '-' );
        nonce.append( formatTime( cal.get( Calendar.DAY_OF_MONTH ), 2 ));
        nonce.append( 'T' );
        nonce.append( formatTime( cal.get( Calendar.HOUR_OF_DAY ), 2 ));
        nonce.append( ':' );
        nonce.append( formatTime( cal.get( Calendar.MINUTE ), 2 ));
        nonce.append( ':' );
        nonce.append( formatTime( cal.get( Calendar.SECOND ), 2 ));
        nonce.append( '.' );
        nonce.append( formatTime( cal.get( Calendar.MILLISECOND ), 3 ));
        nonce.append( 'Z' );

        return nonce.toString();
    }

    /**
     * Helper method to format an integer correctly.
     *
     * @param n the number to format
     * @param digits the number of digits in the format
     * @return formatted string
     */
    private static String formatTime(
            int n,
            int digits )
    {
        String ret = String.valueOf( n );
        if( ret.length() < digits ) {
            StringBuilder buf = new StringBuilder( digits );
            for( int i=ret.length() ; i<digits ; ++i ) {
                buf.append( "0" );
            }
            buf.append( ret );
            ret = buf.toString();
        }
        return ret;
    }

    /**
     * Helper method to validate the time range of a nonce.
     * 
     * @param nonce the presented nonce
     * @return true if the time range of the nonce is valid, false if it is not or unknown
     */
    protected boolean validateNonceTimeRange(
            String nonce )
    {
        if( nonce == null ) {
            return false;
        }

        // we do the parsing ourselves to make sure there's no problem that's silently ignored by
        // other parsers
        Matcher m = theLidNoncePattern.matcher( nonce );
        if( !m.matches() ) {
            return false;
        }
        int year   = Integer.parseInt( m.group( 1 ));
        int month  = Integer.parseInt( m.group( 2 ));
        int day    = Integer.parseInt( m.group( 3 ));
        int hour   = Integer.parseInt( m.group( 4 ));
        int minute = Integer.parseInt( m.group( 5 ));
        int second = Integer.parseInt( m.group( 6 ));
        double milli = Double.parseDouble( m.group( 7 ));

        if(    year   < 2000 || month  < 1  || month > 12 || day    < 1  || day > 31
            || hour   < 0    || hour   > 23 || minute < 0 || minute > 59
            || second < 0    || second > 62 || milli < 0  || milli  >= 1000 )
        {
            // leap seconds can go to 62, according to perldoc TimeDate
            return false;
        }

        Calendar cal = new GregorianCalendar( TimeZone.getTimeZone( "GMT" ));
        cal.set( year, month-1, day, hour, minute, second ); // month is 0-based
        long nonceTime = cal.getTimeInMillis();

        cal.clear();
        cal.setTimeInMillis( System.currentTimeMillis() );
        long now = cal.getTimeInMillis();

        long delta = now - nonceTime;
        delta /= 1000L;

        if( delta > 0 ) {
            if( delta > theMaxNonceAge ) {
                return false;
            }
        } else {
            if( delta < -theMaxNonceFuture ) {
                return false;
            }
        }
        return true;        
    }


    /**
     * The pattern for the LID V2 nonce.
     */
    protected static final Pattern theLidNoncePattern = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})Z$" );

    /**
     * Our ResourceHelper.
     */
    protected static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( AbstractLidNonceManager.class );

    /**
     * The maximum age of a nonce that we tolerate.
     */
    protected static long theMaxNonceAge = theResourceHelper.getResourceLongOrDefault( "MaxNonceAge", 60L*60L*1000L ); // 1 hour
    
    /**
     * The maximum amount of time we tolerate a nonce to be in the future.
     */
    protected static long theMaxNonceFuture = theResourceHelper.getResourceLongOrDefault( "MaxNonceAge", 5L*60L*1000L ); // 5 minutes
}
