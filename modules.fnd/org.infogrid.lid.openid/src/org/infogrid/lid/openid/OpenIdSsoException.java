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

package org.infogrid.lid.openid;

import org.infogrid.lid.LidAbortProcessingPipelineException;
import org.infogrid.lid.LidProcessingPipelineStage;

/**
 * This Exception is thrown when an error occurred while attempting to SSO.
 */
public class OpenIdSsoException
    extends
        LidAbortProcessingPipelineException
{
    /**
     * Constructor.
     *
     * @param source the LidProcessingPipelineStage that threw this exception
     * @param message the message, if any
     */
    protected OpenIdSsoException(
            LidProcessingPipelineStage source,
            String                     message )
    {
        super( source, null, message, null );
    }
}
