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

import org.infogrid.util.FactoryException;
import org.infogrid.util.context.Context;

/**
 * Captures the idea of identity provider or other authentication service.
 */
public interface LidAuthenticationService
{
    /**
     * Determine the URL to redirect the user to in order to make the LidAuthenticationService
     * confirm the identity of the user.
     *
     * @param clientIdentifier client's identifier, e.g. URL
     * @param returnToUrl the URL to return to at the relying party
     * @param realm the realm of the relying party
     * @param context the Context to use
     * @return the constructed redirect URL
     * @throws FactoryException thrown if a possibly required cryptographic association could not be created
     */
    public abstract String determineRedirectUrl(
            String  clientIdentifier,
            String  returnToUrl,
            String  realm,
            Context context )
        throws
            FactoryException;
}
