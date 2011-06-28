/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.xml.XMLEventParser;

import javax.xml.namespace.QName;

public class ColladaSource  extends ColladaAbstractObject
{
    public ColladaSource(String ns)
    {
        super(ns);
    }

    @Override
    public void setField(QName keyName, Object value)
    {
         super.setField(keyName, value);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
