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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that displays one or more types of a MeshObject.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectBlessedByTag
    extends
        AbstractRestInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectBlessedByTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObject           = null;
        theMeshObjectName       = null;
        theSupertypeName        = null;
        theSupertype            = null;
        theSeparator            = ", "; // comma as default
        theStringRepresentation = null;
        theMaxLength            = -1;
        theColloquial           = true;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public final Object getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public final void setMeshObject(
            Object newValue )
    {
        theMeshObject = newValue;
    }

    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the supertypeName property.
     *
     * @return value of the supertypeName property
     * @see #setSupertypeName
     */
    public String getSupertypeName()
    {
        return theSupertypeName;
    }

    /**
     * Set value of the supertypeName property.
     *
     * @param newValue new value of the supertypeName property
     * @see #getSupertypeName
     */
    public void setSupertypeName(
            String newValue )
    {
        theSupertypeName = newValue;
    }

    /**
     * Obtain value of the supertype property.
     *
     * @return value of the supertype property
     * @see #setSupertype
     */
    public String setSupertype()
    {
        return theSupertype;
    }

    /**
     * Set value of the supertype property.
     *
     * @param newValue new value of the supertype property
     * @see #setSupertype
     */
    public void setSupertype(
            String newValue )
    {
        theSupertype = newValue;
    }

    /**
     * Obtain value of the separator property.
     *
     * @return value of the separator property
     * @see #setSeparator
     */
    public String getSeparator()
    {
        return theSeparator;
    }

    /**
     * Set value of the separator property.
     *
     * @param newValue new value of the separator property
     * @see #getSeparator
     */
    public void setSeparator(
            String newValue )
    {
        theSeparator = newValue;
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
    }

    /**
     * Obtain value of the maxLength property.
     *
     * @return value of the maxLength property
     * @see #setMaxLength
     */
    public int getMaxLength()
    {
        return theMaxLength;
    }

    /**
     * Set value of the maxLength property.
     *
     * @param newValue new value of the maxLength property
     */
    public void setMaxLength(
            int newValue )
    {
        theMaxLength = newValue;
    }

    /**
     * Obtain value of the colloquial property.
     *
     * @return value of the colloquial property
     * @see #setColloquial
     */
    public boolean getColloquial()
    {
        return theColloquial;
    }

    /**
     * Set value of the colloquial property.
     *
     * @param newValue new value of the colloquial property
     */
    public void setColloquial(
            boolean newValue )
    {
        theColloquial = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj  = lookupMeshObjectOrThrow( "meshObject", theMeshObject, "meshObjectName", theMeshObjectName );
        EntityType type = (EntityType) findMeshTypeByIdentifier( theSupertypeName );

        if( obj != null ) {
            EntityType [] allTypes = obj.getTypes();
            String        sep      = "";
            for( int i=0 ; i<allTypes.length ; ++i ) {
                if( type != null && !allTypes[i].equalsOrIsSupertype( type ) ) {
                    continue;
                }
                try {
                    String text = ((RestfulJeeFormatter)theFormatter).formatMeshType( pageContext, type, theStringRepresentation, theMaxLength, theColloquial );
                    print( sep );
                    print( text );

                    sep = theSeparator;

                } catch( StringifierException ex ) {
                    throw new JspException( ex );
                }
            }
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Name of the bean that holds the MeshObject.
     */
    protected String theMeshObjectName;

    /**
     * The MeshObject.
     */
    protected Object theMeshObject;

    /**
     * Name of the bean that holds the supertype EntityType (mutually exclusive with theSupertype).
     */
    protected String theSupertypeName;

    /**
     * The EntityType's identifier (mutually exclusive with theSupertypeName).
     */
    protected String theSupertype;

    /**
     * The separator between multiple printed types.
     */
    protected String theSeparator;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;

    /**
     * The maximum length of an emitted String.
     */
    protected int theMaxLength;

    /**
     * Should the value be outputted in colloquial form.
     */
    protected boolean theColloquial;
}
