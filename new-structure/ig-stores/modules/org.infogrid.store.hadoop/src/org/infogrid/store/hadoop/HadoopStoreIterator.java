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

package org.infogrid.store.hadoop;

import java.util.NoSuchElementException;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.infogrid.store.IterableStoreCursor;
import org.infogrid.store.StoreValue;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FilteringCursorIterator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.TreeFacade;
import org.infogrid.util.tree.TreeFacadeCursorIterator;

/**
 * Iterator implementation for the StoreValues in the HadoopStore.
 */
public class HadoopStoreIterator
        implements
            IterableStoreCursor
{
    private static final Log log = Log.getLogInstance( HadoopStoreIterator.class ); // our own, private logger

    /**
     * Factory method.
     * 
     * @param store the HadoopStore to iterate over
     * @return the created HadoopStoreIterator
     */
    public static HadoopStoreIterator create(
            HadoopStore store )
    {
        TreeFacade<Path>     facade   = HadoopFsPathTreeFacade.create( store.getFileSystem(), store.getTopDirectory() );
        CursorIterator<Path> delegate = TreeFacadeCursorIterator.create( facade, Path.class );
        
        FilesOnlyFilter theFilesOnlyFilter = new FilesOnlyFilter( store.getFileSystem() );
        
        delegate = FilteringCursorIterator.create( delegate, theFilesOnlyFilter, Path.class );
        
        return new HadoopStoreIterator( store, delegate );
    }

    /**
     * Constructor. Start at the beginning.
     *
     * @param store the FilesystemStore to iterate over
     * @param delegate the delegate Iterator that iterates over the file system
     */
    protected HadoopStoreIterator(
            HadoopStore          store,
            CursorIterator<Path> delegate )
    {
        theStore    = store;
        theDelegate = delegate;
    }
    
    /**
     * Obtain the next element, without iterating forward.
     *
     * @return the next element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public StoreValue peekNext()
    {
        Path       found = theDelegate.peekNext();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                ret = theStore.getStoreValueMapper().readStoreValue(
                        theStore.getFileSystem().open( found ));

            } catch( IOException ex ) {
                log.error( ex );
            }
        }
        return ret;
    }
    
    /**
     * Obtain the previous element, without iterating backwards.
     *
     * @return the previous element
     * @throws NoSuchElementException iteration has no current element (e.g. because the end of the iteration was reached)
     */
    public StoreValue peekPrevious()
    {
        Path       found = theDelegate.peekPrevious();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                ret = theStore.getStoreValueMapper().readStoreValue(
                        theStore.getFileSystem().open( found ));

            } catch( IOException ex ) {
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
        Path       found = theDelegate.next();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                ret = theStore.getStoreValueMapper().readStoreValue(
                        theStore.getFileSystem().open( found ));

            } catch( FileNotFoundException ex ) {
                // can happen with unitialized Store
            } catch( IOException ex ) {
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
        Path []       found = theDelegate.next( n );
        StoreValue [] ret   = new StoreValue[ found.length ];
        try {
            for( int i=0 ; i<found.length ; ++i ) {
                ret[i] = theStore.getStoreValueMapper().readStoreValue(
                    theStore.getFileSystem().open( found[i] ));
            }
        } catch( IOException ex ) {
            log.error( ex );
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
        Path       found = theDelegate.previous();
        StoreValue ret   = null;
        if( found != null ) {
            try {
                ret = theStore.getStoreValueMapper().readStoreValue(
                        theStore.getFileSystem().open( found ));

            } catch( IOException ex ) {
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
        Path []       found = theDelegate.previous( n );
        StoreValue [] ret   = new StoreValue[ found.length ];

        try {
            for( int i=0 ; i<found.length ; ++i ) {
                ret[i] = theStore.getStoreValueMapper().readStoreValue(
                    theStore.getFileSystem().open( found[i] ));
            }
        } catch( IOException ex ) {
            log.error( ex );
        }
        return ret;
    }

    /**
     * Move the cursor by N positions. Positive numbers indicate forward movemement;
     * negative numbers indicate backward movement. This can move all the way forward
     * to the position "past last" and all the way backward to the position "before first".
     *
     * @param n the number of positions to move
     * @throws NoSuchElementException thrown if the position does not exist
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
        if( position.hasNext() ) {
            StoreValue next = position.peekNext();
            
            Path delegateNext = theStore.getKeyFileMapper().keyToPath( next.getKey() );
            theDelegate.moveToBefore( delegateNext );
            
        } else if( position.hasPrevious() ) {
            StoreValue next = position.peekPrevious();
            
            Path delegateNext = theStore.getKeyFileMapper().keyToPath( next.getKey() );
            theDelegate.moveToAfter( delegateNext );
            
        } else {
            theDelegate.moveToBeforeFirst();
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
        Path delegatePos = theStore.getKeyFileMapper().keyToPath( key );

        int ret = theDelegate.moveToBefore( delegatePos );
        return ret;
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
        Path delegatePos = theStore.getKeyFileMapper().keyToPath( key );
        
        int ret = theDelegate.moveToAfter( delegatePos );
        return ret;
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public HadoopStoreIterator createCopy()
    {
        CursorIterator<Path> delegateCopy = theDelegate.createCopy();
        
        return new HadoopStoreIterator( theStore, delegateCopy );
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
     * The HadoopStore to iterate over.
     */
    protected HadoopStore theStore;

    /**
     * The delegate iterator.
     */
    protected CursorIterator<Path> theDelegate;
    
    /**
     * This Filter only returns regular data files, not directories.
     */
    protected static class FilesOnlyFilter
            implements
                FilteringCursorIterator.Filter<Path>
    {
        /**
         * Constructor.
         * 
         * @param fs the FileSystem to use
         */
        public FilesOnlyFilter(
                FileSystem fs )
        {
            theFileSystem = fs;
        }
                    
        /**
          * Determine whether or not to accept a candidate Object.
          *
          * @param candidate the candidate Object
          * @return true if this Object shall be accepted according to this Filter
          */
        public boolean accept(
                Path candidate )
        {
            try {
                boolean ret = !theFileSystem.getFileStatus( candidate ).isDir();
                return ret;

            } catch( FileNotFoundException ex ) {
                // can happen with an uninitialized store
            } catch( IOException ex ) {
                log.error( ex );
            }
            return false;
        }
        
        /**
         * The FileSystem to use.
         */
        protected FileSystem theFileSystem;
    }
}
