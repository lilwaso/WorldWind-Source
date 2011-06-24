/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.formats.shapefile;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.ogc.kml.impl.KMLExportUtil;
import gov.nasa.worldwind.util.*;

import javax.xml.stream.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Holds the information for a single record of a Polygon shape.
 *
 * @author Patrick Murris
 * @version $Id: ShapefileRecordPolygon.java 14631 2011-02-02 20:09:59Z pabercrombie $
 */
public class ShapefileRecordPolygon extends ShapefileRecordPolyline
{
    /** {@inheritDoc} */
    public ShapefileRecordPolygon(Shapefile shapeFile, ByteBuffer buffer)
    {
        super(shapeFile, buffer);
    }

    /**
     * Export the record to KML as a {@code <Placemark>} element. If the shapefile export mode (defined by {@link
     * AVKey#EXPORT_MODE}) is {@link AVKey#EXTRUDED}, the polygon will be exported as an extruded polygon, with a height
     * defined by this record's "height" attribute.
     *
     * @param xmlWriter XML writer to receive the generated KML.
     *
     * @throws javax.xml.stream.XMLStreamException
     *                             If an exception occurs while writing the KML
     * @throws java.io.IOException If an exception occurs while exporting the data.
     */
    @Override
    public void exportAsKML(XMLStreamWriter xmlWriter) throws IOException, XMLStreamException
    {
        Iterable<? extends LatLon> outerBoundary = null;
        List<Iterable<? extends LatLon>> innerBoundaries = new ArrayList<Iterable<? extends LatLon>>();

        // If the shapefile export mode is "extruded", export the shape as an extruded polygon using the height attribute
        Double height = null;
        if (AVKey.EXTRUDED.equals(this.getShapeFile().getValue(AVKey.EXPORT_MODE)))
            height = ShapefileUtils.extractHeightAttribute(this);

        for (int i = 0; i < this.getNumberOfParts(); i++)
        {
            // Although the shapefile spec says that inner and outer boundaries can be listed in any order, it's
            // assumed here that inner boundaries are at least listed adjacent to their outer boundary, either
            // before or after it. The below code accumulates inner boundaries into the polygon until an
            // outer boundary comes along. If the outer boundary comes before the inner boundaries, the inner
            // boundaries are added to the polygon until another outer boundary comes along, at which point a new
            // polygon is started.

            VecBuffer buffer = this.getCompoundPointBuffer().subBuffer(i);
            if (WWMath.computeWindingOrderOfLocations(buffer.getLocations()).equals(AVKey.CLOCKWISE))
            {
                if (outerBoundary == null)
                {
                    outerBoundary = buffer.getLocations();
                }
                else
                {
                    this.exportPolygonAsKML(xmlWriter, outerBoundary, innerBoundaries, height);

                    outerBoundary = this.getCompoundPointBuffer().getLocations();
                    innerBoundaries.clear();
                }
            }
            else
            {
                innerBoundaries.add(buffer.getLocations());
            }
        }

        if (outerBoundary != null && outerBoundary.iterator().hasNext())
        {
            this.exportPolygonAsKML(xmlWriter, outerBoundary, innerBoundaries, height);
        }
    }

    protected void exportPolygonAsKML(XMLStreamWriter xmlWriter, Iterable<? extends LatLon> outerBoundary,
        List<Iterable<? extends LatLon>> innerBoundaries, Double height) throws IOException, XMLStreamException
    {
        xmlWriter.writeStartElement("Placemark");
        xmlWriter.writeStartElement("name");
        xmlWriter.writeCharacters(Integer.toString(this.getRecordNumber()));
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("Polygon");

        String altitudeMode;
        if (height != null)
        {
            xmlWriter.writeStartElement("extrude");
            xmlWriter.writeCharacters("1");
            xmlWriter.writeEndElement();

            altitudeMode = "absolute";
        }
        else
        {
            altitudeMode = "clampToGround";
            height = 0.0;
        }

        xmlWriter.writeStartElement("altitudeMode");
        xmlWriter.writeCharacters(altitudeMode);
        xmlWriter.writeEndElement();

        xmlWriter.writeStartElement("outerBoundaryIs");
        KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, outerBoundary, height);
        xmlWriter.writeEndElement(); // outerBoundaryIs

        for (Iterable<? extends LatLon> innerBoundary : innerBoundaries)
        {
            xmlWriter.writeStartElement("innerBoundaryIs");
            KMLExportUtil.exportBoundaryAsLinearRing(xmlWriter, innerBoundary, height);
            xmlWriter.writeEndElement(); // innerBoundaryIs
        }

        xmlWriter.writeEndElement(); // Polygon
        xmlWriter.writeEndElement(); // Placemark
        xmlWriter.flush();
    }
}
