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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.viewlet;

import org.infogrid.module.ModuleRegistry;
import org.infogrid.util.FactoryException;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;

/**
 * No Viewlet could be found with the the required Viewlet type.
 */
public class NoViewletFoundException
        extends
            FactoryException
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
     * For debugging.
     *
     * @return String representation of this object.
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
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
     * Determine the correct internationalized string that can be shown to the
     * user when the LocalizedException is thrown.
     *
     * @param formatter the formatter to use for data objects to be displayed as part of the message
     * @return the internationalized string
     */
    public String getLocalizedMessage(
            LocalizedObjectFormatter formatter )
    {
        String ret;
        if( theObjectsToView.getViewletTypeName() == null ) {
            if( formatter != null ) {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "NoViewletFoundWithoutTypeMessage",
                        formatter.asLocalizedString( theObjectsToView.getSubject() ));
            } else {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "NoViewletFoundWithoutTypeMessage",
                        theObjectsToView.getSubject() );
            }
        } else {
            if( formatter != null ) {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "NoViewletFoundWithTypeMessage",
                        formatter.asLocalizedString( theObjectsToView.getSubject() ));
            } else {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "NoViewletFoundWithTypeMessage",
                        theObjectsToView.getSubject() );
            }
        }
        return ret;
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
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance(  NoViewletFoundException.class );
}
