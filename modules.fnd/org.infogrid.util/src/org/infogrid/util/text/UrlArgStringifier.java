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

import org.infogrid.util.http.HTTP;

/**
 * Stringifies a String by escaping all characters necessary to make the String a valid URL argument.
 * For example, it replaces ? and &.
 */
public class UrlArgStringifier
        extends
            StringStringifier
{
    /**
     * Factory method.
     *
     * @return the created StringStringifier
     */
    public static UrlArgStringifier create()
    {
        return new UrlArgStringifier();
    }

    /**
     * No-op constructor. Use factory method.
     */
    protected UrlArgStringifier()
    {
        // no op
    }

    /**
     * Overridable method to possibly escape a String first.
     *
     * @param s the String to be escaped
     * @return the escaped String
     */
    @Override
    protected String escape(
            String s )
    {
        String ret = HTTP.encodeToValidUrl( s );
        return ret;
    }

    /**
     * Overridable method to possibly unescape a String first.
     *
     * @param s the String to be unescaped
     * @return the unescaped String
     */
    @Override
    protected String unescape(
            String s )
    {
        String ret = HTTP.decodeUrlArgument( s );
        return ret;
    }
}
