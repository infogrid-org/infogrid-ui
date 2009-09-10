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

package org.infogrid.store;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * <p>A <code>StoreValue</code> encapsulates into one Oject the data and meta-data written to
 * and read from a Store for a given key.</p>
 */
public class StoreValue
        implements
            CanBeDumped
{
    /**
     * Constructor.
     *
     * @param key the key by which this data element was found
     * @param encodingId the id of the encoding that was used to encode the data element. This must be 64 bytes or less.
     * @param timeCreated the time at which the data element was created originally
     * @param timeUpdated the time at which the data element was successfully updated the most recent time
     * @param timeRead the time at which the data element was last read by some client
     * @param timeExpires the time at which the data element expires
     * @param data the data element, expressed as a sequence of bytes
     */
    public StoreValue(
            String  key,
            String  encodingId,
            long    timeCreated,
            long    timeUpdated,
            long    timeRead,
            long    timeExpires,
            byte [] data )
    {
        theKey          = key;
        theEncodingId   = encodingId;
        theTimeCreated  = timeCreated;
        theTimeUpdated  = timeUpdated;
        theTimeRead     = timeRead;
        theTimeExpires  = timeExpires;
        theData         = data;
    }

    /**
     * Obtain the key by which this data element is stored in the Store.
     *
     * @return the key
     */
    public final String getKey()
    {
        return theKey;
    }

    /**
     * Obtain the id of the encoding that was used to encode the data element.
     * This is 64 bytes or less.
     *
     * @return the encoding id
     */
    public final String getEncodingId()
    {
        return theEncodingId;
    }

    /**
     * Obtain the time this data element was created, in <code>System.currentTimeMillis()</code> format.
     *
     * @return the time this data element was created
     */
    public long getTimeCreated()
    {
        return theTimeCreated;
    }
    
    /**
     * Obtain the time this data element was last updated, in <code>System.currentTimeMillis()</code> format.
     *
     * @return the time this data element was last updated
     */
    public long getTimeUpdated()
    {
        return theTimeUpdated;
    }
    
    /**
     * Obtain the time this data element was last read, in <code>System.currentTimeMillis()</code> format.
     *
     * @return the time this data element was last read
     */
    public long getTimeRead()
    {
        return theTimeRead;
    }

    /**
     * Obtain the time this data element expires, in <code>System.currentTimeMillis()</code> format.
     *
     * @return if positive, the time when this data element will expire.
     */
    public long getTimeExpires()
    {
        return theTimeExpires;
    }

    /**
     * The data, as a sequence of bytes.
     *
     * @return the data, as a sequence of bytes
     */
    public byte [] getData()
    {
        return theData;
    }
    
    /**
     * Alternate method to retrieve the data, as an InputStream.
     *
     * @return the data, as an <code>InputStream</code>.
     */
    public InputStream getDataAsStream()
    {
        return new ByteArrayInputStream( theData );
    }

    /**
     * Determine whether this data element is expired.
     *
     * @return true if this data element is expired
     */
    public boolean isExpired()
    {
        if( theTimeExpires <= 0 ) {
            return false;
        }

        long now = System.currentTimeMillis();
        if( now < theTimeExpires ) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Determine equality.
     *
     * @param other the Object to compare to
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof StoreValue )) {
            return false;
        }
        StoreValue realOther = (StoreValue) other;

        if( !theKey.equals( realOther.theKey )) {
            return false;
        }
        if( !theEncodingId.equals( realOther.theEncodingId )) {
            return false;
        }
        if( theTimeCreated != realOther.theTimeCreated ) {
            return false;
        }
        if( theTimeUpdated != realOther.theTimeUpdated ) {
            return false;
        }
        if( theTimeRead != realOther.theTimeRead ) {
            return false;
        }
        if( theTimeExpires != realOther.theTimeExpires ) {
            return false;
        }
        if( theData.length != realOther.theData.length ) {
            return false;
        }
        for( int i=0 ; i<theData.length ; ++i ) {
            if( theData[i] != realOther.theData[i] ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return theKey.hashCode();
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    public String toString()
    {
        StringBuilder almost = new StringBuilder();
        almost.append( getClass().getName() );
        almost.append( "{ key: " );
        almost.append( theKey );
        almost.append( " }" );
        return almost.toString();
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
                    "theKey",
                    "theEncodingId",
                    "theTimeCreated",
                    "theTimeUpdated",
                    "theTimeRead",
                    "theTimeExpires",
                    "theData",
                    "String(theData)"
                },
                new Object[] {
                    theKey,
                    theEncodingId,
                    theTimeCreated,
                    theTimeUpdated,
                    theTimeRead,
                    theTimeExpires,
                    theData,
                    new String( theData )
                });
    }

    /**
     * The key.
     */
    protected String theKey;

    /**
     * The id of the encoding.
     */
    protected String theEncodingId;

    /**
     * The time the data element was created.
     */
    protected long theTimeCreated;
    
    /**
     * The time the data element was last updated.
     */
    protected long theTimeUpdated;
    
    /**
     * The time the data element was last read.
     */
    protected long theTimeRead;
   
    /**
     * The time the data element will expire (if positive).
     */
    protected long theTimeExpires;

    /**
     * The data, as a sequence of bytes.
     */
    protected byte [] theData;
}
