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

import org.infogrid.util.Factory;
import org.infogrid.util.FactoryException;

/**
 * <p>A factory for Viewlets. Objects supporting this interface not only support the
 *    regular {@link org.infogrid.util.Factory Factory} pattern, but also method
 *    {@link #determineFactoryChoices determineFactoryChoices}.</p>
 * <p>One can best visualize the purpose of this method in terms of a user interface:
 *    the obtained {@link ViewletFactoryChoice ViewletFactoryChoices} could
 *    be displayed as the choices the user has in executing the creation method of
 *    a factory. For example, a given MeshObject could be displayed using a PropertySheet
 *    or a custom viewlet; either of those could be displayed in the current window
 *    or in a new window. In this example, 4 ViewletFactoryChoices would be returned,
 *    representing the 2x2 choices the user has to display the given MeshObject.
 *    The ViewletFactoryChoices are clearly distinct from the results of executing
 *    the factory method directly: they act as a mediator of some kind that allows
 *    the user to make a more informed choice about how to instantiate a Viewlet
 *    using the ViewletFactory.</p>
 */
public interface ViewletFactory
        extends
            Factory<MeshObjectsToView,Viewlet,Void>
{
    /**
     * Factory method. This is inhereted from the <code>Factory</code> interface, but
     * repeated here for clarity.
     *
     * @param key the MeshObjectsToView with this Viewlet
     * @param argument any argument-style information required for object creation, if any
     * @return the created Viewlet
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public abstract Viewlet obtainFor(
            MeshObjectsToView key,
            Void              argument )
        throws
            FactoryException;
    
    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoices(
            MeshObjectsToView theObjectsToView );

    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, and return them in
     * order of match quality.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoicesOrderedByMatchQuality(
            MeshObjectsToView theObjectsToView );
}
