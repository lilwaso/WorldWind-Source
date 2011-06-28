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
import gov.nasa.worldwind.util.Logging;
import java.util.logging.Logger;

public class BasicOrbitViewModel
  implements OrbitViewModel
{
  public Matrix computeTransformMatrix(Globe paramGlobe, Position paramPosition, Angle paramAngle1, Angle paramAngle2, double paramDouble)
  {
    if (paramGlobe == null)
    {
            String localObject = Logging.getMessage("nullValue.GlobeIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramPosition == null)
    {
            String localObject = Logging.getMessage("nullValue.CenterIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramAngle1 == null)
    {
            String localObject = Logging.getMessage("nullValue.HeadingIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramAngle2 == null)
    {
            String localObject = Logging.getMessage("nullValue.PitchIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = computeHeadingPitchZoomTransform(paramAngle1, paramAngle2, paramDouble);
    localObject = ((Matrix)localObject).multiply(computeCenterTransform(paramGlobe, paramPosition));
    return (Matrix)localObject;
  }

  public OrbitViewModel.ModelCoordinates computeModelCoordinates(Globe paramGlobe, Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43)
  {
    if (paramGlobe == null)
    {
            String localObject = Logging.getMessage("nullValue.GlobeIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramVec41 == null)
    {
            String localObject = "nullValue.EyePointIsNull";
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramVec42 == null)
    {
            String localObject = "nullValue.CenterPointIsNull";
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramVec43 == null)
    {
            String localObject = "nullValue.UpIsNull";
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = Matrix.fromViewLookAt(paramVec41, paramVec42, paramVec43);
    return (OrbitViewModel.ModelCoordinates)computeModelCoordinates(paramGlobe, (Matrix)localObject, paramVec42);
  }

  public OrbitViewModel.ModelCoordinates computeModelCoordinates(Globe paramGlobe, Matrix paramMatrix, Vec4 paramVec4)
  {
    if (paramGlobe == null)
    {
            String localObject1 = Logging.getMessage("nullValue.GlobeIsNull");
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
    if (paramMatrix == null)
    {
            String localObject1 = "nullValue.ModelTransformIsNull";
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
    if (paramVec4 == null)
    {
            String localObject1 = "nullValue.CenterPointIsNull";
      Logging.logger().severe((String)localObject1);
      throw new IllegalArgumentException((String)localObject1);
    }
    Object localObject1 = paramGlobe.computePositionFromPoint(paramVec4);
    Matrix localMatrix1 = computeCenterTransform(paramGlobe, (Position)localObject1);
    Matrix localMatrix2 = localMatrix1.getInverse();
    if (localMatrix2 == null)
    {
            String localObject2 = Logging.getMessage("generic.NoninvertibleMatrix");
      Logging.logger().severe((String)localObject2);
      throw new IllegalStateException((String)localObject2);
    }
    Object localObject2 = paramMatrix.multiply(localMatrix2);
    Angle localAngle1 = computeHeading((Matrix)localObject2);
    Angle localAngle2 = computePitch((Matrix)localObject2);
    double d = computeZoom((Matrix)localObject2);
    if ((localAngle1 == null) || (localAngle2 == null))
      return null;
    return (OrbitViewModel.ModelCoordinates)(OrbitViewModel.ModelCoordinates)new BasicModelCoordinates((Position)localObject1, localAngle1, localAngle2, d);
  }

  protected Matrix computeCenterTransform(Globe paramGlobe, Position paramPosition)
  {
    if (paramGlobe == null)
    {
            String localObject = Logging.getMessage("nullValue.GlobeIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramPosition == null)
    {
            String localObject = Logging.getMessage("nullValue.CenterIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = paramGlobe.computePointFromPosition(paramPosition);
    Vec4 localVec41 = paramGlobe.computeSurfaceNormalAtLocation(paramPosition.getLatitude(), paramPosition.getLongitude());
    Vec4 localVec42 = ((Vec4)localObject).subtract3(localVec41);
    Vec4 localVec43 = paramGlobe.computeNorthPointingTangentAtLocation(paramPosition.getLatitude(), paramPosition.getLongitude());
    return (Matrix)Matrix.fromViewLookAt((Vec4)localObject, localVec42, localVec43);
  }

  protected Matrix computeHeadingPitchZoomTransform(Angle paramAngle1, Angle paramAngle2, double paramDouble)
  {
    if (paramAngle1 == null)
    {
            String localObject = Logging.getMessage("nullValue.HeadingIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramAngle2 == null)
    {
            String localObject = Logging.getMessage("nullValue.PitchIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = Matrix.fromTranslation(0.0D, 0.0D, -paramDouble);
    localObject = ((Matrix)localObject).multiply(Matrix.fromRotationX(paramAngle2.multiply(-1.0D)));
    localObject = ((Matrix)localObject).multiply(Matrix.fromRotationZ(paramAngle1));
    return (Matrix)localObject;
  }

  protected Angle computeHeading(Matrix paramMatrix)
  {
    if (paramMatrix == null)
    {
      String str = "nullValue.HeadingPitchZoomTransformTransformIsNull";
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    return paramMatrix.getRotationZ();
  }

  protected Angle computePitch(Matrix paramMatrix)
  {
    if (paramMatrix == null)
    {
            String localObject = "nullValue.HeadingPitchZoomTransformTransformIsNull";
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = paramMatrix.getRotationX();
    if (localObject != null)
      localObject = ((Angle)localObject).multiply(-1.0D);
    return (Angle)localObject;
  }

  protected double computeZoom(Matrix paramMatrix)
  {
    if (paramMatrix == null)
    {
            String localObject = "nullValue.HeadingPitchZoomTransformTransformIsNull";
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = paramMatrix.getTranslation();
    return localObject != null ? ((Vec4)localObject).getLength3() : 0.0D;
  }

  protected static class BasicModelCoordinates
    implements OrbitViewModel.ModelCoordinates
  {
    private final Position center;
    private final Angle heading;
    private final Angle pitch;
    private final double zoom;

    public BasicModelCoordinates(Position paramPosition, Angle paramAngle1, Angle paramAngle2, double paramDouble)
    {
      String str;
      if (paramPosition == null)
      {
        str = Logging.getMessage("nullValue.CenterIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      if (paramAngle1 == null)
      {
        str = Logging.getMessage("nullValue.HeadingIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      if (paramAngle2 == null)
      {
        str = Logging.getMessage("nullValue.PitchIsNull");
        Logging.logger().severe(str);
        throw new IllegalArgumentException(str);
      }
      this.center = paramPosition;
      this.heading = paramAngle1;
      this.pitch = paramAngle2;
      this.zoom = paramDouble;
    }

    public Position getCenterPosition()
    {
      return this.center;
    }

    public Angle getHeading()
    {
      return this.heading;
    }

    public Angle getPitch()
    {
      return this.pitch;
    }

    public double getZoom()
    {
      return this.zoom;
    }
  }
}