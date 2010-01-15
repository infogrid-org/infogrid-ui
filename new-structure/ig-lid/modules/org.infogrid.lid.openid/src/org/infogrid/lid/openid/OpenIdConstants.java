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

package org.infogrid.lid.openid;

import java.math.BigInteger;

/**
 * Defines constants related to OpenID.
 */
public interface OpenIdConstants
{
    /**
     * A pre-defined association type.
     */
    public static final String HMAC_SHA1 = "HMAC-SHA1";

    /**
     * A pre-defined session type.
     */
    public static final String DH_SHA1 = "DH-SHA1";

    /**
     * A pre-defined association type.
     */
    public static final String HMAC_SHA256 = "HMAC-SHA256";

    /**
     * A pre-defined session type.
     */
    public static final String DH_SHA256 = "DH-SHA256";

    /**
     * The default value for the Diffie-Helloman p parameter in OpenID.
     */
    public static final BigInteger DEFAULT_P = new BigInteger(
            "155172898181473697471232257763715539915724801966915404479707795314057629378541917580651227423698188993727816152646631438561595825688188889951272158842675419950341258706556549803580104870537681476726513255747040765857479291291572334510643245094715007229621094194349783925984760375594985848253359305585439638443" );

    /**
     * The default value for the Diffie-Helloman g parameter in OpenID.
     */
    public static final BigInteger DEFAULT_G = BigInteger.valueOf( 2 );
}
