/* Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.StringSetXMLEventParser;

import javax.xml.namespace.QName;

/**
 * Parses the OGC Web Service Common (OWS) Languages and Language elements and provides access to their contents as a
 * set of language strings. See http://schemas.opengis.net/ows/2.0/owsGetCapabilities.xsd.
 *
 * @author dcollins
 * @version $Id: OWSLanguages.java 14954 2011-03-12 23:29:25Z dcollins $
 */
public class OWSLanguages extends StringSetXMLEventParser
{
    public OWSLanguages(String namespaceURI)
    {
        super(namespaceURI, new QName(namespaceURI, "Language"));
    }
}
