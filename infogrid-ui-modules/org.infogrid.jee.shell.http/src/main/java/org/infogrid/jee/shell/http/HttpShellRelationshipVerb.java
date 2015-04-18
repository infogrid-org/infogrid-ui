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

package org.infogrid.jee.shell.http;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.http.SaneRequest;

/**
 * Captures what operation should be applied to a relationship.
 */
public enum HttpShellRelationshipVerb
        implements
            HttpShellKeywords
{
    RELATE ( "relate" ) {
            /**
             * Perform this verb.
             *
             * @param source the source MeshObject
             * @param destination the destination MeshObject
             * @param sourceVarName variable name of the source MeshObject, for error reporting
             * @param destinationVarName variable name of the destination MeshObject, for error reporting
             * @param tx the Transaction if and when created
             * @param request the request
             * @throws SpecifiedMeshObjectNotFoundException thrown if one of the required MeshObjects was not given
             * @throws RelatedAlreadyException thrown if the two MeshObjects are related already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public void perform(
                    MeshObject                    source,
                    MeshObject                    destination,
                    String                        sourceVarName,
                    String                        destinationVarName,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    SpecifiedMeshObjectNotFoundException,
                    RelatedAlreadyException,
                    TransactionException,
                    NotPermittedException
            {
                if( source == null ) {
                    throw new SpecifiedMeshObjectNotFoundException( sourceVarName );
                }
                if( destination == null ) {
                    throw new SpecifiedMeshObjectNotFoundException( destinationVarName );
                }

                Transaction tx2 = tx.obtain();

                source.relate( destination );
            }
    },
    RELATE_IF_NEEDED ( "relateifneeded" ) {
            /**
             * Perform this verb.
             *
             * @param source the source MeshObject
             * @param destination the destination MeshObject
             * @param sourceVarName variable name of the source MeshObject, for error reporting
             * @param destinationVarName variable name of the destination MeshObject, for error reporting
             * @param tx the Transaction if and when created
             * @param request the request
             * @throws RelatedAlreadyException thrown if the two MeshObjects are related already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public void perform(
                    MeshObject                    source,
                    MeshObject                    destination,
                    String                        sourceVarName,
                    String                        destinationVarName,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    RelatedAlreadyException,
                    TransactionException,
                    NotPermittedException
            {
                if( source == null ) {
                    return; // be lenient
                }
                if( destination == null ) {
                    return; // be lenient
                }
                if( !source.isRelated( destination )) {
                    Transaction tx2 = tx.obtain();

                    source.relate( destination );
                }
            }
    },
    UNRELATE( "unrelate" ) {
            /**
             * Perform this verb.
             *
             * @param source the source MeshObject
             * @param destination the destination MeshObject
             * @param sourceVarName variable name of the source MeshObject, for error reporting
             * @param destinationVarName variable name of the destination MeshObject, for error reporting
             * @param tx the Transaction if and when created
             * @param request the request
             * @throws SpecifiedMeshObjectNotFoundException thrown if one of the required MeshObjects was not given
             * @throws NotRelatedException thrown if the two MeshObjects are not related
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public void perform(
                    MeshObject                    source,
                    MeshObject                    destination,
                    String                        sourceVarName,
                    String                        destinationVarName,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    SpecifiedMeshObjectNotFoundException,
                    NotRelatedException,
                    TransactionException,
                    NotPermittedException
            {
                if( source == null ) {
                    throw new SpecifiedMeshObjectNotFoundException( sourceVarName );
                }
                if( destination == null ) {
                    throw new SpecifiedMeshObjectNotFoundException( destinationVarName );
                }

                Transaction tx2 = tx.obtain();

                source.unrelate( destination );
            }
    },
    UNRELATE_IF_NEEDED( "unrelateifneeded" ) {
            /**
             * Perform this verb.
             *
             * @param source the source MeshObject
             * @param destination the destination MeshObject
             * @param sourceVarName variable name of the source MeshObject, for error reporting
             * @param destinationVarName variable name of the destination MeshObject, for error reporting
             * @param tx the Transaction if and when created
             * @param request the request
             * @throws NotRelatedException thrown if the two MeshObjects are not related
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public void perform(
                    MeshObject                    source,
                    MeshObject                    destination,
                    String                        sourceVarName,
                    String                        destinationVarName,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    NotRelatedException,
                    TransactionException,
                    NotPermittedException
            {
                if( source == null ) {
                    return; // be lenient
                }
                if( destination == null ) {
                    return; // be lenient
                }
                if( source.isRelated( destination )) {
                    Transaction tx2 = tx.obtain();

                    source.unrelate( destination );
                }
            }
    };

    /**
     * Constructor.
     *
     * @param s the String designating a verb
     */
    HttpShellRelationshipVerb(
            String s )
    {
        theString = s;
    }

    /**
     * Extract the right verb for this source object and for this destination object from this request.
     *
     * @param varName1 the variable name of the source object in the request
     * @param varName2 the variable name of the destination object in the request
     * @param request the request
     * @return the found verb
     */
    public static HttpShellRelationshipVerb findPerformFor(
            String      varName1,
            String      varName2,
            SaneRequest request )
    {
        StringBuilder key = new StringBuilder();
        key.append( PREFIX );
        key.append( varName1 );
        key.append( TO_TAG );
        key.append( SEPARATOR );
        key.append( varName2 );
        key.append( PERFORM_TAG );

        String value = request.getPostedArgument( key.toString() );
        if( value == null ) {
            return null;
        }
        value = value.toLowerCase();
        for( HttpShellRelationshipVerb v : values() ) {
            if( value.equals( v.theString )) {
                return v;
            }
        }
        // if not found, we default to null
        return null;
    }

    /**
     * Perform this verb.
     *
     * @param source the source MeshObject
     * @param destination the destination MeshObject
     * @param sourceVarName variable name of the source MeshObject, for error reporting
     * @param destinationVarName variable name of the destination MeshObject, for error reporting
     * @param tx the Transaction if and when created
     * @param request the request
     * @throws SpecifiedMeshObjectNotFoundException thrown if one of the required MeshObjects was not given
     * @throws NotRelatedException thrown if the two MeshObjects are not related
     * @throws RelatedAlreadyException thrown if the two MeshObjects are related already
     * @throws TransactionException thrown if a problem occurred with the Transaction
     * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     */
    public abstract void perform(
            MeshObject                    source,
            MeshObject                    destination,
            String                        sourceVarName,
            String                        destinationVarName,
            CreateWhenNeeded<Transaction> tx,
            SaneRequest                   request )
        throws
            SpecifiedMeshObjectNotFoundException,
            RelatedAlreadyException,
            NotRelatedException,
            TransactionException,
            NotPermittedException;

    /**
     * The String designating a verb.
     */
    protected String theString;
}
