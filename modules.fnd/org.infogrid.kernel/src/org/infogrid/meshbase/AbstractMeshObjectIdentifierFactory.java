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

package org.infogrid.meshbase;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.util.SimpleTimeBasedUniqueLongGenerator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Factors out common features of MeshObjectIdentifierFactories.
 */
public abstract class AbstractMeshObjectIdentifierFactory
        implements
            MeshObjectIdentifierFactory
{
    private static final Log log = Log.getLogInstance( AbstractMeshObjectIdentifierFactory.class ); // our own, private logger

    /**
     * Constructor.
     */
    protected AbstractMeshObjectIdentifierFactory()
    {
    }

    /**
     * Create a unique MeshObjectIdentifier.
     *
     * @return the unique MeshObjectIdentifier
     */
    public MeshObjectIdentifier createMeshObjectIdentifier()
    {
        long unique = theDelegate.createUniqueToken();

        StringBuilder id = new StringBuilder();
        id.append( INTERNAL_PREFIX ); // keep this short
        id.append( Long.toHexString( unique ));
        
        try {
            MeshObjectIdentifier ret = fromExternalForm( id.toString() );

            return ret;

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * The internally used UniqueTokenCreator.
     */
    protected static SimpleTimeBasedUniqueLongGenerator theDelegate = SimpleTimeBasedUniqueLongGenerator.create();
    
    /**
     * String that only internal identifiers may start with, not external ones.
     */
    public static String INTERNAL_PREFIX = "_";
}
