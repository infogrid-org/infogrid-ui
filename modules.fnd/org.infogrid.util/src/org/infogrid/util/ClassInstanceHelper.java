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
  * A bundle of helper functions for frequently used operations
  * that deal with classes and instances.
  */
public abstract class ClassInstanceHelper
{
    /**
     * Private constructur to keep this abstract.
     */
    private ClassInstanceHelper()
    {
        // no op
    }

    /**
      * Determine whether an Object is an instance of a certain Class,
      * and if so, whether it is a direct or an indirect instance.
      * Returns -1 if not an instance, 0 for direct instance, and a
      * positive number for the number of levels in the inheritance
      * hierarchy it is removed. Works for both classes and interfaces.
      *
      * @param theObject the Object that we examine
      * @param theTargetClass the Class to which we determine the distance
      * @return the number of levels from the Object's Class to theTargetClass, or -1 if this is not an instance of theTargetClass
      */
    public static int obtainDistanceFromInstanceToClass(
              Object theObject,
              Class  theTargetClass )
    {
        // this is for speedup
        if( ! theTargetClass.isInstance( theObject )) {
            return -1;
        }

        return obtainDistanceFromClassToClass(
                theObject.getClass(),
                theTargetClass );
    }

    /**
      * Determine whether a source Class is a subtype of a target Class,
      * and if so, how many levels are in between.
      *
      * @param theSourceClass the Class that we examine
      * @param theTargetClass the Class to which we determine the distance
      * @return the number of levels from theSourceClass to theTargetClass, or -1 if this is not an subtype of theTargetClass
      */
    public static int obtainDistanceFromClassToClass(
            Class theSourceClass,
            Class theTargetClass )
    {
        // we need to do a breadth-first algorithm in order to
        // find the shortest distance in a multiple-inheritance case.
        // This can potentially be sped up by eliminating multiple
        // path investigation (but that needs a trial ...)

        ArrayList<Class> currentClasses = new ArrayList<Class>();
        currentClasses.add( theSourceClass );

        Iterator<Class> currentIter;
        int level = 0;

        while( currentClasses.size() > 0 ) {
            currentIter = currentClasses.iterator();
            while( currentIter.hasNext() ) {
                Class testClass = currentIter.next();
                if( testClass.equals( theTargetClass )) {
                    return level;
                }
            }
            // not found on this level
            currentClasses = getSupertypesOfClasses( currentClasses );
            ++level;
        }
        return -1;
    }

    /**
      * Obtain an array of Class objects from an array of instances. This is
      * similar to what a getClass method would do on an array.
      *
      * @param theObjects the passed-in instances
      * @return array of Class Objects for the corresponding entry in theObjects
      */
    public static Class [] getClasses(
            Object [] theObjects )
    {
        Class [] ret = new Class[ theObjects.length ];
        for( int i=0 ; i<theObjects.length ; ++i ) {
            ret[i] = theObjects[i].getClass();
        }

        return ret;
    }

    /**
      * An internal helper to find all superclasses of all classes
      * in an ArrayList.
      *
      * @param theClasses the input data
      * @return the list of all superclasses
      */
    private static ArrayList<Class> getSupertypesOfClasses(
            ArrayList<Class> theClasses )
    {
        ArrayList<Class> ret     = new ArrayList<Class>();
        Iterator<Class>  theIter = theClasses.iterator();

        while( theIter.hasNext() ) {
            Class current = theIter.next();
            Class superClass = current.getSuperclass();
            if( superClass != null ) {
                ret.add( current.getSuperclass() );
            }
            Class [] intfs = current.getInterfaces();
            for( int i=0 ; i<intfs.length ; ++i ) {
                ret.add( intfs[i] );
            }
        }
        return ret;
    }
}
