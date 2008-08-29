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

package org.infogrid.lid.yadis;

import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.lid.LidService;
import org.infogrid.lid.SpecialContentAbortProcessingException;

/**
 * Emits the LID Profiles.
 */
public class EmitYadisAbortProcessingException
        extends
           SpecialContentAbortProcessingException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param source the LidService that threw this exception
     * @param lidRequest the incoming request
     * @param lidResponse the outgoing response
     * @param yadisContent the content of the response
     */
    public EmitYadisAbortProcessingException(
            LidService         source,
            SaneServletRequest lidRequest,
            StructuredResponse lidResponse,
            String             yadisContent )
    {
        super( source, lidRequest, lidResponse, stringToBytes( yadisContent ), "application/xrds+xml" );
    }
}

    