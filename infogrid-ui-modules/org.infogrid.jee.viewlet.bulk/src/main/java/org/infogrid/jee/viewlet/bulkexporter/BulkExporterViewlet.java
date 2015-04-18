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
package org.infogrid.jee.viewlet.bulkexporter;

import java.io.IOException;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.SimpleJeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.externalized.xml.BulkExternalizedMeshObjectXmlEncoder;
import org.infogrid.meshbase.IterableMeshBase;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.primitives.externalized.EncodingException;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet for an EcgTrace. Defaults from SimpleJeeViewlet.
 */
public class BulkExporterViewlet
        extends SimpleJeeViewlet
{
    private static final Log log = Log.getLogInstance(BulkExporterViewlet.class); // our own, private logger

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected BulkExporterViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super(viewed, c);
    }

    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static BulkExporterViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects(mb);
        BulkExporterViewlet         ret    = new BulkExporterViewlet(viewed, c);
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
            double               matchQuality )
    {
        return new DefaultJeeViewletFactoryChoice(toView, BulkExporterViewlet.class, matchQuality) {
                public Viewlet instantiateViewlet()
                    throws
                        CannotViewException
                {
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
            SaneRequest        request,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        HttpServletResponse response = structured.getDelegate();
        response.setContentType(MIME_TYPE);
        BulkExternalizedMeshObjectXmlEncoder theParser = new BulkExternalizedMeshObjectXmlEncoder();
        OutputStream outStream = response.getOutputStream();
        outStream.write(XML.getBytes( "UTF-8" ));
        IterableMeshBase meshBase = (IterableMeshBase) getSubject().getMeshBase(); // derive from the subject, so we can do any MeshBase
        CursorIterator<MeshObject> ret = meshBase.iterator();
        try {
            theParser.bulkWrite(ret, outStream);
        } catch (EncodingException ex) {
            log.error(ex);
        }
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
            SaneRequest        request,
            StructuredResponse response,
            Throwable          thrown)
        throws
            ServletException
    {
        // nop - the response has been streamed
    }

    /**
     * Mime header on the response.
     */
    private static final String MIME_TYPE = "application/xml+meshbulk";

    /**
     * Xml header.
     */
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
}
