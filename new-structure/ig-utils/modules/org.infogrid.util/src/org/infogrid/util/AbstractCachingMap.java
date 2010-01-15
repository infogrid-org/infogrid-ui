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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Factors out common functionality for {@link CachingMap} implementations.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public abstract class AbstractCachingMap<K,V>
        implements
            CachingMap<K,V>
{
    /**
     * Obtain the keys for an existing value. This is the opposite operation
     * of {@link #get}. Depending on the implementation of this interface,
     * this operation may take a long time. This is the (slow) default implementation.
     * 
     * @param value the value whose keys need to be found
     * @return an Iterator over the keys
     */
    public Iterator<K> reverseGet(
            V value )
    {
        ArrayList<K> ret = new ArrayList<K>();

        for( K key : keySet() ) {
            V found = get( key );
            if( value == found ) {
                ret.add( key );
            }
        }
        return ret.iterator();
    }

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
    public V remove(
            K                 key,
            Invocable<V,Void> cleanupCode )
    {
        V ret = remove( key );
        if( cleanupCode != null && ret != null ) {
            cleanupCode.invoke( ret );
        }
        return ret;
    }

    /**
      * Add a listener.
      * This listener is added directly to the listener list, which prevents the
      * listener from being garbage-collected before this Object is being garbage-collected.
      *
      * @param newListener the to-be-added listener
      * @see #addSoftCachingMapListener
      * @see #addWeakCachingMapListener
      * @see #removeCachingMapListener
      */
    public void addDirectCachingMapListener(
            CachingMapListener newListener )
    {
        theListeners.addDirect( newListener );
    }

    /**
      * Add a listener.
      * This listener is added to the listener list using a <code>java.lang.ref.SoftReference</code>,
      * which allows the listener to be garbage-collected before this Object is being garbage-collected
      * according to the semantics of Java references.
      *
      * @param newListener the to-be-added listener
      * @see #addDirectCachingMapListener
      * @see #addWeakCachingMapListener
      * @see #removeCachingMapListener
      */
    public void addSoftCachingMapListener(
            CachingMapListener newListener )
    {
        theListeners.addSoft( newListener );
    }

    /**
      * Add a listener.
      * This listener is added to the listener list using a <code>java.lang.ref.WeakReference</code>,
      * which allows the listener to be garbage-collected before this Object is being garbage-collected
      * according to the semantics of Java references.
      *
      * @param newListener the to-be-added listener
      * @see #addDirectCachingMapListener
      * @see #addSoftCachingMapListener
      * @see #removeCachingMapListener
      */
    public void addWeakCachingMapListener(
            CachingMapListener newListener )
    {
        theListeners.addWeak( newListener );        
    }

    /**
      * Remove a listener.
      * This method is the same regardless how the listener was subscribed to events.
      * 
      * @param oldListener the to-be-removed listener
      * @see #addDirectCachingMapListener
      * @see #addSoftCachingMapListener
      * @see #addWeakCachingMapListener
      */
    public void removeCachingMapListener(
            CachingMapListener oldListener )
    {
        theListeners.remove( oldListener );
    }


    /**
     * Fire a "an element has been added" event.
     *
     * @param key the key of the element that has been added
     * @param value the value that has been added
     */
    protected void fireValueAdded(
            K key,
            V value )
    {
        theListeners.fireEvent( new CachingMapEvent.Added<K,V>( this, key, value ), 0 );
    }

    /**
     * Fire a "an element has been removed" event.
     * This does not carry the removed value because it might be gone by this time.
     *
     * @param key the key of the element that has been removed
     */
    protected void fireValueRemoved(
            K key )
    {
        theListeners.fireEvent( new CachingMapEvent.Removed<K,V>( this, key ), 1 );
    }

    /**
     * Fire a "an element has been removed from the cache because it expired" event.
     * Compare: because it was removed by the client.
     *
     * @param key the key whose value has been removed
     */
    protected final void fireValueCleanedUp(
            K key )
    {
        theListeners.fireEvent( new CachingMapEvent.Expired<K,V>( this, key ), 2 );
    }

    /**
      * The listeners (if any).
      */
    private FlexibleListenerSet<CachingMapListener, CachingMapEvent, Integer> theListeners
            = new FlexibleListenerSet<CachingMapListener,CachingMapEvent,Integer>() {
                    protected void fireEventToListener(
                            CachingMapListener l,
                            CachingMapEvent    e,
                            Integer            p )
                    {
                        switch( p.intValue() ) {
                            case 0:
                                l.mapElementAdded( (CachingMapEvent.Added) e );
                                break;

                            case 1:
                                l.mapElementRemoved( (CachingMapEvent.Removed) e );
                                break;

                            case 2:
                                l.mapElementExpired( (CachingMapEvent.Expired) e );
                                break;
                        }
                    }
    };    
}
