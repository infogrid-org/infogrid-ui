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

package org.infogrid.lid.store;

import java.util.Map;
import java.util.Set;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.local.AbstractLidLocalPersona;
import org.infogrid.util.Identifier;

/**
 * Implementation of LidLocalPersona for the LidLocalPersonaManager.
 */
public class StoreLidLocalPersona
        extends
            AbstractLidLocalPersona
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     * @param credentials the credentials available to authenticate this LidLocalPersona
     */
    protected StoreLidLocalPersona(
            Identifier                    identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
    {
        super( identifier, attributes );

        theCredentials = credentials;
    }

    /**
     * Obtain the set of available credential types.
     *
     * @return the set of available credential types
     */
    public Set<LidCredentialType> getCredentialTypes()
    {
        return theCredentials.keySet();
    }

    /**
     * Obtain a specific credential.
     * 
     * @param type the LidCredentialType for which the credential is to be obtained
     * @return the credential, or null
     */
    public String getCredentialFor(
            LidCredentialType type )
    {
        return theCredentials.get( type );
    }

    /**
     * Our credentials.
     */
    protected Map<LidCredentialType,String> theCredentials;
}
