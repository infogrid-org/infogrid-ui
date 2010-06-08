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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.security.aclbased;

import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.model.AclBasedSecurity.AclBasedSecuritySubjectArea;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.http.SaneRequestUtils;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringifierException;

/**
 * Prints ACL information of a given MeshObject.
 */
public class PrintAclTag
    extends
        AbstractRestInfoGridBodyTag
{
    private static final Log  log              = Log.getLogInstance( PrintAclTag.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public PrintAclTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theTestObject            = null;
        theTestObjectName        = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the testObject property.
     *
     * @return value of the testObject property
     * @see #setTestObject
     */
    public final String getTestObject()
    {
        return theTestObject;
    }

    /**
     * Set value of the testObject property.
     *
     * @param newValue new value of the testObject property
     * @see #getTestObject
     */
    public final void setTestObject(
            String newValue )
    {
        theTestObject = newValue;
    }

    /**
     * Obtain value of the testObjectName property.
     *
     * @return value of the testObjectName property
     * @see #setTestObjectName
     */
    public final String getTestObjectName()
    {
        return theTestObjectName;
    }

    /**
     * Set value of the testObjectName property.
     *
     * @param newValue new value of the testObjectName property
     * @see #getTestObjectName
     */
    public final void setTestObjectName(
            String newValue )
    {
        theTestObjectName = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj;
        if( theTestObject != null ) {
            obj = findMeshObjectOrThrow( theTestObject );
        } else {
            if( theTestObjectName == null ) {
                theTestObjectName = JeeViewlet.SUBJECT_ATTRIBUTE_NAME;
            }
            obj = (MeshObject) lookupOrThrow( theTestObjectName );
        }

        Integer nextId = (Integer) lookup( INSTANCE_ID_PAR_NAME );
        if( nextId == null ) {
            nextId = 1;
        }

        String divId = INSTANCE_ID_PAR_NAME + nextId;
        print( "<div class=\"" );
        print( getClass().getName().replace( ".", "-" ));
        print( "\" id=\"" );
        print( divId );
        println( "\">" );
        println( theResourceHelper.getResourceStringWithArguments( "Header", divId ));
        println( "<table class=\"rights\">" );
        println( "<tr>" );
        println( theResourceHelper.getResourceStringOrNull( "OwnerLabel" ));
        println( "<td>" );

        pageContext.getRequest().setAttribute( INSTANCE_ID_PAR_NAME, nextId+1 );

        MeshObjectSet owners = obj.traverse( AclBasedSecuritySubjectArea.MESHOBJECT_HASOWNER_MESHOBJECT.getSource() );
        if( owners.isEmpty() ) {
            println( theResourceHelper.getResourceStringOrNull( "NoOwner" ));
        } else {
            Iterator<MeshObject> iter = owners.iterator();
            while( iter.hasNext() ) {
                MeshObject current = iter.next();

                printMeshObject( current );

                if( iter.hasNext() ) {
                    println( theResourceHelper.getResourceStringOrNull( "Separator" ));
                }
            }
        }
        println( "</td>" );
        println( "</tr>" );

        println( "<tr>" );
        println( theResourceHelper.getResourceStringOrNull( "ProtectionDomainLabel" ));
        println( "<td>" );
        MeshObject domain = obj.traverse( AclBasedSecuritySubjectArea.PROTECTIONDOMAIN_GOVERNS_MESHOBJECT.getDestination() ).getSingleMember();
        if( domain == null ) {
            println( theResourceHelper.getResourceStringOrNull( "NoProtectionDomain" ));
        } else {
            printMeshObject( domain );
        }
        println( "</td>" );
        println( "</tr>" );

        if( domain != null ) {
            MeshObjectSet principalSet = domain.traverse( AclBasedSecuritySubjectArea.MESHOBJECT_HASACCESSTO_PROTECTIONDOMAIN.getDestination() );
            if( !principalSet.isEmpty() ) {
                println( "<tr>" );
                println( theResourceHelper.getResourceStringOrNull( "PrincipalsLabel" ));
                println( "<td>" );

                Iterator<MeshObject> iter = principalSet.iterator();
                while( iter.hasNext() ) {
                    MeshObject current = iter.next();

                    printMeshObject( current );

                    String sep = null;
                    try {
                        print( theResourceHelper.getResourceStringOrNull( "BeginAccessRights" ));
                        if( domain.isRelated( AclBasedSecuritySubjectArea.MESHOBJECT_HASREADACCESSTO_PROTECTIONDOMAIN.getDestination(), current )) {
                            // print( sep );
                            print( theResourceHelper.getResourceStringOrNull( "ReadAccessRights" ));
                            sep = theResourceHelper.getResourceStringOrNull( "Separator" );
                        }
                        if( domain.isRelated( AclBasedSecuritySubjectArea.MESHOBJECT_HASUPDATEACCESSTO_PROTECTIONDOMAIN.getDestination(), current )) {
                            print( sep );
                            print( theResourceHelper.getResourceStringOrNull( "UpdateAccessRights" ));
                            // sep = theResourceHelper.getResourceStringOrNull( "Separator" );
                        }
                        print( theResourceHelper.getResourceStringOrNull( "EndAccessRights" ));
                    } catch( NotRelatedException ex ) {
                        log.error( ex );
                    }

                }
                println( "</td>" );
                println( "</tr>" );
            }
        }
        println( "</table>" );
        println( "</div>" );

        StructuredResponse theResponse = (StructuredResponse) lookupOrThrow( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        String             contextPath = ((HttpServletRequest)pageContext.getRequest()).getContextPath();

        StringBuilder js = new StringBuilder();
        js.append( "<script src=\"" );
        js.append( contextPath );
        js.append( "/v/org/infogrid/jee/taglib/candy/ToggleCssClass.js\" type=\"text/javascript\"></script>\n" );

        StringBuilder css = new StringBuilder();
        css.append( "<link rel=\"stylesheet\" href=\"" );
        css.append( contextPath );
        css.append( "/v/" );
        css.append( getClass().getName().replace( '.' , '/' ));
        css.append( ".css" );
        css.append( "\" />\n" );

        TextStructuredResponseSection headSection = theResponse.obtainTextSection( StructuredResponse.HTML_HEAD_SECTION );
        if( !headSection.containsContent( css.toString() )) {
            headSection.appendContent( css.toString() );
        }
        if( !headSection.containsContent( js.toString() )) {
            headSection.appendContent( js.toString() );
        }
        return SKIP_BODY;
    }

    /**
     * Helper method to print one object.
     *
     * @param obj the MeshObject to print
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected void printMeshObject(
            MeshObject obj )
        throws
            JspException,
            IgnoreException
    {
        String text;

        try {
            text = ((RestfulJeeFormatter)theFormatter).formatMeshObjectLinkStart( pageContext, obj, null, null, null, "Html" );
            print( text );
            text = ((RestfulJeeFormatter)theFormatter).formatMeshObject( pageContext, obj, "Html", -1, true );
            print( text );
            text = ((RestfulJeeFormatter)theFormatter).formatMeshObjectLinkEnd( pageContext, obj, "Html" );
            print( text );
        } catch( StringifierException ex ) {
            log.error( ex );
        }
    }

    /**
     * String form of the MeshObject's identifier.
     */
    protected String theTestObject;

    /**
     * Name of the bean that containsContent the MeshObject.
     */
    protected String theTestObjectName;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( PrintAclTag.class );

    /**
     * Identifies the attribute in the request context that counts up instances of this tag per request.
     */
    protected static final String INSTANCE_ID_PAR_NAME = SaneRequestUtils.classToAttributeName( PrintAclTag.class, "instanceid" );

}
