/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.kml;

import gov.nasa.worldwind.util.xml.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

/**
 * Represents the KML <i>Change</i> element and provides access to its contents.
 *
 * @author tag
 * @version $Id: KMLChange.java 13513 2010-06-30 07:01:20Z tgaskins $
 */
public class KMLChange extends AbstractXMLEventParser
{
    protected List<KMLAbstractObject> objects = new ArrayList<KMLAbstractObject>();

    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    public KMLChange(String namespaceURI)
    {
        super(namespaceURI);
    }

    @Override
    protected void doAddEventContent(Object o, XMLEventParserContext ctx, XMLEvent event, Object... args)
        throws XMLStreamException
    {
        if (o instanceof KMLAbstractObject)
            this.addObject((KMLAbstractObject) o);
        else
            super.doAddEventContent(o, ctx, event, args);
    }

    protected void addObject(KMLAbstractObject o)
    {
        this.objects.add(o);
    }

    public List<KMLAbstractObject> getObjects()
    {
        return this.objects;
    }
}
