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

import java.beans.PropertyChangeEvent;

/**
  * A regular <code>java.beans.PropertyChangeEvent</code> with a time stamp attached
  * that indicates when it was instantiated.
  */
public class TimeStampedPropertyChangeEvent
        extends PropertyChangeEvent
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Constructor.
      *
      * @param src the sender object
      * @param propertyName name of the changed property
      * @param oldValue old value of the property
      * @param newValue new value of the property
      */
    public TimeStampedPropertyChangeEvent(
            Object src,
            String propertyName,
            Object oldValue,
            Object newValue )
    {
        super( src, propertyName, oldValue, newValue );

        timeStamp = System.currentTimeMillis();
    }

    /**
      * Obtain the time stamp.
      *
      * @return the time stamp
      */
    public long getTimeStamp()
    {
        return timeStamp;
    }

    /**
      * The time stamp.
      */
    protected long timeStamp;
}

