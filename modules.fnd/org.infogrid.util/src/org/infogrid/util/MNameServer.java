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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.infogrid.util.logging.Log;

/**
 * A very simple, in-memory only implementation of {@link NameServer}. This NameServer
 * keeps mappings locally, and if not found, it will delegate to (optional) delegate
 * NameServers.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 */
public class MNameServer<K,V>
        implements
            WritableNameServer<K,V>
{
    private static final Log log = Log.getLogInstance( MNameServer.class ); // our own, private logger
    
    /**
     * Factory method.
     *
     * @return the created MNameServer
     * @param <K> the type of key
     * @param <V> the type of value
     */
    public static <K,V> MNameServer<K,V> create()
    {
        MNameServer<K,V> ret = new MNameServer<K,V>();
        return ret;
    }
    
    /**
     * Factory method, speciifying a delegate.
     *
     * @param newDelegate the delegate
     * @return the created MNameServer
     * @param <K> the type of key
     * @param <V> the type of value
     */
    public static <K,V> MNameServer<K,V> create(
            NameServer<K,? extends V> newDelegate )
    {
        MNameServer<K,V> ret = new MNameServer<K,V>();
        ret.addDelegate( newDelegate );
        return ret;
    }
    
    /**
     * Constructor.
     */
    protected MNameServer()
    {
        // no op
    }
    
    /**
     * Obtain an already existing value for a provided key.
     * 
     * @param key the key for which we want to obtain a value
     * @return the already existing value, or null
     */
    public V get(
            K key )
    {
        V ret = null;
        try {
            synchronized( this ) {
                ret = theLocalMappings.get( key );
                if( ret != null ) {
                    return ret;
                }
                if( theDelegates != null ) {
                    for( NameServer<K,? extends V> current : theDelegates ) {
                        ret = current.get( key );
                        if( ret != null ) {
                            return ret;
                        }
                    }
                }
                return ret;
            }

        } finally {
            if( log.isDebugEnabled() ) {
                log.debug( this + ".get( " + key + " ) -> " + ret );
            }
        }
    }

    /**
     * Obtain the keys for an existing value. This is the opposite operation
     * of {@link #get}. Depending on the implementation of this interface,
     * this operation may take a long time.
     * 
     * @param value the value whose keys need to be found
     * @return an Iterator over the keys
     */
    public Iterator<K> reverseGet(
            V value )
    {
        // Default implementation
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "reverseGet", value );
        }
        ArrayList<K> ret = new ArrayList<K>();

        synchronized( theLocalMappings ) {
            for( K key : theLocalMappings.keySet() ) {
                V found = theLocalMappings.get( key );
                if( value == found ) {
                    ret.add( key );
                }
            }
        }
        if( theDelegates != null ) {
            for( NameServer<K,? extends V> current : theDelegates ) {
                Iterator<K> iter = current.keySet().iterator();
                while( iter.hasNext() ) {
                    K      key   = iter.next();
                    Object found = current.get( key );
                    if( value == found ) {
                        ret.add( key );
                    }
                }
            }
        }
        return ret.iterator();
    }

    /**
     * Add a new key and a new value without going through the obtain method.
     *
     * @param key the key
     * @param value the value for the key
     * @return the old value at this key, if any
     */
    public V put(
            K key,
            V value )
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "put", key, value );
        }
        synchronized( this ) {
            return theLocalMappings.put( key, value );
        }
    }

    /**
     * Obtain a set of the currently known keys. This follows the pattern in
     * java.util.HashMap.
     *
     * @return a set of the currently known keys
     */
    public synchronized Set<K> keySet()
    {
        HashSet<K> ret = new HashSet<K>();
        for( K current : theLocalMappings.keySet()) {
            ret.add( current );
        }
        for( NameServer<K, ? extends V> delegate : theDelegates ) {
            for( K current : delegate.keySet() ) {
                ret.add( current );
            }
        }
        return ret;
    }

    /**
     * Obtain a Collection of the currently known values. This follows the pattern in
     * java.util.HashMap.
     *
     * @return a set of the currently known values
     */
    public synchronized Collection<V> values()
    {
        ArrayList<V> ret = new ArrayList<V>();
        for( K current : theLocalMappings.keySet()) {
            V value = theLocalMappings.get( current );
            ret.add( value );
        }
        for( NameServer<K, ? extends V> delegate : theDelegates ) {
            for( K current : delegate.keySet() ) {
                V value = delegate.get( current );
                ret.add( value );
            }
        }
        return ret;
    }
    
    /**
     * Remove a key-value pair that was previously created. This does not affect
     * values that are currently still being constructed. 
     *
     * @param key the key of the key-value pair to be removed
     * @return the value of the key-value pair to be removed, if found
     */
    public V remove(
            K key )
    {
        return remove( key, null );
    }

    /**
     * Remove a key-value pair that was previously created. This does not affect
     * values that are currently still being constructed.
     *
     * This performs the provided code fragment before this method completes.
     * That way a to-be-deleted object can finish up cleanly before a new
     * factory request comes in for the same object.
     *
     * @param key the key of the key-value pair to be removed
     * @param cleanupCode the cleanup code to run, if any
     * @return the value of the key-value pair to be removed, if found
     */
    public synchronized V remove(
            K                 key,
            Invocable<V,Void> cleanupCode )
    {
        V ret = theLocalMappings.remove( key );
        
        if( cleanupCode != null ) {
            cleanupCode.invoke( ret );
        }
        
        return ret;
    }

    /**
     * Determine whether the number of key-value pairs in this SmartFactory is zero.
     *
     * @return true if there are no key-value paris in this SmartFactory
     */
    public synchronized boolean isEmpty()
    {
        if( !theLocalMappings.isEmpty() ) {
            return false;
        }
        if( theDelegates != null ) {
            for( NameServer<K,? extends V> current : theDelegates ) {
                if( !current.isEmpty() ) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * The number of known key-value pairs in this SmartFactory at this time.
     *
     * @return the number of known key-value pairs
     */
    public synchronized int size()
    {
        int ret = theLocalMappings.size();
        
        if( theDelegates != null ) {
            for( NameServer<K,? extends V> current : theDelegates ) {
                ret += current.size();
            }
        }
        return ret;
    }

    /**
     * Add a delegate.
     *
     * @param newDelegate the new delegate
     */
    public synchronized void addDelegate(
            NameServer<K,? extends V> newDelegate )
    {
        if( theDelegates == null ) {
            theDelegates = new ArrayList<NameServer<K,? extends V>>();
        }
        theDelegates.add( newDelegate );
    }

    /**
     * Remove a delegate.
     *
     * @param oldDelegate the old delegate
     */
    public synchronized void removedDelegate(
            NameServer<K,? extends V> oldDelegate )
    {
        theDelegates.remove( oldDelegate );
    }

    /**
     * The set of local mappings.
     */
    protected final HashMap<K,V> theLocalMappings = new HashMap<K,V>();
    
    /**
     * The list of delegate NameServers, in sequence.
     */
    protected ArrayList<NameServer<K,? extends V>> theDelegates = null;
}
