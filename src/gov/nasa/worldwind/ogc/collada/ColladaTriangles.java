/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.ogc.kml.KMLAbstractObject;
import gov.nasa.worldwind.util.xml.XMLEventParser;

import java.util.ArrayList;


public class ColladaTriangles extends ColladaAbstractObject
{
    public ColladaTriangles(String ns)
    {
        super(ns);
    }

    protected ArrayList<ColladaInput> inputs = new ArrayList<ColladaInput>();

    public ArrayList<ColladaInput> getInputs()
    {
        return inputs;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("input"))
        {
            inputs.add((ColladaInput) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
