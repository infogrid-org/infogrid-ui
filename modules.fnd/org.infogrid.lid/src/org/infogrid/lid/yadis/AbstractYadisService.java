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

package org.infogrid.lid.yadis;

import org.infogrid.lid.LidProcessingPipelineStage;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;

/**
 * Useful superclass for classes that implement Yadis services.
 */
public abstract class AbstractYadisService
        extends
            AbstractObjectInContext
        implements
            DeclaresYadisFragment,
            LidProcessingPipelineStage
{
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param yadisFragment Yadis fragment of this DeclaresYadisFragment
     * @param c the context in which this <code>ObjectInContext</code> runs.
     */
    protected AbstractYadisService(
            String  yadisFragment,
            Context c )
    {
        super( c );

        theYadisFragment = yadisFragment;
    }

    /**
     * Obtain the Yadis fragment to be inserted into an XRDS file.
     * 
     * @return the Yadis fragment
     */
    public String getParameterizedYadisFragment()
    {
        return theYadisFragment;
    }
    
    /**
     * Yadis fragment.
     */
    protected String theYadisFragment;
}
