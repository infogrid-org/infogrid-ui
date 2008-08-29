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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;

/**
 * A drop-in <code>java.io.InputStream</code> that buffers all information that comes by.
 */
public class BufferingInputStream
        extends
            InputStream
{
    /**
     * Constructor.
     * 
     * @param delegate the delegate InputStream
     */
    public BufferingInputStream(
            InputStream delegate )
    {
        this( delegate, DEFAULT_INITIAL_BUFFER_SIZE );
    }
    
    /**
     * Constructor.
     *
     * @param delegate the delegate InputStream
     * @param bufsize initial buffer size
     */
    public BufferingInputStream(
            InputStream delegate,
            int         bufsize )
    {
        if( delegate == null ) {
            throw new IllegalArgumentException();
        }
        theDelegate    = delegate;
        theCacheStream = new ByteArrayOutputStream( bufsize );
    }
    
    /**
     * Reads the next byte of data from the InputStream.
     *
     * @return the next byte that was read
     * @throws IOException thrown if an I/O error occurred
     */
    public int read()
        throws
            IOException
    {
        int r = theDelegate.read();
        if( r != -1 && theCacheStream != null ) {
            theCacheStream.write( r );
        }
        return r;
    }
    
    /**
     * Reads the next array of bytes of data from the InputStream.
     *
     * @param b the data buffer to read into
     * @return the number of bytes read and written into the buffer
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public int read(
            byte b[] )
        throws
            IOException
    {
        int r = theDelegate.read( b );
        if( r != -1 && theCacheStream != null ) {
            theCacheStream.write( b, 0, r );
        }
        return r;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the InputStream into
     * an array of bytes.
     *
     * @param b the data buffer to read into
     * @param off the index into the buffer where we start writing
     * @param len the maximum number of bytes to write to the buffer
     * @return the number of bytes read and written into the buffer
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public int read(
            byte b[],
            int  off,
            int  len )
        throws
            IOException
    {
        int r = theDelegate.read( b, off, len );
        if( r != -1 && theCacheStream != null ) {
            theCacheStream.write( b, off, r );
        }
        return r;
    }
    
    /**
     * Skips over and discards <code>n</code> bytes of data from this InputStream.
     *
     * @param  n   the number of bytes to be skipped.
     * @return the actual number of bytes skipped.
     * @throws IOException thrown if an I/O error occurred
     * @throws UnsupportedOperationException always thrown, we don't know how to do this
     */
    @Override
    public long skip(
            long n )
        throws
            IOException
    {
        throw new UnsupportedOperationException( "don't know how to skip" );
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from this
     * input stream without blocking by the next caller of a method for this InputStream.
     *
     * @return the number of available bytes
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public int available()
        throws
            IOException
    {
        return theDelegate.available();
    }

    /**
     * Closes this InputStream and releases any system resources associated
     * with the InputStream.
     *
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public void close()
        throws
            IOException
    {       
        theDelegate.close();
        
        if( theCacheStream != null ) {
            theCacheStream.close();
        }
    }       
    
    /**
     * We do not support the mark for the time being.
     * 
     * @return always returns false
     */
    @Override
    public boolean markSupported()
    {
        return false;
    }   
    
    /**
     * Mark the present position in the InputStream.
     * 
     * @param readAheadLimit the maximum number of bytes falling back
     * @throws UnsupportedOperationException always thrown, we don't know how to do this
     */
    @Override
    public void mark(
            int readAheadLimit )
    {       
        throw new UnsupportedOperationException( "don't know how to mark" );
    }   
    
    /**
     * Reset the InputStream.
     * 
     * @throws UnsupportedOperationException always thrown, we don't know how to do this
     */
    @Override
    public void reset()
    {
        throw new UnsupportedOperationException( "don't know how to reset" );
    }

    /**
     * Obtain the buffer content.
     *
     * @return the buffer content
     */
    public byte [] getBuffer()
    {
        return theCacheStream.toByteArray();
    }

    /**
     * The default initial buffer size.
     */
    public static final int DEFAULT_INITIAL_BUFFER_SIZE = 4096;
    
    /**
     * The delegate InputStream.
     */
    protected InputStream theDelegate;

    /**
     * The OutputStream that buffers.
     */
    protected ByteArrayOutputStream theCacheStream;
}
