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

import org.infogrid.lid.yadis.DeclaresYadisFragment;
import javax.servlet.RequestDispatcher;

/**
 * Thrown by stages of the LID pipeline during processing of an incoming request, for the sole purpose
 * of interrupting the regular control flow in the LidProcessingPipeline.
 */
public class LidAbortProcessingPipelineException
        extends
            Exception
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param source the LidProcessingPipelineStage that threw this exception
     */
    public LidAbortProcessingPipelineException(
            LidProcessingPipelineStage source )
    {
        this( source, null );
    }

    /**
     * Constructor.
     *
     * @param source the LidProcessingPipelineStage that threw this exception
     * @param dispatcherToRun the RequestDispatcher to run as a result of this Exception, if any
     */
    public LidAbortProcessingPipelineException(
            LidProcessingPipelineStage source,
            RequestDispatcher          dispatcherToRun )
    {
        theSource          = source;
        theDispatcherToRun = dispatcherToRun;
    }

    /**
     * Obtain the RequestDispatcher to execute in order to produce the result.
     * 
     * @return the RequestDispatcher
     */
    public RequestDispatcher getRequestDispatcher()
    {
        return theDispatcherToRun;
    }

    /**
     * The LidProcessingPipelineStage that threw this exception.
     */
    protected transient LidProcessingPipelineStage theSource;
            
    /**
     * The RequestDispatcher to run, if any.
     */
    protected transient RequestDispatcher theDispatcherToRun;
}
