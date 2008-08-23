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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import org.infogrid.util.Factory;
import org.infogrid.util.logging.Log;

/**
 * Factory for LidCredentialTypes based on their names.
 */
public class LidCredentialTypeFactory
    implements
        Factory<String,LidCredentialType,Void>
{
    private static final Log log = Log.getLogInstance( LidCredentialTypeFactory.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @param mappings the mappings from credential type name to Class that can be instantiated of that credential type
     * @return the created LidCredentialTypeFactory
     */
    public static LidCredentialTypeFactory create(
            Map<String,Class<? extends LidCredentialType>> mappings )
    {
        LidCredentialTypeFactory ret = new LidCredentialTypeFactory( mappings );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param mappings the mappings from credential type name to Class that can be instantiated of that credential type
     */
    protected LidCredentialTypeFactory(
            Map<String,Class<? extends LidCredentialType>> mappings )
    {
        theMappings = mappings;
    }

    /**
     * Factory method. This is equivalent to specifying a null argument.
     *
     * @param key the key information required for object creation, if any
     * @return the created object
     * @throws UnknownLidCredentialTypeException thrown if the given credential type is not known
     */
    public LidCredentialType obtainFor(
            String key )
        throws
            UnknownLidCredentialTypeException
    {
        return obtainFor( key, null );
    }

    /**
     * Factory method.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws UnknownLidCredentialTypeException thrown if the given credential type is not known
     */
    public LidCredentialType obtainFor(
            String key,
            Void   argument )
        throws
            UnknownLidCredentialTypeException
    {
        Class<? extends LidCredentialType> type = theMappings.get( key );
        if( type == null ) {
            throw new UnknownLidCredentialTypeException( key );
        }
        
        try {
            Method factoryMethod = type.getDeclaredMethod( "create", String.class );
            Object ret           = factoryMethod.invoke( null, key );
        
            return (LidCredentialType) ret;

        } catch( NoSuchMethodException ex ) {
            log.error( ex );
        } catch( IllegalAccessException ex ) {
            log.error( ex );
        } catch( InvocationTargetException ex ) {
            log.error( ex );
        } catch( ClassCastException ex ) {
            log.error( ex );
        }
        return null;
    }
    
    /**
     * The mappings from name to LidCredentialType classes.
     */
    protected Map<String,Class<? extends LidCredentialType>> theMappings;
}
