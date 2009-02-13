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

package org.infogrid.util.context;

import java.util.HashMap;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.MapCursorIterator;

/**
 * A simple implementation of ContextDirectory.
 */
public class SimpleContextDirectory
    implements
        ContextDirectory
{
    /**
     * Factory method.
     *
     * @return the created SimpleContextDirectory
     */
    public static SimpleContextDirectory create()
    {
        return new SimpleContextDirectory();
    }

    /**
     * Add a Context to the directory.
     *
     * @param toAdd the Context to add
     * @throws IllegalArgumentException thrown if a Context by this name exists already
     */
    public synchronized void addContext(
            Context toAdd )
    {
        Context found = theContextTable.put( toAdd.getName(), toAdd );
        if( found != null ) {
            // put it back in and throw
            theContextTable.put( found.getName(), found );
            throw new IllegalArgumentException( "Context exists already: " + toAdd.getName() );
        }
    }

    /**
     * Obtain a Context from the directory.
     *
     * @param name the name of the Context
     * @return the found Context, or null if not found
     */
    public Context getContext(
            String name )
    {
        Context ret = theContextTable.get( name );
        return ret;
    }

    /**
     * Obtain an Iterator over the content of this ContextDirectory.
     *
     * @return the Iterator
     */
    public CursorIterator<Context> iterator()
    {
        return MapCursorIterator.createForValues( theContextTable, String.class, Context.class );
    }

    /**
     * The table that maps Context names (not hierarchical names) to the actual Context instance.
     */
    protected HashMap<String,Context> theContextTable = new HashMap<String,Context>();
}
