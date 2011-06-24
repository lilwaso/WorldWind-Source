/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import java.util.ArrayList;

public class ColladaLibraryImages extends ColladaAbstractObject
{
    public ColladaLibraryImages(String ns)
    {
        super(ns);
    }

    protected ArrayList<ColladaImage> images = new ArrayList<ColladaImage>();

    public ArrayList<ColladaImage> getNewParams()
    {
        return images;
    }

    @Override
    public void setField(String keyName, Object value)
    {
        if (keyName.equals("image"))
        {
            images.add((ColladaImage) value);
        }
        else
        {
            super.setField(keyName, value);
        }
    }

    public ColladaImage getImageByName(String name)
    {
        for (ColladaImage image : images)
        {
            if (image.getField("id").equals(name))
            {
                return image;
            }
        }

        return null;
    }
}
