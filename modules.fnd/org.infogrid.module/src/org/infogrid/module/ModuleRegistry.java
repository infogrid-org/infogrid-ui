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

package org.infogrid.module;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

 
/**
 * A ModuleRegistry is a place where Modules can be found and resolved. There is typically
 * only one ModuleRegistry per virtual machine or application. This class is abstract; instantiate a
 * concrete subclass instead.
 */
public abstract class ModuleRegistry
{
    /**
     * Private constructor, use factory method.
     *
     * @param ads the ModuleAdvertisements found
     * @param installation the SoftwareInstallation
     */
    protected ModuleRegistry(
            ArrayList<ModuleAdvertisement> ads,
            SoftwareInstallation           installation)
    {
        theAdvertisements = ads;
        theInstallation   = installation;
    }

    /**
     * Obtain the SoftwareInstallation whose parameters are used for this ModuleRegistry.
     *
     * @return the SoftwareInstallation whose parameters are used for this ModuleRegistry
     */
    public final SoftwareInstallation getSoftwareInstallation()
    {
        return theInstallation;
    }

    /**
     * Determine whether a certain ModuleAdvertisement is locally resolved already, and if so,
     * return it. Do not attempt to resolve.
     *
     * @param adv the ModuleAdvertisement whose resolution we check
     * @return the Module to which the ModuleAdvertisement has been resolved already, or null if not
     */
    public final Module getLocalResolution(
            ModuleAdvertisement adv )
    {
        synchronized( RESOLVE_LOCK ) {
            return theModules.get( adv );
        }
    }

    /**
     * Determine the ModuleAdvertisements that are candidates to resolve a
     * Module dependency, based on the knowledge of this ModuleRegistry.
     *
     * @param req the ModuleRequirement that we attempt to resolve
     * @return the ModuleAdvertisements that are candidates for the resolution of the ModuleRequirement
     */
    public final ModuleAdvertisement [] determineResolutionCandidates(
            ModuleRequirement req )
    {
        String moduleName    = req.getRequiredModuleName();
        String moduleVersion = req.getRequiredModuleVersion();

        // we are creating a snapshot of ModuleAdvertisements here in case a concurrent Thread adds some

        ModuleAdvertisement [] adsSnapshot;
        synchronized( theAdvertisements ) {
            adsSnapshot = theAdvertisements.toArray( new ModuleAdvertisement[ theAdvertisements.size() ]);
        }

        int        count  = 0;
        boolean [] marker = new boolean[ adsSnapshot.length ];
        for( int i=0 ; i<adsSnapshot.length ; ++i ) {
            if(    moduleName.equals( adsSnapshot[i].getModuleName() )
                && (    moduleVersion == null
                     || adsSnapshot[i].getModuleVersion() == null
                     || moduleVersion.equals( adsSnapshot[i].getModuleVersion() )))
            {
                marker[i] = true;
                ++count;
            }
        }
        if( count == 0 ) {
            return null;
        }

        ModuleAdvertisement [] ret = new ModuleAdvertisement[ count ];
        for( int i=adsSnapshot.length-1 ; i>=0 ; --i ) {
            if( marker[i] ) {
                ret[--count] = adsSnapshot[i];
            }
        }
        return ret;
    }

    /**
     * Determine the one and only ModuleAdvertisement that is the only candidate to resolve a Module
     * dependency, based on the knowledge of this ModuleRegistry. If there is more or less than one
     * match, thrown an Exception.
     * 
     * @param req the ModuleRequirement that we attempt to resolve
     * @return the ModuleAdvertisements that are candidates for the resolution of the ModuleRequirement
     * @throws ModuleResolutionCandidateNotUniqueException thrown if there were fewer or more than one ModuleAdvertisement found
     */
    public final ModuleAdvertisement determineSingleResolutionCandidate(
            ModuleRequirement req )
        throws
            ModuleResolutionCandidateNotUniqueException
    {
        ModuleAdvertisement [] found = determineResolutionCandidates( req );
        if( found != null && found.length == 1 ) {
            return found[0];
        } else {
            throw new ModuleResolutionCandidateNotUniqueException( req, found );
        }
    }

    /**
     * Find a set of StandardModuleAdvertisements that provide an interface with this name.
     *
     * @param interfaceName the name of the interface for which we look
     * @param maxNumber the maximum number of StandardModuleAdvertisements to return
     * @return the found StandardModuleAdvertisements that provide this interface
     */
    public final StandardModuleAdvertisement [] findAdvertisementsForInterface(
            String interfaceName,
            int    maxNumber )
    {
        ArrayList<StandardModuleAdvertisement> almostRet   = null;
        int                                    foundNumber = 0;

        synchronized( RESOLVE_LOCK ) {
            for( ModuleAdvertisement adv : theAdvertisements ) {
                if( adv instanceof StandardModuleAdvertisement ) {
                    StandardModuleAdvertisement realAdv = (StandardModuleAdvertisement) adv;
                    boolean found;

                    if( interfaceName != null ) {
                        found = realAdv.findCapabilitiesByInterface( interfaceName ).length > 0;
                    } else {
                        found = realAdv.getSupportedCapabilities().length > 0;
                    }
                    
                    if( found ) {
                        if( almostRet == null ) {
                            almostRet = new ArrayList<StandardModuleAdvertisement>(
                                    Math.min( maxNumber, theModules.size() ));
                        }
                        almostRet.add( realAdv );
                        ++foundNumber;

                        if( foundNumber == maxNumber ) {
                            break;
                        }
                    }
                }
            }
        }
        if( almostRet == null ) {
            return new StandardModuleAdvertisement[0];
        }
        return almostRet.toArray( new StandardModuleAdvertisement[ foundNumber ]);
    }

    /**
     * Find a set of StandardModuleAdvertisements that support all the interfaces with these names.
     *
     * @param interfaceNames the names of the interfaces for which we look
     * @param maxNumber the maximum number of StandardModuleAdvertisements to return
     * @return the found StandardModuleAdvertisements that provide these interfaces
     */
    public final StandardModuleAdvertisement [] findAdvertisementsForInterfaces(
            String [] interfaceNames,
            int       maxNumber )
    {
        ArrayList<StandardModuleAdvertisement> almostRet   = null;
        int                                    foundNumber = 0;

        synchronized( RESOLVE_LOCK ) {
            for( ModuleAdvertisement adv : theAdvertisements ) {
                if( adv instanceof StandardModuleAdvertisement ) {
                    StandardModuleAdvertisement realAdv = (StandardModuleAdvertisement) adv;
                    boolean found;

                    if( interfaceNames != null && interfaceNames.length > 0 ) {
                        found = realAdv.findCapabilityByInterfaces( interfaceNames ) != null;
                    } else {
                        found = realAdv.getSupportedCapabilities().length > 0;
                    }
                    
                    if( found ) {
                        if( almostRet == null ) {
                            almostRet = new ArrayList<StandardModuleAdvertisement>(
                                    Math.min( maxNumber, theModules.size() ));
                        }
                        almostRet.add( realAdv );
                        ++foundNumber;

                        if( foundNumber == maxNumber ) {
                            break;
                        }
                    }
                }
            }
        }
        if( almostRet == null ) {
            return new StandardModuleAdvertisement[0];
        }
        return almostRet.toArray( new StandardModuleAdvertisement[ foundNumber ]);
    }

    /**
     * Recursively resolve this ModuleAdvertisement into a Module.
     *
     * @param adv the ModuleAdvertisement to resolve
     * @return the resolved Module.
     * @throws ModuleNotFoundException thrown if the Module could not be found
     * @throws ModuleResolutionException thrown if the Module could not be resolved
     */
    public final Module resolve(
            ModuleAdvertisement adv )
        throws
            ModuleNotFoundException,
            ModuleResolutionException
    {
        return resolve( adv, true );
    }

    /**
     * Resolve this ModuleAdvertisement into a Module.
     *
     * @param adv the ModuleAdvertisement to resolve
     * @param recursive resolve recursively if set to true
     * @return the resolved Module.
     * @throws ModuleNotFoundException thrown if the Module could not be found
     * @throws ModuleResolutionException thrown if the Module could not be resolved
     */
    public final Module resolve(
            ModuleAdvertisement adv,
            boolean             recursive )
        throws
            ModuleNotFoundException,
            ModuleResolutionException
    {
        synchronized( RESOLVE_LOCK ) {
            Module ret = theModules.get( adv );
            if( ret == null ) {
                File [] jarFiles;

                try {
                    jarFiles = findJarFilesFor( adv );
                } catch( IOException ex ) {
                    throw new ModuleNotFoundException( adv, ex ); // if the JAR files aren't there, resolve() fails
                }

                Module [] dependentModules = null;
                if( recursive ) {
                    ModuleRequirement [] reqs = adv.getRunTimeModuleRequirements();
                    dependentModules          = new Module[ reqs.length ];

                    for( int i=0 ; i<reqs.length ; ++i ) {
                        Module dependModule = null;
                        ModuleAdvertisement [] dependAds = determineResolutionCandidates( reqs[i] );
                        if( dependAds == null ) {
                            throw new ModuleResolutionException( adv, reqs[i], null );
                        }
                        Exception chained = null;
                        for( int j=0 ; j<dependAds.length ; ++j ) {
                            try {
                                dependModule = resolve( dependAds[j], true );
                            } catch( ModuleResolutionException ex ) {
                                chained = ex;
                            } catch( ModuleNotFoundException ex ) {
                                chained = ex;
                            }
                        }
                        if( dependModule != null ) {
                            dependentModules[i] = dependModule;
                        } else {
                            throw new ModuleResolutionException( adv, reqs[i], chained );
                        }
                    }
                }
                File moduleDir;
                try {
                    moduleDir = getModuleDirectoryFor( adv );
                } catch( IOException ex ) {
                    throw new ModuleNotFoundException( adv, ex );
                }
                ret = adv.createModule( this, moduleDir, jarFiles, getClass().getClassLoader() );

                if( ret != null ) {
                    theModules.put( adv, ret );

                    if( recursive ) {
                        theForwardDependencies.put( ret, dependentModules );

                        for( int i=0 ; i<dependentModules.length ; ++i ) {
                            Module [] uses    = theUses.get( dependentModules[i] );
                            Module [] newUses = null;

                            if( uses == null ) {
                                newUses = new Module[] { ret };
                            } else {
                                // append one if not there already
                                boolean found = false;
                                for( int j=0 ; j<uses.length ; ++j ) {
                                    if( ret == uses[j] ) {
                                        found = true;
                                        break;
                                    }
                                }
                                if( !found ) {
                                    newUses = new Module[ uses.length+1 ];
                                    System.arraycopy( uses, 0, newUses, 0, uses.length );
                                    newUses[uses.length] = ret;
                                }
                            }
                            theUses.put( dependentModules[i], newUses );
                        }
                    }
                } else {
                    throw new NullPointerException( "createModule returned null" );
                }
            }
            return ret;
        }
    }

    /**
     * Find the JAR files for this ModuleAdvertisement. These must be local Files
     * (which may require an implementation of this method to download them first).
     * This method throws an Exception if a required File could not be found.
     * It returns an empty array if no local Files are required given the specific Module.
     * It returns null if the local installation has the JAR files on the classpath already.
     *
     * @param adv the ModuleAdvertisement to consider
     * @return the locally found File containing the JARs, or null if none needed to be consid
     * @throws IOException thrown if the Files could not be loaded
     */
    public File [] findJarFilesFor(
            ModuleAdvertisement adv )
        throws
            IOException
    {
        return null;
    }

    /**
     * Obtain the directory for a Module specified through its ModuleAdvertisement.
     * This must be a local directory or may return null.
     * 
     * @param adv the ModuleAdvertisement to consider
     * @return the locally found directory, or null
     * @throws IOException thrown if the directory could not be accessed
     */
    public File getModuleDirectoryFor(
            ModuleAdvertisement adv )
        throws
            IOException
    {
        return null;
    }

    /**
     * Given a Module, this allows us to determine which other Modules it depends on.
     * This is similar to the result of determineResolutionCandidates(), except that
     * this returns one resolved Module for each dependency, not a set of ModuleAdvertisements
     * that may or may not be resolvable.
     *
     * @param theModule the Module whose dependencies we want to determine
     * @return the set of Modules that this Module depends on.
     * @throws ModuleNotFoundException thrown if the Module could not be found
     * @throws ModuleResolutionException thrown if the Module could not be resolved
     * @see #determineUses
     */
    public final Module [] determineDependencies(
            Module theModule )
        throws
            ModuleNotFoundException,
            ModuleResolutionException
    {
        resolve( theModule.getModuleAdvertisement(), true );

        synchronized( RESOLVE_LOCK ) {
            return theForwardDependencies.get( theModule );
        }
    }

    /**
     * Given a Module, this allows us to determine which other Modules use it.
     *
     * This is the inverse relationship found by determineDependencies. However,
     * unlike determineDependencies, the returned values may change dramatically
     * during operation of the system as additional Modules become users.
     *
     * @param theModule the Module whose uses we want to determine
     * @return the set of Moduels that this Module currently is used by
     * @see #determineDependencies
     */
    public final Module [] determineUses(
            Module theModule )
    {
        synchronized( RESOLVE_LOCK ) {
            return theUses.get( theModule );
        }
    }

    /**
     * Add another ModuleAdvertisment during runtime. Take care that we don't accidentially
     * add the same one again.
     *
     * @param ad the ModuleAdvertisement to be added
     */
    protected void addAdvertisement(
            ModuleAdvertisement ad )
    {
        String name    = ad.getModuleName();
        String version = ad.getModuleVersion();

        synchronized( theAdvertisements ) {
            for( ModuleAdvertisement current : theAdvertisements ) {
                if( name.equals( current.getModuleName() )) {
                    if( version == null && current.getModuleVersion() == null ) {
                        return;
                    }
                    if( version != null && current.equals( current.getModuleVersion() )) {
                        return;
                    }
                }
            }
            theAdvertisements.add( ad );
        }
    }

    /**
     * Obtain an iterator over all ModuleAdvertisements known by this ModuleRegistry.
     *
     * @return an iterator over all ModuleAdvertisements
     */
    public final Iterator<ModuleAdvertisement> advertisementIterator()
    {
        return theAdvertisements.iterator();
    }

    /**
     * Obtain an iterator over all StandardModuleAdvertisements known by this ModuleRegistry.
     *
     * @return an iterator over all StandardModuleAdvertisements
     */
    public final Iterator<StandardModuleAdvertisement> standardAdvertisementIterator()
    {
        return new FilteringIterator<StandardModuleAdvertisement>( theAdvertisements.iterator(), StandardModuleAdvertisement.class );
    }

    /**
     * Obtain an iterator over all ModelModuleAdvertisements known by this ModuleRegistry.
     *
     * @return an iterator over all ModelModuleAdvertisements
     */
    public final Iterator<ModelModuleAdvertisement> modelAdvertisementIterator()
    {
        return new FilteringIterator<ModelModuleAdvertisement>( theAdvertisements.iterator(), ModelModuleAdvertisement.class );
    }

    /**
     * Add a ModuleRegistry listener to be notified when new Modules become available etc.
     *
     * @param newListener the new listener to add
     * @see #removeModuleRegistryListener
     */
    public final synchronized void addModuleRegistryListener(
            ModuleRegistryListener newListener )
    {
        if( theModuleRegistryListeners == null ) {
            theModuleRegistryListeners = new ArrayList<ModuleRegistryListener>();
        }
        theModuleRegistryListeners.add( newListener );
    }

    /**
     * Remove a ModuleRegistry listener.
     *
     * @param oldListener the listener to remove
     * @see #addModuleRegistryListener
     */
    public final synchronized void removeModuleRegistryListener(
            ModuleRegistryListener oldListener )
    {
        theModuleRegistryListeners.remove( oldListener );
    }

    /**
     * Send an event to our listeners.
     *
     * @param theEvent the event to send
     */
 protected final void fireModuleAddedEvent(
            ModuleRegistryEvent theEvent )
    {
        Iterator<ModuleRegistryListener> theIter;
        synchronized( this ) {
            if( theModuleRegistryListeners == null || theModuleRegistryListeners.isEmpty() ) {
                return;
            }
            theIter = ( new ArrayList<ModuleRegistryListener>( theModuleRegistryListeners )).iterator();
        }

        while( theIter.hasNext() ) {
            ModuleRegistryListener current = theIter.next();
            current.newModuleAvailable( theEvent );
        }
    }

    /**
     * Convert into a String representation, mainly for debugging.
     *
     * @return String representation of this object
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer( 200 );
        buf.append( "<" );
        buf.append( super.toString() );
        buf.append( "{ #moduleAdv: " );
        buf.append( theAdvertisements.size() );
        for( ModuleAdvertisement ad : theAdvertisements ) {
            buf.append( ", " );
            buf.append( ad.getModuleName() );
        }
        buf.append( " }>" );
        return buf.toString();
    }

    /**
     * Construct a printable String that reflects the content of this ModuleRegistry.
     * This is primarily used for debugging.
     *
     * @return String that can be printed
     */
    public String contentToString()
    {
        StringBuffer buf = new StringBuffer( 100 );

        Iterator theIter = advertisementIterator();
        int count = 0;
        while( theIter.hasNext() ) {
            ModuleAdvertisement current = (ModuleAdvertisement) theIter.next();
            buf.append( String.valueOf( ++count ));
            buf.append( ": " );
            buf.append( current.contentToString() );

            if( theIter.hasNext() ) {
                buf.append( "\n" );
            }
        }
        return buf.toString();
    }

    /**
     * The set of currently known ModuleAdvertisements.
     */
    private ArrayList<ModuleAdvertisement> theAdvertisements;

    /**
     * The set of currently available Modules, keyed by ModuleAdvertisement.
     */
    private HashMap<ModuleAdvertisement,Module> theModules = new HashMap<ModuleAdvertisement,Module>();

    /**
     * This maps from Module to Module[], reflecting the dependency of one
     * Module on a set of others. This is the inverse relationship of what
     * is captured in theUses.
     *
     * The sequence of the items in the Module[] is the same as the sequence
     * of the items in the ModuleAdvertisement's ModuleRequirements array.
     */
    private HashMap<Module,Module[]> theForwardDependencies = new HashMap<Module,Module[]>();

    /**
     * This maps from Module to Module[], reflecting the use of one Module
     * by a set of others. This is the inverse relationship of what is
     * captured in theForwardDependencies.
     */
    private HashMap<Module,Module[]> theUses = new HashMap<Module,Module[]>();

    /**
     * The SoftwareInstallation with which we have to work.
     */
    private SoftwareInstallation theInstallation;

    /**
     * The set of currently subscribed ModuleRegistryListeners. Allocated as needed.
     */
    private ArrayList<ModuleRegistryListener> theModuleRegistryListeners = null;

    /**
     * This object is used as a semaphore for Module loads.
     */
    protected final Object RESOLVE_LOCK = new Object();

    /**
     * Simple iterator over the elements in an array.
     * 
     * @param <T> the type of elements to iterate over
     */
    static class SimpleArrayIterator<T>
        implements
            Iterator<T>
    {
        /**
         * Constructor.
         * 
         * @param a the array to iterate over
         */
        public SimpleArrayIterator(
                T [] a )
        {
            theArray = a;
        }

        /**
         * Is there a next element?
         * 
         * @return true if there is a next element
         */
        public boolean hasNext()
        {
            return index < theArray.length;
        }

        /**
         * Obtain the next element.
         * 
         * @return the next element
         */
        public T next()
        {
            return theArray[index++];
        }

        /**
         * Remove the current element.
         * 
         * @throws UnsupportedOperationException always thrown
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        private int  index = 0;
        private T [] theArray;
    }
    
    /**
     * Helper iterator to filter out by type the results of a delegate Iterator.
     * 
     * @param <T> the type of elements to iterate over
     */
    static class FilteringIterator<T>
        implements
            Iterator<T>
    {
        /**
         * Constructor.
         *
         * @param delegate the delegate Iterator
         * @param clazz the type of result acceptable
         */
        public FilteringIterator(
                Iterator<? super T> delegate,
                Class<T>            clazz )
        {
            theDelegate = delegate;
            theClazz    = clazz;
            
            advance();
        }
        
        /**
         * Is there a next element?
         * 
         * @return true if there is a next element
         */
        public boolean hasNext()
        {
            return theNext != null;
        }

        /**
         * Obtain the next element.
         * 
         * @return the next element
         */
        public T next()
        {
            T ret = theNext;
            
            advance();
            
            return ret;
        }

        /**
         * Remove the current element.
         * 
         * @throws UnsupportedOperationException always thrown
         */
        public void remove()
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Advance the Iterator by one.
         */
        @SuppressWarnings(value={"unchecked"})
        protected void advance()
        {
            while( theDelegate.hasNext() ) {
                Object candidate = theDelegate.next();
                if( theClazz.isInstance( candidate )) {
                    theNext = (T) candidate;
                    return;
                }
            }
            theNext = null;
        }
        
        /**
         * The underlying Iterator.
         */
        protected Iterator<? super T> theDelegate;
        
        /**
         * The class by which we filter.
         */
        protected Class<T> theClazz;
        
        /**
         * The next value to return, if any.
         */
        protected T theNext;
    }
}
