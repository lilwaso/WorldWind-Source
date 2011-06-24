/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import java.util.ArrayList;

public class ColladaTechnique extends ColladaAbstractObject
{
    public ColladaTechnique(String ns)
    {
        super(ns);
    }

    protected ArrayList<ColladaNewParam> params = new ArrayList<ColladaNewParam>();

    public ArrayList<ColladaNewParam> getNewParams()
    {
        return params;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("newparam"))
        {
            params.add((ColladaNewParam) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
