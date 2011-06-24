/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.util.xml;

import gov.nasa.worldwind.ogc.kml.KMLAbstractObject;

/**
 * Holds the content of unrecognized elements. There are no field-specific accessors because the field names are
 * unknown, but all fields can be accessed via the inherited {@link gov.nasa.worldwind.util.xml.AbstractXMLEventParser#getField(javax.xml.namespace.QName)}
 * and {@link gov.nasa.worldwind.util.xml.AbstractXMLEventParser#getFields()}.
 *
 * @author tag
 * @version $Id: UnrecognizedXMLEventParser.java 15431 2011-05-13 00:50:15Z tgaskins $
 */
public class UnrecognizedXMLEventParser extends KMLAbstractObject
{
    public UnrecognizedXMLEventParser()
    {
    }

    public UnrecognizedXMLEventParser(String namespaceURI)
    {
        super(namespaceURI);
    }
}
