/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.ogc.kml.KMLAbstractFeature;
import gov.nasa.worldwind.util.xml.XMLEventParser;

import javax.xml.namespace.QName;
import java.util.*;


public class ColladaMesh  extends ColladaAbstractObject
{
    public ColladaMesh(String ns)
    {
        super(ns);
    }

    @Override
    public void setField(QName keyName, Object value)
    {
        super.setField(keyName, value);    //To change body of overridden methods use File | Settings | File Templates.
    }

    protected ArrayList<ColladaSource> sources = new ArrayList<ColladaSource>();
    protected ArrayList<ColladaTriangles> triangles = new ArrayList<ColladaTriangles>();
    protected ArrayList<ColladaVertices> vertices = new ArrayList<ColladaVertices>();

    public ArrayList<ColladaSource> getSources()
    {
        return sources;
    }

    public ArrayList<ColladaTriangles> getTriangles()
    {
        return triangles;
    }

    public ArrayList<ColladaVertices> getVertices()
    {
        return vertices;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("vertices"))
        {
             vertices.add((ColladaVertices)value);
        }
        else
        if (keyName.equals("source"))
        {
             sources.add((ColladaSource)value);
        }
        else
        if (keyName.equals("triangles"))
        {
             triangles.add((ColladaTriangles)value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }

    @Override
    public Object getField(String keyName)
    {
        if (keyName.equals("source"))
        {
            return sources.get(0);
        }
        else
        if (keyName.equals("triangles"))
        {
            return triangles.get(0);
        }
        else
        {
            return super.getField(keyName);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    @Override
    public void setFields(Map<String, Object> newFields)
    {
        super.setFields(newFields);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
