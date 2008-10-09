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

package org.infogrid.lid;

import java.util.Map;
import java.util.Set;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.StringHelper;

/**
 * Collects features of LidLocalPersona that are common to many implementations.
 */
public abstract class AbstractLidLocalPersona
        extends
            AbstractLidPersona
        implements
            LidLocalPersona
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor for subclasses only.
     * 
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     * @param credentialTypes the credential types available to authenticate this LidLocalPersona
     */
    protected AbstractLidLocalPersona(
            String                 identifier,
            Map<String,String>     attributes,
            Set<LidCredentialType> credentialTypes )
    {
        super( identifier, attributes );

        theCredentialTypes = credentialTypes;
    }
    
    /**
     * Determine whether this LidPersona is hosted locally or remotely.
     * 
     * @return true if the LidPersona is hosted locally
     */
    public boolean isHostedLocally()
    {
        return true;
    }

    /**
     * Obtain the credential types available.
     * 
     * @return the credential types
     */
    public Set<LidCredentialType> getCredentialTypes()
    {
        return theCredentialTypes;
    }

    /**
     * Translate to String form, for debugging.
     * 
     * @return String form
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "identifier",
                    "attributes"
                },
                new Object[] {
                    theIdentifier,
                    theAttributes
                });
    }

    /**
     * The credential types available.
     * 
     * @return the credential types
     */
    protected Set<LidCredentialType> theCredentialTypes;
}
