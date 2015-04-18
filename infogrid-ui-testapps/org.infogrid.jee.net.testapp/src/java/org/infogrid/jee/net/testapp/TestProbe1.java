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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.net.testapp;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;

/**
 * A Probe for testing purposes.
 */
public class TestProbe1
        implements
            ApiProbe
{
    public void readFromApi(
            NetMeshBaseIdentifier  dataSourceIdentifier,
            CoherenceSpecification coherenceSpecification,
            StagingMeshBase        freshMeshBase )
        throws
            EntityBlessedAlreadyException,
            EntityNotBlessedException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            IOException,
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            ModuleException,
            NotPermittedException,
            NotRelatedException,
            ProbeException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            TransactionException,
            URISyntaxException,
            ParseException
    {
        StagingMeshBaseLifecycleManager life = freshMeshBase.getMeshBaseLifecycleManager();
        
        NetMeshObject xxxx = life.createMeshObject( freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( "#xxxx" ));
        
        xxxx.relate( freshMeshBase.getHomeObject() );
    }
}
