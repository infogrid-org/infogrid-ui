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
 * Indicates that a value carried by an ExternalizableEvent was not resolved.
 * The identifier of the unresolved value is carried as an Object, as Java does
 * not like generics in Exceptions.
 */
public class ValueUnresolvedException
        extends
            UnresolvedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param event the ExternalizableEvent that was not resolved
     * @param unresolvedValueIdentifier the identifier of the unresolved value
     */
    public ValueUnresolvedException(
            ExternalizableEvent event,
            Object              unresolvedValueIdentifier )
    {
        super( event, null );
        
        theUnresolvedValueIdentifier = unresolvedValueIdentifier;
    }

    /**
     * Constructor.
     *
     * @param event the ExternalizableEvent that was not resolved
     * @param unresolvedValueIdentifier the identifier of the unresolved value
     * @param cause the cause of the Exception, if any
     */
    public ValueUnresolvedException(
            ExternalizableEvent event,
            Object              unresolvedValueIdentifier,
            Throwable           cause )
    {
        super( event, cause );

        theUnresolvedValueIdentifier = unresolvedValueIdentifier;
    }
    
    /**
     * Obtain the identifier of the unresolved value.
     * 
     * @return the identifier
     */
    public Object getUnresolvedValueIdentifier()
    {
        return theUnresolvedValueIdentifier;
    }

    /**
     * The Value identifier that could not be resolved.
     */
    protected Object theUnresolvedValueIdentifier;
}

