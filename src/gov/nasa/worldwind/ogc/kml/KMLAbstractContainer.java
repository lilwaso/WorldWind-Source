/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.ogc.kml.impl.KMLTraversalContext;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>Container</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLAbstractContainer.java 15159 2011-03-30 19:40:52Z pabercrombie $
 */
public class KMLAbstractContainer extends KMLAbstractFeature
{
    protected ArrayList<KMLAbstractFeature> features = new ArrayList<KMLAbstractFeature>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected KMLAbstractContainer(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAbstractFeature)
            this.addFeature((KMLAbstractFeature) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public List<KMLAbstractFeature> getFeatures()
    {
        return this.features;
    }

    protected void addFeature(KMLAbstractFeature feature)
    {
        this.features.add(feature);
    }

    /**
     * Indicates whether this <code>KMLAbstractContainer</code> is active and should be rendered on the specified
     * <code>DrawContext</code>. This returns <code>true</code> if this container's <code>visibility</code> is
     * unspecified (<code>null</code>) or is set to <code>true</code>.
     * <p/>
     * Regions do not apply directly to KML containers, because a descendant features can override the container's
     * Region with its own. Since a descendant Region may be larger or have a less restrictive LOD range than this
     * container, we cannot determine the visibility of the entire tree based on this container's Region. A container's
     * Region is inherited by any descendants that do not specify a Region, and Region visibility is determined at the
     * leaf nodes.
     *
     * @param tc the current KML traversal context. This parameter is ignored.
     * @param dc the draw context. This parameter is ignored.
     *
     * @return <code>true</code> if this container should be rendered, otherwise <code>false</code>.
     */
    @Override
    protected boolean isFeatureActive(KMLTraversalContext tc, DrawContext dc)
    {
        return this.getVisibility() == null || this.getVisibility();
    }

    /**
     * Pre-renders the KML features held by this <code>KMLAbstractContainer</code>.
     * <p/>
     * Pushes this container's Region on the KML traversal context's region stack before rendering the features, and
     * pops the Region off the stack afterward. Descendant features use the KML traversal context's region stack to
     * inherit Regions from parent containers.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @Override
    protected void doPreRender(KMLTraversalContext tc, DrawContext dc)
    {
        this.beginRendering(tc, dc);
        try
        {
            this.preRenderFeatures(tc, dc);
        }
        finally
        {
            this.endRendering(tc, dc);
        }
    }

    /**
     * Renders the KML features held by this <code>KMLAbstractContainer</code>.
     * <p/>
     * Pushes this container's Region on the KML traversal context's region stack before rendering the features, and
     * pops the Region off the stack afterward. Descendant features use the KML traversal context's region stack to
     * inherit Regions from parent containers.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @Override
    protected void doRender(KMLTraversalContext tc, DrawContext dc)
    {
        this.beginRendering(tc, dc);
        try
        {
            this.renderBalloon(tc, dc);
            this.renderFeatures(tc, dc);
        }
        finally
        {
            this.endRendering(tc, dc);
        }
    }

    /**
     * Prepares this KML container for rendering. This pushes this container's Region on the KML traversal context's
     * region stack. Descendant features use the KML traversal context's region stack to inherit Regions from parent
     * containers. This must be followed by a call to <code>endRendering</code>.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void beginRendering(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getRegion() != null)
            tc.pushRegion(this.getRegion());
    }

    /**
     * Restores any rendering state changed during rendering. This pops this container's Region off the KML traversal
     * context's region stack to restore the previous container's Region (if any). This must be preceded by a call to
     * <code>beginRendering</code>.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void endRendering(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getRegion() != null)
            tc.popRegion();
    }

    /**
     * PreRenders this KML container's list of KML features, in the order they appear in the list. This does nothing if
     * the list of features is empty.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    protected void preRenderFeatures(KMLTraversalContext tc, DrawContext dc)
    {
        List<KMLAbstractFeature> containers = new ArrayList<KMLAbstractFeature>();

        // PreRender non-container child features first, and containers second. This ensures that features closer to the
        // root are rendered before features deeper in the tree. In the case of an image pyramid of GroundOverlays,
        // this causes the deeper nested overlays (which are typically more detailed) to render on top of the more
        // general overlay that is higher in the tree.
        for (KMLAbstractFeature feature : this.getFeatures())
        {
            if (feature instanceof KMLAbstractContainer || feature instanceof KMLNetworkLink)
                containers.add(feature);
            else
                feature.preRender(tc, dc);
        }

        // Now preRender the containers
        for (KMLAbstractFeature feature : containers)
        {
            feature.preRender(tc, dc);
        }
    }

    /**
     * Draws this KML container's list of KML features, in the order they appear in the list. This does nothing if the
     * list of features is empty.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    protected void renderFeatures(KMLTraversalContext tc, DrawContext dc)
    {
        List<KMLAbstractFeature> containers = new ArrayList<KMLAbstractFeature>();

        // Render non-container child features first, and containers second. This ensures that features closer to the
        // root are rendered before features deeper in the tree. In the case of an image pyramid of GroundOverlays,
        // this causes the deeper nested overlays (which are typically more detailed) to render on top of the more
        // general overlay that is higher in the tree.
        for (KMLAbstractFeature feature : this.getFeatures())
        {
            if (feature instanceof KMLAbstractContainer || feature instanceof KMLNetworkLink)
                containers.add(feature);
            else
                feature.render(tc, dc);
        }

        // Now render the containers
        for (KMLAbstractFeature feature : containers)
        {
            feature.render(tc, dc);
        }
    }

    /**
     * Draws the <code>{@link gov.nasa.worldwind.render.Balloon}</code> associated with this KML container. This does
     * nothing if there is no <code>Balloon</code> associated with this container.
     *
     * @param tc the current KML traversal context.
     * @param dc the current draw context.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void renderBalloon(KMLTraversalContext tc, DrawContext dc)
    {
        if (this.getBalloon() != null)
            this.getBalloon().render(dc);
    }
}
