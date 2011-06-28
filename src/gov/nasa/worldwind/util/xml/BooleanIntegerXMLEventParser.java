/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.util.WWUtil;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * @author tag
 * @version $Id: BooleanIntegerXMLEventParser.java 15431 2011-05-13 00:50:15Z tgaskins $
 */
public class BooleanIntegerXMLEventParser extends AbstractXMLEventParser
{
    public BooleanIntegerXMLEventParser()
    {
    }

    public BooleanIntegerXMLEventParser(String namespaceUri)
    {
        super(namespaceUri);
    }

    public Object parse(XMLEventParserContext ctx, XMLEvent booleanEvent, Object... args) throws XMLStreamException
    {
        String s = this.parseCharacterContent(ctx, booleanEvent);
        if (s == null)
            return false;

        s = s.trim();

        if (s.length() > 1)
            return s.equalsIgnoreCase("true");

        return WWUtil.convertNumericStringToBoolean(s);
    }
}
