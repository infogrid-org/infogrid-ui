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

package org.infogrid.httpd;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import org.infogrid.util.ResourceHelper;

/**
  * An HTTP Response indicating an HTTP redirect.
  */
public class HttpRedirectResponse
    extends
        HttpResponse
{
    /**
     * Factory method.
     *
     * @param req the incoming HttpRequest
     * @param newUrl the URL to which this HttpRedirectResponse redirects
     * @param returnCode the HTTP return code
     * @return the created response
     */
    public static HttpRedirectResponse create(
            HttpRequest req,
            String      newUrl,
            String      returnCode )
    {
        return new HttpRedirectResponse( req, newUrl, returnCode );
    }

    /**
     * Constructor.
     *
     * @param req the incoming HttpRequest
     * @param newUrl the URL to which this HttpRedirectResponse redirects
     * @param returnCode the HTTP return code
     */
    protected HttpRedirectResponse(
            HttpRequest req,
            String      newUrl,
            String      returnCode )
    {
        super( req, returnCode );

        theNewUrl = newUrl;
    }

    /**
      * We override this to insert the URL to which we redirect.
      *
      * @param theWriter the Writer we write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    @Override
    protected void writeResponseHeader(
            Writer theWriter )
        throws
            IOException
    {
        super.writeResponseHeader( theWriter );

        theWriter.write( HttpResponseHeaderFields.LOCATION_TAG );
        theWriter.write( HttpResponseHeaderFields.SEPARATOR );
        theWriter.write( theNewUrl );
        theWriter.write( HttpResponseHeaderFields.CR );
    }

    /**
      * Write the content of this Response to an OutputStream. This method
      * also writes an HTML page that allows the user to manually redirect, if,
      * for some reason, the browser does not do it for them.
      *
      * @param theOutStream the OutputStream to write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    protected void writeContent(
            OutputStream theOutStream )
        throws
            IOException
    {
        Writer theWriter = new OutputStreamWriter( theOutStream );

        theWriter.write( theResourceHelper.getResourceStringWithArguments( "ManualRedirectBody", new Object[] { theNewUrl } ) );

        theWriter.flush();
    }

    /**
     * The new URL that we redirect to.
     */
    protected String theNewUrl;

    /**
     * Our ResourceHelper.
     */
    protected static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( HttpRedirectResponse.class );
}
