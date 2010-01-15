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

import org.infogrid.util.logging.Log;

import java.io.IOException;

// We are not supposed to import from this package, but well, the code is there,
// why duplicate as long as it works? FIXME?
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Collects Base64 utilities.
 */
public abstract class Base64
{
    private static final Log log = Log.getLogInstance( Base64.class ); // our own, private logger

    /**
     * Helper method to decode base64.
     *
     * @param data the input data
     * @return the output data
     */
    public static byte[] base64decode(
            String data )
    {
        try {
            byte [] ret = theDecoder.decodeBuffer( data );
            return ret;

        } catch( IOException ex ) {
            log.error( ex );
            return new byte[0];
        }
    }

    /**
     * Helper method to encode base64.
     *
     * @param data the input data
     * @return the output data
     */
    public static String base64encode(
            byte [] data )
    {
        String ret = theEncoder.encode( data );
        return ret;
    }

    /**
     * Helper method to encode base64, but without line feeds / carriage returns
     *
     * @param data the input data
     * @return the output data
     */
    public static String base64encodeNoCr(
            byte [] data )
    {
        String       encoded = base64encode( data );
        StringBuffer buf     = new StringBuffer( encoded.length() );

        // remove
        int cr1 = 0;
        int cr2 = 0;
        while( ( cr2 = encoded.indexOf( "\n", cr1 )) >= 0 ) {
            buf.append( encoded.substring( cr1, cr2 ));
            cr1 = cr2+1;
        }
        buf.append( encoded.substring( cr1 ));
        String ret = buf.toString();
        return ret;
    }

    /**
     * Shared Encoder.
     */
    protected static BASE64Encoder theEncoder = new BASE64Encoder();

    /**
     * Shared Decoder.
     */
    protected static BASE64Decoder theDecoder = new BASE64Decoder();
}
