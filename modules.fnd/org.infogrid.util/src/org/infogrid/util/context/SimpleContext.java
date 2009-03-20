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

package org.infogrid.util.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ArrayListCursorIterator;
import org.infogrid.util.CompositeCursorIterator;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FilteringCursorIterator;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
  * A simple implementation of the Context interface.
  */
public class SimpleContext
        implements
            Context,
            CanBeDumped
{
    private static final Log log = Log.getLogInstance(SimpleContext.class); // our own, private logger

    /**
     * Factory method to create a root Context. A root Context is a Context that does not have a parent
     * Context.
     *
     * @param nameOfRoot name of the root Context (may be null)
     * @return the newly created Context.
     */
    public static SimpleContext createRoot(
            String nameOfRoot )
    {
        return new SimpleContext( nameOfRoot, null );
    }

    /**
     * Factory method to create a named child Context of a given parent Context.
     *
     * @param parent the parent Context
     * @param name the name of the to-be-created Context.
     * @return the newly created Context.
     */
    public static SimpleContext create(
            Context parent,
            String  name )
    {
        return new SimpleContext( name, parent );
    }

    /**
      * Private constructor to create a new Context. Use factory methods instead.
      *
      * @param parent the parent Context
      * @param name the name of the to-be-created Context.
      */
    protected SimpleContext(
            String  name,
            Context parent )
    {
        this.theName          = name;
        this.theParentContext = parent;

        if( log.isTraceEnabled() ) {
            log.traceConstructor( this );
        }
    }

    /**
     * <p>Find an object in this <code>Context</code>by using its <code>Class</code>
     * or a superclass as a key. This method will return the
     * first object found if there are multiple objects of this class; to avoid this,
     * specify a more concrete class.</p>
     * <p>If the context object cannot be found, return <code>null</code>.</p>
     *
     * @param classOfContextObject class of the object that we are looking for it
     * @return the found object, or null
     * @param <T> type of the object to find
     */
    @SuppressWarnings(value={"unchecked"})
    public <T> T findContextObject(
            Class<? extends T> classOfContextObject )
    {
        if( theObjects != null ) {
            synchronized( this ) {
                Iterator<Object> theIter = theObjects.iterator();
                while( theIter.hasNext() ) {
                    Object current = theIter.next();
                    if( classOfContextObject.isInstance( current )) {
                        return (T) current;
                    }
                }
            }
        }
        if( theParentContext != null ) {
            return theParentContext.findContextObject( classOfContextObject );
        }
        return null;
    }

    /**
     * <p>Find an object in this <code>Context</code>by using its <code>Class</code>
     * or a superclass as a key. This method will return the
     * first object found if there are multiple objects of this class; to avoid this,
     * specify a more concrete class.</p>
     * <p>If the context object cannot be found, throw a
     *  {@link ContextObjectNotFoundException ContextObjectNotFoundException}.</p>
     *
     * @param classOfContextObject class of the object that we are looking for it
     * @return the found object
     * @throws ContextObjectNotFoundException if the Context object was not found
     * @param <T> type of the object to find
     */
    public <T> T findContextObjectOrThrow(
            Class<? extends T> classOfContextObject )
        throws
            ContextObjectNotFoundException
    {
        T ret = findContextObject( classOfContextObject );
        if( ret != null ) {
            return ret;
        } else {
            throw new ContextObjectNotFoundException( classOfContextObject );
        }
    }

    /**
      * Add an object to this Context.
      *
      * @param theContextObject the object to be added
      *
      * @see #removeContextObject
      */
    public void addContextObject(
            Object theContextObject )
    {
        if( theContextObject == null ) {
            throw new IllegalArgumentException( "Cannot add null object to context" );
        }
        synchronized( this ) {
            if( theObjects == null ) {
                theObjects = new ArrayList<Object>();
            }
            theObjects.add( 0, theContextObject ); // add at the beginning, so newer objects override old ones
        }

        if( log.isDebugEnabled() ) {
            log.debug( "added to \"" + this + "\" ( \"" + theContextObject + "\" )" );
        }
    }

    /**
      * Remove an object currently in this Context from this Context.
      *
      * @param theContextObject the object to be removed
      *
      * @see #addContextObject
      */
    public void removeContextObject(
            Object theContextObject )
    {
        synchronized( this ) {
            theObjects.remove( theContextObject );
        }

        if( log.isDebugEnabled() ) {
            log.debug( "removed from \"" + this + " ( \"" + theContextObject + "\" )" );
        }
    }

    /**
     * Obtain an iterator over all context objects of this type.
     * FIXME: there must be an effective way of using Java generics for this; if there is,
     * it escapes me.
     * 
     * @param classOfContextObject class of the object that we are looking for it
     * @return the iterator 
     * @param <T> the type of Context object to find
     */
    @SuppressWarnings( { "unchecked" } )
    public <T> CursorIterator<T> contextObjectIterator(
            final Class<? extends T> classOfContextObject )
    {
        FilteringCursorIterator.Filter filter = new FilteringCursorIterator.Filter() {
            
            /**
              * Determine whether or not to accept a candidate Object.
              *
              * @param candidate the candidate Object
              * @return true if this Object shall be accepted according to this Filter
              */
            public boolean accept(
                    Object candidate )
            {
                boolean ret = classOfContextObject.isInstance( candidate );
                return ret;
            }
        };
        
        Iterator thisIter = ArrayListCursorIterator.create( theObjects, Object.class );

        CursorIterator ret = FilteringCursorIterator.create( 
                (CursorIterator<Object>) thisIter,
                filter,
                (Class<Object>) classOfContextObject );

        if( theParentContext != null ) {
            List iterators = new ArrayList();
            iterators.add( ret );
            iterators.add( theParentContext.contextObjectIterator( classOfContextObject ));
            
            ret = CompositeCursorIterator.<Object>create( iterators, (Class<Object>) classOfContextObject );
        }
        return (CursorIterator<T>) ret;
    }

    /**
      * Obtain the parent Context of this Context, if any.
      *
      * @return the parent Context of this Context, or null
      */
    public Context getParentContext()
    {
        return theParentContext;
    }

    /**
     * Get the name of this Context if it has one. The Name of a Context has
     * no further meaning; it is only used to assist with debugging.
     *
     * @return the name of the Context, or null
     */
    public String getName()
    {
        return theName;
    }

    /**
     * Obtain the hierarchical name of this Context if it has one. The result
     * of this method call is a concatenation of all of the parents' getName()
     * methods' results.
     *
     * @return the hierarchical name of the Context. This may contain null elements.
     */
    public synchronized String [] getHierarchicalName()
    {
        if( theHierarchicalName == null ) {
            if( theParentContext != null ) {
                theHierarchicalName = ArrayHelper.append(
                        theParentContext.getHierarchicalName(),
                        theName,
                        String.class );
            } else {
                theHierarchicalName = new String[] { theName };
            }
        }
        return theHierarchicalName;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "hierName",
                    // "parentContext",
                    "objects"
                },
                new Object[] {
                    getHierarchicalName(),
                    // theParentContext,
                    theObjects
                });
    }

    /**
     * <p>The currently known context objects.</p>
     * <p>This uses an array list (rather than a hash table) because
     * we need to check against all supertypes, not only the concrete
     * subtype that happens to have been added to this context.</p>
     * <p>This is allocated dynamically.</p>
     */
    protected ArrayList<Object> theObjects = null;

    /**
     * The parent Context if it exists.
     */
    protected Context theParentContext;

    /**
     * The name of the Context, if it has one.
     */
    protected String theName;

    /**
     * The hierarchical name of the Context, constructed when needed and buffered then.
     */
    protected String [] theHierarchicalName = null;
}
