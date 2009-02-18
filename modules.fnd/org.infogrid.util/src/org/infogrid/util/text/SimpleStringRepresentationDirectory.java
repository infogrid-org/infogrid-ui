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

import org.infogrid.util.CachingMap;
import org.infogrid.util.Factory;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.MSmartFactory;

/**
 * A simple implementation of StringRepresentationDirectory with default content.
 */
public class SimpleStringRepresentationDirectory
        extends
            MSmartFactory<String,StringRepresentation,StringRepresentation>
        implements
            StringRepresentationDirectory
{
    /**
     * Factory method.
     *
     * @param delegateFactory the factory that knows how to instantiate the various StringRepresentations
     * @param fallback the fallback StringRepresentation, if any
     * @return the created SimpleStringRepresentationDirectory
     */
    public static SimpleStringRepresentationDirectory create(
            Factory<String,StringRepresentation,StringRepresentation> delegateFactory,
            StringRepresentation                                      fallback )
    {
        MCachingHashMap<String,StringRepresentation> storage = MCachingHashMap.create();
        return new SimpleStringRepresentationDirectory( delegateFactory, storage, fallback );
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param delegateFactory the Factory that knows how to instantiate values
     * @param storage the storage to use for this instance
     * @param fallback the fallback StringRepresentation, if any
     */
    protected SimpleStringRepresentationDirectory(
            Factory<String,StringRepresentation,StringRepresentation> delegateFactory,
            CachingMap<String,StringRepresentation>                   storage,
            StringRepresentation                                      fallback )
    {
        super( delegateFactory, storage );
        
        theFallback = fallback;
    }

    /**
     * Obtain the fallback. This fallback is known to exist even if the factory method failed.
     * 
     * @return the fallback StringRepresentation
     */
    public StringRepresentation getFallback()
    {
        return theFallback;
    }
    
    /**
     * The fallback StringRepresentation.
     */
    protected StringRepresentation theFallback;
}
