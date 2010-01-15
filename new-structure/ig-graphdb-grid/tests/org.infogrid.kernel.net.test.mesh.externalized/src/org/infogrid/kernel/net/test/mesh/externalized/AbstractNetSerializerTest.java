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

package org.infogrid.kernel.net.test.mesh.externalized;

import org.infogrid.meshbase.net.DefaultNetMeshBaseIdentifierFactory;
import org.infogrid.meshbase.net.NetMeshBaseIdentifierFactory;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.m.MModelBase;
import org.infogrid.testharness.AbstractTest;
/**
 * Factors out common functionality of SerializerTests.
 */
public abstract class AbstractNetSerializerTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     *
     * @param testClass the class containing the actual test
     */
    public AbstractNetSerializerTest(
            Class testClass )
    {
        super( localFileName( testClass, "/ResourceHelper" ));

    }
    
    /**
     * The serializer to be tested.
     */
    protected static NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory = DefaultNetMeshBaseIdentifierFactory.create(
            new DefaultNetMeshBaseIdentifierFactory.Protocol [] {
                    new DefaultNetMeshBaseIdentifierFactory.Protocol( "http",  true ),
                    new DefaultNetMeshBaseIdentifierFactory.Protocol( "https", true ),
                    new DefaultNetMeshBaseIdentifierFactory.Protocol( "test",  false ),
    });

    /**
     * The ModelBase.
     */
    protected static ModelBase theModelBase = MModelBase.create();
}
