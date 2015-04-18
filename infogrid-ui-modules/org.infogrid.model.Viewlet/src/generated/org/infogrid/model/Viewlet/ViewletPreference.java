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
// Copyright 1998-2013 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

//
// This file has been generated AUTOMATICALLY. DO NOT MODIFY.
// on Sat, 2015-04-18 16:39:28 +0000
//
// DO NOT MODIFY -- re-generate!

package org.infogrid.model.Viewlet;

import org.infogrid.model.primitives.*;
import org.infogrid.mesh.*;
import org.infogrid.modelbase.ModelBaseSingleton;

/**
  * <p>Java interface for EntityType org.infogrid.model.Viewlet/ViewletPreference.</p>
  * <p>To instantiate, use the factory methods provided by the <code>MeshBase</code>'s
  * <code>MeshObjectLifecycleManager</code>.</p>
  * <p><b>Note:</b> As an application programmer, you must never rely on any characteristic of any
  * class that implements this interface, only on the characteristics provided by this interface
  * and its supertypes.</p>
  *
  * <table>
  *  <tr><td>Identifier:</td><td><tt>org.infogrid.model.Viewlet/ViewletPreference</tt></td></tr>
  *  <tr><td>Name:</td><td><tt>ViewletPreference</tt></td></tr>
  *  <tr><td>IsAbstract:</td><td><code>false</code></td></tr>
  *  <tr><td>UserVisibleName:</td><td><table><tr><td>default locale:</td><td>Viewlet Preference</td></tr></table></td></tr>
  *  <tr><td>UserVisibleDescription:</td><td><table><tr><td>default locale:</td><td>Enables any MeshObject to express its preference of Viewlet type for rendering.</td></tr></table></td></tr>
  * </table>
  */

public interface ViewletPreference
        extends
            org.infogrid.mesh.TypedMeshObjectFacade
{
    /**
      * Subject area in which this MeshObject is declared.
      */
    public static final SubjectArea _SUBJECTAREA = ModelBaseSingleton.findSubjectArea( "org.infogrid.model.Viewlet" );

    /**
      * This MeshType.
      */
    public static final EntityType _TYPE = ModelBaseSingleton.findEntityType( "org.infogrid.model.Viewlet/ViewletPreference" );

    /**
      * <p>Set value for property ViewletType.</p>
      *
      * <table>
      *  <tr><td>Identifier:</td><td><tt>org.infogrid.model.Viewlet/ViewletPreference_ViewletType</tt></td></tr>
      *  <tr><td>Name:</td><td><tt>ViewletType</tt></td></tr>
      *  <tr><td>DataType:</td><td><tt>String</tt></td></tr>
      *  <tr><td>DefaultValue:</td><td><tt><code>null</code></tt></td></tr>
      *  <tr><td>IsOptional:</td><td><tt><code>true</code></tt></td></tr>
      *  <tr><td>IsReadOnly:</td><td><tt><code>false</code></tt></td></tr>
      *  <tr><td>SequenceNumber:</td><td><tt>0.0</tt></td></tr>
      *  <tr><td>UserVisibleName:</td><td><table><tr><td>default locale:</td><td>Viewlet Type</td></tr></table></td></tr>
      *  <tr><td>UserVisibleDescription:</td><td><table><tr><td>default locale:</td><td>Name of the Viewlet Type that should be used to render this MeshObject</td></tr></table></td></tr>
      * </table>
      *
       * @param newValue the new value for this property
       * @throws NotPermittedException thrown if this caller was not allowed to perform this operation
       * @throws TransactionException thrown if this operation is invoked from outside of a Transaction
      */
    public abstract void setViewletType(
            org.infogrid.model.primitives.StringValue newValue )
        throws
            org.infogrid.mesh.NotPermittedException,
            org.infogrid.meshbase.transaction.TransactionException;

    /**
      * <p>Obtain value for property ViewletType.</p>
      *
      * <table>
      *  <tr><td>Identifier:</td><td><tt>org.infogrid.model.Viewlet/ViewletPreference_ViewletType</tt></td></tr>
      *  <tr><td>Name:</td><td><tt>ViewletType</tt></td></tr>
      *  <tr><td>DataType:</td><td><tt>String</tt></td></tr>
      *  <tr><td>DefaultValue:</td><td><tt><code>null</code></tt></td></tr>
      *  <tr><td>IsOptional:</td><td><tt><code>true</code></tt></td></tr>
      *  <tr><td>IsReadOnly:</td><td><tt><code>false</code></tt></td></tr>
      *  <tr><td>SequenceNumber:</td><td><tt>0.0</tt></td></tr>
      *  <tr><td>UserVisibleName:</td><td><table><tr><td>default locale:</td><td>Viewlet Type</td></tr></table></td></tr>
      *  <tr><td>UserVisibleDescription:</td><td><table><tr><td>default locale:</td><td>Name of the Viewlet Type that should be used to render this MeshObject</td></tr></table></td></tr>
      * </table>
      *
      * @return the current value of the property
      * @throws NotPermittedException thrown if this caller was not permitted to perform this operation
      */
    public abstract org.infogrid.model.primitives.StringValue getViewletType()
        throws
            org.infogrid.mesh.NotPermittedException;

    /**
      * Name of the ViewletType property.
      */
    public static final String VIEWLETTYPE_name = "ViewletType";

    /**
      * The ViewletType PropertyType.
      */
    public static final PropertyType VIEWLETTYPE = ModelBaseSingleton.findPropertyType( "org.infogrid.model.Viewlet/ViewletPreference_ViewletType" );
    public static final org.infogrid.model.primitives.StringDataType VIEWLETTYPE_type = (org.infogrid.model.primitives.StringDataType) VIEWLETTYPE.getDataType();

}
