package gov.nasa.worldwind.examples.sunlight;

import gov.nasa.worldwind.geom.Vec4;
import java.awt.Color;

public class AtmosphericScatteringComputer
{
  private float fInnerRadius;
  private float fOuterRadius;
  private float fScale;
  private int nSamples = 4;
  private float Kr = 0.001F;
  private float Kr4PI = this.Kr * 4.0F * 3.141593F;
  private float Km = 0.0015F;
  private float Km4PI = this.Km * 4.0F * 3.141593F;
  private float ESun = 15.0F;
  private float g = -0.85F;
  private float fRayleighScaleDepth = 0.25F;
  private float fMieScaleDepth = 0.1F;
  private float[] fWavelength = { 0.65F, 0.57F, 0.475F };
  private float[] fWavelength4 = new float[3];
  private float[] fCameraDepth = { 0.0F, 0.0F, 0.0F, 0.0F };
  private float[] fLightDepth = new float[4];
  private float[] fSampleDepth = new float[4];
  private float[] fRayleighSum = { 0.0F, 0.0F, 0.0F };
  private float[] fMieSum = { 0.0F, 0.0F, 0.0F };
  private float[] fAttenuation = new float[3];
  private float[] opticalDepthBuffer;
  private float DELTA = 1.0E-006F;
  private int nChannels = 4;
  private int nBufferWidth = 128;
  private int nBufferHeight = 128;
  private Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

  public AtmosphericScatteringComputer(double paramDouble1, double paramDouble2)
  {
    this.fWavelength4[0] = (float)Math.pow(this.fWavelength[0], 4.0D);
    this.fWavelength4[1] = (float)Math.pow(this.fWavelength[1], 4.0D);
    this.fWavelength4[2] = (float)Math.pow(this.fWavelength[2], 4.0D);
    this.fInnerRadius = (float)paramDouble1;
    this.fOuterRadius = (float)(paramDouble1 + paramDouble2);
    this.fScale = (1.0F / (this.fOuterRadius - this.fInnerRadius));
    computeOpticalDepthBuffer();
  }

  public Color getAtmosphereColor(Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43)
  {
    Vec4 localVec41 = paramVec41.subtract3(paramVec42);
    localVec41 = localVec41.normalize3();
    float f1 = 2.0F * (float)paramVec42.dot3(localVec41);
    float f2 = (float)(paramVec42.dotSelf3() - this.fOuterRadius * this.fOuterRadius);
    float f3 = f1 * f1 - 4.0F * f2;
    Color localColor = this.TRANSPARENT_COLOR;
    if (f3 >= 0.0F)
    {
      float f4 = 0.5F * (-f1 - (float)Math.sqrt(f3));
      float f5 = 0.5F * (-f1 + (float)Math.sqrt(f3));
      if ((f4 >= 0.0F) || (f5 >= 0.0F))
      {
        float f6 = Math.max(f4, f5);
        Vec4 localVec42 = paramVec42.add3(localVec41.multiply3(f6));
        localColor = getColorForVertex(localVec42, paramVec42, paramVec43);
      }
    }
    return localColor;
  }

  private Color getColorForVertex(Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43)
  {
    Vec4 localVec41 = paramVec41.subtract3(paramVec42);
    float f1 = (float)localVec41.getLength3();
    localVec41 = localVec41.normalize3();
    float f2 = 2.0F * (float)paramVec42.dot3(localVec41);
    float f3 = (float)(paramVec42.dotSelf3() - this.fOuterRadius * this.fOuterRadius);
    float f4 = Math.max(0.0F, f2 * f2 - 4.0F * f3);
    float f5 = 0.5F * (-f2 - (float)Math.sqrt(f4));
    int i = 1;
    for (int j = 0; j < this.fCameraDepth.length; j++)
      this.fCameraDepth[j] = 0.0F;
    for (int j = 0; j < this.fLightDepth.length; j++)
      this.fLightDepth[j] = 0.0F;
    for (int j = 0; j < this.fSampleDepth.length; j++)
      this.fSampleDepth[j] = 0.0F;
    if (f5 <= 0.0F)
    {
      f5 = 0.0F;
      float f6 = (float)paramVec42.getLength3();
      float f8 = (f6 - this.fInnerRadius) * this.fScale;
      i = f6 >= paramVec41.getLength3() ? 1 : 0;
      float f9 = (float)(i != 0 ? localVec41.getNegative3().dot3(paramVec42) : localVec41.dot3(localVec41)) / f6;
      interpolate(this.fCameraDepth, f8, 0.5F - f9 * 0.5F);
    }
    else
    {
      paramVec42 = paramVec42.add3(localVec41.multiply3(f5));
      f1 -= f5;
      f5 = 0.0F;
    }
    if (f1 <= this.DELTA)
      return this.TRANSPARENT_COLOR;
    for (int k = 0; k < this.fRayleighSum.length; k++)
      this.fRayleighSum[k] = 0.0F;
    for (int k = 0; k < this.fMieSum.length; k++)
      this.fMieSum[k] = 0.0F;
    float f7 = f1 / this.nSamples;
    float f8 = f7 * this.fScale;
    Vec4 localVec42 = localVec41.multiply3(f7);
    paramVec41 = paramVec42.add3(localVec42.multiply3(0.5D));
    for (int m = 0; m < this.nSamples; m++)
    {
      float f11 = (float)paramVec41.getLength3();
      float f12 = (float)paramVec43.dot3(paramVec41) / f11;
      float f13 = (f11 - this.fInnerRadius) * this.fScale;
      interpolate(this.fLightDepth, f13, 0.5F - f12 * 0.5F);
      if (this.fLightDepth[0] > this.DELTA)
      {
        float f14 = f8 * this.fLightDepth[0];
        float f15 = this.fLightDepth[1];
        float f16 = f8 * this.fLightDepth[2];
        float f17 = this.fLightDepth[3];
        float f18;
        if (i != 0)
        {
          f18 = (float)localVec41.getNegative3().dot3(paramVec41) / f11;
          interpolate(this.fSampleDepth, f13, 0.5F - f18 * 0.5F);
          f15 += this.fSampleDepth[1] - this.fCameraDepth[1];
          f17 += this.fSampleDepth[3] - this.fCameraDepth[3];
        }
        else
        {
          f18 = (float)localVec41.dot3(paramVec41) / f11;
          interpolate(this.fSampleDepth, f13, 0.5F - f18 * 0.5F);
          f15 += this.fCameraDepth[1] - this.fSampleDepth[1];
          f17 += this.fCameraDepth[3] - this.fSampleDepth[3];
        }
        f15 *= this.Kr4PI;
        f17 *= this.Km4PI;
        this.fAttenuation[0] = (float)Math.exp(-f15 / this.fWavelength4[0] - f17);
        this.fAttenuation[1] = (float)Math.exp(-f15 / this.fWavelength4[1] - f17);
        this.fAttenuation[2] = (float)Math.exp(-f15 / this.fWavelength4[2] - f17);
        this.fRayleighSum[0] += f14 * this.fAttenuation[0];
        this.fRayleighSum[1] += f14 * this.fAttenuation[1];
        this.fRayleighSum[2] += f14 * this.fAttenuation[2];
        this.fMieSum[0] += f16 * this.fAttenuation[0];
        this.fMieSum[1] += f16 * this.fAttenuation[1];
        this.fMieSum[2] += f16 * this.fAttenuation[2];
      }
      paramVec41 = paramVec41.add3(localVec42);
    }
    float f10 = (float)localVec41.getNegative3().dot3(paramVec43);
    float[] arrayOfFloat1 = new float[2];
    float f12 = f10 * f10;
    float f13 = this.g * this.g;
    arrayOfFloat1[0] = (0.75F * (1.0F + f12));
    arrayOfFloat1[1] = (1.5F * ((1.0F - f13) / (2.0F + f13)) * (1.0F + f12) / (float)Math.pow(1.0F + f13 - 2.0F * this.g * f10, 1.5D));
    arrayOfFloat1[0] *= this.Kr * this.ESun;
    arrayOfFloat1[1] *= this.Km * this.ESun;
    float[] arrayOfFloat2 = { 0.0F, 0.0F, 0.0F };
    arrayOfFloat2[0] = (this.fRayleighSum[0] * arrayOfFloat1[0] / this.fWavelength4[0] + this.fMieSum[0] * arrayOfFloat1[1]);
    arrayOfFloat2[1] = (this.fRayleighSum[1] * arrayOfFloat1[0] / this.fWavelength4[1] + this.fMieSum[1] * arrayOfFloat1[1]);
    arrayOfFloat2[2] = (this.fRayleighSum[2] * arrayOfFloat1[0] / this.fWavelength4[2] + this.fMieSum[2] * arrayOfFloat1[1]);
    arrayOfFloat2[0] = Math.min(arrayOfFloat2[0], 1.0F);
    arrayOfFloat2[1] = Math.min(arrayOfFloat2[1], 1.0F);
    arrayOfFloat2[2] = Math.min(arrayOfFloat2[2], 1.0F);
    float f15 = (arrayOfFloat2[0] + arrayOfFloat2[1] + arrayOfFloat2[2]) / 3.0F;
    f15 = (float)Math.min(f15 + 0.5D, 1.0D);
    return new Color(arrayOfFloat2[0], arrayOfFloat2[1], arrayOfFloat2[2], f15);
  }

  private void interpolate(float[] paramArrayOfFloat, float paramFloat1, float paramFloat2)
  {
    float f1 = paramFloat1 * (this.nBufferWidth - 1);
    float f2 = paramFloat2 * (this.nBufferHeight - 1);
    int i = Math.min(this.nBufferWidth - 2, Math.max(0, (int)f1));
    int j = Math.min(this.nBufferHeight - 2, Math.max(0, (int)f2));
    float f3 = f1 - i;
    float f4 = f2 - j;
    int k = (this.nBufferWidth * j + i) * 4;
    for (int m = 0; m < this.nChannels; m++)
    {
      paramArrayOfFloat[m] = (this.opticalDepthBuffer[k] * (1.0F - f3) * (1.0F - f4) + this.opticalDepthBuffer[(k + this.nChannels)] * f3 * (1.0F - f4) + this.opticalDepthBuffer[(k + this.nChannels * this.nBufferWidth)] * (1.0F - f3) * f4 + this.opticalDepthBuffer[(k + this.nChannels * (this.nBufferWidth + 1))] * f3 * f4);
      k++;
    }
  }

  private void computeOpticalDepthBuffer()
  {
    int i = 128;
    int j = 50;
    if (this.opticalDepthBuffer == null)
      this.opticalDepthBuffer = new float[i * i * 4];
    int k = 0;
    for (int m = 0; m < i; m++)
    {
      float f1 = 1.0F - (m + m) / i;
      float f2 = (float)Math.acos(f1);
      Vec4 localVec41 = new Vec4((float)Math.sin(f2), (float)Math.cos(f2), 0.0D);
      for (int n = 0; n < i; n++)
      {
        float f3 = this.DELTA + this.fInnerRadius + (this.fOuterRadius - this.fInnerRadius) * n / i;
        Vec4 localVec42 = new Vec4(0.0D, f3, 0.0D);
        float f4 = 2.0F * (float)localVec42.dot3(localVec41);
        float f5 = f4 * f4;
        float f6 = (float)localVec42.dotSelf3();
        float f7 = f6 - this.fInnerRadius * this.fInnerRadius;
        float f8 = f5 - 4.0F * f7;
        int i1 = (f8 < 0.0F) || ((0.5F * (-f4 - (float)Math.sqrt(f8)) <= 0.0F) && (0.5F * (-f4 + (float)Math.sqrt(f8)) <= 0.0F)) ? 1 : 0;
        float f9;
        float f10;
        if (i1 != 0)
        {
          f9 = (float)Math.exp(-(f3 - this.fInnerRadius) * this.fScale / this.fRayleighScaleDepth);
          f10 = (float)Math.exp(-(f3 - this.fInnerRadius) * this.fScale / this.fMieScaleDepth);
        }
        else
        {
          f9 = this.opticalDepthBuffer[(k - i * this.nChannels)] * 0.75F;
          f10 = this.opticalDepthBuffer[(k + 2 - i * this.nChannels)] * 0.75F;
        }
        f7 = f6 - this.fOuterRadius * this.fOuterRadius;
        f8 = f5 - 4.0F * f7;
        float f11 = 0.5F * (-f4 + (float)Math.sqrt(f8));
        float f12 = f11 / j;
        float f13 = f12 * this.fScale;
        Vec4 localVec43 = localVec41.multiply3(f12);
        localVec42 = localVec42.add3(localVec43.multiply3(0.5D));
        float f14 = 0.0F;
        float f15 = 0.0F;
        for (int i2 = 0; i2 < j; i2++)
        {
          f3 = (float)localVec42.getLength3();
          float f16 = (f3 - this.fInnerRadius) * this.fScale;
          f16 = Math.max(f16, 0.0F);
          f14 += (float)Math.exp(-f16 / this.fRayleighScaleDepth);
          f15 += (float)Math.exp(-f16 / this.fMieScaleDepth);
          localVec42 = localVec42.add3(localVec43);
        }
        f14 *= f13;
        f15 *= f13;
        this.opticalDepthBuffer[(k++)] = f9;
        this.opticalDepthBuffer[(k++)] = f14;
        this.opticalDepthBuffer[(k++)] = f10;
        this.opticalDepthBuffer[(k++)] = f15;
      }
    }
  }
}