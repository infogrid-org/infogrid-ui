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

package org.infogrid.jee.taglib.security.aclbased;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.meshbase.security.aclbased.AclbasedSubjectArea;

/**
 * Factors out functionality common to testing for update access.
 */
public abstract class AbstractUpdateAccessTag
        extends
            AbstractAclBasedTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    protected AbstractUpdateAccessTag()
    {
        // noop
    }

    /**
     * Evaluate whether the caller has update access to a given MeshObject.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean hasUpdateAccess()
        throws
            JspException,
            IgnoreException
    {
        boolean ret = hasAccess( AclbasedSubjectArea.MESHOBJECT_HASUPDATEACCESSTO_PROTECTIONDOMAIN.getSource() );
        return ret;
    }
}
