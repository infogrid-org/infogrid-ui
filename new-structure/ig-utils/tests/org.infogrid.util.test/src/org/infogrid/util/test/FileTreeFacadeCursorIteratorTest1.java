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

package org.infogrid.util.test;

import java.io.File;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.FileTreeFacade;

/**
 * Tests the TreeFacadeCursorIterator.
 */
public class FileTreeFacadeCursorIteratorTest1
        extends
            AbstractCursorIteratorTest1
{
    /**
     * Run the test.
     * 
     * @throws Exception all kinds of things may go wrong in a test
     */
    public void run()
        throws
            Exception
    {
        log.info( "create a test hierarchy, out of order" );
        
        File top = new File( "build/top" );
        deleteFile( top ); // cleanup

        top.mkdirs();
        
        File a = new File( top, "a" );
        File b = new File( top, "b" );
        File c = new File( top, "c" );

        File b1 = new File( b, "1" );
        File b2 = new File( b, "2" );
        File b3 = new File( b, "3" );
        
        File b3x = new File( b3, "x" );
        File b3y = new File( b3, "y" );
        
        c.createNewFile();
        a.createNewFile();
        b3.mkdirs();
        b3x.createNewFile();
        b1.createNewFile();
        b2.createNewFile();
        b3y.createNewFile();
        
        File [] testData = {
            top,
            a,
            b,
            b1,
            b2,
            b3,
            b3x,
            b3y,
            c
        };
        
        FileTreeFacade facade = FileTreeFacade.create( top );
        
        //
        
        log.info( "testing facade" );
        
        checkEquals( facade.getForwardSiblingNode( a ), b,    "a->b wrong" );
        checkEquals( facade.getForwardSiblingNode( b ), c,    "b->c wrong" );
        checkEquals( facade.getForwardSiblingNode( c ), null, "c->null wrong" );

        checkEquals( facade.getBackwardSiblingNode( a ), null, "a<-null wrong" );
        checkEquals( facade.getBackwardSiblingNode( b ), a,    "b<-a wrong" );
        checkEquals( facade.getBackwardSiblingNode( c ), b,    "c<-b wrong" );

        checkEquals( facade.getForwardSiblingNode( b1 ), b2,   "b1->b2 wrong" );
        checkEquals( facade.getForwardSiblingNode( b2 ), b3,   "b2->b3 wrong" );
        checkEquals( facade.getForwardSiblingNode( b3 ), null, "b3->null wrong" );
        
        checkEquals( facade.getBackwardSiblingNode( b1 ), null, "b1<-null wrong" );
        checkEquals( facade.getBackwardSiblingNode( b2 ), b1,   "b2<-b1 wrong" );
        checkEquals( facade.getBackwardSiblingNode( b3 ), b2,   "b3<-b2 wrong" );

        checkEquals( facade.getForwardSiblingNode( b3x ), b3y,   "b3x->b3y wrong" );
        checkEquals( facade.getForwardSiblingNode( b3y ), null,  "b3y->null wrong" );
        
        checkEquals( facade.getBackwardSiblingNode( b3x ), null, "b3x<-null wrong" );
        checkEquals( facade.getBackwardSiblingNode( b3y ), b3x,  "b3y<-b3y wrong" );

        checkEqualsOutOfSequence( facade.getChildNodes( top ), new File[] { a, b, c    }, "top has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( a   ), new File[] {            }, "a has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b   ), new File[] { b1, b2, b3 }, "b has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( c   ), new File[] {            }, "c has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b1  ), new File[] {            }, "b1 has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b2  ), new File[] {            }, "b2 has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b3  ), new File[] { b3x, b3y   }, "b3 has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b3x ), new File[] {            }, "b3x has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b3y ), new File[] {            }, "b3y has wrong children" );
        
        checkEquals( facade.getParentNode( top ), null, "top has wrong parent" );
        checkEquals( facade.getParentNode( a   ), top, "a has wrong parent" );
        checkEquals( facade.getParentNode( b   ), top, "b has wrong parent" );
        checkEquals( facade.getParentNode( c   ), top, "c has wrong parent" );
        checkEquals( facade.getParentNode( b1  ), b,   "b1 has wrong parent" );
        checkEquals( facade.getParentNode( b2  ), b,   "b2 has wrong parent" );
        checkEquals( facade.getParentNode( b3  ), b,   "b3 has wrong parent" );
        checkEquals( facade.getParentNode( b3x ), b3,  "b3x has wrong parent" );
        checkEquals( facade.getParentNode( b3y ), b3,  "b3y has wrong parent" );
        
        //
        
        log.info( "testing iterator" );
        
        CursorIterator<File> iter1 = facade.iterator();
        
        super.runWith( testData, iter1, log );
    }
    

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        FileTreeFacadeCursorIteratorTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new FileTreeFacadeCursorIteratorTest1( args );
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
            log.error( "FAIL (" + errorCount + " errors)" );
        }

        System.exit( errorCount );
    }

    /**
     * Constructor.
     *
     * @param args command-line arguments
     * @throws Exception all kinds of things may go wrong in a test
     */
    public FileTreeFacadeCursorIteratorTest1(
            String [] args )
        throws
            Exception
    {
        super();
    }

    private static final Log log = Log.getLogInstance( FileTreeFacadeCursorIteratorTest1.class  ); // our own, private logger
}
