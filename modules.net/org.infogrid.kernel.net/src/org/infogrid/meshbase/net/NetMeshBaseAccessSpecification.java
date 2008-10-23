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

/**
 * Specifies how to access a NetMeshBase.
 */
public interface NetMeshBaseAccessSpecification
{
    /**
     * Obtain the NetMeshBaseIdentifier.
     *
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetMeshBaseIdentifier();
    
    /**
     * Obtain the ScopeSpecification, if any.
     *
     * @return the ScopeSpecification
     */
    public ScopeSpecification getScopeSpecification();

    /**
     * Obtain the CoherenceSpecification, if any.
     *
     * @return the CoherenceSpecification
     */
    public CoherenceSpecification getCoherenceSpecification();

    /**
     * Convert NetMeshBaseAccessSpecification into an external form.
     *
     * @return the external form
     */
    public String toExternalForm();

    /**
     * The default scope for the object graph that we want.
     */
    public static final ScopeSpecification DEFAULT_SCOPE = null;
    
    /**
     * The default coherence for the object graph that we want.
     */
    public static final CoherenceSpecification DEFAULT_COHERENCE = null;
    
    /**
     * URL parameter keyword indicating the scope parameter.
     */
    public static final String SCOPE_KEYWORD = "lid-scope";
    
    /**
     * URL parameter keyword indicating the coherence parameter.
     */
    public static final String COHERENCE_KEYWORD = "lid-coherence";
 }
