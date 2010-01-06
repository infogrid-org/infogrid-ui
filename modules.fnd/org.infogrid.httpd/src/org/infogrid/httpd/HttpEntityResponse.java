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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.httpd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * A Response that returns what HTTP calls an Entity.
 */
public class HttpEntityResponse
    extends
        HttpResponse
{
    /**
     * Factory method for an EntityResponse with a message body.
     *
     * @param req the HttpRequest to which we respond
     * @param entity the HttpEntity that we return
     * @return the created HttpEntityResponse
     */
    public static HttpEntityResponse create(
            HttpRequest req,
            HttpEntity  entity )
    {
        return new HttpEntityResponse( req, HttpStatusCodes.OK_CODE, true, entity );
    }

    /**
     * Factory method for an EntityResponse that does not contain a message body.
     *
     * @param req the HttpRequest to which we respond
     * @param entity the HttpEntity that we return
     * @return the created HttpEntityResponse
     */
    public static HttpEntityResponse createHead(
            HttpRequest req,
            HttpEntity  entity )
    {
        return new HttpEntityResponse( req, HttpStatusCodes.OK_CODE, false, entity );
    }

    /**
     * Factory method for an EntityResponse that could be either full content or
     * head only.
     *
     * @param req the Request to which we respond
     * @param sendContent if true, we send content
     * @param entity the Entity that we return
     * @return the created EntityResponse
     */
    public static HttpEntityResponse create(
            HttpRequest req,
            boolean     sendContent,
            HttpEntity  entity )
    {
        return new HttpEntityResponse( req, HttpStatusCodes.OK_CODE, sendContent, entity );
    }

    /**
     * Factory method for an EntityResponse with a message body.
     *
     * @param req the HttpRequest to which we respond
     * @param statusCode the return status code
     * @param entity the HttpEntity that we return
     * @return the created HttpEntityResponse
     */
    public static HttpEntityResponse create(
            HttpRequest req,
            String      statusCode,
            HttpEntity  entity )
    {
        return new HttpEntityResponse( req, statusCode, true, entity );
    }

    /**
     * Factory method for an EntityResponse that does not contain a message body.
     *
     * @param req the HttpRequest to which we respond
     * @param statusCode the return status code
     * @param entity the HttpEntity that we return
     * @return the created HttpEntityResponse
     */
    public static HttpEntityResponse createHead(
            HttpRequest req,
            String      statusCode,
            HttpEntity  entity )
    {
        return new HttpEntityResponse( req, statusCode, false, entity );
    }

    /**
     * Factory method for an EntityResponse that could be either full content or
     * head only.
     *
     * @param req the Request to which we respond
     * @param statusCode the return status code
     * @param sendContent if true, we send content
     * @param entity the Entity that we return
     * @return the created EntityResponse
     */
    public static HttpEntityResponse create(
            HttpRequest req,
            String      statusCode,
            boolean     sendContent,
            HttpEntity  entity )
    {
        return new HttpEntityResponse( req, statusCode, sendContent, entity );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param req the HttpRequest to which we respond
     * @param statusCode the return status code
     * @param sendContent if true, we send content
     * @param entity the HttpEntity that we return
     */
    protected HttpEntityResponse(
            HttpRequest req,
            String      statusCode,
            boolean     sendContent,
            HttpEntity  entity )
    {
        super( req, statusCode );

        theSendContent = sendContent;
        theEntity      = entity;
    }

    /**
      * Write the response header aspects of this Response to a Writer.
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
        writeEntityHeader( theWriter );
    }

    /**
      * Write the entity headers of this Response to a Writer.
      *
      * @param theWriter the Writer we write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    protected void writeEntityHeader(
            Writer theWriter )
        throws
            IOException
    {
        // Allow
        Iterator allowIter = theEntity.allowIterator();
        if( allowIter.hasNext() )
        {
            theWriter.write( HttpEntityHeaderFields.ALLOW_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            while( allowIter.hasNext() )
            {
                String current = (String) allowIter.next();
                theWriter.write( current );
                if( allowIter.hasNext() ) {
                    theWriter.write( ", " );
                }
            }
        }
        theWriter.write( HttpResponseHeaderFields.CR );

        // Content-Range -- we do this out of alphabetical sequence because we need this before Content-Length
        int min = getRequest().getByteRangeStartIndex();
        int max = getRequest().getByteRangeEndIndex();
        if( min != 0 || max != -1 )
        {
            theWriter.write( HttpEntityHeaderFields.CONTENT_RANGE_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( HttpEntityHeaderFields.CONTENT_RANGE_BYTES_TAG );
            if( min != 0 ) {
                theWriter.write( String.valueOf( min ));
            }
            theWriter.write( '-' );
            if( max != -1 ) {
                theWriter.write( String.valueOf( max ));
            }
            theWriter.write( HttpResponseHeaderFields.CR );
        }

        // Content-Encoding: skipped for now
        // Content-Language: skipped
        String contentLanguage = theEntity.getContentLanguage();
        if( contentLanguage != null )
        {
            theWriter.write( HttpEntityHeaderFields.CONTENT_LANGUAGE_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( contentLanguage );
            theWriter.write( HttpResponseHeaderFields.CR );
        }

        // Content-Length
        int contentLength = theEntity.getContentLength();
        if( contentLength >= 0 )
        {
            if( min > contentLength ) {
                contentLength = 0;
            } else if( max == -1 || max > contentLength ) {
                contentLength -= min;
            } else {
                contentLength = max-min+1;
            }

            theWriter.write( HttpEntityHeaderFields.CONTENT_LENGTH_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( String.valueOf( contentLength ) );
            theWriter.write( HttpResponseHeaderFields.CR );
        }

        // Content-Location: skipped
        String contentLocation = theEntity.getContentLocation();
        if( contentLocation != null )
        {
            theWriter.write( HttpEntityHeaderFields.CONTENT_LOCATION_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( contentLocation );
            theWriter.write( HttpResponseHeaderFields.CR );
        }
        // Content-MD5: skipped

        // Content-Type
        String mime = theEntity.getMime();
        if( mime != null && mime.length() != 0 )
        {
            theWriter.write( HttpEntityHeaderFields.CONTENT_TYPE_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( mime );
            theWriter.write( HttpResponseHeaderFields.CR );
        }

        DateFormat myRfc1123Format = (DateFormat) theRfc1123Format.clone(); // DateFormat is not thread safe

        // Expires
        Date expires = theEntity.getExpires();
        if( expires != null )
        {
            theWriter.write( HttpEntityHeaderFields.EXPIRES_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( myRfc1123Format.format( expires ) );
            theWriter.write( HttpResponseHeaderFields.CR );
        }

        // Last-Modified
        Date lastModified = theEntity.getLastModified();
        if( lastModified != null )
        {
            theWriter.write( HttpEntityHeaderFields.LAST_MODIFIED_TAG );
            theWriter.write( HttpResponseHeaderFields.SEPARATOR );
            theWriter.write( myRfc1123Format.format( lastModified ) );
            theWriter.write( HttpResponseHeaderFields.CR );
        }
    }

    /**
      * Write this Response to an OutputStream.
      *
      * @param theOutStream the OutputStream to write to
      * @throws IOException thrown if an error occurred while attempting to write the OutputStream
      */
    protected void writeContent(
            OutputStream theOutStream )
        throws
            IOException
    {
        if( !theSendContent ) {
            return;
        }

        try {
            InputStream theInStream = theEntity.getAsStream();
            if( theInStream==null ) {
                return;
            }

            int min = getRequest().getByteRangeStartIndex();
            int max = getRequest().getByteRangeEndIndex();


            byte [] buf = new byte[ 512 ];
            int totalCount = 0;
            int currentCount;

            while( ( currentCount = theInStream.read( buf )) > 0 )
            {
                // determine start index
                if( totalCount+currentCount > min )
                {
                    int startIndex = min-totalCount;
                    if( startIndex < 0 ) {
                        startIndex = 0;
                    }

                    if( max == -1 || totalCount+currentCount < max )
                    {
                        int length = currentCount-startIndex;
                        // write to the end of the buffer
                        theOutStream.write( buf, startIndex, length );
                    }
                    else
                    {
                        int endIndex = max-totalCount+1; // +1: inclusive end
                        if( endIndex > buf.length ) {
                            endIndex = buf.length;
                        }
                        int length = endIndex - startIndex;

                        // don't write to the end of the buffer
                        theOutStream.write( buf, startIndex, length );
                    }
                }
                else
                {
                    // skip
                }

                totalCount += currentCount;
            }
        } catch( IOException ex ) {
            // do nothing -- FIXME?
        }
    }

    /**
      * Obtain the HttpEntity that is sent back with this Response.
      *
      * @return the HttpEntity that is being sent back
      */
    public HttpEntity getEntity()
    {
        return theEntity;
    }

    /**
     * If true, we send the content; no content if false.
     */
    private boolean theSendContent;

    /**
      * The HttpEntity in this Response.
      */
    protected HttpEntity theEntity;

}
