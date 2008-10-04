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

import java.util.HashMap;
import java.util.Map;
import org.infogrid.util.logging.Log;

/**
 * <p>A {@link SmartFactory SmartFactory} specifically for there case where the creation of a
 *    new object is a time-consuming operation. In this case, access to the PatientSmartFactory
 *    is only blocked for those Threads that attempt to obtain the same object; all
 *    other Threads can continue without being blocked.</p>
 * 
 * <p>It performs the actual object creation by delegating to another {@link Factory}.</p>
 *
 * <p>This currently blocks while objects are removed. This could potentially be made to
 *    be more friendly to Threads.</p>
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 * @param <A> the type of argument
 */
public class PatientSmartFactory<K,V,A>
        extends
            MSmartFactory<K, V, A>
{
    private static final Log log = Log.getLogInstance( PatientSmartFactory.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param delegateFactory the Factory that knows how to instantiate values
     * @param storage the storage to use
     */
    public PatientSmartFactory(
            Factory<K,V,A>  delegateFactory,
            CachingMap<K,V> storage )
    {
        super( delegateFactory, storage );

        theOngoingObjectCreations = new HashMap<K,Object>(); // FIXME? We don't know the right size here
    }

    /**
     * Create a new, or obtain an already existing value for a provided key.
     *
     * @param key the key for which we want to obtain a value
     * @param argument optional argument to pass through to the createFor method
     * @return the found or created value for this key
     * @throws FactoryException catch-all Exception, consider its cause
     */
    @Override
    @SuppressWarnings(value={"unchecked"})
    public V obtainFor(
            K key,
            A argument )
        throws
            FactoryException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".obtainFor( " + key + ", " + argument + " )" );
        }

        V ret;

        boolean weAreCreating       = false;
        Object  creationSyncObject  = null;

        synchronized( theKeyValueMap ) {
            ret = theKeyValueMap.get( key );
            if( ret == null ) {
                creationSyncObject = theOngoingObjectCreations.get( key );
                if( creationSyncObject == null ) {
                    weAreCreating = true;
                    creationSyncObject = new Object();
                    theOngoingObjectCreations.put( key, creationSyncObject );
                }
            }
        }

        if( ret == null ) {
            if( weAreCreating ) {
                synchronized( creationSyncObject ) {

                    try {
                        ret = theDelegateFactory.obtainFor( key, argument );
                        if( ret != null ) {
                            synchronized( theKeyValueMap ) {
                                theKeyValueMap.put( key, ret );
                            }
                        }
                    } finally {
                        theOngoingObjectCreations.remove( key );
                        creationSyncObject.notifyAll();
                    }
                }
            } else {
                synchronized( creationSyncObject ) {
                    try {
                        if( theOngoingObjectCreations.get( key ) != null ) {
                            creationSyncObject.wait();
                        }

                    } catch( InterruptedException ex ) {
                        log.error( ex );
                        return null;
                    }
                }
                ret = theKeyValueMap.get( key );
            }
        }
        if( ret instanceof FactoryCreatedObject ) {
            ((FactoryCreatedObject<K,V,A>) ret).setFactory( this );
        }
        if( weAreCreating ) {
            createdHook( key, ret, argument );
        }
        return ret;
    }
    
    /**
     * The currently ongoing creations of value. This is important
     * to avoid to create a value twice if two concurrent Threads
     * ask for the same value.
     */
    private Map<K,Object> theOngoingObjectCreations;
}
