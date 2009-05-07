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

package org.infogrid.util.http;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.MapCursorIterator;

/**
 * Any MIME part conveyed as part of a request using form-data.
 */
public class MimePart
{
    /**
     * Factory method.
     *
     * @param name the name of the part
     * @param headers the headers of the part
     * @param disposition disposition of the part
     * @param content the content of the part
     * @param mimeType the content type of the part
     * @param charset the encoding of the part if it needs to converted to String
     * @return the created MimePart
     */
    public static MimePart create(
            String             name,
            Map<String,String> headers,
            String             disposition,
            byte []            content,
            String             mimeType,
            String             charset )
    {
        return new MimePart( name, headers, disposition, content, mimeType, charset );
    }

    /**
     * Constructor.
     *
     * @param name the name of the part
     * @param headers the headers of the part
     * @param disposition disposition of the part
     * @param content the content of the part
     * @param mimeType the content type of the part
     * @param charset the encoding of the part if it needs to converted to String
     */
    protected MimePart(
            String             name,
            Map<String,String> headers,
            String             disposition,
            byte []            content,
            String             mimeType,
            String             charset )
    {
        theName        = name;
        theHeaders     = headers;
        theDisposition = disposition;
        theContent     = content;
        theMimeType    = mimeType;
        theCharset     = charset;
    }

    /**
     * Obtain the name.
     *
     * @return the name
     */
    public String getName()
    {
        return theName;
    }

    /**
     * Obtain an iterator over the names of the headers of this MimePart.
     *
     * @return the iterator
     */
    public CursorIterator<String> getHeaderNames()
    {
        return MapCursorIterator.createForKeys( theHeaders, String.class, String.class );
    }

    /**
     * Obtain the value of the header.
     *
     * @param name name of the header
     * @return the value, if any
     */
    public String getHeader(
            String name )
    {
        String ret = theHeaders.get( name );
        return ret;
    }

    /**
     * Obtain the disposition field.
     *
     * @return the disposition
     */
    public String getDisposition()
    {
        return theDisposition;
    }

    /**
     * Obtain the content of the MimePart.
     *
     * @return the content
     */
    public byte [] getContent()
    {
        return theContent;
    }

    /**
     * Obtain the content of the MimePart as String. This will attempt to
     * convert regardless of whether the content is actually text. Check
     * MIME type first.
     *
     * @return the content as String
     * @throws UnsupportedEncodingException thrown if the byte array could not be decoded
     */
    public String getContentAsString()
        throws
            UnsupportedEncodingException
    {
        String ret = new String( theContent, theCharset );
        return ret;
    }

    /**
     * Obtain the MIME type of the MimePart.
     * 
     * @return the MIME type
     */
    public String getMimeType()
    {
        return theMimeType;
    }

    /**
     * Name of this part.
     */
    protected String theName;

    /**
     * Headers of this part.
     */
    protected Map<String,String> theHeaders;

    /**
     * Disposition of the part.
     */
    protected String theDisposition;

    /**
     * Content of the part.
     */
    protected byte [] theContent;

    /**
     * MIME type of the part.
     */
    protected String theMimeType;

    /**
     * Charset of the part if decoded to String.
     */
    protected String theCharset;
}
