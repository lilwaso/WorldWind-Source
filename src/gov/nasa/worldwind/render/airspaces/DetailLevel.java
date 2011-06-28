/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.render.airspaces;

import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.render.*;

/**
 * @author dcollins
 * @version $Id: DetailLevel.java 14654 2011-02-09 23:04:48Z ccrick $
 */
public interface DetailLevel extends Comparable<DetailLevel>, AVList
{
    boolean meetsCriteria(DrawContext dc, Airspace airspace);

    int compareTo(DetailLevel level);
}
