/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.util.HotSpotController;
import gov.nasa.worldwind.examples.util.layertree.LayerTree;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.util.WWUtil;

import java.awt.*;

/**
 * Example of using {@link gov.nasa.worldwind.util.tree.BasicTree} to display a list of layers.
 *
 * @author pabercrombie
 * @version $Id: LayerTreeUsage.java 15668 2011-06-18 00:35:07Z pabercrombie $
 */
public class LayerTreeUsage extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected LayerTree layerTree;
        protected RenderableLayer hiddenLayer;

        protected HotSpotController controller;

        public AppFrame()
        {
            super(true, false, false); // Don't include the layer panel; we're using the on-screen layer tree.

            // Add the on-screen layer tree, refreshing model with the WorldWindow's current layer list. We
            // intentionally refresh the tree's model before adding the layer that contains the tree itself. This
            // prevents the tree's layer from being displayed in the tree itself.
            this.layerTree = new LayerTree();
            this.layerTree.getModel().refresh(this.getWwd().getModel().getLayers());

            // Set up a layer to display the on-screen layer tree in the WorldWindow. This layer is not displayed in
            // the layer tree's model. Doing so would enable the user to hide the layer tree display with no way of
            // bringing it back.
            this.hiddenLayer = new RenderableLayer();
            this.hiddenLayer.addRenderable(this.layerTree);
            this.getWwd().getModel().getLayers().add(this.hiddenLayer);

            // Add a controller to handle input events on the layer tree.
            this.controller = new HotSpotController(this.getWwd());

            // Size the World Window to take up the space typically used by the layer panel. This illustrates the
            // screen space gained by using the on-screen layer tree.
            Dimension size = new Dimension(1000, 600);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Layer Tree", AppFrame.class);
    }
}
