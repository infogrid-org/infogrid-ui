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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.util;

/**
 * Marks Tags that iterate, so the IfIterationHasNextTag can work.
 */
public interface InfoGridIterationTag
{
    /**
     * Determine whether this iteration tag has a next element to be returned
     * in the iteration.
     * 
     * @return true if there is a next element
     */
    public boolean hasNext();
}