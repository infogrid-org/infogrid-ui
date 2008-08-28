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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

/**
  * Provides helper functions that deal with arrays. It cannot be instantiated.
  */
public abstract class ArrayHelper
{
    /**
     * Hidden constructor to keep this abstract.
     */
    private ArrayHelper() {}
    
    /**
     * An "equals" method on Object that also works for null
     * pointers.
     *
     * @param one first Object, or null
     * @param two second Object, or null
     * @return true if the two values are equal
     */
    public static boolean equals(
            Object one,
            Object two )
    {
        if( one == null ) {
            return two == null;
        }
        return one.equals( two );
    }

    /**
     * Essentially an "equals" method on Object arrays which
     * compares for the same content in the same sequence.
     *
     * @param one first argument to compare
     * @param two second argument to compare
     * @return true if the arrays are the same
     */
    public static boolean equals(
            Object [] one,
            Object [] two )
    {
        if( one == two ) {
            return true;
        }
        if( one == null || two == null ) {
            return false;
        }
        if( one.length != two.length ) {
            return false;
        }
        for( int i=0 ; i<one.length ; ++i ) {
            if( one[i] == null ) {
                if( two[i] != null ) {
                    return false;
                }
            }
            else if( ! one[i].equals( two[i] )) {
                return false;
            }
        }
        return true;
    }

    /**
     * Helper method to produce more comprehensible Exceptions when arrays are being
     * assigned. It basically does this:
     * <pre>
     *     one[fromOne] = two;
     * </pre>
     *
     * @param one the array to which we assign
     * @param fromOne the index of into array one
     * @param two the Object which is being assigned.
     * @throws ArrayStoreException passed-on Exception with better error message
     * @throws ArrayIndexOutOfBoundsException passed-on Exception with better error message
     * @param <T> parameterizes the array
     */
    public static final <T> void myArrayCopy(
            T [] one,
            int  fromOne,
            T    two )
        throws
            ArrayStoreException,
            ArrayIndexOutOfBoundsException
    {
        try {
            one[ fromOne ] = two;

        } catch( ArrayStoreException ex ) {
            throw new ArrayStoreException( "tried storing " + two + " in array of type " + one.getClass().getComponentType() );

        } catch( ArrayIndexOutOfBoundsException ex ) {
            throw new ArrayIndexOutOfBoundsException( "index: " + fromOne + ", array: " + arrayToString( one ));
        }
    }

    /**
     * Helper to create an array of a generic type. I can't believe this isn't in the JDK.
     *
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @param size the size of the to-be-created array
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    @SuppressWarnings(value={"unchecked"})
    public static <T> T [] createArray(
            Class<T> arrayComponentType,
            int      size )
    {
        T [] ret = (T []) Array.newInstance(
                    arrayComponentType,
                    size );
        return ret;
    }
    
    /**
     * Helper to create an array of a generic type. I can't believe this isn't in the JDK.
     *
     * @param templateArray this array's component type is used as the component type of the new array
     * @param size the size of the to-be-created array
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    @SuppressWarnings(value={"unchecked"})
    public static <T> T [] createArray(
            T [] templateArray,
            int  size )
    {
        T [] ret = (T []) Array.newInstance(
                    (Class<T>) templateArray.getClass(),
                    size );
        return ret;
    }

    /**
     * Helper method to create an arrayComponentType from the componentType of an array
     * that's provided.
     *
     * @param arg the array
     * @return the component type of the array
     * @param <T> parameterizes the array
     */
    @SuppressWarnings(value={"unchecked"})
    public static <T> Class<T> arrayComponentClassFromArray(
            T [] arg )
    {
        return (Class<T>) arg.getClass().getComponentType();
    }
    
    /**
     * Append the second array to the first. Do not modify arguments.
     *
     * @param firstArray the first array
     * @param secondArray the second array
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] append(
            T []     firstArray,
            T []     secondArray,
            Class<T> arrayComponentType )
    {
        T [] ret = null;
        if ( firstArray == null ) {
            ret = secondArray;
        } else if ( secondArray == null ) {
            ret = firstArray;
        } else {
            ret = createArray(
                    arrayComponentType,
                    firstArray.length + secondArray.length );

            for( int i=0 ; i<firstArray.length ; ++i ) {
                myArrayCopy( ret, i, firstArray[i] );
            }

            int offset = firstArray.length;
            for( int i=0 ; i<secondArray.length ; ++i ) {
                myArrayCopy( ret, i+offset, secondArray[i] );
            }
        }
        return ret;
    }

    /**
     * Append the third and the second array to the first. Do not modify arguments.
     *
     * @param firstArray the first array
     * @param secondArray the second array
     * @param thirdArray the second array
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] append(
            T []     firstArray,
            T []     secondArray,
            T []     thirdArray,
            Class<T> arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                firstArray.length + secondArray.length + thirdArray.length );

        for( int i=0 ; i<firstArray.length ; ++i ) {
            myArrayCopy( ret, i, firstArray[i] );
        }

        int offset = firstArray.length;
        for( int i=0 ; i<secondArray.length ; ++i ) {
            myArrayCopy( ret, i+offset, secondArray[i] );
        }
        offset += secondArray.length;
        for( int i=0 ; i<thirdArray.length ; ++i ) {
            myArrayCopy( ret, i+offset, thirdArray[i] );
        }
        return ret;
    }

    /**
     * Prepend an object to an array and return newly allocated array. Do not modify arguments.
     *
     * @param first the Object to prepend
     * @param secondArray the second array
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] append(
            T        first,
            T []     secondArray,
            Class<T> arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                secondArray.length + 1 );

        ret[0] = first;
        for( int i=0 ; i<secondArray.length ; ++i ) {
            myArrayCopy( ret, i+1, secondArray[i] );
        }
        return ret;
    }

    /**
     * Append an object to an array and return newly allocated array. Do not modify arguments.
     *
     * @param firstArray the first array
     * @param item the Object to append
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] append(
            T []     firstArray,
            T        item,
            Class<T> arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                firstArray.length + 1 );

        for( int i=0 ; i<firstArray.length ; ++i ) {
            myArrayCopy( ret, i, firstArray[i] );
        }
        myArrayCopy( ret, ret.length-1, item );

        return ret;
    }

    /**
     * Append N arrays to each other and return newly allocated array. Do not modify arguments.
     *
     * @param theArrays the arrays whose content shall be appended
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] append(
            T [][]   theArrays,
            Class<T> arrayComponentType )
    {
        if( theArrays == null ) {
            return null;
        }

        int length=0;
        for( int i=0 ; i<theArrays.length ; ++i ) {
            length += theArrays[i].length;
        }
        T [] ret = createArray(
                arrayComponentType,
                length );

        for( int i=0, j=0 ; i<theArrays.length ; ++i ) {
            for( int k=0 ; k<theArrays[i].length ; ++k ) {
                myArrayCopy( ret, j++, theArrays[i][k] );
            }
        }
        return ret;
    }

    /**
      * Append 2 arrays to each other and return newly allocated array. Do not modify arguments.
      *
      * @param firstArray the first array
      * @param secondArray the second array
      * @return a newly created array
      */
    public static byte [] append(
            byte [] firstArray,
            byte [] secondArray )
    {
        byte [] ret = new byte[ firstArray.length + secondArray.length ];

        System.arraycopy( firstArray,  0, ret, 0,                 firstArray.length );
        System.arraycopy( secondArray, 0, ret, firstArray.length, secondArray.length );

        return ret;
    }

    /**
     * Append item to array if item is not yet in the array,
     * otherwise return array unchanged.
     *
     * @param firstArray     the array to append to
     * @param item           the item to append
     * @param useEquals      use equals() method to determine whether already in the array, ==
     * @param arrayComponentType the type of the array to return (if newly allocated)
     * @return the newly allocated array of type arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] appendWithoutDuplicates(
            T []     firstArray,
            T        item,
            boolean  useEquals,
            Class<T> arrayComponentType )
    {
        if( useEquals ) {
            for( int i=0 ; i<firstArray.length ; ++i ) {
                if( firstArray[i] == null ) {
                    if( item == null ) {
                        return firstArray;
                    }
                } else if( firstArray[i].equals( item )) {
                    return firstArray;
                }
            }
        }  else {
            for( int i=0 ; i<firstArray.length ; ++i ) {
                if( firstArray[i] == item ) {
                    return firstArray;
                }
            }
        }
        return append( firstArray, item, arrayComponentType );
    }

    /**
     * Append the content of an array to an array, but leave out
     * duplicates.
     *
     * @param firstArray     the array to append to
     * @param secondArray    the array to append
     * @param useEquals      use equals() method to determine whether already in the array, == otherwise
     * @param arrayComponentType the type of the array to return (if newly allocated)
     * @return the newly allocated array of type arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] appendWithoutDuplicates(
            T []     firstArray,
            T []     secondArray,
            boolean  useEquals,
            Class<T> arrayComponentType )
    {
        if( secondArray == null ) {
            return firstArray;
        } else if( firstArray == null ) {
            return secondArray;
        }

        T [] buf = createArray( arrayComponentType, secondArray.length ); // over-allocated
        int count = 0;

        for( int i=0 ; i<secondArray.length ; ++i ) {
            boolean found = false;
            for( int j=0 ; j<firstArray.length ; ++j ) {
                if( firstArray[j] == secondArray[i] ) {
                    found = true;
                    break;
                }
                if( useEquals && firstArray[j] != null && firstArray[j].equals( secondArray[i] )) {
                    found = true;
                    break;
                }
            }
            if( !found ) {
                buf[ count++ ] = secondArray[i];
            }
        }

        T [] ret = createArray(
                arrayComponentType,
                firstArray.length + count );

        for( int i=0 ; i<firstArray.length ; ++i ) {
            myArrayCopy( ret, i, firstArray[i] );
        }
        for( int i=ret.length-1 ; count>0 ; --i ) {
            myArrayCopy( ret, i, buf[--count] );
        }

        return ret;
    }

    /**
     * Append the contents of N arrays to each other, but leave out
     * duplicates.
     *
     * @param theArrays      the array of arrays to append after each other
     * @param useEquals      use equals() method to determine whether already in the array, == otherwise
     * @param skipNull       if true, do not return any nulls found in the input arrays
     * @param arrayComponentType the type of the array to return (if newly allocated)
     * @return the newly allocated array of type arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] appendWithoutDuplicates(
            T [][]   theArrays,
            boolean  useEquals,
            boolean  skipNull,
            Class<T> arrayComponentType )
    {
        if( theArrays == null || theArrays.length == 0 ) {
            return null;
        }

        ArrayList<T> almostRet = new ArrayList<T>( theArrays.length * 5 ); // fudge factor, better than nothing
        for( int i=0 ; i<theArrays.length ; ++i ) {
            if( theArrays[i] == null ) {
                continue;
            }
            for( int j=0 ; j<theArrays[i].length ; ++j ) {
                if( skipNull && theArrays[i][j] == null ) {
                    continue;
                }
                Iterator<T> theIter = almostRet.iterator();
                boolean found = false;
                if( useEquals ) {
                    while( theIter.hasNext() ) {
                        if( theArrays[i][j].equals( theIter.next() )) {
                            found = true;
                            break;
                        }
                    }
                } else {
                    while( theIter.hasNext() ) {
                        if( theArrays[i][j] == theIter.next() ) {
                            found = true;
                            break;
                        }
                    }
                }
                if( ! found ) {
                    almostRet.add( theArrays[i][j] );
                }
            }
        }
        return copyIntoNewArray( almostRet, arrayComponentType );
    }

    /**
     * Copy content from one array into a new one.
     *
     * @param theOldArray the array from where we take the content
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] copyIntoNewArray(
            T []               theOldArray,
            Class<? extends T> arrayComponentType )
    {
        return copyIntoNewArray( theOldArray, 0, theOldArray.length, arrayComponentType );
    }

    /**
     * Copy content from one array into a new one starting with index start
     * and ending with index end-1.
     *
     * @param theOldArray the array from where we take the content
     * @param startIndex index of the first object to copy
     * @param endIndex index of the first object not to copy any more
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] copyIntoNewArray(
            T []               theOldArray,
            int                startIndex,
            int                endIndex,
            Class<? extends T> arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                endIndex - startIndex );

        for( int i=startIndex ; i<endIndex ; ++i ) {
            myArrayCopy( ret, i-startIndex, theOldArray[i] );
        }
        return ret;
    }

    /**
     * Copy content from one a Vector into a new array.
     *
     * @param theVector the Vector from where we take the content
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] copyIntoNewArray(
            Vector<? extends T> theVector,
            Class<T>            arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                theVector.size() );

        theVector.copyInto( ret );
        return ret;
    }

    /**
     * Copy content from one a Hashtable into a new array.
     *
     * @param theTable the Hashtable from where we take the content
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] copyIntoNewArray(
            Hashtable<?,? extends T> theTable,
            Class<T>                 arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                theTable.size() );

        synchronized( theTable ) {
            Enumeration<? extends T> theEnum = theTable.elements();
            int i = 0;
            while( theEnum.hasMoreElements() ) {
                myArrayCopy( ret, i++, theEnum.nextElement() );
            }
            return ret;
        }
    }

    /**
     * Copy content from one a Collection into a new array.
     *
     * @param theCollection the Collection from where we take the content
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] copyIntoNewArray(
            Collection<? extends T> theCollection,
            Class<T>                arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                theCollection.size() );

        Iterator<? extends T> theIter = theCollection.iterator();
        for( int i=0 ; i<ret.length ; ++i ) {
            myArrayCopy( ret, i, theIter.next() );
        }
        return ret;
    }

    /**
     * Copy content from what can be obtained from an Iterator into a new array.
     *
     * @param theIter the Iterator from where we take the content
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] copyIntoNewArray(
            Iterator<? extends T> theIter,
            Class<T>              arrayComponentType )
    {
        ArrayList<T> temp = new ArrayList<T>();
        while( theIter.hasNext() ) {
            T current = theIter.next();
            temp.add( current );
        }
        return copyIntoNewArray( temp, arrayComponentType );
    }

    /**
     * Determine whether two arrays contain the same content
     * and in the same sequence.
     *
     * @param firstArray the first array
     * @param secondArray the second array
     * @param useEquals if true, use the equals() method to determine equality
     * @return true if the two arrays have the same content in order
     * @param <T> parameterizes the array
     */
    public static <T> boolean hasSameContentInOrder(
            T []    firstArray,
            T []    secondArray,
            boolean useEquals )
    {
        if( firstArray.length != secondArray.length ) {
            return false;
        }
        for( int i=0 ; i<firstArray.length ; ++i ) {
            if( useEquals ) {
                if( firstArray[i] == null && secondArray[i] != null ) {
                    return false;
                }
                if( ! firstArray[i].equals( secondArray[i] )) {
                    return false;
                }
            }
            else if( firstArray[i] != secondArray[i] ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Determine whether two arrays contain the same content,
     * but potentially in a different sequence.
     *
     * @param firstArray the first array
     * @param secondArray the second array
     * @param useEquals if true, use the equals() method to determine equality
     * @return true if the two arrays have the same content
     * @param <T> parameterizes the array
     */
    public static <T> boolean hasSameContentOutOfOrder(
            T []    firstArray,
            T []    secondArray,
            boolean useEquals )
    {
        if( firstArray == null ) {
            return secondArray == null;
        } else if( secondArray == null ) {
            return false;
        }

        if( firstArray.length != secondArray.length ) {
            return false;
        }

        boolean [] takenAlready = new boolean[ firstArray.length ];
            // content defaults to false

        outer: for( int i=0 ; i<firstArray.length ; ++i ) {
            if( firstArray[i] == null ) {
                for( int j=0 ; j<firstArray.length ; ++j ) {
                    if( takenAlready[j] ) {
                        continue;
                    }
                    if( secondArray[j] == null ) {
                        takenAlready[j] = true;
                        continue outer;
                    }
                }
                return false;
            } else {
                for( int j=0 ; j<firstArray.length ; ++j ) {
                    if( takenAlready[j] ) {
                        continue;
                    }
                    if( useEquals ) {
                        if( firstArray[i].equals( secondArray[j] )) {
                            takenAlready[j] = true;
                            continue outer;
                        }
                    } else {
                        if( firstArray[i] == secondArray[j] ) {
                            takenAlready[j] = true;
                            continue outer;
                        }
                    }
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Create a new array of the passed type with the same content as the
     * input array, except for Object objectToRemove.
     *
     * @param theArray the input array
     * @param objectToRemove the Object to remove from the input array if present
     * @param useEquals if true, use equals method to determine equality
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @throws IllegalArgumentException if objectToRemove could not be found in the input array
     * @param <T> parameterizes the array
     */
    public static <T> T [] remove(
            T []     theArray,
            T        objectToRemove,
            boolean  useEquals,
            Class<T> arrayComponentType )
        throws
            IllegalArgumentException
    {
        int index=-1;
        for( int i=0 ; i<theArray.length ; ++i ) {
            if( theArray[i] == objectToRemove
                || ( useEquals && theArray[i] != null && theArray[i].equals( objectToRemove ) ))
            {
                index = i;
                break;
            }
        }
        if( index < 0 ) {
            throw new IllegalArgumentException( "object not found in array" );
        }

        T [] ret = createArray(
                arrayComponentType,
                theArray.length-1 );
        for( int i=0 ; i<index ; ++i ) {
            myArrayCopy( ret, i, theArray[i] );
        }
        for( int i=index+1 ; i<theArray.length ; ++i ) {
            myArrayCopy( ret, i-1, theArray[i] );
        }
        return ret;
    }

    /**
     * Create a new array of the passed type with the same content as the
     * input array, except for Object objectToRemove. Don't complain if the
     * object isn't there
     *
     * @param theArray the input array
     * @param objectToRemove the Object to remove from the input array if present
     * @param useEquals if true, use equals method to determine equality
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] removeIfPresent(
            T []     theArray,
            T        objectToRemove,
            boolean  useEquals,
            Class<T> arrayComponentType )
    {
        int index = -1;
        for( int i=0 ; i<theArray.length ; ++i ) {
            if( theArray[i] == objectToRemove
                || ( useEquals && theArray[i] != null && theArray[i].equals( objectToRemove ) ))
            {
                index = i;
                break;
            }
        }

        T [] ret;

        if( index >= 0 ) {
            // normal case, we found it
            ret = createArray(
                    arrayComponentType,
                    theArray.length-1 );

            for( int i=0 ; i<index ; ++i ) {
                myArrayCopy( ret, i, theArray[i] );
            }
            for( int i=index+1 ; i<theArray.length ; ++i ) {
                myArrayCopy( ret, i-1, theArray[i] );
            }
        } else {
            ret = createArray(
                    arrayComponentType,
                    theArray.length );

            for( int i=0 ; i<theArray.length ; ++i ) {
                myArrayCopy( ret, i, theArray[i] );
            }
        }
        return ret;
    }

    /**
     * Create a new array of the passed type with the same content as the first
     * input array, except that all objects from second input array are not present.
     * Don't complain if objects aren't there.
     *
     * @param one the input array
     * @param two the array whose objects shall not appear in the return value
     * @param useEquals if true, use equals method to determine equality
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] removeIfPresent(
            T []     one,
            T []     two,
            boolean  useEquals,
            Class<T> arrayComponentType )
    {
        if( one == null ) {
            return null;
        }
        if( two == null ) {
            return one;
        }

        ArrayList<T> almostRet = new ArrayList<T>( one.length );
        outer: for( int i=0 ; i<one.length ; ++i ) {
            for( int j=0 ; j<two.length ; ++j ) {
                if( one[i] == two[j] ) {
                    continue outer;
                }
                if( useEquals && one[i] != null && one[i].equals( two[j] )) {
                    continue outer;
                }
            }
            almostRet.add( one[i] );
        }
        return copyIntoNewArray( almostRet, arrayComponentType );
    }

    /**
     * Create a new array of the passed type with the same content as the
     * input array, except for the object at a certain index which is removed.
     * All subsequent objects move forward by one spot.
     *
     * @param theArray the input array
     * @param index the index of the object that shall not be returned
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] remove(
            T []     theArray,
            int      index,
            Class<T> arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                theArray.length - 1 );

        for( int i=0 ; i<index ; ++i ) {
            ret[i] = theArray[i];
        }
        for( int i=index+1 ; i<theArray.length ; ++i ) {
            ret[i-1] = theArray[i];
        }
        return ret;
    }

    /**
     * Create a new array that is like the input array, except that an additional object
     * has been inserted at a certain location.
     *
     * @param theArray the input array
     * @param toInsert the Object to be inserted
     * @param insertIndex the index where the Object shall be inserted
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] insert(
            T []     theArray,
            T        toInsert,
            int      insertIndex,
            Class<T> arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                theArray.length + 1 );

        for( int i=0 ; i<insertIndex ; ++i ) {
            ret[i] = theArray[i];
        }
        ret[insertIndex] = toInsert;
        for( int i=insertIndex ; i<theArray.length ; ++i ) {
            ret[i+1] = theArray[i];
        }
        return ret;
    }

    /**
     * This is a "substring" for arrays.
     *
     * @param inputArray the input array
     * @param startIndex the index from which we start copying
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] subarray(
            T []     inputArray,
            int      startIndex,
            Class<T> arrayComponentType )
    {
        return subarray( inputArray, startIndex, inputArray.length, arrayComponentType );
    }

    /**
     * This is a "substring" for arrays.
     *
     * @param inputArray the input array
     * @param startIndex the index from which we start copying
     * @param endIndex the index that we are not copying any more
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] subarray(
            T []     inputArray,
            int      startIndex,
            int      endIndex,
            Class<T> arrayComponentType )
    {
        T [] ret = createArray(
                arrayComponentType,
                endIndex - startIndex );

        for( int i=0 ; i<ret.length ; ++i ) {
            myArrayCopy( ret, i, inputArray[i + startIndex ] );
        }
        return ret;
    }

    /**
     * Determine whether an Object is contained in an array.
     *
     * @param theObject the Object that we are looking for
     * @param theArray the array in which we look
     * @param useEquals if true, use equals method to determine equality
     * @return true if the Object is contained in the array
     * @param <T> parameterizes the array
     */
    public static <T> boolean isIn(
            T       theObject,
            T []    theArray,
            boolean useEquals )
    {
        return isIn( theObject, theArray, 0, theArray.length, useEquals );
    }

    /**
     * Determine whether an Object is contained in an array between two indices.
     *
     * @param theObject the Object that we are looking for
     * @param theArray the array in which we look
     * @param from the index at which we start looking
     * @param to the index at which we are not looking any more
     * @param useEquals if true, use equals method to determine equality
     * @return true if the Object is contained in the array between the indices
     * @param <T> parameterizes the array
     */
    public static <T> boolean isIn(
            T       theObject,
            T []    theArray,
            int     from,
            int     to,
            boolean useEquals )
    {
        for( int i=from ; i<to ; ++i ) {
            if( theObject == theArray[i] ) {
                return true;
            }
            if( useEquals && theObject != null && theObject.equals( theArray[i] )) {
                return true;
            }
        }
        return false;
    }

    /**
     * Find the index of an Object in an array.
     *
     * @param theObject the Object that we are looking for
     * @param theArray the array in which we look
     * @param useEquals if true, use equals method to determine equality
     * @return the index where the Object was found, or -1 if not found
     * @param <T> parameterizes the array
     */
    public static <T> int findIn(
            T       theObject,
            T []    theArray,
            boolean useEquals )
    {
        return findIn( theObject, theArray, 0, theArray.length, useEquals );
    }

    /**
     * Find the index of an Object in an array.
     *
     * @param theObject the Object that we are looking for
     * @param theArray the array in which we look
     * @param from the index at which we start looking
     * @param to the index at which we are not looking any more
     * @param useEquals if true, use equals method to determine equality
     * @return the index where the Object was found, or -1 if not found
     * @param <T> parameterizes the array
     */
    public static <T> int findIn(
            T       theObject,
            T []    theArray,
            int     from,
            int     to,
            boolean useEquals )
    {
        for( int i=from ; i<to ; ++i ) {
            if( theObject == theArray[i] ) {
                return i;
            }
            if( useEquals && theObject != null && theObject.equals( theArray[i] )) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determine whether an Object is contained in an array or an array
     *
     * @param theObject the Object that we are looking for
     * @param theArrayArray the array of arrays in which we look
     * @param useEquals if true, use equals method to determine equality
     * @return true if the Object is contained in the array
     * @param <T> parameterizes the array
     */
    public static <T> boolean isIn(
            T       theObject,
            T [][]  theArrayArray,
            boolean useEquals )
    {
        for( int i=0 ; i<theArrayArray.length ; ++i ) {
            if( isIn( theObject, theArrayArray[i], 0, theArrayArray[i].length, useEquals )) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine whether the first array contains all elements of the second array.
     *
     * @param first the first array
     * @param second the second array.
     * @param useEquals if true, use equals method to determine equality
     * @return true if the first array contains all elements of the second array
     * @param <T> parameterizes the array
     */
    public static <T> boolean firstHasSecondAsSubset(
            T []    first,
            T []    second,
            boolean useEquals )
    {
        for( int i=0 ; i<second.length ; ++i ) {
            if( !isIn( second[i], first, useEquals )) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a new array with the same content as the passed-in
     * array but without any duplicates. The returned array may have fewer
     * elements.
     *
     * @param theArray the input array
     * @param useEquals if true, use equals method to determine equality
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] getWithoutDuplicates(
            T []     theArray,
            boolean  useEquals,
            Class<T> arrayComponentType )
    {
        T [] almostRet = createArray( arrayComponentType, theArray.length );
        int max = 0;

        if( theArray == null ) {
            return null;
        }
        if( theArray.length > 0 ) {
            myArrayCopy( almostRet, max++, theArray[0] );

            for( int i=1 ; i<theArray.length ; ++i ) {
                boolean found = false;

                for( int j=0 ; j<max ; ++j ) {
                    if( almostRet[ j ] == theArray[i] ) {
                        found = true;
                        break;
                    }
                    if( useEquals && almostRet[j] != null && almostRet[j].equals( theArray[i] )) {
                        found = true;
                        break;
                    }
                }
                if( !found ) {
                    myArrayCopy( almostRet, max++, theArray[i] );
                }
            }
        }
        return subarray( almostRet, 0, max, arrayComponentType );
    }

    /**
     * This function selects the full set or a subset of the first argument
     * by applying the comparison object on each of them with one of the
     * arguments from the second set. If there is at least one true result
     * for any argument object, the candidate object is included in the
     * result.
     *
     * @param candidateObjects the Objects that we test for inclusion in the return result
     * @param argumentObjects for each Object in this array, theFunction is executed with one Object from canidateObjects
     * @param theFunction the function to be executed
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] select(
            T []             candidateObjects,
            T []             argumentObjects,
            SelectorFunction theFunction,
            Class<T>         arrayComponentType )
    {
        ArrayList<T> tempRet = new ArrayList<T>();

        for( int i=0 ; i<candidateObjects.length ; ++i ) {
            for( int j=0 ; j<argumentObjects.length ; ++j ) {
                if( theFunction.matches( candidateObjects[i], argumentObjects[j] )) {
                    tempRet.add( candidateObjects[i] );
                    break;
                }
            }
        }
        return copyIntoNewArray( tempRet, arrayComponentType );
    }

    /**
     * This returns THE SAME array with the same elements, but
     * ordered according to the Comparator. Not very efficient.
     *
     * @param ret the input array which is also returned
     * @param c the Comparator according to which we sort
     * @return same as ret
     * @param <T> parameterizes the array
     */
    public static <T> T [] sort(
            T []          ret,
            Comparator<T> c )
    {
        for( int i=0 ; i<ret.length ; ++i ) {
            T bestMatch = ret[i];
            for( int j=i+1 ; j<ret.length ; ++j ) {
                if( c.compare( ret[j], bestMatch ) > 0 ) {
                    ret[i] = ret[j];
                    ret[j] = bestMatch;
                    bestMatch = ret[i];
                }
            }
        }

        return ret;
    }

    /**
     * This returns the Objects provided by the Iterator in array form, and sorted
     * according to a Comparator.
     *
     * @param iter the Iterator from which we determine Objects
     * @param c the Comparator according to which we sort
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return a newly created array of arrayComponentType
     * @param <T> parameterizes the array
     */
    public static <T> T [] sort(
            Iterator<T>   iter,
            Comparator<T> c,
            Class<T>      arrayComponentType )
    {
        TreeSet<T> temp = new TreeSet<T>( c );
        while( iter.hasNext() ) {
            temp.add( iter.next() );
        }
        return copyIntoNewArray( temp, arrayComponentType );
    }

    /**
     * The following function essentially provides a toString() method for a
     * array of objects. Imagine you could do this:
     * <pre>
     *     Object [] myArray = new Object[] { "abc", 13, new java.awt.Button() };
     *     System.println( myArray.toString() );
     * </pre>
     * where the output would be
     * <pre>
     *     abc/13/java.awt.Button@1234
     * </pre>
     * This is what this method returns. It returns &quot;java.lang.Object[0]&quot; if the
     * array has length 0, and null if it is null.
     *
     * @param array the array to convert to a String
     * @return String form of the array
     */
    public static String arrayToString(
            Object [] array )
    {
        return arrayToString( array, "/" );
    }

    /**
     * This is the same as arrayToString( Object [] ) but it lets you specify a
     * separator String.
     *
     * @param array the array to convert to a String
     * @param separator the sepator String
     * @return String form of the array
     */
    public static String arrayToString(
            Object [] array,
            String    separator )
    {
        if( array == null ) {
            return "null";
        }
        if( array.length == 0 ) {
            return array.toString();
        }
        StringBuffer ret = new StringBuffer( "{ " );
        if( array[0] instanceof Object[] ) {
            ret.append( ArrayHelper.arrayToString( (Object []) array[0] ));
        } else {
            ret.append( array[0] );
        }
        for( int i=1 ; i<array.length ; ++i ) {
            ret.append( separator );
            if( array[i] instanceof Object[] ) {
                ret.append( ArrayHelper.arrayToString( (Object []) array[i] ));
            } else {
                ret.append( array[i] );
            }
        }
        ret.append( " }" );
        return new String( ret );
    }

    /**
     * The following function essentially provides a toString() method for a
     * array of long.
     *
     * @param array the array to convert to a String
     * @return String form of the array
     */
    public static String arrayToString(
            long [] array )
    {
        return arrayToString( array, "/" );
    }

    /**
     * This is the same as arrayToString( long [] ) but it lets you specify a
     * separator String.
     *
     * @param array the array to convert to a String
     * @param separator the sepator String
     * @return String form of the array
     */
    public static String arrayToString(
            long [] array,
            String  separator )
    {
        if( array == null ) {
            return "null";
        }
        if( array.length == 0 ) {
            return array.toString();
        }
        StringBuffer ret = new StringBuffer( "{ " );
        ret.append( array[0] );
        for( int i=1 ; i<array.length ; ++i ) {
            ret.append( separator );
            ret.append( array[i] );
        }
        ret.append( " }" );
        return new String( ret );
    }

    /**
     * Same as arrayToString except for Collections.
     *
     * @param coll the Collection to convert to a String
     * @return the String form of the Collection
     */
    public static String collectionToString(
            Collection<?> coll )
    {
        return collectionToString( coll, "/" );
    }

    /**
     * Same as arrayToString except for Collections.
     *
     * @param coll the Collection to convert to a String
     * @param separator the sepator String
     * @return the String form of the Collection
     */
    public static String collectionToString(
            Collection<?> coll,
            String        separator )
    {
        if( coll == null ) {
            return "null";
        }

        synchronized( coll ) {
            if( coll.isEmpty() ) {
                return coll.toString();
            }

            Iterator theIter = coll.iterator();

            StringBuffer ret = new StringBuffer( "{ " );
            ret.append( theIter.next() );

            while( theIter.hasNext() ) {
                ret.append( separator );
                ret.append( theIter.next() );
            }
            ret.append( " }" );
            return new String( ret );
        }
    }

    /**
     * Collect all non-null elements from an array into a new array, in sequence.
     *
     * @param theArray the array that may contain null and non-null elements
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @return the array with non-null elements only
     * @param <T> parameterizes the array
     */
    public static <T> T [] collectNonNull(
            T []     theArray,
            Class<T> arrayComponentType )
    {
        int count = 0;
        for( int i=0 ; i<theArray.length ; ++i ) {
            if( theArray[i] != null ) {
                ++count;
            }
        }
        return collectNonNull( theArray, arrayComponentType, count );
    }

    /**
     * Collect all non-null elements from an array into a new array, in sequence.
     * Use this call if the number of non-null elements in the array are known.
     *
     * @param theArray the array that may contain null and non-null elements
     * @param arrayComponentType the base type of the to-be-created array that we return
     * @param n the number of non-null elements in the array
     * @return the array with non-null elements only
     * @param <T> parameterizes the array
     */
    public static <T> T [] collectNonNull(
            T []     theArray,
            Class<T> arrayComponentType,
            int      n )
    {
        T [] ret = createArray(
                arrayComponentType,
                n );

        for( int i=0, j=0 ; i<theArray.length ; ++i ) {
            if( theArray[i] != null ) {
                ret[j++] = theArray[i];
            }
        }
        
        return ret;
    }

    /**
     * Same as arrayToString except for Maps.
     *
     * @param map the Map to convert to a String
     * @return the String form of the Map
     */
    public static String mapToString(
            Map<?,?> map )
    {
        return mapToString( map, "/", "=>" );
    }

    /**
     * Same as arrayToString except for Maps.
     *
     * @param map the Map to convert to a String
     * @param sep1 the separator prior to a key
     * @param sep2 the separator between the key and the value
     * @return the String form of the Map
     */
    public static String mapToString(
            Map<?,?> map,
            String   sep1, 
            String   sep2 )
    {
        if( map == null ) {
            return "null";
        }

        synchronized( map ) {
            if( map.isEmpty() ) {
                return map.toString();
            }

            Iterator theIter = map.keySet().iterator();

            StringBuffer ret = new StringBuffer( "{ " );
            Object       key = theIter.next();
            Object       val = map.get( key );
                    
            ret.append( key );
            ret.append( sep2 );
            ret.append( val );

            while( theIter.hasNext() ) {
                key = theIter.next();
                val = map.get( key );

                ret.append( sep1 );
                ret.append( key );
                ret.append( sep2 );
                ret.append( val );
            }
            ret.append( " }" );
            return new String( ret );
        }
    }

    /**
     * Join Strings, like Perl.
     *
     * @param data the data elements
     * @return the joined String
     */
    public static String join(
            Object [] data )
    {
        return join( ", ", data );
    }
    
    /**
     * Join Strings, like Perl.
     *
     * @param separator the separator between the data elements
     * @param data the data elements
     * @return the joined String
     */
    public static String join(
            String    separator,
            Object [] data )
    {
        return join( separator, "", "", "null", data );
    }

    /**
     * Join Strings, like Perl.
     *
     * @param separator the separator between the data elements
     * @param prefix the prefix, if the data is non-null
     * @param postfix the prefix, if the data is non-null
     * @param ifNull to be written if the data is null
     * @param data the data elements
     * @return the joined String
     */
    public static String join(
            String    separator,
            String    prefix,
            String    postfix,
            String    ifNull,
            Object [] data )
    {
        if( data == null ) {
            return ifNull;
        }
        String       sep = "";
        StringBuffer ret = new StringBuffer();

        ret.append( prefix );
        for( int i=0 ; i<data.length ; ++i ) {
            ret.append( sep );
            ret.append( data[i] );
            sep = separator;
        }
        ret.append( postfix );
        return ret.toString();
    }

    /**
     * Determine the difference between two arrays.
     *
     * @param one the first array
     * @param two the second array
     * @param useEquals if true, us the equals method for comparison
     * @param arrayComponentType the array component type for the returned arrays
     * @return the difference
     * @param <T> parameterizes the array
     */
    public static <T> Difference<T> determineDifference(
            T []     one,
            T []     two,
            boolean  useEquals,
            Class<T> arrayComponentType )
    {
        int max = Math.max( one.length, two.length );
        T [] additions = createArray( arrayComponentType, max );
        T [] removals  = createArray( arrayComponentType, max );
        int addCounter = 0;
        int remCounter = 0;

        for( int oneI=0 ; oneI<one.length ; ++oneI ) {
            T current = one[oneI]; // easier for debugging
            if( !isIn( current, two, useEquals )) {
                removals[ remCounter++ ] = current;
            }
        }
        for( int twoI=0 ; twoI<two.length ; ++twoI ) {
            T current = two[twoI]; // easier for debugging
            if( !isIn( current, one, useEquals )) {
                additions[ addCounter++ ] = current;
            }
        }
        
        if( addCounter < additions.length ) {
            additions = copyIntoNewArray( additions, 0, addCounter, arrayComponentType );
        }
        if( remCounter < removals.length ) {
            removals = copyIntoNewArray( removals, 0, remCounter, arrayComponentType );
        }
        return new Difference<T>( additions, removals );
    }

    /**
     * Helper class to return two values.
     * @param <T> the underlying type for the values
     */
    public static class Difference<T>
    {
        /**
         * Constructor.
         * 
         * @param additions the additions
         * @param removals the removals
         */
        public Difference(
                T [] additions,
                T [] removals )
        {
            theAdditions = additions;
            theRemovals  = removals;
        }
        
        /**
         * Obtain the additions.
         *
         * @return the additions
         */
        public T [] getAdditions()
        {
            return theAdditions;
        }
        
        /**
         * Obtain the removals.
         *
         * @return the removals
         */
        public T [] getRemovals()
        {
            return theRemovals;
        }

        protected T [] theAdditions;
        protected T [] theRemovals;
    }
    
    /**
      * This is a boolean function that is applied on one Object with one
      * argument. It is typically applied to determine whether an Object
      * shall be selected; classes implementing this interface provide the
      * selection criteria.
      */
    public interface SelectorFunction
    {
        /**
         * Determine whether this Object shall be selected.
         *
         * @param theObject the Object that we test
         * @param theArgument a user-specified argument
         * @return true if theObject shall be selected given the argument
         */
        public abstract boolean matches(
                Object theObject,
                Object theArgument );
    }
}
