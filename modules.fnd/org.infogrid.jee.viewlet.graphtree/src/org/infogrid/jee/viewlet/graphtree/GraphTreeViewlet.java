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

package org.infogrid.jee.viewlet.graphtree;

import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.DefaultMeshObjectSorter;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSetFactory;
import org.infogrid.mesh.set.MeshObjectSorter;
import org.infogrid.mesh.set.OrderedMeshObjectSet;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet that shows a MeshObject graph as a tree,
 */
public class GraphTreeViewlet
        extends
            AbstractJeeViewlet
{
    private static final Log log = Log.getLogInstance( GraphTreeViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param traversalSpecs the TraversalSpecifications from MeshObjects on one level to the MeshObjects on the next. Indexed by the level of the tree.
     * @param sorters the sorters to be applied on each level. Indexed by the level of the tree.
     * @param c the application context
     * @return the created Viewlet
     */
    public static GraphTreeViewlet create(
            TraversalSpecification [] traversalSpecs,
            MeshObjectSorter []       sorters,
            Context                   c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        GraphTreeViewlet         ret    = new GraphTreeViewlet( viewed, traversalSpecs, sorters, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param traversalSpecs the TraversalSpecifications from MeshObjects on one level to the MeshObjects on the next. Indexed by the level of the tree.
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            final TraversalSpecification [] traversalSpecs,
            double                          matchQuality )
    {
        return choice( traversalSpecs, null, matchQuality );
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param traversalSpecs the TraversalSpecifications from MeshObjects on one level to the MeshObjects on the next. Indexed by the level of the tree.
     * @param sorters the sorters to be applied on each level. Indexed by the level of the tree.
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            final TraversalSpecification [] traversalSpecs,
            MeshObjectSorter []             sorters,
            double                          matchQuality )
    {
        if( traversalSpecs == null || traversalSpecs.length == 0 ) {
            throw new IllegalArgumentException( "Require non-null TraversalSpecifications" );
        }

        final MeshObjectSorter [] realSorters;
        if( sorters == null ) {
            // use default
            realSorters = new MeshObjectSorter[ traversalSpecs.length ];
            for( int i=0 ; i<realSorters.length ; ++i ) {
                realSorters[i] = DefaultMeshObjectSorter.BY_IDENTIFIER;
            }
        } else if( sorters.length != traversalSpecs.length ) {
            throw new IllegalArgumentException( "MeshObjectSorter.length != TraversalSpecification.length" );
        } else {
            realSorters = sorters;
        }

        return new DefaultViewletFactoryChoice( GraphTreeViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet(
                        MeshObjectsToView        toView,
                        Context                  c )
                    throws
                        CannotViewException
                {
                    return create( traversalSpecs, realSorters, c );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param traversalSpecs the TraversalSpecifications from MeshObjects on one level to the MeshObjects on the next. Indexed by the level of the tree.
     * @param sorters the sorters to be applied on each level. Indexed by the level of the tree.
     * @param c the application context
     */
    protected GraphTreeViewlet(
            AbstractViewedMeshObjects viewed,
            TraversalSpecification [] traversalSpecs,
            MeshObjectSorter []       sorters,
            Context                   c )
    {
        super( viewed, c );

        theTraversalSpecs = traversalSpecs;
        theSorters        = sorters;
    }

    /**
     * Given the current start MeshObject, and the current level, determine the
     * set of child MeshObjects to be shown.
     *
     * @param start the start MeshObject
     * @param level the current level
     * @return the set of child MeshObjects to be shown
     */
    public OrderedMeshObjectSet subItems(
            MeshObject start,
            int        level )
    {
        MeshObjectSetFactory setFactory = start.getMeshBase().getMeshObjectSetFactory();

        if( level >= theTraversalSpecs.length ) {
            return setFactory.obtainEmptyImmutableMeshObjectSet();
        }
        TraversalSpecification trav = theTraversalSpecs[ level ];
        MeshObjectSorter       sort = theSorters[ level ];

        MeshObjectSet found = start.traverse( trav );

        OrderedMeshObjectSet ret = setFactory.createOrderedImmutableMeshObjectSet( found, sort );
        return ret;
    }

    /**
     * The TraversalSpecifications from MeshObjects on one level to the MeshObjects on the next.
     * This is indexed by the level of the tree.
     */
    protected TraversalSpecification [] theTraversalSpecs;

    /**
     * The sorters to be applied on each level. This is indexed by the level of the tree.
     */
    protected MeshObjectSorter [] theSorters;
}
