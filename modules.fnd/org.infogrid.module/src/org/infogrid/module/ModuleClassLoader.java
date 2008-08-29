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

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;
import java.util.jar.*;
import java.util.zip.*;

/**
 * <p>This is a class loader that knows how to load the code for a Module. It first looks
 * for code in its own JARs, and then delegates to the ModuleClassLoaders of the Modules
 * that this Module depends on.</p>
 *
 * <p>This used to inherit from URLClassLoader, but the URLClassLoader did mysterious
 * things (it suddenly added additional JARs to itself and I have no idea where
 * they came from), so I did it myself.</p>
 */
public class ModuleClassLoader
        extends
            ClassLoader
{
    /**
      * Construct one with the Module whose classes we are supposed to load and its registry.
      *
      * @param mod the Module whose classes we load
      * @param parent the ClassLoader of this ClassLoader
      * @throws MalformedURLException thrown if we can't convert the JAR file names to URLs
      * @throws ModuleNotFoundException a dependent Module could not be found
      * @throws ModuleResolutionException a dependent Module could not be resolved
      */
    public ModuleClassLoader(
            Module      mod,
            ClassLoader parent )
        throws
            MalformedURLException,
            ModuleNotFoundException,
            ModuleResolutionException
    {
        super( parent );

        theModule = mod;

        theJars = new JarFile[ theModule.getModuleJars().length ]; // filled-in on demand
        
        Module [] dependencies = theModule.getModuleRegistry().determineDependencies( theModule );
        dependencyClassLoaders = new ModuleClassLoader[ dependencies.length ];

        for( int i=0 ; i<dependencies.length ; ++i ) {
            dependencyClassLoaders[i] = (ModuleClassLoader)dependencies[i].getClassLoader();
        }
    }

    /**
     * Obtain the Module that goes with this ModuleClassLoader.
     *
     * @return the Module for whose classes this ClassLoader is responsible
     */
    public Module getModule()
    {
        return theModule;
    }

    /**
     * Set whether the default ClassLoader should be consulted by this ModuleClassLoader,
     * prior to consulting the Module's own JAR files.
     *
     * @param newValue true if the default ClassLoader shall be consulted first
     */
    public void setConsultDefaultClassLoaderFirst(
            boolean newValue )
    {
        theConsultDefaultClassLoaderFirst = newValue;
    }

    /**
     * Find a resource through this ClassLoader. First look for our local resources, then in
     * our dependencyClassLoaders.
     *
     * @param name name of the resource to find
     * @return URL to the resource
     */
    @Override
    public URL getResource(
            String name )
    {
        URL ret = findResource( name );
        if( ret != null ) {
            return ret;
        }

        for( int i=0 ; i<dependencyClassLoaders.length ; ++i ) {
            ret = dependencyClassLoaders[i].getResource( name );
            if( ret != null ) {
                return ret;
            }
        }
        return null;
    }

    /**
     * Obtain an Enumeration of Resources.
     *
     * @param name the name of the Resource
     * @return the Enumeration
     * @throws IOException thrown if an I/O error occurred
     */
    @Override
    public Enumeration<URL> getResources(
            String name )
        throws
            IOException
    {
        ClassLoader parent = getParent();

        URL localResource = getResource( name );
        if( localResource != null ) {
            return new CompoundIterator<URL>( localResource, parent.getResources( name ));
        } else {
            return parent.getResources( name );
        }
    }

    /**
     * Override loadClass() per comment above.
     *
     * @param name name of the to-be-loaded class
     * @param resolve do we also resolve the class
     * @return the loaded class
     * @throws ClassNotFoundException loading the class failed, it could not be found
     */
    @Override
    protected synchronized Class loadClass(
            String  name,
            boolean resolve )
        throws
            ClassNotFoundException
    {
        boolean closeReporting = false;

        Class c = findLoadedClass( name );
        if( c == null ) {
            closeReporting = true;
            ModuleErrorHandler.informLoadClassAttemptStart( theModule, name );

            if( cannotFindTable.get( name ) == null ) {

                boolean consultDefaultClassLoader = theConsultDefaultClassLoaderFirst;
                if( !consultDefaultClassLoader ) {
                    for( String prefix : MODULE_CLASSES_PREFIXES ) {
                        if( name.startsWith( prefix )) {
                            consultDefaultClassLoader = true;
                            break; // we won't have more than one prefix match
                        }
                    }
                }
                if( consultDefaultClassLoader ) {
                    try {
                        c = ModuleClassLoader.class.getClassLoader().loadClass( name );
                    } catch( ClassNotFoundException ex ) {
                        // do nothing
                    }
                }

                String path = name.replace('.', '/').concat(".class");
                byte [] classBytes = findBlob( path );
                if( classBytes != null && classBytes.length > 0 ) {
                    try {
                        c = defineClass( name, classBytes, 0, classBytes.length );
                    } catch( ClassFormatError ex ) {
                        ModuleErrorHandler.error( ex );
                    }
                }
                
                if( c == null ) {
                    for( int i=0 ; i<dependencyClassLoaders.length ; ++i ) {
                        try {
                            c = dependencyClassLoaders[i].loadClass( name, false );
                        } catch( ClassNotFoundException ex ) {
                            // do nothing
                        }
                        if( c != null ) {
                            break;
                        }
                    }
                }
            }
        }
        if( c == null ) {
            // we caught all exceptions, so we need to throw ourselves
            cannotFindTable.put( name, CANNOT_FIND_OBJECT );

            if( closeReporting ) {
                ModuleErrorHandler.informLoadClassAttemptFailed( theModule, name );
            }
            throw new ClassNotFoundException( name );
        }

        if( resolve ) {
            resolveClass( c );
        }
        if( closeReporting ) {
            ModuleErrorHandler.informLoadClassAttemptSucceeded( theModule, name );
        }

        return c;
    }

    /**
     * Find a URL.
     *
     * @param name the name of the resource
     * @return the URL of the resource, if found
     */
    @Override
    protected synchronized URL findResource(
            String name )
    {
        File [] files = theModule.getModuleJars();

        File     foundFile = null;
        JarEntry foundEntry = null;

        for( int i=0 ; i<files.length ; ++i ) {
            try {
                if( theJars[i] == null ) {
                    theJars[i] = new JarFile( files[i] );
                }
                foundEntry = theJars[i].getJarEntry( name );
                if( foundEntry != null ) {
                    foundFile = files[i];
                    break;
                }
            } catch( IOException ex ) {
                // Files that don't have the requested resource throw this exception, so don't do anything
            }
        }
        if( foundEntry == null ) {
            return null;
        }
        try {
            StringBuffer urlSpec = new StringBuffer();
            urlSpec.append( "jar:" );
            urlSpec.append( foundFile.toURL() );
            urlSpec.append( "!/" );
            return new URL( new URL( urlSpec.toString() ), foundEntry.getName() );

        } catch( MalformedURLException ex ) {
            ModuleErrorHandler.error( ex );
            return null;
        }
    }

    /**
     * Find a blob of data.
     *
     * @param name the name of the resource
     * @return the blob of data, as byte array, if found
     */
    protected synchronized byte [] findBlob(
            String name )
    {
        File [] files = theModule.getModuleJars();
        for( int i=0 ; i<files.length ; ++i ) {
            try {
                if( theJars[i] == null ) {
                    theJars[i] = new JarFile( files[i] );
                }
                JarEntry entry = theJars[i].getJarEntry( name );
                if( entry == null ) {
                    continue;
                }
                InputStream stream = theJars[i].getInputStream( entry );
                if( stream == null ) {
                    continue;
                }
                return slurp( stream, (int) entry.getSize(), -1 );
                
            } catch( IOException ex ) {
                // Files that don't have the requested resource throw this exception, so don't do anything
            }
        }
        return null;
    }

    /**
     * Helper method to read a byte array from a stream until EOF.
     *
     * @param inStream the stream to read from
     * @param initial the initial size of the buffer
     * @param maxBytes the maximum number of bytes we accept
     * @return the found byte array
     * @throws IOException thrown if an I/O error occurred
     */
    protected static byte [] slurp(
            InputStream inStream,
            int         initial,
            int         maxBytes )
        throws
            IOException
    {
        int    bufsize = 1024;
        if( initial > 0 ) {
            bufsize = initial;
        }
        if( maxBytes > 0 && bufsize > maxBytes ) {
            bufsize = maxBytes;
        }
        byte[] buf    = new byte[ bufsize ];
        int    offset = 0;

        while( true ) {
            int toRead = buf.length;
            if( maxBytes > 0 && maxBytes < toRead ) {
                toRead = maxBytes;
            }
            int read = inStream.read( buf, offset, toRead  - offset);
            if( read <= 0 ) {
                break;
            }
            offset += read;
            if( offset == buf.length ) {
                byte [] temp = new byte[ buf.length * 2 ];
                System.arraycopy( buf, 0, temp, 0, offset );
                buf = temp;
            }
        }
        
        // now chop if necessary
        if( buf.length > offset ) {
            byte [] temp = new byte[ offset ];
            System.arraycopy( buf, 0, temp, 0, offset );
            return temp;
        } else {
            return buf;
        }
    }

    /**
     * Obtain the ClassLoaders from dependent Modules.
     *
     * @return the ClassLoaders from dependent Modules
     */
    public ModuleClassLoader [] getDependencyClassLoaders()
    {
        return dependencyClassLoaders;
    }

    /**
     * Convert to a string representation for debugging.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer( 100 ); // fudge
        buf.append( "<" );
        buf.append( super.toString() );
        buf.append( "{ modulename: " );
        buf.append( theModule.getModuleAdvertisement().getModuleName() );
        buf.append( ", files: " );

        File [] theFiles = theModule.getModuleJars();
        for( int i=0 ; i<theFiles. length ; ++i ) {
            if( i>0 ) {
                buf.append( ", " );
            }
            buf.append( theFiles[i] );
        }

        buf.append( ", depends: " );
        for( int i=0 ; i<dependencyClassLoaders.length ; ++i ) {
            if( i>0 ) {
                buf.append( ", " );
            }
            buf.append( "{" );
            buf.append( dependencyClassLoaders[i].getModule().getModuleName() );
            buf.append( " with " );

            File [] dependentFiles = dependencyClassLoaders[i].getModule().getModuleJars();
            for( int j=0 ; j<dependentFiles.length ; ++j ) {
                if( j>0 ) {
                    buf.append( ", " );
                }
                buf.append( dependentFiles[j] );
            }
            buf.append( "}" );
        }

        buf.append( " }>" );
        return buf.toString();
    }

    /**
     * The Module whose classes we we are responsible for loading.
     */
    protected Module theModule;

    /**
     * The set of ModuleClassLoaders from the dependent Modules. Allocated as needed.
     */
    protected ModuleClassLoader [] dependencyClassLoaders = null;

    /**
     * The JAR files that belong to this ModuleClassLoader, read as needed.
     */
    protected JarFile [] theJars;

    /**
     * Shall we consult the default ClassLoader prior to the Module's own JAR files.
     */
    protected boolean theConsultDefaultClassLoaderFirst = false;
    
    /**
     * Our StreamHandler, allocated as needed.
     */
    protected URLStreamHandler theStreamHandler;

    /**
     * This map maps names of resources that we know for sure we can't load to a
     * marker object, so we stop attempting to load here and not delegate.
     */
    protected HashMap<String,Object> cannotFindTable = new HashMap<String,Object>( 20 );

    /**
     * Marker object to be inserted into the cannotFindTable.
     */
    private static final Object CANNOT_FIND_OBJECT = new Object();
    
    /**
     * Only load classes with this prefix from the default ClassLoader.
     */
    public static final String [] MODULE_CLASSES_PREFIXES = {
        "java", // java, javax
        "com.sun.",
        "sun", // sun, sunw
        "org.infogrid.module.",
        "org.ietf.jgss",
        "org.omg.",
        "org.w3c.dom",
        "org.xml.sax"
    };

    /**
     * Compound iterator helper class.
     * 
     * @param T the type of element to iterate over
     */
    static class CompoundIterator<T>
            implements
                Enumeration<T>
    {
        /**
         * Constructor.
         *
         * @param firstElement the first element to return
         * @param continued Enumeration over the remaining elements
         */
         public CompoundIterator(
                 T              firstElement,
                 Enumeration<T> continued )
         {
             theFirstElement = firstElement;
             theContinued    = continued;
         }

         /**
         * Tests if this enumeration contains more elements.
         *
         * @return  <code>true</code> if and only if this enumeration object
         *           contains at least one more element to provide;
         *          <code>false</code> otherwise.
         */
        public boolean hasMoreElements()
        {
            if( doFirst ) {
                return true;
            }
            return theContinued.hasMoreElements();
        }

        /**
         * Returns the next element of this enumeration if this enumeration
         * object has at least one more element to provide.
         *
         * @return     the next element of this enumeration.
         * @exception  NoSuchElementException  if no more elements exist.
         */
        public T nextElement()
        {
            if( doFirst ) {
                doFirst = false;
                return theFirstElement;
            }
            return theContinued.nextElement();
        }
        
        /**
          * The first element to return.
          */
        protected T theFirstElement;
         
        /**
          * The Enumeration over all other elements to return after the first.
          */
        protected Enumeration<T> theContinued;

        /**
          * Flag that tells whether to return the first element next.
          */
        protected boolean doFirst = true;
    }
}
