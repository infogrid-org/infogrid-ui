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

package org.infogrid.comm;

/**
 * This subclass of MessageSendException indicates that the MessageEndpoint is dead.
 */
public class MessageEndpointIsDeadException
        extends
            MessageSendException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MessageEndpointIsDeadException()
    {
        super( null, "Endpoint is dead" );
    }

    /**
     * Constructor with Exception.
     *
     * @param cause the cause
     */
    public MessageEndpointIsDeadException(
            Throwable cause )
    {
        super( null, "Endpoint is dead", cause );
    }
}
