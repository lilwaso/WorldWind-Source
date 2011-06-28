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

public abstract interface OrbitViewModel
{
  public abstract Matrix computeTransformMatrix(Globe paramGlobe, Position paramPosition, Angle paramAngle1, Angle paramAngle2, double paramDouble);

  public abstract ModelCoordinates computeModelCoordinates(Globe paramGlobe, Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43);

  public abstract ModelCoordinates computeModelCoordinates(Globe paramGlobe, Matrix paramMatrix, Vec4 paramVec4);

  public static abstract interface ModelCoordinates
  {
    public abstract Position getCenterPosition();

    public abstract Angle getHeading();

    public abstract Angle getPitch();

    public abstract double getZoom();
  }
}