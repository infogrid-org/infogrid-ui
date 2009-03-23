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

package org.infogrid.meshbase.net.xpriso;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;

/**
 * Factors out functionality common to XprisoMessage implementations.
 */
public abstract class AbstractXprisoMessage
        implements
            XprisoMessage
{
    /**
     * Constructor. This does not pass the requestId and responseId as they
     * are set by the communications framework.
     * 
     * @param sender identifies the sender of this message
     * @param receiver identifies the receiver of this message
     */
    protected AbstractXprisoMessage(
            NetMeshBaseIdentifier sender,
            NetMeshBaseIdentifier receiver )
    {
        theSenderIdentifier   = sender;
        theReceiverIdentifier = receiver;
    }

    /**
     * Obtain the NetMeshBaseIdentifier of the sender.
     * 
     * @return the sender's NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getSenderIdentifier()
    {
        return theSenderIdentifier;
    }
    
    /**
     * Obtain the NetMeshBaseIdentifier of the receiver.
     * 
     * @return the receiver's NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getReceiverIdentifier()
    {
        return theReceiverIdentifier;
    }

    /**
     * Obtain the request ID.
     *
     * @return the request ID
     */
    public long getRequestId()
    {
        return theRequestId;
    }

    /**
     * Obtain the response ID.
     *
     * @return the response ID
     */
    public long getResponseId()
    {
        return theResponseId;
    }

    /**
     * Determine whether or not to cease communications after this message.
     *
     * @return if true, cease communications
     */
    public boolean getCeaseCommunications()
    {
        return theCeaseCommunications;
    }
    
    /**
     * Default implementation of equals().
     *
     * @param other the Object to compare against
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof XprisoMessage )) {
            return false;
        }
        XprisoMessage realOther = (XprisoMessage) other;

        // alphabetically, so we can make sure we have all of them by comparing with the IDE
        if( theCeaseCommunications != realOther.getCeaseCommunications()) {
            return false;
        }
        if( !arraysEquals( getConveyedMeshObjects(), realOther.getConveyedMeshObjects() )) {
            return false;
        }
        if( !arraysEquals( getDeletions(), realOther.getDeletions() )) {
            return false;
        }
        if( !arraysEquals( getEquivalentsAdditions(), realOther.getEquivalentsAdditions() )) {
            return false;
        }
        if( !arraysEquals( getEquivalentsRemovals(), realOther.getEquivalentsRemovals() )) {
            return false;
        }
        if( !arraysEquals( getNeighborAdditions(), realOther.getNeighborAdditions() )) {
            return false;
        }
        if( !arraysEquals( getNeighborRemovals(), realOther.getNeighborRemovals() )) {
            return false;
        }
        if( !arraysEquals( getPropertyChanges(), realOther.getPropertyChanges() )) {
            return false;
        }
        if( !arraysEquals( getPushHomeReplicas(), realOther.getPushHomeReplicas() )) {
            return false;
        }
        if( !arraysEquals( getPushLockObjects(), realOther.getPushLockObjects() )) {
            return false;
        }
        if( !objectsEquals( theReceiverIdentifier, realOther.getReceiverIdentifier() )) {
            return false;
        }
        if( !arraysEquals( getReclaimedLockObjects(), realOther.getReclaimedLockObjects() )) {
            return false;
        }
        if( theRequestId != realOther.getRequestId() ) {
            return false;
        }
        if( !arraysEquals( getRequestedCanceledObjects(), realOther.getRequestedCanceledObjects() )) {
            return false;
        }
        if( !arraysEquals( getRequestedFirstTimeObjects(), realOther.getRequestedFirstTimeObjects() )) {
            return false;
        }
        if( !arraysEquals( getRequestedHomeReplicas(), realOther.getRequestedHomeReplicas() )) {
            return false;
        }
        if( !arraysEquals( getRequestedLockObjects(), realOther.getRequestedLockObjects() )) {
            return false;
        }
        if( !arraysEquals( getRequestedResynchronizeReplicas(), realOther.getRequestedResynchronizeReplicas() )) {
            return false;
        }
        if( theResponseId != realOther.getResponseId() ) {
            return false;
        }
        if( !arraysEquals( getRoleAdditions(), realOther.getRoleAdditions() )) {
            return false;
        }
        if( !arraysEquals( getRoleRemovals(), realOther.getRoleRemovals() )) {
            return false;
        }
        if( !objectsEquals( theSenderIdentifier, realOther.getSenderIdentifier() )) {
            return false;
        }
        if( !arraysEquals( getTypeAdditions(), realOther.getTypeAdditions() )) {
            return false;
        }
        if( !arraysEquals( getTypeRemovals(), realOther.getTypeRemovals() )) {
            return false;
        }
        return true;
    }

    /**
     * Default implementation for hash code.
     * 
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        // following the default NetBeans implementation here. We don't deal with a lot of content -- likely not necessary
        // because of the uniqueness of request/response and sender/receiver
        
        int hash = 5;

        hash = 79 * hash + ( int ) ( theRequestId  ^ ( theRequestId  >>> 32 ) );        
        hash = 79 * hash + ( int ) ( theResponseId ^ ( theResponseId >>> 32 ) );
        hash = 79 * hash + ( theSenderIdentifier != null ? theSenderIdentifier.hashCode() : 0 );
        hash = 79 * hash + ( theReceiverIdentifier != null ? theReceiverIdentifier.hashCode() : 0 );

        return hash;
    }

    /**
     * Check that the XprisoMessage is internally correct.
     *
     * @throws IllegalStateException thrown if the XprisoMessage is not internally correct
     */
    public void check()
            throws
                IllegalStateException
    {
        StringBuilder buf = new StringBuilder();
        String        sep = "";
        String        CR  = "\n";

        if( theSenderIdentifier == null ) {
            buf.append( sep );
            buf.append( "SenderIdentifier is null" );
            sep = CR;
        }
        if( theReceiverIdentifier == null ) {
            buf.append( sep );
            buf.append( "ReceiverIdentifier is null" );
            sep = CR;
        }
        if( theRequestId < 0 ) {
            buf.append( sep );
            buf.append( "RequestId is out of range: " );
            buf.append( theRequestId );
            sep = CR;
        }
        if( theResponseId < 0 ) {
            // FIXME I think
            buf.append( sep );
            buf.append( "ResponseId is out of range: " );
            buf.append( theResponseId );
            sep = CR;
        }
        if( buf.length() > 0 ) {
            throw new IllegalStateException( buf.toString() );
        }
    }

    /**
     * Helper method to compare two objects.
     *
     * @param one the first Object
     * @param two the second Object
     * @return true if the Objects are equal
     */
    protected boolean objectsEquals(
            Object one,
            Object two )
    {
        if( one != null ) {
            if( !one.equals( two )) {
                return false;
            }
        } else if( two != null ) {
            return false;
        }
        return true;
    }

    /**
     * Helper method to compare the content of two arrays.
     *
     * @param one first array
     * @param two second array
     * @return true if the content of the arrays is equal
     */
    protected boolean arraysEquals(
            Object [] one,
            Object [] two )
    {
        if( one != null ) {
            if( two == null ) {
                return false;
            } else if( one.length != two.length ) {
                return false;
            } else {
                for( int i=0 ; i<one.length ; ++i ) {
                    boolean found = false;
                    for( int j=0 ; j<two.length ; ++j ) {
                        if( objectsEquals( one[i], two[j] )) {
                            found = true;
                            break;
                        }
                    }
                    if( !found ) {
                        return false;
                    }
                }
            }
        } else if( two != null ) {
            return false;
        }
        return true;
    }

    /**
     * The request invocation ID.
     */
    protected long theRequestId;

    /**
     * The response invocation ID.
     */
    protected long theResponseId;

    /**
     * The return address. This is the same as the sender's address.
     */
    protected NetMeshBaseIdentifier theSenderIdentifier;
    
    /**
     * The destination address.
     */
    protected NetMeshBaseIdentifier theReceiverIdentifier;
    
    /**
     * If true, this indicates that communications should cease after this message.
     */
    protected boolean theCeaseCommunications = false;
//
//    /**
//     * Represents "not set" for the request or response id.
//     */
//    public static final int NO_ID = -1;
}
