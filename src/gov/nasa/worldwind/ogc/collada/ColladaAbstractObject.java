/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.ogc.kml.KMLAbstractObject;

abstract public class ColladaAbstractObject extends KMLAbstractObject
{
    /**
     * Construct an instance.
     *
     * @param namespaceURI the qualifying namespace URI. May be null to indicate no namespace qualification.
     */
    protected ColladaAbstractObject(String namespaceURI)
    {
        super(namespaceURI);
    }
}
