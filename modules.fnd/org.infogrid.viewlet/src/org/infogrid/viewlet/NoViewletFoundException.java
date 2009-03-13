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

package org.infogrid.viewlet;

import org.infogrid.module.ModuleRegistry;
import org.infogrid.util.FactoryException;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * No Viewlet could be found with the the required Viewlet type.
 */
public class NoViewletFoundException
        extends
            FactoryException
        implements
            HasStringRepresentation,
            CanBeDumped
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param sender the Factory that threw this exception
     * @param o which MeshObjectsToView could not be viewed
     */
    public NoViewletFoundException(
            ViewletFactory    sender,
            MeshObjectsToView o )
    {
        super( sender );

        theObjectsToView  = o;
        theModuleRegistry = null;
    }

    /**
     * Constructor.
     *
     * @param sender the Factory that threw this exception
     * @param o which MeshObjectsToView could not be viewed
     * @param registry the ModuleRegistry in which the Viewlet was not found
     */
    public NoViewletFoundException(
            ViewletFactory    sender,
            MeshObjectsToView o,
            ModuleRegistry    registry )
    {
        super( sender );

        theObjectsToView  = o;
        theModuleRegistry = registry;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "objectsToView",
                    "moduleRegistry"
                },
                new Object[] {
                    theObjectsToView,
                    theModuleRegistry
        } );
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context,
            int                         maxLength )
    {
        if( theObjectsToView.getViewletTypeName() == null ) {
            return rep.formatEntry(
                    getClass(),
                    DEFAULT_NO_VIEWLET_TYPE_ENTRY,
                    maxLength,
                    theObjectsToView.getSubject(),
                    theObjectsToView.getSubject().getIdentifier(),
                    theObjectsToView.getSubject().getIdentifier().toExternalForm() );

        } else {
            return rep.formatEntry(
                    getClass(),
                    DEFAULT_VIEWLET_TYPE_ENTRY,
                    maxLength,
                    theObjectsToView.getSubject(),
                    theObjectsToView.getSubject().getIdentifier(),
                    theObjectsToView.getSubject().getIdentifier().toExternalForm() );
        }
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param additionalArguments additional arguments for URLs, if any
     * @param target the HTML target, if any
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            String                      additionalArguments,
            String                      target,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }

    /**
     * The MeshObjectsToView that could not be viewed.
     */
    protected MeshObjectsToView theObjectsToView;

    /**
     * The ModuleRegistry that did not contain a suitable Viewlet.
     */
    protected transient ModuleRegistry theModuleRegistry;
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( NoViewletFoundException.class );

    /**
     * The default entry in the resouce files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_ENTRY = "Message";

    /**
     * The default entry in the resouce files for the case where no ViewletType has
     * been specified, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_NO_VIEWLET_TYPE_ENTRY = "MessageNoViewletType";

    /**
     * The default entry in the resouce files for the case where a ViewletType has
     * been specified, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_VIEWLET_TYPE_ENTRY = "MessageViewletType";
}
