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
 * A helper class that represents a pair of name and value.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class Pair<K,V>
{
    /**
      * Constructor.
      *
      * @param name the name
      * @param value the value
      */
    public Pair(
            K name,
            V value )
    {
        this.theName  = name;
        this.theValue = value;
    }

    /**
      * Obtain the name component.
      *
      * @return the name component
      */
    public K getName()
    {
        return theName;
    }

    /**
      * Obtain the value component.
      *
      * @return the value component
      */
    public V getValue()
    {
        return theValue;
    }

    /**
      * The value for the name.
      */
    protected K theName;

    /**
      * The value for the value.
      */
    protected V theValue;
}
