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

package org.infogrid.lid.openid;

/**
 * Collects the parameters specified by a RelyingParty application for OpenID
 * association negotiation.
 */
public class OpenIdRpSideAssociationNegotiationParameters
        implements
            OpenIdConstants
{
    /**
     * Factory method.
     * 
     * @return default OpenIdRpSideAssociationNegotiationParameters
     */
    public static OpenIdRpSideAssociationNegotiationParameters createWithDefaults()
    {
        return new OpenIdRpSideAssociationNegotiationParameters(
                HMAC_SHA1,
                DH_SHA1 );
    }

    /**
     * Factory method.
     * 
     * @param wantedAssociationType the desired association type
     * @param wantedSessionType the desired session type
     * @return default OpenIdRpSideAssociationNegotiationParameters
     */
    public static OpenIdRpSideAssociationNegotiationParameters create(
            String                     wantedAssociationType,
            String                     wantedSessionType )
    {
        return new OpenIdRpSideAssociationNegotiationParameters( wantedAssociationType, wantedSessionType );
    }

    /**
     * Constructor.
     * 
     * @param wantedAssociationType the desired association type
     * @param wantedSessionType the desired session type
     */
    protected OpenIdRpSideAssociationNegotiationParameters(
            String                     wantedAssociationType,
            String                     wantedSessionType )
    {
        theWantedAssociationType = wantedAssociationType;
        theWantedSessionType     = wantedSessionType;
    }

    /**
     * Obtain the wanted association type, such as HMAC_SHA1.
     *
     * @return the wanted association type
     */
    public String getWantedAssociationType()
    {
        return theWantedAssociationType;
    } 

    /**
     * Obtain the wanted session type, such as DH_SHA1.
     *
     * @return the wanted session type
     */
    public String getWantedSessionType()
    {
        return theWantedSessionType;
    }
    
    /**
     * The wanted association type.
     */
    protected String theWantedAssociationType;

    /**
     * The wanted session type.
     */
    protected String theWantedSessionType;
}
