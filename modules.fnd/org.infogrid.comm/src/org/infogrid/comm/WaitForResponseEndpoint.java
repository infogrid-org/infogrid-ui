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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.comm;

import java.util.HashMap;
import java.util.List;
import java.lang.reflect.InvocationTargetException;
import org.infogrid.util.logging.Log;
import org.infogrid.util.RemoteQueryTimeoutException;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * A communication endpoint that suspends the thread sending a message until a
 * response has arrived. This is useful to implement RPC-style communications
 * on top of the ping-pong framework.
 * 
 * @param <T> the message type
 */
public class WaitForResponseEndpoint<T extends CarriesInvocationId>
        extends
            AbstractWaitingEndpoint<T>
        implements
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( WaitForResponseEndpoint.class ); // our own, private logger
    
    /**
     * Factory method.
     *
     * @param <T> the message type
     * @param messageEndpoint the BidirectionalMessageEndpoint to use as communications endpoint
     * @return the created WaitForResponseEndpoint
     */
    public static <T extends CarriesInvocationId> WaitForResponseEndpoint<T> create(
            BidirectionalMessageEndpoint<T> messageEndpoint )
    {
        WaitForResponseEndpoint<T> ret = new WaitForResponseEndpoint<T>( messageEndpoint );

        messageEndpoint.addWeakMessageEndpointListener( ret );
        return ret;
    }

    /**
     * Constructor.
     * 
     * @param messageEndpoint the BidirectionalMessageEndpoint to use as communications endpoint
     */
    protected WaitForResponseEndpoint(
            BidirectionalMessageEndpoint<T> messageEndpoint )
    {
        super( messageEndpoint );
    }

    /**
     * Invoke the remote procedure call.
     *
     * @param message the message that represents the argument to the call
     * @return the return value
     * @throws RemoteQueryTimeoutException thrown if the invocation timed out
     * @throws InvocationTargetException thrown if the invocation produced an Exception
     */
    public T call(
            T message )
        throws
            RemoteQueryTimeoutException,
            InvocationTargetException
    {
        return call( message, defaultTimeout );
    }
    
    /**
     * Invoke the remote procedure call.
     *
     * @param message the message that represents the argument to the call
     * @param timeout the timeout, in milliseconds, until the call times out
     * @return the return value
     * @throws RemoteQueryTimeoutException thrown if the invocation timed out
     * @throws InvocationTargetException thrown if the invocation produced an Exception
     */
    public T call(
            T    message,
            long timeout )
        throws
            RemoteQueryTimeoutException,
            InvocationTargetException
    {
        long   invocationId = createInvocationId();
        Object syncObject   = new Object();
        
        message.setRequestId( invocationId );

        if( log.isInfoEnabled() ) { // better here because here we have the invocation id set
            log.info( this + ".invoke( " + message + ", " + timeout + " )" );
        }

        synchronized( theOngoingInvocations ) {
            theOngoingInvocations.put( invocationId, syncObject );        
        }
        
        try {
            synchronized( syncObject ) {
                theMessageEndpoint.sendMessageAsap( message );
                syncObject.wait( timeout );
            }
            
        } catch( InterruptedException ex ) {
            // ignore
        }

        synchronized( theOngoingInvocations ) {
            T         receivedMessage  = theResults.remove( invocationId );
            Throwable ex               = theExceptions.remove( invocationId );

            if( log.isInfoEnabled() ) {
                StringBuilder buf = new StringBuilder();
                buf.append( this ).append( ".invoke( ... invocationId=" ).append( invocationId ).append( " ) has woken up with " );
                if( receivedMessage != null ) {
                    buf.append( "result " ).append( receivedMessage );
                } else if( ex != null ) {
                    buf.append( "exception " ).append( ex );
                } else {
                    buf.append( "null result and null exception" );
                }
                log.info( buf.toString() );
            }

            if( ex != null ) {
                throw new InvocationTargetException( ex );
            }
            if( theOngoingInvocations.remove( invocationId ) != null ) {
                throw new RemoteQueryTimeoutException.QueryIsOngoing( this, receivedMessage != null, receivedMessage );
            }

            return receivedMessage;
        }
    }
    
    /**
     * Determine whether a call is waiting for a response with the provided responseId.
     * 
     * @param responseId the responseId
     * @return true a call is waiting for this responseId
     */
    public boolean isCallWaitingFor(
            long responseId )
    {
        Object syncObject;
        synchronized( theOngoingInvocations ) {
            syncObject = theOngoingInvocations.get( responseId );
        }
        if( syncObject != null ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called when an incoming message has arrived.
     *
     * @param endpoint the BidirectionalMessageEndpoint that received the message
     * @param msg the received message
     */
    public void messageReceived(
            ReceivingMessageEndpoint<T> endpoint,
            T                           msg )
    {
        long responseId = msg.getResponseId();
        
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "messageReceived", msg );
        }

        Object syncObject;
        synchronized( theOngoingInvocations ) {
            syncObject = theOngoingInvocations.remove( responseId );
            if( syncObject != null ) {
                theResults.put( responseId, msg );
            }
        }

        if( syncObject != null ) {
            synchronized( syncObject ) {
                syncObject.notifyAll();
            }
        } else {
            otherMessageReceived( endpoint, msg );
        }
    }

    /**
     * Called when the receiving endpoint threw the EndpointIsDeadException.
     *
     * @param endpoint the BidirectionalMessageEndpoint that sent this event
     * @param msg the status of the outgoing queue
     * @param t the Throwable that caused this error, if any
     */
    public void disablingError(
            MessageEndpoint<T> endpoint,
            List<T>            msg,
            Throwable          t )
    {
        // notify all waiting threads
        
        synchronized( theOngoingInvocations ) {
            for( Long responseId : theOngoingInvocations.keySet() ) {
                Object syncObject = theOngoingInvocations.remove( responseId );
                if( syncObject != null ) {
                    theExceptions.put( responseId, t );
                    synchronized( syncObject ) {
                        syncObject.notifyAll();
                    }
                }
            }
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theOngoingInvocations",
                    "theResults",
                    "theMessageEndpoint"
                },
                new Object[] {
                    theOngoingInvocations,
                    theResults,
                    theMessageEndpoint
                });
    }

    /**
     * The ongoing invocations.
     */
    protected final HashMap<Long,Object> theOngoingInvocations = new HashMap<Long,Object>();
    
    /**
     * The assembled result(s).
     */
    protected HashMap<Long,T> theResults = new HashMap<Long,T>();
    
    /**
     * The Exceptions that resulted.
     */
    protected HashMap<Long,Throwable> theExceptions = new HashMap<Long,Throwable>();
}
