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

package org.infogrid.meshbase.net;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.mesh.set.MeshObjectSetFactory;

/**
 * Defines the requested scope for a replication operation. This is an abstract
 * class; concrete subclasses are defined as inner classes.
 */
public abstract class ScopeSpecification
{
    /**
     * Parse a ScopeSpecification's external form and instantiate the right subclass.
     *
     * @param ext the external form
     * @return the created ScopeSpecification
     */
    public static ScopeSpecification fromExternalForm(
            String ext )
    {
        if( ext == null ) {
            return null;
        }
        ScopeSpecification ret = null;
        
        ret = SimpleStep.fromExternalForm( ext );
        if( ret != null ) {
            return ret;
        }

        return null;
    }
    
    /**
     * Obtain the external form of this ScopeSpecification.
     *
     * @return the external form
     */
    public abstract String toExternalForm();

    /**
     * Determine the set of MeshObjects that are in scope, given this start MeshObject.
     *
     * @param start the start MeshObject
     * @return the MeshObjectSet that is in scope, excluding the start MeshObject
     */
    public abstract MeshObjectSet determineMeshObjectsInScope(
            MeshObject start );

    /**
     * This subclass uses a simple integer to capture the number of "neighbor" steps
     * from the originating MeshObject to be considered in scope.
     */
    public static class SimpleStep
            extends
                ScopeSpecification
    {
        /**
         * Constructor.
         *
         * @param scope
         */
        public SimpleStep(
                int scope )
        {
            theScope = scope;
        }
        
        /**
         * Obtain the scope.
         *
         * @return the scope as a number of "neighbor" steps
         */
        public int getScope()
        {
            return theScope;
        }
        
        /**
         * Parse a ScopeSpecification's external form and instantiate the right subclass.
         *
         * @param ext the external form
         * @return the created ScopeSpecification
         */
        public static ScopeSpecification fromExternalForm(
                String ext )
        {
            Matcher m = thePattern.matcher( ext );
            if( m.matches() ) {
                String scope = m.group( 1 );
                return new SimpleStep( Integer.parseInt( scope ));
            }
            return null;
        }

        /**
         * Obtain the external form of this ScopeSpecification.
         *
         * @return the external form
         */
        public String toExternalForm()
        {
            StringBuilder ret = new StringBuilder();
            ret.append( getClass().getName() );
            ret.append( "{" );
            ret.append( theScope );
            ret.append( "}" );
            return ret.toString();
        }
        
        /**
         * Determine the set of MeshObjects that are in scope, given this start MeshObject.
         *
         * @param start the start MeshObject
         * @return the MeshObjectSet that is in scope, excluding the start MeshObject
         */
        public MeshObjectSet determineMeshObjectsInScope(
                MeshObject start )
        {
            MeshObjectSetFactory setFactory = start.getMeshBase().getMeshObjectSetFactory();

            MeshObjectSet current = setFactory.createSingleMemberImmutableMeshObjectSet( start );
            MeshObjectSet ret     = current;
            for( int i=0 ; i<theScope ; ++i ) {
                current = current.traverseToNeighborMeshObjects();
                ret = setFactory.createImmutableMeshObjectSetUnification( ret, current );
            }
            return ret;
        }

        /**
         * Determine equality.
         *
         * @param other the Object to compare with
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( !( other instanceof SimpleStep )) {
                return false;
            }
            SimpleStep realOther = (SimpleStep) other;
            return theScope == realOther.theScope;
        }

        /**
         * Determine hash code.
         * 
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            return theScope;
        }

        /**
         * The scope.
         */
        protected int theScope;
        
        /**
         * Our Regex pattern to parse an externalized ScopeSpecification.Simple.
         */
        protected static final Pattern thePattern = Pattern.compile(
                SimpleStep.class.getName().replace( ".", "\\." ).replace( "$", "\\$" )
                + "\\{(\\d+)\\}" );
    }
}
