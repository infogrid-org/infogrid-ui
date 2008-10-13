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

package org.infogrid.util.text;

import java.util.Iterator;
import java.util.Map;

/**
 * Simple implementation of StringRepresentationContext.
 */
public class SimpleStringRepresentationContext
        implements
            StringRepresentationContext
{
    /**
     * Factory method.
     * 
     * @param contextObjects the objects in the context
     * @return the created SimpleStringRepresentationContext
     */
    public static SimpleStringRepresentationContext create(
            Map<String,Object> contextObjects )
    {
        return new SimpleStringRepresentationContext( contextObjects, null );
    }
    
    /**
     * Factory method.
     * 
     * @param contextObjects the objects in the context
     * @param delegate the StringRepresentationContext to delegate to if a context object could not be found locally
     * @return the created SimpleStringRepresentationContext
     */
    public static SimpleStringRepresentationContext create(
            Map<String,Object>          contextObjects,
            StringRepresentationContext delegate )
    {
        return new SimpleStringRepresentationContext( contextObjects, delegate );
    }
    
    /**
     * Private constructor for subclasses only, use factory method.
     * 
     * @param contextObjects the objects in the context
     * @param delegate the StringRepresentationContext to which to delegate if a context object could not be found locally
     */
    protected SimpleStringRepresentationContext(
            Map<String,Object>          contextObjects,
            StringRepresentationContext delegate )
    {
        theContextObjects = contextObjects;
        theDelegate       = delegate;
    }

    /**
     * Obtain an iterator over the keys.
     * 
     * @return iterator over the keys
     */
    public Iterator<String> keyIterator()
    {
        return theContextObjects.keySet().iterator();
    }
    
    /**
     * Obtain a specific value.
     * 
     * @param key the key
     * @return the value with this key
     */
    public Object get(
            String key )
    {
        Object ret = theContextObjects.get( key );
        if( ret == null && theDelegate != null ) {
            ret = theDelegate.get( key );
        }
        return ret;
    }
    
    /**
     * Add or change a specific value.
     * 
     * @param key the key
     * @param value the new value
     * @return the old value, if any
     */
    public Object put(
            String key,
            Object value )
    {
        Object ret = theContextObjects.put( key, value );
        return ret;
    }

    /**
     * The objects in the context.
     */
    protected Map<String,Object> theContextObjects;
    
    /**
     * The delegate StringRepresentationContext, if any.
     */
    protected StringRepresentationContext theDelegate;
}
