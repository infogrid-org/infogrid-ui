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

package org.infogrid.mesh.net.a;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.a.DefaultAMeshObjectIdentifier;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.a.DefaultAnetMeshObjectIdentifierFactory;
import org.infogrid.util.text.IdentifierStringifier;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Implements NetMeshObjectIdentifier for the Anet implementation.
 */
public class DefaultAnetMeshObjectIdentifier
        extends
            DefaultAMeshObjectIdentifier
        implements
            NetMeshObjectIdentifier
{
    /**
     * Factory method.
     *
     * @param factory the DefaultAnetMeshObjectIdentifierFactory that created this identifier
     * @param baseIdentifier identifier of the NetMeshBase relative to which a localId is specified
     * @param localId the localId of the to-be-created DefaultAnetMeshObjectIdentifier
     * @return the created DefaultAnetMeshObjectIdentifier
     * @throws IllegalArgumentException thrown if a non-null localId contains a period.
     */
    public static DefaultAnetMeshObjectIdentifier create(
            DefaultAnetMeshObjectIdentifierFactory factory,
            NetMeshBaseIdentifier                  baseIdentifier,
            String                                 localId )
    {
        if( baseIdentifier == null ) {
            throw new NullPointerException();
        }
        
        if( localId != null && factory.treatAsGlobalIdentifier( localId )) {
            throw new IllegalArgumentException( "DefaultAnetMeshObjectIdentifier's localId must not contain a period: " + localId );
        }
        
        if( localId != null && localId.length() == 0 ) {
            localId = null;
        }
        
        return new DefaultAnetMeshObjectIdentifier( factory, baseIdentifier, localId );
    }

    /**
     * Private constructor.
     * 
     * @param factory the DefaultAnetMeshObjectIdentifierFactory that created this identifier
     * @param baseIdentifier identifier of the NetMeshBase relative to which a localId is specified
     * @param localId the localId of the to-be-created MeshObjectIdentifier
     */
    protected DefaultAnetMeshObjectIdentifier(
            DefaultAnetMeshObjectIdentifierFactory factory,
            NetMeshBaseIdentifier                  baseIdentifier,
            String                                 localId )
    {
        super( factory, localId );

        theNetMeshBaseIdentifier = baseIdentifier;
    }

    /**
     * Obtain the factory that created this identifier.
     *
     * @return the factory
     */
    @Override
    public DefaultAnetMeshObjectIdentifierFactory getFactory()
    {
        return (DefaultAnetMeshObjectIdentifierFactory) super.getFactory();
    }

    /**
     * Determine whether this MeshObjectIdentifier identifies a Home Object.
     *
     * @return true if it identifies a Home Object
     */
    @Override
    public boolean identifiesHomeObject()
    {
        if( theLocalId == null || theLocalId.length() == 0 ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Obtain the identifier of the NetMeshBase in which this NetMeshObjectIdentifier was allocated.
     *
     * @return the dentifier of the NetMeshBase
     */
    public NetMeshBaseIdentifier getNetMeshBaseIdentifier()
    {
        return theNetMeshBaseIdentifier;
    }

    /**
     * Obtain an external form for this NetMeshObjectIdentifier, similar to
     * URL's getExternalForm(). This returns an empty String for local home objects.
     *
     * @return external form of this NetMeshObjectIdentifier
     */
    @Override
    public String toExternalForm()
    {
        if( theLocalId != null && theLocalId.length() > 0 ) {
            StringBuilder buf = new StringBuilder();
            buf.append( theNetMeshBaseIdentifier.toExternalForm() );
            buf.append( SEPARATOR );
            buf.append( theLocalId );
            return buf.toString();
        } else {
            return theNetMeshBaseIdentifier.toExternalForm();
        }
    }

    /**
     * Obtain the external form just of the local part of the NetMeshObjectIdentifier.
     * 
     * @return the local external form
     */
    public String toLocalExternalForm()
    {
        if( theLocalId == null || theLocalId.length() == 0 ) {
            return "";
        } else {
            return SEPARATOR + theLocalId;
        }
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    @Override
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        MeshObject meshObject  = context != null ? (MeshObject) context.get( MeshStringRepresentationContext.MESHOBJECT_KEY ) : null;
        String     contextPath = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;
        MeshBase   meshBase    = meshObject != null ? meshObject.getMeshBase() : null;

        boolean isDefaultMeshBase = false;
        if( meshBase != null && context != null ) {
            isDefaultMeshBase = meshBase.equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ));
        }
        boolean isHomeObject;
        if( meshBase != null ) {
            isHomeObject = meshObject == meshBase.getHomeObject();
        } else {
            isHomeObject = identifiesHomeObject();
        }

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject ) {
                key = DEFAULT_MESH_BASE_HOME_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_ENTRY;
            }
        } else {
            if( isHomeObject ) {
                key = NON_DEFAULT_MESH_BASE_HOME_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_ENTRY;
            }
        }

        String meshBaseExternalForm = meshBase != null ? meshBase.getIdentifier().toExternalForm() : null;

        String meshObjectExternalForm;
        if( meshBase != null && getNetMeshBaseIdentifier().equals( meshBase.getIdentifier() )) {
            meshObjectExternalForm = toLocalExternalForm();
        } else {
            meshObjectExternalForm = toExternalForm();
        }

        meshBaseExternalForm   = IdentifierStringifier.defaultFormat( meshBaseExternalForm,   pars );
        meshObjectExternalForm = IdentifierStringifier.defaultFormat( meshObjectExternalForm, pars );

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                key,
                pars,
                meshObjectExternalForm,
                contextPath,
                meshBaseExternalForm );

        return ret;
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param additionalArguments additional arguments for URLs, if any
     * @param target the HTML target, if any
     * @param title title of the HTML link, if any
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    @Override
    public String toStringRepresentationLinkStart(
            String                      additionalArguments,
            String                      target,
            String                      title,
            StringRepresentation        rep,
            StringRepresentationContext context )
        throws
            StringifierException
    {
        MeshObject meshObject  = context != null ? (MeshObject) context.get( MeshStringRepresentationContext.MESHOBJECT_KEY ) : null;
        String     contextPath = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;
        MeshBase   meshBase    = meshObject != null ? meshObject.getMeshBase() : null;

        boolean isDefaultMeshBase = true;
        if( meshBase != null && context != null ) {
            isDefaultMeshBase = meshBase.equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ));
        }
        boolean isHomeObject;
        if( meshBase != null ) {
            isHomeObject = meshObject == meshBase.getHomeObject();
        } else {
            isHomeObject = identifiesHomeObject();
        }

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject ) {
                key = DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_LINK_START_ENTRY;
            }
        } else {
            if( isHomeObject ) {
                key = NON_DEFAULT_MESH_BASE_HOME_LINK_START_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_LINK_START_ENTRY;
            }
        }
        if( target == null ) {
            target = "_self";
        }

        String meshObjectExternalForm;
        String meshBaseExternalForm;
        if( meshBase != null ) {
            meshBaseExternalForm   = meshBase.getIdentifier().toExternalForm();
            meshObjectExternalForm = toLocalExternalForm(); // only escape localIds

            StringBuffer buf = new StringBuffer();

            if( !getNetMeshBaseIdentifier().equals( meshBase.getIdentifier() )) {
                String there = getNetMeshBaseIdentifier().toExternalForm();
                for( int i=0 ; i<there.length() ; ++i ) {
                    char c = there.charAt( i );
                    switch( c ) {
                        case '?':
                        case '&':
                            buf.append( '%' );
                            buf.append( Integer.toHexString( ((int)c) / 16 ));
                            buf.append( Integer.toHexString( ((int)c) % 16 ));
                            break;

                        default:
                            buf.append( c );
                            break;
                    }
                }
            }

            for( int i=0 ; i<meshObjectExternalForm.length() ; ++i ) {
                char c = meshObjectExternalForm.charAt( i );
                switch( c ) {
                    case '.':
                    case ':':
                    case '/':
                    case '#':
                    case '?':
                    case '&':
                    case ';':
                    case '%':
                        buf.append( '%' );
                        buf.append( Integer.toHexString( ((int)c) / 16 ));
                        buf.append( Integer.toHexString( ((int)c) % 16 ));
                        break;

                    default:
                        buf.append( c );
                        break;
                }
            }
            meshObjectExternalForm = buf.toString();

        } else {
            meshBaseExternalForm = null;
            meshObjectExternalForm = toExternalForm();
        }


        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subclass
                key,
                null,
        /* 0 */ meshObjectExternalForm,
        /* 1 */ contextPath,
        /* 2 */ meshBaseExternalForm,
        /* 3 */ additionalArguments,
        /* 4 */ target,
        /* 5 */ title );

        return ret;
    }

    /**
     * Obtain the end part of a String representation of this MeshBase that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    @Override
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
        throws
            StringifierException
    {
        MeshObject meshObject  = context != null ? (MeshObject) context.get( MeshStringRepresentationContext.MESHOBJECT_KEY ) : null;
        String     contextPath = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;
        MeshBase   meshBase    = meshObject != null ? meshObject.getMeshBase() : null;

        boolean isDefaultMeshBase = false;
        if( meshBase != null && context != null ) {
            isDefaultMeshBase = meshBase.equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ));
        }
        boolean isHomeObject;
        if( meshBase != null ) {
            isHomeObject = meshObject == meshBase.getHomeObject();
        } else {
            isHomeObject = identifiesHomeObject();
        }

        String key;
        if( isDefaultMeshBase ) {
            if( isHomeObject ) {
                key = DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY;
            } else {
                key = DEFAULT_MESH_BASE_LINK_END_ENTRY;
            }
        } else {
            if( isHomeObject ) {
                key = NON_DEFAULT_MESH_BASE_HOME_LINK_END_ENTRY;
            } else {
                key = NON_DEFAULT_MESH_BASE_LINK_END_ENTRY;
            }
        }

        String meshBaseExternalForm = meshBase != null ? meshBase.getIdentifier().toExternalForm() : null;

        String meshObjectExternalForm;
        if( meshBase != null && getNetMeshBaseIdentifier().equals( meshBase.getIdentifier() )) {
            meshObjectExternalForm = toLocalExternalForm();
        } else {
            meshObjectExternalForm = toExternalForm();
        }

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subclass
                key,
                null,
                meshObjectExternalForm,
                contextPath,
                meshBaseExternalForm );

        return ret;
    }

    /**
     * The Identifier for the NetMeshBase in which this NetMeshObjectIdentifier was allocated.
     */
    protected NetMeshBaseIdentifier theNetMeshBaseIdentifier;

    /**
     * Separator between NetMeshBaseIdentifier and local id.
     */
    public static final char SEPARATOR = '#';
}
