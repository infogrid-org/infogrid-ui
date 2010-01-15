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

package org.infogrid.meshbase.net;

import org.infogrid.meshbase.IterableMeshBase;

/**
 * A NetMeshBase that is also iterable.
 */
public interface IterableNetMeshBase
        extends
            NetMeshBase,
            IterableMeshBase
{
    /**
     * Factory method for a IterableMeshBaseDifferencer, with this IterableMeshBase
     * being the comparison base. This overrides the method defined higher up
     * in the inheritance hierarchy with a more specific subtype.
     *
     * @return the IterableMeshBaseDifferencer
     */
    public abstract IterableNetMeshBaseDifferencer getDifferencer();    
}
