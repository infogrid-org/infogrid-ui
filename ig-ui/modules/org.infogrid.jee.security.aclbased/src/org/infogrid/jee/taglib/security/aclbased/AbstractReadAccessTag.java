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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.security.aclbased;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.meshbase.security.aclbased.AclbasedSubjectArea;

/**
 * Factors out functionality common to testing for read access.
 */
public abstract class AbstractReadAccessTag
        extends
            AbstractAclBasedTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    protected AbstractReadAccessTag()
    {
        // noop
    }

    /**
     * Evaluatate whether the caller has read access to a given MeshObject.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean hasReadAccess()
        throws
            JspException,
            IgnoreException
    {
        boolean ret = hasAccess( AclbasedSubjectArea.MESHOBJECT_HASREADACCESSTO_PROTECTIONDOMAIN.getSource() );
        return ret;
    }
}
