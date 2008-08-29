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

package org.infogrid.store.jets3t;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.StoreValue;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.TreeFacade;
import org.infogrid.util.tree.TreeFacadeCursorIterator;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Object;

/**
 * Iterator implementation for the StoreValues in the JetS3tStore.
 */
public class JetS3tStoreIterator
        implements
            IterableStoreCursor
{
    private static final Log log = Log.getLogInstance( JetS3tStoreIterator.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @param store the HadoopStore to iterate over
     * @return the created HadoopStoreIterator
     */
    public static JetS3tStoreIterator create(
            JetS3tStore store )
    {
        TreeFacade<S3Object>     facade   = JetS3tTreeFacade.create( store );
        CursorIterator<S3Object> delegate = TreeFacadeCursorIterator.create( facade, S3Object.class );
        
        return new JetS3tStoreIterator( store, delegate );
    }

    /**
     * Constructor. Start at the beginning.
     *
     * @param store the JetS3tStore to iterate over
     * @param delegate the delegate Iterator that iterates over the file system
     */
    protected JetS3tStoreIterator(
            JetS3tStore              store,
            CursorIterator<S3Object> delegate )
    {
        theStore    = store;
        theDelegate = delegate;
    }
    
    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @throw NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public StoreValue peekNext()
    {
        S3Object   found = theDelegate.peekNext();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                InputStream stream = new BufferedInputStream( found.getDataInputStream() );
                ret = theStore.getStoreValueMapper().readStoreValue( stream );
                stream.close();

            } catch( IOException ex ) {
                log.error( ex );
            } catch( S3ServiceException ex ) {
                log.error( ex );
            }
        }
        return ret;
    }
    
    /**
     * Obtain the previous element, without iterating backwards.
     *
     * @return the previous element
     * @throw NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public StoreValue peekPrevious()
    {
        S3Object   found = theDelegate.peekPrevious();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                InputStream stream = new BufferedInputStream( found.getDataInputStream() );
                ret = theStore.getStoreValueMapper().readStoreValue( stream );
                stream.close();

            } catch( IOException ex ) {
                log.error( ex );
            } catch( S3ServiceException ex ) {
                log.error( ex );
            }
        }
        return ret;
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
        return theDelegate.hasNext();
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
        return theDelegate.hasPrevious();
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
        return theDelegate.hasNext( n );
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
        return theDelegate.hasPrevious( n );
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     */
    public StoreValue next()
    {
        S3Object   found = theDelegate.next();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                InputStream stream = new BufferedInputStream( found.getDataInputStream() );
                ret = theStore.getStoreValueMapper().readStoreValue( stream );
                stream.close();

            } catch( IOException ex ) {
                log.error( ex );
            } catch( S3ServiceException ex ) {
                log.error( ex );
            }
        }
        return ret;
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
            int n )
    {
        S3Object []   found = theDelegate.next( n );
        StoreValue [] ret   = new StoreValue[ found.length ];
        if( found != null ) {
            try {
                for( int i=0 ; i<found.length ; ++i ) {
                    InputStream stream = new BufferedInputStream( found[i].getDataInputStream() );
                    ret[i] = theStore.getStoreValueMapper().readStoreValue( stream );
                    stream.close();
                }
            } catch( IOException ex ) {
                log.error( ex );
            } catch( S3ServiceException ex ) {
                log.error( ex );
            }
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
        S3Object   found = theDelegate.previous();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                InputStream stream = new BufferedInputStream( found.getDataInputStream() );
                ret = theStore.getStoreValueMapper().readStoreValue( stream );
                stream.close();

            } catch( IOException ex ) {
                log.error( ex );
            } catch( S3ServiceException ex ) {
                log.error( ex );
            }
        }
        return ret;
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
        S3Object []   found = theDelegate.previous( n );
        StoreValue [] ret   = new StoreValue[ found.length ];
        if( found != null ) {
            try {
                for( int i=0 ; i<found.length ; ++i ) {
                    InputStream stream = new BufferedInputStream( found[i].getDataInputStream() );
                    ret[i] = theStore.getStoreValueMapper().readStoreValue( stream );
                    stream.close();
                }
            } catch( IOException ex ) {
                log.error( ex );
            } catch( S3ServiceException ex ) {
                log.error( ex );
            }
        }
        return ret;
    }

    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backwards movement.
     * Throws NoSuchElementException if the position does not exist.
     *
     * @param n the number of positions to move
     * @throws NoSuchElementException
     */
    public void moveBy(
            int n )
        throws
            NoSuchElementException
    {
        theDelegate.moveBy( n );
    }

    /**
     * Move the cursor to just before this element, i.e. return this element when {@link #next next} is invoked
     * right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToBefore(
            StoreValue pos )
        throws
            NoSuchElementException
    {
        return moveToBefore( pos.getKey() );
    }

    /**
     * Move the cursor to just before this element, i.e. return this element when {@link #previous previous} is invoked
     * right afterwards.
     *
     * @param pos the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToAfter(
            StoreValue pos )
        throws
            NoSuchElementException
    {
        return moveToAfter( pos.getKey() );
    }

    /**
     * Removes from the underlying collection the last element returned by the
     * iterator (optional operation). This is the same as the current element.
     *
     * @throws UnsupportedOperationException if the <tt>remove</tt>
     *		  operation is not supported by this Iterator.
     
     * @throws IllegalStateException if the <tt>next</tt> method has not
     *		  yet been called, or the <tt>remove</tt> method has already
     *		  been called after the last call to the <tt>next</tt>
     *		  method.
     */
    public void remove()
    {
        theDelegate.remove();
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
        try {
            if( position.hasNext() ) {
                StoreValue next = position.peekNext();
            
                S3Object delegateNext = theStore.getService().getObject(
                        theStore.getBucket(),
                        theStore.getKeyS3KeyMapper().keyToS3Key( next.getKey() ));
                theDelegate.moveToBefore( delegateNext );
            
            } else if( position.hasPrevious() ) {
                StoreValue next = position.peekPrevious();

                S3Object delegateNext = theStore.getService().getObject(
                        theStore.getBucket(),
                        theStore.getKeyS3KeyMapper().keyToS3Key( next.getKey() ));
                theDelegate.moveToAfter( delegateNext );

            } else {
                theDelegate.moveToBeforeFirst();
            }
        } catch( S3ServiceException ex ) {
            log.error( ex );
        }
    }
    
    /**
     * Move the cursor to this element, i.e. return this element when {@link #next next} is invoked
     * right afterwards.
     *
     * @param key the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToBefore(
            String key )
        throws
            NoSuchElementException
    {
        try {
            S3Object delegatePos = theStore.getService().getObject(
                    theStore.getBucket(),
                    theStore.getKeyS3KeyMapper().keyToS3Key( key ));

            int ret = theDelegate.moveToBefore( delegatePos );
            return ret;
            
        } catch( S3ServiceException ex ) {
            log.error( ex );
            return 0;
        }
    }

    /**
     * Move the cursor to this element, i.e. return this element when {@link #previous previous} is invoked
     * right afterwards.
     *
     * @param key the element to move the cursor to
     * @return the number of steps that were taken to move. Positive number means forward, negative backward
     * @throws NoSuchElementException thrown if this element is not actually part of the collection to iterate over
     */
    public int moveToAfter(
            String key )
        throws
            NoSuchElementException
    {
        try {
            S3Object delegatePos = theStore.getService().getObject(
                    theStore.getBucket(),
                    theStore.getKeyS3KeyMapper().keyToS3Key( key ));

            int ret = theDelegate.moveToAfter( delegatePos );
            return ret;

        } catch( S3ServiceException ex ) {
            log.error( ex );
            return 0;
        }
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public JetS3tStoreIterator createCopy()
    {
        CursorIterator<S3Object> delegateCopy = theDelegate.createCopy();
        
        return new JetS3tStoreIterator( theStore, delegateCopy );
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
        int ret = theDelegate.moveToBeforeFirst();
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
        int ret = theDelegate.moveToAfterLast();
        return ret;
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
     * The JetS3tStore to iterate over.
     */
    protected JetS3tStore theStore;

    /**
     * The delegate iterator.
     */
    protected CursorIterator<S3Object> theDelegate;
}
