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

package org.infogrid.lid.gpg;

import org.infogrid.util.Identifier;

/**
 * A public/private key pair for LID purposes.
 */
public class LidKeyPair
{
    /**
     * Constructor.
     * 
     * @param lid the LID identifier
     * @param publicKey the public key
     * @param privateKey the private key
     */
    public LidKeyPair(
            Identifier lid,
            String     publicKey,
            String     privateKey )
    {
        theLid        = lid;
        thePublicKey  = publicKey;
        thePrivateKey = privateKey;
    }
    
    /**
     * Obtain the LID identifier.
     * 
     * @return the LID identifier
     */
    public Identifier getLid()
    {
        return theLid;
    }
    
    /**
     * Obtain the public key.
     * 
     * @return the public key
     */
    public String getPublicKey()
    {
        return thePublicKey;
    }

    /**
     * Obtain the private key.
     * 
     * @return the private key
     */
    public String getPrivateKey()
    {
        return thePrivateKey;
    }

    /**
     * The LID identifier.
     */
    protected Identifier theLid;
    
    /**
     * The public key.
     */
    protected String thePublicKey;
    
    /**
     * The private key.
     */
    protected String thePrivateKey;
}
