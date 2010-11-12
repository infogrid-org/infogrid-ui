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

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that displays the identifier of a MeshObject.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectIdentifierTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectIdentifierTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName       = null;
        theMeshObject           = null;
        theStringRepresentation = null;
        theMaxLength            = -1;

        super.initializeToDefaults();
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
     * Set value of the meshObjectBean property.
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
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public MeshObject getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public void setMeshObject(
            MeshObject newValue )
    {
        theMeshObject = newValue;
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
        MeshObject obj;
        if( theMeshObject != null ) {
            obj = theMeshObject;
        } else {
            obj = (MeshObject) lookupOrThrow( theMeshObjectName );
        }

        if( obj != null ) {
            try {
                String text = ((RestfulJeeFormatter)theFormatter).formatMeshObjectIdentifier( pageContext, obj, theStringRepresentation, theMaxLength );
                print( text );

            } catch( StringifierException ex ) {
                throw new JspException( ex );
            }
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Name of the bean that holds the MeshObject (mutually exclusive with theMeshObject).
     */
    protected String theMeshObjectName;

    /**
     * The MeshObject (mutually exclusive with theMeshObjectName).
     */
    protected MeshObject theMeshObject;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
    
    /**
     * The maximum length of an emitted String.
     */
    protected int theMaxLength;
}
