package gov.nasa.worldwind.examples.sunlight;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;
import java.awt.Rectangle;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import javax.media.opengl.GLContext;

public class AtmosphereLayer extends AbstractLayer
{
  protected static final int STACKS = 24;
  protected static final int SLICES = 64;
  protected int glListId = -1;
  protected double thickness = 60000.0D;
  protected double lastRebuildHorizon = 0.0D;
  protected AtmosphericScatteringComputer asc;
  protected Vec4 sunDirection;
  protected boolean update = true;

  public double getAtmosphereThickness()
  {
    return this.thickness;
  }

  public void setAtmosphereThickness(double paramDouble)
  {
    if (paramDouble < 0.0D)
    {
      String str = Logging.getMessage("generic.ArgumentOutOfRange");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.thickness = paramDouble;
    this.asc = null;
    this.update = true;
  }

  public Vec4 getSunDirection()
  {
    return this.sunDirection;
  }

  public void setSunDirection(Vec4 paramVec4)
  {
    this.sunDirection = paramVec4;
    this.update = true;
  }

  public void doRender(DrawContext paramDrawContext)
  {
    GL localGL = paramDrawContext.getGL();
    int i = 0;
    int j = 0;
    int k = 0;
    try
    {
      View localView = paramDrawContext.getView();
      Position localPosition = paramDrawContext.getGlobe().computePositionFromPoint(localView.getEyePoint());
      double d1 = paramDrawContext.getGlobe().getRadiusAt(localPosition);
      double d2 = localView.getEyePoint().getLength3();
      double d3 = localPosition.getElevation();
      double d4 = localView.computeHorizonDistance();
      double d5 = d4;
      double d6 = (-1.570796326794897D + Math.acos(d4 / d2)) * 180.0D / 3.141592653589793D;
      double d7 = 90.0D;
      if (d3 >= this.thickness)
      {
        double d8 = Math.sqrt(d2 * d2 - (d1 + this.thickness) * (d1 + this.thickness));
        d7 = (-1.570796326794897D + Math.acos(d8 / d2)) * 180.0D / 3.141592653589793D;
      }
      if ((d3 < this.thickness) && (d3 > this.thickness * 0.7D))
        d7 = (this.thickness - d3) / (this.thickness - this.thickness * 0.7D) * 90.0D;
      if ((this.update) || (this.glListId == -1) || (Math.abs(this.lastRebuildHorizon - d4) > 100.0D))
      {
        if (this.glListId != -1)
          localGL.glDeleteLists(this.glListId, 1);
        makeSkyDome(paramDrawContext, (float)d5, d6, d7, 64, 24);
        this.lastRebuildHorizon = d4;
        this.update = false;
      }
      localGL.glPushAttrib(8);
      localGL.glPopAttrib();
      localGL.glPushAttrib(291081);
      i = 1;
      localGL.glDisable(3553);
      localGL.glDisable(2929);
      localGL.glDepthMask(false);
      Matrix localMatrix1 = Matrix.fromPerspective(localView.getFieldOfView(), localView.getViewport().getWidth(), localView.getViewport().getHeight(), 10000.0D, 2.0D * d2 + 10000.0D);
      double[] arrayOfDouble = new double[16];
      localMatrix1.toArray(arrayOfDouble, 0, false);
      localGL.glMatrixMode(5889);
      localGL.glPushMatrix();
      k = 1;
      localGL.glLoadMatrixd(arrayOfDouble, 0);
      localGL.glMatrixMode(5888);
      localGL.glPushMatrix();
      j = 1;
      Matrix localMatrix2 = computeSkyTransform(paramDrawContext);
      Matrix localMatrix3 = localView.getModelviewMatrix().multiply(localMatrix2);
      localMatrix3.toArray(arrayOfDouble, 0, false);
      localGL.glLoadMatrixd(arrayOfDouble, 0);
      if (this.glListId != -1)
        localGL.glCallList(this.glListId);
    }
    finally
    {
      if (j != 0)
      {
        localGL.glMatrixMode(5888);
        localGL.glPopMatrix();
      }
      if (k != 0)
      {
        localGL.glMatrixMode(5889);
        localGL.glPopMatrix();
      }
      if (i != 0)
        localGL.glPopAttrib();
    }
  }

  protected void makeSkyDome(DrawContext paramDrawContext, float paramFloat, double paramDouble1, double paramDouble2, int paramInt1, int paramInt2)
  {
    if (this.sunDirection == null)
      return;
    GL localGL = paramDrawContext.getGL();
    this.glListId = localGL.glGenLists(1);
    localGL.glNewList(this.glListId, 4864);
    drawSkyGradient(paramDrawContext, paramFloat, paramDouble1, paramDouble2, paramInt1, paramInt2);
    localGL.glEndList();
  }

  protected void drawSkyGradient(DrawContext paramDrawContext, float paramFloat, double paramDouble1, double paramDouble2, int paramInt1, int paramInt2)
  {
    if (this.asc == null)
      this.asc = new AtmosphericScatteringComputer(paramDrawContext.getGlobe().getRadius(), this.thickness);
    Matrix localMatrix = computeSkyTransform(paramDrawContext);
    GL localGL = paramDrawContext.getGL();
    localGL.glBlendFunc(770, 771);
    localGL.glEnable(3042);
    localGL.glDisable(3553);
    double d3 = paramDouble2;
    Color[] arrayOfColor = new Color[paramInt1 + 1];
    Vec4 localVec41 = paramDrawContext.getView().getEyePoint();
    double d1 = paramDouble1 - Math.max((paramDouble2 - paramDouble1) / 4.0D, 2.0D);
    localGL.glBegin(8);
    double d2;
    Vec4 localVec44;
    Color localColor;
    for (int i = 0; i <= paramInt1; i++)
    {
      d2 = 180.0F - i / paramInt1 * 360.0F;
      Vec4 localVec42 = SphericalToCartesian(d1, d2, paramFloat);
      localVec44 = SphericalToCartesian(paramDouble1, d2, paramFloat);
      localColor = this.asc.getAtmosphereColor(localVec44.transformBy4(localMatrix), localVec41, this.sunDirection);
      localGL.glColor4f(localColor.getRed() / 255.0F, localColor.getGreen() / 255.0F, localColor.getBlue() / 255.0F, 0.0F);
      localGL.glVertex3d(localVec42.getX(), localVec42.getY(), localVec42.getZ());
      localGL.glColor4f(localColor.getRed() / 255.0F, localColor.getGreen() / 255.0F, localColor.getBlue() / 255.0F, localColor.getAlpha() / 255.0F);
      localGL.glVertex3d(localVec44.getX(), localVec44.getY(), localVec44.getZ());
      arrayOfColor[i] = localColor;
    }
    localGL.glEnd();
    for (int i = 1; i < paramInt2 - 1; i++)
    {
      double d4 = (i - 1) / (paramInt2 - 1.0F);
      double d6 = 1.0D - Math.cos(d4 * 3.141592653589793D / 2.0D);
      d1 = paramDouble1 + Math.pow(d6, 3.0D) * (paramDouble2 - paramDouble1);
      double d5 = i / (paramInt2 - 1.0F);
      double d7 = 1.0D - Math.cos(d5 * 3.141592653589793D / 2.0D);
      d3 = paramDouble1 + Math.pow(d7, 3.0D) * (paramDouble2 - paramDouble1);
      localGL.glBegin(8);
      for (int j = 0; j <= paramInt1; j++)
      {
        d2 = 180.0F - j / paramInt1 * 360.0F;
        localVec44 = SphericalToCartesian(d1, d2, paramFloat);
        localColor = arrayOfColor[j];
        localGL.glColor4f(localColor.getRed() / 255.0F, localColor.getGreen() / 255.0F, localColor.getBlue() / 255.0F, localColor.getAlpha() / 255.0F);
        localGL.glVertex3d(localVec44.getX(), localVec44.getY(), localVec44.getZ());
        localVec44 = SphericalToCartesian(d3, d2, paramFloat);
        localColor = this.asc.getAtmosphereColor(localVec44.transformBy4(localMatrix), localVec41, this.sunDirection);
        localGL.glColor4f(localColor.getRed() / 255.0F, localColor.getGreen() / 255.0F, localColor.getBlue() / 255.0F, localColor.getAlpha() / 255.0F);
        localGL.glVertex3d(localVec44.getX(), localVec44.getY(), localVec44.getZ());
        arrayOfColor[j] = localColor;
      }
      localGL.glEnd();
    }
    if (paramDouble2 < 90.0D)
    {
      localGL.glBegin(8);
      for (int i = 0; i <= paramInt1; i++)
      {
        d2 = 180.0F - i / paramInt1 * 360.0F;
        Vec4 localVec43 = SphericalToCartesian(d3, d2, paramFloat);
        localColor = arrayOfColor[i];
        localGL.glColor4f(localColor.getRed() / 255.0F, localColor.getGreen() / 255.0F, localColor.getBlue() / 255.0F, localColor.getAlpha() / 255.0F);
        localGL.glVertex3d(localVec43.getX(), localVec43.getY(), localVec43.getZ());
        localVec43 = SphericalToCartesian(paramDouble2, d2, paramFloat);
        localGL.glColor4f(localColor.getRed() / 255.0F, localColor.getGreen() / 255.0F, localColor.getBlue() / 255.0F, 0.0F);
        localGL.glVertex3d(localVec43.getX(), localVec43.getY(), localVec43.getZ());
      }
      localGL.glEnd();
    }
    localGL.glEnable(3553);
    localGL.glDisable(3042);
  }

  protected Matrix computeSkyTransform(DrawContext paramDrawContext)
  {
    Matrix localMatrix = Matrix.IDENTITY;
    localMatrix = localMatrix.multiply(paramDrawContext.getGlobe().computeTransformToPosition(paramDrawContext.getView().getEyePosition()));
    localMatrix = localMatrix.multiply(Matrix.fromRotationX(Angle.POS90));
    return localMatrix;
  }

  protected static Vec4 SphericalToCartesian(double paramDouble1, double paramDouble2, double paramDouble3)
  {
    paramDouble1 *= 0.0174532925199433D;
    paramDouble2 *= 0.0174532925199433D;
    double d = paramDouble3 * Math.cos(paramDouble1);
    return new Vec4(d * Math.sin(paramDouble2), paramDouble3 * Math.sin(paramDouble1), d * Math.cos(paramDouble2));
  }

  public void dispose()
  {
    if (this.glListId < 0)
      return;
    GLContext localGLContext = GLContext.getCurrent();
    if (localGLContext == null)
      return;
    localGLContext.getGL().glDeleteLists(this.glListId, 1);
    this.glListId = -1;
  }

  public String toString()
  {
    return Logging.getMessage("layers.Earth.SkyGradientLayer.Name");
  }
}