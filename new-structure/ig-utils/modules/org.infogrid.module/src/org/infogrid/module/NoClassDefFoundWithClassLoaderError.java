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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.module;

/**
 * Extends NoClassDefFoundError with the ModuleClassLoader that attempted to find the Class.
 */
public class NoClassDefFoundWithClassLoaderError
        extends
            NoClassDefFoundError
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructs a <code>NoClassDefFoundError</code> with the specified
     * detail message.
     *
     * @param s   the detail message.
     * @param loader the ModuleClassLoader that was used
     */
    public NoClassDefFoundWithClassLoaderError(
            String            s,
            ModuleClassLoader loader )
    {
    	super( s );

        theClassLoader = loader;
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( super.toString() );
        buf.append( ": ClassLoader of Module " );
        buf.append( theClassLoader.getModule().getModuleName() );

        if( theClassLoader.getModule().getModuleVersion() != null ) {
            buf.append( ", version: " );
            buf.append( theClassLoader.getModule().getModuleVersion() );
        }
        return buf.toString();
    }

    /**
     * The ClassLoader through which loading failed.
     */
    protected ModuleClassLoader theClassLoader;
}
