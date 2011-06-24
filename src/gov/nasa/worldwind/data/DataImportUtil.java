/*
Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.data;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.*;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;

/**
 * DataImportUtil is a collection of utility methods for common data import tasks.
 *
 * @author dcollins
 * @version $Id: DataImportUtil.java 14568 2011-01-26 22:48:17Z garakl $
 */
public class DataImportUtil
{
    // TODO: The is* methods with an "Object source" argument assume that a reader can be obtained via static calls
    // to the DataRasterReader class. But using that class statically does not provide a clean way for the application
    // or extensions to add readers.

    /**
     * Returns true if the specified input source is non-null and represents a data raster (imagery or elevation), and
     * false otherwise. The input source may be one of the following: <ul> <li>{@link String}</li> <li>{@link
     * java.io.File}</li> <li>{@link java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link
     * java.io.InputStream}</li> </ul> Supported input source formats are: <ul> <li>BIL (Band Interleaved by Line)</li>
     * </ul>
     *
     * @param source the input source reference to test as a data raster.
     *
     * @return true if the input source is data raster, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    protected static boolean canOpenAsDataRaster(Object source, AVList params)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        DataRasterReaderFactory readerFactory;
        try
        {
            readerFactory = (DataRasterReaderFactory) WorldWind.createConfigurationComponent(
                AVKey.DATA_RASTER_READER_FACTORY_CLASS_NAME);
        }
        catch (Exception e)
        {
            readerFactory = new BasicDataRasterReaderFactory();
        }

        params = (null == params) ? new AVListImpl() : params;
        DataRasterReader reader = readerFactory.findReaderFor(source, params);
        if (reader == null)
            return false;

        if (!params.hasKey(AVKey.RASTER_TYPE) && !params.hasKey(AVKey.PIXEL_FORMAT))
        {
            try
            {
                reader.readMetadata(source, params);
            }
            catch (Exception e)
            {
                // Reading the input source's metadata caused an exception. This exception does not prevent us from
                // determining if the source represents elevation data, but we want to make a note of it. Therefore we
                // log the exception with level FINE.
                String message = Logging.getMessage("generic.ExceptionWhileReading", e.getMessage());
                Logging.logger().finest(message);
            }
        }

        return (isImagery(params) || isElevations(params));
    }

    /**
     * Returns true if the specified input source is non-null and represents a data raster (imagery or elevation), and
     * false otherwise. The input source may be one of the following: <ul> <li>{@link String}</li> <li>{@link
     * java.io.File}</li> <li>{@link java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link
     * java.io.InputStream}</li> </ul> Supported input source formats are: <ul> <li>BIL (Band Interleaved by Line)</li>
     * </ul>
     *
     * @param source the input source reference to test as a data raster.
     *
     * @return true if the input source is data raster, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    public static boolean isDataRaster(Object source, AVList params)
    {
        return canOpenAsDataRaster(source, params);
    }

    /**
     * Returns true if the specified input source is non-null and represents elevation data, and false otherwise. The
     * input source may be one of the following: <ul> <li>{@link String}</li> <li>{@link java.io.File}</li> <li>{@link
     * java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link java.io.InputStream}</li> </ul> Supported input
     * source formats are: <ul> <li>BIL (Band Interleaved by Line)</li> </ul>
     *
     * @param source the input source reference to test as a elevation data.
     *
     * @return true if the input source is elevation data, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    public static boolean isElevationData(Object source)
    {
        AVList params = new AVListImpl();
        return (canOpenAsDataRaster(source, params) && isElevations(params));
    }

    /**
     * Returns true if the specified input source is non-null and represents image data, and false otherwise. The input
     * source may be one of the following: <ul> <li>{@link String}</li> <li>{@link java.io.File}</li> <li>{@link
     * java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link java.io.InputStream}</li> </ul> Supported input
     * source formats are: <ul> <li>BMP (with georeferencing file)</li> <li>GeoTIFF</li> <li>GIF (with georeferencing
     * file)</li> <li>JPEG (with georeferencing file)</li> <li>PNG (with georeferencing file)</li> <li>RPF (Raster
     * Product Format)</li> <li>TIFF (with georeferencing file)</li> <li>WBMP (with georeferencing file)</li> <li>X-PNG
     * (with georeferencing file)</li> </ul>
     *
     * @param source the input source reference to test as image data.
     *
     * @return true if the input source is image data, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    public static boolean isImageData(Object source)
    {
        AVList params = new AVListImpl();
        return (canOpenAsDataRaster(source, params) && isImagery(params));
    }

    // TODO: eliminate one of these

    /**
     * Evaluates an {@link AVList} to determine whether the associated data source is imagery.
     * <p/>
     *
     * @param params the parameters to evaluate. May be null, in which case this method returns false.
     *
     * @return true if the parameters indicate imagery, otherwise false.
     */
    public static boolean isImagery(AVList params)
    {
        return paramsIndicateImagery(params);
    }

    /**
     * Evaluates an {@link AVList} to determine whether the associated data source is imagery.
     * <p/>
     *
     * @param params the parameters to evaluate. May be null, in which case this method returns false.
     *
     * @return true if the parameters indicate imagery, otherwise false.
     */
    public static boolean paramsIndicateImagery(AVList params)
    {
        return params != null
            && (AVKey.IMAGE.equals(params.getStringValue(AVKey.PIXEL_FORMAT))
            || AVKey.RASTER_TYPE_COLOR_IMAGE.equals(params.getStringValue(AVKey.RASTER_TYPE))
            || AVKey.RASTER_TYPE_MONOCHROME_IMAGE.equals(params.getStringValue(AVKey.RASTER_TYPE)));
    }

    /**
     * Evaluates an {@link AVList} to determine whether the associated data source is elevation data.
     * <p/>
     *
     * @param params the parameters to evaluate. May be null, in which case this method returns false.
     *
     * @return true if the parameters indicate elevations, otherwise false.
     */
    public static boolean isElevations(AVList params)
    {
        return paramsIndicateElevations(params);
    }

    /**
     * Evaluates an {@link AVList} to determine whether the associated data source is elevation data.
     * <p/>
     *
     * @param params the parameters to evaluate. May be null, in which case this method returns false.
     *
     * @return true if the parameters indicate elevations, otherwise false.
     */
    public static boolean paramsIndicateElevations(AVList params)
    {
        return AVKey.ELEVATION.equals(params.getStringValue(AVKey.PIXEL_FORMAT))
            || AVKey.RASTER_TYPE_ELEVATION.equals(params.getStringValue(AVKey.RASTER_TYPE));
    }

    /**
     * Returns true if the specified input source is non-null and represents a reference to a World Wind .NET LayerSet
     * XML document, and false otherwise. The input source may be one of the following: <ul> <li>{@link String}</li>
     * <li>{@link java.io.File}</li> <li>{@link java.net.URL}</li> <li>{@link java.net.URI}</li> <li>{@link
     * java.io.InputStream}</li> </ul>
     *
     * @param source the input source reference to test as a World Wind .NET LayerSet document.
     *
     * @return true if the input source is a World Wind .NET LayerSet document, and false otherwise.
     *
     * @throws IllegalArgumentException if the input source is null.
     */
    public static boolean isWWDotNetLayerSet(Object source)
    {
        if (source == null)
        {
            String message = Logging.getMessage("nullValue.SourceIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        String path = WWIO.getSourcePath(source);
        if (path != null)
        {
            String suffix = WWIO.getSuffix(path);
            if (suffix != null && !suffix.toLowerCase().endsWith("xml"))
                return false;
        }

        // Open the document in question as an XML event stream. Since we're only interested in testing the document
        // element, we avoiding any unnecessary overhead incurred from parsing the entire document as a DOM.
        XMLEventReader eventReader = null;
        try
        {
            eventReader = WWXML.openEventReader(source);

            // Get the first start element event, if any exists, then determine if it represents a LayerSet
            // configuration document.
            XMLEvent event = WWXML.nextStartElementEvent(eventReader);
            return event != null && DataConfigurationUtils.isWWDotNetLayerSetConfigEvent(event);
        }
        catch (Exception e)
        {
            Logging.logger().fine(Logging.getMessage("generic.ExceptionAttemptingToParseXml", source));
            return false;
        }
        finally
        {
            WWXML.closeEventReader(eventReader, source.toString());
        }
    }

    /**
     * Returns a location in the specified {@link gov.nasa.worldwind.cache.FileStore} which should be used as the
     * default location for importing data. This attempts to use the first FileStore location marked as an "install"
     * location. If no install location exists, this falls back to the FileStore's default write location, the same
     * location where downloaded data is cached.
     * <p/>
     * The returned {@link java.io.File} represents an abstract path, and therefore may not exist. In this case, the
     * caller must create the missing directories composing the abstract path.
     *
     * @param fileStore the FileStore to determine the default location for importing data.
     *
     * @return the default location in the specified FileStore to be used for importing data.
     *
     * @throws IllegalArgumentException if the FileStore is null.
     */
    public static File getDefaultImportLocation(FileStore fileStore)
    {
        if (fileStore == null)
        {
            String message = Logging.getMessage("nullValue.FileStoreIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        for (File location : fileStore.getLocations())
        {
            if (fileStore.isInstallLocation(location.getPath()))
                return location;
        }

        return fileStore.getWriteLocation();
    }
}
