/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.util.Logging;
import java.util.Date;
import java.util.logging.Logger;

public class ScheduledOrbitViewInterpolator
  implements OrbitViewInterpolator
{
  private long startTime = -1L;
  private final long length;

  public ScheduledOrbitViewInterpolator(long paramLong)
  {
    this(null, paramLong);
  }

  public ScheduledOrbitViewInterpolator(Date paramDate, long paramLong)
  {
    if (paramLong < 0L)
    {
      String str = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Long.valueOf(paramLong) });
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramDate != null)
      this.startTime = paramDate.getTime();
    this.length = paramLong;
  }

  public ScheduledOrbitViewInterpolator(Date paramDate1, Date paramDate2)
  {
    String str;
    if ((paramDate1 == null) || (paramDate2 == null))
    {
      str = Logging.getMessage("nullValue.DateIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramDate1.after(paramDate2))
    {
      str = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { paramDate1 });
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.startTime = paramDate1.getTime();
    this.length = (paramDate2.getTime() - paramDate1.getTime());
  }

  public final double nextInterpolant(OrbitView paramOrbitView)
  {
    if (paramOrbitView == null)
    {
      String str = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    long l1 = System.currentTimeMillis();
    if (this.startTime < 0L)
      this.startTime = l1;
    if (l1 < this.startTime)
      return 0.0D;
    long l2 = l1 - this.startTime;
    double d = l2 / this.length;
    return clampDouble(d, 0.0D, 1.0D);
  }

  public final OrbitViewInterpolator coalesceWith(OrbitView paramOrbitView, OrbitViewInterpolator paramOrbitViewInterpolator)
  {
    String str;
    if (paramOrbitView == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramOrbitViewInterpolator == null)
    {
      str = Logging.getMessage("nullValue.OrbitViewInterpolatorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    return this;
  }

  private static double clampDouble(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    return paramDouble1 > paramDouble3 ? paramDouble3 : paramDouble1 < paramDouble2 ? paramDouble2 : paramDouble1;
  }
}