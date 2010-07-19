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

package org.infogrid.jee.shell.http;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.util.http.MimePart;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.SafeUnsafePostFilter;
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
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.MeshObjectAccessException;
import org.infogrid.meshbase.MeshObjectIdentifierFactory;
import org.infogrid.meshbase.transaction.OnDemandTransaction;
import org.infogrid.meshbase.transaction.OnDemandTransactionFactory;
import org.infogrid.meshbase.transaction.Transaction;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.BlobDataType;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.PropertyValueParsingException;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.FactoryException;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.MSmartFactory;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.ContextObjectNotFoundException;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Recognizes <code>MeshObject</code> change-related requests as part of the incoming HTTP
 *    request and processes them. The protocol to express those change-related requests has been
 *    constructed to make it easy to issue them from HTML forms using HTTP POST.</p>
 */
public class HttpShellFilter
    implements
        Filter,
        HttpShellKeywords
{
    private static Log log; // initialized only after the InitializationFilter has run.

    /**
     * Constructor.
     */
    public HttpShellFilter()
    {
    }

    /**
     * Main filter operation.
     *
     * @param request The servlet request to process
     * @param response The servlet response to assemble
     * @param chain The filter chain this Filter is part of
     *
     * @throws IOException if an input/output error occurs
     * @throws ServletException if a servlet error occurs
     */
    public void doFilter(
            ServletRequest  request,
            ServletResponse response,
            FilterChain     chain )
        throws
            IOException,
            ServletException
    {
        HttpServletRequest  realRequest  = (HttpServletRequest)  request;
        HttpServletResponse realResponse = (HttpServletResponse) response;
        SaneRequest         lidRequest   = SaneServletRequest.create( realRequest );
        String              redirectUrl  = null;

        try {
            if( "POST".equals( lidRequest.getMethod() )) {
                if(    SafeUnsafePostFilter.isSafePost( lidRequest )
                    || SafeUnsafePostFilter.mayBeSafeOrUnsafePost( lidRequest ))
                {
                    String command = lidRequest.getPostedArgument( FULL_SUBMIT_TAG );
                    if( command == null || command.equals( SUBMIT_COMMIT_VALUE )) {
                        redirectUrl = performFactoryOperations( lidRequest );
                    }

                } else {
                    getLog().warn( "Ignoring unsafe POST " + lidRequest );
                }
            }
        
        } catch( Throwable ex ) {
            getLog().warn( ex );
            
            @SuppressWarnings( "unchecked" )
            List<Throwable> problems = (List<Throwable>) request.getAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME );
            if( problems == null ) {
                problems = new ArrayList<Throwable>();
                request.setAttribute( InfoGridWebApp.PROCESSING_PROBLEM_EXCEPTION_NAME, problems );
            }
            synchronized( problems ) {
                problems.add( ex );
            }
            
        }
        if( redirectUrl != null ) {
            realResponse.sendRedirect( redirectUrl );
        } else {
            chain.doFilter( realRequest, realResponse );
        }
    }
    
    /**
     * Perform all factory methods contained in the request.
     * 
     * @param lidRequest the incoming request
     * @return URL to redirect to, if any
     * @throws NotPermittedException thrown if the caller had insufficient privileges to perform this operation
     * @throws HttpShellException a factory Exception occurred
     */
    protected String performFactoryOperations(
            SaneRequest lidRequest )
        throws
            NotPermittedException,
            HttpShellException
    {
        MSmartFactory<MeshBase,OnDemandTransaction,Void> txs = MSmartFactory.create(
                new OnDemandTransactionFactory(),
                MCachingHashMap.<MeshBase,OnDemandTransaction>create() );

        Map<String,String[]>       postArguments = lidRequest.getPostedArguments();
        HashMap<String,MeshObject> variables     = new HashMap<String,MeshObject>();

        try {
            // first look for all arguments of the form <PREFIX>.<VARIABLE>
            for( String arg : postArguments.keySet() ) {
                if( !arg.startsWith( PREFIX )) {
                    continue; // skip all that aren't for us
                }
                String coreArg = arg.substring( PREFIX.length() );
                if( coreArg.equals( SUBMIT_TAG )) {
                    continue; // skip submit tag
                }
                if( coreArg.indexOf( SEPARATOR ) >= 0 ) {
                    continue; // skip all that aren't referring to the MeshObjects
                }
                String varName  = coreArg;
                String varValue = lidRequest.getPostedArgument( arg ); // use SaneRequest's error handling for multiple values

                MeshBase            base       = findMeshBaseFor( varName, lidRequest );
                HttpShellAccessVerb accessVerb = HttpShellAccessVerb.findAccessFor( varName, lidRequest );

                MeshObjectIdentifier id = parseMeshObjectIdentifier( base.getMeshObjectIdentifierFactory(), varValue );
                OnDemandTransaction  tx = txs.obtainFor( base );

                MeshObject accessed = accessVerb.access( id, base, tx, lidRequest );
                if( accessed != null ) {
                    variables.put( varName, accessed );

                    // first bless then unbless, then properties
                    potentiallyBless(         varName, accessed, tx, lidRequest );
                    potentiallyUnbless(       varName, accessed, tx, lidRequest );
                    potentiallySetProperties( varName, accessed, tx, lidRequest );
                }
            }

            // then look for all arguments of the form <PREFIX>.<VARIABLE>.<ACCESS_TAG> for which
            // there is no corresponding <PREFIX>.<VARIABLE>. This implies that a new MeshObject shall be created
            // with an automatically-generated MeshObjectIdentifier.
            for( String arg : postArguments.keySet() ) {
                if( !arg.startsWith( PREFIX )) {
                    continue; // skip all that aren't for us
                }
                if( !arg.endsWith( ACCESS_TAG )) {
                    continue; // not in this loop
                }
                String coreArg = arg.substring( PREFIX.length(), arg.length()-ACCESS_TAG.length() );
                String varName = coreArg;
                String varValue = lidRequest.getPostedArgument( PREFIX + varName );
                if( varValue != null ) {
                    // dealt with this one already
                    continue;
                }
                MeshBase            base       = findMeshBaseFor( varName, lidRequest );
                HttpShellAccessVerb accessVerb = HttpShellAccessVerb.findAccessFor( varName, lidRequest );

                OnDemandTransaction  tx = txs.obtainFor( base );

                MeshObject accessed = accessVerb.access( null, base, tx, lidRequest );
                if( accessed != null ) {
                    variables.put( varName, accessed );

                    // first bless then unbless, then properties
                    potentiallyBless(         varName, accessed, tx, lidRequest );
                    potentiallyUnbless(       varName, accessed, tx, lidRequest );
                    potentiallySetProperties( varName, accessed, tx, lidRequest );
                }
            }

            // now unbless roles
            for( String var1Name : variables.keySet() ) {
                String key = PREFIX + var1Name + TO_TAG + SEPARATOR;

                for( String arg : postArguments.keySet() ) {
                    if( !arg.startsWith( key )) {
                        continue; // not relevant here
                    }
                    if( arg.endsWith( UNBLESS_ROLE_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-UNBLESS_ROLE_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        String [] values = lidRequest.getMultivaluedPostedArgument( arg );
                        if( values != null ) {
                            OnDemandTransaction tx = txs.obtainFor( found1.getMeshBase() );

                            for( String v : values ) {
                                RoleType toUnbless = (RoleType) findMeshType( v ); // can thrown ClassCastException
                                Transaction tx2 = tx.obtain();
                                found1.unblessRelationship( toUnbless, found2 );
                            }
                        }
                    }
                }
            }

            // now create and delete relationships
            for( String var1Name : variables.keySet() ) {
                String key = PREFIX + var1Name + TO_TAG + SEPARATOR;

                for( String arg : postArguments.keySet() ) {
                    if( !arg.startsWith( key )) {
                        continue; // not relevant here
                    }
                    if( arg.endsWith( PERFORM_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-PERFORM_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        HttpShellRelationshipVerb relVerb = HttpShellRelationshipVerb.findPerformFor( var1Name, var2Name, lidRequest );

                        OnDemandTransaction tx = txs.obtainFor( found1.getMeshBase() );

                        if( relVerb != null ) {
                            relVerb.perform( found1, found2, tx, lidRequest );
                        }
                    }
                }
            }

            // now bless roles
            for( String var1Name : variables.keySet() ) {
                String key = PREFIX + var1Name + TO_TAG + SEPARATOR;

                for( String arg : postArguments.keySet() ) {
                    if( !arg.startsWith( key )) {
                        continue; // not relevant here
                    }
                    if( arg.endsWith( BLESS_ROLE_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-BLESS_ROLE_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        if( found1 != null && found2 != null ) {
                            // be lenient
                            String [] values = lidRequest.getMultivaluedPostedArgument( arg );
                            if( values != null ) {
                                OnDemandTransaction tx = txs.obtainFor( found1.getMeshBase() );

                                for( String v : values ) {
                                    RoleType toBless = (RoleType) findMeshType( v ); // can thrown ClassCastException
                                    Transaction tx2 = tx.obtain();
                                    found1.blessRelationship( toBless, found2 );
                                }
                            }
                        }
                    }
                }
            }

            // now deal with checkboxes
            for( String var1Name : variables.keySet() ) {
                String key = PREFIX + var1Name + TO_TAG + SEPARATOR;

                for( String arg : postArguments.keySet() ) {
                    if( !arg.startsWith( key )) {
                        continue; // not relevant here
                    }
                    if( arg.endsWith( CHECKBOX_ROLE_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-CHECKBOX_ROLE_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        String   value = lidRequest.getPostedArgument( arg );
                        RoleType rt    = (RoleType) findMeshType( value );

                        // now look for whether the checkbox argument has been POST'd or not
                        String arg2 = arg.substring( 0, arg.length()-CHECKBOX_ROLE_TAG.length() ) + CHECKBOX_TAG;
                        String [] values = lidRequest.getMultivaluedPostedArgument( arg2 );

                        OnDemandTransaction tx = txs.obtainFor( found1.getMeshBase() );
                        tx.obtain();

                        if( values != null && values.length > 0 ) {
                            // relate and bless
                            try {
                                found1.relate( found2 );
                            } catch( RelatedAlreadyException t ) {
                                // ignore
                            }
                            try {
                                found1.blessRelationship( rt, found2 );
                            } catch( RoleTypeBlessedAlreadyException ex ) {
                                // ignore
                            }
                        } else {
                            // unbless and possibly unrelate
                            try {
                                found1.unblessRelationship( rt, found2 );
                            } catch( RoleTypeNotBlessedException ex ) {
                                // ignore
                            } catch( NotRelatedException ex ) {
                                // ignore
                            }
                            if( found1.getRoleTypes( found2 ).length == 0 ) {
                                try {
                                    found1.unrelate( found2 );
                                } catch( NotRelatedException ex ) {
                                    // ignore
                                }
                            }
                        }
                    }
                }
            }

        } catch( ParseException ex ) {
            throw new HttpShellException( ex );

        } catch( MeshObjectAccessException ex ) {
            throw new HttpShellException( ex );
            
        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            throw new HttpShellException( ex );

        } catch( MeshTypeWithIdentifierNotFoundException ex ) {
            throw new HttpShellException( ex );

        } catch( IsAbstractException ex ) {
            throw new HttpShellException( ex );

        } catch( EntityNotBlessedException ex ) {
            throw new HttpShellException( ex );

        } catch( EntityBlessedAlreadyException ex ) {
            throw new HttpShellException( ex );

        } catch( IllegalPropertyTypeException ex ) {
            throw new HttpShellException( ex );

        } catch( IllegalPropertyValueException ex ) {
            throw new HttpShellException( ex );

        } catch( PropertyValueParsingException ex ) {
            throw new HttpShellException( ex );

        } catch( NotRelatedException ex ) {
            throw new HttpShellException( ex );

        } catch( RelatedAlreadyException ex ) {
            throw new HttpShellException( ex );

        } catch( RoleTypeNotBlessedException ex ) {
            throw new HttpShellException( ex );

        } catch( RoleTypeBlessedAlreadyException ex ) {
            throw new HttpShellException( ex );

        } catch( RoleTypeRequiresEntityTypeException ex ) {
            throw new HttpShellException( ex );

        } catch( FactoryException ex ) {
            getLog().error( ex ); // should not happen

        } catch( TransactionException ex ) {
            getLog().error( ex ); // should not happen

        } finally {

            for( OnDemandTransaction tx : txs.values() ) {
                if( tx.hasBeenCreated() ) {
                    try {
                        tx.obtain().commitTransaction();
                    } catch( Throwable t ) {
                        getLog().error( t );
                    }
                }
            }
        }

        // and now for redirects
        String redirectVar = null;
        for( String var1Name : variables.keySet() ) {
            String    key   = PREFIX + var1Name + REDIRECT_TAG;
            String [] value = postArguments.get( key );

            if( value != null && value.length == 1 && REDIRECT_TAG_TRUE.equalsIgnoreCase( value[0] )) {
                if( redirectVar != null ) {
                    throw new HttpShellException( new ConflictingArgumentsException( key, redirectVar ));
                }
                redirectVar = var1Name;
            }
        }

        String ret = null;
        if( redirectVar != null ) {
            MeshObject redirectObj = variables.get( redirectVar );

            StringRepresentationDirectory  dir  = StringRepresentationDirectorySingleton.getSingleton();
            StringRepresentation           rep  = dir.get( StringRepresentationDirectory.TEXT_URL_NAME );
            
            if( rep == null ) {
                rep = dir.getFallback();
            }
            
            SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
            pars.put( StringRepresentationParameters.COLLOQUIAL,               false );
            pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, lidRequest.getAbsoluteContextUri() );
            pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, lidRequest.getContextPath() );

            try {
                ret = redirectObj.getIdentifier().toStringRepresentation( rep, pars );

                ret = lidRequest.getAbsoluteContextUriWithSlash() + ret;
                
            } catch( StringifierException ex ) {
                getLog().error( ex );
            }
        }
        return ret;
    }

    /**
     * Find the MeshBase in which a MeshObject is to be accessed.
     *
     * @param varName the variable name of the to-be-accessed object in the request
     * @param request the request
     * @return the MeshBase
     * @throws ParseException thrown if the name of the MeshBase could not be parsed
     */
    @SuppressWarnings("unchecked")
    protected MeshBase findMeshBaseFor(
            String      varName,
            SaneRequest request )
        throws
            ParseException
    {
        ensureInitialized();

        StringBuilder key = new StringBuilder();
        key.append( PREFIX );
        key.append( varName );
        key.append( MESH_BASE_TAG );

        String value = request.getPostedArgument( key.toString() );
        if( value == null || value.length() == 0 ) {
            return theMainMeshBase;
        }
        if( theMeshBaseIdentifierFactory == null ) {
            throw new ContextObjectNotFoundException( MeshBaseIdentifierFactory.class );
        }
        if( theMeshBaseNameServer == null ) {
            throw new ContextObjectNotFoundException( MeshBase.class );
        }

        MeshBaseIdentifier id  = theMeshBaseIdentifierFactory.fromExternalForm( value );
        MeshBase           ret = theMeshBaseNameServer.get( id );

        return ret;
    }

    /**
     * Parse a String into a MeshObjectIdentifier.
     * 
     * @param idFact the MeshObjectIdentifierFactory
     * @param raw the String
     * @return the parsed MeshObjectIdentifier
     * @throws ParseException thrown if the MeshObjectIdentifier could not be parsed
     */
    protected MeshObjectIdentifier parseMeshObjectIdentifier(
            MeshObjectIdentifierFactory idFact,
            String                      raw )
        throws
            ParseException
    {
        ensureInitialized();

        MeshObjectIdentifier ret;
        if( raw == null || raw.length() == 0 ) {
            ret = null;
        } else {
            ret = idFact.fromStringRepresentation( theParsingRepresentation, raw );
        }

        return ret;
    }

    /**
     * Determine from the request whether the object with a variable name is supposed to be blessed and how,
     * and if so, perform the blessing.
     *
     * @param varName the variable name of the to-be-blessed object in the request
     * @param obj the accessed object
     * @param tx the Transaction if and when created
     * @param request the request
     * @throws ClassCastException thrown if a MeshType with this identifier could be found, but it was of the wrong type
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a MeshType with this identifier could not be found
     * @throws EntityBlessedAlreadyException thrown if the MeshObject is already blessed with this MeshType
     * @throws IsAbstractException thrown if the MeshType is abstract
     * @throws TransactionException thrown if a problem occurred with the Transaction
     * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     */
    protected void potentiallyBless(
            String                        varName,
            MeshObject                    obj,
            CreateWhenNeeded<Transaction> tx,
            SaneRequest                   request )
        throws
            ClassCastException,
            MeshTypeWithIdentifierNotFoundException,
            EntityBlessedAlreadyException,
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        StringBuilder buf = new StringBuilder();
        buf.append( PREFIX );
        buf.append( varName );
        buf.append( BLESS_TAG );

        String [] values = request.getMultivaluedPostedArgument( buf.toString() );
        if( values != null ) {
            for( String v : values ) {
                EntityType toBless = (EntityType) findMeshType( v ); // can thrown ClassCastException
                Transaction tx2 = tx.obtain();
                obj.bless( toBless );
            }
        }
    }

    /**
     * Determine from the request whether the object with a variable name is supposed to be unblessed and how,
     * and if so, perform the unblessing.
     *
     * @param varName the variable name of the to-be-unblessed object in the request
     * @param obj the accessed object
     * @param tx the Transaction if and when created
     * @param request the request
     * @throws ClassCastException thrown if a MeshType with this identifier could be found, but it was of the wrong type
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a MeshType with this identifier could not be found
     * @throws EntityNotBlessedException thrown if the MeshObject is not blessed with this MeshType
     * @throws RoleTypeRequiresEntityTypeException thrown if this MeshObject cannot be unblessed as long as one of its role requires this EntityType
     * @throws IsAbstractException thrown if the MeshType is abstract
     * @throws TransactionException thrown if a problem occurred with the Transaction
     * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     */
    protected void potentiallyUnbless(
            String                        varName,
            MeshObject                    obj,
            CreateWhenNeeded<Transaction> tx,
            SaneRequest                   request )
        throws
            ClassCastException,
            MeshTypeWithIdentifierNotFoundException,
            EntityNotBlessedException,
            RoleTypeRequiresEntityTypeException,
            IsAbstractException,
            TransactionException,
            NotPermittedException
    {
        StringBuilder buf = new StringBuilder();
        buf.append( PREFIX );
        buf.append( varName );
        buf.append( UNBLESS_TAG );

        String [] values = request.getMultivaluedPostedArgument( buf.toString() );
        if( values != null ) {
            for( String v : values ) {
                EntityType toUnbless = (EntityType) findMeshType( v ); // can thrown ClassCastException
                Transaction tx2 = tx.obtain();
                obj.unbless( toUnbless );
            }
        }
    }

    /**
     * Determine from the request whether the object with a variable name is supposed to have any properties
     * set, and if so, perform the property setting.
     *
     * @param varName the variable name of the to-be-unblessed object in the request
     * @param obj the accessed object
     * @param tx the Transaction if and when created
     * @param request the request
     * @throws ClassCastException thrown if a MeshType with this identifier could be found, but it was of the wrong type
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a MeshType with this identifier could not be found
     * @throws PropertyValueParsingException thrown if a PropertyValue could not be parsed
     * @throws IllegalPropertyTypeException thrown if a PropertyType was not valid on this MeshObject
     * @throws IllegalPropertyValueException thrown if a PropertyValue was not valid for a PropertyType
     * @throws TransactionException thrown if a problem occurred with the Transaction
     * @throws NotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     */
    protected void potentiallySetProperties(
            String                        varName,
            MeshObject                    obj,
            CreateWhenNeeded<Transaction> tx,
            SaneRequest                   request )
        throws
            ClassCastException,
            MeshTypeWithIdentifierNotFoundException,
            PropertyValueParsingException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            TransactionException,
            NotPermittedException
    {
        Map<String,String[]> postArguments = request.getPostedArguments();

        StringBuilder buf = new StringBuilder();
        buf.append( PREFIX );
        buf.append( varName );
        buf.append( PROPERTY_TYPE_TAG );

        String propTypePrefix = buf.toString();

        for( String arg : postArguments.keySet() ) {
            if( !arg.startsWith( propTypePrefix )) {
                continue; // not relevant here
            }
            String propVarName = arg.substring( propTypePrefix.length() );

            buf = new StringBuilder();
            buf.append( PREFIX );
            buf.append( varName );
            buf.append( PROPERTY_VALUE_TAG );
            buf.append( propVarName );

            String propValueKey     = buf.toString();
            String propValueString  = request.getPostedArgument( propValueKey );
            String propMimeString   = request.getPostedArgument( propValueKey + MIME_TAG );
            String propTypeString   = request.getPostedArgument( arg );
            MimePart uploadPart     = request.getMimePart( propValueKey + UPLOAD_PROPERTY_VALUE_TAG );

            PropertyType propertyType = (PropertyType) findMeshType( propTypeString );

            buf = new StringBuilder();
            buf.append( PREFIX );
            buf.append( varName );
            buf.append( PROPERTY_VALUE_TAG );
            buf.append( propVarName );
            buf.append( NULL_PROPERTY_VALUE_TAG );

            String nullValueKey    = buf.toString();
            String nullValueString = request.getPostedArgument( nullValueKey );

            PropertyValue value;

            // null has preference over upload, which has preference over the regular value
            if( NULL_PROPERTY_VALUE_TAG_TRUE.equals( nullValueString )) {
                value = null;
                
            } else if( uploadPart != null && uploadPart.getContent().length > 0 && propertyType.getDataType() instanceof BlobDataType ) {
                BlobDataType type = (BlobDataType) propertyType.getDataType();

                if( uploadPart.getMimeType().startsWith( "text/" )) {
                    try {
                        value = type.createBlobValue( uploadPart.getContentAsString(), uploadPart.getMimeType() );
                    } catch( UnsupportedEncodingException ex ) {
                        log.warn( ex );
                        value = type.createBlobValue( uploadPart.getContent(), uploadPart.getMimeType() ); // try this instead
                    }
                } else {
                    value = type.createBlobValue( uploadPart.getContent(), uploadPart.getMimeType() );
                }


            } else if( propValueString != null ) {
                value = propertyType.fromStringRepresentation( theParsingRepresentation, propValueString, propMimeString );

            } else {
                // nothing given: leave as is
                continue;
            }

            Transaction tx2 = tx.obtain();

            obj.setPropertyValue( propertyType, value );
        }
    }

    /**
     * Ensure that the buffered objects are initialized.
     */
    @SuppressWarnings("unchecked")
    protected void ensureInitialized()
    {
        if( theMainMeshBase == null ) {
            // the name server may be null, so we test against main MeshBase, which is always non-null

            InfoGridWebApp app        = InfoGridWebApp.getSingleton();
            Context        appContext = app.getApplicationContext();

            theMeshBaseNameServer        = appContext.findContextObject( MeshBaseNameServer.class );
            theMeshBaseIdentifierFactory = appContext.findContextObject( MeshBaseIdentifierFactory.class );
            theMainMeshBase              = appContext.findContextObjectOrThrow( MeshBase.class );

            StringRepresentationDirectory dir = appContext.findContextObjectOrThrow( StringRepresentationDirectory.class );

            theParsingRepresentation = dir.get( StringRepresentationDirectory.TEXT_HTTP_POST_NAME );
            if( theParsingRepresentation == null ) {
                theParsingRepresentation = dir.getFallback();
            }
        }
    }

    /**
     * Find a MeshType from an identifier given as String.
     *
     * @param s the String
     * @return the MeshType
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a MeshType with this identifier could not be found
     */
    protected MeshType findMeshType(
            String s )
        throws
            MeshTypeWithIdentifierNotFoundException
    {
        ensureInitialized();

        s = s.trim();

        ModelBase          modelBase  = theMainMeshBase.getModelBase();
        MeshTypeIdentifier identifier = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( s );
        MeshType           ret        = modelBase.findMeshTypeByIdentifier( identifier );
        return ret;
    }

    /**
     * Initialization method for this filter.
     *
     * @param filterConfig the filter configuration object
     */
    public void init(
            FilterConfig filterConfig )
    {
        theFilterConfig = filterConfig;
    }

    /**
     * Destroy method for this filter.
     */
    public void destroy()
    {
    }

    /**
     * Initialize and get the log.
     *
     * @return the log
     */
    private static Log getLog()
    {
        if( log == null ) {
            log = Log.getLogInstance( HttpShellFilter.class ); // our own, private logger
        }
        return log;
    }

    /**
     * The Filter configuration object.
     */
    protected FilterConfig theFilterConfig;

    /**
     * Buffered MeshBase name server.
     */
    protected MeshBaseNameServer<MeshBaseIdentifier,MeshBase> theMeshBaseNameServer;

    /**
     * Buffered factory for MeshBaseIdentifiers.
     */
    protected MeshBaseIdentifierFactory theMeshBaseIdentifierFactory;

    /**
     * Buffered main MeshBase.
     */
    protected MeshBase theMainMeshBase;

    /**
     * The StringRepresentation to use.
     */
    protected StringRepresentation theParsingRepresentation;
}
