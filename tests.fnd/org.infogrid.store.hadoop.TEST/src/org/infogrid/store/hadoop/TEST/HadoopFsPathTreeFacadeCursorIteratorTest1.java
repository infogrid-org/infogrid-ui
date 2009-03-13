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

package org.infogrid.store.hadoop.TEST;

import java.util.NoSuchElementException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RawLocalFileSystem;
import org.infogrid.store.hadoop.HadoopFsPathTreeFacade;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;

/**
 * Tests the TreeFacadeCursorIterator.
 */
public class HadoopFsPathTreeFacadeCursorIteratorTest1
        extends
            AbstractTest
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

        Path top = new Path( TEST_SUBDIR_NAME );
        
        FileSystem fileSystem = new RawLocalFileSystem();
        fileSystem.setConf( new Configuration() );

        fileSystem.delete( top, true ); // cleanup
        
        fileSystem.mkdirs( top );
        
        Path a = new Path( top.toString() + "/a" );
        Path b = new Path( top.toString() + "/b" );
        Path c = new Path( top.toString() + "/c" );

        Path b1 = new Path( b.toString() + "/1" );
        Path b2 = new Path( b.toString() + "/2" );
        Path b3 = new Path( b.toString() + "/3" );
        
        Path b3x = new Path( b3.toString() + "/x" );
        Path b3y = new Path( b3.toString() + "/y" );
        
        fileSystem.createNewFile( c );
        fileSystem.createNewFile( a );
        fileSystem.mkdirs( b3 );
        fileSystem.createNewFile( b3x );
        fileSystem.createNewFile( b1 );
        fileSystem.createNewFile( b2 );
        fileSystem.createNewFile( b3y );
        
        Path [] testData = {
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
        
        Path qualifiedTop = top.makeQualified( fileSystem );
        Path qualifiedA   = a.makeQualified( fileSystem );
        Path qualifiedB   = b.makeQualified( fileSystem );
        Path qualifiedC   = c.makeQualified( fileSystem );
        Path qualifiedB1  = b1.makeQualified( fileSystem );
        Path qualifiedB2  = b2.makeQualified( fileSystem );
        Path qualifiedB3  = b3.makeQualified( fileSystem );
        Path qualifiedB3x = b3x.makeQualified( fileSystem );
        Path qualifiedB3y = b3y.makeQualified( fileSystem );

        Path [] qualifiedTestData = {
            qualifiedTop,
            qualifiedA,
            qualifiedB,
            qualifiedB1,
            qualifiedB2,
            qualifiedB3,
            qualifiedB3x,
            qualifiedB3y,
            qualifiedC
        };

        HadoopFsPathTreeFacade facade = HadoopFsPathTreeFacade.create( fileSystem, top );
        
        //
        
        log.info( "testing facade" );
        // we test against qualified Paths, so the equality works, but use relative paths for the test as that
        // captures more cases
        
        
        checkEquals( facade.getForwardSiblingNode( a ), qualifiedB, "a->b wrong" );
        checkEquals( facade.getForwardSiblingNode( b ), qualifiedC, "b->c wrong" );
        checkEquals( facade.getForwardSiblingNode( c ), null,       "c->null wrong" );

        checkEquals( facade.getBackwardSiblingNode( a ), null,       "a<-null wrong" );
        checkEquals( facade.getBackwardSiblingNode( b ), qualifiedA, "b<-a wrong" );
        checkEquals( facade.getBackwardSiblingNode( c ), qualifiedB, "c<-b wrong" );

        checkEquals( facade.getForwardSiblingNode( b1 ), qualifiedB2, "b1->b2 wrong" );
        checkEquals( facade.getForwardSiblingNode( b2 ), qualifiedB3, "b2->b3 wrong" );
        checkEquals( facade.getForwardSiblingNode( b3 ), null,        "b3->null wrong" );
        
        checkEquals( facade.getBackwardSiblingNode( b1 ), null,        "b1<-null wrong" );
        checkEquals( facade.getBackwardSiblingNode( b2 ), qualifiedB1, "b2<-b1 wrong" );
        checkEquals( facade.getBackwardSiblingNode( b3 ), qualifiedB2, "b3<-b2 wrong" );

        checkEquals( facade.getForwardSiblingNode( b3x ), qualifiedB3y, "b3x->b3y wrong" );
        checkEquals( facade.getForwardSiblingNode( b3y ), null,         "b3y->null wrong" );
        
        checkEquals( facade.getBackwardSiblingNode( b3x ), null,         "b3x<-null wrong" );
        checkEquals( facade.getBackwardSiblingNode( b3y ), qualifiedB3x, "b3y<-b3y wrong" );

        checkEqualsOutOfSequence( facade.getChildNodes( top ), new Path[] { qualifiedA, qualifiedB, qualifiedC    }, "top has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( a   ), new Path[] {                                       }, "a has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b   ), new Path[] { qualifiedB1, qualifiedB2, qualifiedB3 }, "b has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( c   ), new Path[] {                                       }, "c has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b1  ), new Path[] {                                       }, "b1 has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b2  ), new Path[] {                                       }, "b2 has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b3  ), new Path[] { qualifiedB3x, qualifiedB3y            }, "b3 has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b3x ), new Path[] {                                       }, "b3x has wrong children" );
        checkEqualsOutOfSequence( facade.getChildNodes( b3y ), new Path[] {                                       }, "b3y has wrong children" );
        
        checkEquals( facade.getParentNode( top ), null, "top has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedA   ), qualifiedTop, "a has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedB   ), qualifiedTop, "b has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedC   ), qualifiedTop, "c has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedB1  ), qualifiedB,   "b1 has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedB2  ), qualifiedB,   "b2 has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedB3  ), qualifiedB,   "b3 has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedB3x ), qualifiedB3,  "b3x has wrong parent" );
        checkEquals( facade.getParentNode( qualifiedB3y ), qualifiedB3,  "b3y has wrong parent" );
        
        //
        
        log.info( "testing iterator" );
        
        CursorIterator<Path> iter1 = facade.iterator();
        
        runWith( qualifiedTestData, iter1, log );
    }
    
    /**
     * Run the test.
     * 
     * @param <T> the type of Iterator to test
     * @param testData the provided test data
     * @param iter the to-be-tested iterator
     * @param log the Logger to use
     */
    protected <T> void runWith(
            T []              testData,
            CursorIterator<T> iter,
            Log               log )
    {
        //
        
        log.info( "Check at the beginning" );
        
        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( testData.length ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( testData.length + 1 ), "Has too many nexts" );
        checkEquals( iter.peekNext(), testData[0], "wrong current element" );

        //
        
        log.info( "Check forward iteration" );

        for( int i=0 ; i<testData.length ; ++i ) {
            checkCondition( iter.hasNext(), "Not found next: " + i );
            
            Object found = iter.next();
            checkEquals( testData[i], found, "Not found element: " + i );
        }

        //
        
        log.info( "Check at the end" );

        checkCondition( !iter.hasNext(), "has next at the end" );
        checkCondition( iter.hasPrevious( testData.length ), "Does not have enough previous" );
        checkCondition( !iter.hasPrevious( testData.length + 1 ), "Has too many previous" );

        try {
            Object found = iter.peekNext();
            reportError( "Found element after end", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }
        
        //
        
        log.info( "Check backward iteration" );

        for( int i=testData.length-1 ; i>=0 ; --i ) {
            checkCondition( iter.hasPrevious(), "Not found previous: " + i );
            
            Object found = iter.previous();
            checkEquals( testData[i], found, "Not found element: " + i );
        }
        
        //
        
        log.info( "Check again at the beginning" );
        
        checkCondition( !iter.hasPrevious(), "has previous at the beginning" );
        checkCondition( iter.hasNext( testData.length ), "Does not have enough nexts" );
        checkCondition( !iter.hasNext( testData.length + 1 ), "Has too many nexts" );
        
        try {
            Object found = iter.peekPrevious();
            reportError( "Found element before beginning", found );
        } catch( NoSuchElementException t ) {
            log.debug( "Correctly received exception" );
        }
        
        //
        
        log.info( "Move to element" );
        
        iter.moveToBefore( testData[ 4 ] ); // "e"
        checkEquals( iter.peekNext(),     testData[4], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[3], "wrong element found" );

        //
        
        log.info( "Move by positive number" );
        
        iter.moveBy( 2 ); // "g"
        checkEquals( iter.peekNext(),     testData[6], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[5], "wrong element found" );

        //
        
        log.info( "Move by negative number" );
        
        iter.moveBy( -3 ); // "d"
        checkEquals( iter.peekNext(),     testData[3], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[2], "wrong element found" );
        checkEquals( iter.peekNext(),     testData[3], "wrong element found" ); // make sure we can move about a bit, so repeat
        checkEquals( iter.peekPrevious(), testData[2], "wrong element found" );

        //
        
        log.info( "Copy" );
        
        CursorIterator<?> copy = iter.createCopy();
        
        checkEquals( iter.peekNext(), copy.peekNext(), "copied iterator in a different place" );
        checkEquals( iter.peekNext(),     testData[3], "wrong element found" );
        checkEquals( iter.peekPrevious(), testData[2], "wrong element found" );
        checkEquals( copy.peekNext(),     testData[3], "wrong element found" );
        checkEquals( copy.peekPrevious(), testData[2], "wrong element found" );
        
        //
        
        log.info( "Look backward" );

        Object [] before = iter.previous( 100 );

        checkEquals( before.length, 3, "wrong number of elements before" );
        for( int i=0 ; i<3 ; ++i ) {
            checkEquals( testData[i], before[ before.length - 1 - i ], "wrong data at index " + i );
        }

        //
        
        log.info( "Look forward" );
        
        Object [] after  = copy.next( 100 );

        checkEquals( after.length, testData.length - 3, "wrong number of elements after" );
        for( int i=3 ; i<testData.length ; ++i ) {
            checkEquals( testData[i], after[ i-3 ], "wrong data at index " + i );
        }
    }

    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
             String [] args )
    {
        HadoopFsPathTreeFacadeCursorIteratorTest1 test = null;
        try {
            if( false && args.length != 0 )
            {
                System.err.println( "Synopsis: {no arguments}" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new HadoopFsPathTreeFacadeCursorIteratorTest1( args );
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
    public HadoopFsPathTreeFacadeCursorIteratorTest1(
            String [] args )
        throws
            Exception
    {
    }

    private static final Log log = Log.getLogInstance( HadoopFsPathTreeFacadeCursorIteratorTest1.class  ); // our own, private logger

    /**
     * The name of the subdirectory in Hadoop in which to store 
     */
    public static final String TEST_SUBDIR_NAME = "build/test-HadoopStoreIterator";
}
