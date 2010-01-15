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

package org.infogrid.jee.security.m;

import java.util.HashMap;
import org.infogrid.jee.security.FormToken;
import org.infogrid.jee.security.FormTokenService;

/**
 * A simplistic implementation of FormTokenService that stores its tokens in memory only.
 * This may not very useful for a production application, but is useful for testing.
 */
public class MFormTokenService
        implements
            FormTokenService
{
    /**
     * Factory method.
     *
     * @return the created MFormTokenService
     */
    public static MFormTokenService create()
    {
        MFormTokenService ret = new MFormTokenService();
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     */
    protected MFormTokenService()
    {
        // nothing
    }
    
    /**
     * Generate a new token.
     * 
     * @return the newly generated token
     */
    public String generateNewToken()
    {
        FormToken token = FormToken.createNew();

        theMap.put( token.getKey(), token );

        return token.getKey();
    }
    
    /**
     * Validate a presented token. It is up to the implementation to decide whether
     * or not to invalidate presented tokens.
     * 
     * @param key the key of the presented token
     * @return true if the token is valid, false if it is not valid or unknown.
     */
    public boolean validateToken(
            String key )
    {
        if( key == null ) {
            return false;
        }

        // regardless, we remove tokens passed into here
        FormToken token = theMap.remove( key );
        if( token == null ) {
            return false;
        }
        if( !token.isStillValid() ) {
            return false;
        }
        return true;
    }

    /**
     * The Map that stores the data.
     */
    protected HashMap<String,FormToken> theMap = new HashMap<String,FormToken>();
}
