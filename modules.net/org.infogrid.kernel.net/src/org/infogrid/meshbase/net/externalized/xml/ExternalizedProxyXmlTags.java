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

package org.infogrid.meshbase.net.externalized.xml;

import org.infogrid.meshbase.net.xpriso.xml.XprisoMessageXmlTags;

/**
 * XML tags used for the default Proxy encoding.
 */
public interface ExternalizedProxyXmlTags
        extends
            XprisoMessageXmlTags
{
    /** Tag indicating a Proxy. */
    public static final String PROXY_INLINED_TAG = "proxy";

    /** Tag indicating the NetworkIdentifier of the NetMeshBase that we belong to. */
    public static final String HERE_TAG = "here";

    /** Tag indicating the NetworkIdentifier of the NetMeshBase that we talk to. */
    public static final String THERE_TAG = "there";

    /** Tag indicating the last sent token. */
    public static final String LAST_SENT_TOKEN_TAG = "lastTokenSent";
    
    /** Tag indicating the last received token. */
    public static final String LAST_RECEIVED_TOKEN_TAG = "lastTokenReceived";
    
    /** Tag indicating the section containing messages that still need to be sent. */
    public static final String MESSAGES_TO_SEND_TAG = "messages-to-send";
    
    /** Tag indicating the section containing the last sent messages. */
    public static final String MESSAGES_LAST_SENT_TAG = "messages-last-sent";
    
    /** Tag indicating the CoherenceSpecification of a Proxy. */
    public static final String COHERENCE_SPECIFICATION_TAG = "coherence-specification";
}
