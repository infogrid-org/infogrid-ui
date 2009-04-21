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

package org.infogrid.model.primitives;

import java.io.ObjectStreamException;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringRepresentationParseException;

/**
  * This represents a Binary Large Object DataType. It carries a MIME type, identifying its content.
  * The MIME type may be null, indicating that instances of this DataType can hold any data. If the MIME
  * type is non-null, only data of the respective MIME type can be held by instances.
  */
public final class BlobDataType
        extends
            DataType
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * This DataType allows any MIME type.
      */
    public static final BlobDataType theAnyType = create(
            BlobValue.create( new byte[0], "*/*" ),
            null,
            null,
            null );

    /**
     * This is a text DataType of any text format, plain or formatted.
     */
    public static final BlobDataType theTextAnyType = create(
            BlobValue.create( "", BlobValue.TEXT_PLAIN_MIME_TYPE ),
            "text",
            null,
            theAnyType );

    /**
     * This is a plain text DataType.
     */
    public static final BlobDataType theTextPlainType = create(
            BlobValue.create( "", BlobValue.TEXT_PLAIN_MIME_TYPE ),
            theTextAnyType );

    /**
     * This is an HTML text DataType.
     */
    public static final BlobDataType theTextHtmlType = create(
            BlobValue.create( "", BlobValue.TEXT_HTML_MIME_TYPE ),
            theTextAnyType );

    /**
     * Helper variable.
     */
    private static final String packageName;
    static{
        String className = BlobDataType.class.getName();
        packageName      = className.substring( 0, className.lastIndexOf( "." ) ).replace( '.', '/' );
    }

    /**
     * This is an image DataType whose underlying representation is supported
     * by the JDK. Currently that is GIF, JPG and PNG.
     */
    public static final BlobDataType theJdkSupportedBitmapType = create(
            BlobValue.createByLoadingFrom(
                    BlobDataType.class.getClassLoader(),
                    packageName + "/BlobDefaultValue.gif",
                    BlobValue.IMAGE_JPG_MIME_TYPE ),
            "image",
            new String[] { "gif", "jpg", "png" },
            theAnyType );

    /**
     * This is a GIF DataType.
     */
    public static final BlobDataType theGifType = create(
            BlobValue.createByLoadingFrom(
                    BlobDataType.class.getClassLoader(),
                    packageName + "/BlobDefaultValue.gif",
                    BlobValue.IMAGE_GIF_MIME_TYPE ),
            theJdkSupportedBitmapType );

    /**
     * Thisis a JPG DataType.
     */
    public static final BlobDataType theJpgType = create(
            BlobValue.createByLoadingFrom(
                    BlobDataType.class.getClassLoader(),
                    packageName + "/BlobDefaultValue.jpg",
                    BlobValue.IMAGE_JPG_MIME_TYPE ),
            theJdkSupportedBitmapType );

    /**
      * This is the default instance of this class (plain text).
      */
    public static final BlobDataType theDefault = theAnyType;

    /**
     * Factory method.
     *
     * @param defaultValue default value for instances of this DataType
     * @param mimeType major MIME type (eg. "text")
     * @param mimeSubTypes list of allowed minor MIME types (eg. "plain", "html")
     * @param superType supertype of this DataType
     * @return the created BlobDataType
     */
    public static BlobDataType create(
            BlobValue defaultValue,
            String    mimeType,
            String [] mimeSubTypes,
            DataType  superType )
    {
        return new BlobDataType( defaultValue, mimeType, mimeSubTypes, superType );
    }

    /**
     * Factory method. Derive MIME types from the default value.
     *
     * @param defaultValue default value for instances of this DataType
     * @param superType supertype of this DataType
     * @return the created BlobDataType
     */
    public static BlobDataType create(
            BlobValue defaultValue,
            DataType  superType )
    {
        String mime = defaultValue.getMimeType();
        int slash = mime.indexOf( '/' );
        if( slash < 0 ) {
            throw new IllegalArgumentException( "Illegal Mime Type " + mime );
        }
        return new BlobDataType(
                defaultValue,
                mime.substring( 0, slash ),
                new String[] { mime.substring( slash+1 ) },
                superType );
    }

    /**
     * Private constructor, use factory methods.
     *
     * @param defaultValue default value for instances of this DataType
     * @param mimeType major MIME type (eg. "text")
     * @param mimeSubTypes list of allowed minor MIME types (eg. "plain", "html")
     * @param superType supertype of this DataType
     */
    private BlobDataType(
            BlobValue defaultValue,
            String    mimeType,
            String [] mimeSubTypes,
            DataType  superType )
    {
        super( superType );

        if( defaultValue == null ) {
            throw new IllegalArgumentException( "Cannot have null default value" );
        }
        theDefaultValue = defaultValue;
        theMimeType     = mimeType;
        theMimeSubTypes = mimeSubTypes;
    }

    /**
     * Determine the fully MIME type, consisting of major and minor component.
     *
     * @return the MIME type
     */
    public String getMimeType()
    {
        StringBuffer almostRet = new StringBuffer();
        if( theMimeType == null ) {
            almostRet.append( "*" );
        } else {
            almostRet.append( theMimeType );
        }
        almostRet.append( "/" );
        if( theMimeSubTypes == null || theMimeSubTypes.length > 1 ) { // FIXME? Is this right?
            almostRet.append( "*" );
        } else {
            almostRet.append( theMimeSubTypes[0] );
        }
        return almostRet.toString();
    }

    /**
      * Test for equality.
      *
      * @param other object to test against
      * @return if true, the objects are equal
      */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof BlobDataType ) {
            BlobDataType realOther = (BlobDataType) other;

            if( theMimeType == null ) {
                if( realOther.theMimeType != null ) {
                    return false;
                }
            } else if( ! theMimeType.equals( realOther.theMimeType )) {
                return false;
            }

            if( theMimeSubTypes == null ) {
                if( realOther.theMimeSubTypes != null ) {
                    return false;
                }
            } else if( ! ArrayHelper.hasSameContentOutOfOrder( theMimeSubTypes, realOther.theMimeSubTypes, true )) {
                return false;
            }

            if( theDefaultValue == null ) {
                return realOther.theDefaultValue == null;
            }
            if( realOther.theDefaultValue == null ) {
                return false;
            }

            return theDefaultValue.equals( realOther.theDefaultValue );
        }
        return false;
    }

    /**
      * Test whether this DataType is a superset of or equals the argument
      * DataType. This is useful to test whether an assigment of
      * values is legal prior to attempting to do it.
      *
      * @param other the DataType we test against
      * @return if true, this DataType is a superset or equals the argument DataType
      */
    @Override
    public boolean isSupersetOfOrEquals(
            DataType other )
    {
        if( other instanceof BlobDataType ) {
            BlobDataType realOther = (BlobDataType) other;

            if( theMimeType == null ) {
                return true;
            }

            if( ! theMimeType.equals( realOther.theMimeType )) {
                return false;
            }

            if( theMimeSubTypes == null ) {
                return true;
            }

            return ArrayHelper.firstHasSecondAsSubset( theMimeSubTypes, realOther.theMimeSubTypes, true );
        }
        return false;
    }

    /**
     * Determine whether this PropertyValue conforms to this DataType.
     *
     * @param value the candidate PropertyValue
     * @return true if the candidate PropertyValue conforms to this type
     */
    public boolean conforms(
            PropertyValue value )
    {
        if( value instanceof BlobValue ) {
            BlobValue realValue = (BlobValue) value;

            if( theMimeType == null ) {
                return true;
            }

            if( !realValue.getMimeType().startsWith( theMimeType )) {
                return false;
            }
            if( theMimeSubTypes == null ) {
                return true;
            }

            String remainder = realValue.getMimeType().substring( theMimeType.length() );
            // FIXME? Am I missing the / or something?
            
            for( int i=0 ; i<theMimeSubTypes.length ; ++i ) {
                if( remainder.equals( theMimeSubTypes[i] )) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
      * Determine whether a domain check shall be performed on
      * assignments. Default to false.
      *
      * @return if true, a domain check shall be performed prior to performing assignments
      */
    @Override
    public boolean getPerformDomainCheck()
    {
        // the old plan:
        // if we have a mime type, we'd like to do a domain check
        // the new plan:
        return true;

        // Reasoning: if we have a PropertyType (eg Content) which
        // accepts any MIME type, but a subtype accepts only a subset (eg
        // HTMLSection) we'd get an IllegalValueException in the subtype, but
        // not in the supertype, and the compiler won't like that ...
    }

    /**
      * Return a boolean expression in the Java language that uses
      * varName as an argument and that evaluates whether the content
      * of variable varName is assignable to a value of this data type.
      *
      * @param varName the name of the variable containing the value
      * @return the boolean expression
      */
    @Override
    public String getJavaDomainCheckExpression(
            String varName )
    {
        if( theMimeType == null ) {
            return "true";
        }

        if( theMimeSubTypes == null ) {
            return varName + ".getMimeType().startsWith( \"" + theMimeType + "/\" )";
        }

        StringBuffer almostRet = new StringBuffer( 100 );
        almostRet.append( "(" );
        for( int i=0 ; i<theMimeSubTypes.length ; ++i ) {
            if( i!=0 ) {
                almostRet.append( " ||" );
            }
            almostRet.append( " \"" );
            almostRet.append( theMimeType );
            almostRet.append( "/" );
            almostRet.append( theMimeSubTypes[i] );
            almostRet.append( "\".equals( " );
            almostRet.append( varName );
            almostRet.append( ".getMimeType() )" );
        }
        almostRet.append( CLOSE_PAREN_STRING );
        return almostRet.toString();
    }

    /**
      * Obtain the Java class that can hold values of this data type.
      *
      * @return the Java class that can hold values of this data type
      */
    public Class getCorrespondingJavaClass()
    {
        return BlobValue.class;
    }

    /**
      * Instantiate this data type into a PropertyValue with a
      * reasonable default value.
      *
      * @return a PropertyValue with a reasonable default value that is an instance of this DataType
      */
    public PropertyValue instantiate()
    {
        return theDefaultValue;
    }

    /**
     * Obtain the default value of this DataType.
     *
     * @return the default value of this DataType
     */
    public PropertyValue getDefaultValue()
    {
        return theDefaultValue;
    }

    /**
     * Convert to string for debugging. This uses '?' instead of '*' as common in mime types, but if we don't
     * we can't put this into a JavaDoc comment!
     *
     * @return this object in string form
     */
    @Override
    public String toString()
    {
        return  "BlobDataType: with"
                + ((theDefaultValue == null ) ? "out" : "" )
                + " default, mime: "
                + ( ( theMimeType == null ) ? "?" : theMimeType )
                + "/"
                + ( ( theMimeSubTypes == null ) ? "?" :
                        ( (theMimeSubTypes.length == 1 ) ? theMimeSubTypes[0] :
                                ArrayHelper.arrayToString( theMimeSubTypes )) );
    }

    /**
     * Correctly deserialize a static instance.
     *
     * @return the static instance if appropriate
     * @throws ObjectStreamException thrown if reading from the stream failed
     */
    public Object readResolve()
        throws
            ObjectStreamException
    {
        if( this.equals( theDefault )) {
            return theDefault;
        } else if( this.equals( theAnyType )) {
            return theAnyType;
        } else if( this.equals( theGifType )) {
            return theGifType;
        } else if( this.equals( theJdkSupportedBitmapType )) {
            return theJdkSupportedBitmapType;
        } else if( this.equals( theJpgType )) {
            return theJpgType;
        } else if( this.equals( theTextAnyType )) {
            return theTextAnyType;
        } else if( this.equals( theTextHtmlType )) {
            return theTextHtmlType;
        } else if( this.equals( theTextPlainType )) {
            return theTextPlainType;
        } else {
            return this;
        }
    }

    /**
     * Obtain a value expression in the Java language that invokes the constructor
     * of factory method of the underlying concrete class, thereby creating or
     * reusing an instance of the underlying concrete class that is identical
     * to the instance on which this method was invoked.
     *
     * This is used primarily for code-generation purposes.
     *
     * @param classLoaderVar name of a variable containing the class loader to be used to initialize this value
     * @return the Java language expression
     */
    public String getJavaConstructorString(
            String classLoaderVar )
    {
        final String className = getClass().getName();

        if( this == theAnyType ) {
            return className + ".theAnyType";
        } else if( this == theDefault ) {
            return className + DEFAULT_STRING;
        } else if( this == theGifType ) {
            return className + ".theGifType";
        } else if( this == theJdkSupportedBitmapType ) {
            return className + ".theJdkSupportedBitmapType";
        } else if( this == theJpgType ) {
            return className + ".theJpgType";
        } else if( this == theTextAnyType ) {
            return className + ".theTextAnyType";
        } else if( this == theTextHtmlType ) {
            return className + ".theTextHtmlType";
        } else if( this == theTextPlainType ) {
            return className + ".theTextPlainType";
        } else {
            StringBuffer ret = new StringBuffer( className );
            ret.append( CREATE_STRING );
            if( theDefaultValue != null ) {
                ret.append( theDefaultValue.getJavaConstructorString( classLoaderVar, null )); // null is okay here
            } else {
                ret.append( NULL_STRING );
            }
            ret.append( COMMA_STRING );
            ret.append( QUOTE_STRING );
            ret.append( theMimeType );
            ret.append( QUOTE_STRING );
            ret.append( COMMA_STRING );

            if( theMimeSubTypes != null ) {
                ret.append( "new String[] { " );
                for( int i=0 ; i<theMimeSubTypes.length ; ++i ) {
                    ret.append( QUOTE_STRING );
                    ret.append( theMimeSubTypes[i] );
                    ret.append( QUOTE_STRING );
                    if( i<theMimeSubTypes.length-1 ) {
                        ret.append( COMMA_STRING );
                    }
                }
                ret.append( " }" );
            } else {
                ret.append( NULL_STRING );
            }

            if( theSupertype != null ) {
                ret.append( theSupertype.getJavaConstructorString( classLoaderVar ));
            } else {
                ret.append( NULL_STRING );
            }
            ret.append( " )" );

            return ret.toString();
        }
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        return rep.formatEntry(
                getClass(),
                DEFAULT_ENTRY,
                pars,
                PropertyValue.toStringRepresentation( theDefaultValue, rep, context, pars ), // presumably shorter, but we don't know
                theMimeType     != null ? theMimeType     : "*",
                theMimeSubTypes != null ? theMimeSubTypes : "*",
                theSupertype );
    }

    /**
     * Obtain a PropertyValue that corresponds to this PropertyType, based on the String representation
     * of the PropertyValue.
     * 
     * @param representation the StringRepresentation in which the String s is given
     * @param s the String
     * @return the PropertyValue
     * @throws PropertyValueParsingException thrown if the String representation could not be parsed successfully
     */
    public BlobValue fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            PropertyValueParsingException
    {
        try {
            Object [] found = representation.parseEntry( BlobValue.class, "TextString", s );

            BlobValue ret;

            switch( found.length ) {
                case 1:
                    ret = BlobValue.create( (String) found[0], determineParsedMimeType( representation, found[0], null ) );
                    break;

                case 3:
                    if( found[2] != null ) {
                        // we prefer String over byte here
                        ret = BlobValue.create( (String) found[2], determineParsedMimeType( representation, found[2], (String) found[0] ));
                    } else {
                        ret = BlobValue.create( (byte []) found[1], determineParsedMimeType( representation, found[1], (String) found[0] ));
                    }
                    break;

                default:
                    throw new PropertyValueParsingException( this, representation, null, s );
            }

            return ret;

        } catch( StringRepresentationParseException ex ) {
            throw new PropertyValueParsingException( this, representation, s, ex.getFormatString(), ex );

        } catch( ClassCastException ex ) {
            throw new PropertyValueParsingException( this, representation, s, null, ex );
        }
    }

    /**
     * Given a String representation, an optional parsed mime type and this BlobDataType, determine which MIME type it should be.
     *
     * @param representation the StringRepresentation in which the content was given
     * @param content the found content
     * @param mime the parsed mime type, if any
     * @return the correct mime type
     */
    protected String determineParsedMimeType(
            StringRepresentation representation,
            Object               content,
            String               mime )
    {
        // FIXME this needs more work
        if( mime != null ) {
            return mime;
        }
        if( content instanceof String ) {
            if( theMimeType == null || "text".equals( theMimeType )) {
                if( StringRepresentationDirectory.TEXT_HTML_NAME.equals( representation.getName() )) {
                    return BlobValue.TEXT_HTML_MIME_TYPE;
                } else {
                    return BlobValue.TEXT_PLAIN_MIME_TYPE;
                }
            }
        }
        if( theMimeType == null ) {
            return null;
        }
        StringBuilder ret = new StringBuilder();
        ret.append( theMimeType );
        ret.append( '/' );
        if( theMimeSubTypes != null && theMimeSubTypes.length > 0 ) {
            ret.append( theMimeSubTypes[0] );
        } else {
            ret.append( '*' ); // FIXME?
        }
        return ret.toString();
    }

    /**
     * The default value that goes with this DataType.
     */
    protected BlobValue theDefaultValue;

    /**
     * The major MIME type that goes with this data type. Can be null to indicate "any".
     */
    protected String theMimeType;

    /**
     * The MIME sub-types that go with this DataType. Can be null to indicate "any",
     * an array of length 1 to indicate exactly one, or a set of alternatives.
     */
    protected String [] theMimeSubTypes;
}
