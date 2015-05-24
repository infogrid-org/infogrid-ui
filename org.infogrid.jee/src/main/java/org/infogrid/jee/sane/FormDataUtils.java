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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.sane;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.infogrid.util.StreamUtils;
import org.infogrid.util.logging.Log;

/**
 * Collects utilities to parse multipart/form-data data.
 */
public abstract class FormDataUtils
{
    private static final Log log = Log.getLogInstance( FormDataUtils.class ); // our own, private logger

    /**
     * Private constructor to keep this abstract.
     */
    private FormDataUtils()
    {
        // nothing
    }


    /**
     * Determine the String indicating the boundary between the parts.
     *
     * @param mime the MIME type of the content
     * @return the boundary String
     */
    public static String determineBoundaryString(
            String mime )
    {
        final String BOUNDARY_EQ = "boundary=";

        int found = mime.lastIndexOf( BOUNDARY_EQ );
        if( found >= 0 ) {
            String ret = mime.substring( found + BOUNDARY_EQ.length() );

            if( ret.startsWith( "\"" ) && ret.endsWith( "\"" )) {
                ret = ret.substring( 1, ret.length()-1 );
            }
            ret = "--" + ret;
            return ret;
        } else {
            throw new IllegalArgumentException( "No boundary could be found" );
        }
    }

    /**
     * Determine the boundary, in a byte array with appended CRLF.
     *
     * @param boundary boundary in String form
     * @param charset the character set
     * @return boundary in byte array form
     * @throws UnsupportedEncodingException thrown if the charset was not available
     */
    public static byte [] constructByteBoundary(
            String boundary,
            String charset )
        throws
            UnsupportedEncodingException
    {
        byte [] ret = boundary.getBytes( charset );

        return ret;
    }

    /**
     * Break a line into name and value, and add it to this map.
     *
     * @param line the line
     * @param map the map
     */
    public static void addNameValuePairTo(
            String             line,
            Map<String,String> map )
    {
        int colon = line.indexOf( ":" );

        String key;
        String value;
        if( colon >= 0 ) {
            key   = line.substring( 0, colon );
            value = line.substring( colon+1 );
        } else {
            key   = line;
            value = "";
        }

        key   = key.trim();
        value = value.trim();

        key = key.toLowerCase();
        
        map.put( key, value );
    }

    /**
     * Read a line from a ServletInputStream.
     *
     * @param inStream the InputStream
     * @param charset the character set to use
     * @return the read line
     * @throws IOException thrown if an I/O error occurred
     */
    public static String readStringLine(
            BufferedInputStream inStream,
            String              charset )
        throws
            IOException
    {
        byte [] buf = StreamUtils.slurpUntilBoundary( inStream, CRLF );
        if( buf == null || buf.length == 0 ) {
            return null;
        } else {
            if( buf.length >= CRLF.length ) {
                boolean found = true;
                for( int i=0 ; i<CRLF.length ; ++i ) {
                    if( CRLF[i] != buf[ buf.length-CRLF.length + i ] ) {
                        found = false;
                    }
                }
                if( found ) {
                    byte [] tmp = new byte[ buf.length-CRLF.length ];
                    System.arraycopy( buf, 0, tmp, 0, buf.length-CRLF.length );
                    buf = tmp;
                }
            }
            String ret = new String( buf, charset );
            return ret;
        }
    }

    /**
     * Strip the training boundary from this String. Also strip trailing CRLF.
     *
     * @param data the data to strip from
     * @param boundary the boundary to strip
     * @return the stripped data
     */
    public static byte [] stripTrailingBoundary(
            byte [] data,
            byte [] boundary )
    {
        if( data.length < boundary.length ) {
            // not enough data, this might be the last read and there is no boundary
            return data;
        }

        boolean found = true;
        for( int i=0 ; i<boundary.length ; ++i ) {
            if( boundary[i] != data[ data.length - boundary.length + i ] ) {
                found = false;
                break;
            }
        }
        if( !found ) {
            return data;
        }

        boolean found2 = true;
        for( int i=0 ; i<CRLF.length ; ++i ) {
            if( CRLF[i] != data[ data.length - boundary.length - CRLF.length + i ] ) {
                found2 = false;
                break;
            }
        }

        int lastIndex;
        if( found2 ) {
            lastIndex = data.length - boundary.length - CRLF.length;
        } else {
            lastIndex = data.length - boundary.length;
        }

        byte [] ret = new byte[ lastIndex ];
        System.arraycopy( data, 0, ret, 0, lastIndex );
        return ret;
    }

    /**
     * Advance the stream to the beginning of the next line.
     *
     * @param inStream the InputStream
     * @return true if there is more data
     * @throws IOException thrown if an I/O error occurred
     */
    public static boolean advanceToBeginningOfLine(
            BufferedInputStream inStream )
        throws
            IOException
    {
        int c1 = inStream.read();
        int c2 = inStream.read();

        if( c1 < 0 || c2 < 0 ) {
            return false;
        }
        if( c1 == '-' && c2 == '-' ) {
            return false;
        }
        if( c1 == CRLF[0] && c2 == CRLF[1] ) {
            return true; // everything ok
        }
        log.warn(
                "Unexpected characters found: \""
                + ((char)c1)
                + ((char)c2)
                + "\" (0x"
                + Integer.toHexString( c1 )
                + Integer.toHexString( c2 )
                + ")" );
        return false;
    }

    /**
     * The line feed for this purpose.
     */
    public static final byte [] CRLF = new byte[] { 0xd, 0xa };
}
