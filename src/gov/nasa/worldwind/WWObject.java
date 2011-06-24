/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind;

import gov.nasa.worldwind.avlist.AVList;

/**
 * An interface provided by the major World Wind components to provide attribute-value list management and
 * property change management. Classifies implementers as property-change listeners, allowing them to receive
 * property-change events.
 *
 * @author Tom Gaskins
 * @version $Id: WWObject.java 14310 2010-12-27 21:53:10Z pabercrombie $
 */
public interface WWObject extends AVList, java.beans.PropertyChangeListener
{
}
