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
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.AbstractFactoryCreatedObject;

/**
 * Pairs a LidLocalPersona with zero or more credentials, for storage in a LidIdentityManager.
 */
public class LidLocalPersonaWithCredentials
        extends
            AbstractFactoryCreatedObject<String,LidLocalPersonaWithCredentials,AttributesCredentials>
{
    /**
     * Factory method.
     *
     * @param persona the LidLocalPersona
     * @param credentials the credentials owned by this LidLocalPersona, as Map from credential type to credential value
     * @return the created LidLocalPersonaWithCredentials
     */
    public static LidLocalPersonaWithCredentials create(
            LidLocalPersona               persona,
            Map<LidCredentialType,String> credentials )
    {
        LidLocalPersonaWithCredentials ret = new LidLocalPersonaWithCredentials( persona, credentials );
        return ret;
    }

    /**
     * Convenienice factory method.
     *
     * @param identifier the identifier of the user
     * @param attributes the attributes owned by the user with this identifier
     * @param credentials the credentials owned by the user with this identifier, as Map from credential type to credential value
     * @return the created LidLocalPersonaWithCredentials
     */
    public static LidLocalPersonaWithCredentials create(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
    {
        LidLocalPersona                persona = LidLocalPersona.create( identifier, attributes );
        LidLocalPersonaWithCredentials ret     = new LidLocalPersonaWithCredentials( persona, credentials );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param persona the LidLocalPersona
     * @param credentials the credentials owned by this LidLocalPersona, as Map from credential type to credential value
     */
    protected LidLocalPersonaWithCredentials(
            LidLocalPersona               persona,
            Map<LidCredentialType,String> credentials )
    {
        thePersona     = persona;
        theCredentials = credentials;
    }
    
    /**
     * Obtain the key for that was used to create this object by the Factory.
     *
     * @return the key
     */
    public String getFactoryKey()
    {
        return thePersona.getIdentifier();
    }

    /**
     * Obtain the LidLocalPersona component.
     * 
     * @return the LidLocalPersona
     */
    public LidLocalPersona getPersona()
    {
        return thePersona;
    }

    /**
     * Obtain the credential for a given credential type.
     * 
     * @param type the credential type
     * @return the credential, if any
     */
    public String getCredentialFor(
            LidCredentialType type )
    {
        String ret = theCredentials.get( type );
        return ret;
    }

    /**
     * Set the credential for a given credential type.
     * 
     * @param type the credential type
     * @param credential the new value for the credential
     */
    public void setCredentialFor(
            LidCredentialType type,
            String            credential )
    {
        theCredentials.put( type, credential );
    }

    /**
     * Obtain the credentials.
     * 
     * @return the credentials
     */
    public Map<LidCredentialType,String> getCredentials()
    {
        return theCredentials;
    }

    /**
     * The LidLocalPersona.
     */
    protected LidLocalPersona thePersona;
    
    /**
     * Their credentials, keyed by credential type.
     */
    protected Map<LidCredentialType,String> theCredentials;
}
