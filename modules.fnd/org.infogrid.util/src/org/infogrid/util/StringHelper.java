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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
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
        } else if( two == null ) {
            ret = 1;
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
        return tokenize( raw, DEFAULT_TOKENS, DEFAULT_QUOTES );
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
        return tokenizeToURL( raw, DEFAULT_TOKENS, DEFAULT_QUOTES );
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
     * Indent a String.
     *
     * @param input the input String
     * @return the indented String. This returns StringBuilder as that is usually more efficient.
     */
    public static StringBuilder indent(
            CharSequence input )
    {
        return indent( input, "    ", 1, 1 );
    }

    /**
     * Indent a String.
     *
     * @param input the input String
     * @param indent the indent to use
     * @return the indented String. This returns StringBuilder as that is usually more efficient.
     */
    public static StringBuilder indent(
            CharSequence input,
            String       indent )
    {
        return indent( input, indent, 1, 1 );
    }

    /**
     * Indent a String.
     *
     * @param input the input String
     * @param indent the indent to use
     * @param firstLineN the number of indents to use for the first line
     * @param sunsequentN the number of indents to use for subsequent lines
     * @return the indented String. This returns StringBuilder as that is usually more efficient.
     */
    public static StringBuilder indent(
            CharSequence input,
            String       indent,
            int          firstLineN,
            int          sunsequentN )
    {
        StringBuilder buf = new StringBuilder( input.length() + indent.length() * firstLineN ); // one line is sure to fit

        for( int k=0 ; k<firstLineN ; ++k ) {
            buf.append( indent );
        }
        for( int i=0 ; i<input.length() ; ++i ) {
            char c = input.charAt( i );
            switch( c ) {
                case '\n':
                    buf.append( c );
                    for( int k=0 ; k<sunsequentN ; ++k ) {
                        buf.append( indent );
                    }
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
     * Escape HTML characters in String. Inspired by <code>http://www.rgagnon.com/javadetails/java-0306.html</code>
     *
     * @param s the unescaped String
     * @return the escaped String
     */
    public static String stringToHtml(
            String s )
    {
        if( s == null ) {
            return NULL_STRING;
        }
        StringBuilder sb = new StringBuilder( s.length() );

        // true if last char was blank
        boolean lastWasBlankChar = false;
        int len = s.length();

        for( int i=0; i<len; ++i ) {
            char c = s.charAt( i );
            if( c == ' ' ) {
                // blank gets extra work,
                // this solves the problem you get if you replace all
                // blanks with &nbsp;, if you do that you loss
                // word breaking
                if (lastWasBlankChar) {
                    lastWasBlankChar = false;
                    sb.append("&nbsp;");
                } else {
                    lastWasBlankChar = true;
                    sb.append(' ');
                }
            } else {
                lastWasBlankChar = false;

                int ci = 0xffff & c;
                if (ci < 128 ) {
                    // nothing special only 7 Bit

                    boolean done = false;
                    for( int k=0 ; k<htmlChars.length ; ++k ) {
                        if( c == htmlChars[k] ) {
                            sb.append( htmlFrags[k] );
                            done = true;
                        }
                    }
                    if( !done ) {
                        sb.append( c );
                    }
                } else {
                    // Not 7 Bit use the unicode system
                    sb.append("&#");
                    sb.append(new Integer(ci).toString());
                    sb.append(';');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Unescape HTML escape characters in String.
     *
     * @param s the escaped String
     * @return the unescaped String
     */
    public static String htmlToString(
            String s )
    {
        StringBuilder sb = new StringBuilder( s.length() );

        int len = s.length();

        for( int i=0; i<len; ++i ) {
            char c = s.charAt( i );
            if( c != '&' ) {
                sb.append( c );
            } else {
                if( i+2 < len && s.charAt( i+1 ) == '#' ) { // +2 because we need at least one after the #
                    int semi = s.indexOf( '#', i+1 );
                    if( semi >= 0 ) {
                        char unicode = Character.forDigit( Integer.parseInt( s.substring( i+2, semi )), 10 );
                        sb.append( unicode );

                    } else {
                        sb.append( c ); // leave everything normal
                    }
                } else {
                    for( int k=0 ; i<htmlFrags.length ; ++k ) {
                        if( s.regionMatches( i, htmlFrags[k], 0, htmlFrags[k].length() )) {
                            sb.append( htmlChars[k] );
                            i += htmlFrags[k].length()-1; // one less, because the loop increments anyway
                            break;
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    /**
     * The set of characters we replace in HTML.
     */
    protected static final char [] htmlChars = {
            '&',
            '<',
            '>'
    };

    /**
     * The set of HTML fragments we replace the characters with.
     */
    protected static final String [] htmlFrags = {
            "&amp;",
            "&lt;",
            "&gt;"
    };

    static {
        if( htmlChars.length != htmlFrags.length ) {
            throw new IllegalArgumentException( "htmlChars and htmlFrags not the same length" );
        }
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
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( StringHelper.class );

    /**
     * The String to return if an argument is null.
     */
    public static final String NULL_STRING = theResourceHelper.getResourceStringOrDefault( "NullString", "null" );

    /**
     * Our default datokens -- used to determine where to split the string when tokenizing.
     */
    public static final String DEFAULT_TOKENS = theResourceHelper.getResourceStringOrDefault( "DefaultTokens", ",;" );

    /**
     * Our default quotes -- used to ignore datokens when tokenizing.
     */
    public static final String DEFAULT_QUOTES = theResourceHelper.getResourceStringOrDefault( "DefaultQuotes", "\"\'" );

    /**
     * Default logging depth.
     */
    public static final int DEFAULT_LOG_DEPTH = 5;

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
