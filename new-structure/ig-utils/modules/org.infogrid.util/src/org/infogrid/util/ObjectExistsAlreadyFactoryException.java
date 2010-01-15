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

package org.infogrid.util;

/**
 * Thrown by a SmartFactory if a to-be-newly-created object exists already.
 */
public class ObjectExistsAlreadyFactoryException
        extends
            FactoryException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param sender the SmartFactory that threw this exception
     * @param existing the object that existed already with this key
     */
    public ObjectExistsAlreadyFactoryException(
            SmartFactory<?,?,?> sender,
            Object              existing )
    {
        super( sender );
        
        theExisting = existing;
    }

    /**
     * Constructor.
     *
     * @param sender the SmartFactory that threw this exception
     * @param existing the object that existed already with this key
     * @param cause the actual underlying cause
     */
    public ObjectExistsAlreadyFactoryException(
            SmartFactory<?,?,?> sender,
            Object         existing,
            Throwable      cause )
    {
        super( sender, cause );
        
        theExisting = existing;
    }

    /**
     * Constructor.
     *
     * @param sender the SmartFactory that threw this exception
     * @param existing the object that existed already with this key
     * @param message the message
     */
    public ObjectExistsAlreadyFactoryException(
            SmartFactory<?,?,?> sender,
            Object         existing,
            String         message )
    {
        super( sender, message );
        
        theExisting = existing;
    }

    /**
     * Constructor.
     *
     * @param sender the SmartFactory that threw this exception
     * @param existing the object that existed already with this key
     * @param message the message
     * @param cause the actual underlying cause
     */
    public ObjectExistsAlreadyFactoryException(
            SmartFactory<?,?,?> sender,
            Object         existing,
            String         message,
            Throwable      cause )
    {
        super( sender, message, cause );
        
        theExisting = existing;
    }
    
    /**
     * Obtain the SmartFactory that threw this exception.
     * 
     * @return the SmartFactory
     */
    @Override
    public SmartFactory<?,?,?> getSender()
    {
        return (SmartFactory<?,?,?>) super.getSender();
    }

    /**
     * Obtain the Object that existed already.
     * 
     * @return the Object
     */
    public Object getExisting()
    {
        return theExisting;
    }

    /**
     * The Object that existed already.
     */
    protected Object theExisting;
}
