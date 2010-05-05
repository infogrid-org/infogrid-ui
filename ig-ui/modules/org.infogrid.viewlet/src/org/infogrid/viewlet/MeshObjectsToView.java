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
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.NotSingleMemberException;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Instances of this class are being used to tell a Viewlet which MeshObjects it is supposed
 * to view, in which context, with which parameters etc.
 */
public class MeshObjectsToView
        implements
            CanBeDumped
{
    /**
     * Factory method. Used when only the subject shall be specified
     * 
     * @param subject the subject for the Viewlet
     * @param request the incoming RestfulRequest
     * @return the created MeshObjectsToView
     */
    public static MeshObjectsToView create(
            MeshObject     subject,
            RestfulRequest request )
    {
        return new MeshObjectsToView(
                subject,
                subject.getIdentifier(),
                null,
                null,
                null,
                null,
                null,
                request,
                subject.getMeshBase() );
    }

    /**
     * Factory method. Used when the subject shall be specified, and the name of Viewlet type to use.
     * 
     * @param subject the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param request the incoming RestfulRequest
     * @return the created MeshObjectsToView
     */
    public static MeshObjectsToView create(
            MeshObject         subject,
            String             viewletTypeName,
            RestfulRequest     request )
    {
        return new MeshObjectsToView(
                subject,
                subject.getIdentifier(),
                viewletTypeName,
                null,
                null,
                null,
                null,
                request,
                subject.getMeshBase() );
    }

    /**
     * Factory method.
     *
     * @param subject the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param arrivedAtPath the TraversalPath by which we arrived here, if any
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     * @param traversalPath the TraversalPath to the single highlighted object
     * @param request the incoming RestfulRequest
     * @return the created MeshObjectsToView
     */
    public static MeshObjectsToView create(
            MeshObject             subject,
            String                 viewletTypeName,
            Map<String,Object[]>   viewletParameters,
            TraversalPath          arrivedAtPath,
            TraversalSpecification traversalSpecification,
            TraversalPath          traversalPath,
            RestfulRequest         request )
    {
        MeshBase mb = subject.getMeshBase();

        return new MeshObjectsToView(
                subject,
                subject.getIdentifier(),
                viewletTypeName,
                viewletParameters,
                arrivedAtPath,
                traversalSpecification,
                traversalPath != null ? mb.getMeshObjectSetFactory().createSingleMemberImmutableTraversalPathSet( traversalPath ) : null,
                request,
                subject.getMeshBase());
    }

    /**
     * Factory method.
     *
     * @param subject the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param arrivedAtPath the TraversalPath by which we arrived here, if any
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     * @param traversalPaths the TraversalPaths to the highlighted objects
     * @param request the incoming RestfulRequest
     * @return the created MeshObjectsToView
     */
    public static MeshObjectsToView create(
            MeshObject             subject,
            String                 viewletTypeName,
            Map<String,Object[]>   viewletParameters,
            TraversalPath          arrivedAtPath,
            TraversalSpecification traversalSpecification,
            TraversalPathSet       traversalPaths,
            RestfulRequest         request )
    {
        return new MeshObjectsToView(
                subject,
                subject.getIdentifier(),
                viewletTypeName,
                viewletParameters,
                arrivedAtPath,
                traversalSpecification,
                traversalPaths,
                request,
                subject.getMeshBase());
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param subject the subject for the Viewlet
     * @param subjectIdentifier the identifier of the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param arrivedAtPath the TraversalPath by which we arrived here, if any
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     * @param traversalPaths the TraversalPaths to the highlighted objects
     * @param request the incoming RestfulRequest
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     */
    protected MeshObjectsToView(
            MeshObject             subject,
            MeshObjectIdentifier   subjectIdentifier,
            String                 viewletTypeName,
            Map<String,Object[]>   viewletParameters,
            TraversalPath          arrivedAtPath,
            TraversalSpecification traversalSpecification,
            TraversalPathSet       traversalPaths,
            RestfulRequest         request,
            MeshBase               mb )
    {
        theSubject                = subject;
        theViewletTypeName        = viewletTypeName;
        theViewletParameters      = viewletParameters;
        theArrivedAtPath          = arrivedAtPath;
        theTraversalSpecification = traversalSpecification;
        theTraversalPaths         = traversalPaths;
        theRequest                = request;
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
     * Obtain the identifier of the subject that the Viewlet is supposed to view.
     *
     * @return the subject's identifier
     */
    public MeshObjectIdentifier getSubjectIdentifier()
    {
        return theSubjectIdentifier;
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
     * Obtain the TraversalSpecification that the Viewlet is supposed to use.
     * 
     * @return the TraversalSpecification that the Viewlet is supposed to use
     */
    public TraversalSpecification getTraversalSpecification()
    {
        return theTraversalSpecification;
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
     * Obtain the incoming RestfulRequest as a result of which this MeshObjectsToView
     * was created.
     *
     * @return the incoming request
     */
    public RestfulRequest getIncomingRequest()
    {
        return theRequest;
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
                    "subjectIdentifier",
                    "viewletTypeName",
                    "viewletPars",
                    "arrivedAtPath",
                    "traversalSpecification",
                    "traversalPaths"
                },
                new Object[] {
                    theSubject,
                    theSubjectIdentifier,
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
     * The identifier of the subject to view.
     */
    protected MeshObjectIdentifier theSubjectIdentifier;

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
     * The incoming Restful request, as a result of which this MeshObjectsToView instance
     * was assembled.
     */
    protected RestfulRequest theRequest;

    /**
     * The MeshBase from which the viewed MeshObjects are taken.
     */
    protected transient MeshBase theMeshBase;
}
