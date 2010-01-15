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

package org.infogrid.util;

/**
 * Uses a very simple, time-based algorithm to create unique longs.
 */
public class SimpleTimeBasedUniqueLongGenerator
    implements
        UniqueTokenGenerator<Long>
{
    /**
     * Factory method.
     *
     * @return the created UniqueIdentifierCreator
     */
    public static SimpleTimeBasedUniqueLongGenerator create()
    {
        return new SimpleTimeBasedUniqueLongGenerator();
    }
    
    /**
     * Constructor, for subclasses only.
     */
    protected SimpleTimeBasedUniqueLongGenerator()
    {
        // noop
    }

    /**
     * Create a unique token.
     *
     * @return the unique token
     */
    public synchronized Long createUniqueToken()
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
