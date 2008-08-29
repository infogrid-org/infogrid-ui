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

package org.infogrid.meshbase;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;

import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.ResourceHelper;

import java.text.MessageFormat;

/**
 * Thrown if one or more MeshObjects could not be found.
 */
public class MeshObjectsNotFoundException
        extends
            MeshObjectAccessException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param mb the MeshBase that threw this Exception
     * @param identifier the identifier of the MeshObject that was not found
     */
    public MeshObjectsNotFoundException(
            MeshBase             mb,
            MeshObjectIdentifier identifier )
    {
        this( mb, null, new MeshObjectIdentifier[] { identifier } );
    }

    /**
     * Constructor.
     * 
     * @param mb the MeshBase that threw this Exception
     * @param identifiers the identifiers of the MeshObjects some of which were not found
     * @param partialResult the subset of MeshObjects that were found, in the same sequence and position as identifiers.
     *        MeshObjects that were not found are null in this array.
     */
    public MeshObjectsNotFoundException(
            MeshBase                mb,
            MeshObject []           partialResult,
            MeshObjectIdentifier [] identifiers )
    {
        super( mb, mb.getIdentifier(), partialResult, identifiers, null );
    }
    
    /**
     * Determine the correct internationalized string that can be shown to the
     * user when the LocalizedException is thrown.
     *
     * @param formatter the formatter to use for data objects to be displayed as part of the message
     * @return the internationalized string
     */
    @Override
    public String getLocalizedMessage(
            LocalizedObjectFormatter formatter )
    {
        Object [] params = getLocalizationParameters();
        Object [] formattedParams;
        if( formatter != null ) {
            formattedParams = new Object[ params.length ];
            for( int i=0 ; i<formattedParams.length ; ++i ) {
                formattedParams[i] = formatter.asLocalizedString( params[i] );
            }
        } else {
            formattedParams = params;
        }
        
        String message = theResourceHelper.getResourceStringOrDefault(
                formattedParams.length == 1 ? MESSAGE_SINGULAR_PARAMETER : MESSAGE_PLURAL_PARAMETER,
                null );
        String conjunction = theResourceHelper.getResourceStringOrDefault( MESSAGE_CONJUNCTION_PARAMETER, ", " );

        StringBuilder tmp = new StringBuilder();
        String        sep = "";
        for( int i=0 ; i < formattedParams.length ; ++i ) {
            tmp.append( sep );
            tmp.append( formattedParams[i] );
            sep = conjunction;
        }
        try {
            message = MessageFormat.format( message, new Object[] { tmp.toString() } );

        } catch( IllegalArgumentException ex ) {
            message = message + "(error while formatting translated message)";
        }
        return message;
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        return super.theFailedIdentifiers;
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MeshObjectsNotFoundException.class );
    
    /**
     * Name of the resource parameter that defines the message in the singular.
     */
    public static final String MESSAGE_SINGULAR_PARAMETER = "SingularMessage";
    
    /**
     * Name of the resource parameter that defines the message in the plural.
     */
    public static final String MESSAGE_PLURAL_PARAMETER = "PluralMessage";
    
    /**
     * Name of the resource parameter that defines the conjunction, such as ", "
     */
    public static final String MESSAGE_CONJUNCTION_PARAMETER = "MessageConjunction";
}
