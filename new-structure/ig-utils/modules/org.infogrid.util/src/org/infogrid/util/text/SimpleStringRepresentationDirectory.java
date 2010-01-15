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

package org.infogrid.util.text;

import org.infogrid.util.MNameServer;
import org.infogrid.util.WritableNameServer;

/**
 * A simple implementation of StringRepresentationDirectory with default content.
 */
public class SimpleStringRepresentationDirectory
        extends
            MNameServer<String,StringRepresentation>
        implements
            StringRepresentationDirectory,
            WritableNameServer<String,StringRepresentation>
{
    /**
     * Factory method.
     *
     * @return the created SimpleStringRepresentationDirectory
     */
    @SuppressWarnings("unchecked") // No idea what this is needed
    public static SimpleStringRepresentationDirectory create()
    {
        SimpleStringRepresentationDirectory ret = new SimpleStringRepresentationDirectory();
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
\     */
    protected SimpleStringRepresentationDirectory()
    {
        super( );
    }

    /**
     * Set the fallback.
     *
     * @param newValue new value
     */
    public void setFallback(
            StringRepresentation newValue )
    {
        if( newValue == null ) {
            throw new NullPointerException();
        }
        theFallback = newValue;
    }

    /**
     * Obtain the fallback. This fallback is known to exist even if the factory method failed.
     * 
     * @return the fallback StringRepresentation
     */
    public StringRepresentation getFallback()
    {
        if( theFallback == null ) {
            throw new NullPointerException( "No fallback was specified when initializing " + this );
        }
        return theFallback;
    }
    
    /**
     * The fallback StringRepresentation.
     */
    protected StringRepresentation theFallback;
}
