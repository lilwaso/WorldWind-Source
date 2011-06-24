/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.WWUtil;

import java.util.ArrayList;

/**
 * @author tag
 * @version $Id: Paths.java 14616 2011-02-01 00:38:17Z tgaskins $
 */
public class Paths extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            RenderableLayer layer = new RenderableLayer();

            // Create and set an attribute bundle.
            ShapeAttributes attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(WWUtil.makeRandomColor(null)));
            attrs.setOutlineWidth(2d);

            // Create a path, set some of its properties and set its attributes.
            ArrayList<Position> pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(28, -102, 1e4));
            pathPositions.add(Position.fromDegrees(35, -100, 1e4));
            Path path = new Path(pathPositions);
            path.setAttributes(attrs);
            path.setVisible(true);
            path.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
            path.setPathType(AVKey.GREAT_CIRCLE);
            layer.addRenderable(path);

            // Create a path that uses all default values.
            pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(28, -104, 1e4));
            pathPositions.add(Position.fromDegrees(35, -102, 1e4));
            path = new Path(pathPositions);
            layer.addRenderable(path);

            // Create a path with more than two positions and closed.
            pathPositions = new ArrayList<Position>();
            pathPositions.add(Position.fromDegrees(28, -106, 4e4));
            pathPositions.add(Position.fromDegrees(35, -104, 4e4));
            pathPositions.add(Position.fromDegrees(35, -107, 4e4));
            pathPositions.add(Position.fromDegrees(28, -107, 4e4));
            path = new Path(pathPositions);
            path.setAltitudeMode(WorldWind.ABSOLUTE);
            path.setExtrude(true);
            path.setPathType(AVKey.GREAT_CIRCLE);

            attrs = new BasicShapeAttributes();
            attrs.setOutlineMaterial(new Material(WWUtil.makeRandomColor(null)));
            attrs.setInteriorMaterial(new Material(WWUtil.makeRandomColor(null)));
            attrs.setOutlineWidth(2);
            path.setAttributes(attrs);

            layer.addRenderable(path);

            // Add the layer to the model.
            insertBeforeCompass(getWwd(), layer);

            // Update layer panel
            this.getLayerPanel().update(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Paths", AppFrame.class);
    }
}
