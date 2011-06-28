/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.cache.*;

import javax.media.opengl.GLAutoDrawable;

/**
 * @author tag
 * @version $Id: WorldWindowGLDrawable.java 14563 2011-01-26 21:22:00Z tgaskins $
 */
public interface WorldWindowGLDrawable extends WorldWindow
{
    void initDrawable(GLAutoDrawable glAutoDrawable);

    void initGpuResourceCache(GpuResourceCache cache);

    void endInitialization();
}
