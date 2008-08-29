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
import org.infogrid.util.ResourceHelper;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;


/**
 * An HttpEntity that is backed by a Directory in the file system.
 * This could probably be made faster if we turned the factory method into
 * a smart factory backed by a table of WeakReferences, but our assumption is
 * that this web server only has to handle weak load.
 */
public class DirectoryEntity
    extends
        HttpEntity
{
    /**
     * Factory method.
     *
     * @param req the incoming HttpRequest
     * @param rootDir the root directory of the web server
     * @return the created DirectoryEntity
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

        if( !f.isDirectory() ) {
            return null;
        }

        return new DirectoryEntity( req.getRelativeBaseUri(), f );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param uri the base URI relative to the web server's document root
     * @param f the File that is the directory behind this DirectoryEntity
     */
    protected DirectoryEntity(
            String uri,
            File   f )
    {
        theUri  = uri;
        theFile = f;
    }

    /**
     * Obtain the MIME type of this DirectoryEntity.
     *
     * @return we return "text/html" for a DirectoryEntity
     */
    public String getMime()
    {
        return "text/html";
    }

    /**
     * Obtain the content of this DirectoryEntity as an InputStream. We generate
     * some HTML that shows the contents of the directory.
     *
     * @return the InputStream from which the content of this Entity can be read
     * @throws IOException thrown if a read error occurred
     */
    public InputStream getAsStream()
        throws
            IOException
    {
        StringBuffer data = new StringBuffer( 1024 );

        Object [] outerPars = new Object[] { theFile.getAbsolutePath(), theUri, theFile.getName() };

        data.append( MessageFormat.format( directoryPrefixString, outerPars ));

        File [] files = theFile.listFiles();
        for( int i=0 ; i<files.length ; ++i )
        {
            String fileName;
            String date = DateFormat.getDateTimeInstance().format( new Date( files[i].lastModified() ));
            String size;

            if( files[i].isDirectory() )
            {
                fileName = files[i].getName() + "/";
                size     = "-";
            }
            else
            {
                fileName = files[i].getName();
                size     = String.valueOf( files[i].length() );
            }
            Object [] innerPars = new Object[] { fileName, date, size };

            data.append( MessageFormat.format( directoryFileString, innerPars ));
        }

        data.append( MessageFormat.format( directoryPostfixString, outerPars ));

        return new ByteArrayInputStream( data.toString().getBytes() );
    }

    /**
     * Determine whether this Entity is readable.
     *
     * @return true if this Entity is readable
     */
    public boolean canRead()
    {
        return theFile.canRead();
    }

    /**
     * Determine when this DirectoryEntity was last modified.
     *
     * @return the Date the DirectoryEntity was last modified, or null
     */
    @Override
    public Date getLastModified()
    {
        return new Date( theFile.lastModified() );
    }

    /**
     * The file.
     */
    protected File theFile;

    /**
     * The Uri that we accessed.
     */
    protected String theUri;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( DirectoryEntity.class );

    /**
     * HTML emitted prior to listing the files.
     */
    private static final String directoryPrefixString = theResourceHelper.getResourceStringOrDefault(
            "DirectoryPrefix",
            "<html><head><title>Directory listing for {1}</title></head>\n"
                + "<body><h1>Directory listing for {1}</h1>\n"
                + "<table width=\"90%\" align=\"center\" border=\"1\">\n"
                + " <thead><tr><th>Name</th><th>Last modified</th><th>Size</th></tr></thead>\n"
                + " <tbody>\n" );

    /**
     * HTML emitted for each file in the directory.
     */
    private static final String directoryFileString = theResourceHelper.getResourceStringOrDefault(
            "DirectoryFileString",
            "  <tr><td><a href=\"{0}\">{0}</a></td><td>{1}</td><td>{2}</td></tr>\n" );

    /**
     * HTML emitted after listing the files.
     */
    private static final String directoryPostfixString = theResourceHelper.getResourceStringOrDefault(
            "DirectoryPostfix",
            " </tbody>\n"
                + "</table></body></html>\n" );
}
