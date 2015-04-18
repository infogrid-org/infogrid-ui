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

package org.infogrid.jee.taglib.lid;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.lid.account.LidAccount;
import org.infogrid.lid.servlet.LidPipelineServlet;

/**
 * Throw exception and thus end processing if the current user does not have an account.
 * This is used to protect JSP files from being incorrectly invokable for the wrong user.
 */
public class RequireAccountTag
    extends
        IfHasAccountTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        LidAccount account = (LidAccount) pageContext.getRequest().getAttribute( LidPipelineServlet.ACCOUNT_ATTRIBUTE_NAME );

        if( evaluateTest( account ) ) {
            return SKIP_BODY;

        } else {
            throw new JspException( new RequireAccountTagException());
        }
    }
}
