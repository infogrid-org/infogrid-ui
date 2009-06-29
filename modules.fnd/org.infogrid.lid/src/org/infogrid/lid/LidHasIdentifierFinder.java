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

package org.infogrid.lid;

import org.infogrid.lid.local.LidLocalPersonaUnknownException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.HasIdentifierFinder;
import org.infogrid.util.InvalidIdentifierException;
import org.infogrid.util.http.SaneRequest;

/**
 * Given a request, this interface is supported by objects that know how to find
 * the corresponding requested HasIdentifier.
 */
public interface LidHasIdentifierFinder
        extends
            HasIdentifierFinder
{
    /**
     * Find the LidResource, or null.
     * 
     * @param request the incoming request
     * @return the found LidResource, or null
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     * @throws InvalidIdentifierException thrown if an invalid Identifier was provided
     */
    public HasIdentifier findFromRequest(
            SaneRequest request )
        throws
            LidLocalPersonaUnknownException,
            InvalidIdentifierException;
}
