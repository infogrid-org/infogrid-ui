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

import java.util.Map;

/**
 * Collects parameters that may influence the formatting of a String using StringRepresentation.
 */
public interface StringRepresentationParameters
    extends
        Map<String,Object>
{
    /**
     * The key into this object that identifies the desired maximum length of the produced String.
     */
    public final String MAX_LENGTH = "maxLength";

    /**
     * The key into this object that identifies whether or not colloquial output is desired.
     */
    public final String COLLOQUIAL = "colloquial";
}
