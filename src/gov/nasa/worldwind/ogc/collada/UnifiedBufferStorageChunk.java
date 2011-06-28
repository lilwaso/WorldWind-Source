package gov.nasa.worldwind.ogc.collada;

import com.sun.opengl.util.BufferUtil;
import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.nio.FloatBuffer;
import java.util.ArrayList;

/**
 * A piece of storage used by the UnifiedBuffer VBO system.   Currently in progress, handles purges, typically because
 * of loss of VBO in GPU Resource cache or because of need for more storage after max size reached.
 * @author jfb
 * @version $ID$
 */

final class UnifiedBufferStorageChunk
{
    static final int    byteSizeFloat = Float.SIZE / Byte.SIZE;

    protected static int sUniqueID = 0;
    protected int uniqueID = sUniqueID++;
    protected static int boundVBO = -1;

    protected Object              chunkVBOCacheKey = new String("VBOCache For UnifiedBufferChunk " + this.uniqueID);

    protected int                 lengthInFloats = -1;
    protected int                 lengthInBytes = -1;
    protected int                 nextAvailableDataStart = 0;
    protected int                 vboSeed = 1;

    // Normally, this buffer is for uploads only for VBOs.
    // But its the Main storage for Vertex Array usage-
    protected FloatBuffer         coordsUploadBuffer;
    protected ArrayList<UnifiedBufferStorageChunkClient> clients = new ArrayList<UnifiedBufferStorageChunkClient>();

    UnifiedBufferStorageChunk(int sharedSizeInFloats)
    {
        lengthInFloats          = sharedSizeInFloats;
        lengthInBytes           = sharedSizeInFloats * byteSizeFloat;
        nextAvailableDataStart  = 0;
    }

    static class UnifiedBufferStorageChunkClient
    {
        int                             chunkSeed;
        UnifiedBufferStorageChunk       chunk;

        void bufferSubData(DrawContext dc, int pushOffsetInBytes, int pushLengthInBytes)
        {
            chunkSeed = chunk.bufferSubData(dc,pushOffsetInBytes,pushLengthInBytes);
        }

        public boolean ready()
        {
            return (chunk != null) && (chunkSeed == chunk.vboSeed);
        }

        public void chunkPurged(UnifiedBufferStorageChunk unifiedBufferStorageChunk, DrawContext dc)
        {
            chunkSeed   = 0;
            chunk       = null;
        }
    }

   /**
   * Returns true if VBO is ready
   *
   * @param dc draw context used for buffer cleanup
   * @return returns true if VBO is ready
   */
    boolean vboReady(DrawContext dc)
    {
        int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.getVboCacheKey());
        boolean vboReady = (vboIds != null);

        if (!vboReady)
        {
            this.purge(dc);
        }

        return vboReady;
    }


   /**
   * Purges this chunk,and notifies and removes clients of the purge
   *
   * @param dc draw context used for buffer cleanup
   */

    void purge(DrawContext dc)
    {
//        System.err.println("purge (clients:("+clients.size()+") ------>" + this.uniqueID);
        for (UnifiedBufferStorageChunkClient client : clients)
        {
            client.chunkPurged(this,dc);
        }

        clients.clear();
        nextAvailableDataStart  = 0;

        // remove VBO buffer from cache, since i just emptied things
        dc.getGpuResourceCache().remove(this.getVboCacheKey());

        GL gl = dc.getGL();
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
        gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, 0);

        UnifiedBufferStorageChunk.ResetBindings();
    }

    /**
   * adds client to this chunk
   *
   * @param client client to add
   */
    void addClient(UnifiedBufferStorageChunkClient client)
    {
        clients.add(client);
        this.vboSeed++;
    }

    @Override
    public String toString()
    {
        return "UnifiedBufferStorageChunk size("+this.lengthInFloats+") id: " + this.uniqueID;
    }

    /**
   * Returns true if this chunk has space for this request size in floats
   *
   * @param requestSizeInFloats check
   * @return returns true if space is available
   */

    boolean hasSpace(int requestSizeInFloats)
    {
        return (nextAvailableDataStart +requestSizeInFloats) < lengthInFloats;
    }

    /**
       * Reserves space in this chunk for the client request size
       *
       * @param sizeInFloats size of buffer requested
     *  @return index offset for request
       */

    int reserveSpaceForClient(int sizeInFloats)
    {
        setupCoordsUploadBuffer(sizeInFloats);

        int outIndexInFloats = nextAvailableDataStart;
        nextAvailableDataStart += sizeInFloats;

        return outIndexInFloats;
    }

    /**
       * sets up the upload buffer all clients of this chunk will use to push data to the VBO
       *
       * @param sizeInFloats size of buffer requested
       */

    protected void setupCoordsUploadBuffer(int sizeInFloats)
    {
        int coordsStorageSizeNeeded = sizeInFloats;
        if (coordsUploadBuffer == null || coordsUploadBuffer.limit() < coordsStorageSizeNeeded)
        {
            int allocSize = coordsStorageSizeNeeded;
            // Normally, this buffer is for uploads only for VBOs.
            // But its the Main storage for Vertex Array usage-

            coordsUploadBuffer = BufferUtil.newFloatBuffer(allocSize);
        }
    }

    Object getVboCacheKey()
    {
        return chunkVBOCacheKey;
    }


    /**
      * pumps data to VBO using glBufferSubData, and initializes VBO using glBufferData if necessary
     * (either on init, or after purge)
     * returns change seed for use by clients in tracking
      *
      * @param dc DrawContext
      * @param pushOffsetInBytes     offset in bytes of the data to push to the VBO
      * @param pushLengthInBytes     length in bytes of the data to push to the VBO
     * @return returns vboSeed for monitoring changes in clients
      */

    protected int bufferSubData(DrawContext dc, int pushOffsetInBytes, int pushLengthInBytes)
    {
        GL gl = dc.getGL();

        int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.getVboCacheKey());
        if (vboIds == null)
        {
             vboIds = new int[1];
             gl.glGenBuffers(vboIds.length, vboIds, 0);

            dc.getGpuResourceCache().put(this.getVboCacheKey(), vboIds, GpuResourceCache.VBO_BUFFERS, this.lengthInBytes);

            this.bindVBOBuffer(dc);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, this.lengthInBytes, null,GL.GL_STATIC_DRAW);
            vboSeed++;
        }
        else
        {
            this.bindVBOBuffer(dc);
        }

        gl.glBufferSubData(GL.GL_ARRAY_BUFFER, pushOffsetInBytes, pushLengthInBytes, coordsUploadBuffer.rewind());

        return vboSeed;
    }

     /**
      * Internal to UnifiedBuffer system, for managing rendering state of bound VBO
      */
    static void ResetBindings()
    {
        boundVBO = -1;
    }

    /**
     * Binds the VBO Buffer for this chunk
     *
     * @param dc DrawContext
     * @return  returns true if we were able to bind the buffer
     */

    boolean bindVBOBuffer(DrawContext dc)
    {
        int[] vboIds = (int[]) dc.getGpuResourceCache().get(this.getVboCacheKey());  // how often do we have to call this?

        if (vboIds != null)
        {
            if (boundVBO != vboIds[0])
            {
                GL gl = dc.getGL();
                gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboIds[0]);
                boundVBO = vboIds[0];

                return true;
            }
        }
        else
        {
            Logging.logger().severe("null vbo ids in chunk..." + this.uniqueID);
        }
        return false;
    }
}
