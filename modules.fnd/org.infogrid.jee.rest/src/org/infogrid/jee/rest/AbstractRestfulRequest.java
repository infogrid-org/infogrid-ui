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

package org.infogrid.jee.rest;

import java.net.URISyntaxException;
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
     * @param contextPath the context path of the JEE application
     */
    protected AbstractRestfulRequest(
            SaneRequest lidRequest,
            String      contextPath )
    {
        theSaneRequest = lidRequest;
        theContextPath = contextPath;
        
        StringBuilder buf = new StringBuilder();
        buf.append( lidRequest.getRootUri() );
        buf.append( contextPath );
        theAbsoluteContextPath = buf.toString();
    }

    /**
     * Obtain the context path of the application.
     *
     * @return the context path
     */
    public String getContextPath()
    {
        return theContextPath;
    }

    /**
     * Obtain the absolute context path of the application.
     *
     * @return the context path
     */
    public String getAbsoluteContextPath()
    {
        return theAbsoluteContextPath;
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
     * Determine the requested traversal, if any.
     * 
     * @return the traversal
     */
    public String getRequestedTraversal()
    {
        if( theRequestedTraversal == null ) {
            String format = theSaneRequest.getArgument( LID_TRAVERSAL_PARAMETER_NAME );

            if( format != null ) {
                theRequestedTraversal = format;
            } else {
                theRequestedTraversal = NO_ANSWER_STRING;
            }
        }
        if( (Object) theRequestedTraversal != (Object) NO_ANSWER_STRING ) { // typecast to avoid warning
            return theRequestedTraversal;
        } else {
            return null;
        }        
    }
    
    /**
     * Obtain the name of the requested Viewlet, if any.
     *
     * @return class name of the requested Viewlet
     */
    public String getRequestedViewletClassName()
    {
        if( theRequestedViewletClass == null ) {
            theRequestedViewletClass = determineArgumentForMulti( LID_FORMAT_PARAMETER_NAME, VIEWLET_PREFIX );
        }
        if( (Object) theRequestedViewletClass != (Object) NO_ANSWER_STRING ) { // typecast to avoid warning
            return theRequestedViewletClass;
        } else {
            return null;
        }
    }
    
    /**
     * Obtain the requested MIME type, if any.
     * 
     * @return the requuested MIME type, if any
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
        String [] formats = theSaneRequest.getMultivaluedArgument( argName );
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
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    public MeshBaseIdentifier determineRequestedMeshBaseIdentifier()
            throws
                URISyntaxException
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
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    public MeshObjectIdentifier determineRequestedMeshObjectIdentifier()
            throws
                URISyntaxException
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
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    protected abstract void calculate()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                URISyntaxException;

    /**
     * Determine the requested MeshObject.
     * 
     * @return the MeshObject, or null if not found
     * @throws MeshObjectAccessException thrown if the requested MeshObject could not be accessed
     * @throws NotPermittedException thrown if the caller did not have the permission to perform this operation
     * @throws URISyntaxException thrown if the request URI could not be parsed
     */
    public MeshObject determineRequestedMeshObject()
            throws
                MeshObjectAccessException,
                NotPermittedException,
                URISyntaxException
    {
        if( theRequestedMeshObject == null ) {
            calculate();
        }
        return theRequestedMeshObject;
    }

    /**
     * The underlying SaneRequest.
     */
    protected SaneRequest theSaneRequest;

    /**
     * The context path of the web application.
     */
    protected String theContextPath;

    /**
     * The calculated absolute context path.
     */
    protected String theAbsoluteContextPath;

    /**
     * The requested traversal, if any. (buffered)
     */
    protected String theRequestedTraversal = null;

    /**
     * The requested Viewlet class, if any. (buffered)
     */
    protected String theRequestedViewletClass = null;

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
     * Buffered values are set to this String to express "we did the parsing, but did not
     * find an answer."
     */
    protected static final String NO_ANSWER_STRING = new String( "" ); // don't want to do String.intern() here
}
