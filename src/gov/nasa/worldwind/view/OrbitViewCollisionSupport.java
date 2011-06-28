/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.worldwind.view;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import java.awt.Rectangle;
import java.util.logging.Logger;

public class OrbitViewCollisionSupport
{
  private double collisionThreshold;
  private int numIterations;

  public OrbitViewCollisionSupport()
  {
    setNumIterations(1);
  }

  public double getCollisionThreshold()
  {
    return this.collisionThreshold;
  }

  public void setCollisionThreshold(double paramDouble)
  {
    if (paramDouble < 0.0D)
    {
      String str = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Double.valueOf(paramDouble) });
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.collisionThreshold = paramDouble;
  }

  public int getNumIterations()
  {
    return this.numIterations;
  }

  public void setNumIterations(int paramInt)
  {
    if (paramInt < 1)
    {
      String str = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Integer.valueOf(paramInt) });
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.numIterations = paramInt;
  }

  public boolean isColliding(OrbitView paramOrbitView, double paramDouble, DrawContext paramDrawContext)
  {
    if (paramOrbitView == null)
    {
            String localObject1 = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
    if (paramDouble < 0.0D)
    {
            String localObject1 = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Double.valueOf(paramDouble) });
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
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
    Object localObject2 = getModelviewInverse(paramOrbitView.getOrbitViewModel(), (Globe)localObject1, paramOrbitView.getCenterPosition(), paramOrbitView.getHeading(), paramOrbitView.getPitch(), paramOrbitView.getZoom());
    if (localObject2 != null)
    {
      double d = computeViewHeightAboveSurface(paramDrawContext, (Matrix)localObject2, paramOrbitView.getFieldOfView(), paramOrbitView.getViewport(), paramDouble);
      return d < this.collisionThreshold;
    }
    return false;
  }

  public Position computeCenterPositionToResolveCollision(OrbitView paramOrbitView, double paramDouble, DrawContext paramDrawContext)
  {
    if (paramOrbitView == null)
    {
            String localObject1 = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
    if (paramDouble < 0.0D)
    {
            String localObject1 = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Double.valueOf(paramDouble) });
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
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
    Position localObject2 = null;
    for (int i = 0; i < this.numIterations; i++)
    {
      Matrix localMatrix = getModelviewInverse(paramOrbitView.getOrbitViewModel(), (Globe)localObject1, localObject2 != null ? localObject2 : paramOrbitView.getCenterPosition(), paramOrbitView.getHeading(), paramOrbitView.getPitch(), paramOrbitView.getZoom());
      if (localMatrix == null)
        continue;
      double d1 = computeViewHeightAboveSurface(paramDrawContext, localMatrix, paramOrbitView.getFieldOfView(), paramOrbitView.getViewport(), paramDouble);
      double d2 = d1 - this.collisionThreshold;
      if (d2 >= 0.0D)
        continue;
      localObject2 = new Position(localObject2 != null ? localObject2 : paramOrbitView.getCenterPosition(), (localObject2 != null ? ((Position)localObject2).getElevation() : paramOrbitView.getCenterPosition().getElevation()) - d2);
    }
    return (Position)(Position)localObject2;
  }

  public Angle computePitchToResolveCollision(OrbitView paramOrbitView, double paramDouble, DrawContext paramDrawContext)
  {
    if (paramOrbitView == null)
    {
            String localObject1 = Logging.getMessage("nullValue.OrbitViewIsNull");
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
    if (paramDouble < 0.0D)
    {
            String localObject1 = Logging.getMessage("generic.ArgumentOutOfRange", new Object[] { Double.valueOf(paramDouble) });
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
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
    Angle localObject2 = null;
    for (int i = 0; i < this.numIterations; i++)
    {
      Matrix localMatrix = getModelviewInverse(paramOrbitView.getOrbitViewModel(), (Globe)localObject1, paramOrbitView.getCenterPosition(), paramOrbitView.getHeading(), localObject2 != null ? localObject2 : paramOrbitView.getPitch(), paramOrbitView.getZoom());
      if (localMatrix == null)
        continue;
      double d1 = computeViewHeightAboveSurface(paramDrawContext, localMatrix, paramOrbitView.getFieldOfView(), paramOrbitView.getViewport(), paramDouble);
      double d2 = d1 - this.collisionThreshold;
      if (d2 >= 0.0D)
        continue;
      Vec4 localVec41 = getEyePoint(localMatrix);
      Vec4 localVec42 = ((Globe)localObject1).computePointFromPosition(paramOrbitView.getCenterPosition());
      if ((localVec41 == null) || (localVec42 == null))
        continue;
      Position localPosition = ((Globe)localObject1).computePositionFromPoint(localVec41);
      Vec4 localVec43 = ((Globe)localObject1).computePointFromPosition(localPosition.getLatitude(), localPosition.getLongitude(), localPosition.getElevation() - d2);
      Vec4 localVec44 = ((Globe)localObject1).computeSurfaceNormalAtPoint(localVec42);
      Vec4 localVec45 = localVec43.subtract3(localVec42).normalize3();
      double d3 = localVec44.dot3(localVec45);
      if ((d3 < -1.0D) && (d3 > 1.0D))
        continue;
      double d4 = Math.acos(d3);
      localObject2 = Angle.fromRadians(d4);
    }
    return (Angle)(Angle)localObject2;
  }

  private double computeViewHeightAboveSurface(DrawContext paramDrawContext, Matrix paramMatrix, Angle paramAngle, Rectangle paramRectangle, double paramDouble)
  {
    double d1 = (1.0D / 0.0D);
    if ((paramDrawContext != null) && (paramMatrix != null) && (paramAngle != null) && (paramRectangle != null) && (paramDouble >= 0.0D))
    {
      Vec4 localVec41 = getEyePoint(paramMatrix);
      if (localVec41 != null)
      {
        double d2 = computePointHeightAboveSurface(paramDrawContext, localVec41);
        if (d2 < d1)
          d1 = d2;
      }
      Vec4 localVec42 = getPointOnNearPlane(paramMatrix, paramAngle, paramRectangle, paramDouble);
      if (localVec42 != null)
      {
        double d3 = computePointHeightAboveSurface(paramDrawContext, localVec42);
        if (d3 < d1)
          d1 = d3;
      }
    }
    return d1;
  }

  private double computePointHeightAboveSurface(DrawContext paramDrawContext, Vec4 paramVec4)
  {
    double d = (1.0D / 0.0D);
    if ((paramDrawContext != null) && (paramDrawContext.getGlobe() != null) && (paramVec4 != null))
    {
      Globe localGlobe = paramDrawContext.getGlobe();
      Position localPosition1 = localGlobe.computePositionFromPoint(paramVec4);
      Position localPosition2 = null;
      Vec4 localVec4 = paramDrawContext.getPointOnGlobe(localPosition1.getLatitude(), localPosition1.getLongitude());
      if (localVec4 != null)
        localPosition2 = localGlobe.computePositionFromPoint(localVec4);
      if (localPosition2 == null)
        localPosition2 = new Position(localPosition1, localGlobe.getElevation(localPosition1.getLatitude(), localPosition1.getLongitude()) * paramDrawContext.getVerticalExaggeration());
      d = localPosition1.getElevation() - localPosition2.getElevation();
    }
    return d;
  }

  private Matrix getModelviewInverse(OrbitViewModel paramOrbitViewModel, Globe paramGlobe, Position paramPosition, Angle paramAngle1, Angle paramAngle2, double paramDouble)
  {
    if ((paramOrbitViewModel != null) && (paramGlobe != null) && (paramPosition != null) && (paramAngle1 != null) && (paramAngle2 != null))
    {
      Matrix localMatrix = paramOrbitViewModel.computeTransformMatrix(paramGlobe, paramPosition, paramAngle1, paramAngle2, paramDouble);
      if (localMatrix != null)
        return localMatrix.getInverse();
    }
    return null;
  }

  private Vec4 getEyePoint(Matrix paramMatrix)
  {
    return paramMatrix != null ? Vec4.UNIT_W.transformBy4(paramMatrix) : null;
  }

  private Vec4 getPointOnNearPlane(Matrix paramMatrix, Angle paramAngle, Rectangle paramRectangle, double paramDouble)
  {
    if ((paramMatrix != null) && (paramAngle != null) && (paramRectangle != null) && (paramDouble >= 0.0D))
    {
      double d1 = (paramRectangle.getWidth() <= 0.0D) || (paramRectangle.getHeight() <= 0.0D) ? 1.0D : paramRectangle.getHeight() / paramRectangle.getWidth();
      double d2 = 2.0D * d1 * paramDouble * paramAngle.tanHalfAngle();
      Vec4 localVec4 = new Vec4(0.0D, -d2 / 2.0D, -paramDouble, 1.0D);
      return localVec4.transformBy4(paramMatrix);
    }
    return null;
  }

}