/*
Copyright (C) 2001, 2009 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.examples.dataimport;

import gov.nasa.worldwind.examples.ApplicationTemplate;
import gov.nasa.worldwind.examples.util.ExampleUtil;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.*;
import gov.nasa.worldwind.terrain.*;
import gov.nasa.worldwind.util.WWIO;

import javax.swing.*;
import java.awt.*;
import java.io.File;

/**
 * This example demonstrates how to import elevation data. It imports a GeoTIFF file containing elevation data and
 * creates an {@link gov.nasa.worldwind.globes.ElevationModel} for it.
 *
 * @author tag
 * @version $Id: ImportElevations.java 15507 2011-05-25 22:17:30Z tgaskins $
 */
public class ImportElevations extends ApplicationTemplate
{
    // The data to import. TODO: need a more distinguishable set of elevations; these are very shallow
    protected static final String ELEVATIONS_URL =
        "http://worldwind.arc.nasa.gov/java/demos/data/wa-snohomish-dtm-16bit.tif.zip";

    public static class AppFrame extends ApplicationTemplate.AppFrame
    {
        public AppFrame()
        {
            super(true, true, false);

            // Show the WAIT cursor because the import may take a while.

            this.setCursor(new Cursor(Cursor.WAIT_CURSOR));

            // Import the elevations on a thread other than the event-dispatch thread to avoid freezing the UI.

            Thread t = new Thread(new Runnable()
            {
                public void run()
                {
                    importElevations();

                    // Restore the cursor.

                    setCursor(Cursor.getDefaultCursor());
                }
            });

            t.start();
        }

        protected void importElevations()
        {
            try
            {
                // Download the data and save it in a temp file.
                File sourceFile = ExampleUtil.downloadAndUnzipToTempFile(WWIO.makeURL(ELEVATIONS_URL), ".tif");

                // Create a local elevation model from the data.

                final LocalElevationModel elevationModel = new LocalElevationModel();
                elevationModel.addElevations(sourceFile);

                SwingUtilities.invokeLater(new Runnable()
                {
                    public void run()
                    {
                        // Get the WorldWindow's current elevation model.

                        Globe globe = AppFrame.this.getWwd().getModel().getGlobe();
                        ElevationModel currentElevationModel = globe.getElevationModel();

                        // Add the new elevation model to the globe.

                        if (currentElevationModel instanceof CompoundElevationModel)
                            ((CompoundElevationModel) currentElevationModel).addElevationModel(elevationModel);
                        else
                            globe.setElevationModel(elevationModel);

                        // Set the view to look at the imported elevations, although they might be hard to detect. To
                        // make them easier to detect, replace the globe's CompoundElevationModel with the new elevation
                        // model rather than adding it.

                        Sector modelSector = elevationModel.getSector();
                        AppFrame.this.getWwd().getView().goTo(new Position(modelSector.getCentroid(), 0), 50e3);
                    }
                });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args)
    {
        ApplicationTemplate.start("World Wind Imagery Import", ImportElevations.AppFrame.class);
    }
}
