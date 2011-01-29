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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.templates;

import org.infogrid.util.LocalizedException;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Special-purpose exception only thrown by ReportProblemTag and ReportInfoMessageTag to
 * report a problem or an informational message to the template framework.
 */
public class ReportException
        extends
            Exception
        implements
            LocalizedException
{
    /**
     * Constructor.
     *
     * @param message the message
     */
    public ReportException(
            String message )
    {
        super( message );
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        return getLocalizedMessage();
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        return "";
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        return "";
    }
}
