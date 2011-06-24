/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.*;

import java.beans.*;
import java.util.concurrent.atomic.*;

/**
 * Represents the KML <i>NetworkLink</i> element and provides access to its contents.
 * <p/>
 * During rendering, <code>KMLNetworkLink</code> retrieves and loads its network resource whenever necessary. Upon a
 * successful retrieval, <code>KMLNetworkLink</code> sends an <code>AVKey.RETRIEVAL_STATE_SUCCESSFUL</code> property
 * change event to this link's property change listeners. Once retrieved and loaded, <code>KMLNetworkLink</code> stores
 * its network resource by calling <code>setNetworkResource</code>, draws its network resource during preRendering and
 * rendering, and forwards property change events from the network resource to its property change listeners.
 * <p/>
 * During retrieval, <code>KMLNetworkLink</code> attempts to use either the <code>Link</code> or the <code>Url</code>.
 * The <code>Link</code> is the preferred method for encoding a KML NetworkLink's address since KML version 2.1,
 * therefore we give it priority over <code>Url</code>.
 *
 * @author tag
 * @version $Id: KMLNetworkLink.java 15460 2011-05-18 17:41:43Z pabercrombie $
 */
public class KMLNetworkLink extends KMLAbstractFeature implements PropertyChangeListener
{
    /** Indicates the network resource referenced by this <code>KMLNetworkLink</code>. Initially <code>null</code>. */
    protected AtomicReference<KMLRoot> networkResource = new AtomicReference<KMLRoot>();
    /**
     * Time, in milliseconds since the Epoch, at which this <code>KMLNetworkLink's</code> network resource was last
     * retrieved. Initially <code>-1</code>, indicating that the network resource has not been retrieved.
     */
    protected AtomicLong networkResourceRetrievalTime = new AtomicLong(-1);
    /**
     * Flag to indicate that the Link has been registered with the Link controller. This should happen once, when the
     * NetworkLink is first rendered.
     */
    protected boolean linkRegistered;

    /** Flag to indicate that the Link has been fetched from the hashmap. */
    protected boolean linkFetched = false;
    protected KMLLink link;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLNetworkLink(String namespaceURI)
    {
        super(namespaceURI);
    }

    /**
     * Forward property change events from the inner {@link KMLRoot} to this object's parent KMLRoot.
     *
     * @param evt Event to forward.
     */
    public void propertyChange(PropertyChangeEvent evt)
    {
        this.getRoot().firePropertyChange(evt);
    }

    public Boolean getRefreshVisibility()
    {
        return (Boolean) this.getField("refreshVisibility");
    }

    public Boolean getFlyToView()
    {
        return (Boolean) this.getField("flyToView");
    }

    public KMLLink getNetworkLink()
    {
        if (!this.linkFetched)
        {
            this.linkFetched = true;
            this.link = (KMLLink) this.getField("Link");
        }

        return this.link;
    }

    public KMLLink getUrl()
    {
        return (KMLLink) this.getField("Url");
    }

    /**
     * Returns this <code>NetworkLink's</code> <code>Link</code> element or its <code>Url</code> element, depending on
     * which is available. <code>Link</code> is the preferred method for encoding a <code>NetworkLink</code> since KML
     * version 2.1, therefore we give it priority. If a <code>Link</code> is not specified, the deprecated
     * <code>Url</code> element is returned.
     *
     * @return this <code>NetworkLink's</code> <code>Link</code> element, if one is specified. Otherwise, this
     *         <code>NetworkLink's</code> <code>Url</code> element. Returns <code>null</code> if neither
     *         <code>Link</code> or a <code>Url</code> are specified.
     */
    protected KMLLink getLinkOrUrl()
    {
        KMLLink link = this.getNetworkLink();
        if (link != null)
            return link;

        return this.getUrl();
    }

    /**
     * Indicates the network resource referenced by this <code>KMLNetworkLink</code>. This returns <code>null</code> if
     * this link has no resource.
     *
     * @return this link's network resource, or <code>null</code> to indicate that this link has no resource.
     *
     * @see #setNetworkResource(KMLRoot)
     */
    public KMLRoot getNetworkResource()
    {
        return networkResource.get();
    }

    /**
     * Specifies the network resource referenced by this <code>KMLNetworkLink</code>, or <code>null</code> if this link
     * has no resource. If the specified <code>kmlRoot</code> is not <code>null</code, this link draws the
     * <code>kmlRoot</code> during preRendering and rendering, and forwards property change events from the
     * <code>kmlRoot</code> to this link's property change listeners.
     *
     * @param kmlRoot the network resource referenced by this <code>KMLNetworkLink</code>. May be <code>null</code>.
     *
     * @see #getNetworkResource()
     */
    public void setNetworkResource(KMLRoot kmlRoot)
    {
        // Remove any property change listeners previously set on the KMLRoot. This eliminates dangling references from
        // the KMLNetworkLink to its previous KMLRoot.
        KMLRoot resource = this.getNetworkResource();
        if (resource != null)
            resource.removePropertyChangeListener(this);

        this.networkResource.set(kmlRoot);
        this.networkResourceRetrievalTime.set(System.currentTimeMillis());

        // Set up to listen for property change events on the KMLRoot. KMLNetworkLink must forward REPAINT and REFRESH
        // property change events from its internal KMLRoot to its parent KMLRoot to support BrowserBalloon repaint
        // events and recursive KMLNetworkLink elements.
        if (kmlRoot != null)
            kmlRoot.addPropertyChangeListener(this);
    }

    /**
     * Pre-renders the network resource referenced by this <code>KMLNetworkLink</code>. If this link must retrieve its
     * network resource, this initiates a retrieval and does nothing until the resource is retrieved and loaded. Once
     * the network resource is retrieved and loaded, this calls <code>{@link #setNetworkResource(KMLRoot)}</code> to
     * specify this link's new network resource, and sends an <code>{@link gov.nasa.worldwind.avlist.AVKey#RETRIEVAL_STATE_SUCCESSFUL}</code>
     * property change event to this link's property change listeners.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     *
     * @see #getNetworkResource()
     */
    @Override
    protected void doPreRender(KMLTraversalContext tc, DrawContext dc)
    {
        // TODO: Implement support for NetworkLink refresh behavior. See refreshVisibility, KMLLink.refreshMode,
        // KMLLink.refreshInterval, KMLLink.viewRefreshMode, and KMLLink.viewRefreshTime.

        // Register the Link element with the Link controller, if it has not been registered already.
        if (!this.linkRegistered && this.getLinkOrUrl() != null)
        {
            this.getRoot().getLinkController().addLink(this.getLinkOrUrl());
            this.linkRegistered = true;
        }

        if (this.mustRetrieveNetworkResource())
            this.requestResource(dc);

        if (this.getNetworkResource() != null)
            this.getNetworkResource().preRender(tc, dc);
    }

    /**
     * Renders the network resource referenced by this <code>KMLNetworkLink</code>. This does nothing if this link has
     * no network resource.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @Override
    protected void doRender(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getNetworkResource() != null)
            this.getNetworkResource().render(tc, dc);
    }

    /**
     * Returns whether this <code>KMLNetworkLink</code> must retrieve its network resource. This always returns
     * <code>false</code> if this <code>KMLNetworkLink</code> has no <code>KMLLink</code>.
     *
     * @return <code>true</code> if this <code>KMLNetworkLink</code> must retrieve its network resource, otherwise
     *         <code>false</code>.
     */
    protected boolean mustRetrieveNetworkResource()
    {
        KMLLink link = this.getLinkOrUrl();
        if (link == null)
            return false; // If both the Link and the Url are null, then there's nothing to retrieve.

        // The resource must be retrieved if the link has been updated since the resource was
        // last retrieved, or if the resource has never been retrieved.
        return this.getNetworkResource() == null || link.getUpdateTime() > this.networkResourceRetrievalTime.get();
    }

    /**
     * Thread's off a task to determine whether the resource is local or remote and then retrieves it either from disk
     * cache or a remote server.
     *
     * @param dc the current draw context.
     */
    protected void requestResource(DrawContext dc)
    {
        if (WorldWind.getTaskService().isFull())
            return;

        KMLLink link = this.getLinkOrUrl();
        if (link == null)
            return; // If both the Link and the Url are null, then there's nothing to retrieve.

        String address = link.getAddress(dc);
        if (address != null)
            address = address.trim();

        if (WWUtil.isEmpty(address))
            return;

        WorldWind.getTaskService().addTask(new RequestTask(this, address));
    }

    /**
     * Initiates a retrieval of the network resource referenced by this <code>KMLNetworkLink</code>. Once the network
     * resource is retrieved and loaded, this calls <code>{@link #setNetworkResource(KMLRoot)}</code> to specify this
     * link's new network resource, and sends an <code>{@link gov.nasa.worldwind.avlist.AVKey#RETRIEVAL_STATE_SUCCESSFUL}</code>
     * property change event to this link's property change listeners.
     * <p/>
     * This does nothing if this <code>KMLNetworkLink</code> has no <code>KMLLink</code>.
     *
     * @param address the address of the resource to retrieve
     */
    protected void retrieveNetworkResource(String address)
    {
        // Treat the address as either a path to a local document, or as an absolute URL to a remote document. If the
        // address references a remote document, this attempts to retrieve it and loads the document once retrieval
        // succeeds. This does not handle absolute local file paths; absolute local file paths are not supported by the
        // KML specification. However, a NetworkLink may reference an absolute local file by specifying an absolute
        // URL with the "file:" protocol.
        //
        // Don't cache remote documents if this link's refreshMode is onInterval, or if this link's viewRefreshMode is
        // onStop. These documents are transient and therefore should be stored in a temporary location.

        Object o = this.getRoot().resolveReference(address, this.isLinkCacheable());
        if (o != null && o instanceof KMLRoot)
        {
            this.setNetworkResource((KMLRoot) o);
            this.getRoot().firePropertyChange(AVKey.RETRIEVAL_STATE_SUCCESSFUL, null, KMLNetworkLink.this);
        }
    }

    /**
     * Indicates whether the network resource references by this <code>KMLNetworkLink</code> should be retrieved to the
     * World Wind cache or to a temporary location. This returns <code>true</code> if all of the following conditions
     * are met, and <code>false</code> otherwise:
     * <p/>
     * <ul> <li>This network link has either a <code>Link</code> or a <code>Url</code> element.</li> <li>The Link or Url
     * element's <code>refreshMode</code> is not <code>onInterval</code>.</li> <li>The Link or Url element's
     * <code>viewRefreshMode</code> is not <code>onStop</code>.</li> </ul>
     *
     * @return <code>true</code> if this link's network resource can should be stored in a cache, or <code>false</code>
     *         if it should be stored in a temporary location.
     */
    public boolean isLinkCacheable()
    {
        KMLLink link = this.getLinkOrUrl();
        return link != null
            && !KMLConstants.ON_INTERVAL.equalsIgnoreCase(link.getRefreshMode())
            && !KMLConstants.ON_STOP.equalsIgnoreCase(link.getViewRefreshMode());
    }

    /** Attempts to find this network link resource file locally, and if that fails attempts to find it remotely. */
    protected static class RequestTask implements Runnable
    {
        /** The link associated with this request. */
        protected final KMLNetworkLink link;
        /** The resource's address. */
        protected final String address;

        /**
         * Construct a request task for a specified network link resource.
         *
         * @param link    the network link for which to construct the request task.
         * @param address the address of the resource to request.
         */
        protected RequestTask(KMLNetworkLink link, String address)
        {
            if (link == null)
            {
                String message = Logging.getMessage("nullValue.ObjectIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            if (address == null)
            {
                String message = Logging.getMessage("nullValue.PathIsNull");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.link = link;
            this.address = address;
        }

        public void run()
        {
            if (Thread.currentThread().isInterrupted())
                return; // the task was cancelled because it's a duplicate or for some other reason

            this.link.retrieveNetworkResource(this.address);
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            RequestTask that = (RequestTask) o;

            if (!this.address.equals(that.address))
                return false;
            //noinspection RedundantIfStatement
            if (!this.link.equals(that.link))
                return false;

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = link.hashCode();
            result = 31 * result + address.hashCode();
            return result;
        }

        public String toString()
        {
            return this.address;
        }
    }
}
