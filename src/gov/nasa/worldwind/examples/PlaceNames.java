/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples;

import javax.swing.*;
import java.awt.*;

/**
 * Example of customizing which place names (names of countries, oceans, cities, etc) are displayed. The panel on the
 * left side of the window lists all of the available place name categories. Click the check boxes to turn individual
 * categories on or off.
 *
 * @author jparsons
 * @version $Id: PlaceNames.java 15655 2011-06-16 22:08:28Z pabercrombie $
 */
public class PlaceNames extends ApplicationTemplate
{
    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);
            this.getLayerPanel().add(makeControlPanel(),  BorderLayout.SOUTH);
        }


        private JPanel makeControlPanel()
        { 
            return new PlaceNamesPanel(this.getWwd());
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Place Names", AppFrame.class);
    }
}
