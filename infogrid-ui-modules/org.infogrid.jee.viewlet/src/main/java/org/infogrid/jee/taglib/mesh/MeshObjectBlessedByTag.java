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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that displays one or more types of a MeshObject.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectBlessedByTag
    extends
        AbstractRestInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectBlessedByTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObject           = null;
        theMeshObjectName       = null;
        theSupertypeName        = null;
        theSupertype            = null;
        theSeparator            = ", "; // comma as default
        theStringRepresentation = null;
        theMaxLength            = -1;
        theVarName              = null;
        theCapitalize           = null;
        theColloquial           = true;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public final Object getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public final void setMeshObject(
            Object newValue )
    {
        theMeshObject = newValue;
    }

    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the supertypeName property.
     *
     * @return value of the supertypeName property
     * @see #setSupertypeName
     */
    public String getSupertypeName()
    {
        return theSupertypeName;
    }

    /**
     * Set value of the supertypeName property.
     *
     * @param newValue new value of the supertypeName property
     * @see #getSupertypeName
     */
    public void setSupertypeName(
            String newValue )
    {
        theSupertypeName = newValue;
    }

    /**
     * Obtain value of the supertype property.
     *
     * @return value of the supertype property
     * @see #setSupertype
     */
    public String getSupertype()
    {
        return theSupertype;
    }

    /**
     * Set value of the supertype property.
     *
     * @param newValue new value of the supertype property
     * @see #getSupertype
     */
    public void setSupertype(
            String newValue )
    {
        theSupertype = newValue;
    }

    /**
     * Obtain value of the separator property.
     *
     * @return value of the separator property
     * @see #setSeparator
     */
    public String getSeparator()
    {
        return theSeparator;
    }

    /**
     * Set value of the separator property.
     *
     * @param newValue new value of the separator property
     * @see #getSeparator
     */
    public void setSeparator(
            String newValue )
    {
        theSeparator = newValue;
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
    }

    /**
     * Obtain value of the maxLength property.
     *
     * @return value of the maxLength property
     * @see #setMaxLength
     */
    public int getMaxLength()
    {
        return theMaxLength;
    }

    /**
     * Set value of the maxLength property.
     *
     * @param newValue new value of the maxLength property
     * @see #getMaxLength
     */
    public void setMaxLength(
            int newValue )
    {
        theMaxLength = newValue;
    }

    /**
     * Obtain value of the colloquial property.
     *
     * @return value of the colloquial property
     * @see #setColloquial
     */
    public boolean getColloquial()
    {
        return theColloquial;
    }

    /**
     * Set value of the colloquial property.
     *
     * @param newValue new value of the colloquial property
     * @see #getColloquial
     */
    public void setColloquial(
            boolean newValue )
    {
        theColloquial = newValue;
    }

    /**
     * Obtain value of the varName property.
     *
     * @return value of the varName property
     * @see #setVarName
     */
    public String getVarName()
    {
        return theVarName;
    }

    /**
     * Set value of the varName property.
     *
     * @param newValue new value of the varName property
     * @see #getVarName
     */
    public void setVarName(
            String newValue )
    {
        theVarName = newValue;
    }

    /**
     * Obtain value of the capitalize property.
     *
     * @return value of the capitalize property
     * @see #setCapitalize
     */
    public String getCapitalize()
    {
        return theCapitalize;
    }

    /**
     * Set value of the capitalize property.
     *
     * @param newValue new value of the capitalize property
     * @see #getCapitalize
     */
    public void setCapitalize(
            String newValue )
    {
        theCapitalize = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj  = lookupMeshObjectOrThrow( "meshObject", theMeshObject, "meshObjectName", theMeshObjectName );
        EntityType type;

        if( theSupertypeName != null ) {
            if( theSupertype != null ) {
                throw new JspException( "Must specify either supertypeName or supertype, but not both" );
            } else {
                type = (EntityType) lookup( theSupertypeName );
            }
        } else if( theSupertype != null ) {
            type = (EntityType) findMeshTypeByIdentifierOrThrow( theSupertype );
        } else {
            type = null;
        }

        StringBuilder result = null;
        if( obj != null ) {

            CAPITALIZE cap = CAPITALIZE.find( theCapitalize );

            EntityType [] allTypes = obj.getTypes();
            String        sep      = "";
            for( int i=0 ; i<allTypes.length ; ++i ) {
                if( type != null && !allTypes[i].equalsOrIsSupertype( type ) ) {
                    continue;
                }
                try {
                    String text = getFormatter().formatMeshType( pageContext, allTypes[i], theStringRepresentation, theMaxLength, theColloquial );
                    text = cap.doCapitalization( text );

                    if( result == null ) {
                        result = new StringBuilder();
                    }
                    result.append( sep );
                    result.append( text );

                    sep = theSeparator;

                } catch( StringifierException ex ) {
                    throw new JspException( ex );
                }
            }
        }
        if( result != null ) {
            if( theVarName != null ) {
                pageContext.getRequest().setAttribute( theVarName, result.toString() );
            } else {
                print( result.toString() );
            }
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Name of the bean that holds the MeshObject.
     */
    protected String theMeshObjectName;

    /**
     * The MeshObject.
     */
    protected Object theMeshObject;

    /**
     * Name of the bean that holds the supertype EntityType (mutually exclusive with theSupertype).
     */
    protected String theSupertypeName;

    /**
     * The EntityType's identifier (mutually exclusive with theSupertypeName).
     */
    protected String theSupertype;

    /**
     * The separator between multiple printed types.
     */
    protected String theSeparator;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;

    /**
     * The maximum length of an emitted String.
     */
    protected int theMaxLength;

    /**
     * Should we capitalize.
     */
    protected String theCapitalize;

    /**
     * If this is non-null, instead of emitting the resulting String, set a variable with this name instead.
     */
    protected String theVarName;

    /**
     * Recognized values for capitalization.
     */
    private static enum CAPITALIZE {
        ASIS( new String[] { "asis", "as-is" } ) {
            public String doCapitalization(
                    String candidate )
            {
                return candidate;
            }
        },
        FIRST_TOUPPER( new String[] { "firsttoupper", "first-toupper" } ) {
            public String doCapitalization(
                    String candidate )
            {
                if( candidate.length() == 0 ) {
                    return candidate;
                }
                return candidate.substring( 0, 1 ).toUpperCase() + candidate.substring( 1 );
            }
        },
        ALL_TOUPPER( new String[] { "alltoupper", "all-toupper", "toupper" } ) {
            public String doCapitalization(
                    String candidate )
            {
                return candidate.toUpperCase();
            }
        },
        FIRST_TOLOWER( new String[] { "firsttolower", "first-tolower" } ) {
            public String doCapitalization(
                    String candidate )
            {
                if( candidate.length() == 0 ) {
                    return candidate;
                }
                return candidate.substring( 0, 1 ).toLowerCase() + candidate.substring( 1 );
            }
        },
        ALL_TOLOWER( new String[] { "alltolower", "all-tolower", "tolower" } ) {
            public String doCapitalization(
                    String candidate )
            {
                return candidate.toLowerCase();
            }
        };

        /**
         * Determine the correct capitalization, given the value of the property.
         *
         * @param property the property value
         * @return the CAPITALIZE
         */
        public static CAPITALIZE find(
                String property )
        {
            if( property == null ) {
                return ASIS;
            }
            for( CAPITALIZE current : values() ) {
                for( String candidate : current.theValues ) {
                    if( property.equals( candidate )) {
                        return current;
                    }
                }
            }
            return ASIS;
        }

        /**
         * Constructor.
         *
         * @param values the values representing the policy.
         */
        CAPITALIZE(
                String [] values )
        {
            theValues = values;
        }

        /**
         * Perform the capitalization.
         *
         * @param candidate the input String
         * @return return the capitalized String
         */
        public abstract String doCapitalization(
                String candidate );

        /**
         * Values representing the policy.
         */
        protected String [] theValues;
    };

    /**
     * Should the value be outputted in colloquial form.
     */
    protected boolean theColloquial;
}
