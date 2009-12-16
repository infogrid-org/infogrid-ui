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

package org.infogrid.lid.model.openid.auth.mesh;

import java.util.ArrayList;
import org.infogrid.lid.LidNonceManager;
import org.infogrid.lid.model.openid.auth.AuthSubjectArea;
import org.infogrid.lid.model.yadis.util.YadisUtil;
import org.infogrid.lid.openid.OpenIdRpSideAssociationManager;
import org.infogrid.lid.openid.auth.AbstractOpenId2CredentialType;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.OrderedMeshObjectSet;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.HasIdentifier;

/**
 * Implements the AbstractOpenId2CredentialType using the model defined in this Module.
 */
public class MeshOpenId2CredentialType
    extends
        AbstractOpenId2CredentialType
{
    /**
     * Factory method.param
     *
     * @param associationManager the relying party-side association manager to use
     * @param nonceManager the LidNonceManager to use
     * @return the created AbstractOpenId1CredentialType
     */
    public static MeshOpenId2CredentialType create(
            OpenIdRpSideAssociationManager associationManager,
            LidNonceManager                nonceManager )
    {
        MeshOpenId2CredentialType ret = new MeshOpenId2CredentialType( associationManager, nonceManager );
        return ret;
    }

    /**
     * Constructor.
     *
     * @param associationManager the relying party-side association manager to use
     * @param nonceManager the LidNonceManager to use
     */
    protected MeshOpenId2CredentialType(
            OpenIdRpSideAssociationManager associationManager,
            LidNonceManager                nonceManager )
    {
        super( associationManager, nonceManager );
    }

    /**
     * Determine the endpoint URLs that support OpenID V1 authentication, for this subject.
     *
     * @param subject the subject
     * @return the endpoint URLs
     */
    protected String [] determineOpenIdEndpointsFor(
            HasIdentifier subject )
    {
        MeshObject realSubject = (MeshObject) subject;

        ArrayList<String> almost = new ArrayList<String>();

        OrderedMeshObjectSet services = YadisUtil.determineServicesFor( realSubject, AuthSubjectArea.AUTHENTICATION2DOT0SERVICE );
        for( MeshObject service : services ) {
            OrderedMeshObjectSet endpoints = YadisUtil.determineOrderedEndpointWebResources( service );
            for( MeshObject ep : endpoints ) {
                String toAdd = ep.getIdentifier().toExternalForm();
                almost.add( toAdd );
            }
        }
        return ArrayHelper.copyIntoNewArray( almost, String.class );
    }
}
