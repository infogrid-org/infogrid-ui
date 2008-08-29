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

package org.infogrid.util;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import org.infogrid.util.logging.Log;

/**
  * <p>Collects a number of useful helper functionalities that
  * deal with resources. These are on a somewhat higher level than the
  * ones provided by the JDK, and in fact they mostly delegate to them.</p>
  *
  * <p>A client that needs a resource typically finds a <code>ResourceHelper</code> by
  * using the {@link #getInstance} method. From there, it can look up its own
  * resources by name. By convention, the argument to the <code>getInstance</code>
  * method is the class looking for the resource, called a channel in the API.</p>
  *
  * <p>A ResourceHelper may delegate, if a resource was not found locally,
  * to another ResourceHelper specified at construction time.</p>
  *
  * <p>There is also
  * a shared instance ResourceHelper that contains application-level resources
  * that override all other ResourceHelpers. That way, InfoGrid can provide useful
  * default resources, but application developers can override them in any way
  * they please. All resources in the application-level resources must be named
  * <code>channel!name</code> instead of just <code>name</code>, in order to
  * avoid naming conflicts across channels.</p>
  *
  * <p>In this file, we instantiate the logger when we have our resources
  * set, in order to avoid a bootstrap problem with {@link org.infogrid.util.logging.Log}.</p>
  *
  * <p>This class is final. In order to define additional convenience methods,
  * create a utility class (such as with only static methods, taking a ResourceHelper
  * as an argument).</p>
  */
public final class ResourceHelper
{
    private static Log log = null; // !!! see comment at beginning of file

    /**
     * Private constructor, use factory methods.
     *
     * @param myName name of the ResourceHelper
     * @param cl the ClassLoader from which to load the resources
     * @param del the ResourceHelper we delegate to, if the resource is not found locally
     */
    protected ResourceHelper(
            String         myName,
            ClassLoader    cl,
            ResourceHelper del )
    {
        theName        = myName;
        theClassLoader = cl;
        theDelegate    = del;
    }

    /**
     * Obtain the ResourceBundle containing application definitions.
     *
     * @return the ResourceBundle containing application definitions
     * @see #setApplicationResourceBundle
     */
    public static ResourceBundle getApplicationResourceBundle()
    {
        return theApplicationResourceBundle;
    }

    /**
     * Set the ResourceBundle containing application definitions.
     *
     * @param source the ResourceBundle containing application definitions
     * @see #getApplicationResourceBundle
     */
    public static void setApplicationResourceBundle(
            ResourceBundle source )
    {
        theApplicationResourceBundle = source;
    }

    /**
     * Tell this class to initialize logging. This is a separate call in order
     * to avoid that logging gets instantiated here before the logger has been
     * initialized. This call must only be called once, at the very begining
     * of a process's life cycle.
     */
    public static void initializeLogging()
    {
        log = Log.getLogInstance( ResourceHelper.class ); // our own, private logger

        if( log.isDebugEnabled() ) {
            log.debug( "ResourceHelper.initializeLogging()" );
        }
    }

    /**
      * <p>Method for getting hold of the ResourceHelper instance for a particular
      * Class. It employs as smart factory pattern. We pass in a channel,
      * which typically is the name of the class/component that wants
      * a resource, or the central name for a set of related classes.</p>
      *
      * <p>This method is guaranteed to return non-null, even if the
      * ResourceHelper does not find any "resources" to speak of.</p>
      *
      * @param channel the channel determining the ResourceHelper instance we obtain
      * @return the created/found ResourceHelper
      */
    public static ResourceHelper getInstance(
            Class channel )
    {
        return getInstance( channel.getName(), channel.getClassLoader(), null );
    }

    /**
      * <p>Method for getting hold of the ResourceHelper instance for a particular
      * Class, using another ResourceHelper as a delegate (which may be null ).
      * It employs as smart factory pattern. We pass in a channel,
      * which typically is the name of the class/component that wants
      * a resource, or the central name for a set of related classes.</p>
      *
      * <p>This method is guaranteed to return non-null, even if the
      * ResourceHelper does not find any "resources" to speak of.</p>
      *
      * @param channel the channel determining the ResourceHelper instance we obtain
      * @param del the ResourceHelper we delegate to, if any
      * @return the created/found ResourceHelper
      */
    public static ResourceHelper getInstance(
            Class          channel,
            ResourceHelper del )
    {
        return getInstance( channel.getName(), channel.getClassLoader(), del );
    }

    /**
      * <p>Method for getting hold of the ResourceHelper instance for a particular
      * Class whose name is provided.
      * It employs as smart factory pattern. We pass in a channel,
      * which typically is the name of the class/component that wants
      * a resource, or the central name for a set of related classes.</p>
      *
      * <p>This method is guaranteed to return non-null, even if the
      * ResourceHelper does not find any "resources" to speak of.</p>
      *
      * @param channelName name of the channel determining the ResourceHelper instance we obtain
      * @param loader the ClassLoader to use
      * @return the created/found ResourceHelper
      */
    public static ResourceHelper getInstance(
            String      channelName,
            ClassLoader loader )
    {
        return getInstance( channelName, loader, null );
    }

    /**
      * <p>Method for getting hold of the ResourceHelper instance for a particular Class
      * whose name is provided, using another ResourceHelper as a delegate (which may be null).
      * It employs as smart factory pattern. We pass in a channel,
      * which typically is the name of the class/component that wants
      * a resource, or the central name for a set of related classes.</p>
      *
      * <p>This method is guaranteed to return non-null, even if the
      * ResourceHelper does not find any "resources" to speak of.</p>
      *
      * @param channelName name of the channel determining the ResourceHelper instance we obtain
      * @param loader the ClassLoader to use
      * @param del the ResourceHelper we delegate to, if any
      * @return the created/found ResourceHelper
      */
    public static ResourceHelper getInstance(
            String         channelName,
            ClassLoader    loader,
            ResourceHelper del )
    {
        Key            myKey = new Key( channelName, del );
        ResourceHelper obj   = theHelpers.get( myKey );
        if( obj == null ) {
            obj = new ResourceHelper( channelName, loader, del );
            theHelpers.put( myKey, obj );

            if( log != null && log.isDebugEnabled() ) {
                log.debug( "ResourceHelper.getInstance( " + channelName + " ) -- created" );
            }
        }
        return obj;
    }

    /**
      * <p>Obtain the value of one string resource given a resource name. If the resource
      * does not exist, we return resourceName so that it is easy to build GUIs
      * that work at least "somewhat". E.g.
      * <pre>
      *    new JButton( theResourceHelper.getResourceString( "NoMore" ))
      * </pre>
      * will at least show "NoMore" instead of nothing.</p>
      *
      * <p>If a resource is not found, this will log an info to our logger.</p>
      *
      * @param resourceName the name of the resource we are looking for
      * @return the value of the resource, or a default
      */
    public String getResourceString(
            String resourceName )
    {
        String ret = internalGetResourceString( resourceName );
        if( ret == null ) {
            cannotFindResource( resourceName );
            ret = resourceName; // fallback
            
        } else if( ret.startsWith( "\"" ) && ret.endsWith( "\"" ) ) {
            ret = ret.substring( 1, ret.length()-1 );
        }

        return ret;
    }

    /**
     * Obtain a resource value, but replace placeholders with arguments.
     *
     * @param resourceName the name of the resource we are looking for
     * @param args replacement arguments for the placeholders in the found resource value
     * @return the value of the resource, or a default, with the placeholders removed
     */
    public String getResourceStringWithArguments(
            String    resourceName,
            Object... args )
    {
        String raw = getResourceString( resourceName );
        return MessageFormat.format( raw, args );
    }

    /**
     * Obtain the values from a String that were inserted by
     * {@link #getResourceStringWithArguments}.
     *
     * @param resourceName the name of the resource we are looking for
     * @param toParse the String to parse
     * @return the obtained values
     * @throws ParseException thrown if parsing failed
     */
    public Object [] parseArgumentsFromResourceString(
            String resourceName,
            String toParse )
        throws
            ParseException
    {
        String        raw    = getResourceString( resourceName );
        MessageFormat format = new MessageFormat( raw );
        Object []     ret    = format.parse( toParse );
        return ret;
    }
    
    /***
     * Obtain a resource value as String[] using the convention
     * for specifying arrays in the resources.
     *
     * @param resourceName the name of the resource we are looking for
     * @return the value of the resource, or a default
     */
    public String [] getResourceStringArray(
            String resourceName )
    {
        return StringHelper.tokenize( getResourceString( resourceName ));
    }

    /**
     * Obtain a resource value as String[] using the convention
     * for specifying arrays in the resources, or a default. The default
     * is returned if the resource does not exist, or if it exists but
     * does not have the right number of elements.
     *
     * @param resourceName the name of the resource we are looking for
     * @param nElements the number of String[] elements we expect in the resource value
     * @param defaultString the String[] to return if the resource value could not be found, or was incorrect
     * @return the value of the resource, or a default
     */
    public String [] getResourceStringArrayOrDefault(
            String    resourceName,
            int       nElements,
            String [] defaultString )
    {
        String res = getResourceStringOrDefault( resourceName, null );
        if( res == null ) {
            return defaultString;
        }
        String [] ret = StringHelper.tokenize( res );
        if( ret.length == nElements ) {
            return ret;
        } else {
            cannotFindResource( resourceName );
            return defaultString;
        }
    }

    /**
     * Obtain a resource value as String[] using the convention
     * for specifying arrays in the resources, or a default. The default
     * is returned if the resource does not exist.
     *
     * @param resourceName the name of the resource we are looking for
     * @param defaultString the String[] to return if the resource value could not be found
     * @return the value of the resource, or a default
     */
    public String [] getResourceStringArrayOrDefault(
            String    resourceName,
            String [] defaultString )
    {
        String res = getResourceStringOrDefault( resourceName, null );
        if( res == null ) {
            return defaultString;
        }
        String [] ret = StringHelper.tokenize( res );
        return ret;
    }

    /**
     * Obtain a resource value as String[] using the convention
     * for specifying arrays in the resources, or null if not given.
     *
     * @param resourceName the name of the resource we are looking for
     * @return the value of the resource, or null
     */
    public String [] getResourceStringArrayOrNull(
            String resourceName )
    {
        String found = internalGetResourceString( resourceName );
        
        String [] ret = StringHelper.tokenize( found );

        for( int i=0 ; i<ret.length ; ++i ) {
            if( ret[i].startsWith( "\"" ) && ret[i].endsWith( "\"" ) ) {
                ret[i] = ret[i].substring( 1, ret[i].length()-1 );
            }
        }

        if( log != null && log.isDebugEnabled() ) {
            log.debug( "     returning " + ArrayHelper.arrayToString( ret ) );
        }
        return ret;
    }
    
    /**
     * Obtain a resource value, or the default if not given.
     *
     * @param resourceName the name of the resource we are looking for
     * @param defaultString the string returned if the resource could not be found
     * @return the value of the resource, or the default
     */
    public String getResourceStringOrDefault(
            String resourceName,
            String defaultString )
    {
        String ret = internalGetResourceString( resourceName );
        if( ret == null ) {
            ret = defaultString;
            
        } else if( ret.startsWith( "\"" ) && ret.endsWith( "\"" )) {
            ret = ret.substring( 1, ret.length()-1 );
        }

        return ret;
    }

    /**
      * Obtain the value of one string resource given resource name. If the resource
      * does not exist, we return null.
      *
      * @param resourceName the name of the resource we are looking for
      * @return the value of the resource, or null
      */
    public String getResourceStringOrNull(
            String resourceName )
    {
        String ret = internalGetResourceString( resourceName );
        // no error reporting
        if( ret !=null && ret.startsWith( "\"" ) && ret.endsWith( "\"" ) ) {
            ret = ret.substring( 1, ret.length()-1 );
        }
        return ret;
    }

    /**
     * Implementation of obtaining a resource.
     *
     * @param resourceName name of the resource to obtain
     * @return found String, or null if not found
     */
    protected String internalGetResourceString(
            String resourceName )
    {
        if( log != null && log.isDebugEnabled() ) {
            log.debug( this + ".internalGetResourceString( \"" + resourceName + "\" )" );
        }
        String  ret   = null;
        boolean found = false;
        if( theApplicationResourceBundle != null ) {
            try {
                ret = theApplicationResourceBundle.getString( createApplicationLevelResourceName( resourceName ));
                
                found = true;
            } catch( Exception ex ) {
                // no op
            }
        }

        if( !found ) {
            try {
                ret = getResourceBundle().getString( resourceName );
                
                found = true;
            } catch( Exception ex ) {

                // FIXME? This may be not so good from an error reporting perspective.
                if( theDelegate != null ) {
                    return theDelegate.internalGetResourceString( resourceName );
                }
            }
        }
        
        if( log != null && log.isDebugEnabled() ) {
            log.debug( "     returning " + ret );
        }
        return ret;
    }
    
    /**
      * Obtain the value of one URL resource.
      *
      * @param resourceName the name of the resource we are looking for
      * @return the value of the resource, or null
      */
    public URL getResource(
            String resourceName )
    {
        String name = getResourceString( resourceName );
        if( name == null ) {
            return null;
        }
        URL ret = theClassLoader.getResource( name );
        return ret;
    }

    /**
     * Obtain a resource whose value is of type int. If not present, return default.
     *
     * @param resourceName the name of the resource we are looking for
     * @param defaultInt the int returned if the resource could not be found
     * @return the value of the resource, or the default
     */
    public int getResourceIntegerOrDefault(
            String resourceName,
            int    defaultInt )
    {
        String value = getResourceStringOrDefault( resourceName, null );
        if( value == null ) {
            return defaultInt;
        }
        try {
            return Integer.parseInt( value );
        } catch( Exception ex ) {
            log.error( ex );
            return defaultInt;
        }
    }

    /**
     * Obtain a resource whose value is of type long. If not present, return default.
     *
     * @param resourceName the name of the resource we are looking for
     * @param defaultLong the long returned if the resource could not be found
     * @return the value of the resource, or the default
     */
    public long getResourceLongOrDefault(
            String resourceName,
            long   defaultLong )
    {
        String value = getResourceStringOrDefault( resourceName, null );
        if( value == null ) {
            return defaultLong;
        }
        try {
            return Long.parseLong( value );
        } catch( Exception ex ) {
            log.error( ex );
            return defaultLong;
        }
    }

    /**
     * Obtain a resource whose value is of type boolean. If not present, return default.
     *
     * @param resourceName the name of the resource we are looking for
     * @param defaultBoolean the boolean returned if the resource could not be found
     * @return the value of the resource, or the default
     */
    public boolean getResourceBooleanOrDefault(
            String  resourceName,
            boolean defaultBoolean )
    {
        String value = getResourceStringOrDefault( resourceName, null );
        if( value == null ) {
            return defaultBoolean;
        }
        if( "false".equalsIgnoreCase( value )) {
            return false;
        }
        if( "f".equalsIgnoreCase( value )) {
            return false;
        }
        if( "true".equalsIgnoreCase( value )) {
            return true;
        }
        if( "t".equalsIgnoreCase( value )) {
            return true;
        }

        log.warn( "Unknown value for resource " + resourceName + ", is " + value );
        return defaultBoolean;
    }

    /**
     * Obtain a resource whose value is of type float. If not present, return default.
     *
     * @param resourceName the name of the resource we are looking for
     * @param defaultFloat the float returned if the resource could not be found
     * @return the value of the resource, or the default
     */
    public float getResourceFloatOrDefault(
            String resourceName,
            float  defaultFloat )
    {
        String value = getResourceStringOrDefault( resourceName, null );
        if( value == null ) {
            return defaultFloat;
        }
        try {
            return Float.parseFloat( value );
        } catch( Exception ex ) {
            log.error( ex );
            return defaultFloat;
        }
    }

    /**
     * Obtain a resource whose value is of type double. If not present, return default.
     *
     * @param resourceName the name of the resource we are looking for
     * @param defaultDouble the double returned if the resource could not be found
     * @return the value of the resource, or the default
     */
    public double getResourceDoubleOrDefault(
            String resourceName,
            double defaultDouble )
    {
        String value = getResourceStringOrDefault( resourceName, null );
        if( value == null ) {
            return defaultDouble;
        }
        try {
            return Double.parseDouble( value );
        } catch( Exception ex ) {
            log.error( ex );
            return defaultDouble;
        }
    }

    /**
     * Obtain a resource whose value is of type PortableIcon. If not present, return default.
     * Use the provided ClassLoader to load the PortableIcon.
     *
     * @param iconResourceName the name of the resource we are looking for
     * @param defaultIcon the default Icon returned if the resource could not be found
     * @param loader the ClassLoader from where the PortableIcon shall be loaded
     * @return the value of the resource, or the default
     */
    public PortableIcon getResourceIconOrDefault(
            String       iconResourceName,
            PortableIcon defaultIcon,
            ClassLoader  loader )
    {
        String value = getResourceStringOrDefault( iconResourceName, null );
        if( value == null ) {
            return defaultIcon;
        }
        return PortableIcon.create( loader, value );
    }

    /**
     * Demand-load the actual ResourceBundle that we use.
     *
     * @return the ResourceBundle that we use
     * @throws MissingResourceException thrown if the needed ResourceBundle could not be found
     */
    private final ResourceBundle getResourceBundle()
        throws
            MissingResourceException
    {
        ResourceBundle ret;
        if( theBundleReference != null ) {
            ret = theBundleReference.get();
        } else {
            ret = null;
        }
        if( ret == null ) {
            try {
                ret = ResourceBundle.getBundle( theName, Locale.getDefault(), theClassLoader );
                if( ret != null ) {
                    theBundleReference = new WeakReference<ResourceBundle>( ret );
                }
            } catch( MissingResourceException ex ) {
                log.warn( "Cannot find resource bundle for channel " + theName + " with loader " + theClassLoader );

                theBundleReference = null;
            }
        }
        return ret;

    }

    /**
     * This is invoked when we cannot find a resource.
     *
     * @param resourceName the name of the resource we could not find
     */
    public void cannotFindResource(
            String resourceName )
    {
        if( log == null || log.isInfoEnabled() ) {
            ResourceBundle bundle;
            if( theBundleReference != null ) {
                bundle = theBundleReference.get();
            } else {
                bundle = null;
            }

            StringBuffer msg = new StringBuffer( 200 ); // fudge
            msg.append( "ResourceHelper cannot find resource \"" );
            msg.append( theName );
            msg.append( "\" ! \"" );
            msg.append( resourceName );
            msg.append( "\", bundle is " );
            msg.append( bundle );

            if( bundle instanceof PropertyResourceBundle ) {
                msg.append( " with keys" );
                PropertyResourceBundle realBundle = (PropertyResourceBundle) bundle;
                Enumeration theEnum = realBundle.getKeys();
                while( theEnum.hasMoreElements() ) {
                    Object current = theEnum.nextElement();
                    msg.append( " " );
                    msg.append( current );
                }
            } else if( theBundleReference == null ) {
                msg.append( ", class loader is " + theClassLoader );
            }
            if( log != null ) {
                log.info( msg );
            } else {
                System.err.println( "WARN:" + msg );
            }
        }
    }

    /**
      * Constructs a string array from the given strings and returns that.
      * The second string can be null in which case it just converts the first
      * string to a string array and returns that.
      *
      * @param str1 the string to be tokenized
      * @param str2 the string to be appended to the array
      * @return the created String array
      */
    public static String [] constructStringArray(
            String str1,
            String str2 )
    {
        String [] ret;
        if( str2 == null ) {
            ret = StringHelper.tokenize( str1 );
        } else {
            String [] temp = StringHelper.tokenize( str1 );
            int len = temp.length;
            ret = new String [len+1];
            System.arraycopy(temp,0,ret,0,len);
            ret[len] = str2;
        }
        return ret;
    }

    /**
     * Create the name of the resource for the application-level resource file.
     *
     * @param resourceName local name of the resource
     * @return application-level resource name
     */
    protected String createApplicationLevelResourceName(
            String resourceName )
    {
        StringBuilder ret = new StringBuilder();
        ret.append( theName );
        ret.append( '!' );
        ret.append( resourceName );
        return ret.toString();
    }

    /**
     * Determine equality.
     *
     * @param other the Object to compare against
     * @return true if the Objects are equal
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof ResourceHelper ) {
            ResourceHelper realOther = (ResourceHelper) other;
            if( ! theName.equals( realOther.theName )) {
                return false;
            }
            if( theDelegate == null ) {
                return realOther.theDelegate == null;
            }
            return theDelegate.equals( realOther.theDelegate );
        }
        return false;
    }

    /**
     * Determine hash code.
     *
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        if( theDelegate == null ) {
            return theName.hashCode();
        } else {
            return theName.hashCode() ^ theDelegate.hashCode();
        }
    }

    /**
     * Convert to string form, for debugging.
     *
     * @return this instance in string form
     */
    @Override
    public String toString()
    {
        return super.toString() + "<" + theName + ">";
    }

    /**
      * This a reference to the resource bundle we actually use. Only allocated when needed,
      * and because it is a Reference, de-allocated as soon as we don't need it any more.
      */
    protected WeakReference<ResourceBundle> theBundleReference;

    /**
     * My name: corresponds to the channel name.
     */
    protected String theName;

    /**
     * The ClassLoader through which we are trying to load the ResourceBundle.
     */
    protected ClassLoader theClassLoader;

    /**
     * The ResourceHelper this instance delegates to if the resource was not found locally.
     */
    protected ResourceHelper theDelegate;

    /**
     * This is the application's ResourceBundle that overrides all other ResourceBundles,
     * if given.
     */
    protected static ResourceBundle theApplicationResourceBundle = null;

    /**
      * The set of currently known ResourceHelpers.
      */
    protected static HashMap<Key,ResourceHelper> theHelpers = new HashMap<Key,ResourceHelper>();

    /**
     * This is a key into our big table of ResourceHelpers. It contains the name of the channel,
     * and the ResourceHelper to which the to-be-found ResourceHelper delegates (if any).
     */
    protected static class Key
    {
        /**
         * Constructor.
         *
         * @param channel the ResourceHelper's channel name
         * @param del the ResourceHelper's delegate ResourceHelper (if any)
         */
        public Key(
                String         channel,
                ResourceHelper del )
        {
            theChannel  = channel;
            theDelegate = del;
        }

        /**
         * We are equal if our components are equal.
         *
         * @param other the Object to compare against
         * @return true if the Objects are equal
         */
        @Override
        public boolean equals(
                Object other )
        {
            if( other instanceof Key ) {
                Key realOther = (Key) other;
                if( !theChannel.equals( realOther.theChannel )) {
                    return false;
                }
                if( theDelegate == null ) {
                    return realOther.theDelegate == null;
                }
                return theDelegate.equals( realOther.theDelegate );
            }
            return false;
        }

        /**
         * Hash code is a combination of the hash code of our constituent parts.
         *
         * @return the hash code
         */
        @Override
        public int hashCode()
        {
            if( theDelegate == null ) {
                return theChannel.hashCode();
            } else {
                return theChannel.hashCode() ^ theDelegate.hashCode();
            }
        }

        /**
         * The channel component.
         */
        private String theChannel;
        
        /**
         *  The delegate, if any.
         */
        private ResourceHelper theDelegate;
    }
}
