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

package org.infogrid.httpd.util;

import java.util.Iterator;
import org.infogrid.util.ArrayCursorIterator;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * A class to capture a list of name-value pairs in memory-efficient (but not
 * necessarily computationally efficient) manner. This class is never
 * expected to be invoked by more than one Thread.
 */
public class NameValueList
        implements
            CanBeDumped
{
    /**
     * Add a name-value pair to the list.
     *
     * @param name the name component of the pair
     * @param value the value component of the pair
     */
    public void add(
            String name,
            String value )
    {
        name = name.toLowerCase();

        if( mark==theNames.length ) {
            String [] temp = theNames;
            theNames = new String[ temp.length*2 ];
            for( int i=0 ; i<temp.length ; ++i ) {
                theNames[i] = temp[i];
            }
            temp = theValues;
            theValues = new String[ temp.length*2 ];
            for( int i=0 ; i<temp.length ; ++i ) {
                theValues[i] = temp[i];
            }
        }
        theNames[ mark ]  = name;
        theValues[ mark ] = value;

        ++mark;
    }

    /**
     * Obtain a value for this name.
     *
     * @param name the name
     * @return the found value, or null
     */
    public String get(
            String name )
    {
        name = name.toLowerCase();

        for( int i=0 ; i<mark ; ++i ) {
            if( theNames[i].equals( name )) {
                return theValues[i];
            }
        }
        return null;
    }

    /**
     * Obtain all values for this name.
     *
     * @param name the name
     * @return the found values, or an empty array
     */
    public String [] getAll(
            String name )
    {
        int count = 0;
        for( int i=0 ; i<mark ; ++i ) {
            if( theNames[i].equals( name )) {
                ++count;
            }
        }
        String [] ret = new String[ count ];
        for( int i=mark-1 ; i>=0 ; --i ) {
            if( theNames[i].equals( name )) {
                ret[--count] = theValues[i];
            }
        }
        return ret;
    }

    /**
     * Obtain an Iterator over the names.
     *
     * @return an Iterator over the names
     */
    public Iterator<String> keyIterator()
    {
        return ArrayCursorIterator.create( theNames, 0, 0, mark );
    }

    /**
     * Determine whether this NameValueList has any content whatsoever.
     *
     * @return true if this NameValueList is empty
     */
    public boolean isEmpty()
    {
        return mark == 0;
    }

    /**
     * Determine the number of entries in this NameValueList.
     *
     * @return the number of entries
     */
    public int getSize()
    {
        return mark;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theNames",
                    "theValues",
                    "mark"
                },
                new Object[] {
                    theNames,
                    theValues,
                    mark
                } );
    }

    /**
     * The names.
     */
    private String [] theNames = new String[4];

    /**
     * The values.
     */
    private String [] theValues = new String[4];

    /**
     * The watermark.
     */
    private int mark = 0;
}
