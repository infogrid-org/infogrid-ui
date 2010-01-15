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

package org.infogrid.comm.pingpong.test;

import org.infogrid.comm.CarriesInvocationId;
import org.infogrid.util.logging.Log;

/**
 * A test message
 */
public class TestMessage
        implements
            CarriesInvocationId
{
    private static final Log log = Log.getLogInstance( TestMessage.class );

    /**
     * Constructor with payload.
     *
     * @param payload the payload
     */
    public TestMessage(
            long payload )
    {
        log.traceConstructor( this, payload );

        thePayload = payload;
    }

    /**
     * Obtain the payload.
     *
     * @return the payload
     */
    public long getPayload()
    {
        return thePayload;
    }

    /**
     * Set the request ID.
     *
     * @param id the request ID
     */
    public void setRequestId(
            long id )
    {
        theRequestId = id;
    }

    /**
     * Obtain the request ID.
     *
     * @return the request ID
     */
    public long getRequestId()
    {
        return theRequestId;
    }

    /**
     * Set the response ID.
     *
     * @param id the response ID
     */
    public void setResponseId(
            long id )
    {
        theResponseId = id;
    }

    /**
     * Obtain the response ID.
     *
     * @return the response ID
     */
    public long getResponseId()
    {
        return theResponseId;
    }

    /**
     * Convert to String, for debugging.
     *
     * @return String form
     */
    @Override
    public String toString()
    {
        return super.toString() + ": requestId: " + theRequestId + ", responseId: " + theResponseId + ", payload: " + thePayload;
    }

    /**
     * The Payload of the message.
     */
    protected long thePayload;

    /**
     * The request ID.
     */
    protected long theRequestId;

    /**
     * The response ID.
     */
    protected long theResponseId;
}
