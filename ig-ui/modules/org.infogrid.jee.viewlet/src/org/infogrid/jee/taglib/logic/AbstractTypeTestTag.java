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

package org.infogrid.jee.taglib.logic;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;

/**
 * <p>Abstract superclass for tags that perform tests related to the type of a MeshObject.</p>
 */
public abstract class AbstractTypeTestTag
    extends
        AbstractRestInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractTypeTestTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName = null;
        theIdentifier   = null;
        
        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectBean property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain the value of the Identifier property.
     * 
     * @return the value of the property property
     * @see #setIdentifier
     */
    public String getIdentifier()
    {
        return theIdentifier;
    }

    /**
     * Set the value of the Identifier property.
     * 
     * @param newValue the new value of the property property
     * @see #getIdentifier
     */
    public void setIdentifier(
            String newValue )
    {
        theIdentifier = newValue;
    }

    /**
     * Evaluatate the condition. If it returns true, we include output
     * the Nodes contained in this Node.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluateTest()
        throws
            JspException,
            IgnoreException
    {
        try {
            MeshObject    obj   = lookupMeshObjectOrThrow( theMeshObjectName );
            EntityType [] types = obj.getTypes();

            ModelBase modelBase = obj.getMeshBase().getModelBase();
            
            MeshTypeIdentifier identifier = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( theIdentifier );
            EntityType         comparison = modelBase.findEntityTypeByIdentifier( identifier );

            for( EntityType current : types ) {
                if( comparison.equalsOrIsSupertype( current )) {
                    return true;
                }
            }
            return false;

        } catch( MeshTypeNotFoundException ex ) {
            throw new JspException( ex );
        }
    }

    /**
     * String containing the name of the bean that is the MeshObject whose property we render.
     */
    protected String theMeshObjectName;

    /**
     * The Identifier property.
     */
    protected String theIdentifier;
}
