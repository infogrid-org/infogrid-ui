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

/**
 * A SceneTemplateRole is the definition of a SceneRole (defined in a SceneTemplate)
 * that can be played by one or more participants in a Scene.
 */
public abstract class SceneTemplateRole
{
    /**
     * Constructor.
     *
     * @param name a name, for debugging only
     */
    public SceneTemplateRole(
            String name )
    {
        theName = name;
    }

    /**
     * Determine whether this is the root SceneTemplateRole.
     *
     * @return true if this is the root SceneTemplateRole
     */
    public abstract boolean isRoot();

    /**
     * Obtain the miminum number of participants that can play this SceneTemplateRole.
     *
     * @return the minimum number of participants that can play this SceneTemplateRole
     */
    public abstract int getMin();

    /**
     * Obtain the maximum number of participants that can play this SceneTemplateRole.
     *
     * @return the maximum number of participants that can play this SceneTemplateRole
     */
    public abstract int getMax();

    /**
     * The name (for debugging).
     */
    protected String theName;

    /**
     * Subclass for SceneTemplateRoles that are root SceneTemplateRoles.
     */
    public static class Root
        extends
            SceneTemplateRole
    {
        /**
         * Construct one.
         *
         * @param name a name, for debugging only
         */
        public Root(
                String name )
        {
            super( name );
        }

        /**
         * Determine whether this is the root SceneTemplateRole.
         *
         * @return true if this is the root SceneTemplateRole
         */
        public final boolean isRoot()
        {
            return true;
        }

        /**
         * Obtain the miminum number of participants that can play this SceneTemplateRole.
         *
         * @return the minimum number of participants that can play this SceneTemplateRole
         */
        public int getMin()
        {
            return 1;
        }

        /**
         * Obtain the maximum number of participants that can play this SceneTemplateRole.
         *
         * @return the maximum number of participants that can play this SceneTemplateRole
         */
        public int getMax()
        {
            return 1;
        }

        /**
         * Convert to string, for debugging.
         *
         * @return this instance in string form
         */
        @Override
        public String toString()
        {
            StringBuffer buf = new StringBuffer( 100 );
            buf.append( super.toString() );
            buf.append( "{ name: " );
            buf.append( theName );
            buf.append( " }" );
            return buf.toString();
        }
    }

    /**
     * Abstract sub/superclass for SceneTemplateRoles that are not root SceneTemplateRoles.
     */
    public static abstract class NonRoot
        extends
            SceneTemplateRole
    {
         /**
          * Construct one with a minimum and maximum multiplicity, ie the minimum
          * and maximum number of MeshObjects that need to play this SceneTemplateRole in a Scene.
          * The maximum number here is inclusive, not exclusive.
          *
          * @param name a name, for debugging only
          * @param minMultiplicity the minimum number of MeshObjects needed
          * @param maxMultiplicity the maximum number of MeshObjects needed
          */
        public NonRoot(
                String name,
                int    minMultiplicity,
                int    maxMultiplicity )
        {
            super( name );

            theMin = minMultiplicity;
            theMax = maxMultiplicity;
        }

        /**
         * Determine whether this is the root SceneTemplateRole.
         *
         * @return true if this is the root SceneTemplateRole
         */
        public final boolean isRoot()
        {
            return false;
        }

        /**
         * Obtain the miminum number of participants that can play this SceneTemplateRole.
         *
         * @return the minimum number of participants that can play this SceneTemplateRole
         */
        public int getMin()
        {
            return theMin;
        }

        /**
         * Obtain the maximum number of participants that can play this SceneTemplateRole.
         *
         * @return the maximum number of participants that can play this SceneTemplateRole
         */
        public int getMax()
        {
            return theMax;
        }

        /**
         * Convert to string, for debugging.
         *
         * @return this instance in string form
         */
        @Override
        public String toString()
        {
            StringBuffer buf = new StringBuffer( 100 );
            buf.append( super.toString() );
            buf.append( "{ name: " );
            buf.append( theName );
            buf.append( ", min: " );
            buf.append( theMin );
            buf.append( ", max: " );
            buf.append( theMax );
            buf.append( " }" );
            return buf.toString();
        }

        /**
         * The miminum number of MeshObjects that must play this SceneTemplateRole.
         */
        protected int theMin;

        /**
         * The maximum number of MeshObjects that can play this SceneTemplateRole.
         */
        protected int theMax;
    }

    /**
     * Subclass for SceneTemplateRoles that are not root SceneTemplateRole and that are played by
     * MeshObjects.
     */
    public static class PlayedByMeshObject
        extends
            NonRoot
    {
        /**
         * Constructor. Specify minimum and maximum multiplicity, ie the minimum
         * and maximum number of MeshObjects that need to play this SceneTemplateRole in a Scene.
         * The maximum number here is inclusive, not exclusive.
         *
         * @param name a name, for debugging only
         * @param minMultiplicity the minimum number of MeshObjects needed
         * @param maxMultiplicity the maximum number of MeshObjects needed
         */
        public PlayedByMeshObject(
                String name,
                int    minMultiplicity,
                int    maxMultiplicity )
        {
            super( name, minMultiplicity, maxMultiplicity );
        }
    }

    /**
     * Subclass for SceneTemplateRoles that are not root SceneTemplateRole and that are played by
     * other Scenes.
     */
    public static class PlayedByScene
        extends
            NonRoot
    {
        /**
         * Construct one with a minimum and maximum multiplicity, ie the minimum
         * and maximum number of Scenes that need to play this SceneTemplateRole in a Scene.
         * The maximum number here is inclusive, not exclusive.
         *
         * @param name a name, for debugging only
         * @param minMultiplicity the minimum number of Scenes needed
         * @param maxMultiplicity the maximum number of Scenes needed
         */
        public PlayedByScene(
                String name,
                int    minMultiplicity,
                int    maxMultiplicity )
        {
            super( name, minMultiplicity, maxMultiplicity );
        }
    }
}
