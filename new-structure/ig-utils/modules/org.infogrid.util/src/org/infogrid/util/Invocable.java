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
 * Similar to Java's <code>Runnable</code>, this interface also supports a parameter
 * to the run method.
 * 
 * @param K the type of the invocation argument
 * @param V the type of the return value
 */
public interface Invocable<K,V>
{
    /**
     * The invocation method.
     *
     * @param arg the argument
     * @return the return value
     */
    public V invoke(
            K arg );
}
