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

package org.infogrid.kernel.TEST.meshbase.m.security;

import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.m.MMeshBase;
import org.infogrid.meshbase.security.AccessManager;
import org.infogrid.model.AclBasedSecurity.AclBasedSecuritySubjectArea;
import org.infogrid.model.AclBasedSecurity.accessmanager.AclBasedAccessManager;
import org.infogrid.model.primitives.RelationshipType;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;

import java.net.URISyntaxException;

/**
 * Factors out common functionality of the various MeshBaseSecurityTests.
 */
public abstract class AbstractMeshBaseSecurityTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     */
    public AbstractMeshBaseSecurityTest(
            Class testClass )
        throws
            MeshTypeNotFoundException,
            URISyntaxException
    {
        super( localFileName( testClass, "/ResourceHelper" ),
               localFileName( testClass, "/Log.properties" ));

        theAccessManager = AclBasedAccessManager.create();
        
        theMeshBase = MMeshBase.create(
                MeshBaseIdentifier.create( "MeshBase" ),
                theModelBase,
                theAccessManager,
                rootContext );

        life = theMeshBase.getMeshBaseLifecycleManager();
        
        rightsTypes = new RelationshipType[] {
                AclBasedSecuritySubjectArea.MESHOBJECT_HASREADACCESSTO_PROTECTIONDOMAIN,
                AclBasedSecuritySubjectArea.MESHOBJECT_HASUPDATEACCESSTO_PROTECTIONDOMAIN,
                // AclBasedSecuritySubjectArea.MESHOBJECT_HASDELETEACCESSTO_PROTECTIONDOMAIN
        };
    }
    
    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        theMeshBase.die();
    }

    /**
     * The ModelBase.
     */
    protected ModelBase theModelBase = ModelBaseSingleton.getSingleton();

    /**
     * The MeshBase for the test.
     */
    protected MeshBase theMeshBase;

    /**
     * The MeshBaseLifecycleManager that goes with the MeshBase.
     */
    protected MeshBaseLifecycleManager life;

    /**
     * Our AccessManager for the MeshBase.
     */
    protected AccessManager theAccessManager;
    
    /**
     * All concrete RelationshipTypes that indicate rights in a ProtectionDomain.
     */
    protected RelationshipType [] rightsTypes;

    /**
     * The root context for these tests.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root-context" );
}
