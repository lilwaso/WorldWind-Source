package gov.nasa.worldwind.examples.sunlight;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WWObjectImpl;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.cache.BasicMemoryCache;
import gov.nasa.worldwind.cache.MemoryCache;
import gov.nasa.worldwind.cache.MemoryCacheSet;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Cylinder;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Frustum;
import gov.nasa.worldwind.geom.Intersection;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Plane;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Triangle;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.terrain.SectorGeometry;
import gov.nasa.worldwind.terrain.SectorGeometry.BoundaryEdge;
import gov.nasa.worldwind.terrain.SectorGeometry.ExtractedShapeDescription;
import gov.nasa.worldwind.terrain.SectorGeometry.GeographicTextureCoordinateComputer;
import gov.nasa.worldwind.terrain.SectorGeometryList;
import gov.nasa.worldwind.terrain.Tessellator;
import gov.nasa.worldwind.util.Logging;
import java.awt.Color;
import java.awt.Point;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
//import java.util.ArrayList<Lgov.nasa.worldwind.examples.sunlight.RectangularNormalTessellator.RectTile;>;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import javax.media.opengl.GL;
import java.lang.Object;

public class RectangularNormalTessellator extends WWObjectImpl
  implements Tessellator
{
  private static final double DEFAULT_LOG10_RESOLUTION_TARGET = 1.3D;
  private static final int DEFAULT_MAX_LEVEL = 12;
  private static final int DEFAULT_NUM_LAT_SUBDIVISIONS = 5;
  private static final int DEFAULT_NUM_LON_SUBDIVISIONS = 10;
  private static final int DEFAULT_DENSITY = 20;
  private static final String CACHE_NAME = "Terrain";
  private static final String CACHE_ID = RectangularNormalTessellator.class.getName();
  private static final HashMap<Integer, DoubleBuffer> parameterizations = new HashMap();
  private static final HashMap<Integer, IntBuffer> indexLists = new HashMap();
  private ArrayList<RectTile> topLevels;
  private PickSupport pickSupport = new PickSupport();
  private SectorGeometryList currentTiles = new SectorGeometryList();
  private Frustum currentFrustum;
  private Sector currentCoverage;
  private boolean makeTileSkirts = true;
  private int currentLevel;
  private int maxLevel = 12;
  private Globe globe;
  private int density = 20;
  private Vec4 lightDirection;
  private Material material = new Material(Color.WHITE);
  private Color lightColor = Color.WHITE;
  private Color ambientColor = new Color(0.1F, 0.1F, 0.1F);

  public SectorGeometryList tessellate(DrawContext paramDrawContext)
  {
    String str;
    if (paramDrawContext == null)
    {
      str = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramDrawContext.getView() == null)
    {
      str = Logging.getMessage("nullValue.ViewIsNull");
      Logging.logger().severe(str);
      throw new IllegalStateException(str);
    }
    if (!WorldWind.getMemoryCacheSet().containsCache(CACHE_ID))
    {
      long l = Configuration.getLongValue("gov.nasa.worldwind.avkey.SectorGeometryCacheSize", Long.valueOf(20000000L)).longValue();
      BasicMemoryCache localBasicMemoryCache = new BasicMemoryCache((long)(0.85D * l), l);
      localBasicMemoryCache.setName("Terrain");
      WorldWind.getMemoryCacheSet().addCache(CACHE_ID, localBasicMemoryCache);
    }
    if (this.topLevels == null)
      this.topLevels = createTopLevelTiles(paramDrawContext);
    this.currentTiles.clear();
    this.currentLevel = 0;
    this.currentCoverage = null;
    this.currentFrustum = paramDrawContext.getView().getFrustumInModelCoordinates();
    Iterator localIterator = this.topLevels.iterator();
    Object localObject;
    while (localIterator.hasNext())
    {
      localObject = (RectTile)localIterator.next();
      selectVisibleTiles(paramDrawContext, (RectTile)localObject);
    }
    this.currentTiles.setSector(this.currentCoverage);
    localIterator = this.currentTiles.iterator();
    while (localIterator.hasNext())
    {
      localObject = (SectorGeometry)localIterator.next();
      makeVerts(paramDrawContext, (RectTile)localObject);
    }
    return (SectorGeometryList)this.currentTiles;
  }

  private ArrayList<RectTile> createTopLevelTiles(DrawContext paramDrawContext)
  {
    ArrayList localArrayList = new ArrayList(50);
    this.globe = paramDrawContext.getGlobe();
    double d1 = 36.0D;
    double d2 = 36.0D;
    Object localObject1 = Angle.NEG90;
    for (int i = 0; i < 5; i++)
    {
      Angle localAngle1 = ((Angle)localObject1).addDegrees(d1);
      if (localAngle1.getDegrees() + 1.0D > 90.0D)
        localAngle1 = Angle.POS90;
      Object localObject2 = Angle.NEG180;
      for (int j = 0; j < 10; j++)
      {
        Angle localAngle2 = ((Angle)localObject2).addDegrees(d2);
        if (localAngle2.getDegrees() + 1.0D > 180.0D)
          localAngle2 = Angle.POS180;
        Sector localSector = new Sector((Angle)localObject1, localAngle1, (Angle)localObject2, localAngle2);
        localArrayList.add(createTile(paramDrawContext, localSector, 0));
        localObject2 = localAngle2;
      }
      localObject1 = localAngle1;
    }
    return (ArrayList<RectTile>)(ArrayList<RectTile>)localArrayList;
  }

  private RectTile createTile(DrawContext paramDrawContext, Sector paramSector, int paramInt)
  {
    Cylinder localCylinder = paramDrawContext.getGlobe().computeBoundingCylinder(paramDrawContext.getVerticalExaggeration(), paramSector);
    double d = paramSector.getDeltaLatRadians() * paramDrawContext.getGlobe().getRadius() / this.density;
    return new RectTile(this, localCylinder, paramInt, this.density, paramSector, d);
  }

  public boolean isMakeTileSkirts()
  {
    return this.makeTileSkirts;
  }

  public void setMakeTileSkirts(boolean paramBoolean)
  {
    this.makeTileSkirts = paramBoolean;
  }

  public Vec4 getLightDirection()
  {
    return this.lightDirection;
  }

  public void setLightDirection(Vec4 paramVec4)
  {
    this.lightDirection = paramVec4;
  }

  public Color getLightColor()
  {
    return this.lightColor;
  }

  public void setLightColor(Color paramColor)
  {
    if (paramColor == null)
    {
      String str = Logging.getMessage("nullValue.ColorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.lightColor = paramColor;
  }

  public Color getAmbientColor()
  {
    return this.ambientColor;
  }

  public void setAmbientColor(Color paramColor)
  {
    if (paramColor == null)
    {
      String str = Logging.getMessage("nullValue.ColorIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    this.ambientColor = paramColor;
  }

  private void selectVisibleTiles(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    Extent localExtent = paramRectTile.getExtent();
    if ((localExtent != null) && (!localExtent.intersects(this.currentFrustum)))
      return;
    if ((this.currentLevel < this.maxLevel - 1) && (needToSplit(paramDrawContext, paramRectTile)))
    {
      this.currentLevel += 1;
      RectTile[] arrayOfRectTile1 = split(paramDrawContext, paramRectTile);
      for (RectTile localRectTile : arrayOfRectTile1)
        selectVisibleTiles(paramDrawContext, localRectTile);
      this.currentLevel -= 1;
      return;
    }
    this.currentCoverage = paramRectTile.getSector().union(this.currentCoverage);
    this.currentTiles.add(paramRectTile);
  }

  private boolean needToSplit(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    Vec4[] arrayOfVec4 = paramRectTile.sector.computeCornerPoints(paramDrawContext.getGlobe(), paramDrawContext.getVerticalExaggeration());
    Vec4 localVec4 = paramRectTile.sector.computeCenterPoint(paramDrawContext.getGlobe(), paramDrawContext.getVerticalExaggeration());
    View localView = paramDrawContext.getView();
    double d1 = localView.getEyePoint().distanceTo3(arrayOfVec4[0]);
    double d2 = localView.getEyePoint().distanceTo3(arrayOfVec4[1]);
    double d3 = localView.getEyePoint().distanceTo3(arrayOfVec4[2]);
    double d4 = localView.getEyePoint().distanceTo3(arrayOfVec4[3]);
    double d5 = localView.getEyePoint().distanceTo3(localVec4);
    double d6 = d1;
    if (d2 < d6)
      d6 = d2;
    if (d3 < d6)
      d6 = d3;
    if (d4 < d6)
      d6 = d4;
    if (d5 < d6)
      d6 = d5;
    double d7 = Math.log10(d6);
    int i = paramRectTile.log10CellSize <= d7 - 1.3D ? 1 : 0;
    return i == 0;
  }

  private RectTile[] split(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    Sector[] arrayOfSector = paramRectTile.sector.subdivide();
    RectTile[] arrayOfRectTile = new RectTile[4];
    arrayOfRectTile[0] = createTile(paramDrawContext, arrayOfSector[0], paramRectTile.getLevel() + 1);
    arrayOfRectTile[1] = createTile(paramDrawContext, arrayOfSector[1], paramRectTile.getLevel() + 1);
    arrayOfRectTile[2] = createTile(paramDrawContext, arrayOfSector[2], paramRectTile.getLevel() + 1);
    arrayOfRectTile[3] = createTile(paramDrawContext, arrayOfSector[3], paramRectTile.getLevel() + 1);
    return arrayOfRectTile;
  }

  private CacheKey createCacheKey(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    return new CacheKey(paramDrawContext, paramRectTile.sector, paramRectTile.density);
  }

  private void makeVerts(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    MemoryCache localMemoryCache = WorldWind.getMemoryCache(CACHE_ID);
    CacheKey localCacheKey = createCacheKey(paramDrawContext, paramRectTile);
    RectTile.access$1502(paramRectTile, (RenderInfo)localMemoryCache.getObject(localCacheKey));
    if ((paramRectTile.ri != null) && (RectTile.access$1500(paramRectTile).time >= System.currentTimeMillis() - 1000L))
      return;
    RectTile.access$1502(paramRectTile, buildVerts(paramDrawContext, paramRectTile, this.makeTileSkirts));
    if (paramRectTile.ri != null)
    {
      localCacheKey = createCacheKey(paramDrawContext, paramRectTile);
      localMemoryCache.add(localCacheKey, paramRectTile.ri, paramRectTile.ri.getSizeInBytes());
    }
  }

  public RenderInfo buildVerts(DrawContext paramDrawContext, RectTile paramRectTile, boolean paramBoolean)
  {
    int i = paramRectTile.density;
    int j = i + 3;
    int k = j * j;
    DoubleBuffer localDoubleBuffer1 = BufferUtil.newDoubleBuffer(k * 3);
    ArrayList localArrayList = computeLocations(paramRectTile);
    double[] arrayOfDouble = new double[localArrayList.size()];
    paramDrawContext.getGlobe().getElevations(paramRectTile.sector, localArrayList, paramRectTile.getResolution(), arrayOfDouble);
    Globe localGlobe = paramDrawContext.getGlobe();
    Angle localAngle1 = paramRectTile.sector.getDeltaLat().divide(i);
    Angle localAngle2 = paramRectTile.sector.getMinLatitude();
    Angle localAngle3 = paramRectTile.sector.getMaxLatitude();
    Angle localAngle4 = paramRectTile.sector.getDeltaLon().divide(i);
    Angle localAngle5 = paramRectTile.sector.getMinLongitude();
    Angle localAngle6 = paramRectTile.sector.getMaxLongitude();
    int m = 0;
    double d2 = paramDrawContext.getVerticalExaggeration();
    LatLon localLatLon1 = paramRectTile.sector.getCentroid();
    Vec4 localVec42 = localGlobe.computePointFromPosition(localLatLon1.getLatitude(), localLatLon1.getLongitude(), 0.0D);
    int n = 0;
    Iterator localIterator = localArrayList.iterator();
    double d1;
    Vec4 localVec41;
    for (int i1 = 0; i1 < j; i1++)
      for (int i2 = 0; i2 < j; i2++)
      {
        LatLon localLatLon2 = (LatLon)localIterator.next();
        d1 = d2 * arrayOfDouble[(n++)];
        localVec41 = localGlobe.computePointFromPosition(localLatLon2.getLatitude(), localLatLon2.getLongitude(), d1);
        localDoubleBuffer1.put(m++, localVec41.x - localVec42.x).put(m++, localVec41.y - localVec42.y).put(m++, localVec41.z - localVec42.z);
      }
    IntBuffer localIntBuffer = getIndices(i);
    DoubleBuffer localDoubleBuffer2 = getNormals(i, localDoubleBuffer1, localIntBuffer, localVec42);
    double d3 = paramBoolean ? Math.abs(localGlobe.getMinElevation() * d2) : 0.0D;
    Angle localAngle7 = localAngle2;
    for (int i3 = 0; i3 < j; i3++)
    {
      n = i3 * j + 1;
      d1 = d2 * arrayOfDouble[n];
      d1 -= (d3 >= 0.0D ? d3 : -d3);
      localVec41 = localGlobe.computePointFromPosition(localAngle7, localAngle5, d1);
      m = i3 * j * 3;
      localDoubleBuffer1.put(m++, localVec41.x - localVec42.x).put(m++, localVec41.y - localVec42.y).put(m++, localVec41.z - localVec42.z);
      n += j - 2;
      d1 = d2 * arrayOfDouble[n];
      d1 -= (d3 >= 0.0D ? d3 : -d3);
      localVec41 = localGlobe.computePointFromPosition(localAngle7, localAngle6, d1);
      m = ((i3 + 1) * j - 1) * 3;
      localDoubleBuffer1.put(m++, localVec41.x - localVec42.x).put(m++, localVec41.y - localVec42.y).put(m++, localVec41.z - localVec42.z);
      if (i3 > i)
      {
        localAngle7 = localAngle3;
      }
      else
      {
        if (i3 == 0)
          continue;
        localAngle7 = localAngle7.add(localAngle1);
      }
    }
    Angle localAngle8 = localAngle5;
    for (int i4 = 0; i4 < j; i4++)
    {
      n = i4 + j;
      d1 = d2 * arrayOfDouble[n];
      d1 -= (d3 >= 0.0D ? d3 : -d3);
      localVec41 = localGlobe.computePointFromPosition(localAngle2, localAngle8, d1);
      m = i4 * 3;
      localDoubleBuffer1.put(m++, localVec41.x - localVec42.x).put(m++, localVec41.y - localVec42.y).put(m++, localVec41.z - localVec42.z);
      n += (j - 2) * j;
      d1 = d2 * arrayOfDouble[n];
      d1 -= (d3 >= 0.0D ? d3 : -d3);
      localVec41 = localGlobe.computePointFromPosition(localAngle3, localAngle8, d1);
      m = (j * (j - 1) + i4) * 3;
      localDoubleBuffer1.put(m++, localVec41.x - localVec42.x).put(m++, localVec41.y - localVec42.y).put(m++, localVec41.z - localVec42.z);
      if (i4 > i)
      {
        localAngle8 = localAngle6;
      }
      else
      {
        if (i4 == 0)
          continue;
        localAngle8 = localAngle8.add(localAngle4);
      }
    }
    return new RenderInfo(i, localDoubleBuffer1, getTextureCoordinates(i), localDoubleBuffer2, localVec42);
  }

  private ArrayList<LatLon> computeLocations(RectTile paramRectTile)
  {
    int i = paramRectTile.density;
    int j = (i + 3) * (i + 3);
    Angle localAngle1 = paramRectTile.sector.getMaxLatitude();
    Angle localAngle2 = paramRectTile.sector.getDeltaLat().divide(i);
    Angle localAngle3 = paramRectTile.sector.getMinLatitude().subtract(localAngle2);
    Angle localAngle4 = paramRectTile.sector.getMinLongitude();
    Angle localAngle5 = paramRectTile.sector.getMaxLongitude();
    Angle localAngle6 = paramRectTile.sector.getDeltaLon().divide(i);
    ArrayList localArrayList = new ArrayList(j);
    for (int k = 0; k <= i + 2; k++)
    {
      Angle localAngle7 = localAngle4.subtract(localAngle6);
      for (int m = 0; m <= i + 2; m++)
      {
        localArrayList.add(new LatLon(localAngle3, localAngle7));
        localAngle7 = localAngle7.add(localAngle6);
        if (localAngle7.degrees < -180.0D)
        {
          localAngle7 = Angle.NEG180;
        }
        else
        {
          if (localAngle7.degrees <= 180.0D)
            continue;
          localAngle7 = Angle.POS180;
        }
      }
      localAngle3 = localAngle3.add(localAngle2);
    }
    return localArrayList;
  }

  private void renderMultiTexture(DrawContext paramDrawContext, RectTile paramRectTile, int paramInt)
  {
    String str;
    if (paramDrawContext == null)
    {
      str = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (paramInt < 1)
    {
      str = Logging.getMessage("generic.NumTextureUnitsLessThanOne");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    render(paramDrawContext, paramRectTile, paramInt);
  }

  private void render(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    if (paramDrawContext == null)
    {
      String str = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    render(paramDrawContext, paramRectTile, 1);
  }

  private long render(DrawContext paramDrawContext, RectTile paramRectTile, int paramInt)
  {
    if (paramRectTile.ri == null)
    {
            String localObject1 = Logging.getMessage("nullValue.RenderInfoIsNull");
      Logging.logger().severe((String)localObject1);
      throw new IllegalStateException((String)localObject1);
    }
    paramDrawContext.getView().pushReferenceCenter(paramDrawContext, RectTile.access$1500(paramRectTile).referenceCenter);
    if ((!paramDrawContext.isPickingMode()) && (this.lightDirection != null))
      beginLighting(paramDrawContext);
    Object localObject1 = paramDrawContext.getGL();
    ((GL)localObject1).glPushClientAttrib(2);
    ((GL)localObject1).glEnableClientState(32884);
    ((GL)localObject1).glVertexPointer(3, 5130, 0, RectTile.access$1500(paramRectTile).vertices.rewind());
    ((GL)localObject1).glEnableClientState(32885);
    ((GL)localObject1).glNormalPointer(5130, 0, RectTile.access$1500(paramRectTile).normals.rewind());
    for (int i = 0; i < paramInt; i++)
    {
      ((GL)localObject1).glClientActiveTexture(33984 + i);
      ((GL)localObject1).glEnableClientState(32888);
      Object localObject2 = paramDrawContext.getValue("gov.nasa.worldwind.avkey.TextureCoordinates");
      if ((localObject2 != null) && ((localObject2 instanceof DoubleBuffer)))
        ((GL)localObject1).glTexCoordPointer(2, 5130, 0, ((DoubleBuffer)localObject2).rewind());
      else
        ((GL)localObject1).glTexCoordPointer(2, 5130, 0, RectTile.access$1500(paramRectTile).texCoords.rewind());
    }
    ((GL)localObject1).glDrawElements(5, RectTile.access$1500(paramRectTile).indices.limit(), 5125, RectTile.access$1500(paramRectTile).indices.rewind());
    ((GL)localObject1).glDisableClientState(32885);
    ((GL)localObject1).glPopClientAttrib();
    if ((!paramDrawContext.isPickingMode()) && (this.lightDirection != null))
      endLighting(paramDrawContext);
    paramDrawContext.getView().popReferenceCenter(paramDrawContext);
    return RectTile.access$1500(paramRectTile).indices.limit() - 2;
  }

  private void beginLighting(DrawContext paramDrawContext)
  {
    GL localGL = paramDrawContext.getGL();
    localGL.glPushAttrib(8257);
    this.material.apply(localGL, 1028);
    localGL.glDisable(2903);
    float[] arrayOfFloat1 = { (float)(-this.lightDirection.x), (float)(-this.lightDirection.y), (float)(-this.lightDirection.z), 0.0F };
    float[] arrayOfFloat2 = new float[4];
    float[] arrayOfFloat3 = new float[4];
    this.lightColor.getRGBComponents(arrayOfFloat2);
    this.ambientColor.getRGBComponents(arrayOfFloat3);
    localGL.glLightfv(16385, 4611, arrayOfFloat1, 0);
    localGL.glLightfv(16385, 4609, arrayOfFloat2, 0);
    localGL.glLightfv(16385, 4608, arrayOfFloat3, 0);
    localGL.glDisable(16384);
    localGL.glEnable(16385);
    localGL.glEnable(2896);
  }

  private void endLighting(DrawContext paramDrawContext)
  {
    GL localGL = paramDrawContext.getGL();
    localGL.glDisable(16385);
    localGL.glEnable(16384);
    localGL.glDisable(2896);
    localGL.glPopAttrib();
  }

  private void renderWireframe(DrawContext paramDrawContext, RectTile paramRectTile, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramDrawContext == null)
    {
      String localObject = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramRectTile.ri == null)
    {
      String localObject = Logging.getMessage("nullValue.RenderInfoIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalStateException((String)localObject);
    }
    Object localObject = getIndices(RectTile.access$1500(paramRectTile).density);
    ((IntBuffer)localObject).rewind();
    paramDrawContext.getView().pushReferenceCenter(paramDrawContext, RectTile.access$1500(paramRectTile).referenceCenter);
    GL localGL = paramDrawContext.getGL();
    localGL.glPushAttrib(270601);
    localGL.glEnable(3042);
    localGL.glBlendFunc(770, 1);
    localGL.glDisable(2929);
    localGL.glEnable(2884);
    localGL.glCullFace(1029);
    localGL.glDisable(3553);
    localGL.glColor4d(1.0D, 1.0D, 1.0D, 0.2D);
    localGL.glPolygonMode(1028, 6913);
    if (paramBoolean1)
    {
      localGL.glPushClientAttrib(2);
      localGL.glEnableClientState(32884);
      localGL.glVertexPointer(3, 5130, 0, RectTile.access$1500(paramRectTile).vertices);
      localGL.glDrawElements(5, ((IntBuffer)localObject).limit(), 5125, (Buffer)localObject);
      localGL.glPopClientAttrib();
    }
    paramDrawContext.getView().popReferenceCenter(paramDrawContext);
    if (paramBoolean2)
      renderPatchBoundary(paramDrawContext, paramRectTile, localGL);
    localGL.glPopAttrib();
  }

  private void renderPatchBoundary(DrawContext paramDrawContext, RectTile paramRectTile, GL paramGL)
  {
    paramGL.glColor4d(1.0D, 0.0D, 0.0D, 1.0D);
    Vec4[] arrayOfVec4 = paramRectTile.sector.computeCornerPoints(paramDrawContext.getGlobe(), paramDrawContext.getVerticalExaggeration());
    paramGL.glBegin(7);
    paramGL.glVertex3d(arrayOfVec4[0].x, arrayOfVec4[0].y, arrayOfVec4[0].z);
    paramGL.glVertex3d(arrayOfVec4[1].x, arrayOfVec4[1].y, arrayOfVec4[1].z);
    paramGL.glVertex3d(arrayOfVec4[2].x, arrayOfVec4[2].y, arrayOfVec4[2].z);
    paramGL.glVertex3d(arrayOfVec4[3].x, arrayOfVec4[3].y, arrayOfVec4[3].z);
    paramGL.glEnd();
  }

  private void renderBoundingVolume(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    Extent localExtent = paramRectTile.getExtent();
    if (localExtent == null)
      return;
    if ((localExtent instanceof Cylinder))
      ((Cylinder)localExtent).render(paramDrawContext);
  }

  private PickedObject[] pick(DrawContext paramDrawContext, RectTile paramRectTile, List<? extends Point> paramList)
  {
    if (paramDrawContext == null)
    {
      String localObject = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramList.size() == 0)
      return null;
    if (paramRectTile.ri == null)
      return null;
    Object localObject = new PickedObject[paramList.size()];
    renderTrianglesWithUniqueColors(paramDrawContext, paramRectTile);
    for (int i = 0; i < paramList.size(); i++)
        
    localObject[i] = resolvePick(paramDrawContext, paramRectTile, (Point)paramList.get(i));
    return (PickedObject)localObject;
  }

  private void pick(DrawContext paramDrawContext, RectTile paramRectTile, Point paramPoint)
  {
    if (paramDrawContext == null)
    {
      String localObject = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramRectTile.ri == null)
      return;
    renderTrianglesWithUniqueColors(paramDrawContext, paramRectTile);
    Object localObject = resolvePick(paramDrawContext, paramRectTile, paramPoint);
    if (localObject != null)
      paramDrawContext.addPickedObject((PickedObject)localObject);
  }

  private void renderTrianglesWithUniqueColors(DrawContext paramDrawContext, RectTile paramRectTile)
  {
    if (paramDrawContext == null)
    {
      String localObject = Logging.getMessage("nullValue.DrawContextIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalStateException((String)localObject);
    }
    if (RectTile.access$1500(paramRectTile).vertices == null)
      return;
    RectTile.access$1500(paramRectTile).vertices.rewind();
    RectTile.access$1500(paramRectTile).indices.rewind();
    Object localObject = paramDrawContext.getGL();
    if (null != RectTile.access$1500(paramRectTile).referenceCenter)
      paramDrawContext.getView().pushReferenceCenter(paramDrawContext, RectTile.access$1500(paramRectTile).referenceCenter);
    RectTile.access$2502(paramRectTile, paramDrawContext.getUniquePickColor().getRGB());
    int i = RectTile.access$1500(paramRectTile).indices.capacity() - 2;
    ((GL)localObject).glBegin(4);
    for (int j = 0; j < i; j++)
    {
      Color localColor = paramDrawContext.getUniquePickColor();
      ((GL)localObject).glColor3ub((byte)(localColor.getRed() & 0xFF), (byte)(localColor.getGreen() & 0xFF), (byte)(localColor.getBlue() & 0xFF));
      int k = 3 * RectTile.access$1500(paramRectTile).indices.get(j);
      ((GL)localObject).glVertex3d(RectTile.access$1500(paramRectTile).vertices.get(k), RectTile.access$1500(paramRectTile).vertices.get(k + 1), RectTile.access$1500(paramRectTile).vertices.get(k + 2));
      k = 3 * RectTile.access$1500(paramRectTile).indices.get(j + 1);
      ((GL)localObject).glVertex3d(RectTile.access$1500(paramRectTile).vertices.get(k), RectTile.access$1500(paramRectTile).vertices.get(k + 1), RectTile.access$1500(paramRectTile).vertices.get(k + 2));
      k = 3 * RectTile.access$1500(paramRectTile).indices.get(j + 2);
      ((GL)localObject).glVertex3d(RectTile.access$1500(paramRectTile).vertices.get(k), RectTile.access$1500(paramRectTile).vertices.get(k + 1), RectTile.access$1500(paramRectTile).vertices.get(k + 2));
    }
    ((GL)localObject).glEnd();
    RectTile.access$2602(paramRectTile, paramDrawContext.getUniquePickColor().getRGB());
    if (null != RectTile.access$1500(paramRectTile).referenceCenter)
      paramDrawContext.getView().popReferenceCenter(paramDrawContext);
  }

  private PickedObject resolvePick(DrawContext paramDrawContext, RectTile paramRectTile, Point paramPoint)
  {
    int i = this.pickSupport.getTopColor(paramDrawContext, paramPoint);
    if ((i < paramRectTile.minColorCode) || (i > paramRectTile.maxColorCode))
      return null;
    double d1 = 9.999999747378752E-006D;
    int j = i - paramRectTile.minColorCode - 1;
    if ((RectTile.access$1500(paramRectTile).indices == null) || (j >= RectTile.access$1500(paramRectTile).indices.capacity() - 2))
      return null;
    double d2 = RectTile.access$1500(paramRectTile).referenceCenter.x;
    double d3 = RectTile.access$1500(paramRectTile).referenceCenter.y;
    double d4 = RectTile.access$1500(paramRectTile).referenceCenter.z;
    int k = 3 * RectTile.access$1500(paramRectTile).indices.get(j);
    Vec4 localVec41 = new Vec4(RectTile.access$1500(paramRectTile).vertices.get(k++) + d2, RectTile.access$1500(paramRectTile).vertices.get(k++) + d3, RectTile.access$1500(paramRectTile).vertices.get(k) + d4);
    k = 3 * RectTile.access$1500(paramRectTile).indices.get(j + 1);
    Vec4 localVec42 = new Vec4(RectTile.access$1500(paramRectTile).vertices.get(k++) + d2, RectTile.access$1500(paramRectTile).vertices.get(k++) + d3, RectTile.access$1500(paramRectTile).vertices.get(k) + d4);
    k = 3 * RectTile.access$1500(paramRectTile).indices.get(j + 2);
    Vec4 localVec43 = new Vec4(RectTile.access$1500(paramRectTile).vertices.get(k++) + d2, RectTile.access$1500(paramRectTile).vertices.get(k++) + d3, RectTile.access$1500(paramRectTile).vertices.get(k) + d4);
    Vec4 localVec44 = localVec42.subtract3(localVec41);
    Vec4 localVec45 = localVec43.subtract3(localVec41);
    Vec4 localVec46 = localVec44.cross3(localVec45);
    Line localLine = paramDrawContext.getView().computeRayFromScreenPoint(paramPoint.getX(), paramPoint.getY());
    Vec4 localVec47 = localLine.getOrigin().subtract3(localVec41);
    double d5 = -localVec46.dot3(localVec47);
    double d6 = localVec46.dot3(localLine.getDirection());
    if (Math.abs(d6) < d1)
      return null;
    double d7 = d5 / d6;
    Vec4 localVec48 = localLine.getOrigin().add3(localLine.getDirection().multiply3(d7));
    Position localPosition1 = paramDrawContext.getGlobe().computePositionFromPoint(localVec48);
    double d8 = paramDrawContext.getGlobe().getElevation(localPosition1.getLatitude(), localPosition1.getLongitude());
    Position localPosition2 = new Position(localPosition1.getLatitude(), localPosition1.getLongitude(), d8);
    return new PickedObject(paramPoint, i, localPosition2, localPosition1.getLatitude(), localPosition1.getLongitude(), d8, true);
  }

  private Intersection[] intersect(RectTile paramRectTile, Line paramLine)
  {
    if (paramLine == null)
    {
      String localObject = Logging.getMessage("nullValue.LineIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (RectTile.access$1500(paramRectTile).vertices == null)
      return null;
    Object localObject = paramLine.getDirection().cross3(this.globe.computeSurfaceNormalAtPoint(paramLine.getOrigin()));
    Plane localPlane1 = new Plane(((Vec4)localObject).x(), ((Vec4)localObject).y(), ((Vec4)localObject).z(), -paramLine.getOrigin().dot3((Vec4)localObject));
    if (!paramRectTile.getExtent().intersects(localPlane1))
      return null;
    Vec4 localVec41 = paramLine.getDirection().cross3((Vec4)localObject);
    Plane localPlane2 = new Plane(localVec41.x(), localVec41.y(), localVec41.z(), -paramLine.getOrigin().dot3(localVec41));
    if (!paramRectTile.getExtent().intersects(localPlane2))
      return null;
    ArrayList localArrayList = new ArrayList();
    int[] arrayOfInt = new int[RectTile.access$1500(paramRectTile).indices.limit()];
    double[] arrayOfDouble = new double[RectTile.access$1500(paramRectTile).vertices.limit()];
    RectTile.access$1500(paramRectTile).indices.rewind();
    RectTile.access$1500(paramRectTile).vertices.rewind();
    RectTile.access$1500(paramRectTile).indices.get(arrayOfInt, 0, arrayOfInt.length);
    RectTile.access$1500(paramRectTile).vertices.get(arrayOfDouble, 0, arrayOfDouble.length);
    RectTile.access$1500(paramRectTile).indices.rewind();
    RectTile.access$1500(paramRectTile).vertices.rewind();
    int i = RectTile.access$1500(paramRectTile).indices.capacity() - 2;
    double d1 = RectTile.access$1500(paramRectTile).referenceCenter.x;
    double d2 = RectTile.access$1500(paramRectTile).referenceCenter.y;
    double d3 = RectTile.access$1500(paramRectTile).referenceCenter.z;
    double d4 = paramRectTile.getSector().getDeltaLatRadians() * this.globe.getRadius() / this.density;
    double d5 = Math.sqrt(d4 * d4 * 2.0D) / 2.0D;
    double d6 = ((Cylinder)paramRectTile.getExtent()).getCylinderHeight();
    int j = (this.density + 2) * 2 + 6;
    int k = i - j;
    int m = -1;
    for (int n = j; n < k; n += 2)
    {
      m = m == this.density - 1 ? -4 : m + 1;
      if (m < 0)
        continue;
      int i1 = 3 * arrayOfInt[(n + 1)];
      Vec4 localVec43 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      i1 = 3 * arrayOfInt[(n + 2)];
      Vec4 localVec44 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      Vec4 localVec45 = Vec4.mix3(0.5D, localVec43, localVec44);
      if ((Math.abs(localPlane1.distanceTo(localVec45)) > d5) || (Math.abs(localPlane2.distanceTo(localVec45)) > d6))
        continue;
      i1 = 3 * arrayOfInt[n];
      Vec4 localVec47 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      i1 = 3 * arrayOfInt[(n + 3)];
      Vec4 localVec48 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      Triangle localTriangle = new Triangle(localVec47, localVec43, localVec44);
      Vec4 localVec46;
      if ((localVec46 = localTriangle.intersect(paramLine)) != null)
        localArrayList.add(new Intersection(localVec46, false));
      localTriangle = new Triangle(localVec43, localVec44, localVec48);
      if ((localVec46 = localTriangle.intersect(paramLine)) == null)
        continue;
      localArrayList.add(new Intersection(localVec46, false));
    }
    n = localArrayList.size();
    if (n == 0)
      return null;
    Intersection[] arrayOfIntersection = new Intersection[n];
    localArrayList.toArray(arrayOfIntersection);
    Vec4 localVec42 = paramLine.getOrigin();
    Arrays.sort(arrayOfIntersection, new Comparator(localVec42)
    {
      public int compare(Intersection paramIntersection1, Intersection paramIntersection2)
      {
        if ((paramIntersection1 == null) && (paramIntersection2 == null))
          return 0;
        if (paramIntersection2 == null)
          return -1;
        if (paramIntersection1 == null)
          return 1;
        Vec4 localVec41 = paramIntersection1.getIntersectionPoint();
        Vec4 localVec42 = paramIntersection2.getIntersectionPoint();
        double d1 = this.val$origin.distanceTo3(localVec41);
        double d2 = this.val$origin.distanceTo3(localVec42);
        return Double.compare(d1, d2);
      }
    });
    return (Intersection)arrayOfIntersection;
  }

  private Intersection[] intersect(RectTile paramRectTile, double paramDouble)
  {
    if (RectTile.access$1500(paramRectTile).vertices == null)
      return null;
    Cylinder localCylinder = (Cylinder)paramRectTile.getExtent();
    if (!(this.globe.isPointAboveElevation(localCylinder.getBottomCenter(), paramDouble) ^ this.globe.isPointAboveElevation(localCylinder.getTopCenter(), paramDouble)))
      return null;
    ArrayList localArrayList = new ArrayList();
    int[] arrayOfInt = new int[RectTile.access$1500(paramRectTile).indices.limit()];
    double[] arrayOfDouble = new double[RectTile.access$1500(paramRectTile).vertices.limit()];
    RectTile.access$1500(paramRectTile).indices.rewind();
    RectTile.access$1500(paramRectTile).vertices.rewind();
    RectTile.access$1500(paramRectTile).indices.get(arrayOfInt, 0, arrayOfInt.length);
    RectTile.access$1500(paramRectTile).vertices.get(arrayOfDouble, 0, arrayOfDouble.length);
    RectTile.access$1500(paramRectTile).indices.rewind();
    RectTile.access$1500(paramRectTile).vertices.rewind();
    int i = RectTile.access$1500(paramRectTile).indices.capacity() - 2;
    double d1 = RectTile.access$1500(paramRectTile).referenceCenter.x;
    double d2 = RectTile.access$1500(paramRectTile).referenceCenter.y;
    double d3 = RectTile.access$1500(paramRectTile).referenceCenter.z;
    int j = (this.density + 2) * 2 + 6;
    int k = i - j;
    int m = -1;
        int n;
    for (n = j; n < k; n += 2)
    {
      m = m == this.density - 1 ? -4 : m + 1;
      if (m < 0)
        continue;
      int i1 = 3 * arrayOfInt[n];
      Vec4 localVec41 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      i1 = 3 * arrayOfInt[(n + 1)];
      Vec4 localVec42 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      i1 = 3 * arrayOfInt[(n + 2)];
      Vec4 localVec43 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      i1 = 3 * arrayOfInt[(n + 3)];
      Vec4 localVec44 = new Vec4(arrayOfDouble[(i1++)] + d1, arrayOfDouble[(i1++)] + d2, arrayOfDouble[i1] + d3);
      Intersection[] arrayOfIntersection2;
      if ((arrayOfIntersection2 = this.globe.intersect(new Triangle(localVec41, localVec42, localVec43), paramDouble)) != null)
      {
        localArrayList.add(arrayOfIntersection2[0]);
        localArrayList.add(arrayOfIntersection2[1]);
      }
      if ((arrayOfIntersection2 = this.globe.intersect(new Triangle(localVec42, localVec43, localVec44), paramDouble)) == null)
        continue;
      localArrayList.add(arrayOfIntersection2[0]);
      localArrayList.add(arrayOfIntersection2[1]);
    }
    n = localArrayList.size();
    if (n == 0)
      return null;
    Intersection[] arrayOfIntersection1 = new Intersection[n];
    localArrayList.toArray(arrayOfIntersection1);
    return arrayOfIntersection1;
  }

  private Vec4 getSurfacePoint(RectTile paramRectTile, Angle paramAngle1, Angle paramAngle2, double paramDouble)
  {
    Vec4 localVec4 = getSurfacePoint(paramRectTile, paramAngle1, paramAngle2);
    if ((paramDouble != 0.0D) && (localVec4 != null))
      localVec4 = applyOffset(this.globe, localVec4, paramDouble);
    return localVec4;
  }

  private static Vec4 applyOffset(Globe paramGlobe, Vec4 paramVec4, double paramDouble)
  {
    Vec4 localVec4 = paramGlobe.computeSurfaceNormalAtPoint(paramVec4);
    paramVec4 = Vec4.fromLine3(paramVec4, paramDouble, localVec4);
    return paramVec4;
  }

  private Vec4 getSurfacePoint(RectTile paramRectTile, Angle paramAngle1, Angle paramAngle2)
  {
    if ((paramAngle1 == null) || (paramAngle2 == null))
    {
      String str = Logging.getMessage("nullValue.LatLonIsNull");
      Logging.logger().severe(str);
      throw new IllegalArgumentException(str);
    }
    if (!paramRectTile.sector.contains(paramAngle1, paramAngle2))
      return null;
    if (paramRectTile.ri == null)
      return null;
    double d1 = paramAngle1.getDegrees();
    double d2 = paramAngle2.getDegrees();
    double d3 = paramRectTile.sector.getMinLatitude().getDegrees();
    double d4 = paramRectTile.sector.getMaxLatitude().getDegrees();
    double d5 = paramRectTile.sector.getMinLongitude().getDegrees();
    double d6 = paramRectTile.sector.getMaxLongitude().getDegrees();
    double d7 = (d2 - d5) / (d6 - d5);
    double d8 = (d1 - d3) / (d4 - d3);
    int i = (int)(d8 * paramRectTile.density);
    int j = (int)(d7 * paramRectTile.density);
    double d9 = createPosition(j, d7, RectTile.access$1500(paramRectTile).density);
    double d10 = createPosition(i, d8, RectTile.access$1500(paramRectTile).density);
    Vec4 localVec4 = interpolate(i, j, d9, d10, paramRectTile.ri);
    localVec4 = localVec4.add3(RectTile.access$1500(paramRectTile).referenceCenter);
    return localVec4;
  }

  private static double createPosition(int paramInt1, double paramDouble, int paramInt2)
  {
    double d1 = paramInt1 / paramInt2;
    double d2 = (paramInt1 + 1) / paramInt2;
    return (paramDouble - d1) / (d2 - d1);
  }

  private static Vec4 interpolate(int paramInt1, int paramInt2, double paramDouble1, double paramDouble2, RenderInfo paramRenderInfo)
  {
    paramInt1++;
    paramInt2++;
    int i = paramRenderInfo.density + 3;
    int j = paramInt1 * i + paramInt2;
    j *= 3;
    int k = i * 3;
    Vec4 localVec41 = new Vec4(paramRenderInfo.vertices.get(j), paramRenderInfo.vertices.get(j + 1), paramRenderInfo.vertices.get(j + 2));
    Vec4 localVec42 = new Vec4(paramRenderInfo.vertices.get(j + 3), paramRenderInfo.vertices.get(j + 4), paramRenderInfo.vertices.get(j + 5));
    j += k;
    Vec4 localVec43 = new Vec4(paramRenderInfo.vertices.get(j), paramRenderInfo.vertices.get(j + 1), paramRenderInfo.vertices.get(j + 2));
    Vec4 localVec44 = new Vec4(paramRenderInfo.vertices.get(j + 3), paramRenderInfo.vertices.get(j + 4), paramRenderInfo.vertices.get(j + 5));
    return interpolate(localVec41, localVec42, localVec44, localVec43, paramDouble1, paramDouble2);
  }

  private static Vec4 interpolate(Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43, Vec4 paramVec44, double paramDouble1, double paramDouble2)
  {
    double d = paramDouble1 + paramDouble2;
    if (d == 1.0D)
      return new Vec4(paramVec44.x * paramDouble2 + paramVec42.x * paramDouble1, paramVec44.y * paramDouble2 + paramVec42.y * paramDouble1, paramVec44.z * paramDouble2 + paramVec42.z * paramDouble1);
    if (d > 1.0D)
    {
      Vec4 localVec41 = paramVec44.subtract3(paramVec43).multiply3(1.0D - paramDouble1);
      Vec4 localVec42 = paramVec42.subtract3(paramVec43).multiply3(1.0D - paramDouble2);
      return paramVec43.add3(localVec41).add3(localVec42);
    }
    Vec4 localVec41 = paramVec42.subtract3(paramVec41).multiply3(paramDouble1);
    Vec4 localVec42 = paramVec44.subtract3(paramVec41).multiply3(paramDouble2);
    return paramVec41.add3(localVec41).add3(localVec42);
  }

  private static double[] baryCentricCoordsRequireInside(Vec4 paramVec4, Vec4[] paramArrayOfVec4)
  {
    double[] arrayOfDouble = new double[3];
    double d1 = distanceFromLine(paramArrayOfVec4[0], paramArrayOfVec4[1], paramArrayOfVec4[2].subtract3(paramArrayOfVec4[1]));
    double d2 = distanceFromLine(paramVec4, paramArrayOfVec4[1], paramArrayOfVec4[2].subtract3(paramArrayOfVec4[1]));
    arrayOfDouble[0] = (d2 / d1);
    if (Math.abs(arrayOfDouble[0]) < 0.0001D)
      arrayOfDouble[0] = 0.0D;
    else if (Math.abs(1.0D - arrayOfDouble[0]) < 0.0001D)
      arrayOfDouble[0] = 1.0D;
    if ((arrayOfDouble[0] < 0.0D) || (arrayOfDouble[0] > 1.0D))
      return null;
    d1 = distanceFromLine(paramArrayOfVec4[1], paramArrayOfVec4[0], paramArrayOfVec4[2].subtract3(paramArrayOfVec4[0]));
    d2 = distanceFromLine(paramVec4, paramArrayOfVec4[0], paramArrayOfVec4[2].subtract3(paramArrayOfVec4[0]));
    arrayOfDouble[1] = (d2 / d1);
    if (Math.abs(arrayOfDouble[1]) < 0.0001D)
      arrayOfDouble[1] = 0.0D;
    else if (Math.abs(1.0D - arrayOfDouble[1]) < 0.0001D)
      arrayOfDouble[1] = 1.0D;
    if ((arrayOfDouble[1] < 0.0D) || (arrayOfDouble[1] > 1.0D))
      return null;
    arrayOfDouble[2] = (1.0D - arrayOfDouble[0] - arrayOfDouble[1]);
    if (Math.abs(arrayOfDouble[2]) < 0.0001D)
      arrayOfDouble[2] = 0.0D;
    else if (Math.abs(1.0D - arrayOfDouble[2]) < 0.0001D)
      arrayOfDouble[2] = 1.0D;
    if (arrayOfDouble[2] < 0.0D)
      return null;
    return arrayOfDouble;
  }

  private static double distanceFromLine(Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43)
  {
    Vec4 localVec4 = paramVec41.subtract3(paramVec42);
    double d1 = localVec4.dot3(localVec4);
    double d2 = paramVec43.normalize3().dot3(localVec4);
    d2 *= d2;
    double d3 = d1 - d2;
    if (d3 < 0.0D)
      return 0.0D;
    return Math.sqrt(d3);
  }

  protected DoubleBuffer makeGeographicTexCoords(SectorGeometry paramSectorGeometry, SectorGeometry.GeographicTextureCoordinateComputer paramGeographicTextureCoordinateComputer)
  {
    if (paramSectorGeometry == null)
    {
      String localObject = Logging.getMessage("nullValue.SectorGeometryIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    if (paramGeographicTextureCoordinateComputer == null)
    {
      String localObject = Logging.getMessage("nullValue.TextureCoordinateComputerIsNull");
      Logging.logger().severe((String)localObject);
      throw new IllegalArgumentException((String)localObject);
    }
    Object localObject = (RectTile)paramSectorGeometry;
    int i = ((RectTile)localObject).density;
    if (i < 1)
      i = 1;
    int j = (i + 3) * (i + 3);
    DoubleBuffer localDoubleBuffer = BufferUtil.newDoubleBuffer(2 * j);
    double d1 = ((RectTile)localObject).sector.getDeltaLatRadians() / i;
    double d2 = ((RectTile)localObject).sector.getDeltaLonRadians() / i;
    Angle localAngle1 = ((RectTile)localObject).sector.getMinLatitude();
    Angle localAngle2 = ((RectTile)localObject).sector.getMaxLatitude();
    Angle localAngle3 = ((RectTile)localObject).sector.getMinLongitude();
    Angle localAngle4 = ((RectTile)localObject).sector.getMaxLongitude();
    int k = 2 * (i + 3);
    Angle localAngle5;
    for (int m = 0; m < i; m++)
    {
      localAngle5 = Angle.fromRadians(localAngle1.radians + m * d1);
      double[] arrayOfDouble = paramGeographicTextureCoordinateComputer.compute(localAngle5, localAngle3);
      localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
      for (int i1 = 0; i1 < i; i1++)
      {
        Angle localAngle6 = Angle.fromRadians(localAngle3.radians + i1 * d2);
        arrayOfDouble = paramGeographicTextureCoordinateComputer.compute(localAngle5, localAngle6);
        localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
      }
      arrayOfDouble = paramGeographicTextureCoordinateComputer.compute(localAngle5, localAngle4);
      localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
      localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
    }
    double[] arrayOfDouble = paramGeographicTextureCoordinateComputer.compute(localAngle2, localAngle3);
    localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
    for (int m = 0; m < i; m++)
    {
      localAngle5 = Angle.fromRadians(localAngle3.radians + m * d2);
      arrayOfDouble = paramGeographicTextureCoordinateComputer.compute(localAngle2, localAngle5);
      localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
    }
    arrayOfDouble = paramGeographicTextureCoordinateComputer.compute(localAngle2, localAngle4);
    localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
    localDoubleBuffer.put(k++, arrayOfDouble[0]).put(k++, arrayOfDouble[1]);
    int m = k - 2 * (i + 3);
    for (int n = 0; n < i + 3; n++)
    {
      localDoubleBuffer.put(k++, localDoubleBuffer.get(m++));
      localDoubleBuffer.put(k++, localDoubleBuffer.get(m++));
    }
    k = 0;
    m = 2 * (i + 3);
    for (int n = 0; n < i + 3; n++)
    {
      localDoubleBuffer.put(k++, localDoubleBuffer.get(m++));
      localDoubleBuffer.put(k++, localDoubleBuffer.get(m++));
    }
    return (DoubleBuffer)localDoubleBuffer;
  }

  private static DoubleBuffer getTextureCoordinates(int paramInt)
  {
    if (paramInt < 1)
      paramInt = 1;
    DoubleBuffer localDoubleBuffer = (DoubleBuffer)parameterizations.get(Integer.valueOf(paramInt));
    if (localDoubleBuffer != null)
      return localDoubleBuffer;
    int i = (paramInt + 3) * (paramInt + 3);
    localDoubleBuffer = BufferUtil.newDoubleBuffer(2 * i);
    double d1 = 1.0D / paramInt;
    int j = 2 * (paramInt + 3);
    for (int k = 0; k < paramInt; k++)
    {
      double d3 = k * d1;
      localDoubleBuffer.put(j++, 0.0D);
      localDoubleBuffer.put(j++, d3);
      for (int n = 0; n < paramInt; n++)
      {
        localDoubleBuffer.put(j++, n * d1);
        localDoubleBuffer.put(j++, d3);
      }
      localDoubleBuffer.put(j++, 0.999999D);
      localDoubleBuffer.put(j++, d3);
      localDoubleBuffer.put(j++, 0.999999D);
      localDoubleBuffer.put(j++, d3);
    }
    double d2 = 0.999999D;
    localDoubleBuffer.put(j++, 0.0D);
    localDoubleBuffer.put(j++, d2);
    for (int m = 0; m < paramInt; m++)
    {
      localDoubleBuffer.put(j++, m * d1);
      localDoubleBuffer.put(j++, d2);
    }
    localDoubleBuffer.put(j++, 0.999999D);
    localDoubleBuffer.put(j++, d2);
    localDoubleBuffer.put(j++, 0.999999D);
    localDoubleBuffer.put(j++, d2);
    int m = j - 2 * (paramInt + 3);
    for (int n = 0; n < paramInt + 3; n++)
    {
      localDoubleBuffer.put(j++, localDoubleBuffer.get(m++));
      localDoubleBuffer.put(j++, localDoubleBuffer.get(m++));
    }
    j = 0;
    m = 2 * (paramInt + 3);
    for (int n = 0; n < paramInt + 3; n++)
    {
      localDoubleBuffer.put(j++, localDoubleBuffer.get(m++));
      localDoubleBuffer.put(j++, localDoubleBuffer.get(m++));
    }
    parameterizations.put(Integer.valueOf(paramInt), localDoubleBuffer);
    return localDoubleBuffer;
  }

  protected static IntBuffer getIndices(int paramInt)
  {
    if (paramInt < 1)
      paramInt = 1;
    IntBuffer localIntBuffer = (IntBuffer)indexLists.get(Integer.valueOf(paramInt));
    if (localIntBuffer != null)
      return localIntBuffer;
    int i = paramInt + 2;
    int j = 2 * i * i + 4 * i - 2;
    localIntBuffer = BufferUtil.newIntBuffer(j);
    int k = 0;
    for (int m = 0; m < i; m++)
    {
      localIntBuffer.put(k);
      if (m > 0)
      {
        k++;
        localIntBuffer.put(k);
        localIntBuffer.put(k);
      }
      int n;
      if (m % 2 == 0)
      {
        k++;
        localIntBuffer.put(k);
        for (n = 0; n < i; n++)
        {
          k += i;
          localIntBuffer.put(k);
          k++;
          localIntBuffer.put(k);
        }
      }
      else
      {
        k--;
        localIntBuffer.put(k);
        for (n = 0; n < i; n++)
        {
          k -= i;
          localIntBuffer.put(k);
          k--;
          localIntBuffer.put(k);
        }
      }
    }
    indexLists.put(Integer.valueOf(paramInt), localIntBuffer);
    return localIntBuffer;
  }

  protected static DoubleBuffer getNormalsQuick(int paramInt, DoubleBuffer paramDoubleBuffer, Vec4 paramVec4)
  {
    int i = paramInt + 3;
    int j = i * i;
    DoubleBuffer localDoubleBuffer = BufferUtil.newDoubleBuffer(j * 3);
    int m;
    int n;
    for (int k = 1; k < i - 1; k++)
      for (m = 1; m < i - 1; m++)
      {
        n = k * i + m;
        int i1 = k * i + (m - 1);
        int i2 = k * i + (m + 1);
        int i3 = (k - 1) * i + m;
        int i4 = (k + 1) * i + m;
        Vec4 localVec41 = getVec4(n, paramDoubleBuffer, paramVec4);
        Vec4 localVec42 = getVec4(i1, paramDoubleBuffer, paramVec4);
        Vec4 localVec43 = getVec4(i2, paramDoubleBuffer, paramVec4);
        Vec4 localVec44 = getVec4(i3, paramDoubleBuffer, paramVec4);
        Vec4 localVec45 = getVec4(i4, paramDoubleBuffer, paramVec4);
        Vec4 localVec46 = localVec41.subtract3(localVec42).normalize3().cross3(localVec41.subtract3(localVec44).normalize3());
        Vec4 localVec47 = localVec41.subtract3(localVec43).normalize3().cross3(localVec41.subtract3(localVec45).normalize3());
        Vec4 localVec48 = localVec46.add3(localVec47).normalize3();
        localDoubleBuffer.put(n * 3, localVec48.x).put(n * 3 + 1, localVec48.y).put(n * 3 + 2, localVec48.z);
      }
    for (int k = 1; k < i - 1; k++)
    {
      m = k;
      n = i + k;
      copyNormalInBuffer(n, m, localDoubleBuffer);
      m = i * (i - 1) + k;
      n = i * (i - 2) + k;
      copyNormalInBuffer(n, m, localDoubleBuffer);
    }
    for (int k = 0; k < i; k++)
    {
      m = k * i;
      n = k * i + 1;
      copyNormalInBuffer(n, m, localDoubleBuffer);
      m = k * i + (i - 1);
      n = k * i + (i - 2);
      copyNormalInBuffer(n, m, localDoubleBuffer);
    }
    return localDoubleBuffer;
  }

  private static Vec4 getVec4(int paramInt, DoubleBuffer paramDoubleBuffer, Vec4 paramVec4)
  {
    return new Vec4(paramDoubleBuffer.get(paramInt * 3) + paramVec4.x, paramDoubleBuffer.get(paramInt * 3 + 1) + paramVec4.y, paramDoubleBuffer.get(paramInt * 3 + 2) + paramVec4.z);
  }

  private static void copyNormalInBuffer(int paramInt1, int paramInt2, DoubleBuffer paramDoubleBuffer)
  {
    paramDoubleBuffer.put(paramInt2 * 3, paramDoubleBuffer.get(paramInt1 * 3));
    paramDoubleBuffer.put(paramInt2 * 3 + 1, paramDoubleBuffer.get(paramInt1 * 3 + 1));
    paramDoubleBuffer.put(paramInt2 * 3 + 2, paramDoubleBuffer.get(paramInt1 * 3 + 2));
  }

  protected static DoubleBuffer getNormals(int paramInt, DoubleBuffer paramDoubleBuffer, IntBuffer paramIntBuffer, Vec4 paramVec4)
  {
    int i = paramInt + 3;
    int j = i * i;
    int k = paramIntBuffer.limit() - 2;
    double d1 = paramVec4.x;
    double d2 = paramVec4.y;
    double d3 = paramVec4.z;
    DoubleBuffer localDoubleBuffer = BufferUtil.newDoubleBuffer(j * 3);
    int[] arrayOfInt = new int[j];
    Vec4[] arrayOfVec4 = new Vec4[j];
    for (int m = 0; m < j; m++)
      arrayOfVec4[m] = new Vec4(0.0D);
    int n;
    for (int m = 0; m < k; m++)
    {
      n = paramIntBuffer.get(m);
      int i1 = paramIntBuffer.get(m + 1);
      int i2 = paramIntBuffer.get(m + 2);
      Vec4 localVec41 = new Vec4(paramDoubleBuffer.get(n * 3) + d1, paramDoubleBuffer.get(n * 3 + 1) + d2, paramDoubleBuffer.get(n * 3 + 2) + d3);
      Vec4 localVec42 = new Vec4(paramDoubleBuffer.get(i1 * 3) + d1, paramDoubleBuffer.get(i1 * 3 + 1) + d2, paramDoubleBuffer.get(i1 * 3 + 2) + d3);
      Vec4 localVec43 = new Vec4(paramDoubleBuffer.get(i2 * 3) + d1, paramDoubleBuffer.get(i2 * 3 + 1) + d2, paramDoubleBuffer.get(i2 * 3 + 2) + d3);
      Vec4 localVec44 = localVec42.subtract3(localVec41);
      Vec4 localVec45;
      if (m % 2 == 0)
        localVec45 = localVec43.subtract3(localVec41);
      else
        localVec45 = localVec41.subtract3(localVec43);
      Vec4 localVec46 = localVec44.cross3(localVec45).normalize3();
      if (localVec46.getLength3() <= 0.0D)
        continue;
      arrayOfVec4[n] = arrayOfVec4[n].add3(localVec46);
      arrayOfVec4[i1] = arrayOfVec4[i1].add3(localVec46);
      arrayOfVec4[i2] = arrayOfVec4[i2].add3(localVec46);
      arrayOfInt[n] += 1;
      arrayOfInt[i1] += 1;
      arrayOfInt[i2] += 1;
    }
    for (int m = 0; m < j; m++)
    {
      if (arrayOfInt[m] > 0)
        arrayOfVec4[m] = arrayOfVec4[m].divide3(arrayOfInt[m]).normalize3();
      n = m * 3;
      localDoubleBuffer.put(n++, arrayOfVec4[m].x).put(n++, arrayOfVec4[m].y).put(n, arrayOfVec4[m].z);
    }
    return localDoubleBuffer;
  }

  private SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(RectTile paramRectTile, Plane[] paramArrayOfPlane)
  {
    RectTile.access$1500(paramRectTile).vertices.rewind();
    RectTile.access$1500(paramRectTile).indices.rewind();
    Vec4 localVec4 = RectTile.access$1500(paramRectTile).referenceCenter;
    if (localVec4 == null)
      localVec4 = new Vec4(0.0D);
    int i = RectTile.access$1500(paramRectTile).indices.capacity() - 2;
    int[] arrayOfInt = new int[3];
    double[] arrayOfDouble = new double[3];
    SectorGeometry.ExtractedShapeDescription localExtractedShapeDescription = null;
    for (int j = 0; j < i; j++)
    {
      RectTile.access$1500(paramRectTile).indices.position(j);
      RectTile.access$1500(paramRectTile).indices.get(arrayOfInt);
      if ((arrayOfInt[0] == arrayOfInt[1]) || (arrayOfInt[0] == arrayOfInt[2]) || (arrayOfInt[1] == arrayOfInt[2]))
        continue;
      Vec4[] arrayOfVec4 = new Vec4[3];
      for (int k = 0; k < 3; k++)
      {
        RectTile.access$1500(paramRectTile).vertices.position(3 * arrayOfInt[k]);
        RectTile.access$1500(paramRectTile).vertices.get(arrayOfDouble);
        arrayOfVec4[k] = new Vec4(arrayOfDouble[0] + localVec4.getX(), arrayOfDouble[1] + localVec4.getY(), arrayOfDouble[2] + localVec4.getZ(), 1.0D);
      }
      localExtractedShapeDescription = addClippedPolygon(arrayOfVec4, paramArrayOfPlane, localExtractedShapeDescription);
    }
    return localExtractedShapeDescription;
  }

  private SectorGeometry.ExtractedShapeDescription addClippedPolygon(Vec4[] paramArrayOfVec4, Plane[] paramArrayOfPlane, SectorGeometry.ExtractedShapeDescription paramExtractedShapeDescription)
  {
    if (isSkirt(paramArrayOfVec4))
      return paramExtractedShapeDescription;
    Vec4[] arrayOfVec4 = new Vec4[3];
    System.arraycopy(paramArrayOfVec4, 0, arrayOfVec4, 0, 3);
    for (Plane localPlane : paramArrayOfPlane)
    {
      arrayOfVec4 = doSHPass(localPlane, arrayOfVec4);
      if (arrayOfVec4 == null)
        return paramExtractedShapeDescription;
    }
    if (paramExtractedShapeDescription == null)
      paramExtractedShapeDescription = new SectorGeometry.ExtractedShapeDescription(new ArrayList(), new ArrayList());
    paramExtractedShapeDescription.interiorPolys.add(arrayOfVec4);
    addBoundaryEdges(arrayOfVec4, paramArrayOfVec4, paramExtractedShapeDescription.shapeOutline);
    return paramExtractedShapeDescription;
  }

  private boolean isSkirt(Vec4[] paramArrayOfVec4)
  {
    Vec4 localVec41 = this.globe.computeSurfaceNormalAtPoint(paramArrayOfVec4[0]);
    double d = Math.max(Math.abs(paramArrayOfVec4[0].x), Math.abs(paramArrayOfVec4[0].y));
    d = Math.max(d, Math.abs(paramArrayOfVec4[0].z));
    Vec4 localVec42 = paramArrayOfVec4[0].divide3(d);
    Vec4 localVec43 = paramArrayOfVec4[1].divide3(d).subtract3(localVec42);
    Vec4 localVec44 = paramArrayOfVec4[(paramArrayOfVec4.length - 1)].divide3(d).subtract3(localVec42);
    Vec4 localVec45 = localVec43.cross3(localVec44).normalize3();
    return Math.abs(localVec45.dot3(localVec41)) < 0.0001D;
  }

  private Vec4[] doSHPass(Plane paramPlane, Vec4[] paramArrayOfVec4)
  {
    ArrayList localArrayList = new ArrayList();
    Object localObject = paramArrayOfVec4[0];
    int i = paramPlane.dot((Vec4)localObject) <= 0.0D ? 1 : 0;
    for (int j = 1; j <= paramArrayOfVec4.length; j++)
    {
      if (i != 0)
        localArrayList.add(localObject);
      Vec4 localVec4 = paramArrayOfVec4[(j % paramArrayOfVec4.length)];
      int k = paramPlane.dot(localVec4) <= 0.0D ? 1 : 0;
      if (i != k)
      {
        Vec4[] arrayOfVec42;
        if (i != 0)
          arrayOfVec42 = paramPlane.clip((Vec4)localObject, localVec4);
        else
          arrayOfVec42 = paramPlane.clip(localVec4, (Vec4)localObject);
        localArrayList.add(arrayOfVec42[0]);
      }
      localObject = localVec4;
      i = k;
    }
    if (localArrayList.size() == 0)
      return null;
    Vec4[] arrayOfVec41 = new Vec4[localArrayList.size()];
    return (Vec4[])localArrayList.toArray(arrayOfVec41);
  }

  private void addBoundaryEdges(Vec4[] paramArrayOfVec41, Vec4[] paramArrayOfVec42, ArrayList<SectorGeometry.BoundaryEdge> paramArrayList)
  {
    for (int i = 0; i < paramArrayOfVec41.length; i++)
    {
      int j = (i + 1) % paramArrayOfVec41.length;
      if (edgeOnTriangle(paramArrayOfVec41[i], paramArrayOfVec41[j], paramArrayOfVec42))
        continue;
      paramArrayList.add(new SectorGeometry.BoundaryEdge(paramArrayOfVec41, i, j));
    }
  }

  private boolean edgeOnTriangle(Vec4 paramVec41, Vec4 paramVec42, Vec4[] paramArrayOfVec4)
  {
    double[] arrayOfDouble1 = baryCentricCoordsRequireInside(paramVec41, paramArrayOfVec4);
    double[] arrayOfDouble2 = baryCentricCoordsRequireInside(paramVec42, paramArrayOfVec4);
    if ((arrayOfDouble1 == null) || (arrayOfDouble2 == null))
      return true;
    for (int i = 0; i < 3; i++)
      if ((arrayOfDouble1[i] < 0.0001D) && (arrayOfDouble2[i] < 0.0001D))
        return true;
    return false;
  }

  private SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(RectTile paramRectTile, Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43, double paramDouble1, double paramDouble2)
  {
    RectTile.access$1500(paramRectTile).vertices.rewind();
    RectTile.access$1500(paramRectTile).indices.rewind();
    Vec4 localVec4 = RectTile.access$1500(paramRectTile).referenceCenter;
    if (localVec4 == null)
      localVec4 = new Vec4(0.0D);
    int i = RectTile.access$1500(paramRectTile).indices.capacity() - 2;
    int[] arrayOfInt = new int[3];
    double[] arrayOfDouble = new double[3];
    SectorGeometry.ExtractedShapeDescription localExtractedShapeDescription = null;
    for (int j = 0; j < i; j++)
    {
      RectTile.access$1500(paramRectTile).indices.position(j);
      RectTile.access$1500(paramRectTile).indices.get(arrayOfInt);
      if ((arrayOfInt[0] == arrayOfInt[1]) || (arrayOfInt[0] == arrayOfInt[2]) || (arrayOfInt[1] == arrayOfInt[2]))
        continue;
      Vec4[] arrayOfVec4 = new Vec4[3];
      for (int k = 0; k < 3; k++)
      {
        RectTile.access$1500(paramRectTile).vertices.position(3 * arrayOfInt[k]);
        RectTile.access$1500(paramRectTile).vertices.get(arrayOfDouble);
        arrayOfVec4[k] = new Vec4(arrayOfDouble[0] + localVec4.getX(), arrayOfDouble[1] + localVec4.getY(), arrayOfDouble[2] + localVec4.getZ(), 1.0D);
      }
      localExtractedShapeDescription = addClippedPolygon(arrayOfVec4, paramVec41, paramVec42, paramVec43, paramDouble1, paramDouble2, localExtractedShapeDescription);
    }
    return localExtractedShapeDescription;
  }

  private SectorGeometry.ExtractedShapeDescription addClippedPolygon(Vec4[] paramArrayOfVec4, Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43, double paramDouble1, double paramDouble2, SectorGeometry.ExtractedShapeDescription paramExtractedShapeDescription)
  {
    if (isSkirt(paramArrayOfVec4))
      return paramExtractedShapeDescription;
    int i = 0;
    int j = 0;
    int k = -1;
    int m = -1;
    for (Object localObject2 : paramArrayOfVec4)
    {
      Vec4 localVec42 = localObject2.subtract3(paramVec41);
      double d1 = localVec42.dot3(paramVec42);
      double d2 = localVec42.dot3(paramVec43);
      double d3 = d1 * d1 / (paramDouble1 * paramDouble1) + d2 * d2 / (paramDouble2 * paramDouble2) - 1.0D;
      if (d3 <= 0.0D)
      {
        k = i++;
        j++;
      }
      else
      {
        m = i++;
      }
    }
    BoundaryEdge Boundary = new SectorGeometry.BoundaryEdge(null, -1, -1);
    Vec4 localVec41;
    switch (j)
    {
    case 0:
      paramArrayOfVec4 = checkForEdgeCylinderIntersections(paramArrayOfVec4, paramVec41, paramVec42, paramVec43, paramDouble1, paramDouble2);
      break;
    case 1:
      if (k != 0)
      {
        localVec41 = paramArrayOfVec4[k];
        paramArrayOfVec4[k] = paramArrayOfVec4[0];
        paramArrayOfVec4[0] = localVec41;
      }
      paramArrayOfVec4 = computeTrimmedPoly(paramArrayOfVec4, paramVec41, paramVec42, paramVec43, paramDouble1, paramDouble2, j, (SectorGeometry.BoundaryEdge)Boundary);
      break;
    case 2:
      if (m != 0)
      {
        localVec41 = paramArrayOfVec4[m];
        paramArrayOfVec4[m] = paramArrayOfVec4[0];
        paramArrayOfVec4[0] = localVec41;
      }
      paramArrayOfVec4 = computeTrimmedPoly(paramArrayOfVec4, paramVec41, paramVec42, paramVec43, paramDouble1, paramDouble2, j, (SectorGeometry.BoundaryEdge)Boundary);
      break;
    case 3:
    }
    if (paramArrayOfVec4 == null)
      return paramExtractedShapeDescription;
    if (paramExtractedShapeDescription == null)
      paramExtractedShapeDescription = new SectorGeometry.ExtractedShapeDescription(new ArrayList(100), new ArrayList(50));
    paramExtractedShapeDescription.interiorPolys.add(paramArrayOfVec4);
    if (((SectorGeometry.BoundaryEdge)Boundary).vertices != null)
      paramExtractedShapeDescription.shapeOutline.add(Boundary);
    return (SectorGeometry.ExtractedShapeDescription)paramExtractedShapeDescription;
  }

  private Vec4[] checkForEdgeCylinderIntersections(Vec4[] paramArrayOfVec4, Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43, double paramDouble1, double paramDouble2)
  {
    return null;
  }

  private Vec4[] computeTrimmedPoly(Vec4[] paramArrayOfVec4, Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43, double paramDouble1, double paramDouble2, int paramInt, SectorGeometry.BoundaryEdge paramBoundaryEdge)
  {
    Vec4 localVec41 = intersectWithEllCyl(paramArrayOfVec4[0], paramArrayOfVec4[1], paramVec41, paramVec42, paramVec43, paramDouble1, paramDouble2);
    Vec4 localVec42 = intersectWithEllCyl(paramArrayOfVec4[0], paramArrayOfVec4[2], paramVec41, paramVec42, paramVec43, paramDouble1, paramDouble2);
    Vec4 localVec43 = localVec41.multiply3(0.5D).add3(localVec42.multiply3(0.5D));
    if (paramInt == 1)
    {
      paramArrayOfVec4[1] = localVec41;
      paramArrayOfVec4[2] = localVec42;
      paramBoundaryEdge.vertices = paramArrayOfVec4;
      paramBoundaryEdge.i1 = 1;
      paramBoundaryEdge.i2 = 2;
      paramBoundaryEdge.toMidPoint = localVec43.subtract3(paramArrayOfVec4[0]);
      return paramArrayOfVec4;
    }
    Vec4[] arrayOfVec4 = new Vec4[4];
    arrayOfVec4[0] = localVec41;
    arrayOfVec4[1] = paramArrayOfVec4[1];
    arrayOfVec4[2] = paramArrayOfVec4[2];
    arrayOfVec4[3] = localVec42;
    paramBoundaryEdge.vertices = arrayOfVec4;
    paramBoundaryEdge.i1 = 0;
    paramBoundaryEdge.i2 = 3;
    paramBoundaryEdge.toMidPoint = paramArrayOfVec4[0].subtract3(localVec43);
    return arrayOfVec4;
  }

  private Vec4 intersectWithEllCyl(Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43, Vec4 paramVec44, Vec4 paramVec45, double paramDouble1, double paramDouble2)
  {
    Vec4 localVec41 = paramVec41.subtract3(paramVec43);
    double d1 = localVec41.dot3(paramVec44);
    double d2 = localVec41.dot3(paramVec45);
    Vec4 localVec42 = paramVec42.subtract3(paramVec43);
    double d3 = localVec42.dot3(paramVec44);
    double d4 = localVec42.dot3(paramVec45);
    double d5 = d3 - d1;
    double d6 = d4 - d2;
    double d7 = paramDouble1 * paramDouble1;
    double d8 = paramDouble2 * paramDouble2;
    double d9 = d5 * d5 / d7 + d6 * d6 / d8;
    double d10 = 2.0D * (d1 * d5 / d7 + d2 * d6 / d8);
    double d11 = d1 * d1 / d7 + d2 * d2 / d8 - 1.0D;
    double d12 = Math.sqrt(d10 * d10 - 4.0D * d9 * d11);
    double d13 = (-d10 + d12) / (2.0D * d9);
    if ((d13 < 0.0D) || (d13 > 1.0D))
      d13 = (-d10 - d12) / (2.0D * d9);
    return paramVec41.multiply3(1.0D - d13).add3(paramVec42.multiply3(d13));
  }

    @Override
    public long getUpdateFrequency() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setUpdateFrequency(long updateFrequency) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

  private static class CacheKey
  {
    private final Sector sector;
    private final int density;
    private final Object globeStateKey;

    public CacheKey(DrawContext paramDrawContext, Sector paramSector, int paramInt)
    {
      this.sector = paramSector;
      this.density = paramInt;
      this.globeStateKey = paramDrawContext.getGlobe().getStateKey(paramDrawContext);
    }

    public boolean equals(Object paramObject)
    {
      if (this == paramObject)
        return true;
      CacheKey localCacheKey = (CacheKey)paramObject;
      if (this.density != localCacheKey.density)
        return false;
      if (this.globeStateKey != null ? !this.globeStateKey.equals(localCacheKey.globeStateKey) : localCacheKey.globeStateKey != null)
        return false;
      return this.sector != null ? this.sector.equals(localCacheKey.sector) : localCacheKey.sector == null;
    }

    public int hashCode()
    {
      int i = this.sector != null ? this.sector.hashCode() : 0;
      i = 31 * i + this.density;
      i = 31 * i + (this.globeStateKey != null ? this.globeStateKey.hashCode() : 0);
      return i;
    }
  }

  public static class RectTile
    implements SectorGeometry
  {

        private static void access$1502(RectTile paramRectTile, RenderInfo renderInfo) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        private static Object access$1500(RectTile paramRectTile) {
            throw new UnsupportedOperationException("Not yet implemented");
        }

        private static void access$2602(RectTile paramRectTile, int rGB) {
            throw new UnsupportedOperationException("Not yet implemented");
        }
    private final RectangularNormalTessellator tessellator;
    private final int level;
    private final Sector sector;
    private final int density;
    private final double log10CellSize;
    private Extent extent;
    private RectangularNormalTessellator.RenderInfo ri;
    private int minColorCode = 0;
    private int maxColorCode = 0;

    public RectTile(RectangularNormalTessellator paramRectangularNormalTessellator, Extent paramExtent, int paramInt1, int paramInt2, Sector paramSector, double paramDouble)
    {
      this.tessellator = paramRectangularNormalTessellator;
      this.level = paramInt1;
      this.density = paramInt2;
      this.sector = paramSector;
      this.extent = paramExtent;
      this.log10CellSize = Math.log10(paramDouble);
    }

    public Sector getSector()
    {
      return this.sector;
    }

    public Extent getExtent()
    {
      return this.extent;
    }

    public void renderMultiTexture(DrawContext paramDrawContext, int paramInt)
    {
      this.tessellator.renderMultiTexture(paramDrawContext, this, paramInt);
    }

    public void render(DrawContext paramDrawContext)
    {
      this.tessellator.render(paramDrawContext, this);
    }

    public void renderWireframe(DrawContext paramDrawContext, boolean paramBoolean1, boolean paramBoolean2)
    {
      this.tessellator.renderWireframe(paramDrawContext, this, paramBoolean1, paramBoolean2);
    }

    public void renderBoundingVolume(DrawContext paramDrawContext)
    {
      this.tessellator.renderBoundingVolume(paramDrawContext, this);
    }

    public PickedObject[] pick(DrawContext paramDrawContext, List<? extends Point> paramList)
    {
      return this.tessellator.pick(paramDrawContext, this, paramList);
    }

    public void pick(DrawContext paramDrawContext, Point paramPoint)
    {
      this.tessellator.pick(paramDrawContext, this, paramPoint);
    }

    public Vec4 getSurfacePoint(Angle paramAngle1, Angle paramAngle2, double paramDouble)
    {
      return this.tessellator.getSurfacePoint(this, paramAngle1, paramAngle2, paramDouble);
    }

    public double getResolution()
    {
      return this.sector.getDeltaLatRadians() / this.density;
    }

    public Intersection[] intersect(Line paramLine)
    {
      return this.tessellator.intersect(this, paramLine);
    }

    public Intersection[] intersect(double paramDouble)
    {
      return this.tessellator.intersect(this, paramDouble);
    }

    public DoubleBuffer makeTextureCoordinates(SectorGeometry.GeographicTextureCoordinateComputer paramGeographicTextureCoordinateComputer)
    {
      return this.tessellator.makeGeographicTexCoords(this, paramGeographicTextureCoordinateComputer);
    }

    public SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(Plane[] paramArrayOfPlane)
    {
      return this.tessellator.getIntersectingTessellationPieces(this, paramArrayOfPlane);
    }

    public SectorGeometry.ExtractedShapeDescription getIntersectingTessellationPieces(Vec4 paramVec41, Vec4 paramVec42, Vec4 paramVec43, double paramDouble1, double paramDouble2)
    {
      return this.tessellator.getIntersectingTessellationPieces(this, paramVec41, paramVec42, paramVec43, paramDouble1, paramDouble2);
    }

        @Override
        public void beginRendering(DrawContext dc, int numTextureUnits) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void endRendering(DrawContext dc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void renderTileID(DrawContext dc) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void renderMultiTexture(DrawContext dc, int numTextureUnits, boolean beginRenderingCalled) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void render(DrawContext dc, boolean beginRenderingCalled) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    public int getLevel()
    {
        return this.level;
    }
  }

  protected static class RenderInfo
  {
    private final int density;
    private final Vec4 referenceCenter;
    private final DoubleBuffer vertices;
    private final DoubleBuffer normals;
    private final DoubleBuffer texCoords;
    private final IntBuffer indices;
    private final long time;

    private RenderInfo(int paramInt, DoubleBuffer paramDoubleBuffer1, DoubleBuffer paramDoubleBuffer2, DoubleBuffer paramDoubleBuffer3, Vec4 paramVec4)
    {
      this.density = paramInt;
      this.vertices = paramDoubleBuffer1;
      this.texCoords = paramDoubleBuffer2;
      this.referenceCenter = paramVec4;
      this.indices = RectangularNormalTessellator.getIndices(this.density);
      this.normals = paramDoubleBuffer3;
      this.time = System.currentTimeMillis();
    }

    private long getSizeInBytes()
    {
      return 32 + this.vertices.limit() * 64;
    }
  }
}