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

package org.infogrid.model.primitives.text;

import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.util.text.AbstractStringifier;
import org.infogrid.util.text.StringRepresentationParameters;

/**
 * Stringifies the allowed MIME types of a BlobDataType.
 */
public class BlobMimeOptionsHtmlStringifier
        extends
            AbstractStringifier<BlobDataType>
{
    /**
     * Factory method.
     *
     * @return the created EnumeratedValueStringifier
     */
    public static BlobMimeOptionsHtmlStringifier create()
    {
        return new BlobMimeOptionsHtmlStringifier( null, null, null );
    }

    /**
     * Factory method.
     *
     * @param middle the string to insert in the middle
     * @return the created EnumeratedValueStringifier
     */
    public static BlobMimeOptionsHtmlStringifier create(
            String middle )
    {
        return new BlobMimeOptionsHtmlStringifier( null, middle, null );
    }

    /**
     * Factory method.
     *
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     * @return the created EnumeratedValueStringifier
     */
    public static BlobMimeOptionsHtmlStringifier create(
            String start,
            String middle,
            String end )
    {
        return new BlobMimeOptionsHtmlStringifier( start, middle, end );
    }

    /**
     * Private constructor for subclasses only, use factory method.
     *
     * @param start the string to insert at the beginning
     * @param middle the string to insert in the middle
     * @param end the string to append at the end
     */
    protected BlobMimeOptionsHtmlStringifier(
            String start,
            String middle,
            String end )
    {
        theStart    = start;
        theMiddle   = middle;
        theEnd      = end;
    }

    /**
     * Obtain the start String, if any.
     *
     * @return the start String, if any
     */
    public String getStart()
    {
        return theStart;
    }

    /**
     * Obtain the middle String, if any.
     *
     * @return the middle String, if any
     */
    public String getMiddle()
    {
        return theMiddle;
    }

    /**
     * Obtain the end String, if any.
     *
     * @return the end String, if any
     */
    public String getEnd()
    {
        return theEnd;
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
            BlobDataType                   arg,
            StringRepresentationParameters pars )
    {
        String []     values = arg.getMimeTypes();
        StringBuilder ret    = new StringBuilder();
        String        sep    = theStart;

        for( int i=0 ; i<values.length ; ++i ) {
            if( sep != null ) {
                ret.append( sep );
            }
            ret.append( values[i] );

            sep = theMiddle;
        }
        if( theEnd != null ) {
            ret.append( theEnd );
        }
        return potentiallyShorten( ret.toString(), pars );
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
        if( arg instanceof BlobDataType ) {
            return format( soFar, (BlobDataType) arg, pars );
        } else if( arg instanceof PropertyType ) {
            return format( soFar, (BlobDataType) ((PropertyType) arg).getDataType(), pars );
        } else {
            throw new ClassCastException( "Cannot stringify " + arg );
        }
    }

    /**
     * The String to insert at the beginning. May be null.
     */
    protected String theStart;

    /**
     * The String to insert when joining two elements. May be null.
     */
    protected String theMiddle;

    /**
     * The String to append at the end. May be null.
     */
    protected String theEnd;
}