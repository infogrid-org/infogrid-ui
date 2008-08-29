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

package org.infogrid.util.event;

/**
 * Thrown if a member of an ExternalizableEvent object is accessed that has not been resolved.
 */
public abstract class UnresolvedException
        extends
            RuntimeException
{
    /** 
     * Constructor, for subclasses only.
     *
     * @param event the ExternalizableEvent that was not resolved
     * @param cause the cause of the Exception, if any
     */
    protected UnresolvedException(
            ExternalizableEvent event,
            Throwable           cause )
    {
        super( cause );

        theEvent = event;
    }
    
    /**
     * Obtain the ExternalizableEvent that was not resolved.
     * 
     * @return the ExternalizableEvent that was not resolved
     */
    public ExternalizableEvent getEvent()
    {
        return theEvent;
    }

    /**
     * The ExternalizableEvent that was not resolved.
     */
    protected ExternalizableEvent theEvent;
}
