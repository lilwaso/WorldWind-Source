/*
Copyright (C) 2001, 2011 United States Government as represented by
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
 * Illustrates how to use a World Wind <code>{@link Balloon}</code> to display on-screen information to the user in the
 * form of a screen-aligned text balloon. There are two abstract balloon types: <code>{@link ScreenBalloon}</code> which
 * displays a balloon at a point on the screen, and <code>{@link GlobeBalloon}</code> which displays a balloon attached
 * to a position on the Globe. For each abstract balloon type, there are two concrete types: AnnotationBalloon which
 * provides support for simple text content with an optional image, and BrowserBalloon which provides support for
 * complex HTML, JavaScript, and Flash content.
 * <p/>
 * <strong>Balloon Content</strong> <br/> A Balloon's content is specified by calling <code>{@link
 * Balloon#setText(String)}</code>, and its visual attributes are specified by calling <code>{@link
 * Balloon#setAttributes(gov.nasa.worldwind.render.BalloonAttributes)}</code> with an instance of <code>{@link
 * BalloonAttributes}</code>.
 * <p/>
 * <strong>ScreenBalloon</strong> <br/> ScreenBalloons display a screen-aligned balloon at a point on the screen. There
 * are two concrete ScreenBalloon types: <ul> <li><code>{@link ScreenAnnotationBalloon}</code> - a screen-attached
 * balloon with support for multi-line text, a background image, simple HTML text markup, and simple text styling
 * attributes.</li> <li><code>{@link ScreenBrowserBalloon}</code> - a screen-attached balloon with support for HTML,
 * JavaScript, and Flash content.</li> </ul>
 * <p/>
 * <strong>GlobeBalloon</strong> <br/> GlobeBalloons display a screen-aligned balloon attached to a position on the
 * Globe. <ul> <li><code>{@link GlobeAnnotationBalloon}</code> - a Globe-attached balloon with support for multi-line
 * text, a background image, simple HTML text markup, and simple text styling attributes.</li> <li><code>{@link
 * GlobeBrowserBalloon}</code> - a Globe-attached balloon with support for HTML, JavaScript, and Flash content.</li>
 * </ul>
 *
 * @author pabercrombie
 * @version $Id: Balloons.java 15664 2011-06-17 23:08:20Z dcollins $
 */
public class Balloons extends ApplicationTemplate
{
    protected static final String BROWSER_BALLOON_CONTENT_URL
        = "http://worldwind.arc.nasa.gov/java/demos/data/BrowserBalloonExample.html";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        protected HotSpotController hotSpotController;
        protected BalloonController balloonController;
        protected RenderableLayer layer;

        public AppFrame()
        {
            super(true, true, false);

            // Add a controller to send input events to BrowserBalloons.
            this.hotSpotController = new HotSpotController(this.getWwd());
            // Add a controller to handle link and navigation events in BrowserBalloons.
            this.balloonController = new BalloonController(this.getWwd());

            // Create a layer to display the balloons.
            this.layer = new RenderableLayer();
            this.layer.setName("Balloons");
            insertBeforeCompass(getWwd(), this.layer);
            this.getLayerPanel().update(this.getWwd());

            // Add an AnnotationBalloon and a BrowserBalloon to the balloon layer.
            this.makeAnnotationBalloon();
            this.makeBrowserBalloon();

            // Size the World Window to provide enough screen space for the BrowserBalloon, and center the World Window
            // on the screen.
            Dimension size = new Dimension(1200, 800);
            this.setPreferredSize(size);
            this.pack();
            WWUtil.alignComponent(null, this, AVKey.CENTER);
        }

        protected void makeAnnotationBalloon()
        {
            Balloon b = new ScreenAnnotationBalloon("<b>AnnotationBalloon</b> attached to the screen",
                new Point(50, 300));

            BalloonAttributes attrs = new BasicBalloonAttributes();
            // Size the balloon to fit its text, place its lower-left corner at the point, put event padding between the
            // balloon's text and its sides, and disable the balloon's leader.
            attrs.setSize(Size.fromPixels(300, 50));
            attrs.setOffset(new Offset(0d, 0d, AVKey.PIXELS, AVKey.PIXELS));
            attrs.setInsets(new Insets(10, 10, 10, 10)); // .
            attrs.setLeaderShape(AVKey.SHAPE_NONE);
            // Configure the balloon's colors to display white text over a semi-transparent black background.
            attrs.setTextColor(Color.WHITE);
            attrs.setInteriorMaterial(Material.BLACK);
            attrs.setInteriorOpacity(0.6);
            attrs.setOutlineMaterial(Material.WHITE);
            b.setAttributes(attrs);

            this.layer.addRenderable(b);
        }

        protected void makeBrowserBalloon()
        {
            URL url = WWIO.makeURL(BROWSER_BALLOON_CONTENT_URL);
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

            // Size the balloon to provide enough space for its content.
            BalloonAttributes attrs = new BasicBalloonAttributes();
            attrs.setSize(Size.fromPixels(650, 500));
            b.setAttributes(attrs);

            this.layer.addRenderable(b);
        }
    }

    public static void main(String[] args)
    {
        // Configure the initial view parameters so that the browser balloon is centered in the viewport.
        Configuration.setValue(AVKey.INITIAL_LATITUDE, 60);
        Configuration.setValue(AVKey.INITIAL_LONGITUDE, -85);
        Configuration.setValue(AVKey.INITIAL_ALTITUDE, 9500000);
        Configuration.setValue(AVKey.INITIAL_PITCH, 45);

        ApplicationTemplate.start("World Wind Balloons", AppFrame.class);
    }
}
