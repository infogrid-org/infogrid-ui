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

package org.infogrid.store.encrypted;

import java.io.IOException;
import org.infogrid.store.IterableStore;
import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.Store;
import org.infogrid.store.StoreValue;
import org.infogrid.util.CursorIterator;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.NoSuchElementException;

/**
 * An {@link EncryptedStore EncryptedStore} that is also an
 * {@link org.infogrid.store.IterableStore IterableStore}.
 */
public class IterableEncryptedStore
        extends
            EncryptedStore
        implements
            IterableStore
{
    /**
     * Factory method.
     *
     * @param transformation the name of the transformation, e.g., DES/CBC/PKCS5Padding, see <code>javax.crypto.Cipher.getInstance(String)</code>
     * @param key the secret key to be used for encryption and decryption
     * @param delegate the Store that does the actual storing
     * @return the created IterableEncryptedStore
     * @throws InvalidKeyException thrown if the key is invalid or does not match the specified transformation
     * @throws NoSuchAlgorithmException thrown if the specified transformation is not available in the default provider package or any of the other provider packages that were searched. 
     * @throws NoSuchPaddingException thrown if transformation contains a padding scheme that is not available.
     */
    public static IterableEncryptedStore create(
            String    transformation,
            SecretKey key,
            Store     delegate )
        throws
            InvalidKeyException,
            NoSuchPaddingException,
            NoSuchAlgorithmException
    {
        Cipher encCipher = Cipher.getInstance( transformation );
        Cipher decCipher = Cipher.getInstance( transformation );

        encCipher.init( Cipher.ENCRYPT_MODE, key );
        decCipher.init( Cipher.DECRYPT_MODE, key );

        return new IterableEncryptedStore( encCipher, decCipher, delegate );
    }

    /**
     * Constructor.
     *
     * @param encCipher the Cipher to use for encryption
     * @param decCipher the Cipher to use for decryption
     * @param delegate the Store that does the actual storing
     */
    protected IterableEncryptedStore(
            Cipher    encCipher,
            Cipher    decCipher,
            Store     delegate )
    {
        super( encCipher, decCipher, delegate );
    }

    /**
     * Returns an iterator over the contained StoreValues.
     * 
     * @return an Iterator.
     */
    public IterableStoreCursor iterator()
    {
        IterableStoreCursor delegateIter = ((IterableStore)theDelegate).iterator();
        
        return new MyIterator( delegateIter );
    }

    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public IterableStoreCursor getIterator()
    {
        return iterator();
    }

    /**
     * Determine the number of data elements in this Store. Some classes implementing
     * this interface may only return an approximation.
     *
     * @return the number of data elements in this Store
     * @throws IOException thrown if an I/O error occurred
     */
    public int size()
        throws
            IOException
    {
        return ((IterableStore)theDelegate).size();
    }

    /**
     * Determine the number of StoreValues in this Store whose key starts with this String
     *
     * @param startsWith the String the key starts with
     * @return the number of StoreValues in this Store whose key starts with this String
     * @throws IOException thrown if an I/O error occurred
     */
    public int size(
            String startsWith )
        throws
            IOException
    {
        return ((IterableStore)theDelegate).size( startsWith );
    }

    /**
     * Determine whether this Store is empty.
     *
     * @return true if this Store is empty
     * @throws IOException thrown if an I/O error occurred
     */
    public boolean isEmpty()
        throws
            IOException
    {
        return ((IterableStore)theDelegate).isEmpty();
    }
    
    /**
     * The Iterator implementation for this class.
     */
    class MyIterator
            implements
                IterableStoreCursor
    {
        /**
         * Constructor.
         *
         * @param delegateIter the underlying Iterator
         */
        public MyIterator(
                IterableStoreCursor delegateIter )
        {
            theDelegateIter = delegateIter;
        }

        /**
         * Obtain the next element, without iterating forward.
         *
         * @return the next element
         * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public StoreValue peekNext()
        {
            return decryptStoreValue( theDelegateIter.peekNext() );
        }

        /**
         * Obtain the previous element, without iterating backwards.
         *
         * @return the previous element
         * @exception NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
         */
        public StoreValue peekPrevious()
        {
            return decryptStoreValue( theDelegateIter.peekPrevious() );
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements in the forward direction.
         *
         * @return <tt>true</tt> if the iterator has more elements in the forward direction.
         * @see #hasPrevious()
         * @see #hasPrevious(int)
         * @see #hasNext(int)
         */
        public boolean hasNext()
        {
            return theDelegateIter.hasNext();
        }

        /**
         * Returns <tt>true</tt> if the iteration has more elements in the backwards direction.
         *
         * @return <tt>true</tt> if the iterator has more elements in the backwards direction.
         * @see #hasNext()
         * @see #hasPrevious(int)
         * @see #hasNext(int)
         */
        public boolean hasPrevious()
        {
            return theDelegateIter.hasPrevious();
        }

        /**
         * Returns <tt>true</tt> if the iteration has at least N more elements in the forward direction.
         *
         * @param n the number of elements for which to check
         * @return <tt>true</tt> if the iterator has at least N more elements in the forward direction.
         * @see #hasNext()
         * @see #hasPrevious()
         * @see #hasPrevious(int)
         */
        public boolean hasNext(
                int n )
        {
            return theDelegateIter.hasNext( n );
        }

        /**
         * Returns <tt>true</tt> if the iteration has at least N more elements in the backwards direction.
         *
         * @param n the number of elements for which to check
         * @return <tt>true</tt> if the iterator has at least N more elements in the backwards direction.
         * @see #hasNext()
         * @see #hasPrevious()
         * @see #hasNext(int)
         */
        public boolean hasPrevious(
                int n )
        {
            return theDelegateIter.hasPrevious( n );
        }

        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration.
         * @exception NoSuchElementException iteration has no more elements.
         */
        public StoreValue next()
        {
            return decryptStoreValue( theDelegateIter.next() );
        }

        /**
         * <p>Obtain the next N elements. If fewer than N elements are available, return
         * as many elements are available in a shorter array.</p>
         * 
         * @param n the number of elements to obtain
         * @return the next no more than N elements
         * @see #previous(int)
         */
        public StoreValue [] next(
                int  n )
        {
            StoreValue [] temp = theDelegateIter.next( n );
            StoreValue [] ret  = new StoreValue[ temp.length ]; // FIXME? We might want to write in place
            
            for( int i=0 ; i<temp.length ; ++i ) {
                ret[i] = decryptStoreValue( temp[i] );
            }
            return ret;
        }

        /**
         * Returns the previous element in the iteration.
         *
         * @return the previous element in the iteration.
         * @see #next()
         */
        public StoreValue previous()
        {
            return decryptStoreValue( theDelegateIter.previous() );
        }

        /**
         * <p>Obtain the previous N elements. If fewer than N elements are available, return
         * as many elements are available in a shorter array.</p>
         * 
         * <p>Note that the elements
         * will be ordered in the opposite direction as you might expect: they are
         * returned in the sequence in which the CursorIterator visits them, not in the
         * sequence in which the underlying Iterable stores them.</p>
         *
         * @param n the number of elements to obtain
         * @return the previous no more than N elements
         * @see #next(int)
         */
        public StoreValue [] previous(
                int n )
        {
            StoreValue [] temp = theDelegateIter.previous( n );
            StoreValue [] ret  = new StoreValue[ temp.length ]; // FIXME? We might want to write in place
            
            for( int i=0 ; i<temp.length ; ++i ) {
                ret[i] = decryptStoreValue( temp[i] );
            }
            return ret;
        }

        /**
         * Move the cursor by N positions. Positive numbers indicate forward movemement;
         * negative numbers indicate backwards movement.
         * Throws NoSuchElementException if the position does not exist.
         *
         * @param n the number of positions to move
         * @exception NoSuchElementException
         */
        public void moveBy(
                int n )
            throws
                NoSuchElementException
        {
            theDelegateIter.moveBy( n );
        }

        /**
         * Move the cursor to just before this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToBefore(
                StoreValue pos )
            throws
                NoSuchElementException
        {
            return theDelegateIter.moveToBefore( pos );
        }

        /**
         * Move the cursor to just after this element, i.e. return this element when {@link #previous previous} is invoked
         * right afterwards.
         *
         * @param pos the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToAfter(
                StoreValue pos )
            throws
                NoSuchElementException
        {
            return theDelegateIter.moveToAfter( pos );
        }

        /**
         * 
         * Removes from the underlying collection the last element returned by the
         * iterator (optional operation). This is the same as the current element.
         *
         * @exception UnsupportedOperationException if the <tt>remove</tt>
         *		  operation is not supported by this Iterator.

         * @exception IllegalStateException if the <tt>next</tt> method has not
         *		  yet been called, or the <tt>remove</tt> method has already
         *		  been called after the last call to the <tt>next</tt>
         *		  method.
         */
        public void remove()
        {
            theDelegateIter.remove();
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
         * right afterwards.
         *
         * @param key the key of the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToBefore(
                String key )
            throws
                NoSuchElementException
        {
            return theDelegateIter.moveToBefore( key );
        }

        /**
         * Move the cursor to this element, i.e. return this element when {@link #previous previous} is invoked
         * right afterwards.
         *
         * @param key the key of the element to move the cursor to
         * @return the number of steps that were taken to move. Positive number means forward, negative backward
         * @exception NoSuchElementException thrown if this element is not actually part of the collection to iterate over
         */
        public int moveToAfter(
                String key )
            throws
                NoSuchElementException
        {
            return theDelegateIter.moveToAfter( key );
        }

        /**
         * Clone this position.
         *
         * @return identical new instance
         */
        public MyIterator createCopy()
        {
            return new MyIterator( theDelegateIter.createCopy() );
        }
    
        /**
         * Set this CursorIterator to the position represented by the provided CursorIterator.
         *
         * @param position the position to set this CursorIterator to
         * @throws IllegalArgumentException thrown if the provided CursorIterator did not work on the same CursorIterable,
         *         or the implementations were incompatible.
         */
        public void setPositionTo(
                CursorIterator<StoreValue> position )
            throws
                IllegalArgumentException
        {
            moveToAfter( encryptStoreValue( position.next() ));
        }
    
        /**
          * Do we have more elements?
          *
          * @return true if we have more elements
          */
        public final boolean hasMoreElements()
        {
            return hasNext();
        }

        /**
          * Return next element and iterate.
          *
          * @return the next element
          */
        public final StoreValue nextElement()
        {
            return next();
        }

        /**
         * Obtain a CursorIterable instead of an Iterator.
         *
         * @return the CursorIterable
         */
        public CursorIterator<StoreValue> iterator()
        {
            return this;
        }

        /**
         * Obtain a CursorIterable. This performs the exact same operation as
         * @link #iterator iterator}, but is friendlier towards JSPs and other software
         * that likes to use JavaBeans conventions.
         *
         * @return the CursorIterable
         */
        public final CursorIterator<StoreValue> getIterator()
        {
            return iterator();
        }

        /**
         * Move the cursor to just before the first element, i.e. return the first element when
         * {@link #next next} is invoked right afterwards.
         *
         * @return the number of steps that were taken to move. Positive number means
         *         forward, negative backward
         */
        public int moveToBeforeFirst()
        {
            int ret = theDelegateIter.moveToBeforeFirst();
            return ret;
        }

        /**
         * Move the cursor to just after the last element, i.e. return the last element when
         * {@link #previous previous} is invoked right afterwards.
         *
         * @return the number of steps that were taken to move. Positive number means
         *         forward, negative backward
         */
        public int moveToAfterLast()
        {
            int ret = theDelegateIter.moveToAfterLast();
            return ret;
        }

        /**
         * The underlying Iterator.
         */
        protected IterableStoreCursor theDelegateIter;
    }
}
