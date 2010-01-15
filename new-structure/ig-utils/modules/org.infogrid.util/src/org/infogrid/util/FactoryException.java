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

package org.infogrid.util;

/**
 * Exception thrown if a Factory failed to create an object.
 */
public class FactoryException
        extends
            Exception
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param sender the Factory that threw this exception
     * @param cause the actual underlying cause
     */
    public FactoryException(
            Factory<?,?,?> sender,
            Throwable      cause )
    {
        super( null, cause ); // don't automatically create message

        theSender = sender;
    }

    /**
     * Constructor.
     *
     * @param sender the Factory that threw this exception
     * @param message the message
     */
    public FactoryException(
            Factory<?,?,?> sender,
            String         message )
    {
        super( message );

        theSender = sender;
    }

    /**
     * Constructor.
     *
     * @param sender the Factory that threw this exception
     * @param message the message
     * @param cause the actual underlying cause
     */
    public FactoryException(
            Factory<?,?,?> sender,
            String         message,
            Throwable      cause )
    {
        super( message, cause );

        theSender = sender;
    }

    /**
     * Constructor for subclasses only.
     * 
     * @param sender the Factory that threw this exception
     */
    protected FactoryException(
            Factory<?,?,?> sender )
    {
        super();

        theSender = sender;
    }
    
    /**
     * Obtain the Factory that threw this exception.
     * 
     * @return the Factory
     */
    public Factory<?,?,?> getSender()
    {
        return theSender;
    }

    /**
     * The Factory that throw this exception.
     */
    protected Factory<?,?,?> theSender;
}
