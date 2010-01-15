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

package org.infogrid.util.text;

/**
 * Factors out common functionality for Stringifiers that process numbers.
 *
 * @param <T> the type of the Objects to be stringified
 */
public abstract class NumberStringifier<T>
        extends
            AbstractStringifier<T>
{
    /**
     * Constructor.
     *
     * @param digits the number of digits
     * @param radix the radix to use, e.g. 16 for hexadecimal
     */
    protected NumberStringifier(
            int digits,
            int radix )
    {
        theDigits = digits;
        theRadix  = radix;
    }
    
    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     */
    public String format(
            String                         soFar,
            long                           arg,
            StringRepresentationParameters pars )
    {
        String ret;
        if( theDigits <= 0 ) {
            ret = String.valueOf( arg );

        } else {
            boolean negative;

            if( arg >= 0 ) {
                ret = Long.toString( arg, theRadix );
                negative = false;
            } else {
                ret = Long.toString( -arg, theRadix );
                negative = true;
            }
            StringBuilder prepend = new StringBuilder();
            if( negative ) {
                prepend.append( '-' );
            }
            for( int i=ret.length() ; i<theDigits ; ++i ) {
                prepend.append( '0' ); // leading zer0
            }
            prepend.append( ret );
            ret = prepend.toString();
        }
        ret = potentiallyShorten( ret, pars );
        return ret;

    }

    /**
     * Format an Object using this Stringifier. This may be null.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param pars collects parameters that may influence the String representation
     * @return the formatted String
     * @throws ClassCastException thrown if this Stringifier could not format the provided Object
     *         because the provided Object was not of a type supported by this Stringifier
     */
    public String attemptFormat(
            String                         soFar,
            Object                         arg,
            StringRepresentationParameters pars )
        throws
            ClassCastException
    {
        if( arg instanceof Short ) {
            return format( soFar, ((Short)arg).longValue(), pars );
        } else if( arg instanceof Long ) {
            return format( soFar, ((Long)arg).longValue(), pars );
        } else {
            return format( soFar, ((Integer)arg).longValue(), pars );
        }
    }
    
    /**
     * Is this a valid char for this Stringifier.
     *
     * @param pos position
     * @param min limits the considered String by this minimum position (inclusive)
     * @param max limits the considered String by this maximum position (exclusive)
     * @param s the String on whose position we find the char
     * @return true or false
     */
    boolean validChar(
            int    pos,
            int    min,
            int    max,
            String s )
    {
        int length = s.length();
        if( max > length ) {
            return false;
        }
        if( pos >= length ) {
            return false;
        }
        if( pos < min ) {
            return false;
        }

        char c = s.charAt( pos );
        c = Character.toLowerCase( c );

        if( pos == min && length > min ) {
            if( c == '+' || c == '-' ) {
                if( validChar( pos+1, min, max, s )) {
                    return true;
                } else {
                    return false;
                }
            }
        }

        boolean ret;
        if( c < '0' ) {
            ret = false;
        } else if( c <= '9' ) {
            if( c - '0' >= theRadix ) {
                ret = false;
            } else {
                ret = true;
            }
        } else if( c < 'a' ) {
            ret = false;
        } else if( c - 'a' + 10 >= theRadix ) {
            ret = false;
        } else {
            ret = true;
        }
        return ret;
    }
    
    /**
     * The number of digits to make. -1 means "don't pay any attention".
     */
    protected int theDigits;

    /**
     * The radix to use, e.g. 16 for hexadecimal.
     */
    protected int theRadix;
}
