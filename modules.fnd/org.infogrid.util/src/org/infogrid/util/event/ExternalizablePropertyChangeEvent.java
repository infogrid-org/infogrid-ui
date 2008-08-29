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
 * Extends {@link ExternalizableEvent} for the specific purpose of communicating
 * the change in a property. This interface relates to <code>ExternalizableEvent</code>
 * in the same manner <code>java.beans.PropertyChangeEvent</code> relates to
 * <code>java.util.EventObject</code>.
 * 
 * 
 * @param S the type of the event source
 * @param SID the type of the identifier of the event source
 * @param P the type of the property
 * @param PID type of the identifier of the property
 * @param V the type of the value
 * @param VID the type of the identifier of the value
 */
public interface ExternalizablePropertyChangeEvent<S,SID,P,PID,V,VID>
        extends
            ExternalizableEvent<S,SID,V,VID>
{
    /**
     * Obtain the property of the event.
     *
     * @return the property of the event
     * @throws PropertyUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    public P getProperty()
        throws
            PropertyUnresolvedException;
    
    /**
     * Obtain the property identifier of the event.
     *
     * @return the property identifier
     */
    public PID getPropertyIdentifier();

    /**
     * Obtain the old value of the property prior to the event.
     *
     * @return the old value of the property
     * @throws ValueUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    public V getOldValue()
        throws
            ValueUnresolvedException;
    
    /**
     * Obtain the old-value identifier of the event.
     *
     * @return the old-value identifier
     */
    public VID getOldValueIdentifier();

    /**
     * Obtain the new value of the property after the event.
     *
     * @return the new value of the property
     * @throws ValueUnresolvedException thrown if this ExternalizableEvent was serialized/deserialized,
     *         and re-resolving the value failed
     */
    public V getNewValue()
        throws
            ValueUnresolvedException;

    /**
     * Obtain the new-value identifier of the event.
     *
     * @return the new-value identifier
     */
    public VID getNewValueIdentifier();
}