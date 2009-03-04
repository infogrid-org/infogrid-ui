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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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
     * Helper method to make it easy to convert an arbitrary object
     * into a format that is useful for logging.
     *
     * @param obj the actual Object
     * @return String representation of the object
     */
    public static String objectLogString(
            Object obj )
    {
        return objectLogString( obj, FlagsLogSelector.SHOW_DEFAULT );
    }

    /**
     * Helper method to make it easy to convert an arbitrary object
     * into a format that is useful for logging.
     *
     * @param obj the actual Object
     * @param flags the flags indicating what to log and what not
     * @return String representation of the object
     */
    public static String objectLogString(
            Object obj,
            int    flags )
    {
        return objectLogString( obj, new FlagsLogSelector( flags ));
    }

    /**
     * Helper method to make it easy to convert an arbitrary object
     * into a format that is useful for logging.
     *
     * @param obj the actual Object
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String objectLogString(
            Object      obj,
            LogSelector selector )
    {
        if( obj == null )  {
            return nullLogString( selector );

        } else if( obj instanceof Object[] ) {
            return arrayLogString( (Object []) obj, selector );

        } else if( obj instanceof Collection ) {
            return collectionLogString( (Collection) obj, selector );

        } else if( obj instanceof byte[] ) {
            return byteArrayLogString( (byte []) obj, selector );

        } else if( obj instanceof short[] ) {
            return shortArrayLogString( (short []) obj, selector );

        } else if( obj instanceof int[] ) {
            return intArrayLogString( (int []) obj, selector );

        } else if( obj instanceof long[] ) {
            return longArrayLogString( (long []) obj, selector );

        } else if( obj instanceof float[] ) {
            return floatArrayLogString( (float []) obj, selector );

        } else if( obj instanceof double[] ) {
            return doubleArrayLogString( (double []) obj, selector );

        } else if( obj instanceof char[] ) {
            return charArrayLogString( (char []) obj, selector );

        } else if( obj instanceof Boolean ) {
            return booleanLogString( (Boolean) obj, selector );

        } else if( obj instanceof Character ) {
            return characterLogString( (Character) obj, selector );

        } else if( obj instanceof Date ) {
            return dateLogString( (Date) obj, selector );

        } else if( obj instanceof Number ) {
            return numberLogString( (Number) obj, selector );

        } else if( obj instanceof CharSequence ) {
            return stringLogString( (CharSequence) obj, selector );

        } else if( obj instanceof Object[] ) {
            return arrayLogString( (Object []) obj, selector );

        } else if( obj instanceof Collection ) {
            return collectionLogString( (Collection) obj, selector );

        } else {
            Field [] fields = obj.getClass().getFields();
            ArrayList<String> names  = new ArrayList<String>();
            ArrayList<Object> values = new ArrayList<Object>();

            for( int i=0 ; i<fields.length ; ++i ) {
                if( Modifier.isStatic( fields[i].getModifiers())) {
                    continue; // we don't want static
                }
                try {
                    Object value = fields[i].get( obj );
                    names.add( fields[i].getName() );
                    values.add( value );

                } catch( Throwable t ) {
                    if( log.isDebugEnabled() ) {
                        log.debug( t );
                    }
                }
            }

            if( !names.isEmpty() ) {
                String [] names2  = ArrayHelper.copyIntoNewArray( names, String.class );
                Object [] values2 = ArrayHelper.copyIntoNewArray( values, Object.class );

                return objectLogString( obj, names2, values2, selector );
            } else {
                return obj.toString();
            }
        }
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
        return objectLogString( obj, fieldNames, fieldValues, FlagsLogSelector.SHOW_DEFAULT );
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
        return objectLogString( obj, fieldNames, fieldValues, new FlagsLogSelector( flags ));
    }

    /**
     * Helper method to make it easy to convert a structured object
     * into a format that is useful for logging.
     *
     * @param obj the actual Object
     * @param fieldNames the field names that we want to log
     * @param fieldValues the corresponding values that we want to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String objectLogString(
            Object      obj,
            String []   fieldNames,
            Object []   fieldValues,
            LogSelector selector )
    {
        StringBuilder buf = createBufferWithObjectId( obj );
        buf.append( "{\n" );
        int min = Math.min(
                fieldNames  != null ? fieldNames.length  : 0,
                fieldValues != null ? fieldValues.length : 0 );

        for( int i=0 ; i<min ; ++i ) {
            Object value = fieldValues[i];

            if( selector.shouldBeLogged( value )) {

                StringBuilder buf2 = new StringBuilder();
                buf2.append( fieldNames[i] );
                buf2.append( ": ");
                buf2.append( objectLogString( value, selector ));
                buf.append( indent( buf2.toString() ));

                if( i<min-1 ) {
                    buf.append( ',' );
                }
                buf.append( '\n' );
            }
        }
        if(    ( fieldNames  != null && fieldNames.length  != min )
            || ( fieldValues != null && fieldValues.length != min ))
        {
            log.error( "non-matching field names and values in toString method" );
        }
        buf.append( "}" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert the null value
     * into a format that is useful for logging.
     *
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String nullLogString(
            LogSelector selector )
    {
        return "null";
    }

    /**
     * Helper method to make it easy to convert a Number object
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String numberLogString(
            Number      value,
            LogSelector selector )
    {
        if( value == null ) {
            return nullLogString( selector );

        } else {
            StringBuilder buf = new StringBuilder();

            buf.append( value.getClass() );
            buf.append( ": " );
            buf.append( value );
            return buf.toString();
        }
    }

    /**
     * Helper method to make it easy to convert a string object
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String stringLogString(
            CharSequence value,
            LogSelector  selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( value.getClass() );
        buf.append( ": \"" );
        buf.append( value );
        buf.append( "\"" );
        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an  object array object
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String arrayLogString(
            Object []   value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( value.getClass().getComponentType().getName() );
        if( value.length > 0 ) {
            buf.append( '[' ).append( value.length ).append( "] = {\n" );
            int max = Math.min( 8, value.length );
            for( int j=0 ; j<max ; ++j ) {
                String delegate = objectLogString( value[j], selector );
                StringBuilder delegate2 = indent( delegate );
                buf.append( delegate2 );
                if( j < max-1 ) {
                    buf.append( "," );
                }
                buf.append( "\n" );
            }
            if( max != value.length ) {
                buf.append( "        ...\n" );
            }
            buf.append( "}" );

        } else {
            buf.append( "[0] = {}" );
        }
        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert a Collection object
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String collectionLogString(
            Collection  value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( value.getClass().getName() );
        if( value.size() > 0 ) {
            buf.append( '[' ).append( value.size() ).append( "] = {\n" );
            int max = Math.min( 8, value.size() );
            int j   = 0;
            for( Object current : value ) {
                String delegate = objectLogString( current, selector );
                StringBuilder delegate2 = indent( delegate );
                buf.append( delegate2 ).append( "\n" );
                if( ++j >= max ) {
                    break;
                }
            }
            if( max != value.size() ) {
                buf.append( "        ...\n" );
            }
            buf.append( "}" );

        } else {
            buf.append( "[0] = {}" );
        }
        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert a byte array
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String byteArrayLogString(
            byte []     value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "byte[").append( value.length ).append( "] = " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( Character.forDigit( ( value[j] >> 4 ) & 0xf, 16 )).append( Character.forDigit( value[j] & 0xf, 16 ));
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an array of short
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String shortArrayLogString(
            short []    value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "short[").append( value.length ).append( "] = { " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( value[j] );
            if( j < max-1 ) {
                buf.append( ", " );
            }
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        buf.append( " }" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an array of int
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String intArrayLogString(
            int []      value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "int[").append( value.length ).append( "] = { " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( value[j] );
            if( j < max-1 ) {
                buf.append( ", " );
            }
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        buf.append( " }" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an array of long
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String longArrayLogString(
            long []     value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "long[").append( value.length ).append( "] = { " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( value[j] );
            if( j < max-1 ) {
                buf.append( ", " );
            }
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        buf.append( " }" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an array of float
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String floatArrayLogString(
            float []    value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "float[").append( value.length ).append( "] = { " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( value[j] );
            if( j < max-1 ) {
                buf.append( ", " );
            }
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        buf.append( " }" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an array of double
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String doubleArrayLogString(
            double []   value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "double[").append( value.length ).append( "] = { " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( value[j] );
            if( j < max-1 ) {
                buf.append( ", " );
            }
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        buf.append( " }" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an array of boolean
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String booleanArrayLogString(
            boolean []  value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "boolean[").append( value.length ).append( "] = { " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( value[j] );
            if( j < max-1 ) {
                buf.append( ", " );
            }
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        buf.append( " }" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert an array of char
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String charArrayLogString(
            char []     value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( "char[").append( value.length ).append( "] = { " );
        int max = Math.min( 8, value.length );
        for( int j=0 ; j<max ; ++j ) {
            buf.append( '\'' );
            buf.append( value[j] );
            buf.append( '\'' );
            if( j < max-1 ) {
                buf.append( ", " );
            }
        }
        if( max < value.length ) {
            buf.append( "..." );
        }
        buf.append( " }" );

        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert a Boolean object
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String booleanLogString(
            Boolean     value,
            LogSelector selector )
    {
        if( value.booleanValue() ) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Helper method to make it easy to convert a Character object
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String characterLogString(
            Character   value,
            LogSelector selector )
    {
        StringBuilder buf = new StringBuilder();

        buf.append( '\'' );
        buf.append( value.charValue() );
        buf.append( '\'' );
        return buf.toString();
    }

    /**
     * Helper method to make it easy to convert a Date object
     * into a format that is useful for logging.
     *
     * @param value the value to log
     * @param selector the LogSelector to use to indicate what to log and what not
     * @return String representation of the object
     */
    public static String dateLogString(
            Date        value,
            LogSelector selector )
    {
        String ret = theObjectLogStringDateFormat.format( value );
        return ret;
    }

    /**
     * Create a StringBuilder with object identifier.
     *
     * @param obj the Object
     * @return return the StringBuilder
     */
    protected static StringBuilder createBufferWithObjectId(
            Object obj )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( obj.getClass().getName() );
        buf.append( '@' );
        buf.append( Integer.toHexString( obj.hashCode() ));
        return buf;
    }

    /**
     * Internal helper to indent a String.
     *
     * @param input the input String
     * @return the indented String. This returns StringBuffer as that is usually more efficient.
     */
    public static StringBuilder indent(
            String input )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "    " );
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
     * Make sure a String is not longer than <code>maxLength</code>. This is accomplished
     * by taking out characters in the middle if needed.
     *
     * @param in the input String
     * @param maxLength the maximally allowed length. -1 means unlimited.
     * @return the String, potentially shortened
     */
    public static String potentiallyShorten(
            String in,
            int    maxLength )
    {
        if( in == null || in.length() == 0 ) {
            return "";
        }
        if( maxLength < 0 ) {
            return in;
        }

        final String insert = "...";
        final int    fromEnd = 5; // how many characters we leave at the end

        String ret = in;
        if( maxLength > 0 && ret.length() > maxLength ) {
            ret = ret.substring( 0, maxLength-fromEnd-insert.length() ) + insert + ret.substring( ret.length() - fromEnd );
        }
        return ret;
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

    /**
     * Interface that allows a client to specify which objects to log.
     */
    public interface LogSelector
    {
        /**
         * Determine whether this object should be logged.
         *
         * @param obj the candidate object
         * @return true if it should be logged
         */
       public boolean shouldBeLogged(
               Object obj );
    }

    /**
     * Flag-based LogSelector.
     */
    public static class FlagsLogSelector
            implements
                LogSelector
    {
        public static final int SHOW_NULL              =  1;
        public static final int SHOW_NON_NULL          =  2;
        public static final int SHOW_ZERO              =  4;
        public static final int SHOW_NON_ZERO          =  8;
        public static final int SHOW_MINUS_ONE         = 16;
        public static final int SHOW_EMPTY_ARRAYS      = 32;
        public static final int SHOW_EMPTY_COLLECTIONS = 64;
        public static final int SHOW_ALL
                = SHOW_NULL
                | SHOW_NON_NULL
                | SHOW_ZERO
                | SHOW_NON_ZERO
                | SHOW_MINUS_ONE
                | SHOW_EMPTY_ARRAYS
                | SHOW_EMPTY_COLLECTIONS;
        public static final int SHOW_DEFAULT
                = SHOW_NON_NULL
                | SHOW_NON_ZERO
                | SHOW_MINUS_ONE;

        /**
         * Constructor.
         *
         * @param flags the flags to use
         */
        public FlagsLogSelector(
                int flags )
        {
            theFlags = flags;
        }

        /**
         * Determine whether this object should be logged.
         *
         * @param obj the candidate object
         * @return true if it should be logged
         */
       public boolean shouldBeLogged(
               Object obj )
       {
           if( obj == null ) {
               if(( SHOW_NULL & theFlags ) != 0 ) {
                   return true;
               } else {
                   return false;
               }
           }
           if( obj instanceof Number ) {
               Number realObj = (Number) obj;

               if( realObj.doubleValue() == 0. ) {
                   if(( SHOW_ZERO & theFlags ) != 0 ) {
                       return true;
                   } else {
                       return false;
                   }
               } else if( realObj.doubleValue() == -1. ) {
                   if(( SHOW_MINUS_ONE & theFlags ) != 0 ) {
                       return true;
                   } else {
                       return false;
                   }
               } else {
                   if(( SHOW_NON_ZERO & theFlags ) != 0 ) {
                       return true;
                   } else {
                       return false;
                   }
               }
           } else if( obj instanceof Object[] ) {
               Object [] realObj = (Object []) obj;

               if( realObj.length == 0 ) {
                   if(( SHOW_EMPTY_ARRAYS & theFlags ) != 0 ) {
                       return true;
                   } else {
                       return false;
                   }
               }
           } else if( obj instanceof Collection ) {
               Collection realObj = (Collection) obj;

               if( realObj.isEmpty() ) {
                   if(( SHOW_EMPTY_COLLECTIONS & theFlags ) != 0 ) {
                       return true;
                   } else {
                       return false;
                   }
               }
           }
           return true;
       }

        /**
         * The masking flags.
         */
        protected int theFlags;
    }
}
