/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;

import java.util.List;

/**
 * @author tag
 * @version $Id: SurfaceTile.java 14895 2011-03-05 07:18:57Z tgaskins $
 */
public interface SurfaceTile
{
    boolean bind(DrawContext dc);
    void applyInternalTransform(DrawContext dc, boolean textureIdentityActive);
    Sector getSector();
    Extent getExtent(DrawContext dc);
    List<? extends LatLon> getCorners();
}
