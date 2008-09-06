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

package org.infogrid.scene;

import java.util.HashMap;
import java.util.Iterator;
import org.infogrid.modelbase.ModelBase;

/**
 * A directory of SceneTemplates.
 */
public class SceneTemplateDirectory
{
    /**
     * Constructor.
     *
     * @param mb the ModelBase in which to look up MeshTypes
     */
    public SceneTemplateDirectory(
            ModelBase mb )
    {
        theModelBase = mb;

        populateWithDefaults();
    }

    /**
     * This can be overridden by subclasses to populate this with a number of
     * SceneTemplates.
     */
    protected void populateWithDefaults()
    {
        // noop
    }

    /**
     * Add a SceneTemplate to this SceneTemplateDirectory. This operation is
     * typically only performed during startup.
     *
     * @param name the name of the SceneTemplate
     * @param newSceneTemplate the new SceneTemplate
     */
    public synchronized void put(
            String        name,
            SceneTemplate newSceneTemplate )
    {
        theSceneTemplates.put( name, newSceneTemplate );
    }

    /**
     * Obtain an iterator over the SceneTemplate members of this directory.
     *
     * @return an Iterator over the content of this set
     */
    public final Iterator<SceneTemplate> iterator()
    {
        // we are not synchronizing this because add operations are
        // very infrequent after an initial establishment of the directory
        return theSceneTemplates.values().iterator();
    }

    /**
     * Obtain the SceneTemplate instance that goes with this name.
     *
     * @param name the name of the SceneTemplate to looki for
     * @return the found SceneTemplate, or null if not found
     */
    public final SceneTemplate findByClassName(
            String name )
    {
        return theSceneTemplates.get( name );
    }

    /**
     * Obtain the ModelBase used for the SceneTemplates in this SceneTemplateDirectory.
     *
     * @return the ModelBase
     */
    public final ModelBase getModelBase()
    {
        return theModelBase;
    }

    /**
     * Convert to string, for debugging.
     *
     * @return string form of this instance
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer( 100 );
        buf.append( "<" );
        buf.append( super.toString() );
        buf.append( "{ content: " );
        Iterator keyIter = theSceneTemplates.keySet().iterator();
        while( keyIter.hasNext() )
        {
            Object key   = keyIter.next();
            Object value = theSceneTemplates.get( key );

            buf.append( key );
            buf.append( "=" );
            buf.append( value );
            buf.append( " " );
        }
        buf.append( "}>" );
        return buf.toString();
    }

    /**
     * The current set of known SceneTemplates.
     */
    private HashMap<String,SceneTemplate> theSceneTemplates = new HashMap<String,SceneTemplate>();

    /**
     * The ModelBase.
     */
    protected ModelBase theModelBase;
}
