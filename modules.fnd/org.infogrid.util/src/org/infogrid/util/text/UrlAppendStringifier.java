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

import org.infogrid.util.StringHelper;

/**
 * Stringifies a URL argument by prepending & or ? correctly.
 */
public class UrlAppendStringifier
        extends
            StringStringifier
{
    /**
     * Factory method.
     *
     * @return the created StringStringifier
     */
    public static UrlAppendStringifier create()
    {
        return new UrlAppendStringifier();
    }

    /**
     * No-op constructor. Use factory method.
     */
    protected UrlAppendStringifier()
    {
        // no op
    }

    /**
     * Format an Object using this Stringifier.
     *
     * @param soFar the String so far, if any
     * @param arg the Object to format, or null
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the formatted String
     */
    @Override
    public String format(
            String  soFar,
            String  arg,
            int     maxLength,
            boolean colloquial )
    {
        if( arg == null ) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        if( soFar.indexOf( '?' ) > 0 ) {
            buf.append( '&' );
        } else {
            buf.append( '?' );
        }

        buf.append( escape( arg ));

        return StringHelper.potentiallyShorten( buf.toString(), maxLength );
        // not sure this is the best we can do. Use case: show too long URL on screen.
    }
}
