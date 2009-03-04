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

package org.infogrid.util.naming;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.HasStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;

/**
 * Helper Exception class to report Naming errors better than with the default NamingExceptions.
 */
public class NamingReportingException
        extends
            NamingException
        implements
            HasStringRepresentation
{
    private static final Log  log              = Log.getLogInstance( NamingReportingException.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     * 
     * @param name the name for which a problem occurred
     * @param ctx the naming context in which the exception occurred
     * @param cause the underlying cause for this exception -- the original NamingException
     */
    public NamingReportingException(
            String          name,
            Context         ctx,
            NamingException cause )
    {
        super( cause.getExplanation() );
        
        initCause( cause );
     
        theName          = name;
        theNamingContext = ctx;
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     * 
     * @param rep the StringRepresentation to use
     * @param context the StringRepresentationContext of this object
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation        rep,
            StringRepresentationContext context,
            int                         maxLength )
    {
        String indentString = rep.formatEntry( getClass(), "Indent", HasStringRepresentation.UNLIMITED_LENGTH );

        StringBuilder contextDump = new StringBuilder();
        
        boolean hasAppended = false;
        try {
            hasAppended = appendThisContext( rep, 0, theNamingContext, "java:comp/env", indentString, contextDump );
            
        } catch( NamingException ex ) {
            log.warn( ex );
            contextDump.append( "[naming exception occurred]" );
        }
        if( !hasAppended ) {
            contextDump.append( rep.formatEntry( getClass(), "NoBindings", HasStringRepresentation.UNLIMITED_LENGTH ));
        }
        
        String ret = rep.formatEntry( getClass(), "String", maxLength, theName, contextDump.toString(), this );
        return ret;
    }
    
    /**
     * Recursively invoked helper method to dump a context.
     * 
     * @param rep the StringRepresentation to use
     * @param indentLevel the number of indents to make
     * @param ctx the current naming Context
     * @param nameFilter the name filter to use when listing bindings in the context
     * @param indentString the String to prepend for one level of ident
     * @param buf the StringBuilder to append to
     * @return false if nothing has been appended
     * @throws NamingException a naming problem occurred
     */
    protected boolean appendThisContext(
            StringRepresentation rep,
            int                  indentLevel,
            Context              ctx,
            String               nameFilter,
            String               indentString,
            StringBuilder        buf )
        throws
            NamingException
    {
        boolean ret = false;

        NamingEnumeration<Binding> iter = ctx.listBindings( nameFilter );
        while( iter.hasMore() ) {
            Binding current = iter.next();

            String name      = current.getName();
            String className = current.getClassName();

            StringBuilder indent = new StringBuilder();
            for( int i=0 ; i<indentLevel ; ++i ) {
                indent.append( indentString );
            }
            buf.append( rep.formatEntry(
                    getClass(),
                    "Binding",
                    HasStringRepresentation.UNLIMITED_LENGTH,
                    indent.toString(),
                    name,
                    className ));
            
            Object child = current.getObject();
            if( child instanceof Context ) {
                appendThisContext( rep, indentLevel+1, (Context) child, "", indentString, buf );
            }
            ret = true;
        } 
        return ret;
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param additionalArguments additional arguments for URLs, if any
     * @param target the HTML target, if any
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            String                      additionalArguments,
            String                      target,
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }

    /**
     * Obtain the end part of a String representation of this MeshBase that acts
     * as a link/hyperlink and can be shown to the user.
     * 
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
    {
        return "";
    }

    /**
     * The name for which the problem occurred.
     */
    protected String theName;

    /**
     * The naming Context in which the problem occurred.
     */
    protected Context theNamingContext;
}
