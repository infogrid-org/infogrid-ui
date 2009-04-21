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
 * Collects functionality common to Stringifier implementations.
 *
 * @param <T> the type of the Objects to be stringified
 */
public abstract class AbstractStringifier<T>
        implements
            Stringifier<T>
{
    /**
     * Helper method to potentially shorten output based on a parameter
     * potentially contained in the StringRepresentationParameters.
     *
     * @param s the String potentially to be shortened
     * @param pars the StringRepresentationParameters, if any
     * @return the potentially shortened String
     */
    protected String potentiallyShorten(
            String                         s,
            StringRepresentationParameters pars )
    {
        if( pars == null ) {
            return s;
        }
        Number n = (Number) pars.get( StringRepresentationParameters.MAX_LENGTH );
        if( n == null ) {
            return s;
        }
        String ret = StringHelper.potentiallyShorten( s, n.intValue() );
        return ret;
    }
}
