/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.examples;

import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.examples.util.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.*;

import java.awt.*;
import java.net.URL;

/**
 * @author dcollins
 * @version $Id: BrowserBalloons.java 15037 2011-03-21 17:07:07Z dcollins $
 */
public class BrowserBalloons extends ApplicationTemplate
{
    protected static final String BALLOON_PAGE_URL
        = "http://worldwind.arc.nasa.gov/java/demos/data/BrowserBalloonExample.html";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected HotSpotController hotSpotController;
        protected BalloonController balloonController;

        public AppFrame()
        {
            // Add a controller to send input events to the BrowserBalloons.
            this.hotSpotController = new HotSpotController(this.getWwd());
            // Add a controller to handle link and navigation events in the BrowserBalloons
            this.balloonController = new BalloonController(this.getWwd());

            this.makeBrowserBalloon();

            // Size the World Window to provide enough screen space for the browser balloon and center it on the screen.
            Dimension size = new Dimension(1200, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void makeBrowserBalloon()
        {
            URL url = WWIO.makeURL(BALLOON_PAGE_URL);
            String htmlString = null;

            try
            {
                // Read the URL content into a String using the default encoding (UTF-8).
                htmlString = WWIO.readURLContentToString(url, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            if (htmlString == null)
                htmlString = Logging.getMessage("URLRetriever.ErrorOpeningConnection", url.getHost());

            // Create a Browser Balloon attached to the globe, and pointing at the NASA headquarters in Washington, D.C.
            // We use the balloon page's URL as its resource resolver to handle relative paths in the page content.
            AbstractBrowserBalloon b = new GlobeBrowserBalloon(htmlString, Position.fromDegrees(38.883056, -77.016389));
            b.setResourceResolver(url);

            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(Size.fromPixels(700, 500));
            b.setAttributes(attrs);

            RenderableLayer layer = new RenderableLayer();
            layer.addRenderable(b);
            insertBeforeCompass(this.getWwd(), layer);
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the browser balloon is centered in the viewport.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 60);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -77.016389);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 9500000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 45);

        start("World Wind Browser Balloons", AppFrame.class);
    }
}
