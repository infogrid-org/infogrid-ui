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
 * A {@link NameServer} whose content may be changed.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public interface WritableNameServer<K,V>
        extends
            NameServer<K,V>
{
    /**
     * Add a new key and a new value.
     *
     * @param key the key
     * @param value the value for the key
     * @return the old value at this key, if any
     */
    public abstract V put(
            K key,
            V value );

    /**
     * Remove a key-value pair that was previously created. This does not affect
     * values that are currently still being constructed. The semantics of
     * &quot;remove&quot; for a SmartFactory imply &quot;deletion&quot; of the
     * object as well. Correspondingly, if the removed Object supports the
     * {@link org.infogrid.util.LiveDeadObject LiveDeadObject} object, this
     * implementation will invoke the <code>die()</code> method there.
     *
     * @param key the key of the key-value pair to be removed
     * @return the value of the key-value pair to be removed, if found
     */
    public abstract V remove(
            K key );

    /**
     * Remove a key-value pair that was previously created. This does not affect
     * values that are currently still being constructed. The semantics of
     * &quot;remove&quot; for a SmartFactory imply &quot;deletion&quot; of the
     * object as well. The provided cleanupCode can be used to implement those
     * semantics, e.g. in order to invoke the die() method.
     *
     * @param key the key of the key-value pair to be removed
     * @param cleanupCode the cleanup code to run, if any
     * @return the value of the key-value pair to be removed, if found
     */
    public abstract V remove(
            K                 key,
            Invocable<V,Void> cleanupCode );
}
