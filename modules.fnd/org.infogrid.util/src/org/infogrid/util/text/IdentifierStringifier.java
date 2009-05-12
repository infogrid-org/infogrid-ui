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

import org.infogrid.util.Identifier;

/**
 * Stringifies an Identifier.
 */
public class IdentifierStringifier
        extends
            AbstractStringifier<Identifier>
{
    /**
     * Factory method without prefix or postfix.
     *
     * @return the created IdentifierStringifier
     */
    public static IdentifierStringifier create()
    {
        return new IdentifierStringifier( null, null );
    }

    /**
     * Factory method with prefix or postfix.
     *
     * @param prefix the prefix for the identifier, if any
     * @param postfix the postfix for the identifier, if any
     * @return the created IdentifierStringifier
     */
    public static IdentifierStringifier create(
            String prefix,
            String postfix )
    {
        return new IdentifierStringifier( prefix, postfix );
    }

    /**
     * No-op constructor. Use factory method.
     *
     * @param prefix the prefix for the identifier, if any
     * @param postfix the postfix for the identifier, if any
     */
    protected IdentifierStringifier(
            String prefix,
            String postfix )
    {
        thePrefix  = prefix;
        thePostfix = postfix;
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
            Identifier                     arg,
            StringRepresentationParameters pars )
    {
        if( arg == null ) {
            return "null";
        }

        String ext = escape( arg.toExternalForm() );

        if( thePrefix == null && thePostfix == null ) {
            return ext;

        } else {
            StringBuilder buf = new StringBuilder();
            if( thePrefix != null ) {
                buf.append( thePrefix );
            }
            buf.append( ext );
            if( thePostfix != null ) {
                buf.append( thePrefix );
            }
            return buf.toString();
        }
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
        return format( soFar, (Identifier) arg, pars );
    }

    /**
     * Overridable method to possibly escape a String first.
     *
     * @param s the String to be escaped
     * @return the escaped String
     */
    protected String escape(
            String s )
    {
        return s;
    }

    /**
     * Overridable method to possibly unescape a String first.
     *
     * @param s the String to be unescaped
     * @return the unescaped String
     */
    protected String unescape(
            String s )
    {
        return s;
    }

    /**
     * Default format. This evaluates and processes the COLLOQUIAL parameter.
     *
     * @param input the full URL
     * @param pars collects parameters that may influence the String representation
     * @return the potentially formatted URL
     */
    public static String defaultFormat(
            String                         input,
            StringRepresentationParameters pars )
    {
        if( pars == null ) {
            return input;
        }
        Boolean colloquial = (Boolean) pars.get( StringRepresentationParameters.COLLOQUIAL );
        if( colloquial == null ) {
            return input;
        }
        if( !colloquial.booleanValue() ) {
            return input;
        }
        if( input == null ) {
            return null;
        }

        final String PREFIX = "http://";
        if( input.startsWith( PREFIX )) {
            String ret = input.substring( PREFIX.length() );
            int    slash = ret.indexOf( '/' );
            if( slash == ret.length()-1 ) {
                // the first found slash is the last character
                ret = ret.substring( 0, slash );
            }
            return ret;
        }
        return input;
    }

    /**
     * The prefix, if any.
     */
    protected String thePrefix;

    /**
     * The postfix, if any.
     */
    protected String thePostfix;
}
