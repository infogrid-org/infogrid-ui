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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.infogrid.store.StoreValue;

/**
 * Knows how to encode and decode a StoreValue to and from an encoded byte stream for storage.
 */
public interface StoreValueMapper
{
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
            IOException;
    
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
            IOException;
    
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
            IOException;

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
            IOException;

}
