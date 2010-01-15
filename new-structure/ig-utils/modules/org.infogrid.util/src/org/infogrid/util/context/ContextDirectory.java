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

import org.infogrid.util.CursorIterator;

/**
 * A place where to look up Contexts by name. Some applications benefit from having
 * the ability to name contexts, and look them up by name again later. The use
 * of this class is optional.
 */
public interface ContextDirectory
{
    /**
     * Add a Context to the directory.
     *
     * @param toAdd the Context to add
     * @throws IllegalArgumentException thrown if a Context by this name exists already
     */
    public void addContext(
            Context toAdd );

    /**
     * Obtain a Context from the directory.
     *
     * @param name the name of the Context
     * @return the found Context, or null if not found
     */
    public Context getContext(
            String name );

    /**
     * Obtain an Iterator over the content of this ContextDirectory.
     *
     * @return the Iterator
     */
    public CursorIterator<Context> iterator();
}
