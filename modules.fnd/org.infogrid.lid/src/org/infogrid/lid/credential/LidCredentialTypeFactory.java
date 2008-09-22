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

package org.infogrid.lid.credential;

import org.infogrid.util.Factory;
import org.infogrid.util.http.SaneRequest;

/**
 * Factory for LidCredentialTypes based on their names.
 */
public interface LidCredentialTypeFactory
    extends
        Factory<String,LidCredentialType,Void>
{
    /**
     * Determine the LidCredentialTypes referenced in this incoming request.
     * This method makes no statement about whether or not credentials provided in this
     * request are valid, only which types were used.
     * 
     * @param request the incoming request
     * @return the referenced LidCredentiapTypes
     */
    public LidCredentialType [] determineReferencedCredentialTypes(
            SaneRequest request );
}
