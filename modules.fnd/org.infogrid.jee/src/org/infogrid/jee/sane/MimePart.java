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

package org.infogrid.jee.sane;

import java.util.Map;

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
     * @return the created MimePart
     */
    public static MimePart create(
            String             name,
            Map<String,String> headers,
            String             disposition,
            byte []            content,
            String             mimeType )
    {
        return new MimePart( name, headers, disposition, content, mimeType );
    }

    /**
     * Constructor.
     *
     * @param name the name of the part
     * @param headers the headers of the part
     * @param disposition disposition of the part
     * @param content the content of the part
     * @param mimeType the content type of the part
     */
    protected MimePart(
            String             name,
            Map<String,String> headers,
            String             disposition,
            byte []            content,
            String             mimeType )
    {
        theName        = name;
        theHeaders     = headers;
        theDisposition = disposition;
        theContent     = content;
        theMimeType    = mimeType;
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
}
