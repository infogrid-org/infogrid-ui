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

package org.infogrid.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.infogrid.util.logging.Log;

/**
  * A collection of String manipulation methods that we found
  * convenient but are not found in the JDK.
  */
public abstract class StringHelper
{
    private static final Log log = Log.getLogInstance( StringHelper.class ); // our own, private logger

    /**
     * Compare two Strings.
     * 
     * @param one the first String
     * @param two the second String
     * @return comparison value, according to String.compareTo
     */
    public static int compareTo(
            String one,
            String two )
    {
        int ret;
        if( one == null ) {
            ret = two == null ? 0 : -1;
        } else {
            ret = one.compareTo( two );
        }
        return ret;
    }

    /**
     * We use backslashes as escape.
     */
    static final char ESCAPE = '\\';

    /**
      * Tokenize a String into String[] with specified delimiters.
      * We cannot use StringTokenizer as that doesn't allow us to use \ as
      * an escape character.
      *
      * @param raw the input stream
      * @param datokens the tokens for which we look
      * @param quotes the quotes that we respect
      * @return the tokenized String
      */
    public static String [] tokenize(
            String raw,
            String datokens,
            String quotes )
    {
        if( raw == null ) {
            return new String[0];
        }
        ArrayList<String> v = new ArrayList<String>();
        int count = 0;

        for( int from = 0; from<raw.length() ; ++from ) {
            int to;
            boolean escapeOn = false;
            boolean quoteOn  = false;
            StringBuffer buf = new StringBuffer( raw.length() );

            for( to = from; to < raw.length() ; ++to ) {
                char c = raw.charAt( to );
                if( c == ESCAPE ) {
                    escapeOn = true;
                    continue;
                }
                if( quotes != null && quotes.indexOf( c ) >= 0 ) {
                    quoteOn = !quoteOn;
                    buf.append( c );
                    continue;
                }
                if( escapeOn ) {
                    buf.append( c );
                    escapeOn = false;

                } else if( quoteOn ) {
                    buf.append( c );

                } else {
                    boolean foundDelim = false;
                    for( int i=0 ; i<datokens.length() ; ++i ) {
                        if( c == datokens.charAt( i )) {
                            foundDelim = true;
                            break;
                        }
                    }
                    if( foundDelim ) {
                        break;
                    } else {
                        buf.append( c );
                    }
                }
            }
            v.add( buf.toString() );
            ++count;
            from = to;
        }

        String [] tokens = new String[ count ];
        v.toArray( tokens );

        if( log.isDebugEnabled() ) {
            log.debug( "parsed \"" + raw + "\" into " + ArrayHelper.arrayToString( tokens ));
        }
        return tokens;
    }

    /**
     * Tokenize a String into String[] with a default datokens.
     *
     * @param raw the input String
     * @return the tokenized String
     */
    public static String [] tokenize(
            String raw )
    {
        return tokenize( raw, defaultDatokens, defaultQuotes );
    }

    /**
     * Tokenize a String into URL[] with specified delimiters.
     *
     * @param raw the input String
     * @param datokens the tokens for which we look
     * @param quotes the quotes that we respect
     * @return the tokenized URLs
     */
    public static URL [] tokenizeToURL(
            String raw,
            String datokens,
            String quotes )
    {
        String [] stringArray = tokenize( raw, datokens, quotes );

        URL [] ret = new URL[ stringArray.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            try {
                ret[i] = new URL( stringArray[i] );
            } catch( MalformedURLException ex ) {
                log.error( ex );
                ret[i] = null;
            }
        }
        return ret;
    }

    /**
     * Tokeinze a String into URL [] with a default datokens.
     *
     * @param raw the input String
     * @return the tokenized URLs
     */
    public static URL [] tokenizeToURL(
            String raw )
    {
        return tokenizeToURL( raw, defaultDatokens, defaultQuotes );
    }

    /**
      * Replace each occurrence of token with replaceWith.
      *
      * @param source the source String
      * @param token the token to replace
      * @param replaceWith the String to substitute
      * @return a String derived from the source String where each occurrence of token
      *         has been replaced with replaceWith
      */
    public static String replace(
            String source,
            String token,
            String replaceWith )
    {
        String ret = "";
        int start = 0;
        int end   = 0;

        while((end = source.indexOf(token,start)) != -1) {
            ret += source.substring(start, end) + replaceWith;
            start = end + token.length();
        }
        ret += source.substring(start, source.length());
        return ret;
    }

    /**
     * Helper method to make it easy to convert a structured object
     * into a format that is useful for logging.
     *
     * @param obj the actual Object
     * @param fieldNames the field names that we want to log
     * @param fieldValues the corresponding values that we want to log
     * @return String representation of the object
     */
    public static String objectLogString(
            Object    obj,
            String [] fieldNames,
            Object [] fieldValues )
    {
        return objectLogString( obj, fieldNames, fieldValues, LOG_FLAGS.SHOW_ALL );
    }
    
    /**
     * Helper method to make it easy to convert a structured object
     * into a format that is useful for logging.
     *
     * @param obj the actual Object
     * @param fieldNames the field names that we want to log
     * @param fieldValues the corresponding values that we want to log
     * @param flags the flags indicating what to log and what not
     * @return String representation of the object
     */
    public static String objectLogString(
            Object    obj,
            String [] fieldNames,
            Object [] fieldValues,
            int       flags )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( obj.getClass().getName() );
        buf.append( '@' );
        buf.append( Integer.toHexString( obj.hashCode() ));
        buf.append( "{\n" );
        int min = Math.min(
                fieldNames  != null ? fieldNames.length  : 0,
                fieldValues != null ? fieldValues.length : 0 );

        for( int i=0 ; i<min ; ++i ) {
            Object value = fieldValues[i];

            if( value == null && !LOG_FLAGS.showNull( flags ) ) {
                continue;
            }
            if( value != null && !LOG_FLAGS.showNonNull( flags ) ) {
                continue;
            }
            if( value instanceof Number ) {
                Number realValue = (Number) value;

                if( 0 == realValue.longValue() && !LOG_FLAGS.showZero( flags ) ) {
                    continue;
                }
                if( -1 == realValue.longValue() && !LOG_FLAGS.showMinusOne( flags ) ) {
                    continue;
                }
                if( 0 != realValue.longValue() && !LOG_FLAGS.showNonZero( flags ) ) {
                    continue;
                }
            }
            if( value instanceof Object[] ) {
                Object [] realValue = (Object []) value;
                if( realValue.length == 0 && !LOG_FLAGS.showEmptyArrays( flags )) {
                    continue;
                }
            }
            if( value instanceof Collection ) {
                Collection realValue = (Collection) value;
                if( realValue.isEmpty() && !LOG_FLAGS.showEmptyArrays( flags )) {
                    continue;
                }
            }
            
            buf.append( "    " );
            buf.append( fieldNames[i] );
            buf.append( ": ");
            if( value instanceof Object[] ) {
                Object [] realValue = (Object []) value;
                buf.append( realValue.getClass().getComponentType().getName() );
                if( realValue.length > 0 ) {
                    buf.append( '[' ).append( realValue.length ).append( "] = {\n" );
                    int max = Math.min( 8, realValue.length );
                    for( int j=0 ; j<max ; ++j ) {
                        buf.append( "        " ).append( indent( String.valueOf( realValue[j] ))).append( "\n" );
                    }
                    if( max != realValue.length ) {
                        buf.append( "        ...\n" );
                    }
                    buf.append( "    }" );

                } else {
                    buf.append( "[0] = {}" );
                }

            } else if( value instanceof Collection ) {
                Collection realValue = (Collection) value;
                buf.append( realValue.getClass().getName() );
                if( realValue.size() > 0 ) {
                    buf.append( '[' ).append( realValue.size() ).append( "] = {\n" );
                    int max = Math.min( 8, realValue.size() );
                    int j   = 0;
                    for( Object current : realValue ) {
                        buf.append( "        " ).append( indent( String.valueOf( current ))).append( "\n" );
                        if( ++j >= max ) {
                            break;
                        }
                    }
                    if( max != realValue.size() ) {
                        buf.append( "        ...\n" );
                    }
                    buf.append( "    }" );

                } else {
                    buf.append( "[0] = {}" );
                }

            } else if( value instanceof byte[] ) {
                byte [] realValue = (byte []) value;
                buf.append( "byte[ ").append( realValue.length ).append( " ] = " );
                int max = Math.min( 8, realValue.length );
                for( int j=0 ; j<max ; ++j ) {
                    buf.append( Character.forDigit( ( realValue[j] >> 4 ) & 0xf, 16 )).append( Character.forDigit( realValue[j] & 0xf, 16 ));
                }
                if( max < realValue.length ) {
                    buf.append( "..." );
                }
                buf.append( ";" );

            } else if( value instanceof Date ) {
                Date realValue = (Date) value;
                String temp = theObjectLogStringDateFormat.format( realValue );
                buf.append( temp );

            } else {
                buf.append( indent( String.valueOf( value )));
            }

            if( i<min-1 ) {
                buf.append( ',' );
            }
            buf.append( '\n' );
        }
        if(    ( fieldNames != null && fieldNames.length != min )
            || ( fieldValues != null && fieldValues.length != min ))
        {
            log.error( "non-matching field names and values in toString method" );
        }
        buf.append( "}" );

        return buf.toString();
    }

    /**
     * Flags to configure the objectLogString output.
     */
    public static class LOG_FLAGS
    {
        public static int SHOW_NULL         =   1;
        public static int SHOW_NON_NULL     =   2;
        public static int SHOW_ZERO         =   4;
        public static int SHOW_NON_ZERO     =   8;
        public static int SHOW_MINUS_ONE    =  16;
        public static int SHOW_EMPTY_ARRAYS =  32;
        public static int SHOW_ALL          = 255;

        /**
         * Determine whether to show null values.
         *
         * @param theFlags the flags indicating what to log and what not
         * @return true if showing null values
         */
        public static boolean showNull(
                int theFlags )
        {
            return ( SHOW_NULL & theFlags ) != 0;
        }
        
        /**
         * Determine whether to show non-null values.
         *
         * @param theFlags the flags indicating what to log and what not
         * @return true if showing non-null values
         */
        public static boolean showNonNull(
                int theFlags )
        {
            return ( SHOW_NON_NULL & theFlags ) != 0;
        }
        
        /**
         * Determine whether to show zero values.
         *
         * @param theFlags the flags indicating what to log and what not
         * @return true if showing zero values
         */
        public static boolean showZero(
                int theFlags )
        {
            return ( SHOW_ZERO & theFlags ) != 0;
        }
        
        /**
         * Determine whether to show non-zero values.
         *
         * @param theFlags the flags indicating what to log and what not
         * @return true if showing non-zero values
         */
        public static boolean showNonZero(
                int theFlags )
        {
            return ( SHOW_NON_ZERO & theFlags ) != 0;
        }
        
        /**
         * Determine whether to show -1 values.
         *
         * @param theFlags the flags indicating what to log and what not
         * @return true if showing -1 values
         */
        public static boolean showMinusOne(
                int theFlags )
        {
            return ( SHOW_MINUS_ONE & theFlags ) != 0;
        }
        
        /**
         * Determine whether to show empty arrays.
         * 
         * @param theFlags the flags indicating what to log and what not
         * @return true if showing empty arrays
         */
        public static boolean showEmptyArrays(
                int theFlags )
        {
            return ( SHOW_EMPTY_ARRAYS & theFlags ) != 0;
        }
    }

    /**
     * Internal helper to indent a String.
     *
     * @param input the input String
     * @return the indented String. This returns StringBuffer as that is usually more efficient.
     */
    public static StringBuffer indent(
            String input )
    {
        StringBuffer buf = new StringBuffer();
        for( int i=0 ; i<input.length() ; ++i ) {
            char c = input.charAt( i );
            switch( c ) {
                case '\n':
                    buf.append( c );
                    buf.append( "    " );
                    break;
                default:
                    buf.append( c );
                    break;
            }
        }
        return buf;
    }

    /**
     * Obtain N blanks.
     *
     * @param n the number of blanks
     * @param blank the text String to use as blank
     * @return a String with N blanks
     */
    public static String blanks(
            int    n,
            String blank )
    {
        if( n <= 0 ) {
            return "";
        }
        if( n==1 ) {
            return blank;
        }
        
        StringBuffer ret = new StringBuffer( blank.length() * n );
        for( int i=0 ; i<n ; ++i ) {
            ret.append( blank );
        }
        return ret.toString();
    }

    /**
     * Obtain N blanks.
     *
     * @param n the number of blanks
     * @return a String with N blanks
     */
    public static String blanks(
            int n )
    {
        return blanks( n, " " );
    }

    /**
     * Our default datokens -- used to determine where to split the string when tokenizing.
     */
    public static String defaultDatokens = ",;";

    /**
     * Our default quotes -- used to ignore datokens when tokenizing.
     */
    public static String defaultQuotes = "\"\'";
    
    /**
     * The date format to use for Date fields in objectLogString
     */
    public static final DateFormat theObjectLogStringDateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss.S" );
}
