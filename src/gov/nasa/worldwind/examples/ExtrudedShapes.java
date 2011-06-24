/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.render.airspaces.*;
import gov.nasa.worldwind.render.airspaces.Polygon;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.zip.*;

/**
 * Demonstrates how to create {@link ExtrudedPolygon}s with cap and side textures. The polygon geometry is retrieved
 * from a world wind data site, as is the image applied to the extruded polygon's sides.
 *
 * @author tag
 * @version $Id: ExtrudedShapes.java 15312 2011-04-18 20:39:24Z tgaskins $
 */
public class ExtrudedShapes extends ApplicationTemplate
{
    protected static final String DEMO_AIRSPACES_URL =
        "http://worldwind.arc.nasa.gov/java/demos/data/AirspaceBuilder-DemoShapes.zip";
    protected static String DEFAULT_IMAGE_URL = "http://worldwind.arc.nasa.gov/java/demos/Images/build123sm.jpg";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            try
            {
                // Create a layer for the shapes.
                RenderableLayer layer = new RenderableLayer();
                layer.setName("Extruded Shapes");
                layer.setPickEnabled(true);

                // Retrieve the geometry from the World Wind demo site.
                List<Airspace> airspaces = new ArrayList<Airspace>();
                loadAirspacesFromURL(new URL(DEMO_AIRSPACES_URL), airspaces);

                // Define attributes for the shapes.
                ShapeAttributes sideAttributes = new BasicShapeAttributes();
                sideAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
                sideAttributes.setOutlineMaterial(Material.DARK_GRAY);

                ShapeAttributes capAttributes = new BasicShapeAttributes(sideAttributes);
                capAttributes.setInteriorMaterial(Material.GRAY);

                // Construct the extruded polygons from the demo data.
                int n = 0, m = 0;
                for (Airspace airspace : airspaces)
                {
                    if (airspace instanceof Polygon) // only polygons in the demo data are used
                    {
                        Polygon pgonAirspace = (Polygon) airspace;

                        // Collect the images to be applied to the shape's sides.
                        ArrayList<String> textures = new ArrayList<String>();
                        for (int i = 0; i < pgonAirspace.getLocations().size(); i++)
                        {
                            textures.add(DEFAULT_IMAGE_URL);
                        }

                        // Construct the extruded polygon. Use the default texture coordinates.
                        double height = 40; // building height
                        ExtrudedPolygon quad = new ExtrudedPolygon(pgonAirspace.getLocations(), height, textures);

                        // Apply the shape's attributes. Note the separate attributes for cap and sides.
                        quad.setSideAttributes(sideAttributes);
                        quad.setCapAttributes(capAttributes);

                        // Specify a cap for the extruded polygon, specifying its texture coordinates and image.
                        if (pgonAirspace.getLocations().size() == 4)
                        {
                            float[] texCoords = new float[] {0, 0, 1, 0, 1, 1, 0, 1};
                            quad.setCapImageSource("images/32x32-icon-nasa.png", texCoords, 4);
                        }

                        // Add the shape to the layer.
                        layer.addRenderable(quad);

                        ++n;
                        m += ((Polygon) airspace).getLocations().size();
                    }
                }

                System.out.printf("NUM SHAPES = %d, NUM SIDES = %d\n", n, m);

                // Add the layer to the model.
                insertBeforePlacenames(this.getWwd(), layer);

                // Make sure the new layer is shown in the layer manager.
                this.getLayerPanel().update(this.getWwd());

                // Adjust the view so that it looks at the buildings.
                View view = getWwd().getView();
                view.setEyePosition(Position.fromDegrees(47.656, -122.306, 1e3));

                // This is how a select listener would notice that one of the shapes was picked.
                getWwd().addSelectListener(new SelectListener()
                {
                    public void selected(SelectEvent event)
                    {
                        if (event.getTopObject() instanceof ExtrudedPolygon)
                            System.out.println("EXTRUDED POLYGON");
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    protected static void loadAirspacesFromURL(URL url, Collection<Airspace> airspaces)
    {
        File file = null;
        try
        {
            file = File.createTempFile("AirspaceBuilder-TempFile", null);

            InputStream is = url.openStream();
            OutputStream os = new FileOutputStream(file);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0)
            {
                os.write(buffer, 0, length);
            }

            is.close();
            os.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if (file == null)
            return;

        try
        {
            ZipFile zipFile = new ZipFile(file);

            ZipEntry entry = null;
            for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); entry = e.nextElement())
            {
                if (entry == null)
                    continue;

                String name = entry.getName();
                name = getFileName(name);

                if (!(name.startsWith("gov.nasa.worldwind.render.airspaces") && name.endsWith(".xml")))
                    continue;

                String[] tokens = name.split("-");

                try
                {
                    Class c = Class.forName(tokens[0]);
                    Airspace airspace = (Airspace) c.newInstance();
                    BufferedReader input = new BufferedReader(new InputStreamReader(zipFile.getInputStream(entry)));
                    String s = input.readLine();
                    airspace.restoreState(s);
                    airspaces.add(airspace);

                    if (tokens.length >= 2)
                    {
                        airspace.setValue(AVKey.DISPLAY_NAME, tokens[1]);
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static String getFileName(String s)
    {
        int index = s.lastIndexOf("/");
        if (index == -1)
            index = s.lastIndexOf("\\");
        if (index != -1 && index < s.length())
            return s.substring(index + 1, s.length());
        return s;
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Extruded Polygons on Ground", AppFrame.class);
    }
}
