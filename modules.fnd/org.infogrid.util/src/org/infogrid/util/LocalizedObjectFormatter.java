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
 * Interface to convert parameters in a localized text String to a consistent
 * String representation. This is used by implementations of
 * {@link LocalizedObject LocalizedObject}. 
 */
public interface LocalizedObjectFormatter
{
    /**
     * Convert an Object to a String representation.
     *
     * @param o the Object
     * @return String representation of the Object
     */
    public abstract String asLocalizedString(
            Object o );
}
