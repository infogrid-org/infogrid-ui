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

import java.util.Map;
import java.util.Set;

/**
 * Represents a persona, which could be provisioned either locally or remotely.
 */
public interface LidPersona
        extends
            LidResource
{
    /**
     * Determine whether this LidPersona is hosted locally or remotely.
     * 
     * @return true if the LidPersona is hosted locally
     */
    public boolean isHostedLocally();

    /**
     * Obtain an attribute of the persona.
     * 
     * @param key the name of the attribute
     * @return the value of the attribute, or null
     */
    public String getAttribute(
            String key );

    /**
     * Get the set of keys into the set of attributes.
     * 
     * @return the keys into the set of attributes
     */
    public Set<String> getAttributeKeys();
    
    /**
     * Obtain the map of attributes. This breaks encapsulation, but works much better
     * for JSP pages.
     * 
     * @return the map of attributes
     */
    public Map<String,String> getAttributes();
}
