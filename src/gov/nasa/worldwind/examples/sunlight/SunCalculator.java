package gov.nasa.worldwind.examples.sunlight;

import gov.nasa.worldwind.geom.LatLon;
import java.util.Calendar;

public class SunCalculator
{
  public static LatLon subsolarPoint(Calendar paramCalendar)
  {
    double d2 = paramCalendar.get(11) + (paramCalendar.get(12) + paramCalendar.get(13) / 60.0D) / 60.0D;
    long l1 = (paramCalendar.get(2) - 14) / 12;
    long l2 = 1461L * (paramCalendar.get(1) + 4800 + l1) / 4L + 367L * (paramCalendar.get(2) - 2 - 12L * l1) / 12L - 3L * ((paramCalendar.get(1) + 4900 + l1) / 100L) / 4L + paramCalendar.get(5) - 32075L;
    double d9 = l2 - 0.5D + d2 / 24.0D;
    double d1 = d9 - 2451545.0D;
    double d7 = 2.1429D - 0.0010394594D * d1;
    double d8 = 4.895063D + 0.017202791698D * d1;
    d9 = 6.24006D + 0.0172019699D * d1;
    double d3 = d8 + 0.03341607D * Math.sin(d9) + 0.00034894D * Math.sin(2.0D * d9) - 0.0001134D - 2.03E-005D * Math.sin(d7);
    double d4 = 0.4090928D - 6.214E-009D * d1 + 3.96E-005D * Math.cos(d7);
    d7 = Math.sin(d3);
    d8 = Math.cos(d4) * d7;
    d9 = Math.cos(d3);
    double d5 = Math.atan2(d8, d9);
    if (d5 < 0.0D)
      d5 += 6.283185307179586D;
    double d6 = Math.asin(Math.sin(d4) * d7);
    d7 = 6.6974243242D + 0.0657098283D * d1 + d2;
    d8 = d5 - Math.toRadians(d7 * 15.0D);
    d8 += 3.141592653589793D;
    while (d6 > 1.570796326794897D)
      d6 -= 3.141592653589793D;
    while (d6 <= -1.570796326794897D)
      d6 += 3.141592653589793D;
    while (d8 > 3.141592653589793D)
      d8 -= 6.283185307179586D;
    while (d8 <= -3.141592653589793D)
      d8 += 6.283185307179586D;
    return LatLon.fromRadians(d6, d8);
  }
}