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

/**
 * Abstract superclass for code that can handle any single HTTP method such as GET.
 */
public abstract class HttpMethodHandler
{
    /**
     * Obtain the name of the HTTP method that this MethodHandler can handle.
     *
     * @return the name of the HTTP method, such as GET
     */
    public abstract String getMethodName();

    /**
      * Factory method for a Response.
      *
      * @param req the HttpRequest for which we create a HttpResponse
      * @param methodTable the HttpMethodHandlerTable that we belong to
      * @param errorHandler the HttpErrorHander that we use if an error occurs
      * @return the created Response
      */
    public abstract HttpResponse createResponse(
            HttpRequest            req,
            HttpMethodHandlerTable methodTable,
            HttpErrorHandler       errorHandler );
}
