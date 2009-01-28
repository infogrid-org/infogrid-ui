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

import org.infogrid.util.HasIdentifier;
import org.infogrid.util.LocalizedObject;
import org.infogrid.util.http.SaneRequest;

/**
 * Represents a credential type, such as a password. All classes implementing
 * this interface must have a static factory method with the following
 * signature: <code>public static LidCredentialType create( String credentialTypeName )</code>.
 */
public interface LidCredentialType
        extends
            LocalizedObject
{
    /**
     * Determine the computable full-qualified name of this LidCredentialType.
     * 
     * @return the computable full name
     */
    public abstract String getFullName();

    /**
     * Determine whether this LidCredentialType is contained in this request.
     *
     * @param request the request
     * @return true if this LidCredentialType is contained in this request
     */
    public abstract boolean isContainedIn(
            SaneRequest request );

    /**
     * Determine whether the request contains a valid LidCredentialType of this type
     * for the given subject.
     *
     * @param request the request
     * @param subject the subject
     * @throws LidInvalidCredentialException thrown if the contained LidCdedentialType is not valid for this subject
     */
    public abstract void checkCredential(
            SaneRequest   request,
            HasIdentifier subject )
        throws
            LidInvalidCredentialException;

    /**
     * Name of the URL parameter that indicates the LID credential type.
     */
    public static final String LID_CREDTYPE_PARAMETER_NAME = "lid-credtype";

    /**
     * Name of the URL parameter that indicates the LID credential.
     */
    public static final String LID_CREDENTIAL_PARAMETER_NAME = "lid-credential";
}
