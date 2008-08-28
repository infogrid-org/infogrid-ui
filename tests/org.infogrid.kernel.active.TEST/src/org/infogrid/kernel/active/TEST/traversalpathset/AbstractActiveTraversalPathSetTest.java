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

package org.infogrid.kernel.active.TEST.traversalpathset;

import java.net.URISyntaxException;
import org.infogrid.kernel.active.TEST.AllTests;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.mesh.set.active.m.ActiveMMeshObjectSetFactory;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.m.MMeshBase;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RelationshipType;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;

/**
 *
 */
public abstract class AbstractActiveTraversalPathSetTest
        extends
            AbstractTest
{
    /**
     * Constructor.
     */
    protected AbstractActiveTraversalPathSetTest(
            Class testClass )
        throws
            MeshTypeNotFoundException,
            URISyntaxException
    {
        super( localFileName( AllTests.class, "/ResourceHelper" ),
               localFileName( AllTests.class, "/Log.properties" ));

        typeAA   = theModelBase.findEntityType(       "org.infogrid.model.Test", null, "AA" );
        typeAR1A = theModelBase.findRelationshipType( "org.infogrid.model.Test", null, "AR1A" );
        typeAR2A = theModelBase.findRelationshipType( "org.infogrid.model.Test", null, "AR2A" );
        typeX    = theModelBase.findPropertyType( typeAA, "X" );
        
        theMeshBase = MMeshBase.create( MeshBaseIdentifier.create( "testMeshBase" ), theModelBase, null, rootContext );

        theMeshObjectSetFactory = ActiveMMeshObjectSetFactory.create( MeshObject.class, MeshObjectIdentifier.class );
        theMeshObjectSetFactory.setMeshBase( theMeshBase );
    }

    /**
     * Create a MeshObject.
     */
    protected MeshObject createMeshObject(
            MeshBaseLifecycleManager   life,
            EntityType                 type,
            MeshObjectIdentifier            identifier )
        throws
            TransactionException,
            EntityBlessedAlreadyException,
            MeshObjectIdentifierNotUniqueException,
            IsAbstractException,
            NotPermittedException
    {
        MeshObject ret = life.createMeshObject( identifier );
        ret.bless( type );
        return ret;
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        theMeshBase = null;
    }

    /**
     * Check whether an (unordered) set of MeshObjects contains exactly the set of
     * MeshObjects whose properties (specified as second parameter) contains the
     * values that are given by the
     * third parameter. This is provided to make certain tests easy where we look
     * for the existence of certain objects in another set.
     *
     * @param set the set of MeshObject in which we look
     * @param propertyType the PropertyType in which we look
     * @param values the set of values for which we look in the set
     * @param msg a message to print to report an error
     * @return true if the test passed
     */
    protected final boolean checkTraversalPathSet(
            TraversalPathSet set,
            PropertyType     propertyType,
            String     [][]  values,
            String           msg )
        throws
            Exception
    {
        // first make sure all MeshObjects are still alive
        for( TraversalPath currentPath : set.getTraversalPaths() ) {
            TraversalPath currentPath2 = currentPath;
            if( currentPath2.getFirstMeshObject().getIsDead() ) {
                reportError( "MeshObject is dead: " + currentPath2.getFirstMeshObject() );
            }
            currentPath2 = currentPath2.getNextSegment();
        }
        
        if( set.size() != values.length ) {
            StringBuffer buf = new StringBuffer();
            if( msg != null ) {
                buf.append( msg );
            }
            buf.append( ", found " );
            String sep = "{ ";
            for( TraversalPath currentPath : set.getTraversalPaths() ) {
                buf.append( sep );
                TraversalPath currentPath2 = currentPath;
                String sep2 = "{ ";
                while( currentPath2 != null ) {
                    buf.append( sep2 );
                    MeshObject currentObject = currentPath2.getFirstMeshObject();
                    PropertyValue currentValue = currentObject.getPropertyValue( propertyType );
                    buf.append( currentValue );
                    currentPath2 = currentPath2.getNextSegment();
                    sep2 = ", ";
                }
                sep = "}, ";
            }
            buf.append( " } vs. " );
            buf.append( ArrayHelper.arrayToString( values ) );
            
            reportError( buf.toString() );
            return false;
        }
        return true;
    }

    /**
     * Dump the content of a TraversalPathSet to log.debug().
     *
     * @param set the TraversalPathSet whose content we want to dump
     * @param prefix a string to prepend
     * @param mylog the Log to dump to
     */
    protected final void dumpTraversalPathSet(
            TraversalPathSet set,
            String           prefix,
            PropertyType     type,
            Log              mylog )
        throws
            Exception
    {
        if( mylog.isDebugEnabled() ) {
            StringBuffer buf = new StringBuffer( prefix );
            for( TraversalPath current : set ) {
                buf.append( "\n  " );
                TraversalPath p = current;
                do {
                    buf.append( " / " );
                    buf.append( p.getFirstMeshObject().getPropertyValue( type ) );
                    p = p.getNextSegment();
                } while( p != null );
            }
            mylog.debug( buf.toString() );
        }
    }
        
    /**
     * The ModelBase.
     */
    protected ModelBase theModelBase = ModelBaseSingleton.getSingleton();

    /**
     * Cached MeshType.
     */
    protected EntityType typeAA;

    /**
     * Cached MeshType.
     */
    protected RelationshipType typeAR1A;

    /**
     * Cached MeshType.
     */
    protected RelationshipType typeAR2A;

    /**
     * Cached MeshType.
     */
    protected PropertyType typeX;

    /**
     * The test MeshBase.
     */
    protected MeshBase theMeshBase;

    /**
     * The test ActiveMeshObjectSetFactory.
     */
    protected ActiveMMeshObjectSetFactory theMeshObjectSetFactory;

    /**
     * The root context for these tests.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root-context" );
}
