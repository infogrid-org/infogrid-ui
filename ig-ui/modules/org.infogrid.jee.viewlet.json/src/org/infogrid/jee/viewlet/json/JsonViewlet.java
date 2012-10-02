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

package org.infogrid.jee.viewlet.json;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.SimpleJeeViewlet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Writes the mesh object directed acyclic graph (DAG) for "this" object to the response as Json, making sure not to write
 * the same object twice. Or, return a Json error.
 * <p>
 * Accepts several URL arguments, namely:
 * <p>
 * <ul>
 * <li>level=n - number of traversal away from this object - 0 says infinity.
 * <li>dateenc=func|string - specifies how to encode dates - either as a string or function. ASP.net likes the latter.
 * <li>ignoreblessing=entitytype_name_regex - the name of an entitytype that blesses any object in the graph for which
 *            properties and relationships should not be output. The main reason for this is to remove things like
 *            ProbeModel blessings which are generally uninteresting to the client.
 * <li>meta=[no|false]|[true|yes]|only - whether or not to output meta information about the object (default is true),
 *            or just output the meta.
 * <li>trimsa=[yes|true]|[no|false] - whether or not to trim the subject area prefix from the property type names.
 * </ul>
 */
public class JsonViewlet
        extends SimpleJeeViewlet {

    private static final Log log = Log.getLogInstance(JsonViewlet.class); // our own, private logger

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected JsonViewlet(
            JeeViewedMeshObjects viewed,
            Context c) {
        super(viewed, c);
    }

    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static JsonViewlet create(
            MeshBase mb,
            Context c) {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects(mb);
        JsonViewlet ret = new JsonViewlet(viewed, c);
        viewed.setViewlet(ret);
        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            JeeMeshObjectsToView toView,
            double matchQuality) {
        return new DefaultJeeViewletFactoryChoice(toView, JsonViewlet.class, matchQuality) {

            @Override
            public Viewlet instantiateViewlet()
                    throws
                    CannotViewException {
                return create(getMeshObjectsToView().getMeshBase(), getMeshObjectsToView().getContext());
            }
        };
    }

    /**
     * Process the incoming request.
     * Set the mime type in the real response so the structured response remains empty.
     * Stream out the XML.
     *
     * @param request the incoming request
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    @Override
    public void processRequest(
            SaneRequest request,
            StructuredResponse structured)
            throws
            ServletException,
            IOException {
        HttpServletResponse response = structured.getDelegate();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MIME_TYPE);
        MeshObjectJsonEncoder theParser = MeshObjectJsonEncoder.create(structured, request, getContext());
        theParser.write(getViewedMeshObjects().getSubject());
    }

    /**
     * <p>Invoked after to the execution of the Servlet. It is the hook by which
     * the JeeViewlet can perform whatever operations needed after to the execution of the servlet, e.g.
     * logging. Subclasses will often override this.</p>
     *
     * @param request the incoming request
     * @param response the response to be assembled
     * @param thrown if this is non-null, it is the Throwable indicating a problem that occurred
     *        either during execution of performBefore or of the servlet.
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeSafePost
     * @see #performBeforeUnsafePost
     */
    @Override
    public void performAfter(
            SaneRequest request,
            StructuredResponse response,
            Throwable thrown)
            throws
            ServletException {
        // nop - the response has been streamed
    }
    /**
     * mime header on the response
     */
    private static final String MIME_TYPE = "application/json";
}
