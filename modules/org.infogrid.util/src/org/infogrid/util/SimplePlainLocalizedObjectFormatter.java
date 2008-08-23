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
 * A very simple LocalizedObjectFormatter for plain text.
 */
public class SimplePlainLocalizedObjectFormatter
        implements
            LocalizedObjectFormatter
{
    /**
     * Factory method. This always returns the same instance.
     * 
     * @return the created SimplePlainLocalizedObjectFormatter
     */
    public static SimplePlainLocalizedObjectFormatter create()
    {
        return theSingleton;
    }

    /**
     * Private constructor for subclasses only.
     */
    protected SimplePlainLocalizedObjectFormatter()
    {
        // no op
    }
    
    /**
     * Convert an Object to a String representation.
     *
     * @param o the Object
     * @return String representation of the Object
     */
    public String asLocalizedString(
            Object o )
    {
        if( o == null ) {
            return theResourceHelper.getResourceStringOrDefault( "NullString", "null" );

        } else if( o instanceof Class ) {
            Class realO = (Class) o;

            String ret = realO.getName();
            return ret;
           
        } else {
            return o.toString();
        }
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SimplePlainLocalizedObjectFormatter.class  );

    /**
     * Singleton instance.
     */
    private static SimplePlainLocalizedObjectFormatter theSingleton = new SimplePlainLocalizedObjectFormatter();
}
