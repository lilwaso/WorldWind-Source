/*
Copyright (C) 2001, 2010 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.util.xml.XMLEventParser;

import java.util.ArrayList;

public class ColladaLibraryEffects  extends ColladaAbstractObject
{
    public ColladaLibraryEffects(String ns)
    {
        super(ns);
    }


protected ArrayList<ColladaEffect> effects = new ArrayList<ColladaEffect>();

      public ArrayList<ColladaEffect> getEffects()
      {
          return effects;
      }

      @Override
      public void setField(String keyName, Object value)
      {
          if (keyName.equals("effect"))
          {
               effects.add((ColladaEffect)value);
          }
          else
          {
              super.setField(keyName, value);
          }
      }

    public ColladaEffect getEffectByName(String name)
    {
        for (ColladaEffect effect : effects)
        {
            if (effect.getField("id").equals(name.substring(1)))
            {
                return effect;
            }
        }

        return null;
    }
}
