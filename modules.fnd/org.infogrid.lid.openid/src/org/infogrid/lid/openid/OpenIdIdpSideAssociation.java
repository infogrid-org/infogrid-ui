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

package org.infogrid.lid.openid;

import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * An association, as held by an OpenID identity provider.
 */
public class OpenIdIdpSideAssociation
        extends
            OpenIdEitherSideAssociation
        implements
            CanBeDumped
{
    /**
     * Factory method.
     *
     * @param associationHandle the unique identifier for the association
     * @param sharedSecret the secret negotiated for the association
     * @param sessionType the session type
     * @param issuedTime the time the association was created
     * @param expiryTime the time the association will expire
     * @return the created OpenIdRpSideAssociation
     */
    public static OpenIdIdpSideAssociation create(
            String  associationHandle,
            byte [] sharedSecret,
            String  sessionType,
            long    issuedTime,
            long    expiryTime )
    {
        return new OpenIdIdpSideAssociation( associationHandle, sharedSecret, sessionType, issuedTime, expiryTime );
    }

    /**
     * Constructor.
     * 
     * @param associationHandle the unique identifier for the association
     * @param sharedSecret the secret negotiated for the association
     * @param sessionType the session type
     * @param issuedTime the time the association was created
     * @param expiryTime the time the association will expire
     */
    protected OpenIdIdpSideAssociation(
            String  associationHandle,
            byte [] sharedSecret,
            String  sessionType,
            long    issuedTime,
            long    expiryTime )
    {
        super( associationHandle, sharedSecret, sessionType, issuedTime, expiryTime );
    }

    /**
     * Helper method to make sure we have a complete association.
     *
     * @throws IllegalStateException if the association is not complete
     */
    public void checkCompleteness()
        throws
            IllegalStateException
    {
        StringBuilder error = new StringBuilder();
        String        sep   = "";
        
        if( theAssociationHandle == null ) {
            error.append( sep );
            error.append( "Have no associationHandle. " );
            sep = " ";
        }
        if( theSharedSecret == null ) {
            error.append( sep );
            error.append( "Have no shared secret. " );
            sep = " ";
        } else if( theSharedSecret.length != 20 ) {
            error.append( sep );
            error.append( "Have shared secret with wrong length (" ).append( theSharedSecret.length ).append( "). " );
            sep = " ";
        }
        if( theIssuedTime < 0L ) {
            error.append( sep );
            error.append( "Have no issuedTime. " );
            sep = " ";
        }
        if( theExpiryTime < 0L ) {
            error.append( sep );
            error.append( "Have no expiryTime. " );
            sep = " ";
        }

        if( error.length() > 0 ) {
            String errorString = error.toString();

            throw new IllegalStateException( errorString );
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
                    "theAssociationHandle",
                    "theIssuedTime",
                    "theExpiryTime",
                    "theSharedSecret"
                },
                new Object[] {
                    theAssociationHandle,
                    theIssuedTime,
                    theExpiryTime,
                    theSharedSecret
                });
    }
}
