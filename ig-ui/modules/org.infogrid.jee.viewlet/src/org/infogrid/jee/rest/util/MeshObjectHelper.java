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

package org.infogrid.jee.rest.util;

import java.lang.reflect.Field;
import javax.servlet.ServletException;
import javax.servlet.jsp.PageContext;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.TypedMeshObjectFacade;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.modelbase.ModelBase;

/**
 * Utilities to make JSP programming simpler.
 */
public abstract class MeshObjectHelper
{
    /**
     * Private constructor to keep this abstract.
     */
    private MeshObjectHelper()
    {
        // no op
    }

    /**
     * Given the name of a request attribute holding a MeshObject, and the String representation
     * of a EntityType's identifier, obtain the corresponding MeshObjectFacade. This will throw
     * an exception of the MeshObject is not blessed with the needed EntityType.
     *
     * @param pageContext the page context of the JSP
     * @param meshObjectName the name of the attribute in which the MeshObject is held
     * @param typeString the identifier of the MeshType
     * @return the MeshObjectFacade
     * @throws ServletException thrown if a problem occurred; the cause provides more details
     */
    public static TypedMeshObjectFacade getFacadeFor(
            PageContext pageContext,
            String      meshObjectName,
            String      typeString )
        throws
            ServletException
    {
        try {
            MeshObject obj = (MeshObject) pageContext.getRequest().getAttribute( meshObjectName );
            ModelBase  mb  = obj.getMeshBase().getModelBase();

            EntityType type = mb.findEntityTypeByIdentifier( mb.getMeshTypeIdentifierFactory().fromExternalForm( typeString ));
            TypedMeshObjectFacade ret = obj.getTypedFacadeFor( type );
            return ret;

        } catch( Throwable t ) {
            throw new ServletException( t );
        }
    }

    /**
     * Given the name of a request attribute holding a MeshObject, and a class generated from
     * an EntityType, obtain the corresponding MeshObjectFacade, downcast to the class. This will throw
     * an exception of the MeshObject is not blessed with the needed EntityType.
     *
     * @param pageContext the page context of the JSP
     * @param meshObjectName the name of the attribute in which the MeshObject is held
     * @param facadeClass the class of the MeshObjectFacade
     * @return the MeshObjectFacade
     * @throws ServletException thrown if a problem occurred; the cause provides more details
     */
    public static <T extends TypedMeshObjectFacade> T getFacadeFor(
            PageContext pageContext,
            String      meshObjectName,
            Class<T>    facadeClass )
        throws
            ServletException
    {
        try {
            MeshObject obj = (MeshObject) pageContext.getRequest().getAttribute( meshObjectName );
            ModelBase  mb  = obj.getMeshBase().getModelBase();

            Field typeField = facadeClass.getField( "_TYPE" );
            EntityType type = (EntityType) typeField.get( null );
            TypedMeshObjectFacade ret = obj.getTypedFacadeFor( type );
            return facadeClass.cast( ret );

        } catch( Throwable t ) {
            throw new ServletException( t );
        }
    }
}
