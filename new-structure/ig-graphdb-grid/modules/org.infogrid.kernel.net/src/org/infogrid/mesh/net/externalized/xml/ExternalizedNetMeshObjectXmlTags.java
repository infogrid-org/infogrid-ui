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

package org.infogrid.mesh.net.externalized.xml;

import org.infogrid.mesh.externalized.xml.ExternalizedMeshObjectXmlTags;

/**
 * Tags used in the serialization of ExternalizedNetMeshObjects to XML.
 */
public interface ExternalizedNetMeshObjectXmlTags
        extends
            ExternalizedMeshObjectXmlTags
{
    /** Tag indicating an active Proxy. */
    public static final String PROXY_REFERENCE_TAG = "P";
    
    /** Tag indicating the NetworkIdentifier for a Proxy. */
    public static final String PROXY_NETWORK_IDENTIFIER_TAG = "ID";
    
    /** Tag indicating that a Proxy is the proxy towards home. */
    public static final String PROXY_TOWARDS_HOME_TAG = "home";
    
    /** Tag indicating that a Proxy is the proxy towards lock. */
    public static final String PROXY_TOWARDS_LOCK_TAG = "lock";
    
    /** Tag saying "yes". */
    public static final String YES_TAG = "yes";
    
    /** Tag indicating whether this replica gives up the lock when asked. */
    public static final String GIVE_UP_LOCK_TAG = "GiveUpLock";

    /** Tag indicating whether this replica gives up the home replica when asked. */
    public static final String GIVE_UP_HOME_TAG = "GiveUpHome";

    /** Tag indicating a relationship Proxy. */
    public static final String RELATIONSHIP_PROXY_REFERENCE_TAG = "RP";
}
