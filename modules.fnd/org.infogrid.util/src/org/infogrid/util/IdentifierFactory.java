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
import org.infogrid.util.text.StringRepresentationParseException;
import org.infogrid.util.text.StringifierUnformatFactory;

/**
 * An abstract factory for {@link Identifier Identifiers}.
 */
public interface IdentifierFactory
        extends
            StringifierUnformatFactory
{
    /**
     * Create an {@link Identifier} given its external form.
     * This is the opposite of {@link Identifier#toExternalForm()}.
     *
     * @param externalForm the externalForm
     * @return the Identifier
     * @throws StringRepresentationParseException thrown if the externalForm could not be successfully parsed
     */
    public Identifier fromExternalForm(
            String externalForm )
        throws
            StringRepresentationParseException;

    /**
     * Convert a String in a given {@link org.infogrid.util.text.StringRepresentation}
     * back to an {@link Identifier}.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created Identifier
     * @throws StringRepresentationParseException thrown if the String could not be successfully parsed
     */
    public Identifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            StringRepresentationParseException;
}
