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

package org.infogrid.httpd.filesystem;

import org.infogrid.httpd.HttpEntity;
import org.infogrid.httpd.HttpRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Date;

/**
 * An Entity that is backed by a File in the file system.
 */
public class FileEntity
    extends
        HttpEntity
{
    /**
     * Factory method.
     *
     * @param req the incoming HttpRequest
     * @param rootDir the root directory of the web server
     * @return the created FileEntity
     */
    public static HttpEntity create(
            HttpRequest req,
            File        rootDir )
    {
        File f = new File( rootDir, req.getRelativeBaseUri() );

        if( !f.getAbsolutePath().startsWith( rootDir.getAbsolutePath() ) ) {
            return null;
        }

        if( !f.exists() ) {
            return null;
        }

        if( !f.isFile() ) {
            return null;
        }

        return new FileEntity( null, f );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param mime the MIME type of the file behind this FileEntity
     * @param f the File that is the file behind this FileEntity
     */
    protected FileEntity(
            String mime,
            File   f )
    {
        theFile = f;
        theMime = mime;
    }

    /**
     * Obtain the MIME type of this FileEntity.
     *
     * @return String representing the MIME type of this FileEntity, such as "text/html"
     */
    public String getMime()
    {
        if( theMime == null ) {
            theMime = determineMime( theFile );
        }

        return theMime;
    }

    /**
     * Helper to determine the MIME type of a File. This consults the FileNameMap
     * maintained by java.net.URLConnection.
     *
     * @param f the File whose MIME type is to be determined
     * @return the determine MIME type
     */
    public static String determineMime(
            File f )
    {
        String type = URLConnection.getFileNameMap().getContentTypeFor( f.getName() );
        return type;
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
        return new FileInputStream( theFile );
    }

    /**
     * Determine whether this FileEntity is readable.
     *
     * @return true if this FileEntity is readable
     */
    public boolean canRead()
    {
        return theFile.canRead();
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
        return new Date( theFile.lastModified() );
    }

    /**
     * The MIME type of this FileEntity.
     */
    protected String theMime;

    /**
     * The file.
     */
    protected File theFile;
}
