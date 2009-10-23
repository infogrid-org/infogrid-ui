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

package org.infogrid.meshbase.net;

import java.io.File;
import java.net.URI;
import java.net.URL;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Factory for NetMeshBaseIdentifiers.
 */
public interface NetMeshBaseIdentifierFactory
        extends
            MeshBaseIdentifierFactory
{
    /**
     * Recreate a NetMeshBaseIdentifier from an external form. Be strict about syntax.
     *
     * @param context the NetMeshBaseIdentifier that forms the context
     * @param raw the external form
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromExternalForm(
            NetMeshBaseIdentifier context,
            String                raw )
        throws
            StringRepresentationParseException;

    /**
     * Recreate a NetMeshBaseIdentifier from an external form.
     *
     * @param raw the external form
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromExternalForm(
            String raw )
        throws
            StringRepresentationParseException;

    /**
     * Factory method.
     * 
     * @param file the local File whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier obtain(
            File file )
        throws
            StringRepresentationParseException;

    /**
     * Factory method.
     * 
     * @param url the URL whose NetMeshBaseIdentifier we obtain
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier obtain(
            URL url )
        throws
            StringRepresentationParseException;
    
    /**
     * Factory method.
     * 
     * @param uri the URI form
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier obtain(
            URI uri )
        throws
            StringRepresentationParseException;

    /**
     * Factory method to obtain a NetMeshBaseIdentifier.
     * This method attempts to guess the protocol if none has been provided.
     * 
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier guessFromExternalForm(
            String string )
        throws
            StringRepresentationParseException;
    
    /**
     * Factory method to obtain a NetMeshBaseIdentifier specified in relative form in the
     * context of another NetMeshBaseIdentifier.
     * 
     * @param context the NetMeshBaseIdentifier that forms the context
     * @param string the (potentially incomplete) String form of this NetMeshBaseIdentifier
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public NetMeshBaseIdentifier guessFromExternalForm(
            NetMeshBaseIdentifier context,
            String                string )
        throws
            StringRepresentationParseException;

    /**
     * Convert this StringRepresentation back to a NetMeshBaseIdentifier.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created NetMeshBaseIdentifier
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseIdentifier fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            StringRepresentationParseException;
}
