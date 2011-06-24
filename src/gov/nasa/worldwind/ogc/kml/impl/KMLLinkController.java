/*
 * Copyright (C) 2001, 2011 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.ogc.kml.impl;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.ogc.kml.*;
import gov.nasa.worldwind.util.Logging;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A controller to mark KML Links as updated based on the Link's refresh mode. The controller handles updating links
 * that refresh onInterval, onExpire, and onStop.
 * <p/>
 * The controller does not retrieve the resource that the link points to. It simply sets the {@code updateTime} of the
 * {@link KMLLink} to the current system time. Code that uses a resource retrieved by a KMLLink should maintain a
 * timestamp that indicates when the resource was retrieved, and periodically check this timestamp against time returned
 * by {@link gov.nasa.worldwind.ogc.kml.KMLLink#getUpdateTime()}.
 *
 * @author pabercrombie
 * @version $Id: KMLLinkController.java 15147 2011-03-29 21:00:42Z pabercrombie $
 */
// TODO The controller implements Disposable, but something needs to dispose of it.
public class KMLLinkController implements Disposable, RenderingListener
{
    public static final long VIEW_REFRESH_INTERVAL = 1000;

    /** WorldWindow that determines when to update links that when the view stops. */
    protected WorldWindow wwd;

    /**
     * The most recent View modelView ID.
     *
     * @see gov.nasa.worldwind.View#getModelviewStateID()
     */
    protected AtomicLong lastViewID = new AtomicLong();

    /** Time at which the view most recently changed. Measured in milliseconds since the Epoch. */
    protected AtomicLong lastViewChangeTime = new AtomicLong();

    /** Links managed by the controller. */
    protected List<KMLLink> links = new ArrayList<KMLLink>();

    /** Tasks that execute periodically to update links. These tasks must be cancelled when the controller is disposed. */
    protected List<ScheduledRefreshTask> periodicTasks = new ArrayList<ScheduledRefreshTask>();

    /** Task to periodically check for links that must refresh because the view has stopped. */
    protected ScheduledFuture viewRefreshTask;

    /** Create a new controller. */
    public KMLLinkController()
    {
    }

    /** Dispose of the controller and release its resources. */
    public void dispose()
    {
        if (this.wwd != null)
            this.wwd.removeRenderingListener(this);

        // Cancel all of the repeating tasks. Any one-shot tasks are allowed to complete, but the
        // repeating tasks would never stop if not cancelled here.
        for (ScheduledRefreshTask task : this.periodicTasks)
        {
            task.cancel();
        }

        if (this.viewRefreshTask != null)
            this.viewRefreshTask.cancel(false);

        this.periodicTasks.clear();
        this.links.clear();
    }

    /**
     * Returns the {@link WorldWindow} that the controller monitors to apply view-based updates.
     *
     * @return the WorldWindow that triggers link updates.
     */
    public WorldWindow getWorldWindow()
    {
        return this.wwd;
    }

    /**
     * Specifies the {@link View} that the controller monitors to apply view-based updates.
     *
     * @param wwd the WorldWindow that triggers will link updates when the view changes.
     */
    public void setWorldWindow(WorldWindow wwd)
    {
        if (this.wwd != null)
        {
            this.wwd.removeRenderingListener(this);
        }

        this.wwd = wwd;

        if (this.wwd != null)
        {
            this.wwd.addRenderingListener(this);
        }
    }

    /**
     * Add a {@link KMLLink} to the set of links that the controller manages. The controller will set the link's update
     * time to the current system time when the links needs to be refreshed.
     *
     * @param link Link to add.
     */
    public void addLink(KMLLink link)
    {
        if (link == null)
        {
            String msg = Logging.getMessage("nullValue.KMLLinkIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        String refreshMode = link.getRefreshMode();
        if (KMLConstants.ON_INTERVAL.equals(refreshMode))
        {
            Double interval = link.getRefreshInterval();
            if (interval != null)
            {
                long time = (long) interval.doubleValue();
                this.schedulePeriodicRefresh(link, time, TimeUnit.SECONDS);
            }
        }
        else if (KMLConstants.ON_EXPIRE.equals(refreshMode))
        {
            // TODO
        }

        String viewRefreshMode = link.getViewRefreshMode();
        if (KMLConstants.ON_STOP.equals(viewRefreshMode))
        {
            if (this.viewRefreshTask == null)
            {
                this.startViewRefreshTask(VIEW_REFRESH_INTERVAL);
            }
        }

        this.links.add(link);
    }

    /**
     * Remove a KMLLink from the set of Links that the controller manages.
     *
     * @param linkToRemove Link to remove.
     */
    public void removeLink(KMLLink linkToRemove)
    {
        if (linkToRemove == null)
        {
            String msg = Logging.getMessage("nullValue.KMLLinkIsNull");
            Logging.logger().severe(msg);
            throw new IllegalArgumentException(msg);
        }

        this.links.remove(linkToRemove);

        // Cancel any periodic tasks that are active for the removed link.
        Iterator<ScheduledRefreshTask> iterator = this.periodicTasks.iterator();
        while (iterator.hasNext())
        {
            ScheduledRefreshTask task = iterator.next();
            if (linkToRemove == task.link)
            {
                task.cancel();
                iterator.remove();
            }
        }
    }

    /**
     * Start the task that checks for links that must update because the view has stopped.
     *
     * @param period How frequently to check for links that must update. Units are milliseconds.
     */
    protected void startViewRefreshTask(long period)
    {
        this.viewRefreshTask = WorldWind.getScheduledTaskService()
            .addRepeatingTask(new ViewRefreshTask(), period, period, TimeUnit.MILLISECONDS);
    }

    /**
     * Schedule a task to periodically mark a link as updated.
     *
     * @param link     Link to refresh.
     * @param interval Refresh interval. The time unit is determined by {code timeUnit}.
     * @param timeUnit The time unit of {@code interval}.
     */
    protected void schedulePeriodicRefresh(KMLLink link, long interval, TimeUnit timeUnit)
    {
        ScheduledFuture future = WorldWind.getScheduledTaskService()
            .addRepeatingTask(new RefreshTask(link), interval, interval, timeUnit);

        // Keep track of the task so that it can be cancelled when the controller is disposed.
        if (future != null)
        {
            this.periodicTasks.add(new ScheduledRefreshTask(link, future));
        }
    }

    /**
     * Schedule a task to mark a link as updated after a delay. The task only executes once.
     *
     * @param link     Link to refresh.
     * @param delay    Delay to wait before executing the task. The time unit is determined by {code timeUnit}.
     * @param timeUnit The time unit of {@code delay}.
     */
    protected void scheduleDelayedRefresh(KMLLink link, long delay, TimeUnit timeUnit)
    {
        WorldWind.getScheduledTaskService().addScheduledTask(new RefreshTask(link), delay, timeUnit);
    }

    /** {@inheritDoc} */
    public void stageChanged(RenderingEvent event)
    {
        // Only check the viewID once per frame.
        if (RenderingEvent.BEFORE_RENDERING.equals(event.getStage()))
        {
            long viewId = this.getWorldWindow().getView().getModelviewStateID();

            // Capture the new viewID if the view has changed since the last frame.
            if (viewId != this.lastViewID.get())
            {
                this.lastViewID.set(viewId);
                this.lastViewChangeTime.set(System.currentTimeMillis());
            }
        }
    }

    /** A task to run periodically and check for links that must be refreshed because the view has stopped. */
    class ViewRefreshTask implements Runnable
    {
        /** The amount of the time (in milliseconds) that the view was stopped the last time that the task was executed. */
        protected long lastTimeSinceViewStopped;

        public void run()
        {
            long now = System.currentTimeMillis();
            long timeSinceViewStopped = now - lastViewChangeTime.get();

            // Don't do anything if the view has been stopped for less than a second. The minimum refresh time that KML
            // allows is one second.
            if (timeSinceViewStopped < 1000)
                return;

            if (timeSinceViewStopped < this.lastTimeSinceViewStopped)
                this.lastTimeSinceViewStopped = 0;

            // Check each link and see if it needs to update.
            for (KMLLink link : KMLLinkController.this.links)
            {
                String refreshMode = link.getViewRefreshMode();
                if (KMLConstants.ON_STOP.equals(refreshMode))
                {
                    Double refreshTime = link.getViewRefreshTime();
                    if (refreshTime != null)
                    {
                        long delay = (long) refreshTime.doubleValue() * 1000; // Convert seconds to milliseconds

                        // Update the link if the view has been stopped for the link's viewRefreshTime,
                        // but the previous view stopped time was less than the viewRefreshTime. The result is that the
                        // link is updated once, when the viewRefreshTime is exceeded, but not after that (unless the
                        // view moves and stops again).
                        if (timeSinceViewStopped >= delay && delay > this.lastTimeSinceViewStopped)
                        {
                            link.setUpdateTime(now);

                            // Trigger a repaint to cause the link to be refreshed.
                            link.getRoot().firePropertyChange(AVKey.REPAINT, null, null);
                        }
                    }
                }
            }

            this.lastTimeSinceViewStopped = timeSinceViewStopped;
        }
    }

    /** A Runnable task that marks a KMLLink as updated when the task executes. */
    class RefreshTask implements Runnable
    {
        /** Link to update. */
        protected KMLLink link;

        /**
         * Create a task to update a KMLLink. When the task executes, the link's updateTime will be set to the current
         * system time.
         *
         * @param link the KMLLink to update.
         */
        public RefreshTask(KMLLink link)
        {
            this.link = link;
        }

        /** Mark the link as updated. */
        public void run()
        {
            this.link.setUpdateTime(System.currentTimeMillis());

            // Trigger a repaint to cause the link to be refreshed.
            this.link.getRoot().firePropertyChange(AVKey.REPAINT, null, null);
        }
    }

    /** A scheduled instance of a refresh task. This object can be used to cancel a scheduled task. */
    class ScheduledRefreshTask
    {
        /** Link that is scheduled to refresh. */
        protected KMLLink link;
        /** Future that represents the scheduled task. */
        protected ScheduledFuture future;

        /**
         * Create a handle to a scheduled task.
         *
         * @param link   Link that is scheduled to refresh.
         * @param future Future that represents the scheduled task.
         */
        public ScheduledRefreshTask(KMLLink link, ScheduledFuture future)
        {
            this.link = link;
            this.future = future;
        }

        /** Cancel the task. */
        public void cancel()
        {
            this.future.cancel(false);
        }
    }
}
