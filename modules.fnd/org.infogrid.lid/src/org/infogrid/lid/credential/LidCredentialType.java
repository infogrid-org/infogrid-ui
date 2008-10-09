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

import org.infogrid.lid.LidLocalPersona;
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
//    /**
//     * Determine the computable short name of this LidCredentialType.
//     * 
//     * @return the computable short name
//     */
//    public abstract String getShortName();
//
    /**
     * Determine the computable full-qualified name of this LidCredentialType.
     * 
     * @return the computable full name
     */
    public abstract String getFullName();

//    /**
//     * Perform a check of the validity of a presented credential.
//     * 
//     * @param identifier the identifier for which credential was presented
//     * @param request the incoming request carrying the presented credential
//     * @param persona what is known locally about the persona
//     * @throws LidInvalidCredentialException thrown if the credential was invalid
//     */
//    public abstract void checkCredential(
//            String          identifier,
//            SaneRequest     request,
//            LidLocalPersona persona )
//        throws
//            LidInvalidCredentialException;
}
