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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.rest;

import java.text.ParseException;
import java.util.Map;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * Collects common behaviors of implementations of RestfulRequest.
 */
public abstract class AbstractRestfulRequest
        implements
            RestfulRequest
{
    private static final Log log = Log.getLogInstance( AbstractRestfulRequest.class ); // our own, private logger

    /**
     * Constructor.
     * 
     * @param lidRequest the underlying incoming SaneRequest
     * @param defaultMeshBaseIdentifier the identifier of the default MeshBase
     */
    protected AbstractRestfulRequest(
            SaneRequest        lidRequest,
            MeshBaseIdentifier defaultMeshBaseIdentifier )
    {
        theSaneRequest               = lidRequest;
        theDefaultMeshBaseIdentifier = defaultMeshBaseIdentifier;
    }

    /**
     * Obtain the underlying SaneRequest.
     *
     * @return the SaneRequest
     */
    public SaneRequest getSaneRequest()
    {
        return theSaneRequest;
    }

    /**
     * Determine the requested traversal parameters, if any.
     *
     * @return the traversal parameters
     */
    public String [] getRequestedTraversalParameters()
    {
        String [] format = theSaneRequest.getMultivaluedUrlArgument( LID_TRAVERSAL_PARAMETER_NAME );
        return format;
    }

    /**
     * Obtain the name of the requested Viewlet type, if any.
     *
     * @return type name of the requested Viewlet
     */
    public String getRequestedViewletTypeName()
    {
        if( theRequestedViewletTypeName == null ) {
            theRequestedViewletTypeName = determineArgumentForMulti( LID_FORMAT_PARAMETER_NAME, VIEWLET_PREFIX );
        }
        if( (Object) theRequestedViewletTypeName != (Object) NO_ANSWER_STRING ) { // typecast to avoid warning
            return theRequestedViewletTypeName;
        } else {
            return null;
        }
    }
    
    /**
     * Obtain the requested MIME type, if any.
     * 
     * @return the requested MIME type, if any
     */
    public String getRequestedMimeType()
    {
        if( theRequestedMimeType == null ) {
            theRequestedMimeType = determineArgumentForMulti( LID_FORMAT_PARAMETER_NAME, MIME_PREFIX );
        }
        if( (Object) theRequestedMimeType != (Object) NO_ANSWER_STRING ) { // typecast to avoid warning
            return theRequestedMimeType;
        } else {
            return null;
        }
    }

    /**
     * Factors out functionality to determine the value of a URL argument with prefix.
     *
     * @param argName the name of the URL argument
     * @param prefix the prefix
     * @return the found value, or NO_ANSWER_STRING
     */
    protected String determineArgumentForMulti(
            String argName,
            String prefix )
    {
        String [] formats = theSaneRequest.getMultivaluedUrlArgument( argName );
        String    ret     = NO_ANSWER_STRING;

        if( formats != null ) {
            for( int i=0 ; i<formats.length ; ++i ) {
                if( formats[i].startsWith( prefix )) {
                    ret = formats[i].substring( prefix.length() );
                    break;
                }
            }
        }
        
        return ret;
    }

    /**
     * Determine the identifier of the requested MeshBase.
     * 
     * @return the MeshBaseIdentifier
     * @throws ParseException thrown if the request URI could not be parsed
     */
    public MeshBaseIdentifier determineRequestedMeshBaseIdentifier()
            throws
                ParseException
    {
        if( theRequestedMeshBaseIdentifier == null ) {
            try {
                calculate();

            } catch( MeshObjectAccessException ex ) {
                log.warn( ex ); // this is not critical here
            } catch( NotPermittedException ex ) {
                log.warn( ex ); // this is not critical here
            }
        }
        return theRequestedMeshBaseIdentifier;
    }

    /**
     * Determine the identifier of the requested MeshObject.
     * 
     * @return the MeshObjectIdentifier
     * @throws ParseException thrown if the request URI could not be parsed
     */
    public MeshObjectIdentifier determineRequestedMeshObjectIdentifier()
            throws
                ParseException
    {
        if( theRequestedMeshObjectIdentifier == null ) {
            try {
                calculate();

            } catch( MeshObjectAccessException ex ) {
                log.warn( ex ); // this is not critical here
            } catch( NotPermittedException ex ) {
                log.warn( ex ); // this is not critical here
            }
        }
        return theRequestedMeshObjectIdentifier;
    }
    
    /**
     * Internal method to calculate the data.
     * 
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws ParseException thrown if the request URI could not be parsed
     */
    protected abstract void calculate()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                ParseException;

    /**
     * Determine the requested MeshObject.
     * 
     * @return the MeshObject, or null if not found
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws ParseException thrown if the request URI could not be parsed
     */
    public MeshObject determineRequestedMeshObject()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                ParseException
    {
        if( theRequestedMeshObject == null ) {
            calculate();
        }
        return theRequestedMeshObject;
    }

    /**
     * Obtain the parameters for the Viewlet, if any.
     *
     * @return the parameters, if any
     */
    public Map<String,String[]> getViewletParameters()
    {
        return theSaneRequest.getUrlArguments();
    }

    /**
     * The underlying SaneRequest.
     */
    protected SaneRequest theSaneRequest;

    /**
     * The requested Viewlet type, if any. (buffered)
     */
    protected String theRequestedViewletTypeName = null;

    /**
     * The requested MIME type, if any. (buffered)
     */
    protected String theRequestedMimeType = null;

    /**
     * The requested MeshBaseIdentifier.
     */
    protected MeshBaseIdentifier theRequestedMeshBaseIdentifier;

    /**
     * The requested MeshObjectIdentifier.
     */
    protected MeshObjectIdentifier theRequestedMeshObjectIdentifier = null;

    /**
     * The MeshObject found by accessing the right MeshBase with the MeshObjectIdentifier.
     */
    protected MeshObject theRequestedMeshObject;

    /**
     * The identifier of the default MeshBase.
     */
    protected MeshBaseIdentifier theDefaultMeshBaseIdentifier;

    /**
     * Buffered values are set to this String to express "we did the parsing, but did not
     * find an answer."
     */
    protected static final String NO_ANSWER_STRING = new String( "" ); // don't want to do String.intern() here
}
