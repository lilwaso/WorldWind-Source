/*
Copyright (C) 2001, 2010 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render;

import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.util.*;

import java.util.*;

/**
 * @author dcollins
 * @version $Id: SurfaceQuad.java 9992 2009-04-08 04:38:54Z dcollins $
 */
public class SurfaceQuad extends AbstractSurfaceShape
{
    protected LatLon center = LatLon.ZERO;
    protected double width;
    protected double height;
    protected Angle heading = Angle.ZERO;

    /**
     * Constructs a new surface quad with the default attributes, default center location, default dimensions, and
     * default heading.
     */
    public SurfaceQuad()
    {
    }

    /**
     * Constructs a new surface quad with the specified normal (as opposed to highlight) attributes, default center
     * location, default dimensions, and default heading. Modifying the attribute reference after calling this
     * constructor causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     */
    public SurfaceQuad(ShapeAttributes normalAttrs)
    {
        super(normalAttrs);
    }

    /**
     * Constructs a new surface quad with the default attributes, the specified center location and dimensions (in
     * meters).
     *
     * @param center the quad's center location.
     * @param width  the quad's width, in meters.
     * @param height the quad's height, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the width or height are negative.
     */
    public SurfaceQuad(LatLon center, double width, double height)
    {
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.width = width;
        this.height = height;
    }

    /**
     * Constructs a new surface quad with the default attributes, the specified center location, dimensions (in meters),
     * and heading clockwise from North.
     *
     * @param center  the quad's center location.
     * @param width   the quad's width, in meters.
     * @param height  the quad's height, in meters.
     * @param heading the quad's heading, clockwise from North.
     *
     * @throws IllegalArgumentException if the center or heading are null, or if the width or height are negative.
     */
    public SurfaceQuad(LatLon center, double width, double height, Angle heading)
    {
        this(center, width, height);

        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heading = heading;
    }

    /**
     * Constructs a new surface quad with the specified normal (as opposed to highlight) attributes, the specified
     * center location and dimensions (in meters). Modifying the attribute reference after calling this constructor
     * causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the quad's center location.
     * @param width       the quad's width, in meters.
     * @param height      the quad's height, in meters.
     *
     * @throws IllegalArgumentException if the center is null, or if the width or height are negative.
     */
    public SurfaceQuad(ShapeAttributes normalAttrs, LatLon center, double width, double height)
    {
        super(normalAttrs);

        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.width = width;
        this.height = height;
    }

    /**
     * Constructs a new surface quad with the specified normal (as opposed to highlight) attributes, the specified
     * center location and dimensions (in meters). Modifying the attribute reference after calling this constructor
     * causes this shape's appearance to change accordingly.
     *
     * @param normalAttrs the normal attributes. May be null, in which case default attributes are used.
     * @param center      the quad's center location.
     * @param width       the quad's width, in meters.
     * @param height      the quad's height, in meters.
     * @param heading     the quad's heading, clockwise from North.
     *
     * @throws IllegalArgumentException if the center or heading are null, or if the width or height are negative.
     */
    public SurfaceQuad(ShapeAttributes normalAttrs, LatLon center, double width, double height, Angle heading)
    {
        this(normalAttrs, center, width, height);

        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heading = heading;
    }

    public LatLon getCenter()
    {
        return this.center;
    }

    public void setCenter(LatLon center)
    {
        if (center == null)
        {
            String message = Logging.getMessage("nullValue.CenterIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.center = center;
        this.onShapeChanged();
    }

    public double getWidth()
    {
        return this.width;
    }

    public double getHeight()
    {
        return this.height;
    }

    public void setWidth(double width)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.width = width;
        this.onShapeChanged();
    }

    public void setHeight(double height)
    {
        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.height = height;
        this.onShapeChanged();
    }

    public void setSize(double width, double height)
    {
        if (width < 0)
        {
            String message = Logging.getMessage("Geom.WidthIsNegative", width);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (height < 0)
        {
            String message = Logging.getMessage("Geom.HeightIsNegative", height);
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.width = width;
        this.height = height;
        this.onShapeChanged();
    }

    public Angle getHeading()
    {
        return this.heading;
    }

    public void setHeading(Angle heading)
    {
        if (heading == null)
        {
            String message = Logging.getMessage("nullValue.HeadingIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        this.heading = heading;
        this.onShapeChanged();
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Overridden to include the globe's state key in the returned state key.
     *
     * @see gov.nasa.worldwind.globes.Globe#getStateKey(DrawContext)
     */
    @Override
    public Object getStateKey(DrawContext dc)
    {
        // Store a copy of the active attributes to insulate the key from changes made to the shape's active attributes.
        return new SurfaceShapeStateKey(this.getUniqueId(), this.lastModifiedTime, this.getActiveAttributes().copy(),
            dc.getGlobe().getStateKey(dc));
    }

    public Position getReferencePosition()
    {
        return new Position(this.center, 0);
    }

    protected void doMoveTo(Position oldReferencePosition, Position newReferencePosition)
    {
        Angle heading = LatLon.greatCircleAzimuth(oldReferencePosition, this.center);
        Angle pathLength = LatLon.greatCircleDistance(oldReferencePosition, this.center);
        this.setCenter(LatLon.greatCircleEndPosition(newReferencePosition, heading, pathLength));
    }

    public Iterable<? extends LatLon> getLocations(Globe globe)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        if (this.width == 0 && this.height == 0)
            return null;

        double hw = this.width / 2.0;
        double hh = this.height / 2.0;
        double globeRadius = globe.getRadiusAt(this.center.getLatitude(), this.center.getLongitude());
        double distance = Math.sqrt(hw * hw + hh * hh);
        double pathLength = distance / globeRadius;

        double[] cornerAngles = new double[]
            {
                Math.atan2(-hh, -hw),
                Math.atan2(-hh, hw),
                Math.atan2(hh, hw),
                Math.atan2(hh, -hw),
                Math.atan2(-hh, -hw),
            };

        LatLon[] locations = new LatLon[cornerAngles.length];

        for (int i = 0; i < cornerAngles.length; i++)
        {
            double azimuth = (Math.PI / 2.0) - (cornerAngles[i] - this.heading.radians);
            locations[i] = LatLon.greatCircleEndPosition(this.center, azimuth, pathLength);
        }

        return java.util.Arrays.asList(locations);
    }

    protected List<List<LatLon>> createGeometry(Globe globe, SurfaceTileDrawContext sdc)
    {
        if (globe == null)
        {
            String message = Logging.getMessage("nullValue.GlobeIsNull");
            Logging.logger().severe(message);
            throw new IllegalArgumentException(message);
        }

        Iterable<? extends LatLon> originalLocations = this.getLocations(globe);
        if (originalLocations == null)
            return null;

        ArrayList<LatLon> drawLocations = new ArrayList<LatLon>();
        double edgeIntervalsPerDegree = this.computeEdgeIntervalsPerDegree(sdc);
        this.generateIntermediateLocations(originalLocations, edgeIntervalsPerDegree, false, drawLocations);

        ArrayList<List<LatLon>> geom = new ArrayList<List<LatLon>>();
        geom.add(drawLocations);

        return geom;
    }

    //**************************************************************//
    //******************** Restorable State  ***********************//
    //**************************************************************//

    protected void doGetRestorableState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doGetRestorableState(rs, context);

        rs.addStateValueAsLatLon(context, "center", this.getCenter());
        rs.addStateValueAsDouble(context, "width", this.getWidth());
        rs.addStateValueAsDouble(context, "height", this.getHeight());
        rs.addStateValueAsDouble(context, "headingDegrees", this.getHeading().degrees);
    }

    protected void doRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.doRestoreState(rs, context);

        LatLon ll = rs.getStateValueAsLatLon(context, "center");
        if (ll != null)
            this.setCenter(ll);

        Double d = rs.getStateValueAsDouble(context, "width");
        if (d != null)
            this.setWidth(d);

        d = rs.getStateValueAsDouble(context, "height");
        if (d != null)
            this.setHeight(d);

        d = rs.getStateValueAsDouble(context, "headingDegrees");
        if (d != null)
            this.setHeading(Angle.fromDegrees(d));
    }

    protected void legacyRestoreState(RestorableSupport rs, RestorableSupport.StateObject context)
    {
        super.legacyRestoreState(rs, context);

        // Previous versions of SurfaceQuad used half-width and half-height properties. We are now using standard
        // width and height, so these restored values must be converted.
        Double width = rs.getStateValueAsDouble(context, "halfWidth");
        Double height = rs.getStateValueAsDouble(context, "halfHeight");
        if (width != null && height != null)
            this.setSize(2 * width, 2 * height);

        // This property has not changed since the previos version, but it's shown here for reference.
        //LatLon center = rs.getStateValueAsLatLon(context, "center");
        //if (center != null)
        //    this.setCenter(center);

        Double od = rs.getStateValueAsDouble(context, "orientationDegrees");
        if (od != null)
            this.setHeading(Angle.fromDegrees(od));
    }
}
