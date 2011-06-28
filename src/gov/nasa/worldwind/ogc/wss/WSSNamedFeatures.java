/* Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.ogc.wss;

import gov.nasa.worldwind.util.xml.StringSetXMLEventParser;

import javax.xml.namespace.QName;

/**
 * Parses the World Wind Web Shape Service (WSS) NamedFeatures and Feature elements and provides access to their as a
 * set of feature names.
 *
 * @author dcollins
 * @version $Id: WSSNamedFeatures.java 14954 2011-03-12 23:29:25Z dcollins $
 */
public class WSSNamedFeatures extends StringSetXMLEventParser
{
    public WSSNamedFeatures(String namespaceURI)
    {
        super(namespaceURI, new QName(namespaceURI, "Feature"));
    }
}
