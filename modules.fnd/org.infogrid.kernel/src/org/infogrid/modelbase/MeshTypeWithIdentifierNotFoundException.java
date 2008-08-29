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

package org.infogrid.modelbase;

import org.infogrid.model.primitives.MeshTypeIdentifier;

import org.infogrid.util.StringHelper;

/**
 * This Exception indicates that a MeshType of any kind, with this Identifier, could not be found.
 */
public class MeshTypeWithIdentifierNotFoundException
        extends
            MeshTypeNotFoundException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param identifier the Identifier of the MeshType that could not be found
     */
    public MeshTypeWithIdentifierNotFoundException(
            MeshTypeIdentifier identifier )
    {
        super();

        theIdentifier = identifier;
    }

    /**
     * Constructor.
     * 
     * @param identifier the Identifier of the MeshType that could not be found
     * @param cause the Throwable that caused this Exception
     */
    public MeshTypeWithIdentifierNotFoundException(
            MeshTypeIdentifier identifier,
            Throwable          cause )
    {
        super( cause );

        theIdentifier = identifier;
    }

    /**
     * Obtain the Identifier that was not found.
     *
     * @return the Identifier
     */
    public MeshTypeIdentifier getIdentifier()
    {
        return theIdentifier;
    }

    /**
     * Convert object into string representation, mostly for debugging.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "Identifier"
                },
                new Object[] {
                    theIdentifier
                });
    }

    /**
     * Obtain parameters for the internationalization.
     *
     * @return the parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[]{ theIdentifier };
    }

    /**
     * The Identifier of the MeshType that could not be found.
     */
    protected MeshTypeIdentifier theIdentifier;
}
