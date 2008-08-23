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

/**
 * Attempts to centralize XML escape and unescape functions.
 */
public abstract class XmlUtils
{
    /**
     * Private constructor to keep this abstract.
     */
    private XmlUtils()
    {
        // nothing
    }

    /**
     * This helper takes arbitrary strings and escapes them in a way so that they
     * fit into XML. FIXME This is not complete at all.
     *
     * @param org the original String
     * @return the escaped String
     * @see #descape
     */
    public static final String escape(
            String org )
    {
        StringBuffer ret = new StringBuffer( org.length() + org.length()/5 ); // make a little larger
        for( int i=0 ; i<org.length() ; ++i ) {
            char c = org.charAt( i );
            boolean escape = false;

            switch( c ) {
                case '\'':
                case '\"':
                case '\t':
                // case '\n':
                case '\\':
                case '&':
                // case '#':
                case '<':
                case '>':
                // case '/':
                    escape = true;
                    break;
                default:
                    if( c < 0x20 ) {
                        escape = true;
                    }
                    break;
            }
            if( escape ) {
                ret.append( "&#x" );
                // needs to have a least four hex digits according to the XML spec
                int digit;
                for( int j=3 ; j>=0 ; --j ) {
                    digit = c >> (j*4);
                    digit &= 0xf;
                    ret.append( charTable[ digit ] );
                }
                ret.append( ";" );
            } else {
                ret.append( c );
                
            }
        }
        return new String( ret );
    }


    /**
     * This helper takes a String escaped according to escape and undos the escape.
     * FIXME This is not complete at all.
     *
     * @param enc the escaped String
     * @return the original String
     * @see #escape
     */
    @SuppressWarnings( "fallthrough" )
    public static final String descape(
            String enc )
    {
        StringBuffer ret = new StringBuffer( enc.length() ); // same length

        for( int i=0 ; i<enc.length() ; ++i ) {
            char c = enc.charAt( i );
            switch( c ) {
                case '&':
                    if( enc.charAt( i+1 ) == '#' ) {
                        i += 3; // we simply believe that the next two are going to be #x. FIXME?
                        String s = enc.substring( i );
                        int colonIndex = s.indexOf( ';' );
                        int v = Integer.parseInt( s.substring( 0, colonIndex ), 16 );

                        ret.append( new Character( (char) v ));

                        i += colonIndex;
                        break;
                    }
                    // else run-through

                default:
                    ret.append( c );
                    break;
            }
        }
        return ret.toString();
    }
    
    /**
     * This helper takes escaped strings and undoes what cdataEscape did.
     *
     * @param enc the escaped String
     * @return the original String
     * @see #cdataEscape
     */
    public static final String cdataDescape(
            String enc )
    {
        return descape( enc );
        // FIXME: This should do CDATA, because it is more efficient, but apparently we lose 0x1-0x1f in the process or something like that
//        final String escaped  = "&#93;&#93;&gt;"; // Exact char sequence required, see cdataEscape
//        final String toEscape = "]]>";
//
//        StringBuffer ret = new StringBuffer( enc.length()); // no need to make longer, needing to escape is very unusual
//        int start = 0;
//        int end;
//        while( ( end = enc.indexOf( escaped, start )) >= 0 ) {
//            ret.append( enc.substring( start, end ));
//            ret.append( toEscape );
//            start = end + escaped.length();
//        }
//        if( start < enc.length() ) {
//            ret.append( enc.substring( start ));
//        }
//        return ret.toString();
    }
    
    
    /**
     * This helper takes arbitrary strings and escapes them in a way so that they
     * fit into XML CDATA sections.
     *
     * @param org the original String
     * @return the escaped String
     * @see #cdataDescape
     */
    public static final String cdataEscape(
            String org )
    {
        return escape( org );
        // FIXME: This should do CDATA, because it is more efficient, but apparently we lose 0x1-0x1f in the process or something like that
//        final String toEscape = "]]>";
//        final String escaped  = "&#93;&#93;&gt;"; // Exact char sequence required, see cdataDescape
//        
//        StringBuffer ret = new StringBuffer( org.length()); // no need to make longer, needing to escape is very unusual
//        int start = 0;
//        int end;
//        while( ( end = org.indexOf( toEscape, start )) >= 0 ) {
//            ret.append( org.substring( start, end ));
//            ret.append( escaped );
//            start = end + toEscape.length();
//        }
//        if( start < org.length() ) {
//            ret.append( org.substring( start ));
//        }
//        return ret.toString();
    }

    /**
     * For conversion speed, a character table.
     */
    protected static char [] charTable = {
            '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
}
