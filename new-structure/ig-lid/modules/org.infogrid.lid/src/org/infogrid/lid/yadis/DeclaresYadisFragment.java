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

package org.infogrid.lid.yadis;

/**
 * Interface implemented by all processing stages in the LID processing pipeline
 * that declare a fragment that needs to go into the Yadis file.
 */
public interface DeclaresYadisFragment
{
    /**
     * Obtain the Yadis fragment to be inserted into an XRDS file. This String may
     * contain parameters according to the conventions of the
     * <code>java.text.MessageFormat</code> class.
     * 
     * @return the Yadis fragment, potentially with parameters
     */
    public abstract String getParameterizedYadisFragment();

    /**
     * Default LID version.
     */
    public static final String DEFAULT_LID_SERVICE_VERSION = "2.0";
}
