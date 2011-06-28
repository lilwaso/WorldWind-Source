package gov.nasa.worldwind.examples.sunlight;

import gov.nasa.worldwind.geom.LatLon;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BasicSunPositionProvider
  implements SunPositionProvider
{
  private LatLon position;
  private Calendar calendar = new GregorianCalendar();

  public BasicSunPositionProvider()
  {
    updatePosition();
    Thread localThread = new Thread(new Runnable()
    {
      public void run()
      {
        while (true)
        {
          try
          {
            Thread.sleep(60000L);
          }
          catch (InterruptedException localInterruptedException)
          {
          }
          BasicSunPositionProvider.this.calendar.setTimeInMillis(System.currentTimeMillis());
          BasicSunPositionProvider.this.updatePosition();
        }
      }
    });
    localThread.setDaemon(true);
    localThread.start();
  }

  private synchronized void updatePosition()
  {
    this.position = SunCalculator.subsolarPoint(this.calendar);
  }

  public synchronized LatLon getPosition()
  {
    return this.position;
  }
}