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

package org.infogrid.httpd;

/**
 * An HttpAccessManager determines whether or not a client has the right to access
 * a certain resource.
 */
public interface HttpAccessManager
{
    /**
     * Determine whether this Request is allowed.
     *
     * @param req the incoming Request
     * @return true if this Request is allowed
     */
    public abstract boolean isAllowed(
            HttpRequest req );

    /**
     * Obtain a challenge for this disallowed Request, so that it will become
     * allowed if the challenge is met.
     *
     * @param req the incoming Request
     * @return the challenge, if any
     */
    public abstract String getChallengeFor(
            HttpRequest req );

}
