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

package org.infogrid.jee.viewlet.servlet;

import java.net.MalformedURLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.ServletExceptionWithHttpStatusCode;
import org.infogrid.util.AbstractLocalizedException;

/**
 * A version of ViewletDispatcherServlet that does not give any indication why an incoming request
 * was unsuccessful: access denied, non-existing object etc. This is often useful in production
 * environments to give attackers less information.
 */
public class WontTellViewletDispatcherServlet
        extends
            ViewletDispatcherServlet
{
    /**
     * Handle exceptions thrown when attempting to create a MeshObjectsToView. This method is
     * factored out so subclasses can easily override.
     *
     * @param t the thrown exception
     */
    @Override
    protected void handleMeshObjectsToViewFactoryException(
            Throwable t )
        throws
            ServletException
    {
        if( t instanceof MalformedURLException ) {
            throw new ServletException( t );
        }

        Throwable cause = t.getCause();

        throw new ServletExceptionWithHttpStatusCode( new WontTellException( cause ), HttpServletResponse.SC_FORBIDDEN ); // 402
    }

    static class WontTellException
            extends
                AbstractLocalizedException
    {
        /**
         * Constructor with the original exception that isn't being emitted.
         *
         * @param cause the cause
         */
        public WontTellException(
                Throwable cause )
        {
            super( cause );
        }

        /**
         * {@inheritDoc}
         */
        public Object [] getLocalizationParameters()
        {
            return new Object[0];
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String findStringRepresentationParameter()
        {
            return findParameterViaEnclosingClass( STRING_REPRESENTATION_KEY );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String findStringRepresentationLinkStartParameter()
        {
            return findParameterViaEnclosingClass( STRING_REPRESENTATION_LINK_START_KEY );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected String findStringRepresentationLinkEndParameter()
        {
            return findParameterViaEnclosingClass( STRING_REPRESENTATION_LINK_END_KEY );
        }
    }
}
