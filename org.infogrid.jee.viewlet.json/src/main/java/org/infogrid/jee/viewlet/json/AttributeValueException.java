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

package org.infogrid.jee.viewlet.json;

/**
 * Thrown if any of the attributes passed in the request are invalid.
 */
public class AttributeValueException extends Exception {

    /**
     * The url parameter in error
     */
    private String identifier;

    AttributeValueException(String message, String identifier) {
        super(message);
        this.identifier = identifier;
    }

    public String getId() {
        return this.identifier;
    }
}
