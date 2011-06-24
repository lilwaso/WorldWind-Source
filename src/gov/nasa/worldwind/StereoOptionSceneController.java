/*
Copyright (C) 2001, 2011 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.render.DrawContext;

import javax.media.opengl.*;

/**
 * This scene controller draws in stereo, either red-blue anaglyph or device supported if the display device provides
 * stereo directly. It can also draw without applying stereo. To select stereo, prior to calling this class' constructor
 * set the Java VM property <code>gov.nasa.worldwind.stereo.mode</code> to "device" for device supported stereo (if
 * provided by the device) or "redblue" for red-blue anaglyph stereo. If the property is not set or is any other value,
 * this class does not draw in stereo.
 * <p/>
 * The {@link WorldWindow} instance must support stereo in order to use device-supported stereo. A stereo
 * <code>WorldWindow</code> is selected by specifying the Java VM property described above prior to creating it. See
 * {@link gov.nasa.worldwind.awt.WorldWindowGLCanvas} for further details.
 * <p/>
 * Note: The logic and much of the code here was contributed by Xander Enzmann of Mitre Corporation.
 *
 * @author tag
 * @version $Id: StereoOptionSceneController.java 15537 2011-06-02 17:48:19Z ccrick $
 */
public class StereoOptionSceneController extends BasicSceneController implements StereoSceneController
{
    /**
     * The default focus angle. May be specified in the World Wind configuration file as the
     * <code>gov.nasa.worldwind.StereoFocusAngle</code> property. The default if not specified in the configuration is
     * 1.6 degrees.
     */
    protected static final double DEFAULT_FOCUS_ANGLE = Configuration.getDoubleValue(AVKey.STEREO_FOCUS_ANGLE, 1.6);

    /**
     * The default stereo inter-ocular distance (IOD). May be specified in the World Wind configuration file as the
     * <code>gov.nasa.worldwind.StereoIOD</code> property. The default if not specified in the configuration is 100
     * meters.
     */
    protected static final double DEFAULT_STEREO_IOD =
        Configuration.getDoubleValue(AVKey.STEREO_INTEROCULAR_DISTANCE, 10000.0);

    /** The current stereo mode. May not be set to null; use {@link AVKey#STEREO_MODE_NONE} instead. */
    protected String stereoMode = AVKey.STEREO_MODE_NONE;
    /** The current stereo type. Must be either (@Link AVKey#STEREO_TYPE_TOED_IN) or (@Link AVKey#STEREO_TYPE_PARALLEL) */
    protected String stereoType = AVKey.STEREO_TYPE_TOED_IN;
    /** The angle between eyes. Larger angles give increased 3D effect. */
    protected Angle focusAngle = Angle.fromDegrees(DEFAULT_FOCUS_ANGLE);
    /**
     * The distance between eyes in asymmetric frustum "parallel" axis projection stereo. Larger angles give increased
     * 3D effect.
     */
    protected double interocularDistance = DEFAULT_STEREO_IOD;
    /** Indicates whether left and right eye positions are swapped. */
    protected boolean swapEyes = false;
    /** Indicates the GL drawable capabilities. Non-null only after this scene controller draws once. */
    protected GLCapabilities capabilities;
    /** Indicates whether hardware device stereo is available. Valid only after this scene controller draws once. */
    protected boolean hardwareStereo = false;
    /**
     * Indicates whether stereo is being applied, either because a stereo device is being used or a stereo mode is in
     * effect. This field is included because the question is asked every frame, and tracking the answer via a boolean
     * avoids the overhead of more complicated logic that determines the stereo-drawing implementation to call.
     */
    protected boolean inStereo = false;

    /** Constructs an instance and initializes its stereo mode to */
    public StereoOptionSceneController()
    {
        String stereo = System.getProperty(AVKey.STEREO_MODE);
        String type = System.getProperty(AVKey.STEREO_TYPE);

        if ("redblue".equalsIgnoreCase(stereo))
            this.setStereoMode(AVKey.STEREO_MODE_RED_BLUE);
        else if ("device".equalsIgnoreCase(stereo))
            this.setStereoMode(AVKey.STEREO_MODE_DEVICE);

        if ("toed".equalsIgnoreCase(type))
            this.setStereoType(AVKey.STEREO_TYPE_TOED_IN);
        else if ("parallel".equalsIgnoreCase(type))
            this.setStereoType(AVKey.STEREO_TYPE_PARALLEL);
    }

    public void setStereoMode(String mode)
    {
        this.stereoMode = mode != null ? mode : AVKey.STEREO_MODE_NONE;

        // If device-implemented stereo is used, stereo is considered always in effect no matter what the stereo mode.
        this.inStereo = this.isHardwareStereo() || AVKey.STEREO_MODE_RED_BLUE.equals(this.stereoMode);
    }

    public String getStereoMode()
    {
        return this.stereoMode;
    }

    public void setStereoType(String type)
    {
        this.stereoType = type != null ? type : AVKey.STEREO_TYPE_TOED_IN;
    }

    public String getStereoType()
    {
        return this.stereoType;
    }

    /**
     * {@inheritDoc} The default focus angle is 1.6 degrees.
     *
     * @param a the left-right eye direction difference. If null, the angle is set to 0.
     */
    public void setFocusAngle(Angle a)
    {
        this.focusAngle = a != null ? a : Angle.ZERO;
    }

    public Angle getFocusAngle()
    {
        return this.focusAngle;
    }

    /**
     * {@inheritDoc} The default interocular distance is 1 meters.
     *
     * @param a the distance between the left and right eyes. If null, the distance is set to 100.
     */
    public void setInterocularDistance(Double a)
    {
        this.interocularDistance = a != null ? a : 10000.0;
    }

    public double getInterocularDistance()
    {
        return this.interocularDistance;
    }

    public void setSwapEyes(boolean swapEyes)
    {
        this.swapEyes = swapEyes;
    }

    public boolean isSwapEyes()
    {
        return this.swapEyes;
    }

    public boolean isHardwareStereo()
    {
        return this.hardwareStereo;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * If the display device is providing stereo -- {@link #isHardwareStereo()} is <code>true</code> -- this method
     * returns true even if the stereo mode is {@link AVKey#STEREO_MODE_NONE}. In this case, individual stereo images
     * are drawn for left and right eyes in order to prevent a blurred scene.
     */
    public boolean isInStereo()
    {
        return this.inStereo;
    }

    @Override
    protected void draw(DrawContext dc)
    {
        // Capture the capabilities actually in use.
        if (this.capabilities == null)
        {
            this.capabilities = dc.getGLContext().getGLDrawable().getChosenGLCapabilities();
            this.hardwareStereo = this.capabilities.getStereo();
            this.inStereo = this.isHardwareStereo() ? true : this.isInStereo();
        }

        // If stereo isn't to be applied, just draw and return.
        if (!isInStereo())
        {
            super.draw(dc);
            return;
        }

        if (AVKey.STEREO_MODE_DEVICE.equals(this.stereoMode) && this.isHardwareStereo())
            this.doDrawToStereoDevice(dc);
        else if (AVKey.STEREO_MODE_RED_BLUE.equals(this.stereoMode))
            this.doDrawStereoRedBlue(dc);
        else // AVKey.STEREO_MODE_NONE
            this.doDrawStereoNone(dc);
    }

    /**
     * Implement no stereo ("Mono") while using a stereo device.
     * <p/>
     * Note that this method draws the image twice, once to each of the left and right eye buffers, even when stereo is
     * not in effect. This is to prevent the stereo device from drawing blurred scenes.
     *
     * @param dc the current draw context.
     */
    protected void doDrawStereoNone(DrawContext dc)
    {
        // If running on a stereo device but want to draw a normal image, both buffers must be filled or the
        // display will be blurry.

        GL gl = dc.getGL();

        gl.glDrawBuffer(GL.GL_BACK_LEFT);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        super.draw(dc);

        gl.glDrawBuffer(GL.GL_BACK_RIGHT);
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        super.draw(dc);
    }

    /**
     * Implement stereo using the red-blue anaglyph technique.
     *
     * @param dc the current draw context.
     */
    protected void doDrawStereoRedBlue(DrawContext dc)
    {
        GL gl = dc.getGL();
        View dcView = dc.getView();

        // Draw the left eye
        if (this.isSwapEyes())
        {
            if (this.isHardwareStereo())
                gl.glDrawBuffer(GL.GL_BACK_RIGHT);
            gl.glColorMask(false, true, true, true); // right eye in green/blue
        }
        else
        {
            if (this.isHardwareStereo())
                gl.glDrawBuffer(GL.GL_BACK_LEFT);
            gl.glColorMask(true, false, false, true); // left eye in red only
        }

        if (this.isHardwareStereo())
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        super.draw(dc);

        // Move the view to the right eye
        Angle viewHeading = dcView.getHeading();
        dcView.setHeading(dcView.getHeading().subtract(this.getFocusAngle()));
        dcView.apply(dc);

        // Draw the right eye frame green and blue only
        try
        {
            gl.glClear(GL.GL_DEPTH_BUFFER_BIT);
            if (this.isSwapEyes())
            {
                if (this.isHardwareStereo())
                    gl.glDrawBuffer(GL.GL_BACK_RIGHT);
                gl.glColorMask(true, false, false, true); // right eye in red only
            }
            else
            {
                if (this.isHardwareStereo())
                    gl.glDrawBuffer(GL.GL_BACK_LEFT);
                gl.glColorMask(false, true, true, true);  // right eye in green/blue
            }

            if (this.isHardwareStereo())
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

            super.draw(dc);
        }
        finally
        {
            // Restore the original view heading
            dcView.setHeading(viewHeading);
            dcView.apply(dc);
            gl.glColorMask(true, true, true, true);
        }
    }

    /**
     * Implement stereo using the stereo-enabled graphics device. The mode has an effect only if the display device
     * implements stereo.
     *
     * @param dc the current draw context.
     */
    protected void doDrawToStereoDevice(DrawContext dc)
    {
        GL gl = dc.getGL();
        View dcView = dc.getView();

        // Draw the left eye
        if (this.isSwapEyes())
            gl.glDrawBuffer(GL.GL_BACK_RIGHT);
        else
            gl.glDrawBuffer(GL.GL_BACK_LEFT);

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        super.draw(dc);

        // Move the view to the right eye
        Angle viewHeading = dcView.getHeading();
        dcView.setHeading(dcView.getHeading().subtract(this.getFocusAngle()));
        dcView.apply(dc);

        // Draw the right eye
        try
        {
            if (this.isSwapEyes())
                gl.glDrawBuffer(GL.GL_BACK_LEFT);
            else
                gl.glDrawBuffer(GL.GL_BACK_RIGHT);

            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            super.draw(dc);
        }
        finally
        {
            // Restore the original view heading
            dcView.setHeading(viewHeading);
            dcView.apply(dc);
        }
    }
}
