/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.examples.util;

import gov.nasa.worldwind.util.WWIO;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;

/**
 * A collection of static utility methods used by the example programs.
 *
 * @author tag
 * @version $ID$
 */
public class ExampleUtil
{
    /**
     * Downloads a specified zip file, unzips it and saves it in a temporary directory.
     *
     * @param url    the URL of the zip file.
     * @param suffix the suffix to give the temp file.
     *
     * @return a {@link File} for the temp file.
     */
    public static File downloadAndUnzipToTempFile(URL url, String suffix)
    {
        try
        {
            ByteBuffer buffer = WWIO.readURLContentToBuffer(url);
            File file = WWIO.saveBufferToTempFile(buffer, WWIO.getFilename(url.toString()));

            buffer = WWIO.readZipEntryToBuffer(file, null);
            return WWIO.saveBufferToTempFile(buffer, suffix);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }
}
