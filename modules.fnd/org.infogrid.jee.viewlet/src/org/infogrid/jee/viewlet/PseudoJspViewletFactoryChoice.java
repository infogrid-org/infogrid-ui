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

package org.infogrid.jee.viewlet;

import java.util.ArrayList;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;

/**
 * <p>A ViewletFactoryChoice that instantiates the SimpleJeeViewlet as default, pretending to
 *    be a Viewlet class with a certain name, called the <code>pseudoClassName</code>. This
 *    is identical to creating a DefaultViewletFactoryChoice with a Viewlet class named
 *    pseudoClassName that does not add any functionality itself.</p>
 * <p>The main purpose of this class is to avoid having to write empty Viewlet classes.</p>
 */
public class PseudoJspViewletFactoryChoice
        extends
            DefaultViewletFactoryChoice
{
    /**
     * Factory method.
     *
     * @param pseudoClassName the name of the (non-exististing) Viewlet class
     * @return the created PseudoJspViewletFactoryChoice
     */
    public static PseudoJspViewletFactoryChoice create(
            String pseudoClassName )
    {
        PseudoJspViewletFactoryChoice ret = create( pseudoClassName, AVERAGE_MATCH_QUALITY );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param pseudoClassName the name of the (non-exististing) Viewlet class
     * @param matchQuality the match quality
     * @return the created PseudoJspViewletFactoryChoice
     */
    public static PseudoJspViewletFactoryChoice create(
            String pseudoClassName,
            double matchQuality )
    {
        PseudoJspViewletFactoryChoice ret = new PseudoJspViewletFactoryChoice( pseudoClassName, matchQuality );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param pseudoClassName the name of the (non-exististing) Viewlet class
     * @param matchQuality the match quality
     */
    protected PseudoJspViewletFactoryChoice(
            String         pseudoClassName,
            double         matchQuality )
    {
        super( SimpleJeeViewlet.class, matchQuality );

        thePseudoClassName = pseudoClassName;
    }
    
    /**
     * Obtain a user-visible String to display to the user for this ViewletFactoryChoice.
     *
     * @return the user-visible String
     */
    @Override
    public String getUserVisibleName()
    {
        ResourceHelper rh  = ResourceHelper.getInstance( thePseudoClassName, PseudoJspViewletFactoryChoice.class.getClassLoader()  );

        String ret = rh.getResourceStringOrDefault( "UserVisibleName", thePseudoClassName );

        return ret;
    }

    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewlet's name
     */
    @Override
    public String getName()
    {
        return thePseudoClassName;
    }

    /**
     * Internal helper method that recursively looks up the names of all interface
     * and class names supported by a Class.
     *
     * @param clazz the Class
     * @param found the set of names found
     */
    @Override
    protected void determineClassNames(
            Class             clazz,
            ArrayList<String> found )
    {
        // We do what our superclass does, but add thePseudoClassName
        
        super.determineClassNames( clazz, found );
        found.add( thePseudoClassName );
    }

    /**
     * Helper method to instantiate a ViewletFactoryChoice into a Viewlet. The use of this
     * method is optional by implementations.
     * 
     * @param toView the MeshObjectsToView; only used for error reporting
     * @param viewletClass the Viewlet Class to instantiate
     * @param c the Context to use
     * @return the instantiated Viewlet
     * @throws CannotViewException if, against expectations, the Viewlet corresponding
     *         to this ViewletFactoryChoice could not view the MeshObjectsToView after
     *         all. This usually indicates a programming error.
     */
    @Override
    protected Viewlet instantiateViewlet(
            MeshObjectsToView        toView,
            Class<? extends Viewlet> viewletClass,
            Context                  c )
        throws
            CannotViewException
    {
        PseudoJspViewlet ret = PseudoJspViewlet.create( thePseudoClassName, c );
        
        return ret;
    }

    /**
     * The name of the Viewlet class this would have been if it had been created as a separate
     * Viewlet class.
     */
    protected String thePseudoClassName;
}
