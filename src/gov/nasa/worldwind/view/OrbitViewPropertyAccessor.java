/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

public class OrbitViewPropertyAccessor
{
  public static PositionAccessor createCenterPositionAccessor()
  {
    return new CenterPositionAccessor();
  }

  public static AngleAccessor createHeadingAccessor()
  {
    return new HeadingAccessor();
  }

  public static AngleAccessor createPitchAccessor()
  {
    return new PitchAccessor();
  }

  public static DoubleAccessor createZoomAccessor()
  {
    return new ZoomAccessor();
  }

  private static class ZoomAccessor
    implements OrbitViewPropertyAccessor.DoubleAccessor
  {
    public final Double getDouble(OrbitView paramOrbitView)
    {
      if (paramOrbitView == null)
        return null;
      return Double.valueOf(paramOrbitView.getZoom());
    }

    public final boolean setDouble(OrbitView paramOrbitView, Double paramDouble)
    {
      if ((paramOrbitView == null) || (paramDouble == null))
        return false;
      try
      {
        paramOrbitView.setZoom(paramDouble.doubleValue());
        return true;
      }
      catch (Exception localException)
      {
      }
      return false;
    }
  }

  private static class PitchAccessor
    implements OrbitViewPropertyAccessor.AngleAccessor
  {
    public final Angle getAngle(OrbitView paramOrbitView)
    {
      if (paramOrbitView == null)
        return null;
      return paramOrbitView.getPitch();
    }

    public final boolean setAngle(OrbitView paramOrbitView, Angle paramAngle)
    {
      if ((paramOrbitView == null) || (paramAngle == null))
        return false;
      try
      {
        paramOrbitView.setPitch(paramAngle);
        return true;
      }
      catch (Exception localException)
      {
      }
      return false;
    }
  }

  private static class HeadingAccessor
    implements OrbitViewPropertyAccessor.AngleAccessor
  {
    public final Angle getAngle(OrbitView paramOrbitView)
    {
      if (paramOrbitView == null)
        return null;
      return paramOrbitView.getHeading();
    }

    public final boolean setAngle(OrbitView paramOrbitView, Angle paramAngle)
    {
      if ((paramOrbitView == null) || (paramAngle == null))
        return false;
      try
      {
        paramOrbitView.setHeading(paramAngle);
        return true;
      }
      catch (Exception localException)
      {
      }
      return false;
    }
  }

  private static class CenterPositionAccessor
    implements OrbitViewPropertyAccessor.PositionAccessor
  {
    public Position getPosition(OrbitView paramOrbitView)
    {
      if (paramOrbitView == null)
        return null;
      return paramOrbitView.getCenterPosition();
    }

    public boolean setPosition(OrbitView paramOrbitView, Position paramPosition)
    {
      if ((paramOrbitView == null) || (paramPosition == null))
        return false;
      try
      {
        paramOrbitView.setCenterPosition(paramPosition);
        return true;
      }
      catch (Exception localException)
      {
      }
      return false;
    }
  }

  public static abstract interface PositionAccessor
  {
    public abstract Position getPosition(OrbitView paramOrbitView);

    public abstract boolean setPosition(OrbitView paramOrbitView, Position paramPosition);
  }

  public static abstract interface DoubleAccessor
  {
    public abstract Double getDouble(OrbitView paramOrbitView);

    public abstract boolean setDouble(OrbitView paramOrbitView, Double paramDouble);
  }

  public static abstract interface AngleAccessor
  {
    public abstract Angle getAngle(OrbitView paramOrbitView);

    public abstract boolean setAngle(OrbitView paramOrbitView, Angle paramAngle);
  }
}