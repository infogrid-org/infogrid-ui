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

package org.infogrid.util.logging;

/**
 * A factory for ToStringDumpers.
 */
public class ToStringDumperFactory
    implements
        BufferingDumperFactory
{
    /**
     * Factory method.
     *
     * @return the created ToStringDumperFactory
     */
    public static ToStringDumperFactory create()
    {
        return new ToStringDumperFactory( ToStringDumper.DEFAULT_MAXLEVEL );
    }

    /**
     * Factory method.
     *
     * @param maxLevel the number of object levels to dump
     * @return the created ToStringDumperFactory
     */
    public static ToStringDumperFactory create(
            int maxLevel )
    {
        return new ToStringDumperFactory( maxLevel );
    }

    /**
     * Constructor.
     *
     * @param maxLevel the number of object levels to dump
     */
    protected ToStringDumperFactory(
            int maxLevel )
    {
        theMaxLevel = maxLevel;
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @return the created object
     */
    public ToStringDumper obtainFor(
            Object key )
    {
        return ToStringDumper.create( theMaxLevel );
    }

    /**
     * The maximum number of object levels to dump.
     */
    protected int theMaxLevel;
}
