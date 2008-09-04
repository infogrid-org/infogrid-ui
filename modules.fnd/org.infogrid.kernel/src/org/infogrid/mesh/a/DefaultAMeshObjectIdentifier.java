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

package org.infogrid.mesh.a;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.meshbase.a.DefaultAMeshObjectIdentifierFactory;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * Implements MeshObjectIdentifier for the "A" implementation.
 */
public class DefaultAMeshObjectIdentifier
        implements
            MeshObjectIdentifier
{
    /**
     * Factory method.
     *
     * @param localId the localId of the to-be-DefaultAMeshObjectIdentifier ReferenceValue
     * @return the created DefaultAMeshObjectIdentifier
     */
    public static DefaultAMeshObjectIdentifier create(
            String localId )
    {
        if( localId == null || localId.length() == 0 ) {
            return DefaultAMeshObjectIdentifierFactory.HOME_OBJECT;
        }

        return new DefaultAMeshObjectIdentifier( localId );
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param localId the localId of the to-be-created DefaultAMeshObjectIdentifier
     */
    protected DefaultAMeshObjectIdentifier(
            String localId )
    {
        theLocalId = localId;
    }

    /**
     * Obtain the localId component.
     *
     * @return the localId component
     */
    public String getLocalId()
    {
        return theLocalId;
    }

    /**
     * Determine whether this MeshObjectIdentifier identifies a Home Object.
     *
     * @return true if it identifies a Home Object
     */
    public boolean identifiesHomeObject()
    {
        return toExternalForm().length() == 0;
    }

    /**
      * Determine hashCode.
      *
      * @return the hash code
      */
    @Override
    public final int hashCode()
    {
        return toExternalForm().hashCode();
    }

    /**
      * Determine equality of two objects.
      *
      * @param otherValue the object to test against
      * @return true if the objects are equal
      */
    @Override
    public final boolean equals(
            Object otherValue )
    {
        if( otherValue instanceof MeshObjectIdentifier ) {
            MeshObjectIdentifier realValue = (MeshObjectIdentifier) otherValue;
            
            if( !toExternalForm().equals( realValue.toExternalForm() )) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
      * Obtain as string representation, for debugging.
      *
      * @return string representation of this object
      */
    @Override
    public String toString()
    {
        return "&" + toExternalForm();
    }

    /**
     * Obtain an external form for this Identifier, similar to
     * <code>java.net.URL.toExternalForm()</code>.
     *
     * @return external form of this Identifier
     */
    public String toExternalForm()
    {
        if( theLocalId != null ) {
            return theLocalId;
        } else {
            return "";
        }
    }

    /**
     * Re-construct a DefaultAMeshObjectIdentifier from an external form.
     *
     * @param raw the external form of the DefaultAMeshObjectIdentifier
     * @return the created DefaultAMeshObjectIdentifier
     */
    public static MeshObjectIdentifier fromExternalForm(
            String raw )
    {
        if( raw == null ) {
            return null;
        }
        return DefaultAMeshObjectIdentifier.create( raw );
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        String externalForm = toExternalForm();

        String key;
        if( identifiesHomeObject() ) {
            key = HOME_DEFAULT_ENTRY;
        } else {
            key = DEFAULT_ENTRY;
        }

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                externalForm );

        return ret;
    }

    /**
     * Obtain the start part of a String representation of this MeshBase that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }

    /**
     * Obtain the end part of a String representation of this MeshBase that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }
    
    /**
     * The real value for the localId.
     */
    protected String theLocalId;

    /**
     * The default entry in the resouce files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_ENTRY = "String";

    /**
     * The home entry in the resouce files, prefixed by the StringRepresentation's prefix.
     */
    public static final String HOME_DEFAULT_ENTRY = "HomeString";
}
