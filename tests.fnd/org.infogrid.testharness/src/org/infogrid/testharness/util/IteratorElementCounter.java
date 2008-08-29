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

package org.infogrid.testharness.util;

import org.infogrid.util.logging.Log;

import java.util.Iterator;

/**
 * A utility method that counts the number of elements returned by an Iterator.
 */
public abstract class IteratorElementCounter
{
    private static final Log log = Log.getLogInstance( IteratorElementCounter.class ); // our own, private logger

    /**
     * Count the number of elements returned by an Iterator.
     *
     * @param iter the Iterator
     * @return the number of elements
     */
    public static int countIteratorElements(
            Iterator<? extends Object> iter )
    {
        int ret = 0;
        while( iter.hasNext() ) {
            Object current = iter.next();
            
            if( log.isDebugEnabled() ) {
                log.debug( "Found element " + current );
            }
            ++ret;
        }
        return ret;
    }
}
