/*
 * Copyright (C) 2001, 2010 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;

import java.awt.*;

/**
 * Example of Balloon usage.
 *
 * @author pabercrombie
 * @version $Id: Balloons.java 15026 2011-03-20 22:54:28Z dcollins $
 */
public class Balloons extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            /// Create screen attached balloon
            Balloon balloon = new ScreenAnnotationBalloon("Balloon attached to screen",
                new Point(200, 500));
            layer.addRenderable(balloon);

            BasicBalloonAttributes attributes = new BasicBalloonAttributes();
            attributes.setLeaderShape(AVKey.SHAPE_NONE);
            balloon.setAttributes(attributes);

            /// Create globe attached balloon
            balloon = new GlobeAnnotationBalloon("<b>Lake Tahoe</b><br/>Balloon attached to globe",
                Position.fromDegrees(39.108, -120.0528));
            layer.addRenderable(balloon);

            attributes = new BasicBalloonAttributes();
            balloon.setAttributes(attributes);

            BasicBalloonAttributes highlightAttributes = new BasicBalloonAttributes();
            highlightAttributes.setTextColor(Color.RED);
            balloon.setHighlightAttributes(highlightAttributes);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Balloons", AppFrame.class);
    }
}
