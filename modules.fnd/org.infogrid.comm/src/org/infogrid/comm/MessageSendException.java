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

import org.infogrid.util.AbstractLocalizedException;

import java.util.List;

/**
 * Thrown if a Message could not be sent. Unfortunately, Java 5 does not support generics
 * for Exceptions, so this has to use Object as the type for messages.
 */
public class MessageSendException
        extends
            AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param message the message that could not be sent
     */
    public MessageSendException(
            List<? extends Object> message )
    {
        theMessage = message;
    }

    /**
     * Constructor.
     *
     * @param message the message
     * @param cause the cause for the Exception
     */
    public MessageSendException(
            List<? extends Object> message,
            Throwable              cause )
    {
        super( cause );
        
        theMessage = message;
    }
    
    /**
     * Constructor.
     *
     * @param message the message
     * @param errorMessage the error message
     */
    public MessageSendException(
            List<? extends Object> message,
            String                 errorMessage )
    {
        super( errorMessage );
        
        theMessage = message;
    }
    
    /**
     * Constructor.
     *
     * @param message the message
     * @param errorMessage the error message
     * @param cause the cause for the Exception
     */
    public MessageSendException(
            List<? extends Object> message,
            String                 errorMessage,
            Throwable              cause )
    {
        super( errorMessage, cause );
        
        theMessage = message;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */    
    public Object [] getLocalizationParameters()
    {
        return new Object[] { getMessage(), theMessage };
    }

    /**
     * The message that could not be sent.
     */
    protected List<? extends Object> theMessage;
}
