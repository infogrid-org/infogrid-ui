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

package org.infogrid.lid.openid;

import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.util.http.SaneRequest;

/**
 * Thrown when a OpenID mode "error" has been discovered.
 */
public class OpenIdModeErrorException
    extends
        LidAbortProcessingPipelineException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param request the incoming request containing the error
     */
    public OpenIdModeErrorException(
            SaneRequest request )
    {
        super( null );

        theErrorCode = request.getUrlArgument( ERROR_CODE_PAR );
        if( theErrorCode == null ) {
            theErrorCode = request.getPostedArgument( ERROR_CODE_PAR );
        }
        theErrorMessage = request.getUrlArgument( ERROR_MESSAGE_PAR );
        if( theErrorMessage == null ) {
            theErrorMessage = request.getPostedArgument( ERROR_MESSAGE_PAR );
        }
    }

    /**
     * Constructor.
     *
     * @param errorCode the error code conveyed by the protocol
     * @param errorMessage the error message conveyed by the protocol
     */
    public OpenIdModeErrorException(
            String errorCode,
            String errorMessage )
    {
        super( null );

        theErrorCode    = errorCode;
        theErrorMessage = errorMessage;
    }

    /**
     * Obtain the error code conveyed by the protocol.
     *
     * @return the error code
     */
    public String getErrorCode()
    {
        return theErrorCode;
    }

    /**
     * Obtain the error message conveyed by the protocol.
     *
     * @return the error message
     */
    public String getErrorMessage()
    {
        return theErrorMessage;
    }

    /**
     * The error code conveyed by the protocol.
     */
    protected String theErrorCode;

    /**
     * The error message conveyed by the protocol.
     */
    protected String theErrorMessage;

    /**
     * Keyword for the error code.
     */
    public static final String ERROR_CODE_PAR = "openid.error_code";

    /**
     * Keyword for the error message.
     */
    public static final String ERROR_MESSAGE_PAR = "openid.error";
}
