/*
Copyright (C) 2001, 2011 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import javax.media.opengl.GL;
import java.awt.*;
import java.io.File;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Responsible for rendering each node found in a Collada File.  Currently created on demand by the parser in
 * ColladaSceneShape. Uses VBOElementRenderer and UnifiedBuffers to manage VBO's.
 *
 * @author tag
 * @version $ID$
 */

public class ColladaNodeShape extends AbstractGeneralShape
{
    static boolean UPDATE_ALL_DATA_EVERY_EXPIRATION = false;
    static boolean PERF_USE_BEGIN_DRAWING_FORPERF = true;

    static boolean DEBUG_USE_IMMEDIATE_MODE = false;
    static int PERFORMANCE_MINSIZE_USETEXTURING = 50;
    static boolean PERFORMANCE_TURNOFF_TEXTURING_FOR_SMALL = false;
    static boolean DEBUG_SKIP_TEXTURING_PERFORMANCE_TESTING = false;

    static final int byteSizeFloat = Float.SIZE / Byte.SIZE;
    static final int vertsPerTri = 3;
    static final int texCoordsPerVert = 2;
    static final int normCoordsPerVert = 3;
    static final int positionCoordsPerVert = 3;
    static boolean unifiedInterleaved = true;
    static boolean textureEnabled = false;

    static final int totalDataSizePerVertexInFloats = ((positionCoordsPerVert + texCoordsPerVert));
    static final int totalDataSizePerVertexInBytes = (byteSizeFloat * totalDataSizePerVertexInFloats);

    static boolean USE_DRAW_ELEMENTS = true;
//    static boolean USE_PER_INSTANCE_STORAGE = false || DEBUG_USE_IMMEDIATE_MODE;

    static int totalShapes = 0;
    static int totalElements = 0;

    static double[] matrixArray = new double[16];

    protected HashMap<String, SourceData> sources = new HashMap<String, SourceData>();
    protected int[] elementsFromPElement;
    protected int numberOfElements;
    protected WWTexture texture;

    protected File colladaRootFile;
    protected String textureSource;
    protected Layer mostRecentLayer;

    protected ColladaSceneShape mother;
    protected boolean haveDrawnWithTexture = false;
    /** Holds the model address as specified at construction. */
    protected String modelAddress;

    /** Holds the model root created by the model parser. */
    protected AtomicReference<ColladaRoot> colladaRoot = new AtomicReference<ColladaRoot>();
    /** Identifies the {@link FileStore} of the supporting file cache for this model. */
    protected FileStore fileStore = WorldWind.getDataFileStore();
    /** Provides a semaphore to synchronize access to the model file. */
    protected final Object fileLock = new Object();

    // TODO: determine computed info that's globe/window dependent and place it here

    /**
     * This class holds globe-specific data for this shape. It's managed via the shape-data cache in {@link
     * gov.nasa.worldwind.render.AbstractShape.AbstractShapeData}.
     */
    protected static class ShapeData extends AbstractGeneralShape.ShapeData
    {
        /**
         * Construct a cache entry for this shape.
         *
         * @param dc    the current draw context.
         * @param shape this shape.
         */
        public ShapeData(DrawContext dc, AbstractGeneralShape shape)
        {
            super(dc, shape);
        }

        VBOElementRenderer vboElementRenderer;
        Matrix renderMatrix;
        Vec4 referenceCenter;
        // min/max used for generating Extent Box
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;

        double minY = Double.MAX_VALUE;
        double maxY = Double.MIN_VALUE;

        double minZ = Double.MAX_VALUE;
        double maxZ = Double.MIN_VALUE;

        boolean readyForRendering = false;

        public boolean readyForRendering()
        {
            return (this.readyForRendering && this.vboElementRenderer.ready());
        }
    }

    public ColladaNodeShape(ColladaRoot colladaRoot, String id, ColladaSceneShape mother)
    {
        setColladaRoot(colladaRoot);
        this.activeAttributes.setDrawOutline(false);
        this.mother = mother;
    }

    /**
     * Returns the current shape data cache entry.
     *
     * @return the current data cache entry.
     */
    protected ShapeData getCurrent()
    {
        return (ShapeData) this.getCurrentData();
    }

    /**
     * Creates and returns a new cache entry specific to the subclass.
     *
     * @param dc the current draw context.
     *
     * @return a data cache entry for the state in the specified draw context.
     */

    @Override
    protected AbstractShapeData createCacheEntry(DrawContext dc)
    {
        return new ShapeData(dc, this);
    }

    /**
     * Returns the root of this model's parsed Collada file.
     *
     * @return the root of this model's parsed Collada file.
     */
    protected ColladaRoot getColladaRoot()
    {
        return this.colladaRoot.get();
    }

    /**
     * Specifies the root of this model's parsed Collada file.
     *
     * @param root the root of this model's parsed Collada file.
     */
    protected void setColladaRoot(ColladaRoot root)
    {
        this.colladaRoot.set(root);
    }

    /**
     * Indicates the address of this shape's model file as specified to the constructor.
     *
     * @return the address of this shape's model file.
     */
    public String getModelAddress()
    {
        return this.modelAddress;
    }

    /**
     * Specifies the local or remote model file address.
     *
     * @param address the model file address, may be to a local or remote file.
     *
     * @throws IllegalArgumentException if the address is null or empty.
     */
    public void setModelAddress(String address)
    {
        if (WWUtil.isEmpty(address))
        {
            String message = Logging.getMessage("nullValue.PathIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.modelAddress = address;
    }

    /**
     * Indicates the {@link FileStore} that supports caching for this shape.
     *
     * @return the file store supporting caching for this shape.
     */
    protected FileStore getFileStore()
    {
        return this.fileStore;
    }

    /**
     * Indicates whether this shape's renderable geometry must be recomputed, either as a result of an attribute or
     * property change or the expiration of the geometry regeneration interval.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     *
     * @return true if this shape's geometry must be regenerated, otherwise false.
     */
    @Override
    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        return this.getCurrent().readyForRendering() == false;
    }

    /**
     * Indicates whether this shape's outline must be drawn.
     *
     * @return true if the outline should be drawn, otherwise false.
     */

    @Override
    protected boolean mustDrawOutline()
    {
        return false; // Collada models don't have independent outlines
    }

    /**
     * Indicates whether texture should be applied to this shape. Called during rendering to determine whether texture
     * state should be established during preparation for interior drawing.
     * <p/>
     * Note: This method always returns false during the pick pass.
     *
     * @param dc the current draw context
     *
     * @return true if texture should be applied, otherwise false.
     */

    @Override
    protected boolean mustApplyTexture(DrawContext dc)
    {
        // TODO: Determine whether textures exist and must be applied
        return true;
    }

    /**
     * Draw this shape as an ordered renderable. If in picking mode, add it to the picked object list of specified
     * {@link PickSupport}. The <code>PickSupport</code> may not be the one associated with this instance. During batch
     * picking the <code>PickSupport</code> of the instance initiating the batch picking is used so that all shapes
     * rendered in batch are added to the same pick list.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc             the current draw context.
     * @param pickCandidates a pick support holding the picked object list to add this shape to.
     */

    @Override
    protected void doDrawOrderedRenderable(DrawContext dc, PickSupport pickCandidates)
    {
        GL gl = dc.getGL();

        if (dc.isPickingMode())
        {
            Color pickColor = dc.getUniquePickColor();
            pickCandidates.addPickableObject(this.createPickedObject(dc, pickColor));
            gl.glColor3ub((byte) pickColor.getRed(), (byte) pickColor.getGreen(), (byte) pickColor.getBlue());
        }

        doDrawInterior(dc);
    }

    /**
     * Produces the geometry and other state necessary to represent this shape as an ordered renderable. Places this
     * shape on the draw context's ordered renderable list for subsequent rendering. This method is called during {@link
     * #pick(DrawContext, java.awt.Point)} and {@link #render(DrawContext)} when it's been determined that the shape is
     * likely to be visible.
     *
     * @param dc the current draw context.
     *
     * @return true if the ordered renderable state was successfully computed, otherwise false, in which case the
     *         current pick or render pass is terminated for this shape. Subclasses should return false if it is not
     *         possible to create the ordered renderable state.
     *
     * @see #pick(DrawContext, java.awt.Point)
     * @see #render(DrawContext)
     */

    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        // Do the minimum necessary to determine the model's reference point, extent and eye distance.
        this.createMinimalGeometry(dc, this.getCurrent());

        // If the shape is less that a pixel in size, don't render it.
        if (
            this.getCurrent().getExtent() == null ||
                // <---- intersectsFrustum returns true if extent is null, which way do we want it?
                dc.isSmall(this.getExtent(), 1))
            return false;

        if (!this.intersectsFrustum(dc))
            return false;

        this.createFullGeometry(dc, dc.getTerrain(), this.getCurrent());

        return true;//this.getColladaRoot() != null;
    }

    /**
     * Determines whether this shape's ordered renderable state is valid and can be rendered. Called by {@link
     * #makeOrderedRenderable(DrawContext)}just prior to adding the shape to the ordered renderable list.
     *
     * @param dc the current draw context.
     *
     * @return true if this shape is ready to be rendered as an ordered renderable.
     */

    @Override
    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        return this.getColladaRoot() != null; // TODO: provide a more definitive test
    }

    /**
     * Draws this shape's outline. Called immediately after calling {@link #prepareToDrawOutline(DrawContext,
     * ShapeAttributes, ShapeAttributes)}, which establishes OpenGL state for lighting, blending, pick color and line
     * attributes. Subclasses should execute the drawing commands specific to the type of shape.
     * <p/>
     * Collada models don't have independent outlines, so this method is not implemented.
     * <p/>
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        // Collada models don't have independent outlines, so this method is not implemented.
    }

    /**
     * Draws this shape's interior. Called immediately after calling {@link #prepareToDrawInterior(DrawContext,
     * ShapeAttributes, ShapeAttributes)}, which establishes OpenGL state for lighting, blending, pick color and
     * interior attributes. Subclasses should execute the drawing commands specific to the type of shape.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void doDrawInterior(DrawContext dc)
    {
        if (dc.isPickingMode())
            return;

        // If the shape is less that a pixel in size, don't render it.
//         if (this.getExtent() == null
//             || dc.isSmall(this.getExtent(), 1))
//             return;

        drawColladaInterior(dc);
    }

    /**
     * Determines whether to add this shape to the draw context's ordered renderable list. Creates this shapes
     * renderable geometry.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Re-use values already calculated this frame.
        if (dc.getFrameTimeStamp() != this.getCurrentData().getFrameNumber())
        {
            this.determineActiveAttributes();
            if (this.getActiveAttributes() == null)
                return;

            // Regenerate the positions and shape at a specified frequency.
            if (this.mustRegenerateGeometry(dc))
            {
                if (!this.doMakeOrderedRenderable(dc))
                    return;

                //if (this.shouldUseVBOs(dc))
                this.fillVBO(dc);   // always do this, so we setup vertex array once

                this.getCurrentData().restartTimer(dc);
            }

            this.getCurrentData().setFrameNumber(dc.getFrameTimeStamp());
        }

        if (!this.isOrderedRenderableValid(dc))
            return;

        if (dc.isPickingMode())
            this.pickLayer = dc.getCurrentLayer();

        dc.addOrderedRenderable(this);
    }

    /**
     * Called during drawing to set the modelview matrix to apply the correct position, scale and orientation for
     * this ColladaNodeShape.
     *
     * @param dc the current DrawContext
     *
     * @throws IllegalArgumentException if draw context is null or the draw context GL is null
     */

    protected final void setModelViewMatrix(DrawContext dc)
    {
        Matrix matrix = dc.getView().getModelviewMatrix();
        matrix = matrix.multiply(computeRenderMatrix(dc));

        GL gl = dc.getGL();
        if (!PERF_USE_BEGIN_DRAWING_FORPERF)
        {
            gl.glMatrixMode(GL.GL_MODELVIEW);
        }

        matrix.toArray(matrixArray, 0, false);
        gl.glLoadMatrixd(matrixArray, 0);
    }

    /**
     * sets up texturing, including lazilyLoadedTextures via getTextureFromImageSource();
     *
     * @param dc            the current DrawContext
     * @param gl            the current GL Context
     * @param texturePushed truen if texture is pushed
     *
     * @return returns true if texture is bound and ready
     *
     * @throws IllegalArgumentException if draw context is null or the draw context GL is null
     */

    protected boolean setupTexturing(DrawContext dc, GL gl, boolean texturePushed)
    {
        if (this.texture == null && this.textureSource != null)
        {
            this.texture = this.getTextureFromImageSource();
        }

        // set up the texture if one exists
        if (texture != null && mustApplyTexture(dc) && texture.bind(dc))
        {
            texturePushed = true;

            if (!textureEnabled)
            {
                gl.glEnable(GL.GL_TEXTURE_2D);
                textureEnabled = true;
            }

            if (!PERF_USE_BEGIN_DRAWING_FORPERF)
            {
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_BORDER);
                gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_BORDER);
            }
        }
        else if (haveDrawnWithTexture || this.textureSource == null)
        {
            if (textureEnabled)
            {
                gl.glDisable(GL.GL_TEXTURE_2D);
                textureEnabled = false;
            }

            if (!PERF_USE_BEGIN_DRAWING_FORPERF)
            {
                gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);
            }
        }
        return texturePushed;
    }

    /**
     * get the texture needed for this Collada Shape using LazilyLoadedTextures, this may start deferred image loading
     *
     * @return the WWTexture for this shape to be used during rendering.
     */

    protected WWTexture getTextureFromImageSource()
    {
        String imageSource = this.textureSource;

        if (imageSource != null)
        {
            String sourceString = imageSource;// (String) imageSource;
            if (sourceString.startsWith("./"))
            {
                sourceString = sourceString.substring(2);
            }

            File requestFile = new File(colladaRootFile, sourceString);
            URL imageURL = WorldWind.getDataFileStore().requestFile(requestFile.getAbsolutePath());
            if (imageURL != null)
            {
                WWTexture texture = new LazilyLoadedTexture(imageURL, true);

                return texture;
            }
        }

        return null;
    }

    /**
     * Establish the OpenGL state needed to draw this shape.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc       the current draw context.
     * @param attrMask an attribute mask indicating state the caller will set. This base class implementation sets
     *                 <code>GL_CURRENT_BIT, GL_LINE_BIT, GL_HINT_BIT, GL_POLYGON_BIT, GL_COLOR_BUFFER_BIT, and
     *                 GL_TRANSFORM_BIT</code>.
     *
     * @return the stack handler used to set the OpenGL state. Callers should use this to set additional state,
     *         especially state indicated in the attribute mask argument.
     */

    @Override
    protected OGLStackHandler beginDrawing(DrawContext dc, int attrMask)
    {
        OGLStackHandler oglState = super.beginDrawing(dc, attrMask);

        GL gl = dc.getGL();

        if (PERF_USE_BEGIN_DRAWING_FORPERF)
        {
            if (!DEBUG_SKIP_TEXTURING_PERFORMANCE_TESTING)
            {
                oglState.pushTexture(gl);
                gl.glEnable(GL.GL_TEXTURE_2D);
                textureEnabled = true;
                gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
                gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);

                gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE,
                    GL.GL_DECAL);   /// dont blend with color, or lighting- since we dont light, its faster
                gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            }
            else
            {
                gl.glDisable(GL.GL_TEXTURE_2D);
                gl.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);
            }

            gl.glMatrixMode(GL.GL_MODELVIEW);

            //-------------------------------------
            // necessary???
            //-------------------------------------
            gl.glDisable(GL.GL_BLEND);
//               gl.glEnable(GL.GL_CULL_FACE);
            gl.glDisable(GL.GL_CULL_FACE);
            gl.glCullFace(GL.GL_BACK);
            //-------------------------------------
            //-------------------------------------

            ShapeData shapeData = (ShapeData) getCurrentData();
            shapeData.vboElementRenderer.beginDrawing(gl);
        }

        return oglState;
    }

    /**
     * Pop the state set in {@link #beginDrawing(DrawContext, int)}.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is
     * called.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void endDrawing(DrawContext dc)
    {
        GL gl = dc.getGL();
        if (PERF_USE_BEGIN_DRAWING_FORPERF)
        {
            ShapeData shapeData = (ShapeData) getCurrentData();
            shapeData.vboElementRenderer.endDrawing(gl);

            gl.glDisable(GL.GL_TEXTURE_2D);
            gl.glDisableClientState(GL.GL_TEXTURE_COORD_ARRAY);

            gl.glTexEnvi(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);   // - restore-
        }

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

        super.endDrawing(dc);
    }

    /**
     * draws the Collada Interior
     *
     * @param dc the current draw context.
     */

    protected void drawColladaInterior(DrawContext dc)
    {
        ShapeData shapedata = (ShapeData) getCurrentData();
        if (shapedata == null || shapedata.vboElementRenderer == null)
            return;

        GL gl = dc.getGL();

        setModelViewMatrix(dc);

        boolean texturePushed = false;

        // skip texturing if debugging options is on or if we want to skip texturing for small shapes
        this.mostRecentLayer = dc.getCurrentLayer();

        if (!DEBUG_SKIP_TEXTURING_PERFORMANCE_TESTING
            && !(PERFORMANCE_TURNOFF_TEXTURING_FOR_SMALL
            && (this.getExtent() == null || dc.isSmall(this.getExtent(), PERFORMANCE_MINSIZE_USETEXTURING))))
        {
            texturePushed = setupTexturing(dc, gl, texturePushed);
        }

        try
        {
            if (this.textureSource == null || haveDrawnWithTexture || texturePushed)
            {
                haveDrawnWithTexture = true;
                if (!PERF_USE_BEGIN_DRAWING_FORPERF)
                {
                    gl.glEnableClientState(GL.GL_VERTEX_ARRAY);
                    gl.glEnableClientState(GL.GL_TEXTURE_COORD_ARRAY);
                }

                if (this.shouldUseVBOs(dc))
                {
                    if (!shapedata.vboElementRenderer.vboReady(dc))
                    {
                        this.fillVBO(dc);
                    }

                    shapedata.vboElementRenderer.drawWithVBO(dc);
                }
                else
                {
                    shapedata.vboElementRenderer.drawWithVertexArray(gl);
                }
            }
        }
        finally
        {
            if (!PERF_USE_BEGIN_DRAWING_FORPERF)
            {
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
                gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
            }
        }
    }

    /**
     * Determines whether this shape's geometry should be invalidated because the view distance changed, and if so,
     * invalidates the geometry.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void checkViewDistanceExpiration(DrawContext dc)
    {
        // Determine whether the distance of this shape from the eye has changed significantly. Invalidate the previous
        // extent and expire the shape geometry if it has. "Significantly" is considered a 10% difference.

        Vec4 refPt = this.currentData.getReferencePoint();
        if (refPt == null)
            return;

        double newRefDistance = dc.getView().getEyePoint().distanceTo3(refPt);
        Double oldRefDistance = this.currentData.getReferenceDistance();
        if (oldRefDistance == null || Math.abs(newRefDistance - oldRefDistance) / oldRefDistance > 0.10)
        {
            this.currentData.setExpired(false);
            this.currentData.setExtent(null);
            this.currentData.setReferenceDistance(newRefDistance);
        }
    }

    /**
     * Generate the extent from the Collada data by making a min max box
     *
     * @param dc the current draw context.
     */

    protected void generateExtentFromCollada(DrawContext dc)
    {
        if (this.getExtent() == null)
        {
            ShapeData shapedata = (ShapeData) getCurrentData();

            SourceData vertexSource = sources.get(
                "VERTEX");        // hard coded for sample data, move to loops and hashmaps
            float[] vertexes = vertexSource.floatData;
            float[] verts = vertexes;

            int sourcesStride = sources.size();
            for (int i = 0; i < numberOfElements; i++)
            {
                // generate index in <p> element for current element
                // use <p> element value to lookup into source array.  Additional "accessor" element complexity here later

                int vertexXIndex = i * (vertsPerTri
                    * sourcesStride);   //   have to stride across all sources, and multiply times vertsPerTri
                int vertexYIndex = vertexXIndex + (1
                    * sourcesStride);  //   (because sources are interleaved, stride across a vert by striding across # of sources)
                int vertexZIndex = vertexXIndex + (2 * sourcesStride);  //    -- same as above but go to last element

                int vertexTRI_FIRSTVERTEX = positionCoordsPerVert
                    * elementsFromPElement[vertexXIndex];   //   <p> element is kind of an overly complicated index
                int vertexTRI_SECONDVERTEX = positionCoordsPerVert * elementsFromPElement[vertexYIndex];
                int vertexTRI_THIRDVERTEX = positionCoordsPerVert * elementsFromPElement[vertexZIndex];

                float vertexX1 = ((verts[vertexTRI_FIRSTVERTEX]));
                float vertexY1 = ((verts[vertexTRI_FIRSTVERTEX + 1]));
                float vertexZ1 = ((verts[vertexTRI_FIRSTVERTEX + 2]));

                float vertexX2 = ((verts[vertexTRI_SECONDVERTEX]));
                float vertexY2 = ((verts[vertexTRI_SECONDVERTEX + 1]));
                float vertexZ2 = ((verts[vertexTRI_SECONDVERTEX + 2]));

                float vertexX3 = ((verts[vertexTRI_THIRDVERTEX]));
                float vertexY3 = ((verts[vertexTRI_THIRDVERTEX + 1]));
                float vertexZ3 = ((verts[vertexTRI_THIRDVERTEX + 2]));

                shapedata.minX = Math.min(shapedata.minX, vertexX1);
                shapedata.minX = Math.min(shapedata.minX, vertexX2);
                shapedata.minX = Math.min(shapedata.minX, vertexX3);

                shapedata.minY = Math.min(shapedata.minY, vertexY1);
                shapedata.minY = Math.min(shapedata.minY, vertexY2);
                shapedata.minY = Math.min(shapedata.minY, vertexY3);

                shapedata.minZ = Math.min(shapedata.minZ, vertexZ1);
                shapedata.minZ = Math.min(shapedata.minZ, vertexZ2);
                shapedata.minZ = Math.min(shapedata.minZ, vertexZ3);

                shapedata.maxX = Math.max(shapedata.maxX, vertexX1);
                shapedata.maxX = Math.max(shapedata.maxX, vertexX2);
                shapedata.maxX = Math.max(shapedata.maxX, vertexX3);

                shapedata.maxY = Math.max(shapedata.maxY, vertexY1);
                shapedata.maxY = Math.max(shapedata.maxY, vertexY2);
                shapedata.maxY = Math.max(shapedata.maxY, vertexY3);

                shapedata.maxZ = Math.max(shapedata.maxZ, vertexZ1);
                shapedata.maxZ = Math.max(shapedata.maxZ, vertexZ2);
                shapedata.maxZ = Math.max(shapedata.maxZ, vertexZ3);
            }

            this.setModelScale(
                new Vec4(shapedata.maxX - shapedata.minX, shapedata.maxY - shapedata.minY,
                    shapedata.maxZ - shapedata.minZ, 1));

            this.getCurrentData().setExtent(computeExtent(dc));
        }
    }

    /**
     * Fill this shape's vertex buffer objects. If the vertex buffer object resource IDs don't yet exist, create them.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void fillVBO(DrawContext dc)
    {
        ShapeData shapedata = (ShapeData) getCurrentData();
        SourceData vertexSource = sources.get(
            "VERTEX");        // hard coded for sample data, move to loops and hashmaps
        SourceData texCoordSource = sources.get("TEXCOORD");

        float[] vertexes = vertexSource.floatData;
        float[] verts = vertexes;
        float[] texcoords = null;

        boolean needFullDataUpdate = UPDATE_ALL_DATA_EVERY_EXPIRATION;

        if (texCoordSource != null)
        {
            texcoords = texCoordSource.floatData;
        }

        if (shapedata.vboElementRenderer == null)
        {
            needFullDataUpdate = true;
            shapedata.vboElementRenderer = UnifiedBuffer.GetVBOElementRenderer(dc,
                GL.GL_TRIANGLES,
                vertsPerTri * numberOfElements,
                positionCoordsPerVert, texCoordsPerVert,
                totalDataSizePerVertexInFloats);
        }
        else if (shapedata.vboElementRenderer.chunk == null)
        {
            needFullDataUpdate = true;
        }

        VBOElementRenderer vboElementRenderer = shapedata.vboElementRenderer;
        if (needFullDataUpdate)
        {
            FloatBuffer pushBuffer = vboElementRenderer.beginDataPush(dc);
            {
                int sourcesStride = sources.size();
                for (int i = 0; i < numberOfElements; i++)
                {
                    pushOneElementOfCoords(texCoordSource, verts, texcoords, sourcesStride, i, pushBuffer);
                }
            }
            vboElementRenderer.endDataPush_SynchToGPU(dc, this.shouldUseVBOs(dc));
        }
        else
        {
            vboElementRenderer.setupIndexBuffer(dc);
        }

        shapedata.readyForRendering = true;
    }

    /**
     * generates one element of coords for consumption by a vbo or vertex array
     *
     * @param texCoordSource the source for the texture coords
     * @param verts          the vertices
     * @param texcoords      the texture coordinates
     * @param sourcesStride  the stride value for the incoming index array (elementsFromPElement)
     * @param i              the index value for the data, used to look up the proper source element
     * @param pushBuffer     the data buffer we are pushing data into
     */
    protected void pushOneElementOfCoords(SourceData texCoordSource, float[] verts, float[] texcoords,
        int sourcesStride, int i, FloatBuffer pushBuffer)
    {
        // generate index in <p> element for current element
        // use <p> element value to lookup into source array.  Additional "accessor" element complexity here later

        int vertexXIndex = i * (vertsPerTri
            * sourcesStride);   //   have to stride across all sources, and multiply times vertsPerTri
        int vertexYIndex = vertexXIndex + (1
            * sourcesStride);  //   (because sources are interleaved, stride across a vert by striding across # of sources)
        int vertexZIndex = vertexXIndex + (2 * sourcesStride);  //    -- same as above but go to last element

        int textureCoordOffset = 0;
        if (texCoordSource != null)
            textureCoordOffset = texCoordSource.inputOffset;

        int texXIndex = vertexXIndex + textureCoordOffset;
        int texYIndex = vertexYIndex + textureCoordOffset;
        int texZIndex = vertexZIndex + textureCoordOffset;

        int texTRI_FIRSTVERTEX = texCoordsPerVert
            * elementsFromPElement[texXIndex];   //   <p> element is kind of an overly complicated index
        int texTRI_SECONDVERTEX = texCoordsPerVert * elementsFromPElement[texYIndex];
        int texTRI_THIRDVERTEX = texCoordsPerVert * elementsFromPElement[texZIndex];

        int vertexTRI_FIRSTVERTEX = positionCoordsPerVert
            * elementsFromPElement[vertexXIndex];   //   <p> element is kind of an overly complicated index
        int vertexTRI_SECONDVERTEX = positionCoordsPerVert * elementsFromPElement[vertexYIndex];
        int vertexTRI_THIRDVERTEX = positionCoordsPerVert * elementsFromPElement[vertexZIndex];

        float vertexX1 = ((verts[vertexTRI_FIRSTVERTEX]));
        float vertexY1 = ((verts[vertexTRI_FIRSTVERTEX + 1]));
        float vertexZ1 = ((verts[vertexTRI_FIRSTVERTEX + 2]));

        float vertexX2 = ((verts[vertexTRI_SECONDVERTEX]));
        float vertexY2 = ((verts[vertexTRI_SECONDVERTEX + 1]));
        float vertexZ2 = ((verts[vertexTRI_SECONDVERTEX + 2]));

        float vertexX3 = ((verts[vertexTRI_THIRDVERTEX]));
        float vertexY3 = ((verts[vertexTRI_THIRDVERTEX + 1]));
        float vertexZ3 = ((verts[vertexTRI_THIRDVERTEX + 2]));

        // Note we always maintain TTVVV by packing zero's if we dont have tex coords
        // this lets us seek within bigger buffer during rendering...

        if (texcoords != null)
            pushBuffer.put(texcoords[texTRI_FIRSTVERTEX]);
        else
            pushBuffer.put(0);

        if (texcoords != null)
            pushBuffer.put(1.0f - texcoords[texTRI_FIRSTVERTEX + 1]);
        else
            pushBuffer.put(0);

        pushBuffer.put((vertexX1));
        pushBuffer.put((vertexY1));
        pushBuffer.put((vertexZ1));

        if (texcoords != null)
            pushBuffer.put(texcoords[texTRI_SECONDVERTEX]);
        else
            pushBuffer.put(0);
        if (texcoords != null)
            pushBuffer.put(1.0f - texcoords[texTRI_SECONDVERTEX + 1]);
        else
            pushBuffer.put(0);

        pushBuffer.put((vertexX2));
        pushBuffer.put((vertexY2));
        pushBuffer.put((vertexZ2));

        if (texcoords != null)
            pushBuffer.put(texcoords[texTRI_THIRDVERTEX]);
        else
            pushBuffer.put(0);
        if (texcoords != null)
            pushBuffer.put(1.0f - texcoords[texTRI_THIRDVERTEX + 1]);
        else
            pushBuffer.put(0);

        pushBuffer.put((vertexX3));
        pushBuffer.put((vertexY3));
        pushBuffer.put((vertexZ3));
    }

    @Override
    protected boolean shouldUseVBOs(DrawContext dc)
    {
        return !UnifiedBuffer.DEBUG_USE_VERTEX_ARRAY && super.shouldUseVBOs(dc);
    }

    public Position getReferencePosition()
    {
        if (mother.getModelPosition() != null)
        {
            return mother.getModelPosition();
        }

        return null;
    }

    /**
     * Computes this path's reference center.
     *
     * @param dc the current draw context.
     *
     * @return the computed reference center, or null if it cannot be computed.
     */
    protected Vec4 computeReferenceCenter(DrawContext dc)
    {
        Position pos = this.getReferencePosition();
        if (pos == null)
            return null;

        Vec4 newPt = computePoint(dc.getTerrain(), pos);
        return newPt;
    }

    /**
     * Computes the transform to use during rendering to convert the unit pyramid geometry representation of this
     * Pyramid to its correct Pyramid location, orientation and scale
     *
     * @param dc the current draw context
     *
     * @return the modelview transform for this Pyramid
     *
     * @throws IllegalArgumentException if draw context is null or the referencePoint is null
     */
    protected final Matrix computeRenderMatrix(DrawContext dc)
    {
        // rotate to the correct default orientation
        ShapeData current = this.getCurrent();

        Vec4 referenceCenter = computeReferenceCenter(dc);
        Position refPosition = dc.getGlobe().computePositionFromPoint(
            referenceCenter);   // do we expire these so i can keep them or do we just always recalc?

        Matrix matrix = dc.getGlobe().computeSurfaceOrientationAtPosition(refPosition);

        if (current.renderMatrix == null)     // just cache my local stuff for now
        {
            Matrix matrixLocal = Matrix.IDENTITY;

            if (heading != null)
                matrixLocal = matrixLocal.multiply(Matrix.fromRotationZ(Angle.POS360.subtract(this.heading)));

            if (pitch != null)
                matrixLocal = matrixLocal.multiply(Matrix.fromRotationX(this.pitch));

            if (roll != null)
                matrixLocal = matrixLocal.multiply(Matrix.fromRotationY(this.roll));

            current.renderMatrix = matrixLocal;
        }

        matrix = matrix.multiply(current.renderMatrix);
        return matrix;//current.renderMatrix;
    }

    protected final Matrix computeExtentMatrix(DrawContext dc)
    {
        Matrix matrix = computeRenderMatrix(dc);
        if (getModelScale() != null)
        {
            matrix = matrix.multiply(
                Matrix.fromScale(this.getModelScale().x, this.getModelScale().y, this.getModelScale().z));
        }

        return matrix;
    }

    /**
     * Computes the node shapes's extent using a bounding cone.
     *
     * @param dc the current drawContext.
     *
     * @return the computed extent.
     */
    protected final Extent computeExtent(DrawContext dc)
    {
        Matrix matrix = computeExtentMatrix(dc);

        // create a list of vertices representing the extrema of the unit sphere
        Vector<Vec4> extrema = new Vector<Vec4>(4);
        // transform the extrema by the render matrix to get their final positions
        Vec4 point = matrix.transformBy3(matrix, -1, 1, -1);   // far upper left
        extrema.add(point);
        point = matrix.transformBy3(matrix, 1, 1, 1);   // near upper right
        extrema.add(point);
        point = matrix.transformBy3(matrix, 1, -1, -1);   // near lower left
        extrema.add(point);
        point = matrix.transformBy3(matrix, -1, -1, 1);   // far lower right
        extrema.add(point);
        gov.nasa.worldwind.geom.Box boundingBox = gov.nasa.worldwind.geom.Box.computeBoundingBox(extrema);

        Vec4 centerPoint = getCurrentData().getReferencePoint();

        return boundingBox != null ? boundingBox.translate(centerPoint) : null;
    }

    /**
     * Compute enough geometry to determine this shape's extent, reference point and eye distance.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc        the current draw context.
     * @param shapeData the current shape data for this shape.
     */
    protected void createMinimalGeometry(DrawContext dc, ShapeData shapeData)
    {
        Vec4 refPt = this.computeReferencePoint(dc.getTerrain());
        if (refPt == null)
            return;
        shapeData.setReferencePoint(refPt);

        generateExtentFromCollada(dc);  // TODO: compute the model's extent

        shapeData.setEyeDistance(this.computeEyeDistance(dc, shapeData));
    }

    /**
     * Computes a shape's full geometry.
     *
     * @param dc        the current draw context.
     * @param terrain   the terrain to use when computing the geometry.
     * @param shapeData the current shape data for this shape.
     */
    protected void createFullGeometry(DrawContext dc, Terrain terrain, ShapeData shapeData) // TODO
    {

    }

    @Override
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException // TODO
    {
        // TODO: See Polygon and/or ExtrudedPolygon for an implementation example

        return null;
    }

    static class SourceData
    {
        private float[] floatData;
        private int inputOffset;

        public SourceData(float[] floatData, int inputOffset)
        {
            this.floatData = floatData;
            this.inputOffset = inputOffset;
        }
    }

    public void addSource(String semantic, int inputOffset, float[] floatData)
    {
        sources.put(semantic, new SourceData(floatData, inputOffset));
    }

    public void addElementsList(int inNumberOfElements, int[] intData)
    {
        numberOfElements = inNumberOfElements;
        elementsFromPElement = intData;

        totalElements += numberOfElements;
    }

    public void setTexture(File colladaRootFile, String textureSource)
    {
        this.colladaRootFile = colladaRootFile;
        this.textureSource = textureSource;
    }
}
