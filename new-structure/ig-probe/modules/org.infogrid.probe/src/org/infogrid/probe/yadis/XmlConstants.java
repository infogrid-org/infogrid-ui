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

package org.infogrid.probe.yadis;

import org.infogrid.model.primitives.StringValue;

/**
 * Collects constants used in Yadis XML files.
 */
public interface XmlConstants
{
    /**
     * XML Namespace URI for the XRDS elements.
     */
    final static public String XRDS_XML_NAMESPACE = "xri://$xrds";

    /**
     * XML Namespace URI for the XRD elements.
     */
    final static public String XRD_XML_NAMESPACE = "xri://$xrd*($v*2.0)";

    /**
     * XML Namespace URI for the OpenID elements.
     */
    final static public String OPENID_XML_NAMESPACE = "http://openid.net/xmlns/1.0";

    /**
     * Name of the OpenID Delegate field in XML.
     */
    final static public String OPENID_XML_DELEGATE_FIELD_NAME = "Delegate";

    /**
     * Yadis type identifier for OpenID Authentication 1.0.
     */
    final static public StringValue OPENID1_0TYPEIDENTIFIER = StringValue.create( "http://openid.net/signon/1.0" );

    /**
     * Yadis type identifier for OpenID Authentication 2.0.
     */
    final static public StringValue OPENID2_0TYPEIDENTIFIER = StringValue.create( "http://specs.openid.net/auth/2.0/signon" );
}
