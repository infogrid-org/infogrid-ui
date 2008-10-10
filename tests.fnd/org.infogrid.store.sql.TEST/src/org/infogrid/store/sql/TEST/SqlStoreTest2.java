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

package org.infogrid.store.sql.TEST;

import org.infogrid.store.SerializingStoreEntryMapper;
import org.infogrid.store.StoreEntryMapper;
import org.infogrid.store.object.ObjectStore;
import org.infogrid.util.logging.Log;

import java.io.Serializable;

/**
 * Tests the storing of Java objects in the SqlStore.
 */
public class SqlStoreTest2
        extends
            AbstractSqlStoreTest
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
        //
        
        log.info( "Deleting old database and creating new database" );
        
        theSqlStore.initializeHard();

        //
        
        StoreEntryMapper<String,Foo> mapper = new SerializingStoreEntryMapper<String,Foo>( SqlStoreTest2.class.getClassLoader() ) {
                public String stringToKey(
                        String stringKey )
                {
                    return stringKey;
                }
        };
        ObjectStore<String,Foo> store  = new ObjectStore<String,Foo>( mapper, theTestStore );
        
        Foo [] data1 = new Foo[] {
            new Foo( 1 ),
            new Foo( 2 ),
            new Foo( 3 ),
            new Foo( 4 ),
            new Foo( 555 )
        };
        
        //
        
        log.info( "Putting test data" );

        for( int i=0 ; i<data1.length ; ++i ) {
            Foo f = data1[i];
            store.put( f.k, f );
        }
        
        //
        
        log.info( "Reading test data" );
        
        Foo [] data2 = new Foo[ data1.length ];
        for( int i=0 ; i<data1.length ; ++i ) {
            Foo f = data1[i];
            data2[i] = store.get( f.k );
            
            checkEquals( data1[i], data2[i], "data not the same" );
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
        SqlStoreTest2 test = null;
        try {
            if( args.length != 1 ) {
                System.err.println( "Synopsis: <database engine>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new SqlStoreTest2( args );
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
     * @throws Exception all sorts of things may go wrong in a test
     */
    public SqlStoreTest2(
            String [] args )
        throws
            Exception
    {
        super( args[0], SqlStoreTest2.class );

        theTestStore = theSqlStore;
    }

    /**
     * Constructor for subclasses.
     *
     * @param dataBaseEngine the name of the database engine to use for testing
     * @param c test class
     * @throws Exception all sorts of things may go wrong in a test
     */
    protected SqlStoreTest2(
            String dataBaseEngine,
            Class  c )
        throws
            Exception
    {
        super( dataBaseEngine, c );
    }
    
    // Our Logger
    private static Log log = Log.getLogInstance( SqlStoreTest2.class);   
    
    /**
     * The Foo test class.
     */
    static class Foo
            implements
                Serializable
    {
        private static final long serialVersionUID = 1L; // make IDE happy

        public Foo(
                int myA )
        {
            k = String.valueOf( myA );
            a = myA;
            b = Math.sqrt( a );
        }
        
        @Override
        public int hashCode()
        {
            return k.hashCode();
        }

        @Override
        public boolean equals(
                Object other )
        {
            if( other instanceof Foo ) {
                Foo realOther = (Foo) other;
                
                return equals( k, realOther.k ) && ( a == realOther.a ) && ( b == realOther.b );
            }
            return false;
        }

        public int getA() {
            return a;
        }
        
        protected static boolean equals(
                String one,
                String two )
        {
            if( one == null ) {
                return two == null;
            } else {
                return one.equals( two );
            }
        }

        public String k;
        public int    a;
        public double b;        
    }
}
