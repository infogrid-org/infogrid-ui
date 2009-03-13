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

package org.infogrid.store.TEST;

import org.infogrid.store.IterableStore;
import org.infogrid.store.SerializingStoreEntryMapper;
import org.infogrid.store.m.MStore;
import org.infogrid.store.util.IterableStoreBackedSwappingHashMap;
import org.infogrid.util.logging.Log;

/**
 * Tests iteration over the content of a StoreBackedSwappingHashMap.
 */
public class StoreBackedSwappingHashMapTest2
        extends
            AbstractStoreTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may go wrong during a test
     */
    public void run()
        throws
            Exception
    {
        //
        
        IterableStore store = MStore.create();
        
        SerializingStoreEntryMapper<String,Integer> mapper = new SerializingStoreEntryMapper<String,Integer>() {
            public String stringToKey(
                    String s )
            {
                return s;
            }
        };
        
        IterableStoreBackedSwappingHashMap<String,Integer> map = IterableStoreBackedSwappingHashMap.createWeak( mapper, store );
                
        String [] keys = {
            "a",
            "d",
            "c",
            "e",
            "b",
        };

        log.info( "Inserting" );
        
        for( int i=0 ; i<keys.length ; ++i ) {
            map.put( keys[i], new Integer( i ));
        }
            
        checkEquals( map.size(), keys.length, "Wrong size map" );
        
        //
        
        log.info( "Waiting and iterating" );
        
        Thread.sleep( 2000L );
        collectGarbage();

        int count = 0;
        for( String key : map.keySet() ) {
            Integer value = map.get( key );
            
            checkObject( value, "Value not found for key " + key );
            
            // they come in a different sequence
            int found = -1;
            for( int i=0 ; i<keys.length ; ++i ) {
                if( key.equals( keys[i] )) {
                    found = i;
                    break;
                }
            }
            if( found < 0 ) {
                reportError( "Could not find key in the original key list", key );
            } else {
                checkEquals( found, value.intValue(), "Wrong value found at index " + found );
            }
            
            ++count;
        }
        checkEquals( count,      keys.length, "Wrong number of elements found via iterator" );
        checkEquals( map.size(), keys.length, "Wrong size map" );
    }

    
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        StoreBackedSwappingHashMapTest2 test = null;
        try {
            if( args.length < 0 ) { // well, not quite possible but to stay with the general outline
                System.err.println( "Synopsis: <no arguments>" );
                System.err.println( "aborting ..." );
                System.exit( 1 );
            }

            test = new StoreBackedSwappingHashMapTest2( args );
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
     * @throws Exception all sorts of things may go wrong during a test
     */
    public StoreBackedSwappingHashMapTest2(
            String [] args )
        throws
            Exception
    {
        super( StoreBackedSwappingHashMapTest2.class  );
    }

    // Our Logger
    private static Log log = Log.getLogInstance( StoreBackedSwappingHashMapTest2.class  );
}
