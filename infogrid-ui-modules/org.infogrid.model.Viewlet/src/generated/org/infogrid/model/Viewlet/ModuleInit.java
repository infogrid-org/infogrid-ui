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
// Copyright 1998-2013 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

//
// This file has been generated AUTOMATICALLY. DO NOT MODIFY.
// on Sat, 2015-04-18 16:39:28 +0000
//

package org.infogrid.model.Viewlet;

import java.io.IOException;
import org.infogrid.model.Viewlet.V.SubjectAreaLoader;
import org.infogrid.model.primitives.TimeStampValue;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.module.Module;
import org.infogrid.module.ModuleActivationException;

/**
  * Activates the module by loading the Subject Area.
  */
public class ModuleInit
{
    /**
     * Activate this Module.
     *
     * @param dependentModules the dependent Modules
     * @param contextObjects context objects of the dependent Modules
     * @param theModule the current Module
     * @return the Subject Area as context object
     * @throws ModuleActivationException thrown if the Module could not be activated
     */
    public static Object activate(
            Module [] dependentModules,
            Object [] contextObjects,
            Module    theModule )
        throws
            ModuleActivationException
    {
        try {
            ModelBase mb = ModelBaseSingleton.getSingleton();

            SubjectAreaLoader saLoader = new SubjectAreaLoader( mb, theModule.getClassLoader() );

            return saLoader.loadAndCheckModel( mb.getMeshTypeLifecycleManager(), TimeStampValue.now());

        } catch( MeshTypeNotFoundException | IOException ex ) {
            throw new ModuleActivationException( theModule.getModuleMeta(), ex );
       }
    }
}
