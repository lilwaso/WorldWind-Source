/*
Copyright (C) 2001, 2008 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/

package gov.nasa.worldwind.event;

import gov.nasa.worldwind.*;

import java.awt.event.*;

/**
 * Provides an input handler that does nothing. Meant to serve as a NULL assignment that can be invoked.
 *
 * @author tag
 * @version $Id: NoOpInputHandler.java 15061 2011-03-23 16:46:33Z dcollins $
 */
public class NoOpInputHandler extends WWObjectImpl implements InputHandler
{
    public void setEventSource(WorldWindow newWorldWindow)
    {
    }

    public WorldWindow getEventSource()
    {
        return null;
    }

    public void setHoverDelay(int delay)
    {
    }

    public int getHoverDelay()
    {
        return 0;
    }

    public void addSelectListener(SelectListener listener)
    {
    }

    public void removeSelectListener(SelectListener listener)
    {
    }

    public void addKeyListener(KeyListener listener)
    {
    }

    public void removeKeyListener(KeyListener listener)
    {
    }

    public void addMouseListener(MouseListener listener)
    {
    }

    public void removeMouseListener(MouseListener listener)
    {
    }

    public void addMouseMotionListener(MouseMotionListener listener)
    {
    }

    public void removeMouseMotionListener(MouseMotionListener listener)
    {
    }

    public void addMouseWheelListener(MouseWheelListener listener)
    {
    }

    public void removeMouseWheelListener(MouseWheelListener listener)
    {
    }

    public void dispose()
    {
    }
}
