/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.xml.XMLEventParserContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Represents the KML <i>Point</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLPoint.java 13606 2010-08-06 21:32:22Z tgaskins $
 */
public class KMLPoint extends KMLAbstractGeometry
{
    protected Position coordinates;

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLPoint(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (event.asStartElement().getName().getLocalPart().equals("coordinates"))
            this.setCoordinates((Position.PositionList) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    public boolean isExtrude()
    {
        return this.getExtrude() == Boolean.TRUE;
    }

    public Boolean getExtrude()
    {
        return (Boolean) this.getField("extrude");
    }

    public String getAltitudeMode()
    {
        return (String) this.getField("altitudeMode");
    }

    public Position getCoordinates()
    {
        return this.coordinates;
    }

    protected void setCoordinates(Position.PositionList coordsList)
    {
        if (coordsList != null && coordsList.list.size() > 0)
            this.coordinates = coordsList.list.get(0);
    }
}
