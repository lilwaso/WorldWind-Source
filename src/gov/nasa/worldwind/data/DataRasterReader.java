/* Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.avlist.AVList;

// TODO: document class
/**
 * @author dcollins
 * @version $Id: DataRasterReader.java 14337 2010-12-29 04:38:03Z tgaskins $
 */
public interface DataRasterReader extends AVList
{
    String getDescription(); // TODO: remove

    String[] getMimeTypes(); // TODO: remove

    String[] getSuffixes(); // TODO: remove

    /**
     * Indicates whether this reader can read a specified data source.
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers.
     *
     * @return true if this reader can read the data source, otherwise false.
     */
    boolean canRead(Object source, AVList params);

    DataRaster[] read(Object source, AVList params) throws java.io.IOException; // TODO: document method

    /**
     * Reads and returns the metadata from a data source.
     * <p/>
     * TODO: Why would the caller specify parameters to this method?
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers. If non-null,
     *               the metadata is added to this list, and the list reference is the return value of this method.
     *
     * @return the list of metadata read from the data source. The list is empty if the data source has no metadata.
     *
     * @throws java.io.IOException if an IO error occurs.
     */
    AVList readMetadata(Object source, AVList params) throws java.io.IOException;

    /**
     * Indicates whether a data source is imagery.
     * <p/>
     * TODO: Identify when parameters must be passed.
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers.
     *
     * @return true if the source is imagery, otherwise false.
     */
    boolean isImageryRaster(Object source, AVList params);

    /**
     * Indicates whether a data source is elevation data.
     * <p/>
     * TODO: Identify when parameters must be passed.
     *
     * @param source the source to examine.
     * @param params parameters required by certain reader implementations. May be null for most readers. TODO: Identify
     *               when parameters must be passed.
     *
     * @return true if the source is elevation data, otherwise false.
     */
    boolean isElevationsRaster(Object source, AVList params);
}