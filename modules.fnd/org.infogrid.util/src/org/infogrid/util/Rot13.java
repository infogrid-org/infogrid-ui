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

package org.infogrid.util;

/**
 * Simplistic obfuscation via Rot13.
 */
public abstract class Rot13
{
    /**
     * Private constructor to prevent subclassing.
     */
    private Rot13()
    {
        // no op
    }
    
    /**
     * Perform a Rot13 operation
     * 
     * @param in the input String
     * @return return output String
     */
    public static String rot(
            String in )
    {
        StringBuffer tempReturn = new StringBuffer();
        int abyte = 0;
        for (int i = 0; i < in.length(); i++) {
            abyte = in.charAt(i);
            int cap = abyte & 32;
            abyte &= ~cap;
            abyte = ((abyte >= 'A') && (abyte <= 'Z') ? ((abyte - 'A' + 13) % 26 + 'A') : abyte) | cap;
            tempReturn.append((char) abyte);
        }
        return tempReturn.toString();
    }
}
