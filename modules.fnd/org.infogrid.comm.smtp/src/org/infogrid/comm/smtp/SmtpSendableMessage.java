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

package org.infogrid.comm.smtp;

/**
 * Captures the characteristics of a message that can be sent via SMTP.
 */
public interface SmtpSendableMessage
{
    /**
     * Obtain the sender identifier as a String.
     * 
     * @return the sender identifier as a String
     */
    public String getSenderString();
    
    /**
     * Obtain the receiver identifier as a String.
     * 
     * @return the receiver identifier as a String
     */
    public String getReceiverString();
    
    /**
     * Obtain the subject of the message.
     * 
     * @return the subject of the message
     */
    public String getSubject();
    
    /**
     * Obtain the payload of the message.
     * 
     * @return the payload of the message
     */
    public String getPayload();
}
