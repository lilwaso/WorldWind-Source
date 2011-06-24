package gov.nasa.worldwind.ogc.collada;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.GL;
import java.nio.*;

/**
 * Class used for rendering with UnifiedBuffer objects. This class is not quite ready for public consumption, but
 * getting close. Usage: vboElementRenderer = UnifiedBuffer.GetVBOElementRenderer( dc, GL.GL_TRIANGLES, vertsPerTri *
 * numberOfElements, positionCoordsPerVert, texCoordsPerVert, totalDataSizePerVertexInFloats);
 *
 * @author jfb
 * @version $ID$
 */

public class VBOElementRenderer
    extends UnifiedBufferStorageChunk.UnifiedBufferStorageChunkClient
{
    protected static final int byteSizeFloat = Float.SIZE / Byte.SIZE;
    protected static final int byteSizeChar = Character.SIZE / Byte.SIZE;
    protected static ByteBuffer indexBuffer;

    protected Object chunkVBOCacheKey = new Object();

    protected int offsetInFloats;
    protected int offsetInVerts;
    protected int lengthInVertices;
    protected int vertexSizeInBytes;
    protected int vertexSizeInFloats;
    protected int elementType = GL.GL_TRIANGLES;
    protected int positonCoordsPerVert = ColladaNodeShape.positionCoordsPerVert;
    protected int textureCoordsPerVert = ColladaNodeShape.texCoordsPerVert;
    protected int vertsPerElement = 3;
    protected int currentlyBoundVBO = -1;

    protected UnifiedBuffer unifiedBuffer;

    VBOElementRenderer(UnifiedBuffer unifiedBuffer, int elementType, int sizeInVertices, int inPositionCoordsPerVert,
        int inTexCoordsPerVert, int inPerVertexSizeInFloats)
    {
        this.unifiedBuffer = unifiedBuffer;
        this.vertexSizeInFloats = inPerVertexSizeInFloats;
        this.vertexSizeInBytes = vertexSizeInFloats * byteSizeFloat;
        this.elementType = elementType;
        this.positonCoordsPerVert = inPositionCoordsPerVert;
        this.textureCoordsPerVert = inTexCoordsPerVert;
        this.lengthInVertices = sizeInVertices;
    }

    @Override
    public String toString()
    {
        return "VBOElementRenderer " + offsetInFloats + " size in verts : " + lengthInVertices;
    }

    /**
     * For batch rendering, this can be called at the beginning of rendering
     *
     * @param gl the current GLContext
     */

    public void beginDrawing(GL gl)
    {
        currentlyBoundVBO = -1;
        UnifiedBufferStorageChunk.ResetBindings();
    }

    /**
     * For batch rendering, this can be called at the beginning of rendering
     *
     * @param gl the current GLContext
     */
    public void endDrawing(GL gl)
    {
        currentlyBoundVBO = -1;
        UnifiedBufferStorageChunk.ResetBindings();

        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * binds the VBO's necessary to draw
     *
     * @param dc the current DrawContext
     *
     * @return returns true if we bound the buffer
     */

    protected boolean bindVBOs(DrawContext dc)
    {
        GL gl = dc.getGL();

        if (this.chunk.bindVBOBuffer(dc))
        {
            gl.glTexCoordPointer(textureCoordsPerVert, GL.GL_FLOAT, vertexSizeInBytes,
                0); // <- last param is buffer offset
            gl.glVertexPointer(positonCoordsPerVert, GL.GL_FLOAT, vertexSizeInBytes,
                2 * byteSizeFloat); // <- last param is buffer offset
        }

        int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.getVboCacheKey());
        if (vboIds != null)
        {
            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[0]);
            return true;
        }
        else
        {
            System.err.println("NULL VBOS-  purge bug?");
        }

        return false;
    }

    Object getVboCacheKey()
    {
        return chunkVBOCacheKey;
    }

    /**
     * Draws the elements placed in this object using Vertex Arrays
     *
     * @param gl the current GLContext
     */
    public void drawWithVertexArray(GL gl)
    {
        gl.glTexCoordPointer(textureCoordsPerVert, GL.GL_FLOAT, vertexSizeInBytes,
            this.chunk.coordsUploadBuffer.rewind());

        this.chunk.coordsUploadBuffer.rewind();

        // last param is buffer offset for interleaved array (TTVVV)
        gl.glVertexPointer(positonCoordsPerVert, GL.GL_FLOAT, vertexSizeInBytes,
            this.chunk.coordsUploadBuffer.position(2));
        gl.glDrawArrays(elementType, 0, lengthInVertices);
    }

    /**
     * Draws the elements placed in this object with VBOs (by calling glDrawRangeElements) after making sure the proper
     * buffers are bound
     *
     * @param dc the current DrawContext
     */

    public void drawWithVBO(DrawContext dc)
    {
        if (bindVBOs(dc))
        {
            GL gl = dc.getGL();
            gl.glDrawRangeElements(elementType, this.offsetInFloats, this.offsetInFloats + this.lengthInVertices,
                this.lengthInVertices, GL.GL_UNSIGNED_SHORT, 0);   //The starting point of the IBO
            //gl.glDrawElements(elementType, this.lengthInVertices, GL.GL_UNSIGNED_SHORT, 0);   //The starting point of the IBO
        }
    }

    /**
     * client code that uses this renderer calls to obtain a buffer to fill with coords to render
     *
     * @param dc the current DrawContext
     *
     * @return returns a coord buffer to fill with XYZ, etc.
     */

    public FloatBuffer beginDataPush(DrawContext dc)
    {
        if (this.chunk == null)
        {
            int requestSizeInFloats = lengthInVertices * vertexSizeInFloats;
            this.chunk = unifiedBuffer.getAvailableStorage(dc, this,
                requestSizeInFloats);   // move this to later binding in push, and then be able to redo it when gpucahce is flushed
            if (this.chunk != null)
            {
                this.offsetInFloats = chunk.reserveSpaceForClient(requestSizeInFloats);
                this.offsetInVerts = this.offsetInFloats / vertexSizeInFloats;
            }
            else
            {
                this.chunk = unifiedBuffer.getAvailableStorage(dc, this,
                    requestSizeInFloats);   // move this to later binding in push, and then be able to redo it when gpucahce is flushed
            }
        }
        else
        {
            if (chunkSeed != chunk.vboSeed)
            {
                int requestSizeInFloats = lengthInVertices * vertexSizeInFloats;
                this.offsetInFloats = chunk.reserveSpaceForClient(requestSizeInFloats);
                this.offsetInVerts = this.offsetInFloats / vertexSizeInFloats;
                chunkSeed = chunk.vboSeed;
            }
        }

        return (FloatBuffer) chunk.coordsUploadBuffer.rewind();
    }

    /**
     * client code that uses this renderer calls this when its done filling the buffer it obtained from beginDataPush so
     * that we can push the data to the GPU by calling our internal bufferSubData caches that are now invalid.
     *
     * @param dc      the current DrawContext
     * @param useVBOs this is so we can in modes where we dont actually make vbos, either for old GPUs or for testing
     */

    public void endDataPush_SynchToGPU(DrawContext dc, boolean useVBOs)
    {
        if (useVBOs)
        {
            if (!ColladaNodeShape.DEBUG_USE_IMMEDIATE_MODE)
            {
                GL gl = dc.getGL();

                try
                {
                    this.bufferSubData(dc, this.offsetInFloats * byteSizeFloat,
                        this.lengthInVertices * vertexSizeInBytes);
                    this.setupIndexBuffer(dc);
                }
                finally
                {
                    UnifiedBufferStorageChunk.ResetBindings();

                    gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER,
                        0);  // during data push we are resetting current VBO alot.
                    gl.glBindBuffer(GL.GL_ARRAY_BUFFER,
                        0);         // a batch like push would be cool, as we do in rendering..
                }
            }
        }
    }

    /**
     * a call back that gets called by the chunk if the chunk has been purged and we need to clear caches that are now
     * invalid.
     *
     * @param unifiedBufferStorageChunk the chunk that has just been purged.
     * @param dc                        the current DrawContext
     */

    @Override
    public void chunkPurged(UnifiedBufferStorageChunk unifiedBufferStorageChunk, DrawContext dc)
    {
        super.chunkPurged(unifiedBufferStorageChunk, dc);

        // remove index buffer from cache, since if the mother cache died, the indices are bad.
        dc.getGpuResourceCache().remove(this.getVboCacheKey());
    }

    /**
     * sets up the index buffer for the renderer.  This involves filling the index buffer with offsets into the chunk
     * buffer so that later glDrawElements or glDrawRangeElements can be called
     *
     * @param dc the current DrawContext
     */

    public void setupIndexBuffer(DrawContext dc)
    {
        int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.getVboCacheKey());

        if (vboIds == null)
        {
            GL gl = dc.getGL();
            vboIds = new int[1];
            gl.glGenBuffers(vboIds.length, vboIds, 0);
            dc.getGpuResourceCache().put(this.getVboCacheKey(), vboIds, GpuResourceCache.VBO_BUFFERS,
                lengthInVertices * byteSizeChar);

            if (indexBuffer == null || indexBuffer.limit() < lengthInVertices * byteSizeChar)
            {
                indexBuffer = BufferUtil.newByteBuffer(this.lengthInVertices * byteSizeChar
                    * 2);   // times two for push buffer, so we tend to re-allocate this less by overestimating what we need
            }

            indexBuffer.rewind();
            int start = this.offsetInVerts;
            for (int i = 0; i < lengthInVertices;
                i++)   // @TODO PERF may be able to just use one unified index buffer and use a buffer offset and length in drawElements
            {
                char index = (char) (i + start);
                indexBuffer.putChar(index); // use chars for speed and scalability  (equivalent to unsigned shorts) -
            }

            gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, vboIds[0]);

            gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, byteSizeChar * lengthInVertices, null, GL.GL_STATIC_DRAW);
            gl.glBufferSubData(GL.GL_ELEMENT_ARRAY_BUFFER, 0, byteSizeChar * lengthInVertices, indexBuffer.rewind());
        }
    }

    /**
     * checks to see if the vbo for our index buffer is ready, and then asks the chunk to see if its vbo is ready as
     * well.
     *
     * @param dc the current DrawContext
     *
     * @return returns true if vbo is ready
     */

    public boolean vboReady(DrawContext dc)
    {
        if (this.chunk == null)
            return false;

        boolean indexReady = (dc.getGpuResourceCache().get(this.getVboCacheKey()) != null);
        boolean chunkReady = this.chunk.vboReady(dc);

        return indexReady && chunkReady;
    }
}
