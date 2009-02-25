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

import java.util.Random;

/**
 * Generates unique Strings.
 */
public class UniqueStringGenerator
        implements
            UniqueTokenGenerator<String>
{
    /**
     * Factory method.
     *
     * @param length the length of the generated Strings
     * @return the created UniqueStringGenerator
     */
    public static UniqueStringGenerator create(
            int length )
    {
        return new UniqueStringGenerator( length );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param length the length of the generated Strings
     */
    protected UniqueStringGenerator(
            int length )
    {
        theLength = length;

        theRandom = new Random(theSeedGenerator.createUniqueToken() );
    }

    /**
     * Create a unique token.
     *
     * @return the unique token
     */
    public synchronized String createUniqueToken()
    {
        char [] buf = new char[ theLength ];
        for( int i=0 ; i<theLength ; ++i ) {
            int n = theRandom.nextInt( 64 );
            buf[i] = toChar( n );
        }
        return new String( buf );
    }

    /**
     * Convert a 6-bit number to a printable character.
     *
     * @param n the number
     * @return the character
     */
    protected char toChar(
            int n )
    {
        if( n < 0 || n >= 64 ) {
            throw new IllegalArgumentException( "Cannot convert " + n );
        }
        if( n < 26 ) {
            return (char)( 'A' + n );
        }
        if( n < 52 ) {
            return (char) ( 'a' + ( n-26 ));
        }
        if( n < 62 ) {
            return (char) ( '0' + (n-52 ));
        }
        if( n == 63 ) {
            return '-';
        } else {
            return '_';
        }
    }

    /**
     * The length of Strings being generated.
     */
    protected int theLength;

    /**
     * The random generator.
     */
    protected Random theRandom;

    /**
     * The generator of seeds.
     */
    protected static UniqueTokenGenerator<Long> theSeedGenerator
            = SimpleTimeBasedUniqueLongGenerator.create();
}
