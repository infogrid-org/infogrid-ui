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

package org.infogrid.jee.taglib.logic;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.model.traversal.TraversalDictionary;
import org.infogrid.model.traversal.TraversalSpecification;

/**
 * <p>Abstract superclass for all tags evaluating a MeshObjects related to a start MeshObject.</p>
 */
public abstract class AbstractRelatedTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractRelatedTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName             = null;
        theTraversalSpecification     = null;
        theTraversalSpecificationName = null;
        theMinFound                   = null;
        theMaxFound                   = null;
        
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
     * Set value of the meshObjectName property.
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
     * Obtain value of the traversalSpecification property.
     *
     * @return value of the traversalSpecification property
     * @see #setTraversalSpecification
     */
    public final String getTraversalSpecification()
    {
        return theTraversalSpecification;
    }

    /**
     * Set value of the traversalSpecification property.
     *
     * @param newValue new value of the traversalSpecification property
     * @see #getTraversalSpecification
     */
    public final void setTraversalSpecification(
            String newValue )
    {
        theTraversalSpecification = newValue;
    }

    /**
     * Obtain value of the traversalSpecificationName property.
     *
     * @return value of the traversalSpecificationName property
     * @see #setTraversalSpecificationName
     */
    public final String getTraversalSpecificationName()
    {
        return theTraversalSpecification;
    }

    /**
     * Set value of the traversalSpecificationName property.
     *
     * @param newValue new value of the traversalSpecificationName property
     * @see #getTraversalSpecificationName
     */
    public final void setTraversalSpecificationName(
            String newValue )
    {
        theTraversalSpecification = newValue;
    }

    /**
     * Obtain value of the minFound property.
     *
     * @return value of the minFound property
     * @see #setMinFound
     */
    public final String getMinFound()
    {
        return theMinFound;
    }

    /**
     * Set value of the minFound property.
     *
     * @param newValue new value of the minFound property
     * @see #getMinFound
     */
    public final void setMinFound(
            String newValue )
    {
        theMinFound = newValue;
    }
    
    /**
     * Obtain value of the maxFound property.
     *
     * @return value of the maxFound property
     * @see #setMaxFound
     */
    public final String getMaxFound()
    {
        return theMaxFound;
    }

    /**
     * Set value of the maxFound property.
     *
     * @param newValue new value of the minFound property
     * @see #getMaxFound
     */
    public final void setMaxFound(
            String newValue )
    {
        theMaxFound = newValue;
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
     * Evaluatate the condition. If it returns true, we include, in the output,
     * the content contained in this tag. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
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
     * Determine the PropertyValue to be used in the test.
     *
     * @return the PropertyValue
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected MeshObjectSet evaluate()
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj  = (MeshObject) lookupOrThrow( theMeshObjectName );

        TraversalSpecification spec;
        if( theTraversalSpecification != null ) {
            if( theTraversalSpecificationName != null ) {
                throw new JspException( "Must not specify both traversalSpecification and traversalSpecificationName" );
            } else {
                TraversalDictionary dict = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( TraversalDictionary.class );
                spec = dict.translate( obj, theTraversalSpecification );
            }
            
        } else if( theTraversalSpecificationName != null ) {
            spec = (TraversalSpecification) lookupOrThrow( theTraversalSpecificationName );

        } else {
            throw new JspException( "Must specify either traversalSpecification or traversalSpecificationName" );
        }

        MeshObjectSet found = obj.traverse( spec );
        return found;
    }

    /**
     * Determine the integer value of the provided String.
     *
     * @param s the String
     * @return the corresponding integer value
     */
    protected int determineValue(
            String s )
    {
        if( s == null || s.length() == 0 ) {
            return -1;
        }
        int ret = Integer.parseInt( s );
        return ret;
    }

    /**
     * String containing the name of the bean that is the MeshObject whose property is considered in the test.
     */
    protected String theMeshObjectName;

    /**
     * String containing the external form of a TraversalSpecification. This is mutually exclusive with theTraversalSpecificationName.
     */
    protected String theTraversalSpecification;

    /**
     * String containing the name of the bean that is the TraversalSpecification. This is mutually exclusive with theTraversalSpecification.
     */
    protected String theTraversalSpecificationName;

    /**
     * The minimum number of MeshObjects found.
     */
    protected String theMinFound;

    /**
     * The maximum number of MeshObjects found.
     */
    protected String theMaxFound;
}
