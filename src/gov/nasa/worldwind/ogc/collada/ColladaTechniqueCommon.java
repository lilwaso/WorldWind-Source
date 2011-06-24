/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import java.util.ArrayList;

public class ColladaTechniqueCommon extends ColladaAbstractObject
{
    public ColladaTechniqueCommon(String ns)
    {
        super(ns);
    }

    protected ArrayList<ColladaInstanceMaterial> materials = new ArrayList<ColladaInstanceMaterial>();

    public ArrayList<ColladaInstanceMaterial> getMaterials()
    {
        return materials;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("instance_material"))
        {
            materials.add((ColladaInstanceMaterial) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }
}
