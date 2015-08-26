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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.testharness.tomcat;

import java.util.Map;
import org.infogrid.module.Module;

/**
 * The initializer class for this Module.
 */
public abstract class Init
{
    /**
     * Configure this Module.
     *
     * @param parameters the parameters for initialization
     * @param whereParametersSpecifiedMap maps which Modules specified each parameter
     * @param thisModule the Module to be configured
     * @throws Exception may throw a range of Exceptions
     */
    public static void configure(
            Map<String,Object> parameters,
            Map<String,Module> whereParametersSpecifiedMap,
            Module             thisModule )
        throws
            Exception
    {
        theModule = thisModule;
    }
    
    /**
     * Obtain the Module that this piece of code belongs to.
     * 
     * @return the Module
     */
    public static Module getModule()
    {
        return theModule;
    }
    
    /**
     * The Module that this piece of code belongs to.
     */
    protected static Module theModule;
}
