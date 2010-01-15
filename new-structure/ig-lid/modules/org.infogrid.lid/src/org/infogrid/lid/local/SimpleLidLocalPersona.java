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

package org.infogrid.lid.local;

import java.util.Map;
import org.infogrid.util.Identifier;

/**
 * Simple implementation of LidLocalPersona.
 */
public class SimpleLidLocalPersona
        extends
            AbstractLidLocalPersona
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     *
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     * @return the created SimpleLidLocalPersona
     */
    public static SimpleLidLocalPersona create(
            Identifier             identifier,
            Map<String,String>     attributes )
    {
        SimpleLidLocalPersona ret = new SimpleLidLocalPersona( identifier, attributes );
        return ret;
    }

    /**
     * Constructor for subclasses only.
     *
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     */
    protected SimpleLidLocalPersona(
            Identifier             identifier,
            Map<String,String>     attributes )
    {
        super( identifier, attributes );
    }
}
