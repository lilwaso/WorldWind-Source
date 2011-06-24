/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.xml.XMLEventParser;

public class ColladaInput  extends ColladaAbstractObject
{
    public ColladaInput(String ns)
    {
        super(ns);
    }

    public int getOffset()
    {
        return Integer.parseInt((String) this.getField("offset"));
    }
}
