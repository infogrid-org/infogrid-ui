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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee;

import javax.servlet.ServletException;

/**
 * Subclasses ServletException to also carry a desired HTTP response code.
 */
public class ServletExceptionWithHttpStatusCode
        extends
            ServletException
        implements
            CarriesHttpStatusCodeException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param status the desired HTTP status code, or -1 if none
     */
    public ServletExceptionWithHttpStatusCode(
            int status )
    {
        theDesiredHttpStatusCode = status;
    }

    /**
     * Constructor.
     * 
     * @param msg message
     * @param status the desired HTTP status code
     */
    public ServletExceptionWithHttpStatusCode(
            String    msg,
            int       status )
    {
        super( msg );
        
        theDesiredHttpStatusCode = status;
    }

    /**
     * Constructor.
     * 
     * @param cause the cause
     * @param status the desired HTTP status code
     */
    public ServletExceptionWithHttpStatusCode(
            Throwable cause,
            int       status )
    {
        super( cause );
        
        theDesiredHttpStatusCode = status;
    }

    /**
     * Constructor.
     * 
     * @param msg message
     * @param cause the cause
     * @param status the desired HTTP status code
     */
    public ServletExceptionWithHttpStatusCode(
            String    msg,
            Throwable cause,
            int       status )
    {
        super( msg, cause );
        
        theDesiredHttpStatusCode = status;
    }

    /**
     * Obtain the HTTP status code desired by this exception.
     * 
     * @return the desired HTTP status code
     */
    public int getDesiredHttpStatusCode()
    {
        return theDesiredHttpStatusCode;
    }
    
    /**
     * The HTTP status code desired by this exception.
     */
    protected int theDesiredHttpStatusCode;
}
