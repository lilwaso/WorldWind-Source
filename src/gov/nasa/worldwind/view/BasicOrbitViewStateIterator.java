/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.ViewStateIterator;
import gov.nasa.worldwind.util.Logging;
import java.util.logging.Logger;

public class BasicOrbitViewStateIterator
  implements ViewStateIterator
{
  private final boolean doCoalesce;
  private final OrbitViewInterpolator interpolator;
  private final OrbitViewAnimator animator;
  private boolean hasNext = true;

  public BasicOrbitViewStateIterator(boolean paramBoolean, OrbitViewInterpolator paramOrbitViewInterpolator, OrbitViewAnimator paramOrbitViewAnimator)
  {
    String str;
    if (paramOrbitViewInterpolator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewStateIterator.InterpolatorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramOrbitViewAnimator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewStateIterator.AnimatorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.doCoalesce = paramBoolean;
    this.interpolator = paramOrbitViewInterpolator;
    this.animator = paramOrbitViewAnimator;
  }

  public final boolean isCoalesce()
  {
    return this.doCoalesce;
  }

  public final void nextState(View paramView)
  {
    String str;
    if (paramView == null)
    {
      str = Logging.getMessage("nullValue.ViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (!(paramView instanceof OrbitView))
    {
      str = Logging.getMessage("view.OrbitView.ViewNotAnOrbitView");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    double d1 = this.interpolator.nextInterpolant((OrbitView)paramView);
    double d2 = clampDouble(d1, 0.0D, 1.0D);
    if (d2 >= 1.0D)
      stop();
    doNextState(d2, (OrbitView)paramView);
    paramView.firePropertyChange("gov.nasa.worldwind.avkey.ViewObject", null, paramView);
  }

  private static double clampDouble(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    return paramDouble1 > paramDouble3 ? paramDouble3 : paramDouble1 < paramDouble2 ? paramDouble2 : paramDouble1;
  }

  protected void doNextState(double paramDouble, OrbitView paramOrbitView)
  {
    if (paramOrbitView == null)
    {
      String str = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.animator.doNextState(paramDouble, paramOrbitView, this);
  }

  public final boolean hasNextState(View paramView)
  {
    String str;
    if (paramView == null)
    {
      str = Logging.getMessage("nullValue.ViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (!(paramView instanceof OrbitView))
    {
      str = Logging.getMessage("view.OrbitView.ViewNotAnOrbitView");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    return this.hasNext;
  }

  public final void stop()
  {
    this.hasNext = false;
  }

  public final ViewStateIterator coalesceWith(View paramView, ViewStateIterator paramViewStateIterator)
  {
    String str;
    if (paramView == null)
    {
      str = Logging.getMessage("nullValue.ViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (!(paramView instanceof OrbitView))
    {
      str = Logging.getMessage("view.OrbitView.ViewNotAnOrbitView");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if ((paramViewStateIterator == null) || (!(paramViewStateIterator instanceof BasicOrbitViewStateIterator)))
      return this;
    if (!this.doCoalesce)
      return this;
    boolean bool = this.doCoalesce;
    OrbitViewInterpolator localOrbitViewInterpolator = this.interpolator.coalesceWith((OrbitView)paramView, ((BasicOrbitViewStateIterator)paramViewStateIterator).interpolator);
    OrbitViewAnimator localOrbitViewAnimator = this.animator.coalesceWith((OrbitView)paramView, ((BasicOrbitViewStateIterator)paramViewStateIterator).animator);
    return doCoalesce((OrbitView)paramView, bool, localOrbitViewInterpolator, localOrbitViewAnimator);
  }

  protected ViewStateIterator doCoalesce(OrbitView paramOrbitView, boolean paramBoolean, OrbitViewInterpolator paramOrbitViewInterpolator, OrbitViewAnimator paramOrbitViewAnimator)
  {
    String str;
    if (paramOrbitView == null)
    {
      str = Logging.getMessage("nullValue.ViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramOrbitViewInterpolator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewStateIterator.InterpolatorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramOrbitViewAnimator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewStateIterator.AnimatorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    return new BasicOrbitViewStateIterator(paramBoolean, paramOrbitViewInterpolator, paramOrbitViewAnimator);
  }
}