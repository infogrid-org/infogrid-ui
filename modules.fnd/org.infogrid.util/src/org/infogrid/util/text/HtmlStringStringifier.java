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

package org.infogrid.util.text;

/**
 * Stringifies a String until valid HTML symtax. For example, this replaces
 * <code>&gt;</code> with <code>&amp;gt;</code>.
 */
public class HtmlStringStringifier
        extends
            StringStringifier
{
    /**
     * Factory method.
     *
     * @return the created HtmlStringStringifier
     */
    public static HtmlStringStringifier create()
    {
        return new HtmlStringStringifier();
    }

    /**
     * No-op constructor. Use factory method.
     */
    protected HtmlStringStringifier()
    {
        // no op
    }
    
    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param arg the Object to format, or null
     * @return the formatted String
     */
    @Override
    public String format(
            String arg )
    {
        String raw = super.format( arg );
        
        String ret = stringToHtml( raw );
        
        return ret;
    }

    /**
     * Parse out the Object in rawString that were inserted using this Stringifier.
     *
     * @param rawString the String to parse
     * @return the found Object
     * @throws StringifierParseException thrown if a parsing problem occurred
     */
    @Override
    public String unformat(
            String rawString )
        throws
            StringifierParseException
    {
        String unescaped = htmlToString( rawString );
        
        String ret = super.unformat( unescaped );
        
        return ret;
    }

    /**
     * Escape HTML characters in String. Inspired by <code>http://www.rgagnon.com/javadetails/java-0306.html</code>
     *
     * @param s the unescaped String
     * @return the escaped String
     */
    protected String stringToHtml(
            String s )
    {
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
    protected String htmlToString(
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
        
    };

    /**
     * The set of HTML fragments we replace the characters with.
     */
    protected static final String [] htmlFrags = {
        
    };
    
    static {
        if( htmlChars.length != htmlFrags.length ) {
            throw new IllegalArgumentException( "htmlChars and htmlFrags not the same length" );
        }
    }
}
