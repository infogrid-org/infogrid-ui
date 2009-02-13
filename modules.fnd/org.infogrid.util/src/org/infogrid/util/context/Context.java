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

package org.infogrid.util.context;

import org.infogrid.util.CursorIterator;

/**
 * <p>A <code>Context</code> collects an "environment" of objects. One can look
 *    at it as the set of "static" variables in a C or C++ program,
 *    or somewhat like the environment of a U*IX shell.</p>
 * <p>The main difference are:</p>
 * <ul>
 *  <li>there can be many instances of <code>Context</code> within the
 *     same Java program (unlike sets of static variables in C).</li>
 *  <li>finding objects in a <code>Context</code> is performed by specifying
 *     a Class, rather than a name.</li>
 * </ul>
 * <p>Generally, a context contains only one instance per object
 *    class so a lookup returns no more than one instance.</p>
 * <p>Contexts can be chained hierarchically: if a lookup in
 *    one Context does not produce a result, Contexts automatically
 *    delegate the query to their parent Context (recursively).</p>
 * <p>Contexts have names. By prepending the hierarchical name of the
 *    parent context, a hierarchical name is constructed for the context.
 *    Note that child context names do not have to be unique within a
 *    parent context.</p>
 */
public interface Context
{
    /**
     * <p>Find an object in this <code>Context</code>by using its <code>Class</code>
     * or a superclass as a key. This method will return the
     * first object found if there are multiple objects of this class; to avoid this,
     * specify a more concrete class.</p>
     * <p>If the context object cannot be found, return <code>null</code>.</p>
     *
     * @param classOfContextObject class of the object that we are looking for
     * @return the found object, or null
     * @param <T> the type of Context object to find
     */
    public abstract <T> T findContextObject(
            Class<? extends T> classOfContextObject );

    /**
     * <p>Find an object in this <code>Context</code>by using its <code>Class</code>
     * or a superclass as a key. This method will return the
     * first object found if there are multiple objects of this class; to avoid this,
     * specify a more concrete class.</p>
     * <p>If the context object cannot be found, throw a
     *  {@link ContextObjectNotFoundException ContextObjectNotFoundException}.</p>
     *
     * @param classOfContextObject class of the object that we are looking for
     * @return the found object
     * @throws ContextObjectNotFoundException if the Context object was not found
     * @param <T> the type of Context object to find
     */
    public abstract <T> T findContextObjectOrThrow(
            Class<? extends T> classOfContextObject )
        throws
            ContextObjectNotFoundException;

    /**
     * Add an object to this Context.
     *
     * @param theContextObject the object to be added
     *
     * @see #removeContextObject
     */
    public abstract void addContextObject(
            Object theContextObject );

    /**
     * Remove an object currently in this Context from this Context.
     *
     * @param theContextObject the object to be removed
     *
     * @see #addContextObject
     */
    public abstract void removeContextObject(
            Object theContextObject );

    /**
     * Obtain an iterator over all context objects of this type.
     * 
     * @param classOfContextObject class of the objects that we are looking for
     * @return the iterator
     * @param <T> the type of Context object to find
     */
    public abstract <T> CursorIterator<T> contextObjectIterator(
            Class<? extends T> classOfContextObject );
    
    /**
     * Obtain the parent Context of this Context, if any.
     *
     * @return the parent Context of this Context, or null
     */
    public abstract Context getParentContext();

    /**
     * Get the name of this Context if it has one. The Name of a Context has
     * no further meaning; it is only used to assist with debugging.
     *
     * @return the name of the Context, or null
     */
    public abstract String getName();

    /**
     * Obtain the hierarchical name of this Context if it has one. The result
     * of this method call is a concatenation of all of the parents' getName()
     * methods' results.
     *
     * @return the hierarchical name of the Context. This may contain null elements.
     */
    public abstract String [] getHierarchicalName();
}
