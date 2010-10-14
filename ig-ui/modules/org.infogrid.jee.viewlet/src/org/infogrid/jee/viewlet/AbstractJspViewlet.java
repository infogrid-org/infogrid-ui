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

package org.infogrid.jee.viewlet;

import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;

/**
 * Factors out common functionality of JSP-based Viewlets.
 * To avoid having to write many often rather shallow subclasses of this class, subclasses
 * of this class can have an "effective class name" which allows the same Viewlet class to
 * be used with many different JSPs, while acting as-if the Viewlet class had been subclassed
 * to match the name of the JSPs.
 */
public abstract class AbstractJspViewlet
        extends
            AbstractJeeViewlet
{
    /**
     * Constructor, for subclasses only.
     *
     * @param pseudoClassName the fully-qualified class name of the class that will be impersonated
     * @param viewed the JeeViewedMeshObjects to use
     * @param c the application context
     */
    protected AbstractJspViewlet(
            String               pseudoClassName,
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );

        thePseudoClassName = pseudoClassName;
    }

    /**
     * Obtain the Html class name for this Viewlet. By default, it is the Java class
     * name, having replaced all periods with hyphens.
     *
     * @return the HTML class name
     */
    @Override
    public String getHtmlClass()
    {
        String ret = thePseudoClassName;

        ret = ret.replaceAll( "\\.", "-" );

        return ret;
    }

    /**
     * Obtain the path to the Servlet for this JeeViewlet.
     *
     * @return the Servlet path
     */
    @Override
    public String getServletPath()
    {
        String ret = constructDefaultDispatcherUrl( thePseudoClassName );
        return ret;
    }

    /**
     * Obtain the computable name of the Viewlet.
     *
     * @return the Viewet's name
     */
    @Override
    public String getName()
    {
        return thePseudoClassName;
    }

    /**
     * Obtain a String, to be shown to the user, that identifies this Viewlet to the user.
     * This default implementation can be overridden by subclasses.
     *
     * @return a String
     */
    @Override
    public String getUserVisibleName()
    {
        ResourceHelper rh = ResourceHelper.getInstance( thePseudoClassName, DefaultJspViewletFactoryChoice.class.getClassLoader() );
        
        String userVisibleName = rh.getResourceStringOrDefault( "UserVisibleName", thePseudoClassName );
        return userVisibleName;
    }

    /**
     * The name of the Viewlet class this would have been if it had been created as a separate
     * Viewlet class.
     */
    protected String thePseudoClassName;
}
