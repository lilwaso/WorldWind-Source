/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.xml.XMLEventParser;
import java.util.ArrayList;

public class ColladaVisualScene extends ColladaAbstractObject
{
    protected ArrayList<ColladaNode> nodes = new ArrayList<ColladaNode>();

    public ColladaVisualScene(String ns)
    {
        super(ns);
    }

    public ArrayList<ColladaNode> getNodes()
    {
        return nodes;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("node"))
        {
            nodes.add((ColladaNode)value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
