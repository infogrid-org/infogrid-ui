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

package org.infogrid.util;

import java.beans.PropertyChangeEvent;

/**
 * This subclasses <code>java.beans.PropertyChangeEvent</code> to make it easier
 * to process PropertyChangeEvents that deal with Array values for the changed properties.
 *
 * @param <T> the type of the Property
 */
public abstract class ArrayPropertyChangeEvent<T>
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
     * @param componentType the actual underlying type
     * @return the created ArrayProertyChangeEvent
     * @param <T> the type of the Property
     */
    public static <T> ArrayPropertyChangeEvent<T> createAdded(
            Object        source,
            String        propertyName,
            T []          oldValue,
            T             addition,
            Class<T>      componentType )
    {
        return new Added<T>( source, propertyName, oldValue, addition, null, componentType );
    }

    /**
     * Factory method.
     *
     * @param source The bean that fired the event.
     * @param propertyName The programmatic name of the property that was changed.
     * @param addition the T that was added to the Set
     * @param newValue the new Set after addition
     * @param componentType the actual underlying type
     * @return the created ArrayProertyChangeEvent
     * @param <T> the type of the Property
     */
    public static <T> ArrayPropertyChangeEvent<T> createAdded(
            Object        source,
            String        propertyName,
            T             addition,
            T []          newValue,
            Class<T>      componentType )
    {
        return new Added<T>( source, propertyName, null, addition, newValue, componentType );
    }

    /**
     * Factory method.
     *
     * @param source The bean that fired the event.
     * @param propertyName The programmatic name of the property that was changed.
     * @param oldValue the old Set prior to removal
     * @param removal the T that was removed from the Set
     * @param componentType the actual underlying type
     * @return the created ArrayProertyChangeEvent
     * @param <T> the type of the Property
     */
    public static <T> ArrayPropertyChangeEvent<T> createRemoved(
            Object        source,
            String        propertyName,
            T []          oldValue,
            T             removal,
            Class<T>      componentType )
    {
        return new Removed<T>( source, propertyName, oldValue, removal, null, componentType );
    }

    /**
     * Factory method.
     *
     * @param source The bean that fired the event.
     * @param propertyName The programmatic name of the property that was changed.
     * @param removal the T that was removed from the Set
     * @param newValue the new Set after removal
     * @param componentType the actual underlying type
     * @return the created ArrayProertyChangeEvent
     * @param <T> the type of the Property
     */
    public static <T> ArrayPropertyChangeEvent<T> createRemoved(
            Object        source,
            String        propertyName,
            T             removal,
            T []          newValue,
            Class<T>      componentType )
    {
        return new Removed<T>( source, propertyName, null, removal, newValue, componentType );
    }

    /**
     * Constructor.
     *
     * @param source  The bean that fired the event.
     * @param propertyName  The programmatic name of the property that was changed.
     * @param oldValue  The old value of the property.
     * @param delta The difference between the old value and the new value
     * @param newValue  The new value of the property.
     * @param componentType the actual underlying type
     */
    protected ArrayPropertyChangeEvent(
            Object        source,
            String        propertyName,
            T []          oldValue,
            T             delta,
            T []          newValue,
            Class<T>      componentType )
    {
        super( source, propertyName, oldValue, newValue );
        
        if( oldValue == null && newValue == null ) {
            throw new IllegalArgumentException( "Old and new values cannot both be null" );
        }
        if( delta == null ) {
            throw new IllegalArgumentException( "delta must not be null" );
        }
        if( componentType == null ) {
            throw new IllegalArgumentException( "componentType must not be null" );
        }
        theOldValue = oldValue;
        theDelta    = delta;
        theNewValue = newValue;
        
        theComponentType = componentType;
    }
    
    /**
     * Obtain the delta object, i.e. the object that has been added or removed.
     * 
     * @return the delta object
     */
    public T getDelta()
    {
        return theDelta;
    }

    /**
     * The old Collection. We'd rather use our superclass's value, but it is marked private!
     */
    protected T [] theOldValue;
    
    /**
     * The delta object.
     */
    protected T theDelta;

    /**
     * The new Collection. We'd rather use our superclass's value, but it is marked private!
     */
    protected T [] theNewValue;

    /**
     * The component type for newly created arrays.
     */
    protected Class<T> theComponentType;
    
    /**
     * Subclass that reflects an addition to the Set.
     *
     * @param <T> the type of the Property
     */
    public static class Added<T>
            extends
                ArrayPropertyChangeEvent<T>
    {
        private static final long serialVersionUID = 1l; // helps with serialization

        /**
         * Constructor.
         *
         * @param source  The bean that fired the event.
         * @param propertyName  The programmatic name of the property that was changed.
         * @param oldValue  The old value of the property.
         * @param addition The difference between the old value and the new value
         * @param newValue  The new value of the property.
         * @param componentType the actual underlying type
         */
        protected Added(
                Object        source,
                String        propertyName,
                T []          oldValue,
                T             addition,
                T []          newValue,
                Class<T>      componentType )
        {
            super( source, propertyName, oldValue, addition, newValue, componentType );
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
                theNewValue = ArrayHelper.append( theOldValue, theDelta, theComponentType );
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
                theOldValue = ArrayHelper.remove( theNewValue, theDelta, false, theComponentType );
            }
            return theOldValue;
        }
    }

    /**
     * Subclass that reflects a removal from the Set.
     *
     * @param <T> the type of the Property
     */
    public static class Removed<T>
            extends
                ArrayPropertyChangeEvent<T>
    {
        private static final long serialVersionUID = 1l; // helps with serialization

        /**
         * Constructor.
         *
         * @param source  The bean that fired the event.
         * @param propertyName  The programmatic name of the property that was changed.
         * @param oldValue  The old value of the property.
         * @param removal The difference between the old value and the new value
         * @param newValue  The new value of the property.
         * @param componentType the actual underlying type
         */
        protected Removed(
                Object        source,
                String        propertyName,
                T []          oldValue,
                T             removal,
                T []          newValue,
                Class<T>      componentType )
        {
            super( source, propertyName, oldValue, removal, newValue, componentType );
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
                theNewValue = ArrayHelper.remove( theOldValue, theDelta, false, theComponentType );
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
                theOldValue = ArrayHelper.append( theNewValue, theDelta, theComponentType );
            }
            return theOldValue;
        }
    }
}
