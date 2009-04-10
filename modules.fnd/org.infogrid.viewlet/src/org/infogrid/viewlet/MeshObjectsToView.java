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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.viewlet;

import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.model.traversal.TraversalSpecification;
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
     * @return the created MeshObjectsToView
     */
    public static MeshObjectsToView create(
            MeshObject subject )
    {
        return new MeshObjectsToView(
                subject,
                subject != null ? subject.getIdentifier() : null,
                null,
                null,
                null,
                null );
    }

    /**
     * Factory method. Used when the subject shall be specified, and the name of Viewlet type to use.
     * 
     * @param subject the subject for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @return the created MeshObjectsToView
     */
    public static MeshObjectsToView create(
            MeshObject         subject,
            String             viewletTypeName )
    {
        return new MeshObjectsToView(
                subject,
                subject != null ? subject.getIdentifier() : null,
                null,
                viewletTypeName,
                null,
                null );
    }


    /**
     * Factory method, specifying all parameters.
     * 
     * @param subject the subject for the Viewlet
     * @param subjectParameters the parameters for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     * @return the created MeshObjectsToView
     */
    public static MeshObjectsToView create(
            MeshObject             subject,
            Map<String,Object>     subjectParameters,
            String                 viewletTypeName,
            Map<String,Object>     viewletParameters,
            TraversalSpecification traversalSpecification )
    {
        return new MeshObjectsToView(
                subject,
                subject != null ? subject.getIdentifier() : null,
                subjectParameters,
                viewletTypeName,
                viewletParameters,
                traversalSpecification );
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param subject the subject for the Viewlet
     * @param subjectIdentifier the identifier of the subject for the Viewlet
     * @param subjectParameters the parameters for the Viewlet
     * @param viewletTypeName the type of Viewlet to use
     * @param viewletParameters the Viewlet parameters (eg size, zoom, ...) to use
     * @param traversalSpecification the TraversalSpecification to apply when viewing the subject
     */
    protected MeshObjectsToView(
            MeshObject             subject,
            MeshObjectIdentifier   subjectIdentifier,
            Map<String,Object>     subjectParameters,
            String                 viewletTypeName,
            Map<String,Object>     viewletParameters,
            TraversalSpecification traversalSpecification )
    {
        theSubject                = subject;
        theSubjectParameters      = subjectParameters;
        theViewletTypeName        = viewletTypeName;
        theViewletParameters      = viewletParameters;
        theTraversalSpecification = traversalSpecification;
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
     * Obtain the parameters for the subject.
     *
     * @return the parameters for the subject, if any
     */
    public Map<String,Object> getSubjectParameters()
    {
        return theSubjectParameters;
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
     */
    public Object getViewletParameter(
            String name )
    {
        if( theViewletParameters != null ) {
            Object ret = theViewletParameters.get( name );
            return ret;
        }
        return null;
    }

    /**
     * Obtain the parameters that the Viewlet is supposed to use.
     *
     * @return the parameters that the Viewlet is supposed to use
     */
    public Map<String,Object> getViewletParameters()
    {
        return theViewletParameters;
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
                    "subjectPars",
                    "viewletTypeName",
                    "viewletPars",
                    "traversalSpecification"
                },
                new Object[] {
                    theSubject,
                    theSubjectIdentifier,
                    theSubjectParameters,
                    theViewletTypeName,
                    theViewletParameters,
                    theTraversalSpecification
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
     * The parameters for the subject, if any.
     */
    protected Map<String,Object> theSubjectParameters;

    /**
     * The type of Viewlet we would like to view the subject.
     */
    protected String theViewletTypeName;

    /**
     * The parameters that we would like the Viewlet to use when viewing the selected objects.
     */
    protected Map<String,Object> theViewletParameters;
    
    /**
     * The TraversalSpecification from the subject, if any.
     */
    protected TraversalSpecification theTraversalSpecification;
}
