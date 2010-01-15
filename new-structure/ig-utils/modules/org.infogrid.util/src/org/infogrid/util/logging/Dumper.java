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

package org.infogrid.util.logging;

import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Enables an object to dump its contents.
 */
public interface Dumper
{
    /**
     * Dump an object.
     *
     * @param obj the object to dump
     */
    public void dump(
            Object obj );

    /**
     * Dump an object by dumping its named fields.
     *
     * @param obj the object to dump
     * @param fieldNames the fields of the object to dump
     * @param fieldValues the values of the fields of the object to dump
     */
    public void dump(
            Object    obj,
            String [] fieldNames,
            Object [] fieldValues );

    /**
     * Dump a null value.
     */
    public void dumpNull();

    /**
     * Dump an array of objects.
     *
     * @param obj the object to dump
     */
    public void dumpArray(
            Object [] obj );

    /**
     * Dump a Collection.
     *
     * @param obj the object to dump
     */
    public void dumpCollection(
            Collection<?> obj );

    /**
     * Dump a Map.
     *
     * @param obj the object to dump
     */
    public void dumpMap(
            Map<?,?> obj );

    /**
     * Dump a boolean array.
     *
     * @param obj the object to dump
     */
    public void dumpBooleanArray(
            boolean [] obj );

    /**
     * Dump a byte array.
     *
     * @param obj the object to dump
     */
    public void dumpByteArray(
            byte [] obj );

    /**
     * Dump a short array.
     *
     * @param obj the object to dump
     */
    public void dumpShortArray(
            short [] obj );

    /**
     * Dump an integer array.
     *
     * @param obj the object to dump
     */
    public void dumpIntArray(
            int [] obj );

    /**
     * Dump a long array.
     *
     * @param obj the object to dump
     */
    public void dumpLongArray(
            long [] obj );

    /**
     * Dump a float array.
     *
     * @param obj the object to dump
     */
    public void dumpFloatArray(
            float [] obj );

    /**
     * Dump a double array.
     *
     * @param obj the object to dump
     */
    public void dumpDoubleArray(
            double [] obj );

    /**
     * Dump a character array.
     *
     * @param obj the object to dump
     */
    public void dumpCharArray(
            char [] obj );

    /**
     * Dump a boolean.
     *
     * @param obj the object to dump
     */
    public void dumpBoolean(
            Boolean obj );

    /**
     * Dump a character.
     *
     * @param obj the object to dump
     */
    public void dumpCharacter(
            Character obj );

    /**
     * Dump a Date object.
     *
     * @param obj the object to dump
     */
    public void dumpDate(
            Date obj );

    /**
     * Dump a number, e.g. a short, int or long.
     *
     * @param obj the object to dump
     */
    public void dumpNumber(
            Number obj );

    /**
     * Dump a character sequence, e.g. String, StringBuffer, StringBuilder.
     *
     * @param obj the object to dump
     */
    public void dumpCharSequence(
            CharSequence obj );

    /**
     * Dump a reference, e.g. a SoftReference, WeakReference, PhantomReference.
     *
     * @param obj the object to dump
     */
    public void dumpReference(
            Reference<?> obj );

    /**
     * Dump an object by using its toString() method.
     *
     * @param obj the object to dump
     */
    public void dumpWithToString(
            Object obj );
}