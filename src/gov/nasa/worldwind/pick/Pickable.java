package gov.nasa.worldwind.pick;

import gov.nasa.worldwind.render.DrawContext;
import java.awt.Point;

public abstract interface Pickable
{
  public abstract void pick(DrawContext paramDrawContext, Point paramPoint);
}