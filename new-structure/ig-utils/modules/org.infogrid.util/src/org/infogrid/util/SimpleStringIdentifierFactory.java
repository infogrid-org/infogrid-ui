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

import org.infogrid.util.text.StringRepresentation;

/**
 * Factory for SimpleStringIdentifiers.
 */
public class SimpleStringIdentifierFactory
    implements
        IdentifierFactory
{
    /**
     * Public constructor.
     */
    public SimpleStringIdentifierFactory()
    {
        // no op
    }

    /**
     * Create an {@link Identifier} given its external form.
     * This is the opposite of {@link Identifier#toExternalForm()}.
     *
     * @param externalForm the externalForm
     * @return the SimpleStringIdentifier
     */
    public SimpleStringIdentifier fromExternalForm(
            String externalForm )
    {
        return SimpleStringIdentifier.create( externalForm );
    }

    /**
     * Convert a String in a given {@link org.infogrid.util.text.StringRepresentation}
     * back to an {@link Identifier}.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created SimpleStringIdentifier
     */
    public SimpleStringIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
    {
        return SimpleStringIdentifier.create( s );
    }
}
