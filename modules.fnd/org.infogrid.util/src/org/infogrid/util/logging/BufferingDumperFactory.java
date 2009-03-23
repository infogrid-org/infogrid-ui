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

import org.infogrid.util.FactoryException;

/**
 * A factory for BufferingDumpers.
 */
public interface BufferingDumperFactory
        extends
            DumperFactory
{
    /**
     * Factory method. This is equivalent to specifying a null argument.
     *
     * @param key the key information required for object creation, if any
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public abstract BufferingDumper obtainFor(
            Object key )
        throws
            FactoryException;
}

