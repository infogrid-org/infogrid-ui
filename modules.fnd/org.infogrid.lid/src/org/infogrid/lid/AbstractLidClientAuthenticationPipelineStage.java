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

package org.infogrid.lid;

import org.infogrid.lid.credential.LidCredentialTypeFactory;
import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;

/**
 * Factors out common functionality of LidClientAuthenticationPipelineStage implementations.
 */
public abstract class AbstractLidClientAuthenticationPipelineStage
        extends
            AbstractObjectInContext
        implements
            LidClientAuthenticationPipelineStage
{
    /**
     * Constructor.
     * 
     * @param credTypeFactory the LidCredentialTypeFactory that knows about available authentication methods
     * @param personaMgr the LidPersona manager through which to find LidPersonas
     * @param sessionMgr the session manager to use for client sessions
     * @param context the context in which this object runs
     */
    protected AbstractLidClientAuthenticationPipelineStage(
            LidCredentialTypeFactory credTypeFactory,
            LidLocalPersonaManager   personaMgr,
            LidSessionManager        sessionMgr,
            Context                  context )
    {
        super( context );
        
        theCredentialTypeFactory = credTypeFactory;
        thePersonaManager        = personaMgr;
        theSessionManager        = sessionMgr;
    }
    
    /**
     * The LidCredentialTypeFactory to use.
     */
    protected LidCredentialTypeFactory theCredentialTypeFactory;
    
    /**
     * The persona manager to use.
     */
    protected LidLocalPersonaManager thePersonaManager;

    /**
     * The LidSessionManager to use.
     */
    protected LidSessionManager theSessionManager;
}
