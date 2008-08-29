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

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;

/**
 * This subclasses <code>java.beans.PropertyChangeEvent</code> to make it easier
 * to process PropertyChangeEvents that deal with Set values for the changed properties.
 * 
 * @param T the type of element in the Collection
 */
public abstract class CollectionPropertyChangeEvent<T>
        extends
            PropertyChangeEvent
{
    /**
     * Factory method.
     *
     * @param source The bean that fired the event.
     * @param propertyName The programmatic name of the property that was changed.
     * @param oldValue the old Set prior to addition
     * @param addition the T that was added to the Set
     * @return the created CollectionPropertyChangeEvent
     */
    public static <T> CollectionPropertyChangeEvent<T> createAdded(
            Object        source,
            String        propertyName,
            Collection<T> oldValue,
            T             addition )
    {
        return new Added<T>( source, propertyName, oldValue, addition, null );
    }

    /**
     * Factory method.
     *
     * @param source The bean that fired the event.
     * @param propertyName The programmatic name of the property that was changed.
     * @param addition the T that was added to the Set
     * @param newValue the new Set after addition
     * @return the created CollectionPropertyChangeEvent
     */
    public static <T> CollectionPropertyChangeEvent<T> createAdded(
            Object        source,
            String        propertyName,
            T             addition,
            Collection<T> newValue )
    {
        return new Added<T>( source, propertyName, null, addition, newValue );
    }

    /**
     * Factory method.
     *
     * @param source The bean that fired the event.
     * @param propertyName The programmatic name of the property that was changed.
     * @param oldValue the old Set prior to removal
     * @param removal the T that was removed from the Set
     * @return the created CollectionPropertyChangeEvent
     */
    public static <T> CollectionPropertyChangeEvent<T> createRemoved(
            Object        source,
            String        propertyName,
            Collection<T> oldValue,
            T             removal )
    {
        return new Removed<T>( source, propertyName, oldValue, removal, null );
    }

    /**
     * Factory method.
     *
     * @param source The bean that fired the event.
     * @param propertyName The programmatic name of the property that was changed.
     * @param removal the T that was removed from the Set
     * @param newValue the new Set after removal
     * @return the created CollectionPropertyChangeEvent
     */
    public static <T> CollectionPropertyChangeEvent<T> createRemoved(
            Object        source,
            String        propertyName,
            T             removal,
            Collection<T> newValue )
    {
        return new Removed<T>( source, propertyName, null, removal, newValue );
    }

    /**
     * Constructor.
     *
     * @param source  The bean that fired the event.
     * @param propertyName  The programmatic name of the property that was changed.
     * @param oldValue  The old value of the property.
     * @param delta     The difference between the old and new value of the property.
     * @param newValue  The new value of the property.
     */
    protected CollectionPropertyChangeEvent(
            Object        source,
            String        propertyName,
            Collection<T> oldValue,
            T             delta,
            Collection<T> newValue )
    {
        super( source, propertyName, oldValue, newValue );
        
        if( oldValue == null && newValue == null ) {
            throw new IllegalArgumentException( "Old and new values cannot both be null" );
        }
        if( delta == null ) {
            throw new IllegalArgumentException( "delta must not be null" );
        }
        theOldValue = oldValue;
        theDelta    = delta;
        theNewValue = newValue;
    }
    
    /**
     * Obtain the delta object, i.e. the object that has been added or removed.
     * 
     * @return the delta
     */
    public T getDelta()
    {
        return theDelta;
    }

    /**
     * The old Collection. We'd rather use our superclass's value, but it is marked private!
     */
    protected Collection<T> theOldValue;
    
    /**
     * The delta object.
     */
    protected T theDelta;

    /**
     * The new Collection. We'd rather use our superclass's value, but it is marked private!
     */
    protected Collection<T> theNewValue;

    /**
     * Subclass that reflects an addition to the Set.
     * 
     * @param T the type of element in the Collection
     */
    public static class Added<T>
            extends
                CollectionPropertyChangeEvent<T>
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source  The bean that fired the event.
         * @param propertyName  The programmatic name of the property that was changed.
         * @param oldValue  The old value of the property.
         * @param addition  The added element of the property.
         * @param newValue  The new value of the property.
         */
        protected Added(
                Object       source,
                String        propertyName,
                Collection<T> oldValue,
                T             addition,
                Collection<T> newValue )
        {
            super( source, propertyName, oldValue, addition, newValue );
        }

        /**
         * Sets the new value for the property, expressed as an Object.
         *
         * @return  The new value for the property, expressed as an Object.
         *		May be null if multiple properties have changed.
         */
        @Override
        public Object getNewValue()
        {
            if( theNewValue == null ) {
                theNewValue = new ArrayList<T>();
                theNewValue.addAll( theOldValue );
                theNewValue.add( theDelta );
            }
            return theNewValue;
        }

        /**
         * Gets the old value for the property, expressed as an Object.
         *
         * @return  The old value for the property, expressed as an Object.
         *		May be null if multiple properties have changed.
         */
        @Override
        public Object getOldValue()
        {
            if( theOldValue == null ) {
                theOldValue = new ArrayList<T>();
                theOldValue.addAll( theNewValue );
                theOldValue.remove( theDelta );
            }
            return theOldValue;
        }
    }

    /**
     * Subclass that reflects a removal from the Set.
     * 
     * @param T the type of element in the Collection
     */
    public static class Removed<T>
            extends
                CollectionPropertyChangeEvent<T>
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source  The bean that fired the event.
         * @param propertyName  The programmatic name of the property that was changed.
         * @param oldValue  The old value of the property.
         * @param removal   The removed element of the property.
         * @param newValue  The new value of the property.
         */
        protected Removed(
                Object        source,
                String        propertyName,
                Collection<T> oldValue,
                T             removal,
                Collection<T> newValue )
        {
            super( source, propertyName, oldValue, removal, newValue );
        }

        /**
         * Sets the new value for the property, expressed as an Object.
         *
         * @return  The new value for the property, expressed as an Object.
         *		May be null if multiple properties have changed.
         */
        @Override
        public Object getNewValue()
        {
            if( theNewValue == null ) {
                theNewValue = new ArrayList<T>();
                theNewValue.addAll( theOldValue );
                theNewValue.remove( theDelta );
            }
            return theNewValue;
        }

        /**
         * Gets the old value for the property, expressed as an Object.
         *
         * @return  The old value for the property, expressed as an Object.
         *		May be null if multiple properties have changed.
         */
        @Override
        public Object getOldValue()
        {
            if( theOldValue == null ) {
                theOldValue = new ArrayList<T>();
                theOldValue.addAll( theNewValue );
                theOldValue.add( theDelta );
            }
            return theOldValue;
        }
    }
}
