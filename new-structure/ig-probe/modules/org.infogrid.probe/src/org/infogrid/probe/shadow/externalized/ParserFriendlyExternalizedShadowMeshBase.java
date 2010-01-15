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

package org.infogrid.probe.shadow.externalized;

import org.infogrid.mesh.net.externalized.ExternalizedNetMeshObject;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;

import org.infogrid.util.ArrayHelper;

import java.util.ArrayList;

/**
 * A temporary buffer for a to-be-deserialized ShadowMeshBase.
 */
public class ParserFriendlyExternalizedShadowMeshBase
        extends
            AbstractExternalizedShadowMeshBase
{
    /**
     * Factory method.
     * 
     * @return the created ParserFriendlyExternalizedShadowMeshBase
     */
    public static ParserFriendlyExternalizedShadowMeshBase create()
    {
        return new ParserFriendlyExternalizedShadowMeshBase();
    }

    /**
     * Constructor.
     */
    protected ParserFriendlyExternalizedShadowMeshBase()
    {
        // noop
    }
    
    /**
     * Set the NetMeshBaseIdentifier of the ShadowMeshBase.
     * 
     * @param newValue the new value
     */
    public void setNetworkIdentifier(
            NetMeshBaseIdentifier newValue )
    {
        theNetworkIdentifier = newValue;
    }

    /**
     * Obtain the ExternalizedProxies.
     *
     * @return the ExternalizedProxies
     */
    public ExternalizedProxy [] getExternalizedProxies()
    {
        ExternalizedProxy [] ret = ArrayHelper.copyIntoNewArray( theExternalizedProxies, ExternalizedProxy.class );
        return ret;
    }

    /**
     * Add an ExternalizedProxy.
     *
     * @param newValue the new value
     */
    public void addExternalizedProxies(
            ExternalizedProxy newValue )
    {
        theExternalizedProxies.add( newValue );
    }

    /**
     * Obtain the ExternalizedNetMeshObjects.
     *
     * @return the ExternalizedNetMeshObjects
     */
    public ExternalizedNetMeshObject [] getExternalizedNetMeshObjects()
    {
        ExternalizedNetMeshObject [] ret = ArrayHelper.copyIntoNewArray( theExternalizedNetMeshObjects, ExternalizedNetMeshObject.class );
        return ret;
    }

    /**
     * Add an ExternalizedNetMeshObject.
     *
     * @param newValue the new value
     */
    public void addExternalizedNetMeshObject(
            ExternalizedNetMeshObject newValue )
    {
        theExternalizedNetMeshObjects.add( newValue );
    }
    
    /**
     * The ExternalizedProxies.
     */
    protected ArrayList<ExternalizedProxy> theExternalizedProxies = new ArrayList<ExternalizedProxy>();
    
    /**
     * The ExternalizedProxies.
     */    
    protected ArrayList<ExternalizedNetMeshObject> theExternalizedNetMeshObjects = new ArrayList<ExternalizedNetMeshObject>();
}
