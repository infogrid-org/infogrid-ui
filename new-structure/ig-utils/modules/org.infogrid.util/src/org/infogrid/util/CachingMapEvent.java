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

import java.util.EventObject;

/**
 * Event sent by the SwappingHashMap. Specific subclasses are defined as inner classes.
 * 
 * @param <K> type for the key
 * @param <V> type for the value
 */
public abstract class CachingMapEvent<K,V>
        extends
            EventObject
{
    /**
     * Constructor.
     *
     * @param map the CachingMap that sent the event
     * @param key the key for the affected value
     */
    protected CachingMapEvent(
            CachingMap<K,V> map,
            K               key )
    {
        super( map );
        
        theKey = key;
    }
    
    /**
     * Obtain the sending SmartFactory.
     *
     * @return the sending SmartFactory
     */
    @SuppressWarnings(value={"unchecked"})
    @Override
    public CachingMap<K,V> getSource()
    {
        return (CachingMap<K,V>) super.getSource();
    }

    /**
     * Obtain the key of the value that was affected.
     *
     * @return the key
     */
    public K getKey()
    {
        return theKey;
    }
    
    /**
     * The key.
     */
    protected K theKey;
    
    /**
     * This event indicates that a value was added to the values held by the CachingMap.
     * 
     * @param <K> type for the key
     * @param <V> type for the value
     */
    public static class Added<K,V>
            extends
                CachingMapEvent<K,V>
    {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param map the CachingMap that sent the event
         * @param key the key for the affected value
         * @param value the affected value
         */
        public Added(
                CachingMap<K,V> map,
                K               key,
                V               value )
        {
            super( map, key );
            
            theValue = value;
        }

        /**
         * Obtain the value that was affected.
         *
         * @return the value
         */
        public V getValue()
        {
            return theValue;
        }

        /**
         * The value.
         */
        protected V theValue;
    }
    
    /**
     * This event indicates that a value was removed from the values held by the SmartFactory.
     * 
     * @param <K> type for the key
     * @param <V> type for the value
     */
    public static class Removed<K,V>
            extends
                CachingMapEvent<K,V>
    {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param map the CachingMap that sent the event
         * @param key the key for the affected value
         */
        public Removed(
                CachingMap<K,V> map,
                K               key )
        {
            super( map, key );
        }        
    }
    
    /**
     * This event indicates that a value was removed from the cache, but can still be retrieved
     * by swapping it back in.
     * 
     * @param <K> type for the key
     * @param <V> type for the value
     */
    public static class Expired<K,V>
            extends
                CachingMapEvent<K,V>
    {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor.
         *
         * @param map the CachingMap that sent the event
         * @param key the key for the affected value
         */
        public Expired(
                CachingMap<K,V> map,
                K               key )
        {
            super( map, key );
        }        
    }
}
