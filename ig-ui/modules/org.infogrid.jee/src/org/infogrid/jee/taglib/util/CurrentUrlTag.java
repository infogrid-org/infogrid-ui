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

package org.infogrid.jee.taglib.util;

import com.sun.net.ssl.internal.www.protocol.https.Regexp;
import java.io.IOException;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.util.http.SaneRequest;

/**
 * Emits the current URL, minus arguments specified as removedArguments, and plus arguments specified as
 * additionalArguments>.
 */
public class CurrentUrlTag
        extends
            AbstractInfoGridTag
{
    /**
     * Constructor.
     */
    public CurrentUrlTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theAdditionalArguments = null;
        theRemovedArguments    = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the additionalArguments property.
     *
     * @return value of the additionalArguments property
     * @see #setAdditionalArguments
     */
    public final String getAdditionalArguments()
    {
        return theAdditionalArguments;
    }

    /**
     * Set value of the additionalArguments property.
     *
     * @param newValue new value of the additionalArguments property
     * @see #getAdditionalArguments
     */
    public final void setAdditionalArguments(
            String newValue )
    {
        theAdditionalArguments = newValue;
    }

    /**
     * Obtain value of the removedArguments property.
     *
     * @return value of the removedArguments property
     * @see #setRemovedArguments
     */
    public final String getRemovedArguments()
    {
        return theRemovedArguments;
    }

    /**
     * Set value of the removedArguments property.
     *
     * @param newValue new value of the removedArguments property
     * @see #getRemovedArguments
     */
    public final void setRemovedArguments(
            String newValue )
    {
        theRemovedArguments = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        SaneRequest sane  = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );
        String      query = sane.getQueryString();

        if( query == null ) {
            query = "";
        }
        if( theRemovedArguments != null && theRemovedArguments.length() > 0 ) {
            for( String toRemove : theRemovedArguments.split( "&" )) {
                if( query.equals( toRemove )) {
                    query = "";
                    break;
                }
                String quotedToRemove = Pattern.quote( toRemove );
                query = query.replaceAll( quotedToRemove + "&", "" );
                query = query.replaceAll( "&" + quotedToRemove, "" );
            }
        }
        if( theAdditionalArguments != null ) {
            if( query.length() > 0 ) {
                query = query + "&" + theAdditionalArguments;
            } else {
                query = theAdditionalArguments;
            }
        }
        print( sane.getAbsoluteBaseUri() );
        if( query.length() > 0 ) {
            print( "?" );
            print( query );
        }

        return EVAL_PAGE;
    }

    /**
     * URL arguments to add, if any.
     */
    protected String theAdditionalArguments;

    /**
     * URL arguments to remove, if any.
     */
    protected String theRemovedArguments;
}
