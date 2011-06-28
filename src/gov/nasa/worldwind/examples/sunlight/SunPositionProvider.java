package gov.nasa.worldwind.examples.sunlight;

import gov.nasa.worldwind.geom.LatLon;

public abstract interface SunPositionProvider
{
  public abstract LatLon getPosition();
}