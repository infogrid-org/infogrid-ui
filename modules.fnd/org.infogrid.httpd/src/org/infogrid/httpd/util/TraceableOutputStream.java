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
import java.io.OutputStream;

/**
  * This OutputStream implementation provides additional methods to trace what is going
  * over the OutputStream.
  */
public class TraceableOutputStream
    extends
        OutputStream
{
    private static final Log log = Log.getLogInstance( TraceableOutputStream.class ); // our own, private logger

    /**
      * Constructor with a delegate and a traceMethodCallEntry stream.
      *
      * @param delegate the delegate OutputStream
      * @param debug if non-null, we write everything we read to this stream as well
      */
    public TraceableOutputStream(
            OutputStream delegate,
            OutputStream debug )
    {
        theDelegate = delegate;
        theDebug    = debug;
    }

    /**
      * Constructor with a delegate but no traceMethodCallEntry stream.
      *
      * @param delegate the delegate OutputStream
      */
    public TraceableOutputStream(
            OutputStream delegate )
    {
        this( delegate, null );
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
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
     * Flushes this output stream and forces any buffered output bytes
     * to be written out.
     *
     * @throws  IOException  if an I/O error occurs.
     */
    @Override
    public void flush()
        throws
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( "flushing" );
        }
        theDelegate.flush();
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array
     * to this output stream.
     *
     * @param      b   the data.
     * @throws  IOException  if an I/O error occurs.
     * @see        java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(
            byte [] b )
        throws
            IOException
    {
        if( theDebug != null ) {
            theDebug.write( b );
            theDebug.flush();
        }
        theDelegate.write( b );
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @throws  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    @Override
    public void write(
            byte [] b,
            int     off,
            int     len )
        throws
            IOException
    {
        if( theDebug != null ) {
            theDebug.write( b, off, len );
            theDebug.flush();
        }
        theDelegate.write( b, off, len );
    }

    /**
     * Writes the specified byte to this output stream. The general
     * contract for <code>write</code> is that one byte is written
     * to the output stream. The byte to be written is the eight
     * low-order bits of the argument <code>b</code>. The 24
     * high-order bits of <code>b</code> are ignored.
     * <p>
     * Subclasses of <code>OutputStream</code> must provide an
     * implementation for this method.
     *
     * @param      b   the <code>byte</code>.
     * @throws  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> may be thrown if the
     *             output stream has been closed.
     */
    public void write(
            int b )
        throws
            IOException
    {
        if( theDebug != null ) {
            theDebug.write( b );
            theDebug.flush();
        }
        theDelegate.write( b );
    }

    /**
      * The OutputStream that we delegate to.
      */
    protected OutputStream theDelegate;

    /**
      * If non-null, we copy everything to this stream as well.
      */
    protected OutputStream theDebug;
}
