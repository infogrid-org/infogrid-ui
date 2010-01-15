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

package org.infogrid.probe.xml;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.probe.ProbeException;

import org.w3c.dom.DocumentType;

/**
  * This Exception refines ProbeException.SyntaxError in order to
  * indicate specific XML-related errors.
  */
public abstract class XmlProbeException
        extends
            ProbeException.SyntaxError
{
    /**
     * Construct one.
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param msg the message
     * @param cause the Exception that caused this Exception
     */
    protected XmlProbeException(
            NetMeshBaseIdentifier dataSourceIdentifier,
            String                msg,
            Throwable             cause )
    {
        super( dataSourceIdentifier, msg, cause );
    }

    /**
     * This is the "document type not found" Exception, indicating that
     * while we were able to read the XML file, we could not figure out
     * its DTD.
     */
    public static class CannotDetermineDtd
            extends
                XmlProbeException
    {
        private static final long serialVersionUID = 1L; // helps with serialzation

        /**
         * Constructor. It does NOT take a message, but our best guess at the
         * document type, which could be null.
         *
         * @param dataSourceIdentifier identifies the data source that is being accessed
         * @param docType our best guess at the document type
         * @param cause the Exception that caused this Exception
         */
        public CannotDetermineDtd(
                NetMeshBaseIdentifier dataSourceIdentifier,
                DocumentType          docType,
                Exception             cause )
        {
            super(  dataSourceIdentifier,
                    docType == null
                            ? "No document type given"
                            : "Unknown document type \"" + docType.getName() + "\"", cause );
        }
    }
}
