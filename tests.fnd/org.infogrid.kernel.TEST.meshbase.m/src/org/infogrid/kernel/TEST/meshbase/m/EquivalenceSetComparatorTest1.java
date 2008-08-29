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

package org.infogrid.kernel.TEST.meshbase.m;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.meshbase.a.AMeshObjectEquivalenceSetComparator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentation;

/**
 * Tests the AMeshObjectEquivalenceSetComparator.
 */
public class EquivalenceSetComparatorTest1
        extends
            AbstractMeshBaseTest
{
    /**
     * Run the test.
     *
     * @throws Exception thrown if an Exception occurred during the test
     */
    public void run()
        throws
            Exception
    {
        MeshObjectIdentifier meshObject = new TestMeshObjectIdentifier( "dddd" );
     
        //

        log.debug( "With null equivalents" );
        
        MeshObjectIdentifier [] results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                null );
        
        checkEquals( results.length, 2, "wrong array size" );
        checkCondition( results[0] == null, "left side not null" );
        checkCondition( results[1] == null, "right side not null" );
        
        //
        
        log.debug( "With empty equivalents" );
        
        results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                new MeshObjectIdentifier[0] );
        
        checkEquals( results.length, 2, "wrong array size" );
        checkCondition( results[0] == null, "left side not null" );
        checkCondition( results[1] == null, "right side not null" );
        
        //
        
        log.debug( "With only a right side" );
        
        MeshObjectIdentifier [] equivalents = new MeshObjectIdentifier[] {
            new TestMeshObjectIdentifier( "eeee" )
        };
        results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                equivalents );
        checkEquals( results.length, 2, "wrong array size" );
        checkCondition( results[0] == null, "left side not null" );
        checkEquals( results[1], equivalents[0], "right side wrong" );
        
        //
        
        log.debug( "With only a left side" );
        
        equivalents = new MeshObjectIdentifier[] {
            new TestMeshObjectIdentifier( "bbbb" )
        };
        results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                equivalents );
        checkEquals( results.length, 2, "wrong array size" );
        checkEquals( results[0], equivalents[0], "left side wrong" );
        checkCondition( results[1] == null, "right side not null" );

        //
        
        log.debug( "With ordered sequence" );
        
        equivalents = new MeshObjectIdentifier[] {
            new TestMeshObjectIdentifier( "aaaa" ),
            new TestMeshObjectIdentifier( "bbbb" ),
            new TestMeshObjectIdentifier( "cccc" ),
            new TestMeshObjectIdentifier( "eeee" ),
            new TestMeshObjectIdentifier( "ffff" ),
            new TestMeshObjectIdentifier( "gggg" )
        };
        results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                equivalents );
        checkEquals( results.length, 2, "wrong array size" );
        checkEquals( results[0], new TestMeshObjectIdentifier( "cccc" ), "left side wrong" );
        checkEquals( results[1], new TestMeshObjectIdentifier( "eeee" ), "left side wrong" );

        //
        
        log.debug( "With unordered sequence" );
        
        equivalents = new MeshObjectIdentifier[] {
            new TestMeshObjectIdentifier( "bbbb" ),
            new TestMeshObjectIdentifier( "cccc" ),
            new TestMeshObjectIdentifier( "ffff" ),
            new TestMeshObjectIdentifier( "aaaa" ),
            new TestMeshObjectIdentifier( "gggg" ),
            new TestMeshObjectIdentifier( "eeee" )
        };
        results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                equivalents );
        checkEquals( results.length, 2, "wrong array size" );
        checkEquals( results[0], new TestMeshObjectIdentifier( "cccc" ), "left side wrong" );
        checkEquals( results[1], new TestMeshObjectIdentifier( "eeee" ), "left side wrong" );

        //
        
        log.debug( "With unordered sequence only to the left" );
        
        equivalents = new MeshObjectIdentifier[] {
            new TestMeshObjectIdentifier( "bbbb" ),
            new TestMeshObjectIdentifier( "cccc" ),
            new TestMeshObjectIdentifier( "aaaa" ),
        };
        results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                equivalents );
        checkEquals( results.length, 2, "wrong array size" );
        checkEquals( results[0], new TestMeshObjectIdentifier( "cccc" ), "left side wrong" );
        checkCondition( results[1] == null, "right side not null" );

        //
        
        log.debug( "With unordered sequence only to the right" );
        
        equivalents = new MeshObjectIdentifier[] {
            new TestMeshObjectIdentifier( "ffff" ),
            new TestMeshObjectIdentifier( "gggg" ),
            new TestMeshObjectIdentifier( "eeee" )
        };
        results = AMeshObjectEquivalenceSetComparator.SINGLETON.findLeftAndRightEquivalents(
                meshObject,
                equivalents );
        checkEquals( results.length, 2, "wrong array size" );
        checkCondition( results[0] == null, "left side not null" );
        checkEquals( results[1], new TestMeshObjectIdentifier( "eeee" ), "right side wrong" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        EquivalenceSetComparatorTest1 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new EquivalenceSetComparatorTest1( args );
            test.run();

        } catch( Throwable ex ) {
            log.error( ex );
            System.exit(1);
        }
        if( test != null ) {
            test.cleanup();
        }
        if( errorCount == 0 ) {
            log.info( "PASS" );
        } else {
            log.info( "FAIL (" + errorCount + " errors)" );
        }
        System.exit( errorCount );
    }

    /**
      * Constructor.
      *
      * @param args command-line arguments
      */
    public EquivalenceSetComparatorTest1(
            String [] args )
        throws
            Exception
    {
        super( EquivalenceSetComparatorTest1.class );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( EquivalenceSetComparatorTest1.class );
    
    
    /**
     * MeshObjectIdentifier test class.
     */
    static class TestMeshObjectIdentifier
            implements
                MeshObjectIdentifier
    {
        /**
         * Constructor.
         */
        public TestMeshObjectIdentifier(
                String s )
        {
            theString = s;
        }
        
        /**
         * Obtain an external form for this Identifier, similar to
         * URL's getExternalForm(). This returns an empty String for local home objects.
         *
         * @return external form of this Identifier
         */
        public String toExternalForm()
        {
            return theString;
        }

        /**
         * Determine whether this MeshObjectIdentifier identifies a Home Object.
         *
         * @return true if it identifies a Home Object
         */
        public boolean identifiesHomeObject()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Convert this Identifier to its String representation, using the representation scheme.
         *
         * @param representation the representation scheme
         * @return the String representation
         */
        public String toStringRepresentation(
                StringRepresentation representation )
        {
            throw new UnsupportedOperationException();
        }
        
        /**
         * Hash code.
         * 
         * @return hash code
         */
        @Override
        public int hashCode()
        {
            return theString.hashCode();
        }

        /**
         * Test for equality.
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( !( other instanceof TestMeshObjectIdentifier )) {
                return false;
            }
            TestMeshObjectIdentifier realOther = (TestMeshObjectIdentifier) other;
            
            boolean ret = toExternalForm().equals( realOther.toExternalForm() );
            return ret;
        }
        
        protected String theString;
                
    }
}
