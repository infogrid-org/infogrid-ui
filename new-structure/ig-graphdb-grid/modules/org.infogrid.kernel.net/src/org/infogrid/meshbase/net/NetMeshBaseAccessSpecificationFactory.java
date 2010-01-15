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

import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Factory for NetMeshBaseAccessSpecifications.
 */
public interface NetMeshBaseAccessSpecificationFactory
{
    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier identifier );

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier  identifier,
            ScopeSpecification     scope );

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param coherence the CoherenceSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier  identifier,
            CoherenceSpecification coherence );

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @param coherence the CoherenceSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier  identifier,
            ScopeSpecification     scope,
            CoherenceSpecification coherence );

    /**
     * Recreate a NetMeshBaseAccessSpecification from an external form.
     *
     * @param raw the external form
     * @return the created NetMeshBaseAccessSpecification
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseAccessSpecification fromExternalForm(
            String raw )
        throws
            StringRepresentationParseException;

    /**
     * Convert this StringRepresentation back to a NetMeshBaseAccessSpecification.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created NetMeshBaseAccessSpecification
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseAccessSpecification fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            StringRepresentationParseException;
}
