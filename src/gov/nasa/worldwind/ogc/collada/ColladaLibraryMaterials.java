/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import java.util.ArrayList;

public class ColladaLibraryMaterials extends ColladaAbstractObject
{
    public ColladaLibraryMaterials(String ns)
    {
        super(ns);
    }

    protected ArrayList<ColladaMaterial> materials = new ArrayList<ColladaMaterial>();

    public ArrayList<ColladaMaterial> getMaterials()
    {
        return materials;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("material"))
        {
            materials.add((ColladaMaterial) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }

    public ColladaMaterial getMaterialByName(String name)
    {
        for (ColladaMaterial material : materials)
        {

            if (material.getField("id").equals(name))
            {
                return material;
            }
        }

        return null;
    }
}
