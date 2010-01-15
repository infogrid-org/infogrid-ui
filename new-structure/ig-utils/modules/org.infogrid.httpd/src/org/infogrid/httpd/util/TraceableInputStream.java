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

package org.infogrid.httpd.util;

import org.infogrid.util.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
  * This InputStream implementation provides additional methods to trace what is going over the
  * InputStream.
  */
public class TraceableInputStream
    extends
        InputStream
{
    private static final Log log = Log.getLogInstance( TraceableInputStream.class ); // our own, private logger

    /**
      * Constructor with a delegate and a traceMethodCallEntry channel.
      *
      * @param delegate the delegate InputStream
      * @param debug if non-null, we write everything we read to this stream as well
      */
    public TraceableInputStream(
            InputStream  delegate,
            OutputStream debug )
    {
        theDelegate = delegate;
        theDebug    = debug;
    }

    /**
      * Constructor with a delegate but no traceMethodCallEntry channel.
      *
      * @param delegate the delegate InputStream
      */
    public TraceableInputStream(
            InputStream delegate )
    {
        this( delegate, null );
    }

    /**
     * Returns the number of bytes that can be read (or skipped over) from
     * this input stream without blocking by the next caller of a method for
     * this input stream.
     *
     * @return     the number of bytes that can be read from this input stream
     *             without blocking.
     * @throws  IOException  if an I/O error occurs.
     */
    @Override
    public int available()
        throws
            IOException
    {
        int ret = theDelegate.available();

        return ret;
    }

    /**
     * Closes this input stream and releases any system resources associated
     * with the stream.
     *
     * @throws  IOException  if an I/O error occurs.
     */
    @Override
    public void close()
        throws
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( "closing" );
        }

        theDelegate.close();
    }

    /**
     * Marks the current position in this input stream.
     *
     * @param   readlimit   the maximum limit of bytes that can be read before
     *                      the mark position becomes invalid.
     * @see     java.io.InputStream#reset()
      */
    @Override
    public void mark(
            int readlimit )
    {
        theDelegate.mark( readlimit );
    }

    /**
     * Tests if this input stream supports the <code>mark</code> and
     * <code>reset</code> methods.
     *
     * @return  <code>true</code> if this stream instance supports the mark
     *          and reset methods; <code>false</code> otherwise.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.InputStream#reset()
     */
    @Override
    public boolean markSupported()
    {
        boolean ret = theDelegate.markSupported();

        return ret;
    }

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @throws  IOException  if an I/O error occurs.
     */
    public int read()
        throws
            IOException
    {
        int ret = theDelegate.read();

        if( theDebug != null && ret >= 0 ) {
            theDebug.write( ret );
            theDebug.flush();
        }
        return ret;
    }

    /**
     * Reads some number of bytes from the input stream and stores them into
     * the buffer array <code>b</code>. The number of bytes actually read is
     * returned as an integer.  This method blocks until input data is
     * available, end of file is detected, or an exception is thrown.
     *
     * @param      b   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> is there is no more data because the end of
     *             the stream has been reached.
     * @throws  IOException  if an I/O error occurs.
     * @throws  NullPointerException  if <code>b</code> is <code>null</code>.
     * @see        java.io.InputStream#read(byte[], int, int)
     */
    @Override
    public int read(
            byte [] b )
        throws
            IOException
    {
        int ret = theDelegate.read( b );

        if( theDebug != null ) {
            theDebug.write( b, 0, ret );
            theDebug.flush();
        }
        return ret;
    }

    /**
     * Reads up to <code>len</code> bytes of data from the input stream into
     * an array of bytes.  An attempt is made to read as many as
     * <code>len</code> bytes, but a smaller number may be read, possibly
     * zero. The number of bytes actually read is returned as an integer.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset in array <code>b</code>
     *                   at which the data is written.
     * @param      len   the maximum number of bytes to read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @throws  IOException  if an I/O error occurs.
     * @throws  NullPointerException  if <code>b</code> is <code>null</code>.
     * @see        java.io.InputStream#read()
     */
    @Override
    public int read(
            byte [] b,
            int     off,
            int     len )
        throws
            IOException
    {
        int ret = theDelegate.read( b, off, len );

        if( theDebug != null && ret > 0 ) {
            theDebug.write( b, 0, ret );
            theDebug.flush();
        }
        return ret;
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * @throws  IOException  if this stream has not been marked or if the
     *               mark has been invalidated.
     * @see     java.io.InputStream#mark(int)
     * @see     java.io.IOException
     */
    @Override
    public void reset()
        throws
            IOException
    {
        theDelegate.reset();
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @throws  IOException  if an I/O error occurs.
     */
    @Override
    public long skip(
            long n )
        throws
            IOException
    {
        long ret = theDelegate.skip( n );

        return ret;
    }

    /**
      * The underlying InputStream that we delegate to.
      */
    protected InputStream theDelegate;

    /**
      * If non-null, we copy everything to this stream as well.
      */
    protected OutputStream theDebug;
}
