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

package org.infogrid.meshbase;

import java.net.URISyntaxException;
import org.infogrid.util.IdentifierFactory;
import org.infogrid.util.text.StringifierException;
import org.infogrid.util.text.StringRepresentation;

/**
 * Factory for MeshBaseIdentifiers.
 */
public class MeshBaseIdentifierFactory
        implements
             IdentifierFactory
{
    /**
     * Recreate a MeshBaseIdentifier from an external form.
     *
     * @param raw the external form
     * @return the created MeshBaseIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public MeshBaseIdentifier fromExternalForm(
            String raw )
        throws
            URISyntaxException
    {
        MeshBaseIdentifier ret = MeshBaseIdentifier.create( raw );
        return ret;
    }

    /**
     * Convert this StringRepresentation back to a MeshBaseIdentifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created MeshBaseIdentifier
     * @throws URISyntaxException thrown if a parsing error occurred
     */
    public MeshBaseIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            URISyntaxException
    {
        try {
            Object [] found = representation.parseEntry( MeshBaseIdentifier.class, MeshBaseIdentifier.DEFAULT_ENTRY, s );

            MeshBaseIdentifier ret;
            switch( found.length ) {
                case 1:
                    ret = fromExternalForm( (String) found[0] );
                    break;

                default:
                    throw new URISyntaxException( s, "Cannot parse identifier" );
            }

            return ret;

        } catch( StringifierException ex ) {
            throw new URISyntaxException( s, "Cannot parse identifier" );

        } catch( ClassCastException ex ) {
            throw new URISyntaxException( s, "Cannot parse identifier" );
        }
        
    }
}
