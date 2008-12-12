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

import java.text.MessageFormat;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.PartialResultException;
import org.infogrid.util.ResourceHelper;

/**
 * Thrown if something went wrong when trying to access a MeshObject. The underlying
 * cause may indicate what went wrong.
 */
public class MeshObjectAccessException
        extends
            AbstractLocalizedException
        implements
            PartialResultException<MeshObject[]>
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param mb the MeshBase in which the Exception occurred
     * @param mbIdentifier the MeshBaseIdentifier in which the Exception occurred
     * @param partialResult a partial result, if any, available at the time the Exception occurred
     * @param failedIdentifiers the MeshObjectIdentifiers 
     * @param cause the underlying cause for the Exception
     */
    public MeshObjectAccessException(
            MeshBase                mb,
            MeshBaseIdentifier      mbIdentifier,
            MeshObject []           partialResult,
            MeshObjectIdentifier [] failedIdentifiers,
            Throwable               cause )
    {
        super( null, cause ); // avoid construction of default message

        theMeshBase           = mb;
        theMeshBaseIdentifier = mbIdentifier;
        thePartialResult      = partialResult;
        theFailedIdentifiers  = failedIdentifiers;
    }

    /**
     * Obtain the MeshBase in which the Exception occurred.
     *
     * @return the MeshBase in which the Exception occurred
     */
    public MeshBase getMeshBase()
    {
        return theMeshBase;
    }

    /**
     * Determine whether a partial result is available.
     *
     * @return true if a partial result is available
     */
    public boolean isPartialResultAvailable()
    {
        return thePartialResult != null;
    }

    /**
     * Obtain the partial result, if any.
     *
     * @return the partial result, if any
     */
    public MeshObject [] getBestEffortResult()
    {
        return thePartialResult;
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
     * Convert to string form, for debugging.
     *
     * @return string form of this object, for debugging
     */
    @Override
    public String toString()
    {
        StringBuilder almostRet = new StringBuilder( super.toString() );
        almostRet.append( "{ mb: " );
        almostRet.append( theMeshBase );
        almostRet.append( ", cause: " );
        almostRet.append( getCause() );
        almostRet.append( " }" );
        return almostRet.toString();
    }

    /**
     * Obtain the parameters with which the internationalized string
     * will be parameterized.
     *
     * @return the parameters with which the internationalized string will be parameterized
     */
    public Object [] getLocalizationParameters()
    {
        String [] ret = new String[ theFailedIdentifiers.length ];

        for( int i=0 ; i<theFailedIdentifiers.length ; ++i ) {
            ret[i] = theFailedIdentifiers[0].toExternalForm();
        }
        return ret;
    }

    /**
     * The MeshBase in which this Exception occurred.
     */
    protected transient MeshBase theMeshBase;

    /**
     * The identifier of the MeshBase in which this Exception occurred.
     */
    protected MeshBaseIdentifier theMeshBaseIdentifier;

    /**
     * The partial result (if any).
     */
    protected transient MeshObject [] thePartialResult;

    /**
     * The identifiers of the MeshObjects whose access failed.
     */
    protected MeshObjectIdentifier [] theFailedIdentifiers;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MeshObjectAccessException.class );
    
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