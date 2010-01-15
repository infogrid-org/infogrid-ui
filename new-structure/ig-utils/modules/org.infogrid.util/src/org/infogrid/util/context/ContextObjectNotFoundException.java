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

package org.infogrid.util.context;

import org.infogrid.util.AbstractLocalizedRuntimeException;

/**
 * Thrown if a Context could not find an object that it was asked for.
 */
public class ContextObjectNotFoundException
        extends
            AbstractLocalizedRuntimeException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param classOfContextObject the Class that was specified to find the context object
     */
    public ContextObjectNotFoundException(
            Class classOfContextObject )
    {
        super( ContextObjectNotFoundException.class.getName() + ": " + classOfContextObject );
        
        theClassOfContextObject = classOfContextObject;
    }
    
    /**
     * Obtain the class that was specified to find the context object
     *
     * @return the Class.
     */
    public Class getClassOfContextObject()
    {
        return theClassOfContextObject;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        return new Object[] { getClassOfContextObject() };
    }

    /**
     * The class that was specified to find the context object.
     */
    protected Class theClassOfContextObject;
}
