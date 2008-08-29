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
 * This interface is implemented by those Exceptions that, while obviously
 * indicating that something went wrong, still want to return at least
 * a partial result.
 * 
 * @param T the type of partial result
 */
public interface PartialResultException<T>
{
    /**
     * Determine whether a partial result is available.
     *
     * @return true if a partial result is available
     */
    public boolean isPartialResultAvailable();

    /**
     * Obtain the partial result, if any.
     * The actual return type is the return type of the method that threw
     * this Exception.
     *
     * @return the partial result
     */
    public T getBestEffortResult();
}
