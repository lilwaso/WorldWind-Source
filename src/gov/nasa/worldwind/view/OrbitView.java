/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

public abstract interface OrbitView extends View
{
  public static final String CENTER_STOPPED = "gov.nasa.worldwind.view.OrbitView.CenterStopped";

  public abstract Position getCenterPosition();

  public abstract void setCenterPosition(Position paramPosition);

  public abstract Angle getHeading();

  public abstract void setHeading(Angle paramAngle);

  public abstract Angle getPitch();

  public abstract void setPitch(Angle paramAngle);

  public abstract double getZoom();

  public abstract void setZoom(double paramDouble);

  public abstract OrbitViewLimits getOrbitViewLimits();

  public abstract void setOrbitViewLimits(OrbitViewLimits paramOrbitViewLimits);

  public abstract OrbitViewModel getOrbitViewModel();

  public abstract boolean canFocusOnViewportCenter();

  public abstract void focusOnViewportCenter();

  public abstract void stopMovementOnCenter();
  
}