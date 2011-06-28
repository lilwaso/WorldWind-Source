/*
 * Copyright (C) 2001, 2011 United States Government
 * as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */

package gov.nasa.worldwind.event;

/**
 * Listener for general purpose message events.
 *
 * @author pabercrombie
 * @version $Id: MessageListener.java 15608 2011-06-14 17:13:34Z pabercrombie $
 */
public interface MessageListener
{
    /**
     * Invoked when a message is received.
     *
     * @param msg The message that was received.
     */
    void onMessage(Message msg);
}
