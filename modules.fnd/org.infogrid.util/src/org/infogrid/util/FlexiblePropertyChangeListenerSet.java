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

import java.beans.*;

/**
 * This is a {@link FlexibleListenerSet} for <code>java.util.PropertyChangeEvent</code>s.
 */
public class FlexiblePropertyChangeListenerSet
    extends
        FlexibleListenerSet<PropertyChangeListener,PropertyChangeEvent,Object>
{
    /**
     * Constructor.
     */
    public FlexiblePropertyChangeListenerSet()
    {
        super();
    }

    /**
     * Constructor with an initial size.
     *
     * @param size the initial size
     */
    public FlexiblePropertyChangeListenerSet(
            int size )
    {
        super( size );
    }

    /**
     * Fire a property change event -- convenience method.
     *
     * @param source the sending object
     * @param propertyName name of the property that changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    public void firePropertyChange(
            Object source,
            String propertyName,
            Object oldValue,
            Object newValue )
    {
        fireEvent( new PropertyChangeEvent( source, propertyName, oldValue, newValue ));
    }

    /**
     * Fire one PropertyChangeEvent.
     *
     * @param listener the listener to which we send the event
     * @param event the event to send
     * @param parameter a parameter (here: ignored)
     */
    protected void fireEventToListener(
            PropertyChangeListener listener,
            PropertyChangeEvent    event,
            Object                 parameter )
    {
        listener.propertyChange( event );
    }
}
