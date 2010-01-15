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

package org.infogrid.probe.shadow.externalized.xml;

import org.infogrid.meshbase.net.externalized.xml.ExternalizedProxyXmlTags;

/**
 * XML tags used for the default ShadowMeshBase encoding.
 */
public interface ExternalizedShadowMeshBaseXmlTags
        extends
            ExternalizedProxyXmlTags
{
    /** Tag indicating a ShadowMeshBase. */
    public static final String SHADOW_TAG = "shadow";
    
    /** Tag identifying the ShadowMeshBase's NetworkIdentifier. */
    public static final String SHADOW_IDENTIFIER_TAG = "ID";

    /** Tag indicating that a Proxy is a placeholder for ForwardReference purposes. */
    public static final String PROXY_PLACEHOLDER_TAG = "placeholder";
}
