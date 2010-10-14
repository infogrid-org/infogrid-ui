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

package org.infogrid.jee.security;

import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.http.SaneRequest;

/**
 * Thrown if an unsafe POST was attempted in a place where that was not acceptable.
 */
public class UnsafePostException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param request the incoming RESTful request
     */
    public UnsafePostException(
            SaneRequest request )
    {
        theRequest = request;
    }
    
    /**
     * Obtain the unsafe POST request.
     * 
     * @return the request
     */
    public SaneRequest getRequest()
    {
        return theRequest;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theRequest, theRequest.getAbsoluteFullUri() };
    }
    
    /**
     * The unsafe request.
     */
    protected SaneRequest theRequest;
}
