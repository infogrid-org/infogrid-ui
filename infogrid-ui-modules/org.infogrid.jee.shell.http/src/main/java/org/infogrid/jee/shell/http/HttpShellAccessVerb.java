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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.shell.http;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.MeshObjectsNotFoundException;
import org.infogrid.meshbase.sweeper.Sweeper;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.http.SaneRequest;

/**
 * Captures how a MeshObject should be accessed by the shell.
 */
public enum HttpShellAccessVerb
        implements
            HttpShellKeywords
{
    CREATE( "create" ) {
            /**
             * Perform this verb.
             *
             * @param identifier the MeshObjectIdentifier of the to-be-accessed object in the request
             * @param mb the MeshBase to use for the access
             * @param tx the Transaction if and when created
             * @param request the request
             * @return the accessed MeshObject
             * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public MeshObject access(
                    MeshObjectIdentifier          identifier,
                    MeshBase                      mb,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    MeshObjectIdentifierNotUniqueException,
                    TransactionException,
                    NotPermittedException
            {
                Transaction tx2 = tx.obtain();
                MeshObject  ret;
                
                if( identifier != null ) {
                    ret = mb.getMeshBaseLifecycleManager().createMeshObject( identifier );
                } else {
                    ret = mb.getMeshBaseLifecycleManager().createMeshObject();
                }
                return ret;
            }
    },
    FIND( "find" ) {
            /**
             * Perform this verb.
             *
             * @param identifier the MeshObjectIdentifier of the to-be-accessed object in the request
             * @param mb the MeshBase to use for the access
             * @param tx the Transaction if and when created
             * @param request the request
             * @return the accessed MeshObject
             * @throws MeshObjectAccessException thrown if accessLocally failed
             * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public MeshObject access(
                    MeshObjectIdentifier          identifier,
                    MeshBase                      mb,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    MeshObjectAccessException,
                    MeshObjectIdentifierNotUniqueException,
                    MeshObjectsNotFoundException,
                    TransactionException,
                    NotPermittedException
            {
                MeshObject found = null;
                if( identifier != null ) {
                    found = mb.accessLocally( identifier );
                }

                if( found != null ) {
                    return found;
                } else {
                    throw new MeshObjectsNotFoundException( mb, identifier );
                }
            }
    },
    FIND_IF_EXISTS( "findifexists" ) {
            /**
             * Perform this verb.
             *
             * @param identifier the MeshObjectIdentifier of the to-be-accessed object in the request
             * @param mb the MeshBase to use for the access
             * @param tx the Transaction if and when created
             * @param request the request
             * @return the accessed MeshObject
             * @throws MeshObjectAccessException thrown if accessLocally failed
             * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public MeshObject access(
                    MeshObjectIdentifier          identifier,
                    MeshBase                      mb,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    MeshObjectAccessException,
                    MeshObjectIdentifierNotUniqueException,
                    MeshObjectsNotFoundException,
                    TransactionException,
                    NotPermittedException
            {
                MeshObject found = null;
                if( identifier != null ) {
                    found = mb.accessLocally( identifier );
                }
                // return null if not found
                return found;
            }
    },
    CREATEORFIND( "createorfind" ) {
            /**
             * Perform this verb.
             *
             * @param identifier the MeshObjectIdentifier of the to-be-accessed object in the request
             * @param mb the MeshBase to use for the access
             * @param tx the Transaction if and when created
             * @param request the request
             * @return the accessed MeshObject
             * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public MeshObject access(
                    MeshObjectIdentifier          identifier,
                    MeshBase                      mb,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    MeshObjectIdentifierNotUniqueException,
                    MeshObjectsNotFoundException,
                    TransactionException,
                    NotPermittedException
            {
                if( identifier == null ) {
                    throw new MeshObjectsNotFoundException( mb, identifier );
                }
                MeshObject found = mb.findMeshObjectByIdentifier( identifier );
                if( found == null ) {
                    Transaction tx2 = tx.obtain();

                    if( identifier != null ) {
                        found = mb.getMeshBaseLifecycleManager().createMeshObject( identifier );
                    } else {
                        found = mb.getMeshBaseLifecycleManager().createMeshObject();
                    }
                }
                return found;
            }
    },
    DELETE( "delete" ) {
            /**
             * Perform this verb.
             *
             * @param identifier the MeshObjectIdentifier of the to-be-accessed object in the request
             * @param mb the MeshBase to use for the access
             * @param tx the Transaction if and when created
             * @param request the request
             * @return the accessed MeshObject
             * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public MeshObject access(
                    MeshObjectIdentifier          identifier,
                    MeshBase                      mb,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    MeshObjectIdentifierNotUniqueException,
                    MeshObjectsNotFoundException,
                    TransactionException,
                    NotPermittedException
            {
                if( identifier == null ) {
                    throw new MeshObjectsNotFoundException( mb, identifier );
                }
                MeshObject found = mb.findMeshObjectByIdentifier( identifier );
                if( found == null ) {
                    throw new MeshObjectsNotFoundException( mb, identifier );
                }

                Transaction tx2 = tx.obtain();
                mb.getMeshBaseLifecycleManager().deleteMeshObject( found );

                return found;
            }
    },
    SWEEP( "sweep" ) {
            /**
             * Perform this verb.
             *
             * @param identifier the MeshObjectIdentifier of the to-be-accessed object in the request
             * @param mb the MeshBase to use for the access
             * @param tx the Transaction if and when created
             * @param request the request
             * @return the accessed MeshObject
             * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
             * @throws TransactionException thrown if a problem occurred with the Transaction
             * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
             */
            public MeshObject access(
                    MeshObjectIdentifier          identifier,
                    MeshBase                      mb,
                    CreateWhenNeeded<Transaction> tx,
                    SaneRequest                   request )
                throws
                    MeshObjectIdentifierNotUniqueException,
                    MeshObjectsNotFoundException,
                    TransactionException,
                    NotPermittedException
            {
                if( identifier == null ) {
                    throw new MeshObjectsNotFoundException( mb, identifier );
                }
                MeshObject found = mb.findMeshObjectByIdentifier( identifier );
                if( found == null ) {
                    throw new MeshObjectsNotFoundException( mb, identifier );
                }

                Sweeper s = mb.getSweeper();
                if( s != null ) {
                    s.sweepObject( found );
                }

                return found;
            }
    };

    /**
     * Constructor.
     *
     * @param s the String designating a verb
     */
    HttpShellAccessVerb(
            String s )
    {
        theString = s;
    }

    /**
     * Extract the right verb for this source object from this request.
     *
     * @param varName the variable name of the to-be-accessed object in the request
     * @param request the request
     * @return the found verb
     */
    public static HttpShellAccessVerb findAccessFor(
            String      varName,
            SaneRequest request )
    {
        StringBuilder key = new StringBuilder();
        key.append( PREFIX );
        key.append( varName );
        key.append( ACCESS_TAG );

        String value = request.getPostedArgument( key.toString() );
        if( value == null ) {
            return FIND;
        }
        value = value.toLowerCase();
        for( HttpShellAccessVerb v : values() ) {
            if( value.equals(  v.theString )) {
                return v;
            }
        }
        // if not found, we default to FIND
        return FIND;
    }

    /**
     * Perform this verb.
     *
     * @param identifier the MeshObjectIdentifier of the to-be-accessed object in the request
     * @param mb the MeshBase to use for the access
     * @param tx the Transaction if and when created
     * @param request the request
     * @return the accessed MeshObject
     * @throws MeshObjectAccessException thrown if accessLocally failed
     * @throws MeshObjectIdentifierNotUniqueException thrown if a MeshObject with this identifier exists already
     * @throws MeshObjectsNotFoundException thrown if the MeshObject could not be found
     * @throws TransactionException thrown if a problem occurred with the Transaction
     * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     */
    public abstract MeshObject access(
            MeshObjectIdentifier          identifier,
            MeshBase                      mb,
            CreateWhenNeeded<Transaction> tx,
            SaneRequest                   request )
        throws
            MeshObjectAccessException,
            MeshObjectIdentifierNotUniqueException,
            MeshObjectsNotFoundException,
            TransactionException,
            NotPermittedException;

    /**
     * The String designating a verb.
     */
    protected String theString;
}
