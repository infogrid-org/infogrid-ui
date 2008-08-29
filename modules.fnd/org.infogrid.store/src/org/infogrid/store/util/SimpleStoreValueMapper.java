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

package org.infogrid.store.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.infogrid.store.StoreValue;
import org.infogrid.store.util.StoreValueMapper;
import org.infogrid.util.StreamUtils;

/**
 * Knows how to encode and decode a StoreValue to and from an encoded byte stream for storage.
 */
public class SimpleStoreValueMapper
        implements
            StoreValueMapper
{
    /**
     * Factory method.
     * 
     * @return the created SimpleStoreValueMapper
     */
    public static SimpleStoreValueMapper create()
    {
        if( theSingleton == null ) {
            theSingleton = new SimpleStoreValueMapper();
        }
        return theSingleton;
    }
    
    /**
     * Private constructor for subclasses only.
     */
    protected SimpleStoreValueMapper()
    {
        // noop
    }

    /**
     * Encode a StoreValue to a byte stream.
     * 
     * @param value the StoreValue to encode
     * @param stream the OutputStream to which to write the byte [] representation
     * @throws IOException thrown if an I/O error occurred
     */
    public void writeStoreValue(
            StoreValue   value,
            OutputStream stream )
        throws
            IOException
    {
        Writer w = new OutputStreamWriter( stream, CHARSET );
        
        w.write( value.getKey() );
        w.write(  CR );

        w.write( value.getEncodingId() );
        w.write(  CR );

        w.write( String.valueOf( value.getTimeCreated() ));
        w.write(  CR );

        w.write( String.valueOf( value.getTimeUpdated() ));
        w.write(  CR );

        w.write( String.valueOf( value.getTimeRead() ));
        w.write(  CR );
        
        w.write( String.valueOf( value.getTimeExpires() ));
        w.write(  CR );

        w.flush();

        stream.write( value.getData() );
    }
    
    /**
     * Encode a StoreValue to a byte stream.
     * 
     * @param value the StoreValue to encode
     * @param file the File to which to write the byte [] representation
     * @throws IOException thrown if an I/O error occurred
     */
    public void writeStoreValue(
            StoreValue   value,
            File         file )
        throws
            IOException
    {
        writeStoreValue( value, new FileOutputStream( file ));
    }

    /**
     * Decode a StoreValue from a byte stream.
     * 
     * @param in the byte [] representation, represented as stream
     * @return the decoded StoreValue
     * @throws IOException thrown if an I/O error occurred
     */
    public StoreValue readStoreValue(
            InputStream in )
        throws
            IOException
    {
        String key         = readUntilCr( in );
        String encodingId  = readUntilCr( in );
        String timeCreated = readUntilCr( in );
        String timeUpdated = readUntilCr( in );
        String timeRead    = readUntilCr( in );
        String timeExpires = readUntilCr( in );
        
        byte [] data = StreamUtils.slurp( in );
        
        StoreValue ret = new StoreValue(
                key,
                encodingId,
                Long.parseLong( timeCreated ),
                Long.parseLong( timeUpdated ),
                Long.parseLong( timeRead ),
                Long.parseLong( timeExpires ),
                data );
        return ret;
    }

    /**
     * Decode a StoreValue from a byte stream.
     * 
     * @param file the File containing the byte [] representation
     * @return the decoded StoreValue
     * @throws IOException thrown if an I/O error occurred
     */
    public StoreValue readStoreValue(
            File file )
        throws
            IOException
    {
        return readStoreValue( new FileInputStream( file ));
    }

    /**
     * Helper method to read through the next CR.
     * 
     * @param in the stream to read from
     * @return the read String
     * @throws IOException thrown if an I/O error occurred
     */
    protected String readUntilCr(
            InputStream in )
        throws
            IOException
    {
        byte [] buf = new byte[ 1024 ]; // seems reasonable
        int     pos = 0;
        int c;
        while( ( c = in.read()) != -1 ) {
            if( c == CR ) {
                break;
            }
            if( pos == buf.length ) {
                byte [] newBuf = new byte[ buf.length * 2];
                System.arraycopy( buf, 0, newBuf, 0, buf.length );
                buf = newBuf;
                // pos remains the same
            }
            buf[pos++] = (byte) c;
        }

        String ret = new String( buf, 0, pos, CHARSET.name() );
        return ret;
    }
    
    /**
     * Singleton instance.
     */
    protected static SimpleStoreValueMapper theSingleton;
    
    /**
     * The character to use as line separator.
     */
    public final char CR = '\n';

    /**
     * The character set to use.
     */
    public static final Charset CHARSET = Charset.forName( "UTF-8" );
}
