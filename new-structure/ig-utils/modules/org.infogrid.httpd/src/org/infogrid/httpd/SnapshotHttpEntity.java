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

package org.infogrid.httpd;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * An Entity that is backed by snapshot data.
 */
public class SnapshotHttpEntity
    extends
        HttpEntity
{
    /**
     * Constructor.
     *
     * @param mime the MIME type of the content behind this Entity
     * @param content the content behind this Entity
     * @param canRead true if this Entity can be read
     */
    public SnapshotHttpEntity(
            String  mime,
            String  content,
            boolean canRead )
    {
        theMime    = mime;
        theContent = content;
        theCanRead = canRead;
    }

    /**
     * Obtain the MIME type of this FileEntity.
     *
     * @return String representing the MIME type of this FileEntity, such as "text/html"
     */
    public String getMime()
    {
        return theMime;
    }

    /**
     * Obtain the content of this FileEntity as an InputStream.
     *
     * @return the InputStream from which the content of this FileEntity can be read
     * @throws IOException thrown if a read error occurred
     */
    public InputStream getAsStream()
        throws
            IOException
    {
        return new ByteArrayInputStream( theContent.getBytes() );
    }

    /**
     * Determine whether this FileEntity is readable.
     *
     * @return true if this FileEntity is readable
     */
    public boolean canRead()
    {
        return theCanRead;
    }

    /**
     * Determine when this FileEntity was last modified. This may return null,
     * indicating that such date is not known.
     *
     * @return the Date the entity was last modified, or null
     */
    @Override
    public Date getLastModified()
    {
        return theLastModified;
    }

    /**
     * The MIME type of this RootEntitySetEntity.
     */
    protected String theMime;

    /**
     * The HTML or other content behind this Entity.
     */
    protected String theContent;

    /**
     * Can we read this Entity?
     */
    protected boolean theCanRead;

    /**
     * The Date at which content behind this Entity was last modified.
     */
    protected Date theLastModified;
}
