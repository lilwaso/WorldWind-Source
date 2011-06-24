/* Copyright (C) 2001, 2011 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.ogc.collada;

import gov.nasa.worldwind.cache.GpuResourceCache;
import gov.nasa.worldwind.render.DrawContext;

import java.util.*;

/**
 * Beginnings of cachable sharable buffer storage. -- They are now purging and as they are used, we round robin through
 * buffers and handle purging from either space needs or because the GPU VBO Buffer Cache purged us
 * <p/>
 * We are going to need to take a few steps yet before this is ready for public consumption- and there are several
 * opportunities to hide implementation complexity and save us from bugs.   For example , the lowest level is sort of
 * byte level moving, the middle is managing chunks which must have the same stride in bytes for elements, and the
 * highest level is rendering with the consituent pieces.
 * <p/>
 * Also, we can use this stuff for vertex arrays too- which simplifies calling patterns from its clients. In that case
 * it can hand out chunks of data which are equivalent to the interleaved vertex array storage that the client would
 * make itself, and doesn't share things between bigger chunks.   Given a clean API to this functionality, this should
 * save us several types of bugs- but we must make sure its easy to : 1) setup a VBOElementRenderer for a particular
 * usage pattern 2) diagnose rendering bugs with clients 3) purge using the GPU cache
 *
 * @author ${USER}
 * @version $Id: UnifiedBuffer.java 15548 2011-06-03 23:24:39Z tgaskins $
 */

public class UnifiedBuffer
{
    static boolean DEBUG_USE_VERTEX_ARRAY = false;

    // weird value- this is max cell size where index is still valid using unsigned shorts (which are chars in java)
    // 5 is the current element stride in floats, so max cell size is the maximum size of a container
    // of 5 float elements that can be indexed by an unsigned short (a java character)

    protected final static int cellSizeInFloats = Character.MAX_VALUE * 5;
    protected static int kNumberOfCells = 10;

    protected static UnifiedBuffer sInstance = null;

    protected AbstractList<UnifiedBufferStorageChunk> storageCells = new ArrayList<UnifiedBufferStorageChunk>();
    protected boolean vBOsActive;
    protected GpuResourceCache gpuResourceCache;

    @Override
    public String toString()
    {
        return "UnifiedBuffer # of cells :"
            + storageCells.size();    //To change body of overridden methods use File | Settings | File Templates.
    }

    static protected UnifiedBuffer GetSharedInstance(DrawContext dc)
    {
        if (UnifiedBuffer.sInstance == null)
        {
            UnifiedBuffer.sInstance = new UnifiedBuffer(dc);
        }

        return UnifiedBuffer.sInstance;
    }

    /**
     * The factory method to get a renderer that fronts the UnifiedBuffer sub system
     *
     * @param dc                      the current DrawContext
     * @param elementType             the GL element being rendered
     * @param sizeInVertices          the size of each element in vertices
     * @param inPositionCoordsPerVert xyz coords per vert  (ie, 1 could be x, 2 could be xy, 3 could be xyz
     * @param inTexCoordsPerVert      texture coords per vert
     * @param inPerVertexSizeInFloats stride or size of element in floats
     *
     * @return returns a renderer setup for request
     */

    static public VBOElementRenderer GetVBOElementRenderer(DrawContext dc, int elementType, int sizeInVertices,
        int inPositionCoordsPerVert,
        int inTexCoordsPerVert, int inPerVertexSizeInFloats)
    {
        return UnifiedBuffer.GetSharedInstance(dc).getUnifiedBufferClient(elementType, sizeInVertices,
            inPositionCoordsPerVert, inTexCoordsPerVert, inPerVertexSizeInFloats);
    }

    protected VBOElementRenderer getUnifiedBufferClient(int elementType, int sizeInVertices,
        int inPositionCoordsPerVert, int inTexCoordsPerVert, int inPerVertexSizeInFloats)
    {
        return new VBOElementRenderer(this, elementType, sizeInVertices, inPositionCoordsPerVert, inTexCoordsPerVert,
            inPerVertexSizeInFloats);
    }

    protected UnifiedBuffer(DrawContext dc)
    {
        vBOsActive = !DEBUG_USE_VERTEX_ARRAY && dc.getGLRuntimeCapabilities().isUseVertexBufferObject();
        gpuResourceCache = dc.getGpuResourceCache();
    }

    /**
     * Simple version of getAvailableStorage- used when Vertex Arrays are active, for example
     *
     * @param requestSizeInFloats request size in floats
     *
     * @return returns a chunk with space available for request.
     */

    UnifiedBufferStorageChunk getAvailableStorage_USE_PERINSTANCE_STORAGE(
        int requestSizeInFloats)  // <-- can use for VertexArrrays
    {
        UnifiedBufferStorageChunk cell = new UnifiedBufferStorageChunk(requestSizeInFloats);
        this.storageCells.add(cell);
        return cell;
    }

    /**
     * Does everything necessary to get available storage for request. - may make space and purge chunks to fulfil
     * request
     *
     * @param dc                  the current DrawContext
     * @param client              the client making the request
     * @param requestSizeInFloats request size in floats
     *
     * @return returns a chunk with space available for request.
     */

    UnifiedBufferStorageChunk getAvailableStorage(DrawContext dc,
        UnifiedBufferStorageChunk.UnifiedBufferStorageChunkClient client, int requestSizeInFloats)
    {
        if (!this.vBOsActive)   // just make a chunk per request when not using VBO's.  Models normal Vertex Array usage.
        {
            return getAvailableStorage_USE_PERINSTANCE_STORAGE(requestSizeInFloats);
        }
        else
        {
            for (UnifiedBufferStorageChunk storageCell : storageCells)
            {
                if (storageCell.hasSpace(requestSizeInFloats))
                {
                    storageCell.addClient(client);
                    return storageCell;
                }
            }

            if (this.storageCells.size()
                < kNumberOfCells) // @todo add max cells, or max space before compact and free cells
            {
                UnifiedBufferStorageChunk cell = new UnifiedBufferStorageChunk(UnifiedBuffer.cellSizeInFloats);
                this.storageCells.add(cell);
                return cell;
            }

            return this.makeSpace(dc, requestSizeInFloats);
        }
    }

    /**
     * make space by purging first cell and placing it back on end of list.
     *
     * @param dc          the current DrawContext
     * @param requestSize request size in floats
     *
     * @return returns a chunk with space available for request.
     *
     * @todo Seems like later you should purge from back, and search for empty from front? -- (so oldest in back, and
     * you find space quicker because fresh cells are in front of list)
     */

    protected UnifiedBufferStorageChunk makeSpace(DrawContext dc, int requestSize)
    {
        UnifiedBufferStorageChunk storageCell = storageCells.remove(0);

        storageCell.purge(dc);
        storageCells.add(storageCell);

        return storageCell;
    }
}
