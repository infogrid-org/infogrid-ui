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

package org.infogrid.jee.rest.templates;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.ModelPrimitivesStringRepresentation;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.util.SimpleHtmlLocalizedObjectFormatter;

/**
 * A LocalizedObjectFormatter for html text that is aware of MeshObjects and MeshTypes.
 */
public class RestfulHtmlLocalizedObjectFormatter
        extends
            SimpleHtmlLocalizedObjectFormatter
{
    /**
     * Factory method. This always returns the same instance.
     * 
     * @return the created SimplePlainLocalizedObjectFormatter
     */
    public static RestfulHtmlLocalizedObjectFormatter create()
    {
        return theSingleton;
    }

    /**
     * Private constructor for subclasses only.
     */
    protected RestfulHtmlLocalizedObjectFormatter()
    {
        // no op
    }

    /**
     * Convert an Object to a String representation.
     *
     * @param o the Object
     * @return String representation of the Object
     */
    @Override
    public String asLocalizedString(
            Object o )
    {
        if( o instanceof MeshObject || o instanceof MeshObjectIdentifier ) {
            MeshObjectIdentifier ref = ( o instanceof MeshObjectIdentifier ) ? ((MeshObjectIdentifier)o) : ((MeshObject)o).getIdentifier();

            String ret = ref.toStringRepresentation( ModelPrimitivesStringRepresentation.TEXT_HTML );
            return "&quot;" + ret + "&quot;";

        } else if( o instanceof MeshType ) {
            MeshType realO = (MeshType) o;

            String ret = PropertyValue.toStringRepresentation( realO.getUserVisibleName(), ModelPrimitivesStringRepresentation.TEXT_HTML );
            return "&quot;" + ret + "&quot;";
        
        } else {
            return super.asLocalizedString( o );
        }
    }
    
    /**
     * Singleton instance.
     */
    private static RestfulHtmlLocalizedObjectFormatter theSingleton = new RestfulHtmlLocalizedObjectFormatter();
}
