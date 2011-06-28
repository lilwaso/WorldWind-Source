/* Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.ogc.ows;

import gov.nasa.worldwind.util.xml.AbstractXMLEventParser;

/**
 * Parses an OGC Web Service Common (OWS) AbstractMetaData element and provides access to its contents. See
 * http://schemas.opengis.net/ows/2.0/owsCommon.xsd.
 *
 * @author dcollins
 * @version $Id: OWSAbstractMetaData.java 14946 2011-03-11 23:25:18Z dcollins $
 */
public class OWSAbstractMetaData extends AbstractXMLEventParser
{
    public OWSAbstractMetaData(String namespaceURI)
    {
        super(namespaceURI);
    }
}
