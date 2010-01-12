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

package org.infogrid.lid.local.regex;

import java.util.regex.Pattern;
import org.infogrid.lid.credential.AbstractLidPasswordCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;

/**
 * A password LidCredentialType that is validated against a regular expression.
 */
public class RegexLidPasswordCredentialType
    extends
        AbstractLidPasswordCredentialType
{
    /**
     * Factory method.
     *
     * @param passwordRegex the password regular expression
     * @return the created RegexLidPasswordCredentialType
     */
    public static RegexLidPasswordCredentialType create(
            String passwordRegex )
    {
        RegexLidPasswordCredentialType ret = new RegexLidPasswordCredentialType( Pattern.compile( passwordRegex ) );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param passwordRegex the password regular expression
     * @return the created RegexLidPasswordCredentialType
     */
    public static RegexLidPasswordCredentialType create(
            Pattern passwordRegex )
    {
        RegexLidPasswordCredentialType ret = new RegexLidPasswordCredentialType( passwordRegex );
        return ret;
    }

    /**
     * Constructor, for subclasses only, use factory method.
     *
     * @param passwordRegex the password regular expression
     */
    protected RegexLidPasswordCredentialType(
            Pattern passwordRegex )
    {
        thePasswordRegex = passwordRegex;
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
        String givenPassword = request.getPostedArgument( "lid-credential" );

        if( thePasswordRegex.matcher( givenPassword ).matches()) {
            return;
        }
        throw new LidInvalidCredentialException( subject.getIdentifier(), this );
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
        if( other instanceof RegexLidPasswordCredentialType ) {
            RegexLidPasswordCredentialType realOther = (RegexLidPasswordCredentialType) other;

            return thePasswordRegex.equals( realOther.thePasswordRegex );
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
        return thePasswordRegex.hashCode();
    }

    /**
     * The password regular expression.
     */
    protected Pattern thePasswordRegex;
}
