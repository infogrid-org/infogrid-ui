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

package org.infogrid.lid.userpass.store;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.infogrid.lid.userpass.LidPasswordManager;
import org.infogrid.store.Store;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreValue;
import org.infogrid.util.logging.Log;

/**
 * A Store implementation of LidPasswordManager. This implementation
 * does not hold passwords in memory for security reasons.
 */
public class StoreLidPasswordManager
        implements
            LidPasswordManager
{
    private static final Log log = Log.getLogInstance( StoreLidPasswordManager.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param store the Store to use
     * @return the created StoreLidPasswordManager
     */
    public static StoreLidPasswordManager create(
            Store store )
    {
        StoreLidPasswordManager ret = new StoreLidPasswordManager( store );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param store the Store to use
     */
    protected StoreLidPasswordManager(
            Store store )
    {
        theStore = store;
    }
    
    /**
     * Create or change a username / password pair.
     * 
     * @param username the user name
     * @param password the password
     */
    public void updateUserPass(
            String username,
            String password )
    {
        try {
            long now = System.currentTimeMillis();

            theStore.putOrUpdate(
                    username,
                    ENCODING,
                    now,
                    now,
                    -1L,
                    -1L,
                    password.getBytes( CHARSET ));

        } catch( UnsupportedEncodingException ex ) {
            log.error( ex );
        } catch( IOException ex ) {
            log.error( ex );
        }
    }
    
    /**
     * Determine whether a record with the given username exists.
     * 
     * @param username the user name
     * @return true if a record exists
     */
    public boolean isUser(
            String username )
    {
        try {
            StoreValue found = theStore.get( username );

            return found != null;

        } catch( StoreKeyDoesNotExistException ex ) {
            return false;
            
        } catch( IOException ex ) {
            log.error( ex );
            return false;
        }
    }
    
    /**
     * Delete a user and their password.
     * 
     * @param username the user name
     */
    public void deleteUser(
            String username )
    {
        try {
            theStore.delete( username );

        } catch( StoreKeyDoesNotExistException ex ) {
            // do nothing

        } catch( IOException ex ) {
            log.error( ex );
        }
    }
    
    /**
     * Validate a user's password.
     * 
     * @param username the user name
     * @param password the password
     * @return true if this user has this password
     */
    public boolean isUserPass(
            String username,
            String password )
    {
        try {
            StoreValue found = theStore.get( username );
            if( found == null ) {
                return false;
            }

            byte [] givenBytes = password.getBytes( CHARSET );
            byte [] foundBytes = found.getData();

            if( givenBytes.length != foundBytes.length ) {
                return false;
            }
            for( int i=0 ; i<givenBytes.length ; ++i ) {
                if( givenBytes[i] != foundBytes[i] ) {
                    return false;
                }
            }
            return true;

        } catch( StoreKeyDoesNotExistException ex ) {
            return false;

        } catch( UnsupportedEncodingException ex ) {
            log.error( ex );
            return false;

        } catch( IOException ex ) {
            log.error( ex );
            return false;
        }
    }
    
    /**
     * The Store to use.
     */
    protected Store theStore;
    
    /**
     * The encoding to use.
     */
    public static final String ENCODING = StoreLidPasswordManager.class.getName();
    
    /**
     * THe characterset to use.
     */
    protected static final String CHARSET = "UTF-8";
}
