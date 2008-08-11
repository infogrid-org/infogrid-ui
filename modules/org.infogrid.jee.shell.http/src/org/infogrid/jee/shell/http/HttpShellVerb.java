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

package org.infogrid.jee.shell.http;

import java.net.URISyntaxException;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.sane.SaneServletRequest;
//import org.infogrid.jee.taglib.InfoGridJspUtils;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.RoleTypeNotBlessedException;
import org.infogrid.mesh.RoleTypeRequiresEntityTypeException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseIdentifierFactory;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.MeshObjectsNotFoundException;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.ModelPrimitivesStringRepresentation;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.PropertyValueParsingException;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.LocalizedObject;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.NameServer;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;

/**
 * The verbs recognized by the {@link org.infogrid.jee.shell.http.HttpShellFilter HttpShellFilter}
 * and their behavior.
 */
public enum HttpShellVerb
        implements
            LocalizedObject
{
    /**
     * <p>Create a <code>MeshObject</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be created.
     *        A <code>MeshObject</code> with this Identifier must not exist yet.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.object</code></td>
     *    <td>If given, identifier (external form of the <code>MeshObject</code>'s Identifier)
     *        for another object to relate to after the creation of the subject</td>
     *    <td>0 or 1</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertytype</code></td>
     *    <td>Identifier for a <code>PropertyType</code> (external form of the <code>PropertyType</code>'s
     *        Identifier) to set once the subject has been created. Repeated use of this parameter
     *        indicates several properties.</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertyvalue</code></td>
     *    <td>New <code>PropertyValue</code> for the property on the subject (external form of
     *        the <code>PropertyValue</code>) identified by
     *        <code>mesh.subject</code> and <code>mesh.propertytype</code></td>
     *    <td>0..N. Must be given the same number of times as <code>mesh.propertytype</code>,
     *        in the same sequence.</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.subjecttype</code></td>
     *    <td>Identifier for an <code>EntityType</code> (external form of the <code>EntityType</code>'s Identifier)
     *        that the subject is to be blessed with after it has been created</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.roletype</code></td>
     *    <td>Identifier for a type of role (external form of the <code>RoleType</code>'s Identifier)
     *        the subject is to play with the object</td>
     *    <td>0..N. Only permitted if <code>mesh.object</code> is given</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    CREATE( "create" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws IdentifierNotUniqueException thrown if the Identifier of a to-be-created MeshObject exists already
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EssentialArgumentMissingException,
                MeshObjectIdentifierNotUniqueException,
                InconsistentArgumentsException,
                MeshTypeNotFoundException,
                IllegalPropertyTypeException,
                NotPermittedException,
                PropertyValueParsingException,
                RelatedAlreadyException,
                RoleTypeBlessedAlreadyException,
                EntityNotBlessedException,
                NotRelatedException,
                TransactionException,
                URISyntaxException
        {
            if( subject != null ) {
                throw new MeshObjectIdentifierNotUniqueException( subject );
            }
            
            if( subjectIdentifier != null ) {
                subject = mb.getMeshBaseLifecycleManager().createMeshObject( subjectIdentifier );
            } else {
                subject = mb.getMeshBaseLifecycleManager().createMeshObject();
            }
            potentiallyBlessEntityTypes( lidRequest, subject, mb, false );
            potentiallySetProperties(    lidRequest, subject, mb, false );
            potentiallyRelateAndBless(   lidRequest, subject, object, mb, false );
        }
    },

    /**
     * <p>Find an existing <code>MeshObject</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be 
     *        found (via <code>accessLocally</code>).</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.object</code></td>
     *    <td>If given, identifier (external form of the <code>MeshObject</code>'s Identifier)
     *        for another object to relate to once it has been found.</td>
     *    <td>0 or 1</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertytype</code></td>
     *    <td>Identifier for a <code>PropertyType</code> (external form of the <code>PropertyType</code>'s
     *        Identifier) to set once the subject has been found. Repeated use of this parameter
     *        indicates several properties.</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertyvalue</code></td>
     *    <td>New <code>PropertyValue</code> for the property on the subject (external form of
     *        the <code>PropertyValue</code>) identified by
     *        <code>mesh.subject</code> and <code>mesh.propertytype</code></td>
     *    <td>0..N. Must be given the same number of times as <code>mesh.propertytype</code>,
     *        in the same sequence.</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.subjecttype</code></td>
     *    <td>Identifier for an <code>EntityType</code> (external form of the <code>EntityType</code>'s Identifier)
     *        that the subject is to be blessed with after its has been found</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.roletype</code></td>
     *    <td>Identifier for a type of role (external form of the <code>RoleType</code>'s Identifier)
     *        the subject plays with the object</td>
     *    <td>0..N. Only permitted if <code>mesh.object</code> is given</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    ACCESS_LOCALLY( "accessLocally" ) {
        /**
         * Perform the operation that goes with this verb.
         * 
         * @param lidRequest the incoming request
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws HttpShellException thrown if the operation could not be performed
         */
        @Override
        public void performVerb(
                SaneServletRequest lidRequest )
            throws
                NotPermittedException,
                HttpShellException
        {
            String meshBaseString = lidRequest.getPostArgument( HttpShellFilter.MESH_BASE_TAG );
            String subjectString  = lidRequest.getPostArgument( HttpShellFilter.SUBJECT_TAG );
            String objectString   = lidRequest.getPostArgument( HttpShellFilter.OBJECT_TAG );

            if( meshBaseString != null ) {
                meshBaseString = meshBaseString.trim();
            }
            if( subjectString != null ) {
                subjectString = subjectString.trim();
            }
            if( objectString != null ) {
                objectString = objectString.trim();
            }
            if( "".equals( subjectString )) {
                subjectString = null; // empty field should not mean home object here
            }
            if( "".equals( objectString )) {
                objectString = null; // empty field should not mean home object here
            }

            InfoGridWebApp app        = InfoGridWebApp.getSingleton();
            Context        appContext = app.getApplicationContext();
            
            @SuppressWarnings( "unchecked" )
            NameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer = appContext.findContextObjectOrThrow( NameServer.class );
            RestfulJeeFormatter                     formatter          = appContext.findContextObjectOrThrow( RestfulJeeFormatter.class );
            
            MeshBaseIdentifier                      meshBaseName       = null;

            if( meshBaseString != null ) {
                try {
                    MeshBaseIdentifierFactory idFact = app.getApplicationContext().findContextObjectOrThrow( MeshBaseIdentifierFactory.class );
                    meshBaseName                     = idFact.fromExternalForm( meshBaseString );

                } catch( URISyntaxException ex ) {
                    throw new HttpShellException( ex );
                }
            }

            MeshBase meshBase = null;
            if( meshBaseName != null ) {
                meshBase = meshBaseNameServer.get( meshBaseName );
            } else {
                meshBase = app.getApplicationContext().findContextObjectOrThrow( MeshBase.class );
            }

            if( meshBase == null ) {
                throw new HttpShellException( new RuntimeException( "Meshbase cannot be found" ));
            }

            MeshObjectIdentifier subjectName;
            MeshObjectIdentifier objectName;
            try {
                subjectName = formatter.fromMeshObjectIdentifier( meshBase.getMeshObjectIdentifierFactory(), ModelPrimitivesStringRepresentation.TEXT_PLAIN, subjectString );
                objectName  = formatter.fromMeshObjectIdentifier( meshBase.getMeshObjectIdentifierFactory(), ModelPrimitivesStringRepresentation.TEXT_PLAIN, objectString );

            } catch( URISyntaxException ex ) {
                throw new HttpShellException( ex );
            }

            MeshObject subject;
            try {
                subject = ( subjectName != null ) ? meshBase.accessLocally( subjectName ) : null;

            } catch( MeshObjectAccessException ex ) {
                throw new HttpShellException( ex );
            }
            MeshObject object  = ( objectName  != null ) ? meshBase.findMeshObjectByIdentifier( objectName  ) : null;

            Transaction tx = null;
            try {
                tx = meshBase.createTransactionAsapIfNeeded();

                performVerbWithinTransaction( lidRequest, subjectName, subject, objectName, object, meshBase );

            } catch( EssentialArgumentMissingException ex ) {
                throw new HttpShellException( ex );

            } catch( IsAbstractException ex ) {
                throw new HttpShellException( ex );

            } catch( EntityBlessedAlreadyException ex ) {
                throw new HttpShellException( ex );

            } catch( IllegalPropertyTypeException ex ) {
                throw new HttpShellException( ex );

            } catch( RoleTypeBlessedAlreadyException ex ) {
                throw new HttpShellException( ex );

            } catch( MeshObjectIdentifierNotUniqueException ex ) {
                throw new HttpShellException( ex );

            } catch( InconsistentArgumentsException ex ) {
                throw new HttpShellException( ex );

            } catch( EntityNotBlessedException ex ) {
                throw new HttpShellException( ex );

            } catch( RelatedAlreadyException ex ) {
                throw new HttpShellException( ex );

//            } catch( RoleTypeRequiresEntityTypeException ex ) {
//                throw new HttpShellException( ex );
//
//            } catch( RoleTypeNotBlessedException ex ) {
//                throw new HttpShellException( ex );
//
            } catch( NotRelatedException ex ) {
                throw new HttpShellException( ex );

            } catch( MeshTypeNotFoundException ex ) {
                throw new HttpShellException( ex );

            } catch( MeshObjectAccessException ex ) {
                throw new HttpShellException( ex );

            } catch( PropertyValueParsingException ex ) {
                throw new HttpShellException( ex );

            } catch( TransactionException ex ) {
                log.error( ex );

            } catch( URISyntaxException ex ) {
                throw new HttpShellException( ex );

            } finally {
                if( tx != null ) {
                    tx.commitTransaction();
                }
            }
        }

        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws IdentifierNotUniqueException thrown if the Identifier of a to-be-created MeshObject exists already
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EssentialArgumentMissingException,
                MeshObjectIdentifierNotUniqueException,
                InconsistentArgumentsException,
                IllegalPropertyTypeException,
                MeshObjectAccessException,
                MeshTypeNotFoundException,
                NotPermittedException,
                RelatedAlreadyException,
                PropertyValueParsingException,
                RoleTypeBlessedAlreadyException,
                EntityNotBlessedException,
                NotRelatedException,
                TransactionException,
                URISyntaxException
        {
            // That's all that's left here
            
            potentiallyBlessEntityTypes( lidRequest, subject, mb, false );
            potentiallySetProperties(    lidRequest, subject, mb, false );
            potentiallyRelateAndBless(   lidRequest, subject, object, mb, false );
        }
    },
    
    /**
     * <p>Delete a <code>MeshObject</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be deleted.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    DELETE( "delete" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                MeshObjectsNotFoundException,
                NotPermittedException,
                TransactionException
        {
            if( subject == null ) { // this has the side effect of preventing that the home object is being deleted
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }
            mb.getMeshBaseLifecycleManager().deleteMeshObject( subject );
        }
    },
    
    /**
     * <p>Bless a <code>MeshObject</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be blessed.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.object</code></td>
     *    <td>If given, identifier (external form of the <code>MeshObject</code>'s Identifier)
     *        for another object to relate to after the blessing of the subject</td>
     *    <td>0 or 1</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertytype</code></td>
     *    <td>Identifier for a <code>PropertyType</code> (external form of the <code>PropertyType</code>'s
     *        Identifier) to set once the subject has been blessed</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertyvalue</code></td>
     *    <td>New <code>PropertyValue</code> for the property on the subject (external form of
     *        the <code>PropertyValue</code>) identified by
     *        <code>mesh.subject</code> and <code>mesh.propertytype</code></td>
     *    <td>0..N. Must be given the same number of times as <code>mesh.propertytype</code>.</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.subjecttype</code></td>
     *    <td>Identifier (external form of the <code>EntityType</code>'s Identifier)
     *        for a type of the subject is to be blessed with</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.roletype</code></td>
     *    <td>Identifier for a type of role (external form of the <code>RoleType</code>'s Identifier)
     *        the subject plays with the object</td>
     *    <td>0..N. Only permitted if <code>mesh.object</code> is given</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    BLESS( "bless" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                EssentialArgumentMissingException,
                InconsistentArgumentsException,
                MeshObjectsNotFoundException,
                MeshTypeNotFoundException,
                IllegalPropertyTypeException,
                RelatedAlreadyException,
                NotPermittedException,
                PropertyValueParsingException,
                RoleTypeBlessedAlreadyException,
                EntityNotBlessedException,
                NotRelatedException,
                TransactionException
        {
            if( subject == null ) {
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }
            potentiallyBlessEntityTypes( lidRequest, subject, mb, true );
            potentiallySetProperties(    lidRequest, subject, mb, false );
            potentiallyRelateAndBless(   lidRequest, subject, object, mb, false );
        }
    },
    
    /**
     * <p>Unbless a <code>MeshObject</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be unblessed.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.subjecttype</code></td>
     *    <td>Identifier (external form of the <code>EntityType</code>'s Identifier)
     *        for a type of the subject is to be unblessed from</td>
     *    <td>0..N</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    UNBLESS( "unbless" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                EntityNotBlessedException,
                MeshObjectsNotFoundException,
                MeshTypeNotFoundException,
                RoleTypeRequiresEntityTypeException,
                NotPermittedException,
                TransactionException
        {
            if( subject == null ) {
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }
            EntityType [] entityTypes = determineEntityTypes( lidRequest, mb );
            if( entityTypes != null ) {
                subject.unbless( entityTypes );
            }            
        }
    },
    
    /**
     * <p>Relate two <code>MeshObjects</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be related
     *        (the source). A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.object</code></td>
     *    <td>External form of the other <code>MeshObject</code>'s Identifier that is to be related
     *        (the destination). A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>0 or 1</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertytype</code></td>
     *    <td>Identifier for a <code>PropertyType</code> (external form of the <code>PropertyType</code>'s
     *        Identifier) to set once the subject has been related</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertyvalue</code></td>
     *    <td>New <code>PropertyValue</code> for the property on the subject (external form of
     *        the <code>PropertyValue</code>) identified by
     *        <code>mesh.subject</code> and <code>mesh.propertytype</code></td>
     *    <td>0..N. Must be given the same number of times as <code>mesh.propertytype</code>.</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.subjecttype</code></td>
     *    <td>Identifier (external form of the <code>EntityType</code>'s Identifier)
     *        for a type of the subject is to be blessed with before relating to the object</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.roletype</code></td>
     *    <td>Identifier for a type of role (external form of the <code>RoleType</code>'s Identifier)
     *        the subject plays with the object</td>
     *    <td>0..N.</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    RELATE( "relate" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                IsAbstractException,
                EntityBlessedAlreadyException,
                IllegalPropertyTypeException,
                EssentialArgumentMissingException,
                InconsistentArgumentsException,
                MeshObjectsNotFoundException,
                MeshTypeNotFoundException,
                RelatedAlreadyException,
                NotPermittedException,
                PropertyValueParsingException,
                RoleTypeBlessedAlreadyException,
                EntityNotBlessedException,
                NotRelatedException,
                TransactionException
        {
            if( subject == null ) {
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }
            if( object == null ) {
                throw new MeshObjectsNotFoundException( mb, objectIdentifier );
            }
            potentiallyBlessEntityTypes( lidRequest, subject, mb, false );
            potentiallyRelateAndBless( lidRequest, subject, object, mb, true );
            potentiallySetProperties( lidRequest, subject, mb, false );
        }
    },
    
    /**
     * <p>Unrelate two <code>MeshObjects</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be unrelated.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.object</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be unrelated.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    UNRELATE( "unrelate" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                EssentialArgumentMissingException,
                InconsistentArgumentsException,
                MeshObjectsNotFoundException,
                NotRelatedException,
                MeshTypeNotFoundException,
                NotPermittedException,
                IllegalPropertyTypeException,
                PropertyValueParsingException,
                TransactionException
        {
            if( subject == null ) {
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }
            if( object == null ) {
                throw new MeshObjectsNotFoundException( mb, objectIdentifier );
            }
            subject.unrelate( object );
            potentiallySetProperties( lidRequest, subject, mb, false );
        }
    },
    
    /**
     * <p>Bless the relationship between two <code>MeshObjects</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be related.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.object</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier that is to be related.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertytype</code></td>
     *    <td>Identifier for a <code>PropertyType</code> (external form of the <code>PropertyType</code>'s
     *        Identifier) to set once the subject and the object have been related</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertyvalue</code></td>
     *    <td>New <code>PropertyValue</code> for the property on the subject (external form of
     *        the <code>PropertyValue</code>) identified by
     *        <code>mesh.subject</code> and <code>mesh.propertytype</code></td>
     *    <td>0..N. Must be given the same number of times as <code>mesh.propertytype</code>.</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.subjecttype</code></td>
     *    <td>Identifier (external form of the <code>EntityType</code>'s Identifier)
     *        for a type of the subject is to be blessed with after creation</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.roletype</code></td>
     *    <td>Identifier for a type of role (external form of the <code>RoleType</code>'s Identifier)
     *        the subject plays with the object</td>
     *    <td>0..N. Only permitted if <code>mesh.object</code> is given</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    BLESS_ROLE( "blessRole" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                IsAbstractException,
                EssentialArgumentMissingException,
                InconsistentArgumentsException,
                MeshTypeNotFoundException,
                MeshObjectsNotFoundException,
                NotPermittedException,
                RelatedAlreadyException,
                IllegalPropertyTypeException,
                PropertyValueParsingException,
                RoleTypeBlessedAlreadyException,
                EntityNotBlessedException,
                NotRelatedException,
                TransactionException
        {
            if( subject == null ) {
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }
            if( object == null ) {
                throw new MeshObjectsNotFoundException( mb, objectIdentifier );
            }

            potentiallyRelateAndBless( lidRequest, subject, object, mb, true );
            potentiallySetProperties( lidRequest, subject, mb, false );
       }
    },

    /**
     * <p>Unbless the relationship between two <code>MeshObjects</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier whose relationship with the
     *        object is to be unblessed. A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.object</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier whose relationship with the
     *        subject is to be unblessed. A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.roletype</code></td>
     *    <td>Identifier for a type of role (external form of the <code>RoleType</code>'s Identifier)
     *        the subject plays with the object and that shall be unblessed</td>
     *    <td>0..N. Only permitted if <code>mesh.object</code> is given</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    UNBLESS_ROLE( "unblessRole" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                EssentialArgumentMissingException,
                InconsistentArgumentsException,
                MeshObjectsNotFoundException,
                MeshTypeNotFoundException,
                RoleTypeNotBlessedException,
                NotPermittedException,
                IllegalPropertyTypeException,
                NotRelatedException,
                PropertyValueParsingException,
                TransactionException
        {
            if( subject == null ) {
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }
            if( object == null ) {
                throw new MeshObjectsNotFoundException( mb, objectIdentifier );
            }

            RoleType [] roleTypes = determineRoleTypes( lidRequest, mb );

            subject.unblessRelationship( roleTypes, object ); // may throw 
            potentiallySetProperties( lidRequest, subject, mb, false );
        }
    },

    /**
     * <p>Set a property of a <code>MeshObject</code>.</p>
     * <p>The following HTTP POST parameters are recognized with this verb:</p>
     * <table class="infogrid-border">
     *  <thead>
     *   <tr>
     *    <td width="15%">Parameter</td>
     *    <td width="60%">Description</td>
     *    <td>Required?</td>
     *   </tr>
     *  </thead>
     *  <tbody>
     *   <tr>
     *    <td><code>mesh.subject</code></td>
     *    <td>External form of the <code>MeshObject</code>'s Identifier whose property/ies are to be changed.
     *        A <code>MeshObject</code> with this Identifier must exist.</td>
     *    <td>exactly 1 required</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertytype</code></td>
     *    <td>Identifier for a <code>PropertyType</code> (external form of the <code>PropertyType</code>'s
     *        Identifier) to set</td>
     *    <td>0..N</td>
     *   </tr>
     *   <tr>
     *    <td><code>mesh.propertyvalue</code></td>
     *    <td>New <code>PropertyValue</code> for the property on the subject (external form of
     *        the <code>PropertyValue</code>) identified by
     *        <code>mesh.subject</code> and <code>mesh.propertytype</code></td>
     *    <td>0..N. Must be given the same number of times as <code>mesh.propertytype</code>.</td>
     *   </tr>
     *  </tbody>
     * </table>
     */
    SET_PROPERTY( "setProperty" ) {
        /**
         * Perform the operation, assuming a Transaction is in place.
         *
         * @param lidRequest the incoming request
         * @param subjectIdentifier the Identifier of the specified subject MeshObject
         * @param subject the subject MeshObject
         * @param objectIdentifier the Identifier of the specified object MeshObject
         * @param object the object MeshObject
         * @param mb the MeshBase on which to perform the operation
         * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
         * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
         * @throws MeshObjectsNotFoundException thrown if one or more MeshObjects essential to the operation cannot be found
         * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
         * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
         * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
         * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
         */
        @Override
        protected void performVerbWithinTransaction(
                SaneServletRequest   lidRequest,
                MeshObjectIdentifier subjectIdentifier,
                MeshObject           subject,
                MeshObjectIdentifier objectIdentifier,
                MeshObject           object,
                MeshBase             mb )
            throws
                EssentialArgumentMissingException,
                InconsistentArgumentsException,
                MeshObjectsNotFoundException,
                MeshTypeNotFoundException,
                IllegalPropertyTypeException,
                NotPermittedException,
                PropertyValueParsingException,
                TransactionException
    {
            if( subject == null ) {
                throw new MeshObjectsNotFoundException( mb, subjectIdentifier );
            }

            potentiallySetProperties( lidRequest, subject, mb, true );
        }
    };

    private static final Log log = Log.getLogInstance( HttpShellVerb.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param tag the tag
     */
    private HttpShellVerb(
            String tag )
    {
        theTag = tag;
    }

    /**
     * Perform the operation that goes with this verb.
     * 
     * @param lidRequest the incoming request
     * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
     * @throws HttpShellException thrown if the operation could not be performed
     */
    public void performVerb(
            SaneServletRequest lidRequest )
        throws
            NotPermittedException,
            HttpShellException
    {
        String meshBaseString = lidRequest.getPostArgument( HttpShellFilter.MESH_BASE_TAG );
        String subjectString  = lidRequest.getPostArgument( HttpShellFilter.SUBJECT_TAG );
        String objectString   = lidRequest.getPostArgument( HttpShellFilter.OBJECT_TAG );

        if( meshBaseString != null ) {
            meshBaseString = meshBaseString.trim();
        }
        if( subjectString != null ) {
            subjectString = subjectString.trim();
        }
        if( objectString != null ) {
            objectString = objectString.trim();
        }
        if( "".equals( subjectString )) {
            subjectString = null; // empty field should not mean home object here
        }
        if( "".equals( objectString )) {
            objectString = null; // empty field should not mean home object here
        }

        InfoGridWebApp app        = InfoGridWebApp.getSingleton();
        Context        appContext = app.getApplicationContext();
        
        @SuppressWarnings( "unchecked" )
        NameServer<MeshBaseIdentifier,MeshBase> meshBaseNameServer = appContext.findContextObjectOrThrow( NameServer.class );
        MeshBaseIdentifier                      meshBaseName       = null;
        RestfulJeeFormatter                     formatter          = appContext.findContextObjectOrThrow( RestfulJeeFormatter.class );

        if( meshBaseString != null ) {
            try {
                MeshBaseIdentifierFactory idFact = app.getApplicationContext().findContextObjectOrThrow( MeshBaseIdentifierFactory.class );
                meshBaseName                     = idFact.fromExternalForm( meshBaseString );

            } catch( URISyntaxException ex ) {
                throw new HttpShellException( ex );
            }
        }

        MeshBase meshBase = null;
        if( meshBaseName != null ) {
            meshBase = meshBaseNameServer.get( meshBaseName );
        } else {
            meshBase = app.getApplicationContext().findContextObjectOrThrow( MeshBase.class );
        }

        if( meshBase == null ) {
            throw new HttpShellException( new RuntimeException( "Meshbase cannot be found" ));
        }
        
        MeshObjectIdentifier subjectName;
        MeshObjectIdentifier objectName;
        try {
            subjectName = formatter.fromMeshObjectIdentifier( meshBase.getMeshObjectIdentifierFactory(), ModelPrimitivesStringRepresentation.TEXT_PLAIN, subjectString );
            objectName  = formatter.fromMeshObjectIdentifier( meshBase.getMeshObjectIdentifierFactory(), ModelPrimitivesStringRepresentation.TEXT_PLAIN, objectString );

        } catch( URISyntaxException ex ) {
            throw new HttpShellException( ex );
        }
        
        MeshObject subject = ( subjectName != null ) ? meshBase.findMeshObjectByIdentifier( subjectName ) : null;
        MeshObject object  = ( objectName  != null ) ? meshBase.findMeshObjectByIdentifier( objectName  ) : null;

        Transaction tx = null;
        try {
            tx = meshBase.createTransactionAsapIfNeeded();

            performVerbWithinTransaction( lidRequest, subjectName, subject, objectName, object, meshBase );

        } catch( EssentialArgumentMissingException ex ) {
            throw new HttpShellException( ex );

        } catch( IsAbstractException ex ) {
            throw new HttpShellException( ex );

        } catch( EntityBlessedAlreadyException ex ) {
            throw new HttpShellException( ex );

        } catch( IllegalPropertyTypeException ex ) {
            throw new HttpShellException( ex );

        } catch( RoleTypeBlessedAlreadyException ex ) {
            throw new HttpShellException( ex );

        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            throw new HttpShellException( ex );

        } catch( InconsistentArgumentsException ex ) {
            throw new HttpShellException( ex );

        } catch( EntityNotBlessedException ex ) {
            throw new HttpShellException( ex );

        } catch( RelatedAlreadyException ex ) {
            throw new HttpShellException( ex );

        } catch( RoleTypeRequiresEntityTypeException ex ) {
            throw new HttpShellException( ex );

        } catch( RoleTypeNotBlessedException ex ) {
            throw new HttpShellException( ex );

        } catch( NotRelatedException ex ) {
            throw new HttpShellException( ex );

        } catch( MeshTypeNotFoundException ex ) {
            throw new HttpShellException( ex );

        } catch( MeshObjectAccessException ex ) {
            throw new HttpShellException( ex );

        } catch( PropertyValueParsingException ex ) {
            throw new HttpShellException( ex );

        } catch( TransactionException ex ) {
            log.error( ex );

        } catch( URISyntaxException ex ) {
            throw new HttpShellException( ex );

        } finally {
            if( tx != null ) {
                tx.commitTransaction();
            }
        }
    }

    /**
     * Perform the operation, assuming a Transaction is in place.
     * 
     * @param lidRequest the incoming request
     * @param subjectIdentifier the Identifier of the specified subject MeshObject
     * @param subject the subject MeshObject
     * @param objectIdentifier the Identifier of the specified object MeshObject
     * @param object the object MeshObject
     * @param mb the MeshBase on which to perform the operation
     * @throws IsAbstractException thrown if a to-be-instantiated MeshType was abstract
     * @throws EntityNotBlessedException thrown if an operation required a MeshObject to be blessed but it was not
     * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
     * @throws MeshObjectIdentifierNotUniqueException thrown if the Identifier of a to-be-created MeshObject exists already
     * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
     * @throws IllegalPropertyTypeException thrown if a specified PropertyType was illegal for a MeshObject
     * @throws MeshObjectAccessException thrown if a specified MeshObject could not be accessed
     * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
     * @throws RoleTypeRequiresEntityTypeException thrown if a RoleType required an EntityType to be present but it was not
     * @throws RoleTypeNotBlessedException thrown if unblessing of a relationship was attempted but the relationship was not blessed with the RoleType
     * @throws NotRelatedException thrown if two MeshObjects needed to be related but they were not
     * @throws EntityBlessedAlreadyException thrown if a MeshObject is already blessed with a given EntityType
     * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
     * @throws RelatedAlreadyException thrown if two MeshObjects were related already
     * @throws RoleTypeBlessedAlreadyException thrown if a relationship is already blessed with a given RoleType
     * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     * @throws URISyntaxException thrown if a URI syntax error occurred
     */
    protected abstract void performVerbWithinTransaction(
            SaneServletRequest   lidRequest,
            MeshObjectIdentifier subjectIdentifier,
            MeshObject           subject,
            MeshObjectIdentifier objectIdentifier,
            MeshObject           object,
            MeshBase             mb )
        throws
            IsAbstractException,
            EntityNotBlessedException,
            EssentialArgumentMissingException,
            MeshObjectIdentifierNotUniqueException,
            InconsistentArgumentsException,
            IllegalPropertyTypeException,
            MeshObjectAccessException,
            MeshTypeNotFoundException,
            RoleTypeRequiresEntityTypeException,
            RoleTypeNotBlessedException,
            NotRelatedException,
            EntityBlessedAlreadyException,
            NotPermittedException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            PropertyValueParsingException,
            TransactionException,
            URISyntaxException;

    /**
     * Find the applicable HttpShellVerb for this request.
     * 
     * @param lidRequest the incoming request
     * @return the found MeHttpShellVerbor null if none
     */
    public static HttpShellVerb findApplicableVerb(
            SaneServletRequest lidRequest )
    {
        String verbValue = lidRequest.getPostArgument( HttpShellFilter.VERB_TAG );
        if( verbValue != null ) {
            verbValue = verbValue.trim();
        }
        if( verbValue == null || verbValue.length() == 0 ) {
            return null;
        }
        for( HttpShellVerb v : values() ) {
            if( v.theTag.equalsIgnoreCase( verbValue )) {
                return v;
            }
        }
        return null;
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
        String inst;
        if( formatter != null ) {
            inst = formatter.asLocalizedString( this );
        } else {
            inst = toString();
        }

        String ret = theResourceHelper.getResourceStringWithArguments( MESSAGE_PARAMETER + "-" + toString(), inst );
        return ret;
    }

    /**
     * Bless the subject with one or more <code>EntityTypes</code>, if this request has
     * this side effect.
     *
     * @param lidRequest the incoming request
     * @param subject the subject MeshObject
     * @param mb the MeshBase on which to perform the operation
     * @param throwExceptions if true, throw Exceptions. If false, silently ignore all errors
     * @throws IsAbstractException thrown if a to-be-instantiated MeshType was abstract
     * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
     * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
     * @throws EntityBlessedAlreadyException thrown if a MeshObject is already blessed with a given EntityType
     * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     */
    protected void potentiallyBlessEntityTypes(
            SaneServletRequest lidRequest,
            MeshObject         subject,
            MeshBase           mb,
            boolean            throwExceptions )
        throws
            IsAbstractException,
            NotPermittedException,
            MeshTypeNotFoundException,
            EntityBlessedAlreadyException,
            EssentialArgumentMissingException,
            TransactionException
    {
        EntityType [] entityTypes = determineEntityTypes( lidRequest, mb );
        if( throwExceptions ) {
            if( entityTypes == null || entityTypes.length == 0 ) {
                throw new EssentialArgumentMissingException( this, HttpShellFilter.SUBJECT_TYPE_TAG );
            }
            subject.bless( entityTypes );
        } else if( entityTypes != null && entityTypes.length > 0 ) {
            try {
                subject.bless( entityTypes );
            } catch( Throwable t ) {
                log.info( t );
            }
        }
    }

    /**
     * Set one or more properties of the subject, if this request has
     * this side effect.
     *
     * @param lidRequest the incoming request
     * @param subject the subject MeshObject
     * @param mb the MeshBase on which to perform the operation
     * @param throwExceptions if true, throw Exceptions. If false, silently ignore all errors
     * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
     * @throws InconsistentArgumentsException thrown if provided arguments are inconsistent with each other
     * @throws IllegalPropertyTypeException thrown if a specified PropertyType was illegal for a MeshObject
     * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
     * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
     * @throws PropertyValueParsingException thrown if a PropertyValue was Improperly encoded
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     */
    protected void potentiallySetProperties(
            SaneServletRequest lidRequest,
            MeshObject         subject,
            MeshBase           mb,
            boolean            throwExceptions )
        throws
            NotPermittedException,
            MeshTypeNotFoundException,
            EssentialArgumentMissingException,
            InconsistentArgumentsException,
            IllegalPropertyTypeException,
            PropertyValueParsingException,
            TransactionException
    {
        String [] propertyTypesStrings  = lidRequest.getMultivaluedPostArgument( HttpShellFilter.PROPERTY_TYPE_TAG );
        String [] propertyValuesStrings = lidRequest.getMultivaluedPostArgument( HttpShellFilter.PROPERTY_VALUE_TAG );

        if(    propertyTypesStrings == null  || propertyTypesStrings.length == 0
            || propertyValuesStrings == null || propertyValuesStrings.length == 0 )
        {
            if( throwExceptions ) {
                throw new EssentialArgumentMissingException( this, HttpShellFilter.PROPERTY_TYPE_TAG );
            } else {
                return; // do nothing
            }
        }
        if( propertyTypesStrings.length != propertyValuesStrings.length ) {
            if( throwExceptions ) {
                throw new InconsistentArgumentsException( this, HttpShellFilter.PROPERTY_TYPE_TAG, HttpShellFilter.PROPERTY_VALUE_TAG );
            } else {
                return; // do nothing
            }
        }

        ModelBase modelBase = mb.getModelBase();
        
        for( int i=0 ; i<propertyTypesStrings.length ; ++i ) {
            propertyTypesStrings[i]  = propertyTypesStrings[i].trim();
            propertyValuesStrings[i] = propertyValuesStrings[i].trim();
            
            MeshTypeIdentifier propertyTypeName = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( propertyTypesStrings[i] );
        
            PropertyType propertyType = modelBase.findPropertyTypeByIdentifier( propertyTypeName );

            if( throwExceptions ) {
                PropertyValue value;
                if( propertyValuesStrings[i].length() == 0 ) {
                    value = null;
                } else {
                    value = propertyType.fromStringRepresentation( ModelPrimitivesStringRepresentation.TEXT_PLAIN, propertyValuesStrings[i] );
                }

                try {
                    subject.setPropertyValue( propertyType, value );
                } catch( IllegalPropertyValueException ex ) {
                    log.error( ex );
                }

            } else {
                try {
                    PropertyValue value = propertyType.fromStringRepresentation( ModelPrimitivesStringRepresentation.TEXT_PLAIN, propertyValuesStrings[i] );

                    subject.setPropertyValue( propertyType, value );

                } catch( IllegalPropertyValueException ex ) {
                    log.error( ex );
                } catch( Throwable t ) {
                    log.info( t );
                }
            }
        }
    }

    /**
     * Relate two MeshObjects and bless the relationship, if this request has
     * this side effect.
     *
     * @param lidRequest the incoming request
     * @param subject the subject MeshObject
     * @param object the object MeshObject
     * @param mb the MeshBase on which to perform the operation
     * @param throwExceptions if true, throw Exceptions. If false, silently ignore all errors
     * @throws IsAbstractException thrown if a to-be-instantiated MeshType was abstract
     * @throws EntityNotBlessedException thrown if an operation required a MeshObject to be blessed but it was not
     * @throws EssentialArgumentMissingException thrown if an argument was missing that is essential for the operation
     * @throws MeshTypeNotFoundException thrown if a MeshType essential to the operation cannot be found
     * @throws NotRelatedException thrown if two MeshObjects needed to be related but they were not
     * @throws NotPermittedException thrown if the caller did not have sufficient privileges for the operation
     * @throws RelatedAlreadyException thrown if two MeshObjects were related already
     * @throws RoleTypeBlessedAlreadyException thrown if a relationship is already blessed with a given RoleType
     * @throws TransactionException thrown if invoked outside of proper Transaction boundaries
     */
    protected void potentiallyRelateAndBless(
            SaneServletRequest lidRequest,
            MeshObject         subject,
            MeshObject         object,
            MeshBase           mb,
            boolean            throwExceptions )
        throws
            IsAbstractException,
            NotPermittedException,
            MeshTypeNotFoundException,
            EssentialArgumentMissingException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            EntityNotBlessedException,
            NotRelatedException,
            TransactionException
    {
        if( throwExceptions ) {
            if( subject == null ) {
                throw new EssentialArgumentMissingException( this, HttpShellFilter.SUBJECT_TAG );
            }
            if( object == null ) {
                throw new EssentialArgumentMissingException( this, HttpShellFilter.OBJECT_TAG );
            }
            if( !subject.isRelated( object )) {
                subject.relate( object );
            }
            RoleType [] roleTypes = determineRoleTypes( lidRequest, mb );
            if( roleTypes != null && roleTypes.length > 0 ) {
                subject.blessRelationship( roleTypes, object );
            }
            
        } else {
            if( subject == null || object == null ) {
                return;
            }
            try {
                if( !subject.isRelated( object )) {
                    subject.relate( object );
                }
                RoleType [] roleTypes = determineRoleTypes( lidRequest, mb );
                if( roleTypes != null && roleTypes.length > 0 ) {
                    subject.blessRelationship( roleTypes, object );
                }
            } catch( Throwable t ) {
                log.info( t );
            }
        }
    }

    /**
     * Helper method to find the specified <code>EntityTypes</code>.
     *
     * @param lidRequest the incoming request
     * @param mb the MeshBase to use
     * @return the found <code>EntityTypes</code>
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a MeshType could not be found
     */
    protected static EntityType [] determineEntityTypes(
            SaneServletRequest lidRequest,
            MeshBase           mb )
        throws
            MeshTypeWithIdentifierNotFoundException
    {
        ModelBase modelBase = mb.getModelBase();
        
        EntityType [] ret;

        String [] entityTypesStrings = lidRequest.getMultivaluedPostArgument( HttpShellFilter.SUBJECT_TYPE_TAG );
        if( entityTypesStrings != null && entityTypesStrings.length > 0 ) {
            ret = new EntityType[ entityTypesStrings.length ];

            int count = 0;
            for( int i=0 ; i<entityTypesStrings.length ; ++i ) {

                MeshTypeIdentifier typeName;
                if( entityTypesStrings[i] == null ) {
                    continue;
                }
                entityTypesStrings[i] = entityTypesStrings[i].trim();
                if( entityTypesStrings[i].length() == 0 ) {
                    typeName = null;
                } else {
                    typeName = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( entityTypesStrings[i] );
                }

                if( typeName != null ) {
                    ret[count] = modelBase.findEntityTypeByIdentifier( typeName );
                    ++count;
                }
            }

            if( count < ret.length ) {
                ret = ArrayHelper.copyIntoNewArray( ret, 0, count, EntityType.class );
            }

        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Helper method to find the specified <code>RoleTypes</code>.
     *
     * @param lidRequest the incoming request
     * @param mb the MeshBase to use
     * @return the found <code>RoleTypes</code>
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a MeshType could not be found
     */
    protected static RoleType [] determineRoleTypes(
            SaneServletRequest lidRequest,
            MeshBase           mb )
        throws
            MeshTypeWithIdentifierNotFoundException
    {
        ModelBase modelBase = mb.getModelBase();

        RoleType [] ret;

        String [] roleTypesStrings = lidRequest.getMultivaluedPostArgument( HttpShellFilter.ROLE_TYPE_TAG );

        if( roleTypesStrings != null && roleTypesStrings.length > 0 ) {
            ret = new RoleType[ roleTypesStrings.length ];

            int count = 0;
            for( int i=0 ; i<roleTypesStrings.length ; ++i ) {

                MeshTypeIdentifier typeName;
                if( roleTypesStrings[i] == null ) {
                    continue;
                }
                roleTypesStrings[i] = roleTypesStrings[i].trim();
                if( roleTypesStrings[i].length() == 0 ) {
                    typeName = null;
                } else {
                    typeName = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( roleTypesStrings[i] );
                }

                if( typeName != null ) {
                    ret[count] = modelBase.findRoleTypeByIdentifier( typeName );
                    ++count;
                }
            }

            if( count < ret.length ) {
                ret = ArrayHelper.copyIntoNewArray( ret, 0, count, RoleType.class );
            }

        } else {
            ret = null;
        }
        return ret;
    }

    /**
     * Name of the tag in the protocol.
     */
    protected String theTag;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( HttpShellVerb.class );

}
