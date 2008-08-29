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
 * Knows how to create unique identifiers, mostly derived from the current time.
 * It makes sure that even if invoked several times within a short amount of time,
 * unique identifiers are being returned.
 */
public class UniqueIdentifierCreator
{
    /**
     * Factory method.
     *
     * @return the created UniqueIdentifierCreator
     */
    public static UniqueIdentifierCreator create()
    {
        return new UniqueIdentifierCreator();
    }
    
    /**
     * Constructor, for subclasses only.
     */
    protected UniqueIdentifierCreator()
    {
        // noop
    }

    /**
     * Create a unique identifier.
     *
     * @return the unique Identifier
     */
    public synchronized long createUniqueIdentifier()
    {
        long currentDate = System.currentTimeMillis();
        if( currentDate > theMostRecent ) {
            theMostRecent = currentDate;
        } else {
            ++theMostRecent;
        }
        return theMostRecent;
    }

    /**
     * The most recently returned unique identifier.
     */
    protected long theMostRecent = 0;
}
