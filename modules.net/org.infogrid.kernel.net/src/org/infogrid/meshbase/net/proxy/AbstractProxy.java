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

package org.infogrid.meshbase.net.proxy;

import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.externalized.ExternalizedProxy;
import org.infogrid.meshbase.net.externalized.SimpleExternalizedProxy;
import org.infogrid.util.Factory;
import org.infogrid.util.FlexibleListenerSet;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.IdentifierStringifier;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Factors out common functionality of Proxy implementations.
 */
public abstract class AbstractProxy
        implements
            Proxy,
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( AbstractProxy.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param mb the NetMeshBase that this Proxy belongs to
     * @param policy the ProxyPolicy to use
     * @param partnerIdentifier identifier of the partner NetMeshBase with which this Proxy communicates
     */
    protected AbstractProxy(
            NetMeshBase           mb,
            ProxyPolicy           policy,
            NetMeshBaseIdentifier partnerIdentifier )
    {
        theMeshBase          = mb;
        theProxyPolicy       = policy;
        thePartnerIdentifier = partnerIdentifier;

        // default, subclass constructors can reset
        theTimeCreated = theTimeUpdated = theTimeRead = System.currentTimeMillis();
        theTimeExpires = -1L;
    }

    /**
     * Obtain the key for smart factories.
     *
     * @return the key
     */
    public final NetMeshBaseIdentifier getFactoryKey()
    {
        return getPartnerMeshBaseIdentifier();
    }

    /**
     * Enable a Factory to indicate to the FactoryCreatedObject that it was
     * it that created it.
     *
     * @param factory the Factory that created the FactoryCreatedObject
     */
    public final void setFactory(
            Factory<NetMeshBaseIdentifier, Proxy, CoherenceSpecification> factory )
    {
        if( factory instanceof ProxyManager ) {
            theFactory = (ProxyManager) factory;

        } else {
            // This can happen because factories delegating to other factories invoke this method
            // repeatedly with the entire chain of factories, not all of which may be SmartFactories.

            theFactory = null;
        }
    }

    /**
     * Obtain the Factory that created this FactoryCreatedObject. In case of
     * chained factories that delegate to each other, this method is
     * supposed to return the outermost factory invoked by the application programmer.
     *
     * @return the Factory that created the FactoryCreatedObject
     */
    public final ProxyManager getFactory()
    {
        return theFactory;
    }

    /**
     * Determine the NetMeshBaseIdentifier of the partner NetMeshBase. The partner
     * NetMeshBase is the NetMeshBase with which this Proxy communicates.
     *
     * @return the NetMeshBaseIdentifier of the partner NetMeshBase
     */
    public NetMeshBaseIdentifier getPartnerMeshBaseIdentifier()
    {
        return thePartnerIdentifier;
    }

    /**
     * Obtain the NetMeshBase to which this Proxy belongs.
     *
     * @return the NetMeshBase
     */
    public final NetMeshBase getNetMeshBase()
    {
        return theMeshBase;
    }

    /**
     * Obtain the CoherenceSpecification currently in effect.
     *
     * @return the current CoherenceSpecification
     */
    public final CoherenceSpecification getCoherenceSpecification()
    {
        return theProxyPolicy.getCoherenceSpecification();
    }

    /**
     * Determine when this Proxy was first created. Often this will refer to a time long before this
     * particular Java object instance was created; this time refers to when the connection between
     * the two logical NetMeshBases was created, which could have been in a previous run prior to, say,
     * a server reboot.
     *
     * @return the time this Proxy was created, in System.currentTimeMillis() format
     */
    public final long getTimeCreated()
    {
        return theTimeCreated;
    }

    /**
     * Determine when information held by this Proxy was last updated.
     *
     * @return the time this Proxy was last updated, in System.currentTimeMillis() format
     */
    public final long getTimeUpdated()
    {
        return theTimeUpdated;
    }

    /**
     * Determine when information held by this Proxy was last read.
     *
     * @return the time this Proxy was last read, in System.currentTimeMillis() format
     */
    public final long getTimeRead()
    {
        return theTimeRead;
    }

    /**
     * Determine when this Proxy will expire, if at all.
     *
     * @return the time this Proxy will expire, in System.currentTimeMillis() format, or -1L if never.
     */
    public final long getTimeExpires()
    {
        return theTimeExpires;
    }

    /**
     * Obtain this Proxy in externalized form.
     *
     * @return the ExternalizedProxy capturing the information in this Proxy
     */
    public ExternalizedProxy asExternalized()
    {
        return SimpleExternalizedProxy.create( this );
    }

    /**
     * Subscribe to lease-related events, without using a Reference.
     *
     * @param newListener the to-be-added listener
     * @see #addWeakProxyListener
     * @see #addSoftProxyListener
     * @see #removeProxyListener
     */
    public final void addDirectProxyListener(
            ProxyListener newListener )
    {
        theProxyListeners.addDirect( newListener );
    }

    /**
     * Subscribe to lease-related events, using a WeakReference.
     *
     * @param newListener the to-be-added listener
     * @see #addDirectProxyListener
     * @see #addSoftProxyListener
     * @see #removeProxyListener
     */
    public final void addWeakProxyListener(
            ProxyListener newListener )
    {
        theProxyListeners.addWeak( newListener );
    }

    /**
     * Subscribe to lease-related events, using a SoftReference.
     *
     * @param newListener the to-be-added listener
     * @see #addWeakProxyListener
     * @see #addDirectProxyListener
     * @see #removeProxyListener
     */
    public final void addSoftProxyListener(
            ProxyListener newListener )
    {
        theProxyListeners.addSoft( newListener );
    }

    /**
     * Unsubscribe from lease-related events.
     *
     * @param oldListener the to-be-removed listener
     * @see #addDirectProxyListener
     * @see #addWeakProxyListener
     * @see #addSoftProxyListener
     */
    public final void removeProxyListener(
            ProxyListener oldListener )
    {
        theProxyListeners.remove( oldListener );
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
                    "theMeshBase.getNetworkIdentifier()",
                    "getPartnerMeshBaseIdentifier()"
                },
                new Object[] {
                    theMeshBase.getIdentifier().toExternalForm(),
                    getPartnerMeshBaseIdentifier().toExternalForm()
                } );
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        boolean isDefaultMeshBase = context != null ? ( getNetMeshBase().equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ))) : true;

        String proxyExternalForm    = IdentifierStringifier.defaultFormat( getPartnerMeshBaseIdentifier().toExternalForm(), pars );
        String meshBaseExternalForm = IdentifierStringifier.defaultFormat( getNetMeshBase().getIdentifier().toExternalForm(), pars );

        String key;
        if( isDefaultMeshBase ) {
            key = DEFAULT_MESH_BASE_ENTRY;
        } else {
            key = NON_DEFAULT_MESH_BASE_ENTRY;
        }

        String ret = rep.formatEntry(
                getClass(),
                key,
                pars,
                proxyExternalForm,
                meshBaseExternalForm );

        return ret;
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param additionalArguments additional arguments for URLs, if any
     * @param target the HTML target, if any
     * @param title title of the HTML link, if any
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentationLinkStart(
            String                      additionalArguments,
            String                      target,
            String                      title,
            StringRepresentation        rep,
            StringRepresentationContext context )
        throws
            StringifierException
    {
        boolean isDefaultMeshBase = context != null ? ( getNetMeshBase().equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ))) : true;
        String  contextPath       = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;

        String proxyExternalForm    = getPartnerMeshBaseIdentifier().toExternalForm();
        String meshBaseExternalForm = this.getNetMeshBase().getIdentifier().toExternalForm();

        String key;
        if( isDefaultMeshBase ) {
            key = DEFAULT_MESH_BASE_LINK_START_ENTRY;
        } else {
            key = NON_DEFAULT_MESH_BASE_LINK_START_ENTRY;
        }

        String ret = rep.formatEntry(
                getClass(),
                key,
                null,
        /* 0 */ contextPath,
        /* 1 */ proxyExternalForm,
        /* 2 */ meshBaseExternalForm,
        /* 3 */ additionalArguments,
        /* 4 */ target,
        /* 5 */ title );

        return ret;
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param context the StringRepresentationContext of this object
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation        rep,
            StringRepresentationContext context )
        throws
            StringifierException
    {
        boolean isDefaultMeshBase = context != null ? ( getNetMeshBase().equals( context.get( MeshStringRepresentationContext.DEFAULT_MESHBASE_KEY ))) : true;
        String  contextPath       = context != null ? (String) context.get(  StringRepresentationContext.WEB_CONTEXT_KEY ) : null;

        String proxyExternalForm    = getPartnerMeshBaseIdentifier().toExternalForm();
        String meshBaseExternalForm = this.getNetMeshBase().getIdentifier().toExternalForm();

        String key;
        if( isDefaultMeshBase ) {
            key = DEFAULT_MESH_BASE_LINK_END_ENTRY;
        } else {
            key = NON_DEFAULT_MESH_BASE_LINK_END_ENTRY;
        }

        String ret = rep.formatEntry(
                getClass(),
                key,
                null,
                contextPath,
                proxyExternalForm,
                meshBaseExternalForm );

        return ret;
    }

    /**
     * Convert to String form, for debugging.
     *
     * @return String form
     */
    @Override
    public String toString()
    {
        StringBuilder buf = new StringBuilder();
        buf.append( super.toString());
        buf.append( "{ mine: " );
        buf.append( theMeshBase.getIdentifier().toExternalForm());
        buf.append( " -> partner: " );
        buf.append( thePartnerIdentifier.toExternalForm());
        buf.append( " }" );
        return buf.toString();
    }

    /**
     * The MeshBase we belong to.
     */
    protected NetMeshBase theMeshBase;

    /**
     * The ProxyManager that created us.
     */
    protected ProxyManager theFactory;

    /**
     * The Policy to use for communication.
     */
    protected ProxyPolicy theProxyPolicy;

    /**
     * Identifier of the NetMeshBase with which this Proxy communicates
     */
    protected NetMeshBaseIdentifier thePartnerIdentifier;

    /**
     * The time at which this Proxy was created logically (not necessarily this Java instance).
     */
    protected long theTimeCreated;

    /**
     * The time at which this Proxy was last updated logically (not necessarily this Java instance).
     */
    protected long theTimeUpdated;

    /**
     * The time at which this Proxy was last read logically (not necessarily this Java instance).
     */
    protected long theTimeRead;

    /**
     * The time at which this Proxy will expire logically (not necessarily this Java instance).
     */
    protected long theTimeExpires;

    /**
     * The Proxy listeners.
     */
    protected FlexibleListenerSet<ProxyListener,ProxyEvent,Object> theProxyListeners
            = new FlexibleListenerSet<ProxyListener,ProxyEvent,Object>() {
                protected void fireEventToListener(
                        ProxyListener listener,
                        ProxyEvent    event,
                        Object        arg )
                {
                    if( event instanceof SendFailedEvent ) {
                        listener.synchronousSendFailedEvent( (SendFailedEvent) event );
                    } else if( event instanceof InitiateResynchronizeFailedEvent ) {
                        listener.initiateResynchronizeFailedEvent( (InitiateResynchronizeFailedEvent) event );
                    } else {
                        log.error( "What is this: " + event );
                    }
                }

                @Override
                protected void fireEventIfNoListeners(
                        ProxyEvent event,
                        Object     arg )
                {
                    // If there are no listeners, we at least log errors
                    log.error( event );
                }
            };


    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_ENTRY = "DefaultMeshBaseString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_LINK_START_ENTRY = "DefaultMeshBaseLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String DEFAULT_MESH_BASE_LINK_END_ENTRY = "DefaultMeshBaseLinkEndString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_ENTRY = "NonDefaultMeshBaseString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_LINK_START_ENTRY = "NonDefaultMeshBaseLinkStartString";

    /**
     * Entry in the resource files, prefixed by the StringRepresentation's prefix.
     */
    public static final String NON_DEFAULT_MESH_BASE_LINK_END_ENTRY = "NonDefaultMeshBaseLinkEndString";
}
