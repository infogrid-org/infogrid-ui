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

package org.infogrid.probe.test;

import java.io.IOException;
import java.net.URISyntaxException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Test.TestSubjectArea;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ApiProbe;
import org.infogrid.probe.ProbeDirectory;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.testharness.util.IteratorElementCounter;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Tests two Probes instantiating ForwardReferences pointing to each other.
 */
public class ForwardReferenceTest8
        extends
            AbstractForwardReferenceTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things can go wrong during a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "Accessing outer Probe" );

        MeshObject outerHome = base.accessLocally( OUTER_URL, theCoherence );

        checkObject( outerHome, "abc not found" );
        checkEquals( IteratorElementCounter.countIteratorElements( base.proxies()), 1, "wrong number of proxies in main NetMeshBase" );
        // if wait, this is the proxy to the old Shadow, if !wait, the proxy to the new Shadow

        //

        log.info( "Finding ForwardReference created in outer Probe" );

        NetMeshObject fwdReference = (NetMeshObject) outerHome.traverse( TestSubjectArea.AR1A.getSource() ).getSingleMember();
        checkObject( fwdReference, "fwdReference not found" );

        NetMeshObject outerNonHome = (NetMeshObject) outerHome.traverse(  TestSubjectArea.AR1A.getDestination() ).getSingleMember();
        checkObject( outerNonHome, "outer nonHome not found" );

        if( theWait ) {
            checkCondition(  fwdReference.isBlessedBy( TestSubjectArea.A, false ), "Not blessed by right type" );
            checkCondition( !fwdReference.isBlessedBy( TestSubjectArea.B, false ), "Blessed by wrong type" );
            checkEquals( fwdReference.getPropertyValue( TestSubjectArea.A_X ), "forwardreference", "wrong property value" );

            log.info( "Finding nonHome created in outer Probe" );

            checkNotEquals( outerNonHome.getPropertyValue( TestSubjectArea.A_X ), "forwardreference", "wrong property value" );

            //

            log.info( "Wait some for ForwardReference to resolve" );

            Thread.sleep( PINGPONG_ROUNDTRIP_DURATION * 3L );
        }

        checkEquals( fwdReference.getPropertyValue( TestSubjectArea.A_X ), "resolved", "ForwardReference was not successfully resolved: " + fwdReference.getIdentifier().toExternalForm() );

        checkEquals(    fwdReference.getAllProxies().length, 1, "Wrong number of proxies on forward reference" );
        checkCondition( fwdReference.getAllProxies()[0].getPartnerMeshBaseIdentifier().equals( INNER_URL ), "Wrong proxy on forward reference" );
        checkCondition( !fwdReference.isBlessedBy( TestSubjectArea.A,  false ), "Blessed still by old type" );
        checkCondition(  fwdReference.isBlessedBy( TestSubjectArea.AA, false ), "Not blessed by the right type (AA)" );
        
        //

        log.info( "Make sure other ForwardReference resolved, too" );

        NetMeshObject fwdReference2 = (NetMeshObject) fwdReference.traverse( TestSubjectArea.AR1A.getSource() ).getSingleMember();
        checkObject( fwdReference2, "Inner fwdReference not found" );
        checkIdentity( outerNonHome, fwdReference2, "Not the same object" );
        checkNotEquals( outerNonHome.getPropertyValue( TestSubjectArea.A_X ), "forwardreference", "wrong property value" );
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        ForwardReferenceTest7 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: \"fast\"|\"slow\"" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new ForwardReferenceTest7( args );
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
     * @throws Exception all sorts of things can go wrong during a test
     */
    public ForwardReferenceTest8(
            String [] args )
        throws
            Exception
    {
        super( ForwardReferenceTest8.class, args[0] );

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                OUTER_URL.toExternalForm(),
                OuterTestApiProbe.class ));

        theProbeDirectory.addExactUrlMatch( new ProbeDirectory.ExactMatchDescriptor(
                INNER_URL.toExternalForm(),
                InnerTestApiProbe.class ));
    }

    // Our Logger
    private static Log log = Log.getLogInstance( ForwardReferenceTest5.class);

    /**
     * URL for the outer Probe.
     */
    public static final NetMeshBaseIdentifier OUTER_URL;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://some.example.com/outer" );
            // temp = NetMeshBaseIdentifier.create( "=foo" );

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
        }
        OUTER_URL = temp;
    }

    /**
     * URL for the inner Probe.
     */
    public static final NetMeshBaseIdentifier INNER_URL;
    static {
        NetMeshBaseIdentifier temp = null;
        try {
            temp = theMeshBaseIdentifierFactory.fromExternalForm( PROTOCOL_NAME + "://some.example.com/inner" );

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
        }
        INNER_URL = temp;
    }

    /**
     * Identifier of the outer MeshObject to reference.
     */
    public static final String OUTER_NON_HOME_LOCAL_IDENTIFIER = "#non-home-outer";

    /**
     * Identifier of the inner MeshObject to reference.
     */
    public static final String INNER_NON_HOME_LOCAL_IDENTIFIER = "#non-home-inner";

    /**
     * The Probe to the outer data feed.
     */
    public static class OuterTestApiProbe
            implements
                ApiProbe
    {
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EntityNotBlessedException,
                RelatedAlreadyException,
                NotRelatedException,
                MeshObjectIdentifierNotUniqueException,
                IllegalPropertyTypeException,
                IllegalPropertyValueException,
                TransactionException,
                NotPermittedException,
                ProbeException,
                IOException,
                ModuleException,
                URISyntaxException,
                StringRepresentationParseException
        {
            MeshObject                      home = mb.getHomeObject();
            StagingMeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

            home.bless( TestSubjectArea.AA );

            MeshObject fwdRef = life.createForwardReference(
                    INNER_URL,
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( INNER_URL, INNER_NON_HOME_LOCAL_IDENTIFIER ),
                    TestSubjectArea.A );
            fwdRef.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "forwardreference" ));

            home.relateAndBless( TestSubjectArea.AR1A.getSource(), fwdRef );

            MeshObject nonHome = life.createMeshObject(
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( OUTER_NON_HOME_LOCAL_IDENTIFIER ),
                    TestSubjectArea.AA );
            nonHome.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "resolved" ) );

            home.relateAndBless( TestSubjectArea.AR1A.getDestination(), nonHome );
        }
    }

    /**
     * The Probe to the inner data feed.
     */
    public static class InnerTestApiProbe
            implements
                ApiProbe
    {
        public void readFromApi(
                NetMeshBaseIdentifier  networkId,
                CoherenceSpecification coherence,
                StagingMeshBase        mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EntityNotBlessedException,
                RelatedAlreadyException,
                NotRelatedException,
                MeshObjectIdentifierNotUniqueException,
                IllegalPropertyTypeException,
                IllegalPropertyValueException,
                TransactionException,
                NotPermittedException,
                ProbeException,
                IOException,
                ModuleException,
                URISyntaxException,
                StringRepresentationParseException
        {
            // don't touch the home object here
            StagingMeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();

            MeshObject fwdRef = life.createForwardReference(
                    OUTER_URL,
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( OUTER_URL, OUTER_NON_HOME_LOCAL_IDENTIFIER ),
                    TestSubjectArea.A );
            fwdRef.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "forwardreference" ));

            MeshObject nonHome = life.createMeshObject(
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( INNER_NON_HOME_LOCAL_IDENTIFIER ),
                    TestSubjectArea.AA );
            nonHome.setPropertyValue( TestSubjectArea.A_X, StringValue.create( "resolved" ) );

            nonHome.relateAndBless( TestSubjectArea.AR1A.getSource(), fwdRef );
        }
    }
}
