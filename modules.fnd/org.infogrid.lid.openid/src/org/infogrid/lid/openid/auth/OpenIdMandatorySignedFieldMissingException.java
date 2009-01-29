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

package org.infogrid.lid.openid.auth;

import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.Identifier;
import org.infogrid.util.StringHelper;

/**
 * Thrown if a mandatory field in a V2 signature was not signed.
 */
public class OpenIdMandatorySignedFieldMissingException
        extends
            LidInvalidCredentialException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param missingFields the one or more missing fields
     * @param identifier the identifier for which an invalid credential was provided
     * @param type the type of credential that was invalid
     */
    public OpenIdMandatorySignedFieldMissingException(
            String []                    missingFields,
            Identifier                   identifier,
            AbstractOpenIdCredentialType type )
    {
        super( identifier, type );

        theMissingFields = missingFields;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        return new Object[] { theIdentifier, theType, theMissingFields };
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                        "theIdentifier",
                        "theType",
                        "theMissingFields"
                },
                new Object[] {
                        theIdentifier,
                        theType,
                        theMissingFields
                } );
    }

    /**
     * The fields that were supposed to be signed but were not.
     */
    protected String [] theMissingFields;
}

