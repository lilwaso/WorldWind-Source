/*
Copyright (C) 2001, 2010 United States Government as represented by 
the Administrator of the National Aeronautics and Space Administration. 
All Rights Reserved. 
*/
package gov.nasa.worldwind.util.webview;

import com.sun.opengl.util.BufferUtil;
import com.sun.opengl.util.texture.*;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.util.Logging;

import javax.media.opengl.GL;
import java.awt.*;
import java.util.logging.Level;

/**
 * @author dcollins
 * @version $Id: WebViewTexture.java 14326 2010-12-28 05:01:19Z dcollins $
 */
public class WebViewTexture extends BasicWWTexture
{
    protected Dimension frameSize;
    protected boolean flipVertically;

    public WebViewTexture(Dimension frameSize, boolean useMipMaps, boolean flipVertically)
    {
        // Create a new unique object to use as the cache key.
        super(new Object(), useMipMaps); // Do not generate mipmaps for the texture.
        
        this.frameSize = frameSize;
        this.flipVertically = flipVertically;
    }

    @Override
    public boolean bind(DrawContext dc)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        boolean isBound = super.bind(dc);

        if (isBound)
        {
            this.updateIfNeeded(dc);
        }

        return isBound;
    }

    @Override
    protected Texture initializeTexture(DrawContext dc, Object imageSource)
    {
        if (dc == null)
        {
            String message = Logging.getMessage("nullValue.DrawContextIsNull");
            Logging.logger().severe(message);
            throw new IllegalStateException(message);
        }

        if (this.textureInitializationFailed)
            return null;

        Texture t;
        try
        {
            // Allocate a texture with the proper dimensions and texture internal format, but with no data.
            TextureData td = new TextureData(
                GL.GL_RGBA, // texture internal format
                this.frameSize.width, // texture image with
                this.frameSize.height, // texture image height
                0, // border
                GL.GL_RGBA, // pixelFormat
                GL.GL_UNSIGNED_BYTE, // pixelType
                false, // mipmap
                false, // dataIsCompressed
                this.flipVertically,
                BufferUtil.newByteBuffer(4 * this.frameSize.width * this.frameSize.height), // buffer
                null); // flusher
            t = TextureIO.newTexture(td);

            dc.getTextureCache().put(imageSource, t);
            t.bind();

            // Configure the texture to use nearest-neighbor filtering. This ensures that the texels are aligned exactly
            // with screen pixels, and eliminates blurry artifacts from linear filtering.
            GL gl = dc.getGL();
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP_TO_BORDER);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP_TO_BORDER);
        }
        catch (Exception e)
        {
            // TODO: refactor as generic.ExceptionDuringTextureInitialization
            String message = Logging.getMessage("generic.IOExceptionDuringTextureInitialization");
            Logging.logger().log(Level.SEVERE, message, e);
            this.textureInitializationFailed = true;
            return null;
        }

        this.width = t.getWidth();
        this.height = t.getHeight();
        this.texCoords = t.getImageTexCoords();

        return t;
    }

    protected void updateIfNeeded(DrawContext dc)
    {
    }
}
