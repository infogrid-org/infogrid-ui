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

package org.infogrid.lid;

import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.util.logging.Log;

/**
 * An AbortProcessingException that carries special content to be emitted instead
 * of the default response.
 */
public abstract class SpecialContentAbortProcessingException
        extends
            AbortProcessingException
{
    private static final Log log = Log.getLogInstance( SpecialContentAbortProcessingException.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param source the LidService that threw this exception
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param content the content with which to respond
     * @param mime the MIME of the content
     */
    protected SpecialContentAbortProcessingException(
            LidService         source,
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse,
            byte []            content,
            String             mime )
    {
        super( source, lidRequest, lidResponse, HttpServletResponse.SC_OK, null, null );
        
        theContent = content;
    }
    
    /**
     * Convert a String into a byte array according to the default character set.
     * 
     * @param s the String
     * @return the bytes
     */
    protected static byte [] stringToBytes(
            String s )
    {
        byte [] ret;

        try {
            ret = s.getBytes( "UTF-8" );

        } catch( UnsupportedEncodingException ex ) {
            log.error( ex );
            ret = s.getBytes();
        }
        return ret;
    }
    
    /**
     * The content to send.
     */
    protected byte [] theContent;
    
    /**
     * The MIME type to send.
     */
    protected String theMime;
}
