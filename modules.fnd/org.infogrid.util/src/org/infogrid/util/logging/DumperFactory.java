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

import org.infogrid.util.Factory;

/**
 * A factory for Dumpers.
 *
 * @param <V> the created Dumper type
 */
public interface DumperFactory<V extends Dumper>
    extends
       Factory<Object,V,Void>
{

    /**
     * The default DumperFactory.
     */
    public static final DumperFactory<? extends Dumper> DEFAULT_FACTORY = ToStringDumperFactory.create();
}
