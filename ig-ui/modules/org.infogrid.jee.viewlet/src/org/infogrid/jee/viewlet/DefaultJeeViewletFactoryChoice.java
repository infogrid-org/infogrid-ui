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

package org.infogrid.jee.viewlet;

import java.util.Deque;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.Viewlet;

/**
 * Defaul implementation of ViewletFactoryChoice for JeeViewlets.
 */
public abstract class DefaultJeeViewletFactoryChoice
        extends
            DefaultViewletFactoryChoice
        implements
            JeeViewletFactoryChoice
{
    /**
     * Private constructor, for subtypes only. Specify match quality.
     *
     * @param toView the JeeMeshObjectsToView for which this is a choice
     * @param viewletClass the Viewlet's class
     * @param matchQuality the match quality
     */
    protected DefaultJeeViewletFactoryChoice(
            JeeMeshObjectsToView     toView,
            Class<? extends Viewlet> viewletClass,
            double                   matchQuality )
    {
        super( toView, viewletClass, viewletClass.getName(), matchQuality );
    }

    /**
     * Private constructor, for subtypes only. Specify match quality.
     *
     * @param toView the JeeMeshObjectsToView for which this is a choice
     * @param viewletClass the Viewlet's class
     * @param viewletTypeName name of the Viewlet type that this represents, if any
     * @param matchQuality the match quality
     */
    protected DefaultJeeViewletFactoryChoice(
            JeeMeshObjectsToView     toView,
            Class<? extends Viewlet> viewletClass,
            String                   viewletTypeName,
            double                   matchQuality )
    {
        super( toView, viewletClass, viewletTypeName, matchQuality );
    }

    /**
     * Determine the MeshObjectsToView for which this is a choice.
     *
     * @return the MeshObjectsToView
     */
    @Override
    public JeeMeshObjectsToView getMeshObjectsToView()
    {
        return (JeeMeshObjectsToView) super.getMeshObjectsToView();
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation. Always provided.
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @return String representation
     */
    @Override
    @SuppressWarnings("unchecked")
    public String toStringRepresentationLinkStart(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        String contextPath = null;
        String target      = null;
        String title       = null;
        String pane        = null;
        Deque<JeeViewedMeshObjects> viewedMeshObjectsStack = null;

        if( pars != null ) {
            contextPath = (String) pars.get( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY );
            target      = (String) pars.get( StringRepresentationParameters.LINK_TARGET_KEY );
            title       = (String) pars.get( StringRepresentationParameters.LINK_TITLE_KEY );
            pane        = (String) pars.get( JeeViewlet.PANE_STRING_REPRESENTATION_PARAMETER_KEY );
            viewedMeshObjectsStack = (Deque<JeeViewedMeshObjects>) pars.get( JeeViewletFactoryChoice.VIEWEDMESHOBJECTS_STACK_KEY );
        }

        if( target == null ) {
            target = "_self";
        }

        String toViewUrl = getMeshObjectsToView().getAsUrl( PANE_TOP.equals( pane ) ? null : viewedMeshObjectsStack );

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                StringRepresentation.DEFAULT_LINK_START_ENTRY,
                pars,
        /* 0 */ this,
        /* 1 */ toViewUrl,
        /* 2 */ contextPath,
        /* 3 */ target,
        /* 4 */ title );

        return ret;
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation. Always provided.
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @return String representation
     */
    @Override
    @SuppressWarnings("unchecked")
    public String toStringRepresentationLinkEnd(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        String contextPath = null;
        String pane        = null;
        Deque<JeeViewedMeshObjects> viewedMeshObjectsStack = null;

        if( pars != null ) {
            contextPath = (String) pars.get( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY );
            pane        = (String) pars.get( JeeViewlet.PANE_STRING_REPRESENTATION_PARAMETER_KEY );
            viewedMeshObjectsStack = (Deque<JeeViewedMeshObjects>) pars.get( JeeViewletFactoryChoice.VIEWEDMESHOBJECTS_STACK_KEY );
        }

        String toViewUrl = getMeshObjectsToView().getAsUrl( PANE_TOP.equals( pane ) ? null : viewedMeshObjectsStack );

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                StringRepresentation.DEFAULT_LINK_END_ENTRY,
                pars,
        /* 0 */ this,
        /* 1 */ toViewUrl,
        /* 2 */ contextPath );

        return ret;
    }

    /**
     * Recognized value for the pane StringRepresentation parameter: Display in the current context.
     */
    public static final String PANE_HERE = "here"; // default

    /**
     * Recognized value for the pane StringRepresentation parameter: Display at the top of the pane.
     */
    public static final String PANE_TOP  = "top";
}
