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

package org.infogrid.jee.servlet;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * <p>A ServletResponse that buffers all content. It does not write any content
 *    to the delegate stream; the caller must do that.</p>
 * 
 * <p>Both a byte[] and a String buffer may be created, in order to avoid converting
 *    to and from String and byte [] all the time.</p>
 */
@SuppressWarnings("deprecation")
public class BufferedServletResponse
        extends
            HttpServletResponseWrapper
        implements
            HttpServletResponse
{
    /**
     * Constructor.
     *
     * @param delegate the underlying HttpServletResponse
     */
    public BufferedServletResponse(
            HttpServletResponse delegate )
    {
        super( delegate );
    }

    /**
     * Obtain the buffered ServletOutputStream. This triggers the creation of a byte buffer.
     *
     * @return the buffered ServletOutputStream
     * @throws IOException an I/O problem occurred
     */
    @Override
    public ServletOutputStream getOutputStream()
        throws
            IOException
    {
        if( theServletOutputStream == null ) {
            if( theOutputStream == null ) {
                theOutputStream = new ByteArrayOutputStream( 2048 );
            }
            theServletOutputStream = new MyServletOutputStream( theOutputStream );
        }
        return theServletOutputStream;
    }

    /**
     * Obtain the buffered PrintWriter. This triggers the creation of a String buffer.
     *
     * @return the buffered PrintWriter
     */
    @Override
    public PrintWriter getWriter()
    {
        if( thePrintWriter == null ) {
            if( theWriter == null ) {
                theWriter = new StringWriter( 2048 );
            }
            thePrintWriter = new PrintWriter( theWriter );
        }
        return thePrintWriter;
    }
 
    /**
     * Obtain the entire buffered output that was written via the PrintWriter.
     *
     * @return the buffered output, or null
     * @throws IOException an I/O problem occurred
     */
    public String getBufferedPrintWriterOutput()
        throws
            IOException
    {
        if( thePrintWriter != null ) {
            thePrintWriter.flush();
        }
        if( theWriter != null ) {
            String ret = theWriter.getBuffer().toString();
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Obtain the entire buffered output that was written via the ServletOutputStream.
     *
     * @return the buffered output, or null
     * @throws IOException an I/O problem occurred
     */
    public byte [] getBufferedServletOutputStreamOutput()
        throws
            IOException
    {
        if( theServletOutputStream != null ) {
            theServletOutputStream.flush();
        }
        if( theOutputStream != null ) {
            byte [] ret = theOutputStream.toByteArray();
            return ret;
        } else {
            return null;
        }
    }

    /**
     * Flush the buffer.
     *
     * @throws IOException an I/O problem occurred
     */
    @Override
    public void flushBuffer()
        throws
            IOException
    {
        if( thePrintWriter != null ) {
            thePrintWriter.flush();
        }
        if( theOutputStream != null ) {
            theOutputStream.flush();
        }
    }
       
    /**
     * Is this content committed?
     *
     * @return always false
     */
    @Override
    public boolean isCommitted()
    {
        return false;
    }

    /**
     * Reset the entire response.
     */
    @Override
    public void reset()
    {
        resetCache();
        super.reset();
    }

    /**
     * Reset the buffer.
     */
    @Override
    public void resetBuffer()
    {
        resetCache();
        super.resetBuffer();
    }

    /**
     * Determine whether the buffer is empty or anything has been written into it.
     * 
     * @return true if this buffer is empty
     */
    public boolean isEmpty()
    {
        if( theOutputStream != null && theOutputStream.size() > 0 ) {
            return false;
        }
        if( theWriter != null && theWriter.getBuffer().length() > 0 ) {
            return false;
        }
        return true;
    }

    /**
     * Reset the locally held cache.
     */
    protected void resetCache()
    {
        if( thePrintWriter != null ) {
            thePrintWriter = null;
        }
        if( theWriter != null ) {
            theWriter = null;
        }
        if( theServletOutputStream != null ) {
            theServletOutputStream = null;
        }
        if( theOutputStream != null ) {
            theOutputStream = null;
        }
    }

    /**
     * I can't believe that there is no API to obtain the set content type.
     *
     * @param contentType the new content type
     */
    @Override
    public void setContentType(
            String contentType )
    {
        theContentType = contentType;
        
        super.setContentType( contentType );
    }

    /**
     * I can't believe that there is no API to obtain the set content type.
     *
     * @return the content type
     */
    public String getContentType()
    {
        return theContentType;
    }

    /**
     * Determine whether or not this context type is text.
     *
     * @return true if this content type is text
     */
    public boolean isText()
    {
        String type = theContentType.toLowerCase();
        if( type.startsWith( "text/" )) {
            return true;
        }
        if( type.startsWith( "application/xhtml" )) {
            return true;
        }
        if( type.startsWith( "application/xml" )) {
            return true;
        }
        return false;
    }

    /**
     * Content type of this response.
     */
    protected String theContentType;
    
    /**
     * Byte-buffer to write into.
     */
    protected ByteArrayOutputStream theOutputStream;
    
    /**
     * ServletOutputStream on top of theOutputStream.
     */
    protected ServletOutputStream theServletOutputStream;
    
    /**
     * String-buffer to write into.
     */
    protected StringWriter theWriter;
    
    /**
     * PrintWriter on top of the theWriter.
     */
    protected PrintWriter thePrintWriter;
    
    /**
     * Simple implementation of ServletOutputStream.
     */
    static class MyServletOutputStream
            extends
                ServletOutputStream
    {
        /**
         * Constructor.
         *
         * @param delegate the OutputStream to write to.
         */
        public MyServletOutputStream(
                OutputStream delegate )
        {
            theDelegate = delegate;
        }
        
        /**
         * Write method.
         *
         * @param i the integer to write
         * @throws IOException
         */
        public void write(
                int i )
            throws
                IOException
        {
            theDelegate.write( i );
        }

        /**
         * The underlying stream.
         */
        protected OutputStream theDelegate;
    }
}
