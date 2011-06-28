package gov.nasa.worldwind.examples.sunlight;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ScreenAnnotation;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Iterator;

public class LensFlareLayer extends RenderableLayer
{
  private static double SUN_DISTANCE = 149597892000.0D;
  private Vec4 sunDirection;
  private Vec4 sunPoint;
  public static final String PRESET_BOLD = "LensFlare.PresetBold";

  public LensFlareLayer()
  {
    setName("Lens Flare");
    setPickEnabled(false);
  }

  public Vec4 getSunDirection()
  {
    return this.sunDirection;
  }

  public void setSunDirection(Vec4 paramVec4)
  {
    if (paramVec4 != null)
    {
      this.sunDirection = paramVec4.normalize3();
      this.sunPoint = this.sunDirection.multiply3(SUN_DISTANCE);
    }
    else
    {
      this.sunDirection = null;
      this.sunPoint = null;
    }
  }

  public void render(DrawContext paramDrawContext)
  {
    if (this.sunPoint == null)
      return;
    if (paramDrawContext.getView().getFrustumInModelCoordinates().getNear().distanceTo(this.sunPoint) < 0.0D)
      return;
    Vec4 localVec4 = paramDrawContext.getView().project(this.sunPoint);
    if (localVec4 == null)
      return;
    Rectangle localRectangle = paramDrawContext.getView().getViewport();
    if (!localRectangle.contains(localVec4.x, localVec4.y))
      return;
    Line localLine = new Line(paramDrawContext.getView().getEyePoint(), this.sunDirection);
    if (paramDrawContext.getSurfaceGeometry().intersect(localLine) != null)
      return;
    Point localPoint1 = new Point(localRectangle.width / 2, localRectangle.height / 2);
    Point localPoint2 = new Point((int)localVec4.x, (int)localVec4.y);
    Iterator localIterator = getRenderables().iterator();
    while (localIterator.hasNext())
    {
      Renderable localRenderable = (Renderable)localIterator.next();
      if ((localRenderable instanceof FlareImage))
        ((FlareImage)localRenderable).update(localPoint2, localPoint1);
    }
    super.render(paramDrawContext);
  }

  public static LensFlareLayer getPresetInstance(String paramString)
  {
    LensFlareLayer localLensFlareLayer = new LensFlareLayer();
    BufferedImage localBufferedImage1 = createDiskImage(64, Color.YELLOW);
    BufferedImage localBufferedImage2 = createHaloImage(64, new Color(1.0F, 1.0F, 0.8F), 2.0F);
    BufferedImage localBufferedImage3 = createDiskImage(128, Color.WHITE);
    BufferedImage localBufferedImage4 = createStarImage(128, Color.WHITE);
    BufferedImage localBufferedImage5 = createHaloImage(128, Color.WHITE);
    BufferedImage localBufferedImage6 = createRainbowImage(128);
    BufferedImage localBufferedImage7 = createRaysImage(128, 12, Color.WHITE);
    if ("LensFlare.PresetBold".equals(paramString))
    {
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage7, 4.0D, 0.0D, 0.05D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage4, 1.4D, 0.0D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage4, 2.5D, 0.0D, 0.04D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage2, 0.6D, 0.0D, 0.9D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage5, 1.0D, 0.0D, 0.9D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage5, 4.0D, 0.0D, 0.9D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage6, 2.2D, 0.0D, 0.03D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage6, 1.2D, 0.0D, 0.04D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.1D, 0.4D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.15D, 0.6D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.2D, 0.7D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.5D, 1.1D, 0.2D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.2D, 1.3D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.1D, 1.4D, 0.05D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.1D, 1.5D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.1D, 1.6D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.2D, 1.65D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.12D, 1.71D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 3.0D, 2.2D, 0.05D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.5D, 2.4D, 0.2D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.7D, 2.6D, 0.1D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage6, 5.0D, 3.0D, 0.03D));
      localLensFlareLayer.addRenderable(new FlareImage(localBufferedImage3, 0.2D, 3.5D, 0.1D));
    }
    return localLensFlareLayer;
  }

  public static BufferedImage createDiskImage(int paramInt, Color paramColor)
  {
    return PatternFactory.createPattern("PatternFactory.PatternCircle", new Dimension(paramInt, paramInt), 0.9F, paramColor);
  }

  public static BufferedImage createBluredDiskImage(int paramInt, Color paramColor)
  {
    BufferedImage localBufferedImage = PatternFactory.createPattern("PatternFactory.PatternCircle", new Dimension(paramInt, paramInt), 0.6F, paramColor);
    localBufferedImage = PatternFactory.blur(localBufferedImage, paramInt / 5);
    localBufferedImage = PatternFactory.blur(localBufferedImage, 10);
    return localBufferedImage;
  }

  public static BufferedImage createStarImage(int paramInt, Color paramColor)
  {
    BufferedImage localBufferedImage = new BufferedImage(paramInt, paramInt, 2);
    Graphics2D localGraphics2D = (Graphics2D)localBufferedImage.getGraphics();
    localGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    localGraphics2D.setColor(paramColor);
    float f1 = paramInt / 2.0F;
    float f2 = f1 * 0.9F;
    float f3 = f1 * 0.1F;
    float f4 = f1 * 0.05F;
    GeneralPath localGeneralPath = new GeneralPath();
    localGeneralPath.moveTo(f1 - f2, f1);
    localGeneralPath.lineTo(f1 - f3, f1 - f4);
    localGeneralPath.lineTo(f1 - f4, f1 - f3);
    localGeneralPath.lineTo(f1, f1 - f2);
    localGeneralPath.lineTo(f1 + f4, f1 - f3);
    localGeneralPath.lineTo(f1 + f3, f1 - f4);
    localGeneralPath.lineTo(f1 + f2, f1);
    localGeneralPath.lineTo(f1 + f3, f1 + f4);
    localGeneralPath.lineTo(f1 + f4, f1 + f3);
    localGeneralPath.lineTo(f1, f1 + f2);
    localGeneralPath.lineTo(f1 - f4, f1 + f3);
    localGeneralPath.lineTo(f1 - f3, f1 + f4);
    localGeneralPath.lineTo(f1 - f2, f1);
    localGraphics2D.fill(localGeneralPath);
    localGraphics2D.translate(f1, f1);
    localGraphics2D.rotate(0.7853981633974483D);
    localGraphics2D.scale(0.7D, 0.7D);
    localGraphics2D.translate(-f1, -f1);
    localGraphics2D.fill(localGeneralPath);
    return localBufferedImage;
  }

  public static BufferedImage createRaysImage(int paramInt1, int paramInt2, Color paramColor)
  {
    BufferedImage localBufferedImage = new BufferedImage(paramInt1, paramInt1, 2);
    Graphics2D localGraphics2D = (Graphics2D)localBufferedImage.getGraphics();
    localGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    localGraphics2D.setColor(paramColor);
    float f1 = paramInt1 / 2.0F;
    float f2 = f1 * 0.9F;
    float f3 = f1 * 0.1F;
    GeneralPath localGeneralPath = new GeneralPath();
    localGeneralPath.moveTo(f1, f1);
    localGeneralPath.lineTo(f1 - f3, f1 - f2);
    localGeneralPath.lineTo(f1 + f3, f1 - f2);
    localGeneralPath.lineTo(f1, f1);
    Color localColor = new Color(paramColor.getRed(), paramColor.getGreen(), paramColor.getBlue(), 0);
    GradientPaint localGradientPaint = new GradientPaint(f1, f1, paramColor, f1, f1 - f2, localColor);
    localGraphics2D.setPaint(localGradientPaint);
    for (int i = 0; i < paramInt2; i++)
    {
      localGraphics2D.translate(f1, f1);
      localGraphics2D.rotate(6.283185307179586D / paramInt2);
      localGraphics2D.translate(-f1, -f1);
      localGraphics2D.fill(localGeneralPath);
    }
    return localBufferedImage;
  }

  public static BufferedImage createHaloImage(int paramInt, Color paramColor)
  {
    return createHaloImage(paramInt, paramColor, 0.2F);
  }

  public static BufferedImage createHaloImage(int paramInt, Color paramColor, float paramFloat)
  {
    BufferedImage localBufferedImage = new BufferedImage(paramInt, paramInt, 2);
    Graphics2D localGraphics2D = (Graphics2D)localBufferedImage.getGraphics();
    localGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    localGraphics2D.setStroke(new BasicStroke(1.5F));
    float[] arrayOfFloat = new float[4];
    paramColor.getRGBComponents(arrayOfFloat);
    float f1 = paramInt / 2.0F;
    float f2 = 0.0F;
    float f3 = f1 * 0.9F;
    float f4 = f2;
    while (f4 <= f3)
    {
      float f5 = 1.0F - (float)Math.pow(f4 / f3, paramFloat);
      localGraphics2D.setColor(new Color(arrayOfFloat[0], arrayOfFloat[1], arrayOfFloat[2], f5));
      localGraphics2D.drawOval((int)(f1 - f4), (int)(f1 - f4), (int)(f4 * 2.0F), (int)(f4 * 2.0F));
      f4 += 1.0F;
    }
    return localBufferedImage;
  }

  public static BufferedImage createRainbowImage(int paramInt)
  {
    BufferedImage localBufferedImage = new BufferedImage(paramInt, paramInt, 2);
    Graphics2D localGraphics2D = (Graphics2D)localBufferedImage.getGraphics();
    localGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    float f1 = paramInt / 2.0F;
    float f2 = f1 * 0.7F;
    float f3 = f1 * 0.9F;
    float f4 = f2;
    while (f4 <= f3)
    {
      float f5 = (f4 - f2) / (f3 - f2);
      localGraphics2D.setColor(new Color(Color.HSBtoRGB(f5, 1.0F, 1.0F)));
      localGraphics2D.drawOval((int)(f1 - f4), (int)(f1 - f4), (int)(f4 * 2.0F), (int)(f4 * 2.0F));
      f4 += 1.0F;
    }
    return localBufferedImage;
  }

  public static class FlareImage extends ScreenAnnotation
  {
    private final BufferedImage image;
    private double scale = 1.0D;
    private double position = 0.0D;
    private double opacity = 0.5D;

    public FlareImage(BufferedImage paramBufferedImage, double paramDouble1, double paramDouble2, double paramDouble3)
    {
      super("",new Point(0, 0));
      this.image = paramBufferedImage;
      this.scale = paramDouble1;
      this.position = paramDouble2;
      this.opacity = paramDouble3;
      initialize();
    }

    private void initialize()
    {
      AnnotationAttributes localAnnotationAttributes = getAttributes();
      localAnnotationAttributes.setBorderWidth(0.0D);
      localAnnotationAttributes.setImageSource(this.image);
      localAnnotationAttributes.setAdjustWidthToText("render.Annotation.SizeFixed");
      localAnnotationAttributes.setSize(new Dimension(this.image.getWidth(), this.image.getHeight()));
      localAnnotationAttributes.setBackgroundColor(new Color(0, 0, 0, 0));
      localAnnotationAttributes.setCornerRadius(0);
      localAnnotationAttributes.setInsets(new Insets(0, 0, 0, 0));
      localAnnotationAttributes.setDrawOffset(new Point(0, -this.image.getHeight() / 2));
    }

    public void update(Point paramPoint1, Point paramPoint2)
    {
      double d1 = paramPoint1.x - (paramPoint1.x - paramPoint2.x) * this.position;
      double d2 = paramPoint1.y - (paramPoint1.y - paramPoint2.y) * this.position;
      setScreenPoint(new Point((int)d1, (int)d2));
      getAttributes().setScale(this.scale);
      getAttributes().setOpacity(this.opacity);
    }
  }
}