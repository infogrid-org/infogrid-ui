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

import org.infogrid.httpd.HttpAccessManager;
import org.infogrid.httpd.HttpEntity;
import org.infogrid.httpd.HttpEntityResponse;
import org.infogrid.httpd.HttpErrorHandler;
import org.infogrid.httpd.HttpErrorResponse;
import org.infogrid.httpd.HttpRequest;
import org.infogrid.httpd.HttpResponse;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.httpd.HttpStatusCodes;

import java.io.File;

/**
  * A simple HttpResponseFactory that serves content of the file system.
  */
public class FilesystemHttpResponseFactory
    implements
        HttpResponseFactory
{
    /**
      * Constructor.
      *
      * @param documentRoot the directory that is the top directory for the web server
      * @param errorHandler the ErrorHandler
      * @param accessMgr the AccessManager, if any
      */
    public FilesystemHttpResponseFactory(
            File              documentRoot,
            HttpErrorHandler  errorHandler,
            HttpAccessManager accessMgr )
    {
        theDocumentRoot  = documentRoot;
        theErrorHandler  = errorHandler;
        theAccessManager = accessMgr;
    }

    /**
     * Set a new HttpAccessManager.
     *
     * @param manager the new HttpAccessManager
     */
    public void setAccessManager(
            HttpAccessManager manager )
    {
        theAccessManager = manager;
    }

    /**
      * Factory method for a HttpResponse.
      *
      * @param req the HttpRequest for which we create a HttpResponse
      * @return the created HttpResponse
      */
    public HttpResponse createResponse(
            HttpRequest req )
    {
        HttpAccessManager accessMgr = theAccessManager; // this trick allows us to avoid a synchronized

        if( accessMgr != null && !accessMgr.isAllowed( req )) {
            return HttpErrorResponse.createWithChallenge( req, HttpStatusCodes.UNAUTHORIZED_CODE, accessMgr.getChallengeFor( req ), theErrorHandler );
        }

        HttpEntity foundEntity = FileEntity.create( req, theDocumentRoot );
        if( foundEntity == null ) {
            foundEntity = DirectoryEntity.create( req, theDocumentRoot );
        }
        if( foundEntity != null ) {
            return HttpEntityResponse.create( req, ! "HEAD".equalsIgnoreCase( req.getMethod()), foundEntity );
        }

        return HttpErrorResponse.create( req, HttpStatusCodes.NOT_FOUND_CODE, theErrorHandler );
    }

    /**
     * The web server's top document root.
     */
    protected File theDocumentRoot;

    /**
     * A suitable error handler.
     */
    protected HttpErrorHandler theErrorHandler;

    /**
     * The HttpAccessManager controlling access to resources.
     */
    protected HttpAccessManager theAccessManager;
}
