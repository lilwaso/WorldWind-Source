/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.util.RestorableSupport;
import gov.nasa.worldwind.util.RestorableSupport.StateObject;

public abstract interface OrbitViewLimits
{
  public abstract Sector getCenterLocationLimits();

  public abstract void setCenterLocationLimits(Sector paramSector);

  public abstract double[] getCenterElevationLimits();

  public abstract void setCenterElevationLimits(double paramDouble1, double paramDouble2);

  public abstract Angle[] getHeadingLimits();

  public abstract void setHeadingLimits(Angle paramAngle1, Angle paramAngle2);

  public abstract Angle[] getPitchLimits();

  public abstract void setPitchLimits(Angle paramAngle1, Angle paramAngle2);

  public abstract double[] getZoomLimits();

  public abstract void setZoomLimits(double paramDouble1, double paramDouble2);

  public abstract void getRestorableState(RestorableSupport paramRestorableSupport, RestorableSupport.StateObject paramStateObject);

  public abstract void restoreState(RestorableSupport paramRestorableSupport, RestorableSupport.StateObject paramStateObject);
}