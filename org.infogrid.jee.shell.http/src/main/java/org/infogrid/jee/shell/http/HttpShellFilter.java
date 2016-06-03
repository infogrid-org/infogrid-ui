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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.jee.ProblemReporter;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.security.SafeUnsafePostFilter;
import org.infogrid.jee.servlet.AbstractInfoGridWebAppFilter;
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
import org.infogrid.meshbase.MeshObjectsNotFoundException;
import org.infogrid.meshbase.sweeper.Sweeper;
import org.infogrid.meshbase.transaction.OnDemandTransaction;
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
import org.infogrid.model.primitives.TimeStampValue;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.CreateWhenNeeded;
import org.infogrid.util.FactoryException;
import org.infogrid.util.MCachingHashMap;
import org.infogrid.util.MSmartFactory;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.ContextObjectNotFoundException;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.MimePart;
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
    extends
        AbstractInfoGridWebAppFilter
    implements
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
     * Initialize
     *
     * @param filterConfig the filter configuration
     * @throws ServletException an exception occurred
     */
    @Override
    protected void internalInit(
            FilterConfig filterConfig )
        throws
            ServletException
    {
        String s = filterConfig.getInitParameter( PREFER_VARS_FROM_REQUEST_PAR_NAME );
        if( s != null ) {
            thePreferVarsFromRequest = new HashSet<>();
            for( String t : s.split( "," )) {
                thePreferVarsFromRequest.add( t.trim() );
            }
        }
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
                    if( getLog().isDebugEnabled() ) {
                        // otherwise it's producing way too much output
                        getLog().debug( "Ignoring unsafe POST", lidRequest );
                    }
                }
            }

        } catch( Throwable ex ) {
            getLog().warn( ex );

            ProblemReporter reporter = (ProblemReporter) request.getAttribute( ProblemReporter.PROBLEM_REPORTER_ATTRIBUTE_NAME );
            if( reporter != null ) {
                reporter.reportProblem( ex );
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
            final SaneRequest lidRequest )
        throws
            NotPermittedException,
            HttpShellException
    {
        ensureInitialized();

        Map<String,String[]>              postArguments = lidRequest.getPostedArguments();
        final ArrayList<HttpShellHandler> handlers      = new ArrayList<>();
        String                            ret           = null;

        // determine handlers
        String [] handlerNames = postArguments.get( HANDLER_TAG );
        if( handlerNames != null ) {
            for( String handlerName : handlerNames ) {
                try {
                    HttpShellHandler handler = findHandler( handlerName );
                    if( handler == null ) {
                        throw new SpecifiedHandlerNotFoundException( handlerName );
                    }
                    handlers.add( handler );

                } catch( Throwable ex ) {
                    throw new HttpShellException( ex );
                }
            }
        }
        HashMap<String,MeshObject> variables = new HashMap<>();
        Throwable                  thrown    = null;
        TimeStampValue             now       = TimeStampValue.now();

        HttpShellOnDemandTransactionFactory txFact = new HttpShellOnDemandTransactionFactory( lidRequest, handlers, theMainMeshBase, now );

        MSmartFactory<MeshBase,OnDemandTransaction,Void> txs = MSmartFactory.create(
                txFact,
                MCachingHashMap.<MeshBase,OnDemandTransaction>create() );
        txFact.setTransactions( txs );

        // invoke pre-transaction
        for( HttpShellHandler handler : handlers ) {
            handler.beforeTransactionStart( lidRequest, theMainMeshBase, now );
        }

        if( thePreferVarsFromRequest != null ) {
            for( String varName : thePreferVarsFromRequest ) {
                Object varValue = lidRequest.getAttribute( varName );
                if( varValue == null || varValue instanceof MeshObject ) {
                    variables.put( varName, (MeshObject) varValue );
                } else {
                    variables.put( varName, null );
                    log.error( "HttpShellFilter variable", varName, "obtained from request has wrong type:", varValue );
                }
            }
        }
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
                if( coreArg.equals( COMMAND_TAG )) {
                    continue; // skip command tag
                }
                if( coreArg.contains( SEPARATOR ) ) {
                    continue; // skip all that aren't referring to the MeshObjects
                }
                String varName  = coreArg;

                if( variables.containsKey( varName )) {
                    continue; // no repetition please
                }
                String varValue = lidRequest.getPostedArgument( arg ); // use SaneRequest's error handling for multiple values

                if( UNASSIGNED_VALUE.equals( varValue )) {
                    throw new HttpShellException( new UnassignedArgumentException( arg ));
                }
                MeshBase            base       = findMeshBaseFor( varName, lidRequest );
                HttpShellAccessVerb accessVerb = HttpShellAccessVerb.findAccessFor( varName, lidRequest );

                MeshObjectIdentifier id = parseMeshObjectIdentifier( base.getMeshObjectIdentifierFactory(), varValue );

                OnDemandTransaction tx = txs.obtainFor( base );

                MeshObject accessed = accessVerb.access( id, base, tx, lidRequest );
                variables.put( varName, accessed );
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
                if( variables.containsKey( varName )) {
                    // dealt with this one already
                    continue;
                }
                MeshBase            base       = findMeshBaseFor( varName, lidRequest );
                HttpShellAccessVerb accessVerb = HttpShellAccessVerb.findAccessFor( varName, lidRequest );

                OnDemandTransaction  tx = txs.obtainFor( base );

                MeshObject accessed = accessVerb.access( null, base, tx, lidRequest );
                variables.put( varName, accessed );
            }

            // invoke after access
            for( HttpShellHandler handler : handlers ) {
                handler.afterAccess( lidRequest, variables, txs, theMainMeshBase, now );
            }

            for( Map.Entry<String,MeshObject> entry : variables.entrySet() ) {
                String     varName  = entry.getKey();
                MeshObject accessed = entry.getValue();

                if( accessed != null && !accessed.getIsDead() ) {
                    // first bless then unbless, then properties

                    OnDemandTransaction  tx = txs.obtainFor( accessed.getMeshBase() );

                    potentiallyBless(         varName, accessed, tx, lidRequest );
                    potentiallyUnbless(       varName, accessed, tx, lidRequest );
                    potentiallySetProperties( varName, accessed, tx, lidRequest );
                }
            }

            // now unbless roles
            for( String var1Name : variables.keySet() ) {

                MeshObject var1 = variables.get( var1Name );
                if( var1 != null && var1.getIsDead() ) {
                    continue;
                }

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
                                if( v.length() > 0 ) { // support "none" as an option in select fields
                                    RoleType toUnbless = (RoleType) findMeshType( v ); // can thrown ClassCastException
                                    Transaction tx2 = tx.obtain();
                                    found1.unblessRelationship( toUnbless, found2 );
                                }
                            }
                        }
                    }
                    if( arg.endsWith( UNBLESS_ROLE_IF_NEEDED_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-UNBLESS_ROLE_IF_NEEDED_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        String [] values = lidRequest.getMultivaluedPostedArgument( arg );
                        if( values != null ) {
                            OnDemandTransaction tx = txs.obtainFor( found1.getMeshBase() );

                            for( String v : values ) {
                                if( v.length() > 0 ) { // support "none" as an option in select fields
                                    RoleType toUnbless = (RoleType) findMeshType( v ); // can thrown ClassCastException
                                    if( found1.isRelated( toUnbless, found2 ) ) {
                                        Transaction tx2 = tx.obtain();
                                        found1.unblessRelationship( toUnbless, found2 );
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // now create and delete relationships
            for( String var1Name : variables.keySet() ) {

                MeshObject var1 = variables.get( var1Name );
                if( var1 != null && var1.getIsDead() ) {
                    continue;
                }

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
                            relVerb.perform( found1, found2, var1Name, var2Name, tx, lidRequest );
                        }
                    }
                }
            }

            // now bless roles
            for( String var1Name : variables.keySet() ) {
                MeshObject var1 = variables.get( var1Name );
                if( var1 != null && var1.getIsDead() ) {
                    continue;
                }

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
                                    if( v.length() > 0 ) { // support "none" as an option in select fields
                                        RoleType toBless = (RoleType) findMeshType( v ); // can thrown ClassCastException
                                        Transaction tx2 = tx.obtain();
                                        found1.blessRelationship( toBless, found2 );
                                    }
                                }
                            }
                        }
                    }
                    if( arg.endsWith( BLESS_ROLE_IF_NEEDED_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-BLESS_ROLE_IF_NEEDED_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        if( found1 != null && found2 != null ) {
                            // be lenient
                            String [] values = lidRequest.getMultivaluedPostedArgument( arg );
                            if( values != null ) {
                                OnDemandTransaction tx = txs.obtainFor( found1.getMeshBase() );

                                for( String v : values ) {
                                    if( v.length() > 0 ) { // support "none" as an option in select fields
                                        RoleType toBless = (RoleType) findMeshType( v ); // can thrown ClassCastException
                                        if( !found1.isRelated( toBless, found2 )) {
                                            Transaction tx2 = tx.obtain();
                                            found1.blessRelationship( toBless, found2 );
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // now deal with checkboxes and radioboxes
            for( String var1Name : variables.keySet() ) {
                MeshObject var1 = variables.get( var1Name );
                if( var1 != null && var1.getIsDead() ) {
                    continue;
                }

                String key = PREFIX + var1Name + TO_TAG + SEPARATOR;

                for( String arg : postArguments.keySet() ) {
                    if( !arg.startsWith( key )) {
                        continue; // not relevant here
                    }
                    if( arg.endsWith( CHECKBOX_ROLE_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-CHECKBOX_ROLE_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        if( found2 == null ) {
                            throw new HttpShellException( new SpecifiedMeshObjectNotFoundException( var2Name ));
                        }
                        if( found1 == null ) {
                            throw new HttpShellException( new SpecifiedMeshObjectNotFoundException( var1Name ));
                        }
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
                    if( arg.endsWith( RADIOBOX_ROLE_TAG )) {
                        String     var2Name = arg.substring( key.length(), arg.length()-RADIOBOX_ROLE_TAG.length() );
                        MeshObject found2   = variables.get( var2Name );
                        MeshObject found1   = variables.get( var1Name );

                        if( found2 == null ) {
                            throw new HttpShellException( new SpecifiedMeshObjectNotFoundException( var2Name ));
                        }
                        if( found1 == null ) {
                            throw new HttpShellException( new SpecifiedMeshObjectNotFoundException( var1Name ));
                        }
                        String   value = lidRequest.getPostedArgument( arg );
                        RoleType rt    = (RoleType) findMeshType( value );

                        String radiogroupName = lidRequest.getPostedArgument( key + var2Name + RADIOBOX_NAME_TAG );
                        if( radiogroupName == null ) {
                            continue;
                        }
                        OnDemandTransaction tx = txs.obtainFor( found1.getMeshBase() );
                        tx.obtain();

                        String doBless = lidRequest.getPostedArgument( radiogroupName );
                        if( doBless != null && doBless.equals( key + var2Name + RADIOBOX_TAG )) {
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
            String [] commands = postArguments.get( FULL_COMMAND_TAG );
            if( commands != null ) {
                for( int i=0 ; i<commands.length ; ++i ) {
                    String current = commands[i];

                    if( SWEEP_ALL_COMMAND.equals( current )) {
                        MeshBase mb = findMeshBaseFor( COMMAND_TAG, lidRequest ); // ugly?

                        Sweeper s = mb.getSweeper();
                        if( s == null ) {
                            continue;
                        }
                        s.sweepAllNow();
                    }
                }
            }

            // invoke pre-transaction
            for( HttpShellHandler handler : handlers ) {
                handler.beforeTransactionEnd( lidRequest, variables, txs, theMainMeshBase, now );
            }

        } catch( HttpShellException ex ) {
            thrown = ex;
            throw ex;

        } catch( SpecifiedMeshObjectNotFoundException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( ParseException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( MeshObjectAccessException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( MeshObjectsNotFoundException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( MeshObjectIdentifierNotUniqueException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( MeshTypeWithIdentifierNotFoundException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( IsAbstractException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( EntityNotBlessedException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( EntityBlessedAlreadyException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( IllegalPropertyTypeException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( IllegalPropertyValueException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( PropertyValueParsingException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( NotRelatedException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( RelatedAlreadyException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( RoleTypeNotBlessedException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( RoleTypeBlessedAlreadyException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( RoleTypeRequiresEntityTypeException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } catch( FactoryException ex ) {
            thrown = ex;
            getLog().error( ex ); // should not happen

        } catch( TransactionException ex ) {
            thrown = ex;
            getLog().error( ex ); // should not happen

        } catch( RuntimeException ex ) {
            thrown = ex;
            throw new HttpShellException( ex );

        } finally {

            for( OnDemandTransaction tx : txs.values() ) {
                if( tx.hasBeenCreated() ) {
                    try {
                        Transaction tx2 = tx.obtain();

                        if( thrown == null ) {
                            tx2.commitTransaction();
                        } else {
                            tx2.rollbackTransaction( thrown );
                        }
                    } catch( Throwable t ) {
                        getLog().error( t );
                    }
                }
            }
            // invoke post-transaction
            for( HttpShellHandler handler : handlers ) {
                try {
                    String ret2 = handler.afterTransactionEnd( lidRequest, variables, txs, theMainMeshBase, now, thrown );
                    if( ret2 == null ) {
                        continue;
                    }

                    if( ret == null || ret.equals( ret2 )) {
                        ret = ret2;
                    } else {
                        getLog().error( "More than one handler declared redirect URL: ", ret, ret2, handler );
                    }

                // make sure we pass on the first exception
                } catch( HttpShellException t ) {
                    if( thrown != null ) {
                        getLog().error( "Two exceptions, passing on first:", thrown, t );
                    } else {
                        throw t;
                    }

                } catch( RuntimeException t ) {
                    if( thrown != null ) {
                        getLog().error( "Two exceptions, passing on first:", thrown, t );
                    } else {
                        throw t;
                    }
                }
            }
        }

        // and now for redirects, if none has been found so far
        if( ret == null ) {
            String redirectVar   = null;
            String redirectValue = null;
            for( String var1Name : variables.keySet() ) {
                String    key   = PREFIX + var1Name + REDIRECT_TAG;
                String [] value = postArguments.get( key );

                if( value != null && value.length == 1 && value[0] != null && value[0].trim().length() > 0 ) {
                    if( redirectVar != null ) {
                        throw new HttpShellException( new ConflictingArgumentsException( key, redirectVar, lidRequest ));
                    }
                    redirectVar   = var1Name;
                    redirectValue = value[0].trim();
                }
            }

            if( redirectVar != null ) {
                MeshObject redirectObj = variables.get( redirectVar );

                String ret2 = calculateRedirectUrlFromMeshObject( lidRequest, redirectObj );

                if( !REDIRECT_TAG_TRUE.equalsIgnoreCase( redirectValue )) {
                    ret2 = HTTP.appendArgumentPairToUrl( ret2, redirectValue );
                }

                if( ret2 != null ) {
                    if( ret == null || ret.equals( ret2 )) {
                        ret = ret2;
                    } else {
                        getLog().error( "Shell declaration of redirect after a handler declared redirect URL: ", ret, ret2, redirectVar );
                    }
                }
            }
        }
        return ret;
    }

    /**
     * Helper method to construct a redirect URL from a MeshObject that will be subject.
     *
     * @param lidRequest the incoming request
     * @param subject the subject MeshObject
     * @return the URL
     */
    public static String calculateRedirectUrlFromMeshObject(
            SaneRequest lidRequest,
            MeshObject  subject )
    {
        StringRepresentationDirectory  dir  = StringRepresentationDirectorySingleton.getSingleton();
        StringRepresentation           rep  = dir.get( StringRepresentationDirectory.TEXT_URL_NAME );

        if( rep == null ) {
            rep = dir.getFallback();
        }

        SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
        pars.put( StringRepresentationParameters.COLLOQUIAL,               false );
        pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, lidRequest.getAbsoluteContextUri() );
        pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, lidRequest.getContextPath() );

        String ret = null;
        try {
            ret = subject.getIdentifier().toStringRepresentation( rep, pars );

            ret = lidRequest.getAbsoluteContextUriWithSlash() + ret;

        } catch( StringifierException ex ) {
            getLog().error( ex );
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
        MeshObjectIdentifier ret;
        if( raw == null || raw.length() == 0 ) {
            ret = null;
        } else {
            raw = raw.trim();

            ret = idFact.fromStringRepresentation( theParsingRepresentation, SimpleStringRepresentationParameters.create(), raw );
        }

        return ret;
    }

    /**
     * Helper method to find the instance of a HttpShellHandler by name.
     *
     * @param name name of the handler
     * @return the handler, or null
     */
    protected HttpShellHandler findHandler(
            String name )
    {
        Iterator<HttpShellHandler> iter = theAppContext.contextObjectIterator( HttpShellHandler.class );
        while( iter.hasNext() ) {
            HttpShellHandler current = iter.next();

            if( name.equals( current.getName() )) {
                return current;
            }
        }
        return null;
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
        StringBuilder buf1 = new StringBuilder();
        buf1.append( PREFIX );
        buf1.append( varName );
        buf1.append( BLESS_TAG );

        String [] values1 = request.getMultivaluedPostedArgument( buf1.toString() );
        if( values1 != null ) {
            for( String v : values1 ) {
                if( v.length() > 0 ) { // support "none" as an option in select fields
                    EntityType toBless = (EntityType) findMeshType( v ); // can thrown ClassCastException
                    Transaction tx2 = tx.obtain();
                    obj.bless( toBless );
                }
            }
        }

        StringBuilder buf2 = new StringBuilder();
        buf2.append( PREFIX );
        buf2.append( varName );
        buf2.append( BLESS_IF_NEEDED_TAG );

        String [] values2 = request.getMultivaluedPostedArgument( buf2.toString() );
        if( values2 != null ) {
            for( String v : values2 ) {
                if( v.length() > 0 ) { // support "none" as an option in select fields
                    EntityType toBless = (EntityType) findMeshType( v ); // can thrown ClassCastException
                    if( !obj.isBlessedBy( toBless ) ) {
                        Transaction tx2 = tx.obtain();
                        obj.bless( toBless );
                    }
                }
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
        StringBuilder buf1 = new StringBuilder();
        buf1.append( PREFIX );
        buf1.append( varName );
        buf1.append( UNBLESS_TAG );

        String [] values1 = request.getMultivaluedPostedArgument( buf1.toString() );
        if( values1 != null ) {
            for( String v : values1 ) {
                if( v.length() > 0 ) { // support "none" as an option in select fields
                    EntityType toUnbless = (EntityType) findMeshType( v ); // can thrown ClassCastException
                    Transaction tx2 = tx.obtain();
                    obj.unbless( toUnbless );
                }
            }
        }

        StringBuilder buf2 = new StringBuilder();
        buf2.append( PREFIX );
        buf2.append( varName );
        buf2.append( UNBLESS_IF_NEEDED_TAG );

        String [] values2 = request.getMultivaluedPostedArgument( buf2.toString() );
        if( values2 != null ) {
            for( String v : values2 ) {
                if( v.length() > 0 ) { // support "none" as an option in select fields
                    EntityType toUnbless = (EntityType) findMeshType( v ); // can thrown ClassCastException
                    if( obj.isBlessedBy( toUnbless ) ) {
                        Transaction tx2 = tx.obtain();
                        obj.unbless( toUnbless );
                    }
                }
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
                        getLog().warn( ex );
                        value = type.createBlobValue( uploadPart.getContent(), uploadPart.getMimeType() ); // try this instead
                    }
                } else {
                    value = type.createBlobValue( uploadPart.getContent(), uploadPart.getMimeType() );
                }


            } else if( propValueString != null ) {
                buf = new StringBuilder();
                buf.append( PREFIX );
                buf.append( varName );
                buf.append( PROPERTY_VALUE_TAG );
                buf.append( propVarName );
                buf.append( PROPERTY_VALUE_FORMAT_TAG );

                String format = request.getPostedArgument( buf.toString() );

                SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
                if( format != null ) {
                    pars.put( StringRepresentationParameters.FORMAT_STRING, format );
                }
                value = propertyType.fromStringRepresentation( theParsingRepresentation, pars, propValueString, propMimeString );

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

            theAppContext = getInfoGridWebApp().getApplicationContext();

            theMeshBaseNameServer        = theAppContext.findContextObject( MeshBaseNameServer.class );
            theMeshBaseIdentifierFactory = theAppContext.findContextObject( MeshBaseIdentifierFactory.class );
            theMainMeshBase              = theAppContext.findContextObjectOrThrow( MeshBase.class );

            StringRepresentationDirectory dir = theAppContext.findContextObjectOrThrow( StringRepresentationDirectory.class );

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
        s = s.trim();

        ModelBase          modelBase  = theMainMeshBase.getModelBase();
        MeshTypeIdentifier identifier = modelBase.getMeshTypeIdentifierFactory().fromExternalForm( s );
        MeshType           ret        = modelBase.findMeshTypeByIdentifier( identifier );
        return ret;
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
     * The Context to use.
     */
    protected Context theAppContext;

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

    /**
     * The set of vars to take from the incomingg request, instead of from HTTP POST.
     */
    protected HashSet<String> thePreferVarsFromRequest = null; // only initialized if needed

    /**
     * Name of the filter parameter that indicates which vars to take from the incoming request.
     * The value is a comma-separated list.
     */
    public static final String PREFER_VARS_FROM_REQUEST_PAR_NAME = "prefer-vars-from-request";

    /**
     * Special value that indicates a field should have been set (e.g. by JavaScript) but wasn't.
     */
    static final String UNASSIGNED_VALUE = ResourceHelper.getInstance( HttpShellFilter.class ).getResourceStringOrDefault( "UnassignedValue", "?" );
}
