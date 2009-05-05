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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility functions for I/O Streams.
 */
public abstract class StreamUtils
{
    /**
     * Read a file into a byte [].
     * 
     * @param f the File
     * @return the buffer into which the bytes have been written
     * @throws IOException an I/O error occurred
     */
    public static byte [] slurp(
            File f )
        throws
            IOException
    {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream( f );
         
            byte [] ret = slurp( inStream );
            return ret;

        } finally {
            if( inStream != null ) {
                inStream.close();
            }
        }
    }

    /**
     * Read an InputStream until EOF and put the content that was found into a byte[].
     *
     * @param inStream the InputStream
     * @return the buffer into which the bytes have been written
     * @throws IOException an I/O error occurred
     */
    public static byte [] slurp(
            InputStream inStream )
        throws
            IOException
    {
        return slurp( inStream, Integer.MAX_VALUE, 512 );
    }

    /**
     * Read an InputStream until EOF but not further than until a maximum number of bytes
     * have been read and put found content into a byte[].
     *
     * @param inStream the InputStream
     * @param maxBytes the maximum number of bytes to read
     * @return the buffer into which the bytes have been written
     * @throws IOException an I/O error occurred
     */
    public static byte [] slurp(
            InputStream inStream,
            int         maxBytes )
        throws
            IOException
    {
        return slurp( inStream, maxBytes, 512 );
    }

    /**
     * Read an InputStream until EOF but not further than until a maximum number of bytes
     * have been read and put found content into a byte[]. Also specify a default initial
     * buffer size.
     *
     * @param inStream the InputStream
     * @param maxBytes the maximum number of bytes to read
     * @param initBufSize the initial buffer size
     * @return the buffer into which the bytes have been written
     * @throws IOException an I/O error occurred
     */
    public static byte [] slurp(
            InputStream inStream,
            int         maxBytes,
            int         initBufSize )
        throws
            IOException
    {
        if( initBufSize <= 0 ) {
            throw new IllegalArgumentException( "Initial buffer size cannot be 0 or negative" );
        }
        byte [] buf = new byte[ initBufSize ];

        int offset = 0;

        while( true ) {
            
            int stillToReadIntoBuf;

            if( buf.length < maxBytes || maxBytes == -1 ) {
                stillToReadIntoBuf = buf.length - offset;
            } else {
                stillToReadIntoBuf = maxBytes - offset;
            }

            int count = inStream.read( buf, offset, stillToReadIntoBuf );
        
            if( count == -1 || offset + count == maxBytes ) {
                int length = offset;
                if( count > 0 ) {
                    length += count;
                }
                if( buf.length == length ) {
                    return buf;
                } else {
                    byte [] ret = new byte[ length ];
                    System.arraycopy( buf, 0, ret, 0, length );
                    return ret;
                }
            } else if( offset + count == buf.length ) {
                // double buffer and keep reading
                byte [] newBuf = new byte[ buf.length*2 ];
                System.arraycopy( buf, 0, newBuf, 0, buf.length );
                buf    = newBuf;
            }
            offset += count;
        }
    }

    /**
     * Read an InputStream until EOF but no further than util a boundary marker has been found.
     * The stream position will be right after the boundary.
     *
     * @param inStream the InputStream
     * @param boundary the boundary to look for
     * @return the buffer into which the bytes have been written
     * @throws IOException an I/O error occurred
     */
    public static byte [] slurpUntilBoundary(
            InputStream inStream,
            byte []     boundary )
        throws
            IOException
    {
        byte [] ret = slurpUntilBoundary(
                inStream,
                new byte[][] {
                    boundary
                });
        return ret;
    }

    /**
     * Read an InputStream until EOF but no further than util one of several possible boundary markers has been found.
     * The stream position will be right after the found boundary.
     *
     * @param inStream the InputStream
     * @param boundaries the boundaries to look for
     * @return the buffer into which the bytes have been written
     * @throws IOException an I/O error occurred
     */
    public static byte [] slurpUntilBoundary(
            InputStream inStream,
            byte [][]   boundaries )
        throws
            IOException
    {
        byte [] ret = new byte[ 512 ];

        int count = 0;

        outer:
        while( true ) {
            int b = inStream.read();
            if( b < 0 ) {
                break; // we are done
            }
            if( count == ret.length ) {
                byte [] tmp = new byte[ ret.length * 2 ];
                System.arraycopy( ret, 0, tmp, 0, ret.length );
                ret = tmp;
            }
            ret[count++] = (byte) b;

            // Now compare
            for( int i=0 ; i<boundaries.length ; ++i ) {
                byte [] currentBoundary = boundaries[i];
                if( count < currentBoundary.length ) {
                    continue; // can't be, we haven't read enough
                }

                boolean found = true;
                for( int j=0 ; j<currentBoundary.length ; ++j ) {
                    if( currentBoundary[j] != ret[count-currentBoundary.length+j ] ) {
                        found = false;
                        break;
                    }
                }
                if( found ) {
                    break outer;
                }
            }
        }
        if( count < ret.length ) {
            byte [] tmp = new byte[ count ];
            System.arraycopy( ret, 0, tmp, 0, count );
            ret = tmp;
        }
        return ret;
    }

    /**
     * Read a Reader until EOF and put found content into a String.
     *
     * @param inReader the Reader
     * @return the String into which the content have been written
     * @throws IOException an I/O error occurred
     */
    public static String slurp(
            Reader inReader )
        throws
            IOException
    {
        return slurp( inReader, -1, DEFAULT_INIT_BUFFER_SIZE );
    }

    /**
     * Read a Reader until EOF, or until a certain number of characters have been read
     * and put found content into a String.
     *
     * @param inReader the Reader
     * @param maxChars the maximum number of characters to read
     * @return the String into which the content have been written
     * @throws IOException an I/O error occurred
     */
    public static String slurp(
            Reader inReader,
            int    maxChars )
        throws
            IOException
    {
        return slurp( inReader, maxChars, DEFAULT_INIT_BUFFER_SIZE );
    }

    /**
     * Read a Reader until EOF, or until a certain number of characters have been read
     * and put found content into a String.
     *
     * @param inReader the Reader
     * @param maxChars the maximum number of characters to read
     * @param initBufSize the initial buffer size
     * @return the String into which the content have been written
     * @throws IOException an I/O error occurred
     */
    public static String slurp(
            Reader inReader,
            int    maxChars,
            int    initBufSize )
        throws
            IOException
    {
        if( initBufSize <= 0 ) {
            throw new IllegalArgumentException( "Initial buffer size cannot be 0 or negative" );
        }
        
        char [] buf = new char[ initBufSize ];

        int offset = 0;

        while( true ) {
            int toRead;
            if( maxChars == -1 || buf.length < maxChars ) {
                toRead = buf.length-offset;
            } else {
                toRead = maxChars-offset;
            }
            int count = inReader.read( buf, offset, toRead );

            if( count == 0 ) {
                return new String( buf );

            } else if( count < buf.length-offset ) {
                // chop off what we don't need
                char [] ret = new char[ offset+count ];
                System.arraycopy( buf, 0, ret, 0, offset+count );

                return new String( ret );

            } else if( count == maxChars-offset ) {
                // chop off what we don't need
                char [] ret = new char[ maxChars ];
                System.arraycopy( buf, 0, ret, 0, maxChars );

                return new String( ret );

            } else {
                // double buffer and keep reading
                char [] newBuf = new char[ buf.length*2 ];
                System.arraycopy( buf, 0, newBuf, 0, buf.length );
                offset = buf.length;
                buf    = newBuf;
            }
        }
    }

    /**
     * Replace all tokens in a String.
     *
     * @param inputString the String where the tokens shall be replaced
     * @param tokenMap the Map of tokens and their replacements
     * @return the String with the token replaced
     */
    public static String replaceTokens(
            String             inputString,
            Map<String,String> tokenMap )
    {
        StringBuffer     ret  = new StringBuffer( inputString.length() + 10 );
        Iterator<String> iter = tokenMap.keySet().iterator();

        ret.append( inputString );

        while( iter.hasNext() ) {
            String token = iter.next();
            String value = tokenMap.get( token );

            int found = 0;
            while( ( found = ret.indexOf( token, found )) > 0 ) {
                ret.replace( found, found+token.length(), value );
            }
        }
        return ret.toString();
    }

    /**
     * The default initial buffer size for the slurp.
     */
    public static int DEFAULT_INIT_BUFFER_SIZE = 1024;
}
