/*
Copyright (C) 2001, 2011 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

/**
 * Shows how to turn on stereo, which is requested via a Java VM property. This example sets the property directly, but
 * it's more conventional to set it on the java command line.
 *
 * @author tag
 * @version $Id: Stereo.java 15500 2011-05-25 00:03:04Z tgaskins $
 */
public class Stereo extends ApplicationTemplate
{
    public static void main(String[] args)
    {
        // Set the stereo.mode property to request stereo. Request red-blue anaglyph in this case. Can also request
        // "device" if the display device supports stereo directly. To prevent stereo, leave the property unset or set
        // it to an empty string.
        System.setProperty("gov.nasa.worldwind.stereo.mode", "redblue");

        ApplicationTemplate.start("World Wind Anaglyph Stereo", AppFrame.class);
    }
}
