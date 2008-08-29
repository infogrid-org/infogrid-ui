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

package org.infogrid.lid;

/**
 * Maps local usernames into identity URLs by prefixing or postfixing
 * a constant character string.
 */
public class PrePostfixTranslatingLidIdentityManager
        extends
            TranslatingLidIdentityManager
{
    /**
     * Factory method.
     *
     * @param prefix the prefix, if any
     * @param postfix the postfix, if any
     * @param delegate the delegate LidIdentityManager
     * @return the created PrePostfixTranslatingLidIdentityManager
     */
    public static PrePostfixTranslatingLidIdentityManager create(
            String             prefix,
            String             postfix,
            LidIdentityManager delegate )
    {
        PrePostfixTranslatingLidIdentityManager ret = new PrePostfixTranslatingLidIdentityManager(
                prefix,
                postfix,
                delegate );

        return ret;
    }

    /**
     * Constructor for subclasses only.
     * 
     * @param prefix the prefix, if any
     * @param postfix the postfix, if any
     * @param delegate the delegate LidIdentityManager
     */
    protected PrePostfixTranslatingLidIdentityManager(
            String             prefix,
            String             postfix,
            LidIdentityManager delegate )
    {
        super( delegate );
        
        thePrefix  = prefix;
        thePostfix = postfix;
    }

    /**
     * Translate the identifier as used by this class into the identifier as used by the delegate.
     * 
     * @param identifier input parameter
     * @return translated identifier
     */
    protected String translateIdentifierForward(
            String identifier )
    {
        if( thePrefix != null && !identifier.startsWith( thePrefix )) {
            throw new IllegalArgumentException( "identifier " + identifier + " does not start with prefix " + thePrefix );
        }
        if( thePostfix != null && !identifier.endsWith( thePostfix )) {
            throw new IllegalArgumentException( "identifier " + identifier + " does not end with postfix " + thePostfix );
        }
        
        String ret;
        if( thePrefix != null ) {
            if( thePostfix != null ) {
                ret = identifier.substring( thePrefix.length(), identifier.length() - thePostfix.length() );
            } else {
                ret = identifier.substring( thePrefix.length() );
            }
        } else if( thePostfix != null ) {
            ret = identifier.substring( 0, identifier.length() - thePostfix.length() );
        } else {
            ret = identifier;
        }
        return ret;
    }

    /**
     * Translate the identifier as used by the delegate into the identifier as used by this class.
     * 
     * @param identifier input parameter
     * @return translated identifier
     */
    protected String translateIdentifierBackward(
            String identifier )
    {
        StringBuilder ret = new StringBuilder();
        if( thePrefix != null ) {
            ret.append( thePrefix );
        }
        ret.append(  identifier );
        if( thePostfix != null ) {
            ret.append( thePostfix );
        }
        return ret.toString();
    }
    
    /**
     * The prefix.
     */
    protected String thePrefix;
    
    /**
     * The postfix.
     */
    protected String thePostfix;
}
