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

package org.infogrid.util.text;

import java.util.Map;
import org.infogrid.util.ArrayFacade;

/**
 * A MessageStringifier that works on Objects and does not require more specific subclasses.
 */
public class AnyMessageStringifier
        extends
            MessageStringifier
{
    /**
     * Factory method.
     * 
     * @param formatString the format string, such as "Your {0,string} items cost {1,currency}."
     * @param childStringifiers the mapping between symbolic names (e.g. "string", "currency") to the actual Stringifier
     * @return the AnyMessageStringifier
     * @throws CompoundStringifierCompileException thrown if the format string could not be compiled
     */
    public static AnyMessageStringifier create(
            String                     formatString,
            Map<String,Stringifier<?>> childStringifiers )
        throws
            CompoundStringifierCompileException
    {
        AnyMessageStringifier ret = new AnyMessageStringifier( formatString, childStringifiers );
        ret.compile();
        return ret;
    }
    
    /**
     * Constructor.
     *
     * @param formatString the format string, such as "Your {0,string} items cost {1,currency}."
     * @param childStringifiers the mapping between symbolic names (e.g. "string", "currency") to the actual Stringifier
     */
    protected AnyMessageStringifier(
            String                     formatString,
            Map<String,Stringifier<?>> childStringifiers )
    {
        super( formatString, childStringifiers );
    }

    /**
     * Factory method to instantiate an instance of T, given the C's from the children.
     *
     * @param childChoices the childrens' StringifierParsingChoices
     * @return this CompoundStringifier's choice
     */
    protected ArrayFacade<Object> compoundUnformat(
            StringifierParsingChoice [] childChoices )
    {
        Object [] almost = new Object[ childChoices.length ];
        for( int i=0 ; i<childChoices.length ; ++i ) {
            almost[i] = childChoices[i].unformat();
        }
        return new ArrayFacade<Object>( almost );
    }
}
