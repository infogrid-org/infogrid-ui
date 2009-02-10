/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.infogrid.jee.viewlet.image;

import java.io.IOException;
import javax.servlet.ServletException;
import org.infogrid.jee.rest.RestfulRequest;
import org.infogrid.jee.templates.BinaryStructuredResponseSection;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.AbstractJeeViewlet;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.Blob.BlobSubjectArea;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.AbstractViewedMeshObjects;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.DefaultViewedMeshObjects;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Viewlet that displays images.
 */
public class ImageViewlet
        extends
            AbstractJeeViewlet
        implements
            JeeViewlet
{
    private static final Log log = Log.getLogInstance( ImageViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static ImageViewlet create(
            Context c )
    {
        DefaultViewedMeshObjects viewed = new DefaultViewedMeshObjects();
        ImageViewlet             ret    = new ImageViewlet( viewed, c );

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
        return new DefaultViewletFactoryChoice( ImageViewlet.class, matchQuality ) {
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
    protected ImageViewlet(
            AbstractViewedMeshObjects viewed,
            Context                   c )
    {
        super( viewed, c );
    }

    /**
     * Process the incoming RestfulRequest. Default implementation that can be
     * overridden by subclasses.
     * 
     * @param restful the incoming RestfulRequest
     * @param structured the StructuredResponse into which to write the result
     * @throws javax.servlet.ServletException processing failed
     * @throws java.io.IOException I/O error
     */
    @Override
    public void processRequest(
            RestfulRequest     restful,
            StructuredResponse structured )
        throws
            ServletException,
            IOException
    {
        MeshObject subject = theViewedMeshObjects.getSubject();
        
        BlobValue content = null;
        try {
            content = (BlobValue) subject.getPropertyValue( BlobSubjectArea.IMAGE_CONTENT );

        } catch( IllegalPropertyTypeException ex ) {
            log.error( ex );
        } catch( NotPermittedException ex ) {
            log.error( ex );
        }
        if( content != null ) {
            BinaryStructuredResponseSection section = structured.getDefaultBinarySection();
            section.setContent( content.value() );
            section.setMimeType( content.getMimeType() );
        }
    }
}
