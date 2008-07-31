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

package org.infogrid.meshbase.net.xpriso.xml;

import org.infogrid.mesh.net.externalized.xml.ExternalizedNetMeshObjectXmlTags;

/**
 * XML tags used for the default XprisoMessage encoding.
 */
public interface XprisoMessageXmlTags
        extends
            ExternalizedNetMeshObjectXmlTags
{
    /** Tag indicating an Xpriso message. */
    public static final String MESSAGE_TAG = "XprisoMessage";

    /** Tag indicating the request id. */
    public static final String REQUEST_ID_TAG = "request-id";
    
    /** Tag indicating the response id. */
    public static final String RESPONSE_ID_TAG = "response-id";
    
    /** Tag indicating the sender NetworkIdentifier. */
    public static final String SENDER_ID_TAG = "sender";
    
    /** Tag indicating the receiver NetworkIdentifier. */
    public static final String RECEIVER_ID_TAG = "receiver";
    
    /** Tag identifying a neighbor MeshObject. */
    public static final String NEIGHBOR_TAG = "N";
    
    /** Tag indicating a first-time object request. */
    public static final String REQUESTED_FIRST_TIME_OBJECT_TAG = "request-first-time";

    /** Tag indicating a NetworkPath. */
    public static final String NETWORK_PATH_TAG = "path";

    /** Tag indicating objects to be canceled. */
    public static final String REQUESTED_CANCELED_OBJECT_TAG = "request-cancel";

    /** Tag indicating objects to be deleted. */
    public static final String MESH_OBJECT_DELETED_TAG = "request-delete";

    /** Tag indicating a conveyed, in-lined MeshObject. */
    public static final String CONVEYED_MESH_OBJECT_TAG = "conveyed";
    
    /** Tag indicating a neighbor was added event. */
    public static final String NEIGHBOR_ADDITION_TAG = "neighbor-added";
    
    /** Tag indicating a neighbor was removed event. */
    public static final String NEIGHBOR_REMOVAL_TAG = "neighbor-removed";
    
    /** Tag indicating a Property change event. */
    public static final String PROPERTY_CHANGE_TAG = "property-change";

    /** Tag indicating a Role blessing event. */
    public static final String ROLE_ADDITION_TAG = "role-addition";
    
    /** Tag indicating a Role unblessing event. */
    public static final String ROLE_REMOVAL_TAG = "role-removal";
    
    /** Tag indicating a MeshObject blessing event. */
    public static final String TYPE_ADDITION_TAG = "type-addition";
    
    /** Tag indicating a MeshObject unblessing event. */
    public static final String TYPE_REMOVAL_TAG = "type-removal";
    
    /** Tag indicating that a lock was requested. */
    public static final String REQUESTED_LOCK_OBJECT_TAG = "requested-lock";
    
    /** Tag indicating that a lock was pushed. */
    public static final String PUSH_LOCK_OBJECT_TAG = "push-lock";
    
    /** Tag indicating whose MeshObject's locks were reclaimed. */
    public static final String RECLAIMED_LOCK_OBJECT_TAG = "reclaimed-lock";
    
    /** Tag indicating which MeshObjects are requested to be resynchronized. */
    public static final String REQUESTED_RESYNCHRONIZE_DEPENDENT_REPLICA_TAG = "requested-resynchronize";
    
    /** Tag indicating that the sending Proxy wishes to cease communications. */
    public static final String CEASE_COMMUNICATIONS_TAG = "cease-communications";
}
