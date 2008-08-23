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

/**
 * Collects the XML tags for the LidLocalPersonaWithCredentials mapping.
 */
public interface LidLocalPersonaWithCredentialsTags
{
    /** Top-level tag representing a LidLocalPersonaWithCredentials. */
    public static final String PERSONA_TAG = "persona";

    /** Tag for an identifier. */
    public static final String IDENTIFIER_TAG = "id";

    /** Tag for an identity attribute. */
    public static final String ATTRIBUTE_TAG = "attribute";
    
    /** Tag for a credential. */
    public static final String CREDENTIAL_TAG = "credential";
}
