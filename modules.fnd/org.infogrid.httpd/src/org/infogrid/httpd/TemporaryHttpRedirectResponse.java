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
  * A Response indicating a termporary Redirect.
  */
public class TemporaryHttpRedirectResponse
    extends
        HttpRedirectResponse
{
    /**
      * Factory method.
      *
      * @param req the incoming Request
      * @param newUrl the URL to which this RedirectResponse redirects
      * @return the new RedirectResponse
      */
    public static TemporaryHttpRedirectResponse create(
            HttpRequest req,
            String      newUrl )
    {
        return new TemporaryHttpRedirectResponse( req, newUrl );
    }

    /**
      * Private constructor, use factory method.
      *
      * @param req the incoming Request
      * @param newUrl the URL to which this RedirectResponse redirects
      */
    protected TemporaryHttpRedirectResponse(
            HttpRequest req,
            String      newUrl )
    {
        super( req, newUrl, HttpStatusCodes.TEMPORARY_REDIRECT_CODE );
    }
}
