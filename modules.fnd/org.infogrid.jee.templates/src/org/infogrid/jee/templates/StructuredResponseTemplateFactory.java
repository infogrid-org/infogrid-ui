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

package org.infogrid.jee.templates;

import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.util.Factory;

/**
 * Marks classes that know how to create StructuredResponseTemplates.
 */
public interface StructuredResponseTemplateFactory
    extends
        Factory<SaneServletRequest,StructuredResponseTemplate,StructuredResponse>
{
    // no op
    
    /**
     * Name of a template that emits plain text without change.
     */
    public static final String VERBATIM_TEXT_NAME = "verbatim";
}
