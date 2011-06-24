/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.xml.XMLEventParser;

import java.util.ArrayList;

public class ColladaLibraryGeometries extends ColladaAbstractObject
{
    protected ArrayList<ColladaGeometry> geometries = new ArrayList<ColladaGeometry>();

    public ColladaLibraryGeometries(String ns)
    {
        super(ns);
    }

    public ColladaGeometry getGeometryByID(String urlForGeom)
    {
        for (ColladaGeometry geom : geometries)
        {
            Object id = geom.getField("id");
            if (id.equals(urlForGeom.substring(1)))
            {
                return geom;
            }
        }

        return null;
    }


       public ArrayList<ColladaGeometry> getGeometries()
       {
           return geometries;
       }

       @Override
       public void setField(String keyName, Object value)
       {
           if (keyName.equals("geometry"))
           {
                geometries.add((ColladaGeometry)value);
           }
           else
           {
               super.setField(keyName, value);
           }
       }

}
