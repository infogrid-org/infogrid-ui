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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet.module;

import java.util.ArrayList;
import org.diet4j.core.ModuleMeta;
import org.diet4j.core.ModuleRegistry;
import org.infogrid.util.ArrayHelper;
import org.infogrid.viewlet.AbstractViewletFactory;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Adds Module framework awareness to the AbstractViewletFactory. This allows a
 * ViewletFactory to find viewlet classes even if they are not explicitly enumerated
 * in the code anywhere, but are advertised in the Module Advertisements.
 */
public class ModuleAwareViewletFactory
        extends
            AbstractViewletFactory
{
    /**
     * Factory method.
     *
     * @param implementationMarkerInterfaceName name of an interface that indicates the right implementation
     *        technology for a given Viewlet, e.g. "org.infogrid.viewlet.swing.SwingViewlet".
     * @param moduleRegistry the ModuleRegistry in which to look for Viewlets.
     * @return the created ModuleAwareViewletFactory
     */
    public static ModuleAwareViewletFactory create(
            String                 implementationMarkerInterfaceName,
            ModuleRegistry         moduleRegistry )
    {
        return new ModuleAwareViewletFactory( implementationMarkerInterfaceName, moduleRegistry );
    }
    
    /**
     * Constructor.
     *
     * @param implementationMarkerInterfaceName name of an interface that indicates the right implementation
     *        technology for a given Viewlet, e.g. "org.infogrid.viewlet.swing.SwingViewlet".
     * @param moduleRegistry the ModuleRegistry in which to look for Viewlets.
     */
    protected ModuleAwareViewletFactory(
            String                 implementationMarkerInterfaceName,
            ModuleRegistry         moduleRegistry )
    {
        super( implementationMarkerInterfaceName );

        theModuleRegistry = moduleRegistry;
    }

    /**
     * Find the ViewletFactoryChoices that apply to these MeshObjectsToView, but ignore the specified
     * viewlet type. If none are found, return an emtpy array.
     *
     * @param theObjectsToView the MeshObjectsToView
     * @return the found ViewletFactoryChoices, if any
     */
    public ViewletFactoryChoice [] determineFactoryChoicesIgnoringType(
            MeshObjectsToView theObjectsToView )
    {
        // FIXME: this ignores theObjectsToView? Does not sound right ...
        ArrayList<ViewletFactoryChoice> ret = new ArrayList<ViewletFactoryChoice>();

        ModuleMeta [] found  = theModuleRegistry.findModuleMetasForInterface( Viewlet.class.getName(), 100 );
        for( ModuleMeta current : found ) {
            ModuleCapability [] caps = current.findCapabilitiesByInterface( Viewlet.class.getName() );
            for( int j=0 ; j<caps.length ; ++j ) {
                ret.add( new InModuleViewletFactoryChoice( theObjectsToView, theModuleRegistry, current, caps[j] ) );
            }
            
        }
        return ArrayHelper.copyIntoNewArray( ret, ViewletFactoryChoice.class );
    }

    /**
     * The ModuleRegistry to use.
     */
    protected ModuleRegistry theModuleRegistry;
}
