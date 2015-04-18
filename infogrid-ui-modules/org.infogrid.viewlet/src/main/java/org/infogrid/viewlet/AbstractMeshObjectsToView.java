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

package org.infogrid.viewlet;

import java.util.HashMap;
import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.NotSingleMemberException;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Dumper;

/**
 * Factors out common functionality of MeshObjectsToView objects.
 */
public abstract class AbstractMeshObjectsToView
        extends
            AbstractObjectInContext
        implements
            MeshObjectsToView
{
    /**
     * Private constructor, use subclass.
     *
     * @param subject the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param arrivedAtPath the TraversalPath by which we arrived here, if any
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     * @param traversalPaths the TraversalPaths to the highlighted objects
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the Context
     */
    protected AbstractMeshObjectsToView(
            MeshObject             subject,
            String                 viewletTypeName,
            Map<String,Object[]>   viewletParameters,
            TraversalPath          arrivedAtPath,
            TraversalSpecification traversalSpecification,
            TraversalPathSet       traversalPaths,
            MeshBase               mb,
            Context                c )
    {
        super( c );

        theSubject                = subject;
        theViewletTypeName        = viewletTypeName;
        theViewletParameters      = viewletParameters;
        theArrivedAtPath          = arrivedAtPath;
        theTraversalSpecification = traversalSpecification;
        theTraversalPaths         = traversalPaths;
        theMeshBase               = mb;
    }

    /**
     * Obtain the subject that the Viewlet is supposed to view.
     *
     * @return the subject
     */
    public MeshObject getSubject()
    {
        return theSubject;
    }

    /**
     * Obtain the name representing the Viewlet type that the Viewlet is supposed to support.
     *
     * @return the name representing the Viewlet type that the Viewlet is supposed to support
     */
    public String getViewletTypeName()
    {
        return theViewletTypeName;
    }

    /**
     * Set the name representing the Viewlet type that the Viewlet is supposed to support.
     *
     * @param newValue the new value
     */
    public void setViewletTypeName(
            String newValue )
    {
        theViewletTypeName = newValue;
    }

    /**
     * Obtain the value of a named Viewlet parameter.
     *
     * @param name the name of the Viewlet parameter
     * @return the value, if any
     * @throws NotSingleMemberException if a Viewlet parameter had more than one value
     */
    public Object getViewletParameter(
            String name )
        throws
            NotSingleMemberException
    {
        if( theViewletParameters == null ) {
            return null;
        }

        Object [] ret = theViewletParameters.get( name );
        if( ret == null ) {
            return null;
        }
        switch( ret.length ) {
            case 0:
                return null;

            case 1:
                return ret[0];

            default:
                throw new NotSingleMemberException( "Parameter name has more than one value", ret.length );
        }
    }

    /**
     * Obtain all values of a multi-valued Viewlet parameter.
     *
     * @param name the name of the Viewlet parameter
     * @return the values, if any
     */
    public Object [] getMultivaluedViewletParameter(
            String name )
    {
        if( theViewletParameters == null ) {
            return null;
        }

        Object [] ret = theViewletParameters.get( name );
        return ret;
    }

    /**
     * Obtain the parameters that the Viewlet is supposed to use.
     *
     * @return the parameters that the Viewlet is supposed to use
     */
    public Map<String,Object[]> getViewletParameters()
    {
        return theViewletParameters;
    }

    /**
     * Set all values of a multi-valued Viewlet parameter.
     *
     * @param name the name of the Viewlet parameter
     * @param values the values
     */
    public void setMultivaluedViewletParameter(
            String    name,
            Object [] values )
    {
        if( theViewletParameters == null ) {
            theViewletParameters = new HashMap<String,Object[]>();
        }
        theViewletParameters.put( name, values );
    }

    /**
     * Determine how we arrived at this Viewlet, if available. This
     * is most likely a member of the TraversalPathSet of the parent Viewlet.
     *
     * @return the TraversalPath through which we arrived here
     */
    public TraversalPath getArrivedAtPath()
    {
        return theArrivedAtPath;
    }

    /**
     * Set how we arrived at this Viewlet.
     *
     * @param newValue the new value
     */
    public void setArrivedAtPath(
            TraversalPath newValue )
    {
        theArrivedAtPath = newValue;
    }

    /**
     * Obtain the TraversalSpecification that the Viewlet is supposed to use.
     *
     * @return the TraversalSpecification that the Viewlet is supposed to use
     */
    public TraversalSpecification getTraversalSpecification()
    {
        return theTraversalSpecification;
    }

    /**
     * Set the TraversalSpecification that the Viewlet is supposed to use.
     *
     * @param newValue the new value
     */
    public void setTraversalSpecification(
            TraversalSpecification newValue )
    {
        theTraversalSpecification = newValue;
    }

    /**
     * Obtain the reached Objects by means of their TraversalPaths.
     *
     * @return the TraversalPaths
     */
    public TraversalPathSet getTraversalPaths()
    {
        return theTraversalPaths;
    }

    /**
     * Obtain the MeshBase from which the MeshObjects are taken.
     *
     * @return the MeshBase
     */
    public MeshBase getMeshBase()
    {
        return theMeshBase;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "subject",
                    "viewletTypeName",
                    "viewletPars",
                    "arrivedAtPath",
                    "traversalSpecification",
                    "traversalPaths"
                },
                new Object[] {
                    theSubject,
                    theViewletTypeName,
                    theViewletParameters,
                    theArrivedAtPath,
                    theTraversalSpecification,
                    theTraversalPaths
        });
    }

    /**
     * The subject to view.
     */
    protected transient MeshObject theSubject;

    /**
     * The type of Viewlet we would like to view the subject.
     */
    protected String theViewletTypeName;

    /**
     * The parameters that we would like the Viewlet to use when viewing the selected objects.
     * This is multi-valued.
     */
    protected Map<String,Object[]> theViewletParameters;

    /**
     * The path by which we got here. This is most likely a member of the TraversalPathSet of
     * the parent Viewlet (if any).
     */
    protected TraversalPath theArrivedAtPath;

    /**
     * The TraversalSpecification from the subject, if any.
     */
    protected TraversalSpecification theTraversalSpecification;

    /**
     * The TraversalPaths to the highlighed objects, if any.
     */
    protected TraversalPathSet theTraversalPaths;

    /**
     * The MeshBase from which the viewed MeshObjects are taken.
     */
    protected transient MeshBase theMeshBase;
}
