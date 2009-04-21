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

package org.infogrid.probe.vcard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.BooleanValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.VCard.VCardSubjectArea;
import org.infogrid.model.primitives.UnknownEnumeratedValueException;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.NonXmlStreamProbe;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.StagingMeshBaseLifecycleManager;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * This Probe knows how to read VCards (RFC 2426). It instantiates VCard, subtypes of
 * Address, and VCard_Shows_Address in the VCard subject area.
 */
public class VCardProbe
        implements
           NonXmlStreamProbe
{
    private static final Log log = Log.getLogInstance( VCardProbe.class ); // our own, private logger

    /**
     * Constructor.
     */
    public VCardProbe()
    {
    }

    /**
     * <p>Read from the InputStream and instantiate corresponding MeshObjects.</p>
     * <p>This method declares
     * many different types of Exceptions; that enables the Probe Framework to handle many
     * possible error conditions out of the box, thereby making Probe programming easier.
     * Note that many of the declared Exceptions, if actually thrown, indicate a programming
     * error in the Probe implementation (e.g. IsAbstractException).</p>
     * <p>The Probe framework invokes this method with an open Transaction on the current Thread;
     * the Probe developer does not have to worry about Transactions.</p>
     * 
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param coherenceSpecification the type of data coherenceSpecification that is requested by the application. Probe
     *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
     *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
     *         Probe must bless the Probe's HomeObject with a subtype of <code>ProbeUpdateSpecification</code> (defined
     *         in the <code>org.infogrid.model.Probe</code> Subject Area) and suitable Property
     *         values that reflect the policy.
     * @param stream the InputStream to read from
     * @param contentType the content type (MIME) if known
     * @param freshMeshBase the StagingMeshBase in which the corresponding MeshObjects are to be instantiated by the Probe.
     *         This StagingMeshBase is empty when passed into this call, except for the home object which always exists
     * @throws EntityBlessedAlreadyException thrown if a MeshObject was incorrectly blessed twice with the same
     *         EntityType. Throwing this typically indicates a programming error.
     * @throws EntityNotBlessedException thrown if a MeshObject was not blessed with a required EntityType.
     *         Throwing this typically indicates a programming error.
     * @throws IllegalPropertyTypeException thrown if a MeshObject did not carry a PropertyType that it needed
     *         to carry. Throwing this typically indicates a programming error.
     * @throws IllegalPropertyValueException thrown if a PropertyValue was assigned to a property that was
     *         outside of the allowed range. Throwing this typically indicates a programming error.
     * @throws IOException an input/output error occurred during execution of the Probe
     * @throws IsAbstractException thrown if an EntityType or a Relationship could not be instantiated because
     *         it was abstract. Throwing this typically indicates a programming error.
     * @throws MeshObjectIdentifierNotUniqueException thrown if the Probe developer incorrectly
     *         assigned duplicate MeshObjectsIdentifiers to created MeshObjects.
     *         Throwing this typically indicates a programming error.
     * @throws ModuleException thrown if a Module required by the Probe could not be loaded
     * @throws NotPermittedException thrown if an operation performed by the Probe was not permitted
     * @throws NotRelatedException thrown if a relationship was supposed to become blessed, but the relationship
     *         did not exist. Throwing this typically indicates a programming error.
     * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
     * @throws RelatedAlreadyException thrown if the Probe developer incorrectly attempted to
     *         relate two already-related MeshObjects. Throwing this typically indicates a programming error.
     * @throws RoleTypeBlessedAlreadyException thrown if a relationship was incorrectly blessed twice with the same
     *         RelationshipType, in the same direction. Throwing this typically indicates a programming error.
     * @throws TransactionException a Transaction problem occurred. Throwing this typically indicates a programming error.
     * @throws URISyntaxException thrown if a URI was constructed in an invalid way
     * @throws StringRepresentationParseException a StringRepresentation could not be parsed
     */
    @SuppressWarnings( "fallthrough" )
    public void readFromStream(
            NetMeshBaseIdentifier  dataSourceIdentifier,
            CoherenceSpecification coherenceSpecification,
            InputStream            stream,
            String                 contentType,
            StagingMeshBase        freshMeshBase )
        throws
            IsAbstractException,
            EntityBlessedAlreadyException,
            EntityNotBlessedException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            NotRelatedException,
            MeshObjectIdentifierNotUniqueException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            TransactionException,
            NotPermittedException,
            ProbeException,
            IOException,
            ModuleException,
            URISyntaxException,
            StringRepresentationParseException
    {
        MeshObject home = freshMeshBase.getHomeObject();
        home.bless( VCardSubjectArea.VCARD );

        StagingMeshBaseLifecycleManager life = freshMeshBase.getMeshBaseLifecycleManager();
        
        // strategy:
        // 1) read the whole thing into memory, while doing so, merge "continuing" lines and
        //    break into name / parameter / value portions
        // 2) check syntax
        // 3) instantiate objects: first set properties of the home object, then create
        //    PhysicalAddress and CommucationAddress objects, set properties, and relate them to
        //    the VCard

        // part 1, read
        LineBuffer     theLineBuffer = new LineBuffer();
        BufferedReader theReader     = new BufferedReader( new InputStreamReader( stream  ));

        try {
            String currentLine = theReader.readLine(); // the line currently being assembled
            if( currentLine == null ) {
                return;
            }

            while( true ) {
                String nextLine = theReader.readLine();
                if( nextLine != null && nextLine.startsWith( " " )) {
                    currentLine = currentLine + nextLine.substring( 1 );
                    continue;
                }

                // now we have a full line in currentLine, and the beginnings of the next
                // line in nextLine
                OneLine parsed = OneLine.parse( currentLine );
                theLineBuffer.add( parsed );

                if( nextLine == null ) {
                    break;
                }

                currentLine = nextLine; // pass on
            }
        } catch( IOException ex ) {
            // do nothing if we were able to at least read something
            if( theLineBuffer.isEmpty() ) {
                throw ex;
            }
        }

        // part 2: check -- FIXME: need to check more
        if( theLineBuffer.size() < 2 ) {
            throw new ProbeException.EmptyDataSource( dataSourceIdentifier  );
        }

        OneLine current = theLineBuffer.get( 0 );
        if( ! "BEGIN".equalsIgnoreCase( current.theName )) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, "No BEGIN keyword", null  );
        }
        if( ! "VCARD".equalsIgnoreCase( current.theValue )) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, "No BEGIN:VCARD keyword", null  );
        }

        current = theLineBuffer.get( theLineBuffer.size()-1 );
        if( ! "END".equalsIgnoreCase( current.theName )) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, "No END keyword", null  );
        }
        if( ! "VCARD".equalsIgnoreCase( current.theValue )) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, "No END:VCARD keyword", null  );
        }

        // the following calls may throw IOExceptions if there isn't exactly one line with this spec
        OneLine fullNameLine = theLineBuffer.getTheOneWith(    null, "FN",       null );
        OneLine nameLine     = theLineBuffer.getTheOneWith(    null, "N",        null );
        OneLine nickNameLine = theLineBuffer.getIfPresentWith( null, "NICKNAME", null );
        // we don't do PHOTO at this time
        // OneLine bdayLine     = theLineBuffer.getIfPresentWith( null, "BDAY",     null );
        Iterator<OneLine> adrIter     = theLineBuffer.getAllWith(       null, "ADR",      null );
        // we don't do LABEL 3.2.2 at this time
        Iterator<OneLine> telIter     = theLineBuffer.getAllWith(       null, "TEL",      null );
        Iterator<OneLine> emailIter   = theLineBuffer.getAllWith(       null, "EMAIL",    null );
        // we don't do MAILER 3.3.3 at this time
        // we don't do TZ 3.4.1
        // we don't do GEO 3.4.2 at this time
        // we don't do TITLE 3.5.1 at this time
        // we don't do ROLE 3.5.2 at this time
        // we don't do LOGO at this time at this time
        // we don't do AGENT 3.5.4 at this time
        // we don't do ORG 3.5.5 at this time
        // we don't do CATEGORIES 3.6.1 at this time
        OneLine noteLine     = theLineBuffer.getIfPresentWith( null, "NOTE",     null );
        // we don't do PRODID 3.6.3 at this time
        // we don't do REV 3.6.4 at this time
        // we don't do SORT-STRING 3.6.5 at this time
        // we don't do SOUND 3.6.6 at this time
        // we don't do UID 3.6.7 at this timeh
        // we don't do URL 3.6.8 at this time
        // OneLine versionLine = theLineBuffer.getTheOneWith(    null, "VERSION",   null ); // FIXME need to check
        // we don't do 3.7.1 CLASS at this time
        // we don't do 3.7.2 KEY at this time

        // part 3: instantiate

        String [] nameComponents = StringHelper.tokenize( nameLine.theValue, ";", null );
        String familyName        = null;
        String givenName         = null;
        String additionalNames   = null;
        String honorificPrefixes = null;
        String honorificSuffixes = null;

        switch( nameComponents.length ) {
            case 5:
                honorificSuffixes = nameComponents[4]; // no break
            case 4:
                honorificPrefixes = nameComponents[3]; // no break
            case 3:
                additionalNames = nameComponents[2]; // no break
            case 2:
                givenName = nameComponents[1]; // no break
            case 1:
                familyName = nameComponents[0]; // no break
            default:
                // do nothing
        }

        if( fullNameLine != null && fullNameLine.theValue != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_FULLNAME, StringValue.create( fullNameLine.theValue ));
        }
        if( familyName != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_FAMILYNAME, StringValue.create( familyName ));
        }
        if( givenName != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_GIVENNAME, StringValue.create( givenName ));
        }
        if( additionalNames != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_ADDITIONALNAMES, StringValue.create( additionalNames ));
        }
        if( honorificPrefixes != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_HONORIFICPREFIXES, StringValue.create( honorificPrefixes ));
        }
        if( honorificSuffixes != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_HONORIFICSUFFIXES, StringValue.create( honorificSuffixes ));
        }

        if( nickNameLine != null && nickNameLine.theValue != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_NICKNAME, StringValue.create( nickNameLine.theValue ));
        }
        // no note right now
        if( noteLine != null && noteLine.theValue != null ) {
            home.setPropertyValue( VCardSubjectArea.VCARD_NOTE, StringValue.create( noteLine.theValue ));
        }

        // for all the addresses that we found
        for( int i=0 ; adrIter.hasNext() ; ++i ) {
            OneLine adrLine = adrIter.next();

            String [] adrComponents = StringHelper.tokenize( adrLine.theValue, ";", null );
            String postOfficeBox   = null;
            String extendedAddress = null;
            String streetAddress   = null;
            String locality        = null;
            String region          = null;
            String postalCode      = null;
            String countryName     = null;

            switch( adrComponents.length ) {
                case 7:
                    countryName = adrComponents[6]; // no break
                case 6:
                    postalCode = adrComponents[5]; // no break
                case 5:
                    region = adrComponents[4]; // no break
                case 4:
                    locality = adrComponents[3]; // no break
                case 3:
                    streetAddress = adrComponents[2]; // no break
                case 2:
                    extendedAddress = adrComponents[1]; // no break
                case 1:
                    postOfficeBox = adrComponents[0]; // no break
                default:
                    // do nothing
            }

            // instantiate a new PhysicalAddress object. We use the location in the
            // file as an object identifier

            MeshObject adr = life.createMeshObject(
                    freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( "ph-" + i ),
                    VCardSubjectArea.PHYSICALADDRESS );

            if( postOfficeBox != null ) {
                adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_POSTOFFICEBOX, StringValue.create( postOfficeBox ));
            }
            if( extendedAddress != null ) {
                adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_EXTENDEDADDRESS, StringValue.create( extendedAddress ));
            }
            if( streetAddress != null ) {
                adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_STREETADDRESS, StringValue.create( streetAddress ));
            }
            if( locality != null ) {
                adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_LOCALITY, StringValue.create( locality ));
            }
            if( region != null ) {
                adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_REGION, StringValue.create( region ));
            }
            if( postalCode != null ) {
                adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_POSTALCODE, StringValue.create( postalCode ));
            }
            if( countryName != null ) {
                adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_COUNTRY, StringValue.create( countryName ));
            }

            String [] params = adrLine.theParams;
            if( params.length == 1 && params[0].startsWith( "TYPE=" )) {
                params = StringHelper.tokenize( params[0].substring( "TYPE=".length()), ",", null );
            }

            for( int j=0 ; j<params.length ; ++j ) {
                String param = params[j];

                try {
                    if( "dom".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_SCOPE, VCardSubjectArea.PHYSICALADDRESS_SCOPE_type.select( "Dom" ));
                    } else if( "intl".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_SCOPE, VCardSubjectArea.PHYSICALADDRESS_SCOPE_type.select( "Intl" ));
                    } else if( "postal".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_ISPOSTAL, BooleanValue.TRUE );
                    } else if( "parcel".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.PHYSICALADDRESS_ISPARCEL, BooleanValue.TRUE );
                    } else if( "home".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.ADDRESS_ISHOME, BooleanValue.TRUE );
                    } else if( "work".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.ADDRESS_ISWORK, BooleanValue.TRUE );
                    } else if( "pref".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.ADDRESS_ISPREFERRED, BooleanValue.TRUE );
                    }
                } catch( UnknownEnumeratedValueException ex ) {
                    log.error( ex );
                }
            }

            // instantiate relationship between VCard and Address
            home.relateAndBless( VCardSubjectArea.VCARD_SHOWS_ADDRESS.getSource(), adr );
        }

        // for all the e-mails that we found
        for( int i=0 ; emailIter.hasNext() ; ++i ) {
            OneLine emailLine = emailIter.next();

            // instantiate a new CommunicationAddress object. We use the location in the
            // file as an object identifier

            MeshObject adr = life.createMeshObject(
                    freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( "co-em-" + i ),
                    VCardSubjectArea.COMMUNICATIONADDRESS );

            try {
                adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "E-mail" ) );
            } catch( UnknownEnumeratedValueException ex ) {
                log.error( ex );
            }

            String [] params = emailLine.theParams;
            if( params.length == 1 && params[0].startsWith( "TYPE=" )) {
                params = StringHelper.tokenize( params[0].substring( "TYPE=".length()), ",", null );
            }

            for( int j=0 ; j<params.length ; ++j ) {
                String param = params[j];

                if( "pref".equalsIgnoreCase( param )) {
                    adr.setPropertyValue( VCardSubjectArea.ADDRESS_ISPREFERRED, BooleanValue.TRUE );
                }
            }

            if( emailLine.theValue != null ) {
                adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_HANDLE, StringValue.create( emailLine.theValue ));
            }

            // instantiate relationship between VCard and Address
            home.relateAndBless( VCardSubjectArea.VCARD_SHOWS_ADDRESS.getSource(), adr );
        }

        // for all the phones that we found
        for( int i=0 ; telIter.hasNext() ; ++i ) {
            OneLine telLine = telIter.next();

            // instantiate a new CommunicationAddress object. We use the location in the
            // file as an object identifier

            MeshObject adr = life.createMeshObject(
                    freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( "co-ph-" + i ),
                    VCardSubjectArea.COMMUNICATIONADDRESS );

            String [] params = telLine.theParams;
            if( params.length == 1 && params[0].startsWith( "TYPE=" )) {
                params = StringHelper.tokenize( params[0].substring( "TYPE=".length()), ",", null );
            }

            for( int j=0 ; j<params.length ; ++j ) {
                String param = params[j];

                try {
                    if( "home".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.ADDRESS_ISHOME, BooleanValue.TRUE );
                    } else if( "msg".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_HASMESSAGEBOX, BooleanValue.TRUE );
                    } else if( "work".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.ADDRESS_ISWORK, BooleanValue.TRUE );
                    } else if( "pref".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.ADDRESS_ISPREFERRED, BooleanValue.TRUE );
                    } else if( "voice".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_ISVOICE, BooleanValue.TRUE );
                    } else if( "fax".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "Fax" ) );
                    } else if( "cell".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "Mobile" ) );
                    } else if( "video".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "Video" ) );
                    } else if( "pager".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "Pager" ) );
                    } else if( "bbs".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "Bbs" ) );
                    } else if( "modem".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "Modem" ) );
                    } else if( "car".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "Car" ) );
                    } else if( "isdn".equalsIgnoreCase( param )) {
                        adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_TYPE, VCardSubjectArea.COMMUNICATIONADDRESS_TYPE_type.select( "ISDN" ) );
                    }
                } catch( UnknownEnumeratedValueException ex ) {
                    log.error( ex );
                }
            }

            if( telLine.theValue != null ) {
                adr.setPropertyValue( VCardSubjectArea.COMMUNICATIONADDRESS_HANDLE, StringValue.create( telLine.theValue ));
            }

            // instantiate relationship between VCard and Address
            home.relateAndBless( VCardSubjectArea.VCARD_SHOWS_ADDRESS.getSource(), adr );
        }
    }

    /**
     * This class contains the content of one unfolded RFC 2425 line as a structure.
     */
    static class OneLine
            implements
                CanBeDumped
    {
        /**
         * Constructor with the components.
         *
         * @param group the Group field in a VCard line
         * @param name the Name field in a VCard line
         * @param params the Params field in a VCard line
         * @param value the Value field in a VCard line
         */
        public OneLine(
                String group,
                String name,
                String [] params,
                String value )
        {
            theGroup  = group;
            theName   = name;
            theParams = params;
            theValue  = value;
        }

        /**
         * Parse a line into its components.
         *
         * @param raw the string to be parsed
         * @return found and instantiated object at this line
         */
        @SuppressWarnings( "fallthrough" )
        public static OneLine parse(
                String raw )
        {
            char [] c = raw.toCharArray();

            String            group  = null;
            String            name   = null;
            ArrayList<String> params = null;
            String            value  = null;
            int dotIndex   = -1;
            int semiIndex  = -1;
            int colonIndex = -1;
            for( int i=0 ; i<c.length ; ++i ) {

                switch( c[i] ) {
                    case ':':
                        colonIndex = i;
                        // NO break here
                    case ';':
                        if( semiIndex < 0 ) {
                            if( dotIndex >= 0 ) {
                                group = raw.substring( 0, dotIndex );
                                name  = raw.substring( dotIndex+1, i );
                            } else {
                                group = null;
                                name  = raw.substring( 0, i );
                            }
                        } else if( colonIndex < 0 || c[i] == ':' ) { // further parameter
                            if( params == null ) {
                                params = new ArrayList<String>();
                            }
                            params.add( raw.substring( semiIndex+1, i ));
                        }
                        semiIndex = i;
                        break;

                    case '.':
                        if( dotIndex == -1 ) {
                            dotIndex = i;
                        }
                        break;
                }
            }
            if( colonIndex >=0 ) {
                value = raw.substring( colonIndex+1 );
            }

            return new OneLine(
                    group,
                    name,
                    params != null ? ArrayHelper.copyIntoNewArray( params, String.class ) : new String[0],
                    value );
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
                    new String [] {
                        "group",
                        "name",
                        "params",
                        "value"
                    },
                    new Object [] {
                        theGroup,
                        theName,
                        theParams,
                        theValue
                    } );
        }

        /**
         * Component of one unfolded line.
         */
        public String    theGroup;

        /**
         * Component of one unfolded line.
         */
        public String    theName;

        /**
         * Component of one unfolded line.
         */
        public String [] theParams;

        /**
         * Component of one unfolded line.
         */
        public String    theValue;
    }

    /**
     * This is basically an ArrayList with some convenience methods.
     */
    static class LineBuffer
            implements
                CanBeDumped
    {
        /**
         * Add a new line to the line buffer.
         *
         * @param newLine the line to be added
         */
        public void add(
                OneLine newLine )
        {
            store.add( newLine );
        }

        /**
         * Obtain a line at a certain position.
         *
         * @param index obtain the index-th line
         * @return the found line at this index, or null
         */
        public OneLine get(
                int index )
        {
            return store.get( index );
        }

        /**
         * Obtain one or more lines with a certain key.
         *
         * @param groupKey look for lines with this group if given, otherwise ignore as criteria
         * @param nameKey look for lines with this name if given, otherwise ignore as criteria
         * @param valueKey look for lines with this value if given, otherwise ignore as criteria
         * @return an iterator over the found lines
         */
        public Iterator<OneLine> getAllWith(
                String groupKey,
                String nameKey,
                String valueKey )
        {
            ArrayList<OneLine> almostRet = new ArrayList<OneLine>();

            for( int i=0 ; i<store.size() ; ++i ) {
                OneLine current = get( i );

                if( groupKey != null && !groupKey.equalsIgnoreCase( current.theGroup )) {
                    continue;
                }
                if( nameKey != null && !nameKey.equalsIgnoreCase( current.theName )) {
                    continue;
                }
                if( valueKey != null && !valueKey.equalsIgnoreCase( current.theValue )) {
                    continue;
                }

                almostRet.add( current );
            }

            return almostRet.iterator();
        }

        /**
         * Obtain the one line with a certain key. Throw exception if more than one line
         * matches key, or no line matches key.
         *
         * @param groupKey look for lines with this group if given, otherwise ignore as criteria
         * @param nameKey look for lines with this name if given, otherwise ignore as criteria
         * @param valueKey look for lines with this value if given, otherwise ignore as criteria
         * @return the one line found
         * @throws IOException thrown if there is 0 or more than 1 lines with this key
         */
        public OneLine getTheOneWith(
                String groupKey,
                String nameKey,
                String valueKey )
            throws
                IOException
        {
            Iterator iter = getAllWith( groupKey, nameKey, valueKey );
            if( ! iter.hasNext() ) {
                myError( "No element with", groupKey, nameKey, valueKey );
            }

            OneLine ret = (OneLine) iter.next();
            if( iter.hasNext() ) {
                myError( "More than one element with", groupKey, nameKey, valueKey );
            }
            
            return ret;
        }

        /**
         * Obtain zero or one lines with a certain key. If more than one line matches
         * key, throw an Exception.
         *
         * @param groupKey look for lines with this group if given, otherwise ignore as criteria
         * @param nameKey look for lines with this name if given, otherwise ignore as criteria
         * @param valueKey look for lines with this value if given, otherwise ignore as criteria
         * @return the one line found, or null if not found
         * @throws IOException thrown if there is more than 1 lines with this key
         */
        public OneLine getIfPresentWith(
                String groupKey,
                String nameKey,
                String valueKey )
            throws
                IOException
        {
            Iterator iter = getAllWith( groupKey, nameKey, valueKey );
            if( ! iter.hasNext() ) {
                return null;
            }

            OneLine ret = (OneLine) iter.next();
            if( iter.hasNext() ) {
                myError( "More than one element with", groupKey, nameKey, valueKey );
            }
            return ret;
        }

        /**
         * Determine whether this LineBuffer is empty.
         *
         * @return returns true if empty
         */
        public boolean isEmpty()
        {
           return store.isEmpty();
        }

        /**
         * Determine the number of lines in this LineBuffer.
         *
         * @return returns the number of lines
         */
        public int size()
        {
            return store.size();
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
                        "store"
                    },
                    new Object[] {
                        store
                    } );
        }

        /**
         * Factored-out private error reporting and exception throwing method.
         *
         * @param prefix the prefix in the line where the error occurred
         * @param groupKey the group in the line where the error occurred
         * @param nameKey the name in the line where the error occurred
         * @param valueKey the value in the line where the error occurred
         * @throws IOException always throws this exception
         */
        private static void myError(
                String prefix,
                String groupKey,
                String nameKey,
                String valueKey )
            throws
                IOException
        {
            StringBuffer msg = new StringBuffer();
            msg.append( prefix );

            boolean doComma = false;
            if( groupKey != null ) {
                msg.append( " group " );
                msg.append( groupKey );
                doComma = true;
            }
            if( nameKey != null ) {
                if( doComma ) {
                    msg.append( "," );
                }
                msg.append( " name " );
                msg.append( nameKey );
                doComma = true;
            }
            if( valueKey != null ) {
                if( doComma ) {
                    msg.append( "," );
                }
                msg.append( " value " );
                msg.append( valueKey );
                doComma = true;
            }

            throw new IOException( msg.toString() );
        }

        /**
         * The lines are stored here.
         */
        private ArrayList<OneLine> store = new ArrayList<OneLine>( 32 );
    }
}
