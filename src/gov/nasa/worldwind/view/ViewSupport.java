/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import java.awt.Rectangle;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;

public class ViewSupport
{
  private final GLU glu = new GLU();

  public void loadGLViewState(DrawContext paramDrawContext, Matrix paramMatrix1, Matrix paramMatrix2)
  {
    if (paramDrawContext == null)
    {
            String localObject = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramDrawContext.getGL() == null)
    {
            String localObject = Logging.getMessage("nullValue.DrawingContextGLIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalStateException((String)localObject);
    }
    if (paramMatrix1 == null)
      Logging.logger().fine("nullValue.ModelViewIsNull");
    if (paramMatrix2 == null)
      Logging.logger().fine("nullValue.ProjectionIsNull");
    double[] localObject = new double[16];
    GL localGL = paramDrawContext.getGL();
    int[] arrayOfInt = new int[1];
    localGL.glGetIntegerv(2976, arrayOfInt, 0);
    localGL.glMatrixMode(5888);
    if (paramMatrix1 != null)
    {
      paramMatrix1.toArray(localObject, 0, false);
      localGL.glLoadMatrixd(localObject, 0);
    }
    else
    {
      localGL.glLoadIdentity();
    }
    localGL.glMatrixMode(5889);
    if (paramMatrix2 != null)
    {
      paramMatrix2.toArray(localObject, 0, false);
      localGL.glLoadMatrixd(localObject, 0);
    }
    else
    {
      localGL.glLoadIdentity();
    }
    localGL.glMatrixMode(arrayOfInt[0]);
  }

  public Vec4 project(Vec4 paramVec4, Matrix paramMatrix1, Matrix paramMatrix2, Rectangle paramRectangle)
  {
    if (paramVec4 == null)
    {
            String localObject = Logging.getMessage("nullValue.PointIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if ((paramMatrix1 == null) || (paramMatrix2 == null))
    {
            String localObject = Logging.getMessage("nullValue.MatrixIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramRectangle == null)
    {
            String localObject = Logging.getMessage("nullValue.RectangleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    double[] localObject = new double[16];
    double[] arrayOfDouble1 = new double[16];
    paramMatrix1.toArray(localObject, 0, false);
    paramMatrix2.toArray(arrayOfDouble1, 0, false);
    int[] arrayOfInt = { paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height };
    double[] arrayOfDouble2 = new double[3];
    if (!this.glu.gluProject(paramVec4.x, paramVec4.y, paramVec4.z, localObject, 0, arrayOfDouble1, 0, arrayOfInt, 0, arrayOfDouble2, 0))
      return null;
    return (Vec4)Vec4.fromArray3(arrayOfDouble2, 0);
  }

  public Vec4 unProject(Vec4 paramVec4, Matrix paramMatrix1, Matrix paramMatrix2, Rectangle paramRectangle)
  {
    if (paramVec4 == null)
    {
            String localObject = Logging.getMessage("nullValue.PointIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if ((paramMatrix1 == null) || (paramMatrix2 == null))
    {
            String localObject = Logging.getMessage("nullValue.MatrixIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramRectangle == null)
    {
            String localObject = Logging.getMessage("nullValue.RectangleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    double[] localObject = new double[16];
    double[] arrayOfDouble1 = new double[16];
    paramMatrix1.toArray(localObject, 0, false);
    paramMatrix2.toArray(arrayOfDouble1, 0, false);
    int[] arrayOfInt = { paramRectangle.x, paramRectangle.y, paramRectangle.width, paramRectangle.height };
    double[] arrayOfDouble2 = new double[3];
    if (!this.glu.gluUnProject(paramVec4.x, paramVec4.y, paramVec4.z, localObject, 0, arrayOfDouble1, 0, arrayOfInt, 0, arrayOfDouble2, 0))
      return null;
    return (Vec4)Vec4.fromArray3(arrayOfDouble2, 0);
  }

  public Line computeRayFromScreenPoint(double paramDouble1, double paramDouble2, Matrix paramMatrix1, Matrix paramMatrix2, Rectangle paramRectangle)
  {
    if ((paramMatrix1 == null) || (paramMatrix2 == null))
    {
            String localObject = Logging.getMessage("nullValue.MatrixIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramRectangle == null)
    {
            String localObject = Logging.getMessage("nullValue.RectangleIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = paramMatrix1.getInverse();
    if (localObject == null)
      return null;
    Vec4 localVec41 = Vec4.UNIT_W.transformBy4((Matrix)localObject);
    if (localVec41 == null)
      return null;
    double d = paramRectangle.height - paramDouble2 - 1.0D;
    Vec4 localVec42 = unProject(new Vec4(paramDouble1, d, 0.0D, 0.0D), paramMatrix1, paramMatrix2, paramRectangle);
    Vec4 localVec43 = unProject(new Vec4(paramDouble1, d, 1.0D, 0.0D), paramMatrix1, paramMatrix2, paramRectangle);
    if ((localVec42 == null) || (localVec43 == null))
      return null;
    return (Line)new Line(localVec41, localVec43.subtract3(localVec42).normalize3());
  }

  public double computePixelSizeAtDistance(double paramDouble, Angle paramAngle, Rectangle paramRectangle)
  {
    String str;
    if (paramAngle == null)
    {
      str = Logging.getMessage("nullValue.AngleIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramRectangle == null)
    {
      str = Logging.getMessage("nullValue.RectangleIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    double d1 = paramRectangle.getWidth();
    double d2 = 2.0D * paramAngle.tanHalfAngle() / (d1 <= 0.0D ? 1.0D : d1);
    return Math.abs(paramDouble) * d2;
  }

  public double computeHorizonDistance(Globe paramGlobe, double paramDouble)
  {
    if (paramGlobe == null)
    {
      String str = Logging.getMessage("nullValue.GlobeIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramDouble <= 0.0D)
      return 0.0D;
    double d = paramGlobe.getMaximumRadius();
    return Math.sqrt(paramDouble * (2.0D * d + paramDouble));
  }

  public double computeElevationAboveSurface(DrawContext paramDrawContext, Position paramPosition)
  {
    if (paramDrawContext == null)
    {
            String localObject1 = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
    Object localObject1 = paramDrawContext.getGlobe();
    if (localObject1 == null)
    {
            String localObject2 = Logging.getMessage("nullValue.DrawingContextGlobeIsNull");
      Logging.logger().severe((String)localObject2);
      throw new IllegalArgumentException((String)localObject2);
    }
    if (paramPosition == null)
    {
            String localObject2 = Logging.getMessage("nullValue.Vec4IsNull");
      Logging.logger().severe((String)localObject2);
      throw new IllegalArgumentException((String)localObject2);
    }
    Object localObject2 = null;
    
    Vec4 localVec4 = paramDrawContext.getPointOnGlobe(paramPosition.getLatitude(), paramPosition.getLongitude());
    if (localVec4 != null)
      localObject2 = ((Globe)localObject1).computePositionFromPoint(localVec4);
    if (localObject2 == null)
      localObject2 = new Position(paramPosition, ((Globe)localObject1).getElevation(paramPosition.getLatitude(), paramPosition.getLongitude()) * paramDrawContext.getVerticalExaggeration());
    return paramPosition.getElevation() - ((Position)localObject2).getElevation();
  }
}