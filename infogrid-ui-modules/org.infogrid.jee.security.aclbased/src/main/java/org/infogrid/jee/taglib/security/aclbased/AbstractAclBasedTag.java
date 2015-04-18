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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.security.aclbased;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.security.ThreadIdentityManager;
import org.infogrid.meshbase.security.aclbased.utils.AclUtils;
import org.infogrid.model.primitives.RoleType;

/**
 * Factors out functionality common to testing whether or not the caller has access of a certain type
 * to a given MeshObject.
 */
public abstract class AbstractAclBasedTag
    extends
        AbstractRestInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractAclBasedTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theTestObject            = null;
        theTestObjectName        = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the testObject property.
     *
     * @return value of the testObject property
     * @see #setTestObject
     */
    public final String getTestObject()
    {
        return theTestObject;
    }

    /**
     * Set value of the testObject property.
     *
     * @param newValue new value of the testObject property
     * @see #getTestObject
     */
    public final void setTestObject(
            String newValue )
    {
        theTestObject = newValue;
    }

    /**
     * Obtain value of the testObjectName property.
     *
     * @return value of the testObjectName property
     * @see #setTestObjectName
     */
    public final String getTestObjectName()
    {
        return theTestObjectName;
    }

    /**
     * Set value of the testObjectName property.
     *
     * @param newValue new value of the testObjectName property
     * @see #getTestObjectName
     */
    public final void setTestObjectName(
            String newValue )
    {
        theTestObjectName = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Evaluate the condition. If it returns true, the content of this tag is processed.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * Evaluate whether the caller has access of a certain type to a given MeshObject.
     *
     * @param roleType the RoleType that indicates the type of access
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean hasAccess(
            RoleType roleType )
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj;
        if( theTestObject != null ) {
            obj = findMeshObjectOrThrow( theTestObject );
        } else {
            obj = (MeshObject) lookupOrThrow( theTestObjectName );
        }

        boolean ret = AclUtils.hasAccess( ThreadIdentityManager.getCaller(), roleType, obj );
        return ret;
    }

    /**
     * Evaluate whether the caller is the owner of a given MeshObject.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean isOwner()
        throws
            JspException,
            IgnoreException
    {
        MeshObject caller = ThreadIdentityManager.getCaller();
        if( caller == null ) {
            return false;
        }

        MeshObject obj;
        if( theTestObject != null ) {
            obj = findMeshObjectOrThrow( theTestObject );
        } else {
            obj = (MeshObject) lookupOrThrow( theTestObjectName );
        }

        boolean ret = AclUtils.isOwner( caller, obj );
        return ret;
    }

    /**
     * String form of the MeshObject's identifier.
     */
    protected String theTestObject;

    /**
     * Name of the bean that contains the MeshObject.
     */
    protected String theTestObjectName;
}
