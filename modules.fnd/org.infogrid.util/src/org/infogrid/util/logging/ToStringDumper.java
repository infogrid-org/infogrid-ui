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

package org.infogrid.util.logging;

import java.util.HashSet;
import java.util.Set;
import org.infogrid.util.StringHelper;

/**
 * A simple implementation of Dumper that dumps to a String.
 */
public class ToStringDumper
        extends
            AbstractDumper
        implements
            BufferingDumper
{
    /**
     * Factory method.
     *
     * @return the created ToStringDumper
     */
    public static ToStringDumper create()
    {
        return new ToStringDumper();
    }

    /**
     * Constructor.
     */
    protected ToStringDumper()
    {
        // nothing
    }

    /**
     * Reset the internal buffer.
     */
    public void reset()
    {
        theBuffer        = new StringBuilder();
        theAlreadyDumped = new HashSet<Object>();
    }

    /**
     * Obtain the result.
     *
     * @return the result
     */
    public String getBuffer()
    {
        return theBuffer.toString();
    }

    /**
     * Emit a String that was put together as part of dumping.
     *
     * @param toEmit the String to emit
     */
    protected void emit(
            CharSequence toEmit )
    {
        CharSequence indented = StringHelper.indent( toEmit, INDENT, 0, level );
        theBuffer.append( indented );
    }

    /**
     * Emit a single character that was put together as part of dumping.
     *
     * @param toEmit the character to emit
     */
    protected void emit(
            char toEmit )
    {
        theBuffer.append( toEmit );

        if( toEmit == '\n' ) {
            for( int i=0 ; i<level ; ++i ) {
                theBuffer.append( INDENT );
            }
        }
    }

    /**
     * Determine whether an object should be dumped with all details.
     *
     * @param obj the Object to be tested
     * @return true if the Object should be dumped with all details
     */
    protected boolean shouldBeDumpedFull(
            Object obj )
    {
        if( obj == null ) {
            return true;

        } else if( theAlreadyDumped.contains( obj )) {
            return false;

        } else {
            return true;
        }
    }

    /**
     * Determine whether an object should be dumped with minimal details.
     *
     * @param obj the Object to be tested
     * @return true if the Object should be dumped with minimum details
     */
    protected boolean shouldBeDumpedShort(
            Object obj )
    {
        return true;
    }

    /**
     * Register an object as having been dumped already.
     *
     * @param obj the Object to be registered
     */
    protected void registerAsDumped(
            Object obj )
    {
        if( obj != null ) {
            theAlreadyDumped.add( obj );
        }
    }

    /**
     * The internal buffer.
     */
    protected StringBuilder theBuffer = new StringBuilder();

    /**
     * The Objects dumped so far.
     */
    protected Set<Object> theAlreadyDumped = new HashSet<Object>();

    /**
     * The character String by which child objects are indented.
     */
    public static final String INDENT = "    ";
}
