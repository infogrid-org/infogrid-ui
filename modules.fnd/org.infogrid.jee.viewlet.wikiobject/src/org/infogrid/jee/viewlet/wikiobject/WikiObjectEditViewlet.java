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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet.wikiobject;

import javax.servlet.ServletException;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.jee.viewlet.DefaultJeeViewletStateTransitionEnum;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionAction;
import org.infogrid.meshbase.transaction.TransactionActionException;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.Wiki.WikiSubjectArea;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet that can display and edit WikiObjects.
 */
public class WikiObjectEditViewlet
        extends
            AbstractJeeViewlet
{
    private static final Log log = Log.getLogInstance( WikiObjectEditViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param c the application context
     * @return the created Viewlet
     */
    public static WikiObjectEditViewlet create(
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        WikiObjectEditViewlet    ret    = new WikiObjectEditViewlet( viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            double matchQuality )
    {
        return new DefaultViewletFactoryChoice( WikiObjectEditViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet(
                        MeshObjectsToView        toView,
                        Context                  c )
                    throws
                        CannotViewException
                {
                    return create( c );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected WikiObjectEditViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the GET method has been requested.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the GET execution of the servlet.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     */
    @Override
    public void performBeforeGet(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        preprocess( request, response );
    }
    
    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet, e.g. the evaluation of POST commands.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     */
    @Override
    public void performBeforeSafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        preprocess( request, response );
    }
    
    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and no FormTokenService has been used.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet.</p>
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     */
    public void performBeforeSafeOrUnsafePost(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        preprocess( request, response );
    }

    /**
     * Internal helper to set up prior to rendering.
     * 
     * @param request the incoming request
     * @param response the response to be assembled
     * @throws ServletException thrown if an error occurred
     */
    protected void preprocess(
            RestfulRequest     request,
            StructuredResponse response )
        throws
            ServletException
    {
        SaneRequest sane = request.getSaneRequest();

        final String postedContent = sane.getPostArgument( "current-content" );
        String       oldContent    = "";

        try {
            BlobValue oldValue = (BlobValue) getSubject().getPropertyValue( WikiSubjectArea.WIKIOBJECT_CONTENT );
            if( oldValue != null ) {
                oldContent = oldValue.getAsString();
            }

            if( theViewletStateTransition == null ) {
                theCurrentContent = oldContent;
                
            } else {
                switch( (DefaultJeeViewletStateTransitionEnum) theViewletStateTransition ) {
                    case DO_EDIT:
                        theCurrentContent = oldContent;
                        break;

                    case DO_PREVIEW:
                        theCurrentContent = postedContent;
                        break;

                    case DO_COMMIT:
                        theCurrentContent = postedContent;
                        getSubject().getMeshBase().executeAsap( new TransactionAction<Void>() {
                            public Void execute(
                                    Transaction tx )
                                throws
                                    TransactionActionException,
                                    TransactionException
                            {
                                try {
                                    getSubject().setPropertyValue( WikiSubjectArea.WIKIOBJECT_CONTENT, BlobValue.create( postedContent, BlobValue.TEXT_HTML_MIME_TYPE ));

                                } catch( Exception ex ) {
                                    throw new TransactionActionException.Error( ex );
                                }
                                return null;
                            }
                        });

                        break;

                    case DO_CANCEL:
                        theCurrentContent = oldContent;
                        break;
                }
            }

        } catch( IllegalPropertyTypeException ex ) {
            throw new ServletException( ex );

        } catch( TransactionException ex ) {
            throw new ServletException( ex );

        } catch( TransactionActionException ex ) {
            throw new ServletException( ex );

        } catch( NotPermittedException ex ) {
            throw new ServletException( ex );
        }
    }
    
    /**
     * Obtain the current content of the WikiObject, specific to the session.
     *
     * @return the current content
     */
    public String getCurrentContent()
    {
        return theCurrentContent;
    }
    
    /**
     * The current content of the WikiObject.
     */
    protected String theCurrentContent;
}
