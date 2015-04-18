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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.viewlet;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.mesh.MeshObjectLinkTag;
import org.infogrid.jee.viewlet.AbstractPagingCursorIterableViewlet;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.text.StringifierException;

/**
 * Tag that emits an HTML link to a different page in the output of a AbstractPagingCursorIterableViewlet.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class NavigateToPageTag
    extends
        MeshObjectLinkTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public NavigateToPageTag()
    {
        // noop
    }

    /**
     * Initialize.
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
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshObject subject = lookupMeshObjectOrThrow( JeeViewlet.SUBJECT_ATTRIBUTE_NAME );
        MeshObject start   = lookupMeshObjectOrThrow( "meshObject", theMeshObject, "meshObjectName", theMeshObjectName );

        String toAppend = AbstractPagingCursorIterableViewlet.PAGE_START_NAME + "=" + HTTP.encodeToValidUrlArgument( start.getIdentifier().toExternalForm() );

        String additionalArguments;
        if( theAddArguments != null ) {
            additionalArguments = theAddArguments + "&" + toAppend;
        } else {
            additionalArguments = toAppend;
        }

        try {
            String text = getFormatter().formatMeshObjectLinkStart( pageContext, subject, additionalArguments, theTarget, theTitle, theStringRepresentation );
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        return EVAL_BODY_INCLUDE;
    }
}
