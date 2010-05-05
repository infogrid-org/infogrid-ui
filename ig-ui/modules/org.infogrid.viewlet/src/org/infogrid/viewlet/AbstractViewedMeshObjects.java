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

import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.NotSingleMemberException;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Factors out common functionality of ViewedMeshObjects implementations.
 */
public abstract class AbstractViewedMeshObjects
        implements
            ViewedMeshObjects,
            CanBeDumped
{
    /**
     * Constructor. Initializes to empty content. After the constructor has
     * been called, the setViewlet method has to be invoked to tell the ViewedMeshObjects
     * about its Viewlet.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken.
     */
    public AbstractViewedMeshObjects(
            MeshBase mb )
    {
        theMeshBase = mb;
    }

    /**
     * Set the Viewlet to which this ViewedMeshObjects belongs.
     *
     * @param v the Viewlet that we belong to.
     */
    public void setViewlet(
            Viewlet v )
    {
        theViewlet = v;
    }

    /**
     * Through this method, the Viewlet that this object belongs to updates this object.
     *
     * @param subject the new subject of the Viewlet
     * @param viewletParameters the parameters of the Viewlet, if any
     * @param traversal the TraversalSpecification currently in effect on the Viewlet, if any
     * @param traversalPaths the TraversalPaths to the currently highlighted objects, if any
     * @param arrivedAtPath the TraversalPath through which we arrived at this Viewlet, if any
     */
    public void update(
            MeshObject             subject,
            Map<String,Object[]>   viewletParameters,
            TraversalSpecification traversal,
            TraversalPathSet       traversalPaths,
            TraversalPath          arrivedAtPath )
    {
        theSubject                = subject;
        theViewletParameters      = viewletParameters;
        theTraversalSpecification = traversal;
        theTraversalPaths         = traversalPaths;
        theArrivedAtPath          = arrivedAtPath;

        if( theTraversalPaths != null ) {
            theReachedObjects = theTraversalPaths.getDestinationsAsSet();
        } else {
            theReachedObjects = null;
        }
    }

    /**
     * Through this convenience method, the Viewlet that this object belongs to updates this object.
     *
     * @param newObjectsToView the new objects accepted to be viewed by the Viewlet
     */
    public void updateFrom(
            MeshObjectsToView newObjectsToView )
    {
        update( newObjectsToView.getSubject(),
                newObjectsToView.getViewletParameters(),
                newObjectsToView.getTraversalSpecification(),
                newObjectsToView.getTraversalPaths(),
                newObjectsToView.getArrivedAtPath() );
    }

    /**
     * Obtain the current subject of the Viewlet. As long as the Viewlet
     * displays any information whatsoever, this is non-null.
     *
     * @return the subject MeshObject
     */
    public final MeshObject getSubject()
    {
        return theSubject;
    }

    /**
      * Obtain the Viewlet by which these objects are viewed.
      *
      * @return the Viewlet by which these objects are viewed.
      */
    public final Viewlet getViewlet()
    {
        return theViewlet;
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
                throw new NotSingleMemberException( "Parameter '" + name + "' has more than one value", ret.length );
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
     * Obtain the parameters of the viewing Viewlet.
     *
     * @return the parameters of the viewing Viewlet, if any.
     */
    public final Map<String,Object[]> getViewletParameters()
    {
        return theViewletParameters;
    }

    /**
     * Determine how we arrived at this Viewlet, if available. This
     * is most likely a member of the TraversalPathSet of the parent Viewlet.
     *
     * @return the TraversalPath through which we arrived here
     */
    public final TraversalPath getArrivedAtPath()
    {
        return theArrivedAtPath;
    }

    /**
     * Obtain the TraversalSpecification that the Viewlet currently uses.
     * 
     * @return the TraversalSpecification that the Viewlet currently uses
     */
    public final TraversalSpecification getTraversalSpecification()
    {
        return theTraversalSpecification;
    }

    /**
     * Obtain the set of TraversalPaths that the Viewlet currently uses, if any.
     *
     * @return the TraversalPathSet
     */
    public TraversalPathSet getTraversalPathSet()
    {
        return theTraversalPaths;
    }

    /**
     * Obtain the Objects, i.e. the MeshObjects reached by traversing from the
     * Subject via the TraversalSpecification.
     * 
     * @return the Objects
     */
    public final MeshObjectSet getReachedObjects()
    {
        if( theReachedObjects != null ) {
            return theReachedObjects;
        }
        if( theTraversalSpecification != null ) {
            return theSubject.traverse( theTraversalSpecification );
        }
        return null;
    }

    /**
     * Obtain the MeshBase from which the viewed MeshObjects are taken.
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
                new String [] {
                        "subject",
                        "viewlet",
                        "reachedObjects",
                        "meshBase"
                },
                new Object [] {
                        theSubject,
                        theViewlet,
                        theReachedObjects,
                        theMeshBase
        } );
    }

    /**
     * The Viewlet that this object belongs to.
     */
    protected Viewlet theViewlet;

    /**
     * The current subject of the Viewlet.
     */
    protected MeshObject theSubject;

    /**
     * The Viewlet parameters, if any.
     */
    protected Map<String,Object[]> theViewletParameters;

    /**
     * The path through which we arrived at this Viewlet, if any.
     */
    protected TraversalPath theArrivedAtPath;

    /**
     * The TraversalSpecification, if any.
     */
    protected TraversalSpecification theTraversalSpecification;

    /**
     * The current set of TraversalPaths.
     */
    protected TraversalPathSet theTraversalPaths;

    /**
     * The set of MeshObjects.
     */
    protected MeshObjectSet theReachedObjects;

    /**
     * The MeshBase from which the viewed MeshObjects are taken.
     */
    protected MeshBase theMeshBase;
}
