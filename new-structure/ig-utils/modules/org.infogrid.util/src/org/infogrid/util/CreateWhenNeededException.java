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
 * Wrapper Exception thrown by CreateWhenNeeded.
 */
public class CreateWhenNeededException
        extends
            RuntimeException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the CreateWhenNeeded that threw this CreateWhenNeededException
     * @param cause the cause for this CreateWhenNeededException
     */
    public CreateWhenNeededException(
            CreateWhenNeeded<?> source,
            Throwable           cause )
    {
        super( cause );

        theSource = source;
    }

    /**
     * Obtain the CreateWhenNeeded that threw this CreateWhenNeededException.
     * 
     * @return the CreateWhenNeeded
     */
    public CreateWhenNeeded<?> getSource()
    {
        return theSource;
    }

    /**
     * The CreateWhenNeeded object that threw this CreateWhenNeededException.
     */
    protected CreateWhenNeeded<?> theSource;
}
