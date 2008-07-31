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

package org.infogrid.lid.userpass;

/**
 * Defines how to store and validate passwords.
 */
public interface LidPasswordManager
{
    /**
     * Create or change a username / password pair.
     * 
     * @param username the user name
     * @param password the password
     */
    public void updateUserPass(
            String username,
            String password );
    
    /**
     * Determine whether a record with the given username exists.
     * 
     * @param username the user name
     * @return true if a record exists
     */
    public boolean isUser(
            String username );
    
    /**
     * Delete a user and their password.
     * 
     * @param username the user name
     */
    public void deleteUser(
            String username );
    
    /**
     * Validate a user's password.
     * 
     * @param username the user name
     * @param password the password
     * @return true if this user has this password
     */
    public boolean isUserPass(
            String username,
            String password );
}
