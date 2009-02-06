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

package org.infogrid.lid.openid;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import org.infogrid.util.CachingMap;
import org.infogrid.util.Invocable;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.Log;

/**
 * Commonly used functionality for OpenIdIdpSideAssociationManagers.
 */
public abstract class AbstractOpenIdIdpSideAssociationManager
        implements
            OpenIdIdpSideAssociationManager
{
    private static final Log log = Log.getLogInstance( AbstractOpenIdIdpSideAssociationManager.class  ); // our own, private logger

    /**
     * Constructor, for subclasses only.
     *
     * @param associationHandlePrefix prefix for the generated association handles
     * @param associationDuration validity period for new associations
     * @param storage storage for the associations
     */
    protected AbstractOpenIdIdpSideAssociationManager(
            String                                      associationHandlePrefix,
            long                                        associationDuration,
            CachingMap<String,OpenIdIdpSideAssociation> storage )
    {
        theAssociationHandlePrefix = associationHandlePrefix;
        theAssociationDuration     = associationDuration;
        theAssociations            = storage;
    }

    /**
     * Create a new association.
     * 
     * @return the created association
     */
    public OpenIdIdpSideAssociation create()
    {
        long   now    = System.currentTimeMillis();
        String handle = createNewAssociationHandle( String.valueOf( now ));
        
        OpenIdIdpSideAssociation ret = create( handle, now );
        return ret;
    }

    /**
     * Create a new association.
     * 
     * @param handle the handle for the to-be-created association
     * @param timeCreated the creation time for the association
     * @return the created association
     */
    public OpenIdIdpSideAssociation create(
            String handle, 
            long   timeCreated )
    {
        long    timeExpires = timeCreated + theAssociationDuration;
        byte [] secret      = CryptUtils.randomWithBits( 160 );
        
        OpenIdIdpSideAssociation ret = OpenIdIdpSideAssociation.create(
                handle,
                secret,
                timeCreated,
                timeExpires );
        
        Object found = theAssociations.put( ret.getAssociationHandle(), ret );
        if( found != null ) {
            log.error( "Had association with this handle already: " + found );
        }
        
        return ret;
    }
            
    /**
     * Overridable method to create an association handle. The returned value must guaranteed
     * to be unique.
     * 
     * @param seed a seed value for the handle
     * @return the new association handle
     */
    protected String createNewAssociationHandle(
            String seed )
    {
        StringBuffer associationHandleBuf = new StringBuffer( 64 );
        associationHandleBuf.append( theAssociationHandlePrefix );
        associationHandleBuf.append( seed );

        return associationHandleBuf.toString();
    }

    /**
     * Obtain an already existing value for a provided key.
     * 
     * @param key the key for which we want to obtain a value
     * @return the already existing value, or null
     */
    public OpenIdIdpSideAssociation get(
            String key )
    {
        return theAssociations.get( key );
    }

    /**
     * Obtain the keys for an existing value. This is the opposite operation
     * of {@link #get}. Depending on the implementation of this interface,
     * this operation may take a long time.
     * 
     * @param value the value whose keys need to be found
     * @return an Iterator over the keys
     */
    public Iterator<String> reverseGet(
            OpenIdIdpSideAssociation value )
    {
        return theAssociations.reverseGet( value );
    }

    /**
     * Obtain a set of the currently known keys. This follows the pattern in
     * java.util.HashMap.
     *
     * @return a set of the currently known keys
     */
    public Set<String> keySet()
    {
        return theAssociations.keySet();
    }

    /**
     * Obtain a Collection of the currently known values. This follows the pattern in
     * java.util.HashMap.
     *
     * @return a set of the currently known values
     */
    public Collection<OpenIdIdpSideAssociation> values()
    {
        return theAssociations.values();
    }

    /**
     * Determine whether the number of key-value pairs in this SmartFactory is zero.
     *
     * @return true if there are no key-value paris in this SmartFactory
     */
    public boolean isEmpty()
    {
        return theAssociations.isEmpty();
    }

    /**
     * The number of known key-value pairs in this SmartFactory at this time.
     *
     * @return the number of known key-value pairs
     */
    public int size()
    {
        return theAssociations.size();
    }

    /**
     * Add a new key and a new value.
     *
     * @param key the key
     * @param value the value for the key
     * @return the old value at this key, if any
     */
    public OpenIdIdpSideAssociation put(
            String                   key,
            OpenIdIdpSideAssociation value )
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Remove a key-value pair that was previously created. This does not affect
     * values that are currently still being constructed. The semantics of
     * &quot;remove&quot; for a SmartFactory imply &quot;deletion&quot; of the
     * object as well. Correspondingly, if the removed Object supports the
     * {@link org.infogrid.util.LiveDeadObject LiveDeadObject} object, this
     * implementation will invoke the <code>die()</code> method there.
     *
     * @param key the key of the key-value pair to be removed
     * @return the value of the key-value pair to be removed, if found
     */
    public OpenIdIdpSideAssociation remove(
            String key )
    {
        OpenIdIdpSideAssociation ret = theAssociations.remove( key );
        return ret;
    }

    /**
     * Remove a key-value pair that was previously created. This does not affect
     * values that are currently still being constructed. The semantics of
     * &quot;remove&quot; for a SmartFactory imply &quot;deletion&quot; of the
     * object as well. The provided cleanupCode can be used to implement those
     * semantics, e.g. in order to invoke the die() method.
     *
     * @param key the key of the key-value pair to be removed
     * @param cleanupCode the cleanup code to run, if any
     * @return the value of the key-value pair to be removed, if found
     */
    public OpenIdIdpSideAssociation remove(
            String                                   key,
            Invocable<OpenIdIdpSideAssociation,Void> cleanupCode )
    {
        OpenIdIdpSideAssociation ret = theAssociations.remove( key, cleanupCode );
        return ret;
    }
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance(  AbstractOpenIdIdpSideAssociationManager.class  );
    
    /**
     * Default duration from the time a new association is created to the time it expires.
     */
    protected long theAssociationDuration;

    /**
     * Association storage.
     */
    protected CachingMap<String,OpenIdIdpSideAssociation> theAssociations;
    
    /**
     * The String that forms the root of the AssociationHandle.
     */
    protected String theAssociationHandlePrefix;
}
