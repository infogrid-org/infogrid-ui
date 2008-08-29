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

import org.infogrid.util.ResourceHelper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;

/**
 * A very simple default implementation of the HttpErrorHandler.
 */
public class DefaultHttpErrorHandler
    implements
        HttpErrorHandler
{
    /**
     * Obtain a Stream with content in response to a particular error.
     *
     * @param req the incoming HttpRequest whose processing caused the error
     * @param statusCode the error status code
     * @return the InputStream providing the content
     */
    public InputStream obtainContentForError(
            HttpRequest req,
            String      statusCode )
    {
        String content = MessageFormat.format( errorString, new Object[] { statusCode } );

        return new ByteArrayInputStream( content.getBytes() );
    }

    /**
     * The error String in MessageFormat.
     */
    private static final String errorString = ResourceHelper.getInstance( DefaultHttpErrorHandler.class ).getResourceStringOrDefault(
            "DefaultErrorMessage",
            "<html><head><title>Error</title></head>\n"
                + "<body><h1>An error occurred.</h1>\n"
                + "<p>{0}</p>\n"
                + "<p>Please contact the system administrator of this site if you feel this error should not have occurred.</p>\n"
                + "</body></html>" );
}
