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

package org.infogrid.lid.credential;

import java.util.ArrayList;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.FactoryException;
import org.infogrid.util.http.SaneRequest;

/**
 * Default factory for LidCredentialTypes contained in a particular SaneRequest.
 */
public class DefaultLidCredentialTypesFactory
    extends
        AbstractFactory<SaneRequest,LidCredentialType[],Void>
    implements
        LidCredentialTypesFactory
{
    /**
     * Factory method.
     *
     * @param availableCredentialTypes set of LidCredentialTypes available to this factory
     * @return the created DefaultLidCredentialTypesFactory
     */
    public static DefaultLidCredentialTypesFactory create(
            LidCredentialType [] availableCredentialTypes )
    {
        DefaultLidCredentialTypesFactory ret = new DefaultLidCredentialTypesFactory( availableCredentialTypes );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param availableCredentialTypes set of LidCredentialTypes available to this factory
     */
    protected DefaultLidCredentialTypesFactory(
            LidCredentialType [] availableCredentialTypes )
    {
        theAvailableCredentialTypes = availableCredentialTypes;
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public LidCredentialType [] obtainFor(
            SaneRequest key,
            Void        argument )
        throws
            FactoryException
    {
        ArrayList<LidCredentialType> ret = new ArrayList<LidCredentialType>();

        for( LidCredentialType current : theAvailableCredentialTypes ) {
            if( current.isContainedIn( key )) {
                ret.add( current );
            }
        }

        return ArrayHelper.copyIntoNewArray( ret, LidCredentialType.class );
    }

    /**
     * The available LidCredentialTypes.
     */
    protected LidCredentialType [] theAvailableCredentialTypes;
}
