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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

/**
  * A Response indicating an error.
  */
public class HttpErrorResponse
    extends
        HttpResponse
{
    /**
      * Factory method for any error other than an authentication error.
      *
      * @param req the incoming HttpRequest
      * @param code the error code
      * @param errorHandler the ErrorHandler from which we find the correct content for the Response
      * @return the created HttpErrorResponse
      */
    public static HttpErrorResponse create(
            HttpRequest      req,
            String           code,
            HttpErrorHandler errorHandler )
    {
        return new HttpErrorResponse( req, code, null, errorHandler );
    }

    /**
     * Factory method for an ErrorHttpResponse with an authentication challenge.
     *
     * @param req the incoming HttpRequest
     * @param code the error code
     * @param challenge the authentication challenge
     * @param errorHandler the ErrorHandler from which we find the correct content for the Response
     * @return the created ErrorResponse
     */
    public static HttpErrorResponse createWithChallenge(
            HttpRequest      req,
            String           code,
            String           challenge,
            HttpErrorHandler errorHandler )
    {
        return new HttpErrorResponse( req, code, challenge, errorHandler );
    }

    /**
      * Factory method for the "server configured" error.
      *
      * @param req the incoming HttpRequest
      * @param code the error code
      * @return the created ErrorResponse
      */
    public static HttpErrorResponse createUnconfigured(
            HttpRequest req,
            String      code )
    {
        return new HttpErrorResponse( req, code, null, null );
    }

    /**
      * Private constructor, use factory method.
      *
     * @param req the incoming HttpRequest
     * @param code the error code
     * @param challenge the authentication challenge, if any
     * @param errorHandler the HttpErrorHandler from which we find the correct content for the HttpResponse
      */
    protected HttpErrorResponse(
            HttpRequest      req,
            String           code,
            String           challenge,
            HttpErrorHandler errorHandler )
    {
        super( req, code );

        theChallenge    = challenge;
        theErrorHandler = errorHandler;
    }

    /**
      * Override this to append the WWW-authenticate field if necessary.
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

        if( theChallenge != null ) {
            // WWW-Authenticate: needed for password-protected URL
            theWriter.write( HttpResponseHeaderFields.WWW_AUTHENTICATE_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( theChallenge );
            theWriter.write( HttpResponseHeaderFields.CR );
        }
    }

    /**
      * Write the content of this Response to an OutputStream.
      *
      * @param theOutStream the OutputStream to write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    protected void writeContent(
            OutputStream theOutStream )
        throws
            IOException
    {
        HttpErrorHandler handler = theErrorHandler; // this trick allows us to not synchronize
        if( handler != null ) {
            InputStream theInStream = handler.obtainContentForError( getRequest(), getReturnCode() );

            byte [] buf = new byte[ 512 ];
            int count;

            while( ( count = theInStream.read( buf, 0, buf.length )) > 0 ) {
                theOutStream.write( buf, 0, count );
            }

        } else {
            PrintStream print = new PrintStream( theOutStream );
            print.print( "<html><head><title>Error: not configured.</title></head>\n"
                + "<body><h1>This web server has been insufficiently configured.</h1>\n"
                + "</body></html>" );
            print.flush();
        }
    }

    /**
     * Our HttpErrorHandler.
     */
    protected HttpErrorHandler theErrorHandler;

    /**
     * The Challenge, if any.
     */
    protected String theChallenge;
}
