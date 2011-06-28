/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import java.util.logging.Logger;

public class ScheduledOrbitViewStateIterator extends BasicOrbitViewStateIterator
{
  private final int maxSmoothing;

  protected ScheduledOrbitViewStateIterator(long paramLong, OrbitViewAnimator paramOrbitViewAnimator, boolean paramBoolean)
  {
    this(new ScheduledOrbitViewInterpolator(paramLong), paramOrbitViewAnimator, paramBoolean);
  }

  protected ScheduledOrbitViewStateIterator(ScheduledOrbitViewInterpolator paramScheduledOrbitViewInterpolator, OrbitViewAnimator paramOrbitViewAnimator, boolean paramBoolean)
  {
    super(false, paramScheduledOrbitViewInterpolator, paramOrbitViewAnimator);
    String str;
    if (paramScheduledOrbitViewInterpolator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewInterpolatorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramOrbitViewAnimator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewAnimatorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.maxSmoothing = maxSmoothingFromFlag(paramBoolean);
  }

  public final boolean isSmoothing()
  {
    return this.maxSmoothing != 0;
  }

  public void doNextState(double paramDouble, OrbitView paramOrbitView)
  {
    if (paramOrbitView == null)
    {
      String str = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    double d = interpolantSmoothed(paramDouble, this.maxSmoothing);
    super.doNextState(d, paramOrbitView);
  }

  private static double interpolantSmoothed(double paramDouble, int paramInt)
  {
    double d = paramDouble;
    for (int i = 0; i < paramInt; i++)
      d = d * d * (3.0D - 2.0D * d);
    return d;
  }

  private static int maxSmoothingFromFlag(boolean paramBoolean)
  {
    if (paramBoolean)
      return 1;
    return 0;
  }

  public static ScheduledOrbitViewStateIterator createCenterIterator(Position paramPosition1, Position paramPosition2)
  {
    if ((paramPosition1 == null) || (paramPosition2 == null))
    {
      String str = Logging.getMessage("nullValue.PositionIsNull");
      Logging.logger().fine(str);
      throw new IllegalArgumentException(str);
    }
    boolean bool = true;
    return createCenterIterator(paramPosition1, paramPosition2, 4000L, bool);
  }

  public static ScheduledOrbitViewStateIterator createCenterIterator(Position paramPosition1, Position paramPosition2, long paramLong, boolean paramBoolean)
  {
    if ((paramPosition1 == null) || (paramPosition2 == null))
    {
            String localObject = Logging.getMessage("nullValue.PositionIsNull");
      Logging.logger().fine((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange");
      Logging.logger().fine((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createCenterPositionAccessor();
    BasicOrbitViewAnimator.PositionAnimator localPositionAnimator = new BasicOrbitViewAnimator.PositionAnimator(paramPosition1, paramPosition2, (OrbitViewPropertyAccessor.PositionAccessor)localObject);
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localPositionAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createHeadingIterator(Angle paramAngle1, Angle paramAngle2)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null))
    {
      String str = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    long l = getScaledLengthMillis(paramAngle1, paramAngle2, Angle.POS180, 500L, 3000L);
    boolean bool = true;
    return createHeadingIterator(paramAngle1, paramAngle2, l, bool);
  }

  public static ScheduledOrbitViewStateIterator createHeadingIterator(Angle paramAngle1, Angle paramAngle2, long paramLong, boolean paramBoolean)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null))
    {
            String localObject = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createHeadingAccessor();
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator = new BasicOrbitViewAnimator.AngleAnimator(paramAngle1, paramAngle2, (OrbitViewPropertyAccessor.AngleAccessor)localObject);
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localAngleAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createPitchIterator(Angle paramAngle1, Angle paramAngle2)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null))
    {
      String str = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    long l = getScaledLengthMillis(paramAngle1, paramAngle2, Angle.POS90, 500L, 1500L);
    boolean bool = true;
    return createPitchIterator(paramAngle1, paramAngle2, l, bool);
  }

  public static ScheduledOrbitViewStateIterator createPitchIterator(Angle paramAngle1, Angle paramAngle2, long paramLong, boolean paramBoolean)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null))
    {
            String localObject = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createPitchAccessor();
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator = new BasicOrbitViewAnimator.AngleAnimator(paramAngle1, paramAngle2, (OrbitViewPropertyAccessor.AngleAccessor)localObject);
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localAngleAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createZoomIterator(double paramDouble1, double paramDouble2)
  {
    boolean bool = true;
    return createZoomIterator(paramDouble1, paramDouble2, 4000L, bool);
  }

  public static ScheduledOrbitViewStateIterator createZoomIterator(double paramDouble1, double paramDouble2, long paramLong, boolean paramBoolean)
  {
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createZoomAccessor();
    BasicOrbitViewAnimator.DoubleAnimator localDoubleAnimator = new BasicOrbitViewAnimator.DoubleAnimator(paramDouble1, paramDouble2, (OrbitViewPropertyAccessor.DoubleAccessor)localObject);
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localDoubleAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createCenterHeadingPitchIterator(Position paramPosition1, Position paramPosition2, Angle paramAngle1, Angle paramAngle2, Angle paramAngle3, Angle paramAngle4, long paramLong, boolean paramBoolean)
  {
    if ((paramPosition1 == null) || (paramPosition2 == null))
    {
            String localObject = Logging.getMessage("nullValue.PositionIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if ((paramAngle1 == null) || (paramAngle2 == null) || (paramAngle3 == null) || (paramAngle4 == null))
    {
            String localObject = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createCenterPositionAccessor();
    OrbitViewPropertyAccessor.AngleAccessor localAngleAccessor1 = OrbitViewPropertyAccessor.createHeadingAccessor();
    OrbitViewPropertyAccessor.AngleAccessor localAngleAccessor2 = OrbitViewPropertyAccessor.createPitchAccessor();
    BasicOrbitViewAnimator.PositionAnimator localPositionAnimator = new BasicOrbitViewAnimator.PositionAnimator(paramPosition1, paramPosition2, (OrbitViewPropertyAccessor.PositionAccessor)localObject);
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator1 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle1, paramAngle2, localAngleAccessor1);
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator2 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle3, paramAngle4, localAngleAccessor2);
    BasicOrbitViewAnimator.CompoundAnimator localCompoundAnimator = new BasicOrbitViewAnimator.CompoundAnimator(new OrbitViewAnimator[] { localPositionAnimator, localAngleAnimator1, localAngleAnimator2 });
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localCompoundAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createCenterZoomIterator(Position paramPosition1, Position paramPosition2, double paramDouble1, double paramDouble2, long paramLong, boolean paramBoolean)
  {
    if ((paramPosition1 == null) || (paramPosition2 == null))
    {
            String localObject = Logging.getMessage("nullValue.PositionIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createCenterPositionAccessor();
    OrbitViewPropertyAccessor.DoubleAccessor localDoubleAccessor = OrbitViewPropertyAccessor.createZoomAccessor();
    BasicOrbitViewAnimator.PositionAnimator localPositionAnimator = new BasicOrbitViewAnimator.PositionAnimator(paramPosition1, paramPosition2, (OrbitViewPropertyAccessor.PositionAccessor)localObject);
    BasicOrbitViewAnimator.DoubleAnimator localDoubleAnimator = new BasicOrbitViewAnimator.DoubleAnimator(paramDouble1, paramDouble2, localDoubleAccessor);
    BasicOrbitViewAnimator.CompoundAnimator localCompoundAnimator = new BasicOrbitViewAnimator.CompoundAnimator(new OrbitViewAnimator[] { localPositionAnimator, localDoubleAnimator });
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localCompoundAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createCenterHeadingPitchZoomIterator(Position paramPosition1, Position paramPosition2, Angle paramAngle1, Angle paramAngle2, Angle paramAngle3, Angle paramAngle4, double paramDouble1, double paramDouble2, long paramLong, boolean paramBoolean)
  {
    if ((paramPosition1 == null) || (paramPosition2 == null))
    {
            String localObject = Logging.getMessage("nullValue.PositionIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if ((paramAngle1 == null) || (paramAngle2 == null) || (paramAngle3 == null) || (paramAngle4 == null))
    {
            String localObject = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createCenterPositionAccessor();
    OrbitViewPropertyAccessor.AngleAccessor localAngleAccessor1 = OrbitViewPropertyAccessor.createHeadingAccessor();
    OrbitViewPropertyAccessor.AngleAccessor localAngleAccessor2 = OrbitViewPropertyAccessor.createPitchAccessor();
    OrbitViewPropertyAccessor.DoubleAccessor localDoubleAccessor = OrbitViewPropertyAccessor.createZoomAccessor();
    BasicOrbitViewAnimator.PositionAnimator localPositionAnimator = new BasicOrbitViewAnimator.PositionAnimator(paramPosition1, paramPosition2, (OrbitViewPropertyAccessor.PositionAccessor)localObject);
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator1 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle1, paramAngle2, localAngleAccessor1);
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator2 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle3, paramAngle4, localAngleAccessor2);
    BasicOrbitViewAnimator.DoubleAnimator localDoubleAnimator = new BasicOrbitViewAnimator.DoubleAnimator(paramDouble1, paramDouble2, localDoubleAccessor);
    BasicOrbitViewAnimator.CompoundAnimator localCompoundAnimator = new BasicOrbitViewAnimator.CompoundAnimator(new OrbitViewAnimator[] { localPositionAnimator, localAngleAnimator1, localAngleAnimator2, localDoubleAnimator });
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localCompoundAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createHeadingPitchIterator(Angle paramAngle1, Angle paramAngle2, Angle paramAngle3, Angle paramAngle4)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null) || (paramAngle3 == null) || (paramAngle4 == null))
    {
      String str = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    long l1 = getScaledLengthMillis(paramAngle1, paramAngle2, Angle.POS180, 500L, 3000L);
    long l2 = getScaledLengthMillis(paramAngle3, paramAngle4, Angle.POS90, 500L, 1500L);
    long l3 = l1 + l2;
    boolean bool = true;
    return createHeadingPitchIterator(paramAngle1, paramAngle2, paramAngle3, paramAngle4, l3, bool);
  }

  public static ScheduledOrbitViewStateIterator createHeadingPitchIterator(Angle paramAngle1, Angle paramAngle2, Angle paramAngle3, Angle paramAngle4, long paramLong, boolean paramBoolean)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null) || (paramAngle3 == null) || (paramAngle4 == null))
    {
            String localObject = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createHeadingAccessor();
    OrbitViewPropertyAccessor.AngleAccessor localAngleAccessor = OrbitViewPropertyAccessor.createPitchAccessor();
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator1 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle1, paramAngle2, (OrbitViewPropertyAccessor.AngleAccessor)localObject);
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator2 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle3, paramAngle4, localAngleAccessor);
    BasicOrbitViewAnimator.CompoundAnimator localCompoundAnimator = new BasicOrbitViewAnimator.CompoundAnimator(new OrbitViewAnimator[] { localAngleAnimator1, localAngleAnimator2 });
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localCompoundAnimator, paramBoolean);
  }

  public static ScheduledOrbitViewStateIterator createHeadingPitchZoomIterator(Angle paramAngle1, Angle paramAngle2, Angle paramAngle3, Angle paramAngle4, double paramDouble1, double paramDouble2, long paramLong, boolean paramBoolean)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null) || (paramAngle3 == null) || (paramAngle4 == null))
    {
            String localObject = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramLong < 0L)
    {
            String localObject = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = OrbitViewPropertyAccessor.createHeadingAccessor();
    OrbitViewPropertyAccessor.AngleAccessor localAngleAccessor = OrbitViewPropertyAccessor.createPitchAccessor();
    OrbitViewPropertyAccessor.DoubleAccessor localDoubleAccessor = OrbitViewPropertyAccessor.createZoomAccessor();
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator1 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle1, paramAngle2, (OrbitViewPropertyAccessor.AngleAccessor)localObject);
    BasicOrbitViewAnimator.AngleAnimator localAngleAnimator2 = new BasicOrbitViewAnimator.AngleAnimator(paramAngle3, paramAngle4, localAngleAccessor);
    BasicOrbitViewAnimator.DoubleAnimator localDoubleAnimator = new BasicOrbitViewAnimator.DoubleAnimator(paramDouble1, paramDouble2, localDoubleAccessor);
    BasicOrbitViewAnimator.CompoundAnimator localCompoundAnimator = new BasicOrbitViewAnimator.CompoundAnimator(new OrbitViewAnimator[] { localAngleAnimator1, localAngleAnimator2, localDoubleAnimator });
    return (ScheduledOrbitViewStateIterator)new ScheduledOrbitViewStateIterator(paramLong, localCompoundAnimator, paramBoolean);
  }

  private static long getScaledLengthMillis(Angle paramAngle1, Angle paramAngle2, Angle paramAngle3, long paramLong1, long paramLong2)
  {
    Angle localAngle = paramAngle1.angularDistanceTo(paramAngle2);
    double d = angularRatio(localAngle, paramAngle3);
    return (long) mixDouble(d, paramLong1, paramLong2);
  }

  private static double angularRatio(Angle paramAngle1, Angle paramAngle2)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null))
    {
      String str = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    double d = paramAngle1.divide(paramAngle2);
    return clampDouble(d, 0.0D, 1.0D);
  }

  private static double clampDouble(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    return paramDouble1 > paramDouble3 ? paramDouble3 : paramDouble1 < paramDouble2 ? paramDouble2 : paramDouble1;
  }

  private static double mixDouble(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    if (paramDouble1 < 0.0D)
      return paramDouble2;
    if (paramDouble1 > 1.0D)
      return paramDouble3;
    return paramDouble2 * (1.0D - paramDouble1) + paramDouble3 * paramDouble1;
  }
}