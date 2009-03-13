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

import org.infogrid.util.AbstractFactory;

/**
 * A factory for ToStringDumpers.
 *
 * @param <K> the key for the factory
 */
public class ToStringDumperFactory<K>
    extends
       AbstractFactory<Object,ToStringDumper,Void>
    implements
        DumperFactory<ToStringDumper>
{
    /**
     * Factory method.
     *
     * @return the created ToStringDumperFactory
     * @param <K> the key for the factory
     */
    public static <K> ToStringDumperFactory<K> create()
    {
        return new ToStringDumperFactory<K>();
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     */
    public ToStringDumper obtainFor(
            Object key,
            Void   argument )
    {
        return ToStringDumper.create();
    }
}
