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

package org.infogrid.jee.shell.http;

/**
 * Collects the keywords understood by the HttpShell.
 */
public interface HttpShellKeywords
{
    /**
     * Separator between the components of the keywords.
     */
    public static final String SEPARATOR = ".";

    /**
     * The prefix for all keywords for the protocol.
     */
    public static final String PREFIX = "shell" + SEPARATOR;

    /**
     * Keyword indicating the identifier of a MeshObject.
     */
    public static final String IDENTIFIER_TAG = ""; // empty, that is intentional

    /**
     * Keyword indicating the type of access to a MeshObject that shall be performed.
     * For possible values, see HttpShellAccessVerb.
     */
    public static final String ACCESS_TAG = SEPARATOR + "access";

    /**
     * Keyword indicating the MeshBase in which the access shall be performed.
     */
    public static final String MESH_BASE_TAG = SEPARATOR + "meshbase";

    /**
     * Keyword indicating the <code>PropertyType</code> for an operation.
     */
    public static final String PROPERTY_TYPE_TAG  = SEPARATOR + "propertytype" + SEPARATOR;

    /**
     * Keyword indicating the <code>PropertyValue</code> for an operation.
     */
    public static final String PROPERTY_VALUE_TAG = SEPARATOR + "propertyvalue" + SEPARATOR;

    /**
     * Keyword indicating a null <code>PropertyValue</code> for a property.
     */
    public static final String NULL_PROPERTY_VALUE_TAG = SEPARATOR + "null";

    /**
     * Keyword indicating an uploaded <code>PropertyValue</code> for a property.
     */
    public static final String UPLOAD_PROPERTY_VALUE_TAG = SEPARATOR + "upload";

    /**
     * Keyword indicating the MIME type for a <code>BlobValue</code> for a property.
     */
    public static final String MIME_TAG = SEPARATOR + "mime";

    /**
     * Value for the NULL_PROPERTY_VALUE_TAG.
     */
    public static final String NULL_PROPERTY_VALUE_TAG_TRUE = "true";

    /**
     * Keyword indicating with which <code>EntityType</code> a MeshObject shall be blessed.
     */
    public static final String BLESS_TAG = SEPARATOR + "bless";

    /**
     * Keyword indicating from which <code>EntityType</code> a MeshObject shall be unblessed.
     */
    public static final String UNBLESS_TAG = SEPARATOR + "unbless";

    /**
     * Keyword indicating a relationship to another MeshObject.
     */
    public static final String TO_TAG = SEPARATOR + "to";

    /**
     * Keyword indicating a relationship verb to another MeshObject.
     * For possible values, see HttpShellRelationshipVerb.
     */
    public static final String PERFORM_TAG = SEPARATOR + "perform";

    /**
     * Keyword indicating the <code>RoleType</code> a relationship shall be blessed with.
     */
    public static final String BLESS_ROLE_TAG = SEPARATOR + "blessRole";

    /**
     * Keyword indicating the <code>RoleType</code> a relationship shall be unblessed from.
     */
    public static final String UNBLESS_ROLE_TAG = SEPARATOR + "unblessRole";

    /**
     * Keyword indicating the presence of a relationship checkbox by specifying a RoleType that
     * should be blessed/unblessed.
     */
    public static final String CHECKBOX_ROLE_TAG = SEPARATOR + "checkbox.role";

    /**
     * Keyword indicating that a checkbox was selected to bless a relationship.
     */
    public static final String CHECKBOX_TAG = SEPARATOR + "checkbox";

    /**
     * Keyword indicating whether any particular component of the operation should or should
     * not throw an exception if its execution failed.
     */
    public static final String THROW_TAG = SEPARATOR + "throw";

    /**
     * Keyword indicating whether the commands shell be performed or ignored. This makes it
     * easier to develop HTML forms that have a cancel button.
     */
    public static final String SUBMIT_TAG = "submit";

    /**
     * Optimization to also have this one around.
     */
    public static final String FULL_SUBMIT_TAG = PREFIX + SUBMIT_TAG;

    /**
     * Value of the SUBMIT_TAG keyword that indicates "perform operations". All other values,
     * if the SUBMIT_TAG is given, indicate "ignore commands".
     */
    public static final String SUBMIT_COMMIT_VALUE = "commit";

    /**
     * Keyword indicating that a redirect shall be performed.
     */
    public static final String REDIRECT_TAG = SEPARATOR + "redirect";

    /**
     * Value for the REDIRECT_TAG.
     */
    public static final String REDIRECT_TAG_TRUE = "true";

    /**
     * Indicates the name of the handler class.
     */
    public static final String HANDLER_TAG = PREFIX + "handler.name";
}
