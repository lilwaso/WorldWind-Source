/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.Logging;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.logging.Logger;

public class BasicOrbitViewAnimator
  implements OrbitViewAnimator
{
  private boolean stopOnInvalidState = false;
  private boolean lastStateValid = true;

  public final void doNextState(double paramDouble, OrbitView paramOrbitView, BasicOrbitViewStateIterator paramBasicOrbitViewStateIterator)
  {
    String str;
    if (paramOrbitView == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramBasicOrbitViewStateIterator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewStateIteratorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    doNextStateImpl(paramDouble, paramOrbitView, paramBasicOrbitViewStateIterator);
    if ((isStopOnInvalidState()) && (!isLastStateValid()))
      paramBasicOrbitViewStateIterator.stop();
  }

  protected void doNextStateImpl(double paramDouble, OrbitView paramOrbitView, BasicOrbitViewStateIterator paramBasicOrbitViewStateIterator)
  {
  }

  public final OrbitViewAnimator coalesceWith(OrbitView paramOrbitView, OrbitViewAnimator paramOrbitViewAnimator)
  {
    if (paramOrbitView == null)
    {
            String localObject = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramOrbitViewAnimator == null)
    {
            String localObject = Logging.getMessage("nullValue.OrbitViewAnimatorIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = coalesceWithImpl(paramOrbitView, paramOrbitViewAnimator);
    if ((isStopOnInvalidState()) || (paramOrbitViewAnimator.isStopOnInvalidState()))
      ((OrbitViewAnimator)localObject).setStopOnInvalidState(true);
    return (OrbitViewAnimator)localObject;
  }

  protected OrbitViewAnimator coalesceWithImpl(OrbitView paramOrbitView, OrbitViewAnimator paramOrbitViewAnimator)
  {
    return this;
  }

  public void setStopOnInvalidState(boolean paramBoolean)
  {
    this.stopOnInvalidState = paramBoolean;
  }

  public boolean isStopOnInvalidState()
  {
    return this.stopOnInvalidState;
  }

  protected void flagLastStateInvalid()
  {
    this.lastStateValid = false;
  }

  protected boolean isLastStateValid()
  {
    return this.lastStateValid;
  }

  public static class CompoundAnimator extends BasicOrbitViewAnimator
  {
    private OrbitViewAnimator[] animators;

    public CompoundAnimator(OrbitViewAnimator[] paramArrayOfOrbitViewAnimator)
    {
      if (paramArrayOfOrbitViewAnimator == null)
      {
        String str = Logging.getMessage("nullValue.ArrayIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      int i = paramArrayOfOrbitViewAnimator.length;
      this.animators = new OrbitViewAnimator[i];
      System.arraycopy(paramArrayOfOrbitViewAnimator, 0, this.animators, 0, i);
    }

    private CompoundAnimator newInstance(OrbitViewAnimator[] paramArrayOfOrbitViewAnimator)
    {
      Object localObject1;
      if (paramArrayOfOrbitViewAnimator == null)
      {
        localObject1 = Logging.getMessage("nullValue.ArrayIsNull");
        Logging.logger().severe((String)localObject1);
        throw new IllegalArgumentException((String)localObject1);
      }
      try
      {
        Class localClass = getClass();
                Class<? extends OrbitViewAnimator[]> localObject2 = paramArrayOfOrbitViewAnimator.getClass();
        Constructor localConstructor = localClass.getConstructor(new Class[] { localObject2 });
        localObject1 = (CompoundAnimator)localConstructor.newInstance(new Object[] { paramArrayOfOrbitViewAnimator });
      }
      catch (Exception localException)
      {
        Object localObject2 = Logging.getMessage("");
        Logging.logger().severe((String)localObject2);
        throw new IllegalArgumentException((String)localObject2);
      }
      return (CompoundAnimator)(CompoundAnimator)localObject1;
    }

    public final Iterable<OrbitViewAnimator> getAnimators()
    {
      return Arrays.asList(this.animators);
    }

    protected final void doNextStateImpl(double paramDouble, OrbitView paramOrbitView, BasicOrbitViewStateIterator paramBasicOrbitViewStateIterator)
    {
      for (OrbitViewAnimator localOrbitViewAnimator : this.animators)
      {
        if (localOrbitViewAnimator == null)
          continue;
        localOrbitViewAnimator.doNextState(paramDouble, paramOrbitView, paramBasicOrbitViewStateIterator);
      }
    }

    protected final OrbitViewAnimator coalesceWithImpl(OrbitView paramOrbitView, OrbitViewAnimator paramOrbitViewAnimator)
    {
      if (!(paramOrbitViewAnimator instanceof CompoundAnimator))
        return this;
      CompoundAnimator localCompoundAnimator1 = (CompoundAnimator)paramOrbitViewAnimator;
      if (this.animators.length != localCompoundAnimator1.animators.length)
        return this;
      int i = this.animators.length;
      OrbitViewAnimator[] arrayOfOrbitViewAnimator = new OrbitViewAnimator[i];
      for (int j = 0; j < i; j++)
        if ((this.animators[j] != null) && (localCompoundAnimator1.animators[j] != null))
        {
          arrayOfOrbitViewAnimator[j] = this.animators[j].coalesceWith(paramOrbitView, localCompoundAnimator1.animators[j]);
        }
        else
        {
          if (this.animators[j] == null)
            continue;
          arrayOfOrbitViewAnimator[j] = this.animators[j];
        }
      CompoundAnimator localCompoundAnimator2 = newInstance(arrayOfOrbitViewAnimator);
      if (localCompoundAnimator2 == null)
        return this;
      return localCompoundAnimator2;
    }

    public void setStopOnInvalidState(boolean paramBoolean)
    {
      super.setStopOnInvalidState(paramBoolean);
      for (OrbitViewAnimator localOrbitViewAnimator : this.animators)
      {
        if (localOrbitViewAnimator == null)
          continue;
        localOrbitViewAnimator.setStopOnInvalidState(paramBoolean);
      }
    }
  }

  public static class PositionAnimator extends BasicOrbitViewAnimator
  {
    private final Position begin;
    private final Position end;
    private final OrbitViewPropertyAccessor.PositionAccessor propertyAccessor;

    public PositionAnimator(Position paramPosition1, Position paramPosition2, OrbitViewPropertyAccessor.PositionAccessor paramPositionAccessor)
    {
      String str;
      if ((paramPosition1 == null) || (paramPosition2 == null))
      {
        str = Logging.getMessage("nullValue.PositionIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      if (paramPositionAccessor == null)
      {
        str = Logging.getMessage("nullValue.OrbitViewPropertyAccessorIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      this.begin = paramPosition1;
      this.end = paramPosition2;
      this.propertyAccessor = paramPositionAccessor;
    }

    public final Position getBegin()
    {
      return this.begin;
    }

    public final Position getEnd()
    {
      return this.end;
    }

    public final OrbitViewPropertyAccessor.PositionAccessor getPropertyAccessor()
    {
      return this.propertyAccessor;
    }

    protected final void doNextStateImpl(double paramDouble, OrbitView paramOrbitView, BasicOrbitViewStateIterator paramBasicOrbitViewStateIterator)
    {
      Position localPosition = nextPosition(paramDouble, paramOrbitView);
      if (localPosition == null)
        return;
      boolean bool = this.propertyAccessor.setPosition(paramOrbitView, localPosition);
      if (!bool)
        flagLastStateInvalid();
    }

    public Position nextPosition(double paramDouble, OrbitView paramOrbitView)
    {
      Angle localAngle1 = LatLon.greatCircleAzimuth(this.begin, this.end);
      Angle localAngle2 = LatLon.greatCircleDistance(this.begin, this.end);
      Angle localAngle3 = Angle.fromDegrees(localAngle2.degrees * paramDouble);
      LatLon localLatLon = LatLon.greatCircleEndPosition(this.begin, localAngle1, localAngle3);
      double d = (1.0D - paramDouble) * this.begin.getElevation() + paramDouble * this.end.getElevation();
      return new Position(localLatLon, d);
    }
  }

  public static class DoubleAnimator extends BasicOrbitViewAnimator
  {
    private final double begin;
    private final double end;
    private final OrbitViewPropertyAccessor.DoubleAccessor propertyAccessor;

    public DoubleAnimator(double paramDouble1, double paramDouble2, OrbitViewPropertyAccessor.DoubleAccessor paramDoubleAccessor)
    {
      if (paramDoubleAccessor == null)
      {
        String str = Logging.getMessage("nullValue.OrbitViewPropertyAccessorIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      this.begin = paramDouble1;
      this.end = paramDouble2;
      this.propertyAccessor = paramDoubleAccessor;
    }

    public final Double getBegin()
    {
      return Double.valueOf(this.begin);
    }

    public final Double getEnd()
    {
      return Double.valueOf(this.end);
    }

    public final OrbitViewPropertyAccessor.DoubleAccessor getPropertyAccessor()
    {
      return this.propertyAccessor;
    }

    protected final void doNextStateImpl(double paramDouble, OrbitView paramOrbitView, BasicOrbitViewStateIterator paramBasicOrbitViewStateIterator)
    {
      Double localDouble = nextDouble(paramDouble, paramOrbitView);
      if (localDouble == null)
        return;
      boolean bool = this.propertyAccessor.setDouble(paramOrbitView, localDouble);
      if (!bool)
        flagLastStateInvalid();
    }

    public Double nextDouble(double paramDouble, OrbitView paramOrbitView)
    {
      return Double.valueOf(mix(paramDouble, this.begin, this.end));
    }

    public static double mix(double paramDouble1, double paramDouble2, double paramDouble3)
    {
      if (paramDouble1 < 0.0D)
        return paramDouble2;
      if (paramDouble1 > 1.0D)
        return paramDouble3;
      return paramDouble2 * (1.0D - paramDouble1) + paramDouble3 * paramDouble1;
    }
  }

  public static class AngleAnimator extends BasicOrbitViewAnimator
  {
    private final Angle begin;
    private final Angle end;
    private final OrbitViewPropertyAccessor.AngleAccessor propertyAccessor;

    public AngleAnimator(Angle paramAngle1, Angle paramAngle2, OrbitViewPropertyAccessor.AngleAccessor paramAngleAccessor)
    {
      String str;
      if ((paramAngle1 == null) || (paramAngle2 == null))
      {
        str = Logging.getMessage("nullValue.AngleIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      if (paramAngleAccessor == null)
      {
        str = Logging.getMessage("nullValue.OrbitViewPropertyAccessorIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      this.begin = paramAngle1;
      this.end = paramAngle2;
      this.propertyAccessor = paramAngleAccessor;
    }

    public final Angle getBegin()
    {
      return this.begin;
    }

    public final Angle getEnd()
    {
      return this.end;
    }

    public final OrbitViewPropertyAccessor.AngleAccessor getPropertyAccessor()
    {
      return this.propertyAccessor;
    }

    protected final void doNextStateImpl(double paramDouble, OrbitView paramOrbitView, BasicOrbitViewStateIterator paramBasicOrbitViewStateIterator)
    {
      Angle localAngle = nextAngle(paramDouble, paramOrbitView);
      if (localAngle == null)
        return;
      boolean bool = this.propertyAccessor.setAngle(paramOrbitView, localAngle);
      if (!bool)
        flagLastStateInvalid();
    }

    public Angle nextAngle(double paramDouble, OrbitView paramOrbitView)
    {
      return Angle.mix(paramDouble, this.begin, this.end);
    }
  }
}