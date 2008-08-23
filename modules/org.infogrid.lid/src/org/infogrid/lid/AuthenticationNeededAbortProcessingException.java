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

package org.infogrid.lid;

import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;

/**
 * An AbortProcessingException that causes the user to be challenged.
 */
public class AuthenticationNeededAbortProcessingException
        extends
            AbortProcessingException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the LidService that threw this exception
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param persona the persona being challenged
     * @param target the eventual target
     */
    public AuthenticationNeededAbortProcessingException(
            LidService         source,
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse,
            LidLocalPersona    persona,
            String             target )
    {
        super( source, lidRequest, lidResponse, HttpServletResponse.SC_OK, null, null );
        
        thePersona = persona;
        theTarget  = target;
    }
    
    /**
     * The Persona that is being challenged.
     */
    protected LidLocalPersona thePersona;
    
    /**
     * The eventual target, if any
     */
    protected String theTarget;
}
