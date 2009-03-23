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

package org.infogrid.meshbase.net.xpriso.logging;

import java.util.ArrayList;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Factors out functionality common to many XprisoMessageLoggers.
 *
 * FIXME: we need to get event subscription ordering under control.
 * Once we do this, the error checking functionality can be uncommented.
 */
public abstract class AbstractXprisoMessageLogger
        implements
            XprisoMessageLogger,
            CanBeDumped
{
    /**
     * Actual logging method.
     *
     * @param msg the XprisoMessage
     * @param args other arguments
     */
    protected abstract void log(
            XprisoMessage msg,
            Object...     args );

    /**
     * Logging method for errors.
     *
     * @param msg the XprisoMessage
     * @param error the error message
     */
    protected abstract void logError(
            XprisoMessage msg,
            String        error );

    /**
     * An XprisoMessage has arrived.
     *
     * @param base the NetMeshBase at which the XprisoMessage has arrived
     * @param msg the XprisoMessage that arrived
     */
    public void messageArrived(
            NetMeshBase   base,
            XprisoMessage msg )
    {
        if( !checkReceiverOk( base, msg )) {
            logError( msg, "Received at invalid NetMeshBase " + base.getIdentifier().toExternalForm() );
        }

        if( notYetSuccessfullyArrived.contains( msg )) {
            notYetSuccessfullyArrived.remove( msg );
//        } else {
//            logError( msg, "Message arrived that had not been sent" );
        }

        log( msg );
    }

    /**
     * An XprisoMessage is about to be sent.
     *
     * @param base the NetMeshBase sending the message
     * @param msg the XprisoMessage to be sent
     */
    public void messageToBeSent(
            NetMeshBase                 base,
            XprisoMessage               msg )
    {
        if( !checkSenderOk( base, msg )) {
            logError( msg, "Sent from invalid NetMeshBase " + base.getIdentifier().toExternalForm() );
        }

        notYetSuccessfullyArrived.add( msg );
        notYetSuccessfullySent.add( msg );
    }

    /**
     * An XprisoMessage has successfully been sent.
     *
     * @param base the NetMeshBase sendingthe message
     * @param msg the XprisoMessage to be sent
     */
    public void messageSentSuccessfully(
            NetMeshBase   base,
            XprisoMessage msg )
    {
        if( notYetSuccessfullySent.contains( msg )) {
            notYetSuccessfullySent.remove( msg );
//        } else {
//            logError( msg, "Message sent that has not been enqueued" );
        }
    }

    /**
     * Check that a message's sender ID corresponds to the NetMeshBase from where it
     * is being sent.
     *
     * @param base the NetMeshBase sending the message
     * @param msg the XprisoMessage
     * @return true if check passed
     */
    protected boolean checkSenderOk(
            NetMeshBase   base,
            XprisoMessage msg )
    {
        NetMeshBaseIdentifier baseIdentifier = base.getIdentifier();
        NetMeshBaseIdentifier msgIdentifier  = msg.getSenderIdentifier();

        return baseIdentifier.equals( msgIdentifier );
    }

    /**
     * Check that a message's receiver ID corresponds to the NetMeshBase to where it
     * is being sent.
     *
     * @param base the NetMeshBase receiving the message
     * @param msg the XprisoMessage
     * @return true if check passed
     */
    protected boolean checkReceiverOk(
            NetMeshBase   base,
            XprisoMessage msg )
    {
        NetMeshBaseIdentifier baseIdentifier = base.getIdentifier();
        NetMeshBaseIdentifier msgIdentifier  = msg.getReceiverIdentifier();

        return baseIdentifier.equals( msgIdentifier );
    }

    /**
     * Determine whether all messages have been successfully sent and received.
     *
     * @return true if all messages have been successfully sent and received
     */
    public boolean allQueuesEmpty()
    {
        if( !notYetSuccessfullySent.isEmpty() ) {
            return false;
        }
        if( !notYetSuccessfullyArrived.isEmpty() ) {
            return false;
        }
        return true;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "notYetSuccessfullySent",
                    "notYetSuccessfullyArrived"
                },
                new Object[] {
                    notYetSuccessfullySent,
                    notYetSuccessfullyArrived
                });
    }

    /**
     * The set of XprisoMessages that have been announced as to be sent, but
     * have not been sent successfully yet.
     */
    protected ArrayList<XprisoMessage> notYetSuccessfullySent = new ArrayList<XprisoMessage>();

    /**
     * The set of XprisoMessages that have been announced as to be sent, but
     * have not arrived yet.
     */
    protected ArrayList<XprisoMessage> notYetSuccessfullyArrived = new ArrayList<XprisoMessage>();
}
