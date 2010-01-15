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

package org.infogrid.lid.store;

import org.infogrid.crypto.hashedpassword.HashedPasswordUtils;
import org.infogrid.lid.credential.AbstractLidPasswordCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;

/**
 * A password LidCredentialType that is validated against hashed passwords contained in a Store.
 */
public class StoreLidPasswordCredentialType
    extends
        AbstractLidPasswordCredentialType
{
    /**
     * Factory method.
     *
     * @return the created RegexLidPasswordCredentialType
     */
    public static StoreLidPasswordCredentialType create()
    {
        StoreLidPasswordCredentialType ret = new StoreLidPasswordCredentialType();
        return ret;
    }

    /**
     * Constructor, for subclasses only, use factory method.
     */
    protected StoreLidPasswordCredentialType()
    {
        // nothing
    }

    /**
     * Determine whether the request contains a valid LidCredentialType of this type
     * for the given subject.
     *
     * @param request the request
     * @param subject the subject
     * @throws LidInvalidCredentialException thrown if the contained LidCdedentialType is not valid for this subject
     */
    public void checkCredential(
            SaneRequest   request,
            HasIdentifier subject )
        throws
            LidInvalidCredentialException
    {
        if( !( subject instanceof StoreLidLocalPersona )) {
            throw new LidInvalidCredentialException(
                    subject.getIdentifier(),
                    this,
                    new ClassCastException( "HasIdentifier is " + subject + ", not instance of " + StoreLidLocalPersona.class.getName() ));
        }
        StoreLidLocalPersona realSubject            = (StoreLidLocalPersona) subject;
        String               storedHashedCredential = realSubject.getCredentialFor( this );

        if( storedHashedCredential == null ) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this );
        }
        byte [] rawHashedCredential = HashedPasswordUtils.string2raw( storedHashedCredential );

        String givenPassword = request.getPostedArgument( "lid-credential" );

        if( !HashedPasswordUtils.isValid( givenPassword, rawHashedCredential )) {
            throw new LidInvalidCredentialException( subject.getIdentifier(), this );
        }
        // else return without further complications
    }

    /**
     * Determine equality.
     *
     * @param other the objects to compare against
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof StoreLidPasswordCredentialType ) {
            return true;
        }
        return false;
    }

    /**
     * Hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return getClass().hashCode();
    }
}
