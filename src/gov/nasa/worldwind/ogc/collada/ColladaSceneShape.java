/*
Copyright (C) 2001, 2011 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.cache.FileStore;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.ogc.kml.KMLConstants;
import gov.nasa.worldwind.pick.PickSupport;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.terrain.Terrain;
import gov.nasa.worldwind.util.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author jfb
 * @version $ID$
 */

public class ColladaSceneShape extends AbstractGeneralShape
{
    /** Holds the model address as specified at construction. */
    protected String modelAddress;

    /** Holds the model root created by the model parser. */
    protected AtomicReference<ColladaRoot> colladaRoot = new AtomicReference<ColladaRoot>();
    /** Identifies the {@link gov.nasa.worldwind.cache.FileStore} of the supporting file cache for this model. */
    protected FileStore fileStore = WorldWind.getDataFileStore();
    /** Provides a semaphore to synchronize access to the model file. */
    protected final Object fileLock = new Object();

    /** Holds the model root file. */
    protected File colladaRootFile;

    /**
     * service i can call from my parse thread to resolve paths. this is io / filesystem work and doesnt belong on ui
     * thread.
     */

    protected ColladaFilePathResolver pathResolver;

    /** The Shapes representing the Collada Content. */
    protected AbstractList<ColladaNodeShape> colladaShapes = new ArrayList<ColladaNodeShape>();

    public ColladaSceneShape(ColladaFilePathResolver colladaFilePathResolver)
    {
        pathResolver = colladaFilePathResolver;
    }

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

        public boolean isValid(DrawContext dc)
        {
            return true;
        }

        public boolean isExpired(DrawContext dc)
        {
            return false;
        }
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

    @Override
    protected boolean mustRegenerateGeometry(DrawContext dc)
    {
        return this.getColladaRoot() == null; // TODO: verify that no other circumstances require geometry regeneration
    }

    @Override
    protected boolean mustDrawOutline()
    {
        return false; // Collada models don't have independent outlines
    }

    @Override
    protected boolean mustApplyTexture(DrawContext dc)
    {
        // TODO: Determine whether textures exist and must be applied
        return false;
    }

    @Override
    protected boolean doMakeOrderedRenderable(DrawContext dc)
    {
        // Do the minimum necessary to determine the model's reference point, extent and eye distance.
        this.createMinimalGeometry(dc, this.getCurrent());

        // If the shape is less that a pixel in size, don't render it.
        if (
            //this.getCurrent().getExtent() == null ||  // <---- intersectsFrustum returns true if extent is null, which way do we want it?
            dc.isSmall(this.getExtent(), 1))
            return false;

        if (!this.intersectsFrustum(dc))
            return false;

        this.createFullGeometry(dc, dc.getTerrain(), this.getCurrent());

        return this.getColladaRoot() != null;
    }

    @Override
    protected boolean isOrderedRenderableValid(DrawContext dc)
    {
        return this.getColladaRoot() != null; // TODO: provide a more definitive test
    }

    @Override
    protected void doDrawOutline(DrawContext dc)
    {
        // Collada models don't have independent outlines, so this method is not implemented.
    }

    public void render(DrawContext dc)
    {
        super.render(dc);

        for (ColladaNodeShape colladaShape : colladaShapes)
        {
            colladaShape.render(dc);
        }
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
        return null;
        // nothing to draw- scene shapes current delegate to node shapes
    }

    /**
     * Pop the state set in {@link #beginDrawing(DrawContext, int)}.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void endDrawing(DrawContext dc)
    {
        // nothing to draw- scene shapes current delegate to node shapes
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
        // nothing to draw- scene shapes current delegate to node shapes
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
        // nothing to draw- scene shapes current delegate to node shapes
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

        // TODO: compute the model's extent

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
        if (this.getColladaRoot() == null)
            this.createModel(dc);
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
        // nothing to draw- scene shapes current delegate to node shapes
    }

    /**
     * Determines whether to add this shape to the draw context's ordered renderable list. Creates this shapes
     * renderable geometry.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is
     * called.
     *
     * @param dc the current draw context.
     */
    protected void makeOrderedRenderable(DrawContext dc)
    {
        // Re-use values already calculated this frame.
        if (dc.getFrameTimeStamp() != this.getCurrentData().getFrameNumber())
        {
            // Regenerate the positions and shape at a specified frequency.
            if (this.mustRegenerateGeometry(dc))
            {
                if (this.getColladaRoot() == null)
                    this.createModel(dc);
                this.getCurrentData().restartTimer(dc);
            }

            this.getCurrentData().setFrameNumber(dc.getFrameTimeStamp());
        }

//          if (!this.isOrderedRenderableValid(dc))
//              return;
//
//          if (dc.isPickingMode())
//              this.pickLayer = dc.getCurrentLayer();

        // --->NOT FOR SCENESHAPES---->      dc.addOrderedRenderable(this);
    }

    /**
     * Draws this shape as an ordered renderable.
     * <p/>
     * A {@link gov.nasa.worldwind.render.AbstractShape.AbstractShapeData} must be current when this method is called.
     *
     * @param dc the current draw context.
     */

    @Override
    protected void drawOrderedRenderable(DrawContext dc)
    {
        // nothing to draw- scene shapes current delegate to node shapes
    }

    public Position getReferencePosition()
    {
        if (colladaRoot.get() == null)
        {
            if (modelPosition != null)
            {
                return modelPosition;
            }

            return null;
        }
        else
        {
            return colladaRoot.get().position;
        }
    }

    /**
     * Opens the Collada model, parses it and loads it into memory. The {@link FileStore} associated with this shape
     * resolves the model file location and retrieves the model if it is remote and not locally cached.
     *
     * @param dc the current draw context.
     */
    protected void createModel(DrawContext dc) // TODO: remove arg if it turns out to be unnecessary
    {
        this.requestModel();
    }

    /**
     * Thread's off a task to determine whether the model is local or remote and then retrieves it either from disk
     * cache or a remote server.
     */
    protected void requestModel()
    {
        if (WorldWind.getTaskService().isFull())
            return;

        WorldWind.getTaskService().addTask(new RequestTask(this));
    }

    /** Attempts to find this shape's model file locally, and if that fails attempts to find it remotely. */
    protected static class RequestTask implements Runnable
    {
        /** The shape associated with this request. */
        protected final ColladaSceneShape shape;

        /**
         * Construct a request task for a specified shape.
         *
         * @param shape the shape for which to construct the request task.
         */
        protected RequestTask(ColladaSceneShape shape)
        {
            if (shape == null)
            {
                String message = Logging.getMessage("nullValue.Shape");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }

            this.shape = shape;
        }

        public void run()
        {
            if (Thread.currentThread().isInterrupted())
                return; // the task was cancelled because it's a duplicate or for some other reason

            try
            {
                String fullmodelAddress = this.shape.pathResolver.resolveFilePath(this.shape.modelAddress);
                URL fileUrl = this.shape.fileStore.requestFile(fullmodelAddress);

                if (fileUrl != null)
                {
                    if (this.shape.loadModel(fileUrl))
                    {
                        this.shape.firePropertyChange(AVKey.LAYER, null, this);
                        return;
                    }
                }
            }
            catch (IOException e)
            {
                String message = Logging.getMessage("nullValue.Shape");
                Logging.logger().severe(message);
                throw new IllegalArgumentException(message);
            }
        }

        public boolean equals(Object o)
        {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            final RequestTask that = (RequestTask) o;

            return !(this.shape != null ? !this.shape.equals(that.shape) : that.shape != null);
        }

        public int hashCode()
        {
            return (this.shape != null ? this.shape.hashCode() : 0);
        }

        public String toString()
        {
            return this.shape.getModelAddress();
        }
    }

    /**
     * Loads the model from disk into memory. If successful, the root of the model is available via {@link
     * #getColladaRoot()}.
     *
     * @param modelUrl the URL of the model file.
     *
     * @return true if the model was successfully loaded, otherwise false.
     */
    protected boolean loadModel(URL modelUrl)
    {
        ColladaRoot root;

        synchronized (this.fileLock)
        {
            root = readModel(modelUrl);
        }

        if (root != null)
            this.setColladaRoot(root);

        return this.getColladaRoot() != null;
    }

    /**
     * Reads and returns a Collada model at a specified URL.
     *
     * @param modelUrl the URL of the model to read.
     *
     * @return ColladaRoot the parsed Collada model.
     */
    protected ColladaRoot readModel(
        URL modelUrl)       // @todo size - should we really keep hold of ColladaRoot after we've parsed?  Maybe we free it since our parsed format is so small?
    {
        try
        {
            File file = new File(modelUrl.getFile());
            boolean fileExists = file.exists();

            if (fileExists && WWIO.isContentType(file, KMLConstants.COLLADA_MIME_TYPE))
            {
                final ColladaRoot refRoot = new ColladaRoot(file);
                try
                {
                    refRoot.parse();
                    WorldWind.getSessionCache().put(modelUrl, refRoot);

                    AbstractList<ColladaNodeShape> nodes = refRoot.createScene(ColladaSceneShape.this);
                    for (ColladaNodeShape node : nodes)
                    {
                        colladaShapes.add(node);
                    }
                }
                finally
                {
                    try
                    {
                        refRoot.closeStream();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Logging.logger().log(java.util.logging.Level.SEVERE,
                            "error closing stream in ColladaSceneShape", e);
                    }
                }

                return refRoot;
            }

            return null;
        }
        catch (Exception e)
        {
            String msg = Logging.getMessage("layers.TextureLayer.ExceptionAttemptingToReadTextureFile", modelUrl);
            Logging.logger().log(java.util.logging.Level.SEVERE, msg, e);

            return null;
        }
    }

    @Override
    public List<Intersection> intersect(Line line, Terrain terrain) throws InterruptedException // TODO
    {
        // TODO: See Polygon and/or ExtrudedPolygon for an implementation example

        return null;
    }
}
