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

/**
 * Represents a locally provisioned LidPersona.
 */
public interface LidLocalPersona
        extends
            LidPersona,
            LidResource
{
    /**
     * Set an attribute of the persona.
     * 
     * @param key the name of the attribute
     * @param value the value of the attribute
     */
    public void setAttribute(
            String key,
            String value );

//    /**
//     * Obtain the credential for a given credential type.
//     * 
//     * @param type the credential type
//     * @return the credential, if any
//     */
//    public String getCredentialFor(
//            LidCredentialType type );
//
//    /**
//     * Set the credential for a given credential type.
//     * 
//     * @param type the credential type
//     * @param credential the new value for the credential
//     */
//    public void setCredentialFor(
//            LidCredentialType type,
//            String            credential );
//
}
