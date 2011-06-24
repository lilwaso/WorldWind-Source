/*
Copyright (C) 2001, 2008 United States Government as represented by
the Administrator of the National Aeronautics and Space Administration.
All Rights Reserved.
*/
package gov.nasa.worldwind.applications.gio.gidb;

/**
 * @author dcollins
 * @version $Id: Server.java 5517 2008-07-15 23:36:34Z dcollins $
 */
public interface Server
{
    Text getTitle();

    void setTitle(Text title);

    Text getURL();

    void setURL(Text url);
}
