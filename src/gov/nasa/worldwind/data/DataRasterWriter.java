/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

/**
 * @author dcollins
 * @version $Id: DataRasterWriter.java 14337 2010-12-29 04:38:03Z tgaskins $
 */
public interface DataRasterWriter
{
    String[] getMimeTypes(); // TODO: remove

    String[] getSuffixes(); // TODO: remove

    boolean canWrite(DataRaster raster, String formatSuffix, java.io.File file);

    void write(DataRaster raster, String formatSuffix, java.io.File file) throws java.io.IOException;
}
